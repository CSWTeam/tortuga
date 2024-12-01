package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsersHaveUniqueLoginNamesValidator.class)
@Documented
@Inherited
public @interface UsersHaveUniqueLoginNames {

    String message() default "Dieser Benutzername wird bereits benutzt.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
