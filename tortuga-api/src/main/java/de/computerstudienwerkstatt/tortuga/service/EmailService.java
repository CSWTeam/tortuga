package de.computerstudienwerkstatt.tortuga.service;

import com.sun.mail.imap.IMAPFolder;
import de.computerstudienwerkstatt.tortuga.repository.support.SupportMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import de.computerstudienwerkstatt.tortuga.model.support.SupportMessage;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author Mischa Holz
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private SimpleMailMessage template;

    public void sendEmail(String to, String subject, String body) {
        Thread thread = new Thread(() -> {
            logger.info("Starting to send email...");
            SimpleMailMessage message = new SimpleMailMessage(template);

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Sent email.");
        });

        thread.start();
    }

    @Scheduled(fixedRate = 10 * 60_000)
    public void pollForEmails() throws MessagingException, IOException {
        logger.info("Checking emails...");

        String imapHost = System.getenv("RMS_IMAP_HOST");
        if(imapHost == null) {
            return;
        }

        String imapUser = System.getenv("RMS_IMAP_USER");
        if(imapUser == null) {
            throw new RuntimeException("You have to set the RMS_IMAP_USER environment variable");
        }

        String imapPassword = System.getenv("RMS_IMAP_PASSWORD");
        if(imapPassword == null) {
            throw new RuntimeException("You have to set the RMS_IMAP_PASSWORD environment variable");
        }

        String imapPort = System.getenv("RMS_IMAP_PORT");
        if(imapPort == null) {
            throw new RuntimeException("You have to set the RMS_IMAP_PASSWORD environment variable");
        }

        Properties properties = new Properties();
        properties.setProperty("mail.debug", "false");
        properties.setProperty("mail.imap.starttls.enable", "true");
        properties.setProperty("mail.imap.port", imapPort);


        Session session = Session.getInstance(properties);
        Store store = session.getStore("imap");
        store.connect(imapHost, imapUser, imapPassword);

        IMAPFolder folder = (IMAPFolder) store.getFolder("inbox");
        folder.open(IMAPFolder.READ_ONLY);
        Message[] messages = folder.getMessages();

        FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add("subject");
        fetchProfile.add("body");
        fetchProfile.add("data");

        folder.fetch(messages, fetchProfile);

        int newMails = 0;

        for(Message message : messages) {
            String id = message.getHeader("Message-Id")[0];

            List<SupportMessage> supportMessagesWithId = supportMessageRepository.findByEmailId(id);
            if(!supportMessagesWithId.isEmpty()) {
                continue;
            }
            newMails++;

            String body = null;
            Object content = message.getContent();

            if(content instanceof MimeMultipart) {
                MimeMultipart multipart = ((MimeMultipart) message.getContent());

                int count = multipart.getCount();

                for(int i = 0; i < count; i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if(bodyPart.getContentType().toLowerCase().contains("text/plain")) {
                        body = (String) bodyPart.getContent();
                        break;
                    }
                }

                if(body == null) {
                    throw new AssertionError("Didn't find any part of this multipart message that is text/plain");
                }
            } else if(content instanceof String) {
                body = (String) content;
            } else {
                throw new AssertionError("Don't know how to deal with this message. Content type: " + message.getContent().getClass());
            }

            String subject = message.getSubject();
            Address[] replyAddresses = message.getReplyTo();
            String addresses = Stream.of(replyAddresses).map(Address::toString).reduce("", (a, b) -> a + ", " + b);

            Address[] fromAddresses = message.getFrom();
            if(fromAddresses.length == 0) {
                continue;
            }
            String from = fromAddresses[0].toString();

            Date receivedDate = message.getReceivedDate();
            if(receivedDate == null) {
                receivedDate = new Date();
            }


            SupportMessage supportMessage = new SupportMessage();
            supportMessage.setOpenedAt(receivedDate);
            supportMessage.setSubject(subject);
            supportMessage.setName(Optional.of(from));
            supportMessage.setEmail(Optional.of(addresses));
            supportMessage.setAnswer(Optional.empty());
            supportMessage.setBody(body);
            supportMessage.setDone(false);
            supportMessage.setEmailId(id);

            supportMessageRepository.save(supportMessage);
        }

        logger.info("Imported {} new emails", newMails);
    }

}
