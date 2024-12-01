package de.computerstudienwerkstatt.tortuga;

import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.statistics.DoorAuthorisationAttemptRepository;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;

import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Service
public class MockLoggedInUserHolder implements LoggedInUserHolder {

    private User user = TestHelper.createLoginUser();

    private UserRepository userRepository;

    private DoorAuthorisationAttemptRepository doorAuthorisationAttemptRepository;

    @Override
    public Optional<User> getLoggedInUser() {
        if(user == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(userRepository.findOne(user.getId()));
    }

    public void forgetMe() {
        user = null;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUp() {
        doorAuthorisationAttemptRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        user = TestHelper.createLoginUser();

        user = userRepository.save(user);
    }

    public void tearDown() {
        doorAuthorisationAttemptRepository.deleteAllInBatch();

        if(user != null) {
            userRepository.delete(user);
        }

        userRepository.deleteAllInBatch();
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setDoorAuthorisationAttemptRepository(DoorAuthorisationAttemptRepository doorAuthorisationAttemptRepository) {
        this.doorAuthorisationAttemptRepository = doorAuthorisationAttemptRepository;
    }
}
