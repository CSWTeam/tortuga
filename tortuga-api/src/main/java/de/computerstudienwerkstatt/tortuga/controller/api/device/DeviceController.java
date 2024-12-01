package de.computerstudienwerkstatt.tortuga.controller.api.device;

import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.model.reservation.DeviceReservation;
import de.computerstudienwerkstatt.tortuga.model.reservation.TimeSpan;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.device.DeviceRepository;
import de.computerstudienwerkstatt.tortuga.repository.reservation.DeviceReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + DeviceController.API_BASE)
public class DeviceController extends AbstractCRUDCtrl<Device> {

    public static final String API_BASE = "devices";

    private DeviceReservationRepository deviceReservationRepository;

    private LoggedInUserHolder loggedInUserHolder;

    private DeviceRepository deviceRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Device> findAll(HttpServletRequest request,
                                @RequestParam(value = "beginningTime", required = false) Long beginningTime,
                                @RequestParam(value = "endTime", required = false) Long endTime,
                                @RequestParam(value = "category", required = false) String categoryId) {
        if(beginningTime == null && endTime == null) {
            return super.findAll(request);
        } else if(beginningTime != null && endTime != null && categoryId != null) {
            return suggestDevice(beginningTime, endTime, categoryId);
        } else {
            throw new BadRequestResponse("Um Geräte vorgeschlagen zu bekommen müssen beginningTime, endTime und category Parameter vorhanden sein");
        }
    }

    private List<Device> suggestDevice(Long beginningTime, Long endTime, String categoryId) {
        TimeSpan timeSpan = new TimeSpan(new Date(beginningTime), new Date(endTime));

        User user = loggedInUserHolder.getLoggedInUser().orElseThrow(() -> new RuntimeException("Not logged in"));

        List<DeviceReservation> reservations = deviceReservationRepository.findAllByUserIdAndDeviceCategoryId(user.getId(), categoryId, new Sort(Sort.Direction.DESC, "timeSpan.end"));

        List<Device> devicesFromCategory = deviceRepository.findByCategoryId(categoryId);
        if(reservations.size() == 0) {
            return filterAvailableDevices(timeSpan, devicesFromCategory);
        }

        List<Device> suggestions = Stream.concat(reservations.stream().map(DeviceReservation::getDevice), devicesFromCategory.stream())
                .distinct()
                .collect(Collectors.toList());

        return filterAvailableDevices(timeSpan, suggestions);
    }

    private List<Device> filterAvailableDevices(TimeSpan timeSpan, List<Device> devices) {
        List<Device> ret = new ArrayList<>();

        for(Device device : devices) {
            List<DeviceReservation> reservations = deviceReservationRepository.findAllByDeviceId(device.getId());

            boolean conflicts = false;

            for(DeviceReservation reservation : reservations) {
                if(reservation.getTimeSpan().intersects(timeSpan)) {
                    conflicts = true;
                }
            }

            if(!conflicts) {
                ret.add(device);
            }
        }

        return ret;
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Device findOne(@PathVariable("id") String id) {
        return super.findOne(id);
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public ResponseEntity<Device> post(@RequestBody Device newEntity, HttpServletResponse response) {
        return super.post(newEntity, response);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public ResponseEntity delete(@PathVariable("id") String id) {
        List<DeviceReservation> futureReservations =
                deviceReservationRepository.findAllByDeviceIdAndTimeSpanBeginningGreaterThan(id, new Date());
        if(futureReservations.size() > 0) {
            throw new BadRequestResponse("Dieses Gerät hat noch Reservierungen, die in der Zukunft liegen. Diese müssen gelöscht werden.");
        }

        List<DeviceReservation> oldReservations =
                deviceReservationRepository.findAllByDeviceIdAndTimeSpanEndLessThan(id, new Date());

        deviceReservationRepository.delete(oldReservations);

        return super.delete(id);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("hasAuthority('OP_TEAM')")
    public Device patch(@PathVariable("id") String id, @RequestBody ChangeSet<Device> entity) {
        return super.patch(id, entity);
    }

    @Autowired
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Autowired
    public void setDeviceReservationRepository(DeviceReservationRepository deviceReservationRepository) {
        this.deviceReservationRepository = deviceReservationRepository;
    }

    @Autowired
    public void setLoggedInUserHolder(LoggedInUserHolder loggedInUserHolder) {
        this.loggedInUserHolder = loggedInUserHolder;
    }

    @Override
    public String getApiBase() {
        return API_BASE;
    }
}
