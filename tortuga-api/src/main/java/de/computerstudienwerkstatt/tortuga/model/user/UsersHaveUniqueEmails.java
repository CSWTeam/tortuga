package de.computerstudienwerkstatt.tortuga.model.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsersHaveUniqueEmailsValidator.class)
@Documented
@Inherited
public @interface UsersHaveUniqueEmails {

    String message() default "Diese Email Adresse wird bereits von einem anderen Benutzer verwendet.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
