package de.computerstudienwerkstatt.tortuga.security;

import de.computerstudienwerkstatt.tortuga.model.user.User;

import java.util.Optional;

/**
 * @author Mischa Holz
 */
public interface LoggedInUserHolder {

    Optional<User> getLoggedInUser();

}
