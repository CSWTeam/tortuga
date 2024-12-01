package de.computerstudienwerkstatt.tortuga.controller.api.device;

import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + DeviceCategoryController.API_BASE)
public class DeviceCategoryController extends AbstractCRUDCtrl<DeviceCategory> {

    public static final String API_BASE = "devicecategories";

    private DeviceRepository deviceRepository;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public List<DeviceCategory> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DeviceCategory findOne(@PathVariable String id) {
        return super.findOne(id);
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public ResponseEntity<DeviceCategory> post(@RequestBody DeviceCategory newEntity, HttpServletResponse response) {
        return super.post(newEntity, response);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public ResponseEntity delete(@PathVariable String id) {
        List<Device> devices = deviceRepository.findByCategoryId(id);
        if(devices.size() > 0) {
            throw new BadRequestResponse("Diese Gerätekategorie beinhaltet noch Geräte, die zunächst gelöscht werden müssen.");
        }

        return super.delete(id);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public DeviceCategory patch(@PathVariable String id, @RequestBody ChangeSet<DeviceCategory> entity) {
        return super.patch(id, entity);
    }

    @Override
    public String getApiBase() {
        return API_BASE;
    }

    @Autowired
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
}
