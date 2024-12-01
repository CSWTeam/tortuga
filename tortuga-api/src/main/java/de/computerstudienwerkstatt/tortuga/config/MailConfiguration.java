package de.computerstudienwerkstatt.tortuga.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

/**
 * @author Mischa Holz
 */
@Configuration
public class MailConfiguration {

    private static Logger logger = LoggerFactory.getLogger(MailConfiguration.class);

    private static class DummyMailSender implements MailSender {

        @Override
        public void send(SimpleMailMessage simpleMailMessage) throws MailException {
            logger.info("DUMMY MAILER SENT MESSAGE");
        }

        @Override
        public void send(SimpleMailMessage... simpleMailMessages) throws MailException {
            logger.info("DUMMY MAILER SENT MESSAGES");
        }
    }

    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        String smtpHost = System.getenv("RMS_SMTP_HOST");
        if(smtpHost == null) {
            logger.info("Using dummy mail sender");
            return new DummyMailSender();
        }
        String smtpPort = System.getenv("RMS_SMTP_PORT");
        if(smtpPort == null) {
            logger.info("Using dummy mail sender");
            return new DummyMailSender();
        }
        String smtpUser = System.getenv("RMS_SMTP_USER");
        if(smtpUser == null) {
            logger.info("Using dummy mail sender");
            return new DummyMailSender();
        }
        String smtpPassword = System.getenv("RMS_SMTP_PASSWORD");
        if(smtpPassword == null) {
            logger.info("Using dummy mail sender");
            return new DummyMailSender();
        }

        mailSender.setHost(smtpHost);
        mailSender.setPassword(smtpPassword);
        mailSender.setUsername(smtpUser);
        mailSender.setPort(Integer.parseInt(smtpPort));

        Properties properties = new Properties();
        properties.setProperty("mail.debug", "false");
        properties.setProperty("mail.smtp.host", smtpHost);
        properties.setProperty("mail.smtp.socketFactory.port", smtpPort);
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        mailSender.setSession(session);

        return mailSender;
    }

    @Bean
    public SimpleMailMessage templateMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        String from = System.getenv("RMS_SMTP_FROM");
        if(from == null) {
            from = "RMS <rms@example.org>";
        }

        simpleMailMessage.setFrom(from);

        return simpleMailMessage;
    }

}
