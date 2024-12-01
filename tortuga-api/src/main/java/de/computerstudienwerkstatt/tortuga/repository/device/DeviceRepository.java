package de.computerstudienwerkstatt.tortuga.repository.device;

import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Mischa Holz
 */
@Repository
public interface DeviceRepository extends JpaSpecificationRepository<Device, String> {
    List<Device> findByCategoryId(String categoryId);
}
