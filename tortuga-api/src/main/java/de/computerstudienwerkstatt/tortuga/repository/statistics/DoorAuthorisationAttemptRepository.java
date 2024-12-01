package de.computerstudienwerkstatt.tortuga.repository.statistics;

import de.computerstudienwerkstatt.tortuga.model.statistics.AuthType;
import de.computerstudienwerkstatt.tortuga.model.statistics.DoorAuthorisationAttempt;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Repository
public interface DoorAuthorisationAttemptRepository extends JpaSpecificationRepository<DoorAuthorisationAttempt, String> {

    default void logSuccessful(AuthType authType, Optional<User> user) {
        DoorAuthorisationAttempt ret = DoorAuthorisationAttempt.successful(authType, user);
        this.save(ret);
    }

    default void logUnsuccessful(AuthType authType, Optional<User> user) {
        DoorAuthorisationAttempt ret = DoorAuthorisationAttempt.unsuccessful(authType, user);
        this.save(ret);
    }

}
