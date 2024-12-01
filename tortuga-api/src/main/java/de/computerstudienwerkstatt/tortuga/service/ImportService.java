package de.computerstudienwerkstatt.tortuga.service;

import de.computerstudienwerkstatt.tortuga.model.major.Major;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import de.computerstudienwerkstatt.tortuga.model.base.IdGenerator;
import de.computerstudienwerkstatt.tortuga.model.user.Gender;
import de.computerstudienwerkstatt.tortuga.model.user.Role;
import de.computerstudienwerkstatt.tortuga.repository.user.MajorRepository;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Mischa Holz
 */
@RestController
public class ImportService {

    private static Logger logger = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private EmailService emailService;

    // "id","username","password","pin","amount","prename","surname","enrolmentNumber","phone","email","rightId","createdAt","validUntil","gender","birthday","semester","degreeId","hint","archiveId","picture"
    private static final int POS_LOGIN_NAME = 1;
    private static final int POS_FIRST_NAME = 5;
    private static final int POS_LAST_NAME = 6;
    private static final int POS_STUDENT_ID = 7;
    private static final int POS_PHONE = 8;
    private static final int POS_EMAIL = 9;
    private static final int POS_RIGHT_ID = 10;
    private static final int POS_EXPIRATION_DATE = 12;
    private static final int POS_GENDER = 13;
    private static final int POS_DEGREE_ID = 16;
    private static final int POS_ARCHIVE_ID = 18;

    @RequestMapping(value = "/api/v1/import/csvFile", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('OP_ADMIN')")
    public ResponseEntity<String> handleFileUpload(@RequestParam(value = "emailText", required = false) String emailText, @RequestParam("file") MultipartFile file) throws IOException {
        if(emailText != null && !emailText.isEmpty()) {
            importUsersFromCsvFile(file.getInputStream(), Optional.of(emailText));
        } else {
            importUsersFromCsvFile(file.getInputStream(), Optional.empty());
        }

        return new ResponseEntity<>("Import completed", HttpStatus.OK);
    }

    public void importUsersFromCsvFile(InputStream csvFile, Optional<String> emailText) {
        logger.warn("DELETING ALL USERS AND MAJORS");

        userRepository.deleteAllInBatch();
        majorRepository.deleteAllInBatch();

        Map<Integer, Major> majors = new HashMap<>();

        Major lagStudent = new Major();
        lagStudent.setName("LAG-Student");
        majors.put(1, majorRepository.save(lagStudent));

        Major educationBachelor = new Major();
        educationBachelor.setName("B. Ed.");
        majors.put(2, majorRepository.save(educationBachelor));

        Major bachelorOfArtsPead = new Major();
        bachelorOfArtsPead.setName("B. A. Pädagogik");
        majors.put(3, majorRepository.save(bachelorOfArtsPead));

        Major masterOfArtsBild = new Major();
        masterOfArtsBild.setName("M. A. Bildungswissenschaften");
        majors.put(4, majorRepository.save(masterOfArtsBild));

        Major masterOfEducation = new Major();
        masterOfEducation.setName("M. Ed.");
        majors.put(15, majorRepository.save(masterOfEducation));


        new BufferedReader(new InputStreamReader(csvFile)).lines().skip(1)
                .map(l -> l.split("§", -1))
                .map(lines -> {
                    for(int i = 0; i < lines.length; i++) {
                        if(lines[i].startsWith("\"")) {
                            lines[i] = lines[i].substring(1, lines[i].length() - 1);
                        }
                    }

                    return lines;
                })
                .map(lines -> {
                    User user = new User();

                    user.setLoginName(lines[POS_LOGIN_NAME]);
                    user.setFirstName(lines[POS_FIRST_NAME]);
                    user.setLastName(lines[POS_LAST_NAME]);
                    user.setEmail(lines[POS_EMAIL]);
                    user.setPhoneNumber(lines[POS_PHONE]);

                    String password = IdGenerator.generate().substring(0, 8);
                    user.setPassword(password);

                    Optional<Gender> gender = Optional.empty();
                    if("Männlich".equals(lines[POS_GENDER])) {
                        gender = Optional.of(Gender.MALE);
                    } else if("Weiblich".equals(lines[POS_GENDER])) {
                        gender = Optional.of(Gender.FEMALE);
                    }
                    user.setGender(gender);

                    if("NULL".equals(lines[POS_ARCHIVE_ID])) {
                        user.setEnabled(true);
                    } else {
                        user.setEnabled(false);
                    }

                    Integer rightsId = Integer.parseInt(lines[POS_RIGHT_ID]);

                    switch(rightsId) {
                        case 0: {
                            user.setRole(Role.STUDENT);

                            Integer majorId = Integer.parseInt(lines[POS_DEGREE_ID]);
                            user.setMajor(Optional.of(majors.get(majorId)));
                            user.setStudentId(Optional.of(lines[POS_STUDENT_ID]));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date validUntil = sdf.parse(lines[POS_EXPIRATION_DATE]);

                                Date expirationDate = User.calculateNextSemesterEnd(validUntil);
                                user.setExpirationDate(Optional.of(expirationDate));
                            } catch(ParseException e) {
                                throw new RuntimeException(e);
                            }

                            break;
                        }
                        case 1: {
                            user.setRole(Role.LECTURER);
                            user.setMajor(Optional.empty());
                            user.setStudentId(Optional.empty());
                            user.setExpirationDate(Optional.empty());
                            break;
                        }
                        case 2: {
                            user.setRole(Role.CSW_TEAM);
                            user.setMajor(Optional.empty());
                            user.setStudentId(Optional.empty());
                            user.setExpirationDate(Optional.empty());
                            break;
                        }
                        case 3: {
                            user.setRole(Role.ADMIN);
                            user.setMajor(Optional.empty());
                            user.setStudentId(Optional.empty());
                            user.setExpirationDate(Optional.empty());
                            break;
                        }
                        default: {
                            throw new AssertionError("You forgot to add something here");
                        }
                    }

                    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                    Validator validator = factory.getValidator();
                    Set<ConstraintViolation<User>> errors = validator.validate(user);
                    if(errors.size() != 0) {
                        logger.warn("Could not import user {}!", lines[POS_LOGIN_NAME]);
                        for(ConstraintViolation<User> error : errors) {
                            logger.warn("\t{}: {}", error.getPropertyPath().toString(), error.getMessage());
                        }

                        return null;
                    }

                    return new UserPasswordPair(user, password);
                })
                .filter(u -> u != null)
                .forEach(userPasswordPair -> {
                    if(emailText.isPresent()) {
                        String body = emailText.get().replace("$FIRSTNAME", userPasswordPair.user.getFirstName());
                        body = body.replace("$LASTNAME", userPasswordPair.user.getLastName());
                        body = body.replace("$PASSWORD", userPasswordPair.password);

                        String subject = "Start des neuen CSW Systems";

                        emailService.sendEmail(userPasswordPair.user.getEmail(), subject, body);
                    }


                    logger.info("Saving user {}", userPasswordPair.user.getLoginName());

                    userRepository.save(userPasswordPair.user);
                });

        logger.info("IMPORT COMPLETE");
    }

    private static class UserPasswordPair {
        public User user;
        public String password;

        public UserPasswordPair(User user, String password) {
            this.user = user;
            this.password = password;
        }
    }

}
