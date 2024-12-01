package de.computerstudienwerkstatt.tortuga.repository.device;

import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;
import org.springframework.stereotype.Repository;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

/**
 * @author Mischa Holz
 */
@Repository
public interface DeviceCategoryRepository extends JpaSpecificationRepository<DeviceCategory, String> {
}
