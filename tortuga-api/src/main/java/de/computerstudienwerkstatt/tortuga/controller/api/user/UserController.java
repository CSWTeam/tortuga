package de.computerstudienwerkstatt.tortuga.controller.api.user;

import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.response.NotFoundResponse;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.ForbiddenResponse;
import de.computerstudienwerkstatt.tortuga.model.user.Role;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;
import de.computerstudienwerkstatt.tortuga.service.PasscodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + UserController.USER_API_BASE)
public class UserController extends AbstractCRUDCtrl<User> {

    public final static String USER_API_BASE = "users";

    @Override
    public String getApiBase() {
        return USER_API_BASE;
    }

    private LoggedInUserHolder loggedInUserHolder;

    private PasscodeService passcodeService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @RequestMapping(method = RequestMethod.GET)
    @PostFilter("filterObject.role != T(de.computerstudienwerkstatt.tortuga.model.user.Role).DELETED && (filterObject.id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM'))")
    public List<User> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PostAuthorize("returnObject.id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public User findOne(@PathVariable("id") String id) {
        User user = super.findOne(id);
        if(user.getRole() == Role.DELETED) {
            throw new NotFoundResponse();
        }
        return user;
    }

    @RequestMapping(value = "/{id}/passcode", method = RequestMethod.POST)
    @PreAuthorize("#id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public Object generatePasscode(@PathVariable("id") String id) throws InterruptedException {
        Thread.sleep(500);

        User user = repository.findOne(id);
        if(user == null) {
            throw new NotFoundResponse("Didn't find user");
        }

        List<String> passcodeList = passcodeService.generateRandomPasscode();
        String passcodeStr = passcodeList.stream().reduce("", (a, b) -> a + b);
        user.setPasscode(Optional.of(passcodeStr));

        repository.save(user);

        return new Object() {
            public List<String> passcode = passcodeList;

            public List<String> getPasscode() {
                return passcode;
            }
        };
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("(hasAuthority('OP_TEAM') && #user.getRole() != T(de.computerstudienwerkstatt.tortuga.model.user.Role).ADMIN) || hasAuthority('OP_ADMIN')")
    public ResponseEntity<User> post(@RequestBody User user, HttpServletResponse response) {
        if(user.getRole() == Role.STUDENT) {
            Date expires = User.calculateNextSemesterEnd(new Date());
            user.setExpirationDate(Optional.of(expires));
        }

        if(user.getRole() == Role.ADMIN) {
            user.setExpirationDate(Optional.empty());
        }

        return super.post(user, response);
    }

    @Override
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@userService.canUserDelete(authentication.getDetails(), #id)")
	public ResponseEntity delete(@PathVariable("id") String id) {
        User user = repository.findOne(id);

        if(user == null) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }

        user.setRole(Role.DELETED);
        user.setPassword(UUID.randomUUID().toString());
        user.setPhoneNumber("0");
        user.setExpirationDate(Optional.empty());
        user.setStudentId(Optional.empty());
        user.setPasscode(Optional.empty());
        user.setEmail("deleted-" + UUID.randomUUID().toString() + "@csw.de");
        user.setEnabled(false);
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setMajor(Optional.empty());
        user.setLoginName("deleted-" + UUID.randomUUID().toString());

        repository.save(user);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("#id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public User patch(@PathVariable("id") String id, @RequestBody ChangeSet<User> user, HttpServletRequest request) {
        User beforeUpdate = repository.findOne(id);
        if(beforeUpdate == null) {
            throw new NotFoundResponse("Can't find user");
        }

        if(!loggedInUserHolder.getLoggedInUser().isPresent()) {
            throw new ForbiddenResponse("Du musst eingeloggt zu sein");
        }
        User loggedInUser = loggedInUserHolder.getLoggedInUser().get();

        if(id.equals(loggedInUser.getId()) && user.getPatchedFields().contains("password")) {
            String oldPassword = request.getHeader("X-Old-Password");
            if(oldPassword == null || !passwordEncoder.matches(oldPassword, loggedInUser.getPassword())) {
                throw new ForbiddenResponse("Das angegebene Passwort war falsch.");
            }
        }


        if(beforeUpdate.getRole() == Role.STUDENT && (user.getPatch().getRole() == null || user.getPatch().getRole() == Role.STUDENT)) {
            if(!user.getPatch().getExpirationDate().isPresent()) {
                Date expires = beforeUpdate.getExpirationDate().orElse(User.calculateNextSemesterEnd(new Date()));
                user.getPatch().setExpirationDate(Optional.of(expires));
            }
        }

        if(user.getPatch().getRole() == Role.STUDENT && (!beforeUpdate.getExpirationDate().isPresent())) {
            Date expires = User.calculateNextSemesterEnd(new Date());
            user.getPatch().setExpirationDate(Optional.of(expires));
        }

        if(beforeUpdate.getRole() != Role.STUDENT && user.getPatch().getRole() != Role.STUDENT) {
            user.getPatch().setExpirationDate(Optional.empty());
        }

        return super.patch(id, user);
    }

    @Autowired
    public void setLoggedInUserHolder(LoggedInUserHolder loggedInUserHolder) {
        this.loggedInUserHolder = loggedInUserHolder;
    }

    @Autowired
    public void setPasscodeService(PasscodeService passcodeService) {
        this.passcodeService = passcodeService;
    }

}
