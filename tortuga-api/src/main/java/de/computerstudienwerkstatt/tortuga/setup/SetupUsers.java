package de.computerstudienwerkstatt.tortuga.setup;

import de.computerstudienwerkstatt.tortuga.model.user.Role;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Component
public class SetupUsers implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SetupUsers.class);

    private UserRepository userRepository;

    @Override
    public void run(String... strings) throws Exception {
        if(userRepository.count() == 0) {
            logger.info("Creating default user since no users are present");

            User user = new User();

            user.setExpirationDate(Optional.empty());
            user.setLoginName("admin");
            user.setRole(Role.ADMIN);
            user.setEmail("bp@ilu.st");
            user.setFirstName("Ilu");
            user.setLastName("St");
            user.setGender(Optional.empty());
            user.setMajor(Optional.empty());
            user.setPassword("change me.");
            user.setStudentId(Optional.empty());
            user.setPhoneNumber("123456789");
            user.setEnabled(true);

            userRepository.save(user);

            logger.info("done");
        }
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
