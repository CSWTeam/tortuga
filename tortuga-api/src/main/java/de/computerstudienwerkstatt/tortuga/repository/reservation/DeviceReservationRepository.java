package de.computerstudienwerkstatt.tortuga.repository.reservation;

import de.computerstudienwerkstatt.tortuga.model.reservation.DeviceReservation;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Mischa Holz
 */
@Repository
public interface DeviceReservationRepository extends JpaSpecificationRepository<DeviceReservation, String> {

    List<DeviceReservation> findAllByDeviceId(@Param("id") String id);

    List<DeviceReservation> findAllByUserId(@Param("id") String id, Sort sort);

    List<DeviceReservation> findAllByUserIdAndDeviceCategoryId(@Param("userId") String userId, @Param("deviceCategoryId") String deviceCategoryId, Sort sort);

    List<DeviceReservation> findAllByDeviceIdAndBorrowed(@Param("deviceId") String deviceId, @Param("borrowed") Boolean borrowed);

    List<DeviceReservation> findAllByDeviceIdAndTimeSpanEndLessThan(String deviceId, Date date);

    List<DeviceReservation> findAllByDeviceIdAndTimeSpanBeginningGreaterThan(String deviceId, Date date);
}
