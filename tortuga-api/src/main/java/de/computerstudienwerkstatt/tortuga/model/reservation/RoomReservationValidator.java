package de.computerstudienwerkstatt.tortuga.model.reservation;

import de.computerstudienwerkstatt.tortuga.repository.reservation.RoomReservationRepository;
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
public class RoomReservationValidator extends SpringInjectedValidator<RoomReservationDoesNotIntersect, RoomReservation> {

    @Autowired
    private RoomReservationRepository roomReservationRepository;

    @Override
    protected boolean _isValid(RoomReservation value, ConstraintValidatorContext context) {
        List<RoomReservation> reservations = roomReservationRepository.findAll();
        Optional<RoomReservation> reservation = reservations.stream()
                .filter(r -> !r.getId().equals(value.getId()))
                .filter(RoomReservation::isApproved)
                .filter(r -> r.getTimeSpan().intersects(value.getTimeSpan()))
                .findAny();

        if(reservation.isPresent()) {
            return false;
        }

        return true;
    }
}
