package de.computerstudienwerkstatt.tortuga.model.reservation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeSpanValidator.class)
@Documented
@Inherited
public @interface TimeSpanIsValid {

    String message() default "Die Zeitspanne muss sich innerhalb eines Tages befinden.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
