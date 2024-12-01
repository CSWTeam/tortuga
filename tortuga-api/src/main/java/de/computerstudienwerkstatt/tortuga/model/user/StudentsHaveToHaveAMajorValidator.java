package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Mischa Holz
 */
public class StudentsHaveToHaveAMajorValidator implements ConstraintValidator<StudentsHaveToHaveAMajor, User> {
    @Override
    public void initialize(StudentsHaveToHaveAMajor constraintAnnotation) {

    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        if(value.getRole() == Role.STUDENT) {
            boolean valid = value.getMajor().isPresent();
            if(!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Studenten müssen einen Studiengang angeben")
                        .addPropertyNode("major").addConstraintViolation();
            }

            return valid;
        }

        return true;
    }
}
