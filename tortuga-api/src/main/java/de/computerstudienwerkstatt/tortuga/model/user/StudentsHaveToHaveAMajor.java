package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StudentsHaveToHaveAMajorValidator.class)
@Documented
@Inherited
public @interface StudentsHaveToHaveAMajor {

    String message() default "Studenten müssen einen Studiengang angeben.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
