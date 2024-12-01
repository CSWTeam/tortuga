package de.computerstudienwerkstatt.tortuga.model.user;

import de.computerstudienwerkstatt.tortuga.repository.user.UserRepository;
import de.computerstudienwerkstatt.tortuga.util.SpringInjectedValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;

/**
 * @author Mischa Holz
 */
@Component
public class UsersHaveUniqueLoginNamesValidator extends SpringInjectedValidator<UsersHaveUniqueLoginNames, User> {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean _isValid(User value, ConstraintValidatorContext context) {
        User candidate = userRepository.findOneByLoginName(value.getLoginName());

        if(candidate == null) {
            return true;
        }

        boolean valid = candidate.getId().equals(value.getId());
        if(!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Dieser Loginname wird bereits von einem anderen Benutzer verwendet.")
                    .addPropertyNode("loginName").addConstraintViolation();
        }

        return valid;
    }
}
