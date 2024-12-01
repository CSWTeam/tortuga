package de.computerstudienwerkstatt.tortuga.model.reservation;

import de.computerstudienwerkstatt.tortuga.model.device.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Mischa Holz
 */
public class BorrowedDeviceFromActiveCategoryValidator implements ConstraintValidator<BorrowedDeviceFromActiveCategory, Device> {
    @Override
    public void initialize(BorrowedDeviceFromActiveCategory constraintAnnotation) {

    }

    @Override
    public boolean isValid(Device value, ConstraintValidatorContext context) {
        return value.getCategory().isActive();
    }
}
