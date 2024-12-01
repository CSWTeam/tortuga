package de.computerstudienwerkstatt.tortuga.model.reservation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeviceReservationValidator.class)
@Documented
@Inherited
public @interface DeviceReservationDoesNotOverlap {

    String message() default "Das Gerät ist in dem ausgewählten Zeitraum bereits reserviert.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
