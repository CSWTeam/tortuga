package de.computerstudienwerkstatt.tortuga.controller.api.reservation;

import de.computerstudienwerkstatt.tortuga.model.reservation.DeviceReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.controller.base.response.ForbiddenResponse;
import de.computerstudienwerkstatt.tortuga.controller.base.response.NotFoundResponse;
import de.computerstudienwerkstatt.tortuga.model.reservation.TimeSpan;
import de.computerstudienwerkstatt.tortuga.repository.reservation.DeviceReservationRepository;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;
import de.computerstudienwerkstatt.tortuga.service.door.DoorOpener;
import de.computerstudienwerkstatt.tortuga.util.NetworkUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + DeviceReservationController.API_BASE)
public class DeviceReservationController extends AbstractCRUDCtrl<DeviceReservation> {


    private static final Logger logger = LoggerFactory.getLogger(DeviceReservationController.class);

    public static final String API_BASE = "devicereservations";

    private LoggedInUserHolder loggedInUserHolder;

    private DoorOpener doorOpener;

    private DeviceReservationRepository deviceReservationRepository;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    @PostFilter("filterObject.user.id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public List<DeviceReservation> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PostAuthorize("returnObject.user.id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public DeviceReservation findOne(@PathVariable("id") String id) {
        return super.findOne(id);
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("#newEntity.user.id.equals(authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public ResponseEntity<DeviceReservation> post(@RequestBody DeviceReservation newEntity, HttpServletResponse response) {
        if(newEntity.getTimeSpan() != null && newEntity.getTimeSpan().getEnd() != null && newEntity.getTimeSpan().endIsInPast()) {
            throw new BadRequestResponse("Endzeitpunkt kann nicht in der Vergangenheit liegen");
        }

        newEntity.setUser(loggedInUserHolder.getLoggedInUser().orElseThrow(() -> new AssertionError("Spring Security should have prevented this")));

        return super.post(newEntity, response);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@possessedEntityPermissionElevator.checkOwner(@deviceReservationRepository, #id, authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public ResponseEntity delete(@PathVariable("id") String id) {
        return super.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("@possessedEntityPermissionElevator.checkOwner(@deviceReservationRepository, #id, authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public DeviceReservation patchDeviceReservation(@PathVariable("id") String id, @RequestBody ChangeSet<DeviceReservation> entity) {
        DeviceReservation old = repository.findOne(id);
        if(old == null) {
            throw new NotFoundResponse("Could not find DeviceReservation with id " + id);
        }

        if(entity.getPatch().getTimeSpan() != null && entity.getPatch().getTimeSpan().getEnd() != null && entity.getPatch().getTimeSpan().endIsInPast()) {
            throw new BadRequestResponse("Endzeitpunkt kann nicht in der Vergangenheit liegen");
        }

        Boolean oldBorrowed = old.isBorrowed() == null ? false : old.isBorrowed();

        Boolean newBorrowed = entity.getPatch().isBorrowed() == null ? false : entity.getPatch().isBorrowed();

        if(oldBorrowed != newBorrowed) {
            if(!NetworkUtil.isLocalNetworkRequest()) {
                logger.warn("NOT OPENING DOOR FOR DEVICE RESERVATION {} BECAUSE NOT LOCAL NETWORK", entity);

                throw new ForbiddenResponse("Geräte können nur an dem Terminal in der CSW ausgeliehen werden.");
            }
        }

        if(newBorrowed && !oldBorrowed) {
            List<DeviceReservation> reservations = deviceReservationRepository.findAllByDeviceIdAndBorrowed(old.getDevice().getId(), true);
            if(reservations.size() > 0) {
                throw new BadRequestResponse("Jemand anderes hat dieses Gerät noch nicht zurückgegeben.");
            }
        }

        DeviceReservation reservation = super.patch(id, entity);

        if(oldBorrowed != newBorrowed) {
            doorOpener.openCabinetDoor(reservation.getDevice().getCabinet());
            if(newBorrowed) {
                reservation.setBorrowedBeginning(new Date());
            } else {
                TimeSpan timeSpan = new TimeSpan();
                timeSpan.setBeginning(old.getBorrowedBeginning());
                timeSpan.setEnd(new Date());

                reservation.setBorrowedBeginning(null);
                reservation.getBorrowedTimeSpans().add(timeSpan);
            }

            repository.save(reservation);
        }

        return reservation;
    }

    @Override
    public String getApiBase() {
        return API_BASE;
    }

    @Autowired
    public void setLoggedInUserHolder(LoggedInUserHolder loggedInUserHolder) {
        this.loggedInUserHolder = loggedInUserHolder;
    }

    @Autowired
    public void setDoorOpener(DoorOpener doorOpener) {
        this.doorOpener = doorOpener;
    }

    @Autowired
    public void setDeviceReservationRepository(DeviceReservationRepository deviceReservationRepository) {
        this.deviceReservationRepository = deviceReservationRepository;
    }
}
