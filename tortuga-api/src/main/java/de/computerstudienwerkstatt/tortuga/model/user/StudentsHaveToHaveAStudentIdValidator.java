package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Mischa Holz
 */
public class StudentsHaveToHaveAStudentIdValidator implements ConstraintValidator<StudentsHaveToHaveAStudentId, User> {
    @Override
    public void initialize(StudentsHaveToHaveAStudentId constraintAnnotation) {

    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        if(value.getRole() == Role.STUDENT) {
            boolean valid = value.getStudentId().isPresent();
            if(!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Studenten müssen eine Matrikel Nummer angeben")
                        .addPropertyNode("studentId").addConstraintViolation();
            }

            return valid;
        }

        return true;
    }
}
