package de.computerstudienwerkstatt.tortuga.model.reservation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Mischa Holz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BorrowedDeviceFromActiveCategoryValidator.class)
@Documented
@Inherited
public @interface BorrowedDeviceFromActiveCategory {

    String message() default "Das Gerät in einer aktiven Kategorie sein";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
