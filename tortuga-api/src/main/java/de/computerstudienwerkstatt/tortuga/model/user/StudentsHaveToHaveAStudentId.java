package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StudentsHaveToHaveAStudentIdValidator.class)
@Documented
@Inherited
public @interface StudentsHaveToHaveAStudentId {

    String message() default "Studenten müssen eine Matrikelnummer haben.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
