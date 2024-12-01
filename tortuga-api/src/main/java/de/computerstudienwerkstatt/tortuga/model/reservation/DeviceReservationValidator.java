package de.computerstudienwerkstatt.tortuga.model.reservation;

import de.computerstudienwerkstatt.tortuga.repository.reservation.DeviceReservationRepository;
import de.computerstudienwerkstatt.tortuga.util.SpringInjectedValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Component
public class DeviceReservationValidator extends SpringInjectedValidator<DeviceReservationDoesNotOverlap, DeviceReservation> {

    @Autowired
    private DeviceReservationRepository deviceReservationRepository;

    @Override
    protected boolean _isValid(DeviceReservation value, ConstraintValidatorContext context) {
        List<DeviceReservation> reservations = deviceReservationRepository.findAllByDeviceId(value.getDevice().getId());
        Optional<DeviceReservation> reservation = reservations.stream()
                .filter(r -> !r.getId().equals(value.getId()))
                .filter(r -> r.getTimeSpan().intersects(value.getTimeSpan()))
                .findAny();

        if(reservation.isPresent()) {
            return false;
        }

        long diff = value.getTimeSpan().getEnd().getTime() - value.getTimeSpan().getBeginning().getTime();


        return true;
    }
}
