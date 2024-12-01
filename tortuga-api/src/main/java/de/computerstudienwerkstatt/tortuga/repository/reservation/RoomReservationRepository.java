package de.computerstudienwerkstatt.tortuga.repository.reservation;

import de.computerstudienwerkstatt.tortuga.model.reservation.RoomReservation;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

import java.util.List;

/**
 * @author Mischa Holz
 */
@Repository
public interface RoomReservationRepository extends JpaSpecificationRepository<RoomReservation, String> {

    List<RoomReservation> findByApprovedAndOpen(@Param("approved") Boolean approved, @Param("open") Boolean open);

}
