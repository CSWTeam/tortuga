package de.computerstudienwerkstatt.tortuga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import de.computerstudienwerkstatt.tortuga.model.user.Role;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;

import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    private LoggedInUserHolder loggedInUserHolder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findOneByLoginName(s);
        if(user == null) {
            throw new UsernameNotFoundException("Did not find user with username " + s);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getLoginName(),
                user.getPassword(),
                user.isActiveUser(),
                true,
                true,
                true,
                user.getRole().getAuthorities()
        );
    }

    public Optional<User> getLoggedInUser() {
        return loggedInUserHolder.getLoggedInUser();
    }

    public boolean isAdminAccount(String id) {
        User user = userRepository.findOne(id);
        if(user == null) {
            return false;
        }

        return user.getRole() == Role.ADMIN;
    }

    @SuppressWarnings("unused")
    public boolean canUserDelete(User subject, String objectId) {
        if(subject.getId().equals(objectId)) {
            return false;
        }

        if(subject.getRole() == Role.ADMIN) {
            return true;
        }

        if(subject.getRole() != Role.CSW_TEAM) {
            return false;
        }

        User object = userRepository.findOne(objectId);

        if(object == null) {
            return true;
        }

        if(object.getRole() == Role.ADMIN || object.getRole() == Role.CSW_TEAM) {
            return false;
        }

        return true;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setLoggedInUserHolder(LoggedInUserHolder loggedInUserHolder) {
        this.loggedInUserHolder = loggedInUserHolder;
    }
}
