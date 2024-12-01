package de.computerstudienwerkstatt.tortuga.controller.api.reservation;

import de.computerstudienwerkstatt.tortuga.Main;
import de.computerstudienwerkstatt.tortuga.controller.base.AbstractCRUDCtrl;
import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.controller.base.response.NotFoundResponse;
import de.computerstudienwerkstatt.tortuga.model.reservation.RoomReservation;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.controller.base.response.ForbiddenResponse;
import de.computerstudienwerkstatt.tortuga.model.base.IdGenerator;
import de.computerstudienwerkstatt.tortuga.model.reservation.TimeSpan;
import de.computerstudienwerkstatt.tortuga.util.NetworkUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/" + RoomReservationController.API_BASE)
public class RoomReservationController extends AbstractCRUDCtrl<RoomReservation> {

    private static final Logger logger = LoggerFactory.getLogger(RoomReservationController.class);

    public static final String API_BASE = "roomreservations";

    private UserService userService;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    @PostFilter("(filterObject.approved != null && filterObject.approved) || hasAuthority('OP_TEAM') || filterObject.user.id.equals(authentication.getPrincipal())")
    public List<RoomReservation> findAll(HttpServletRequest request) {
        return super.findAll(request);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PostAuthorize("(returnObject.approved != null && returnObject.approved) || hasAuthority('OP_TEAM')")
    public RoomReservation findOne(@PathVariable("id") String id) {
        return super.findOne(id);
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('OP_LECTURER')")
    public ResponseEntity<RoomReservation> post(@RequestBody RoomReservation newEntity, HttpServletResponse response) {
        if(newEntity.getTimeSpan() != null && newEntity.getTimeSpan().getEnd() != null && newEntity.getTimeSpan().endIsInPast()) {
            throw new BadRequestResponse("Endzeitpunkt kann nicht in der Vergangenheit liegen");
        }

        User user = userService.getLoggedInUser().orElseThrow(() -> new AssertionError("Spring security should not have executed this"));
        newEntity.setUser(user);

        newEntity.setApproved(false);
        newEntity.setOpen(false);

        if(newEntity.getRepeatOption().isPresent()) {
            if(!newEntity.getRepeatUntil().isPresent()) {
                throw new BadRequestResponse("if you specify a repeat option you also have to specify an end date");
            }

            String sharedId = IdGenerator.generate();
            newEntity.setSharedId(Optional.of(sharedId));

            long length = newEntity.getTimeSpan().getEnd().getTime() - newEntity.getTimeSpan().getBeginning().getTime();

            List<Date> beginningDates = newEntity.getRepeatOption().get().calculateDates(newEntity.getTimeSpan().getBeginning(), newEntity.getRepeatUntil().get());

            List<RoomReservation> reservations = beginningDates.stream().map(d -> {
                Date endDate = new Date(d.getTime() + length);

                TimeSpan timeSpan = new TimeSpan(d, endDate);

                RoomReservation roomReservation = new RoomReservation();
                roomReservation.setSharedId(Optional.of(sharedId));
                roomReservation.setUser(newEntity.getUser());
                roomReservation.setApproved(false);
                roomReservation.setOpen(false);
                roomReservation.setRepeatUntil(Optional.empty());
                roomReservation.setRepeatOption(Optional.empty());
                roomReservation.setTimeSpan(timeSpan);
                roomReservation.setTitle(newEntity.getTitle());

                return roomReservation;
            }).collect(Collectors.toList());

            RoomReservation master = reservations.stream().filter(r -> r.getTimeSpan().getBeginning().equals(newEntity.getTimeSpan().getBeginning())).findAny().get();
            master.setRepeatUntil(newEntity.getRepeatUntil());
            master.setRepeatOption(newEntity.getRepeatOption());

            reservations = repository.save(reservations);

            response.setHeader(HttpHeaders.LOCATION, Main.getApiBase() + "/" + getApiBase() + "/" + master.getId());

            return new ResponseEntity<>(master, HttpStatus.CREATED);
        } else {
            newEntity.setRepeatOption(Optional.empty());
            newEntity.setRepeatUntil(Optional.empty());
            newEntity.setSharedId(Optional.empty());
        }

        return super.post(newEntity, response);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@possessedEntityPermissionElevator.checkOwner(@roomReservationRepository, #id, authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public ResponseEntity delete(@PathVariable("id") String id) {
        return super.delete(id);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("@possessedEntityPermissionElevator.checkOwner(@roomReservationRepository, #id, authentication.getPrincipal()) || hasAuthority('OP_TEAM')")
    public RoomReservation patch(@PathVariable("id") String id, @RequestBody ChangeSet<RoomReservation> entity) {
        RoomReservation old = repository.findOne(id);

        if(old == null) {
            throw new NotFoundResponse("Could not find RoomReservation with id " + id);
        }

        Boolean oldOpened = old.isOpen() == null ? false : old.isOpen();

        Boolean newOpened = entity.getPatch().isOpen() == null ? false : entity.getPatch().isOpen();

        if(!oldOpened && newOpened && !NetworkUtil.isLocalNetworkRequest()) {
            logger.warn("NOT OPENING ROOM RESERVATION {} BECAUSE NOT LOCAL NETWORK", entity);

            throw new ForbiddenResponse();
        }

        return super.patch(id, entity);
    }

    @Override
    public String getApiBase() {
        return API_BASE;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
