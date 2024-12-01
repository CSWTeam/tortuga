package de.computerstudienwerkstatt.tortuga.model.statistics;

import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;
import de.computerstudienwerkstatt.tortuga.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Entity
public class DoorAuthorisationAttempt extends PersistentEntity {

    public static DoorAuthorisationAttempt successful(AuthType authType, Optional<User> user) {
        return build(authType, user, true);
    }

    public static DoorAuthorisationAttempt unsuccessful(AuthType authType, Optional<User> user) {
        return build(authType, user, false);
    }

    private static DoorAuthorisationAttempt build(AuthType authType, Optional<User> user, Boolean successful) {
        DoorAuthorisationAttempt ret = new DoorAuthorisationAttempt();
        ret.setAuthType(authType);
        ret.setUser(user);
        ret.setTime(new Date());
        ret.setSuccessful(successful);
        return ret;
    }

    @NotNull
    private Date time;

    @OneToOne
    @Access(AccessType.FIELD)
    private User user;

    @NotNull
    private Boolean successful;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AuthType authType;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    public void setUser(Optional<User> user) {
        this.user = user.orElse(null);
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }
}
