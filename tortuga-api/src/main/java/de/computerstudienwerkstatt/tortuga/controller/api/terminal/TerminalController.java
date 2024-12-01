package de.computerstudienwerkstatt.tortuga.controller.api.terminal;

import de.computerstudienwerkstatt.tortuga.model.statistics.AuthType;
import de.computerstudienwerkstatt.tortuga.model.terminal.OpenDoorRequest;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import de.computerstudienwerkstatt.tortuga.repository.reservation.RoomReservationRepository;
import de.computerstudienwerkstatt.tortuga.repository.statistics.DoorAuthorisationAttemptRepository;
import de.computerstudienwerkstatt.tortuga.security.LoggedInUserHolder;
import de.computerstudienwerkstatt.tortuga.service.PasscodeService;
import de.computerstudienwerkstatt.tortuga.service.TimedTokenService;
import de.computerstudienwerkstatt.tortuga.service.door.DoorOpener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@RestController
@RequestMapping("/api/v1/terminal")
public class TerminalController {

    private static final Logger logger = LoggerFactory.getLogger(TerminalController.class);

    private PasscodeService passcodeService;

    private DoorOpener doorOpener;

    private RoomReservationRepository roomReservationRepository;

    private TimedTokenService timedTokenService;

    private LoggedInUserHolder loggedInUserHolder;

    private DoorAuthorisationAttemptRepository attemptLogger;

    @RequestMapping(value = "/door", method = RequestMethod.PATCH)
    public ResponseEntity<Void> openDoorWithOpenRoomReservation(
            @RequestParam(value = "token", required = false) Long token,
            @RequestParam(value = "passcode", required = false) String passcode,
            @RequestBody OpenDoorRequest openDoorRequest) {
        if(!openDoorRequest.getOpen()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        logger.info("Received door opening request...");

        if(passcode != null) {
            Optional<User> user = passcodeService.getUserFromPasscode(passcode);
            if(!user.isPresent()) {
                logger.info("Wrong passcode entered");

                attemptLogger.logUnsuccessful(AuthType.EMOJIS, Optional.empty());

                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            logger.info("DOOR: Opening with passcode");

            attemptLogger.logSuccessful(AuthType.EMOJIS, user);

            doorOpener.openRoomDoorWithoutCheckingNetwork();

            //doorOpener.openRoomDoor();

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        if(token != null) {
            logger.info("Using token to authenticate");

            Optional<User> user = loggedInUserHolder.getLoggedInUser();
            if(!user.isPresent()) {
                logger.info("User is not logged in");

                attemptLogger.logUnsuccessful(AuthType.QR_CODE, Optional.empty());

                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if(timedTokenService.isValidToken(token)) {
                logger.info("DOOR: Opening with token");
                doorOpener.openRoomDoorWithoutCheckingNetwork();

                attemptLogger.logSuccessful(AuthType.QR_CODE, user);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            logger.info("Token is invalid");

            attemptLogger.logUnsuccessful(AuthType.QR_CODE, user);

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if(roomReservationRepository.findByApprovedAndOpen(true, true).stream().filter(
                r -> r.getOpenedTimeSpan().isCurrent())
                .findAny()
                .isPresent()) {

            logger.info("DOOR: Opening with open room reservation");

            attemptLogger.logSuccessful(AuthType.ROOM_RESERVATION, Optional.empty());

            doorOpener.openRoomDoorWithoutCheckingNetwork();
            //doorOpener.openRoomDoor();

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        attemptLogger.logUnsuccessful(AuthType.EMOJIS, Optional.empty());

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public ResponseEntity<Long> getCurrentDoorOpenCode(HttpServletRequest request) {
        //TODO MAKE PROPER FIX
        //if(!NetworkUtil.isLocalNetworkRequest(request)) {
        //    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        //}

        long currentCode = timedTokenService.getCurrentToken();

        return new ResponseEntity<>(currentCode, HttpStatus.OK);
    }

    @Autowired
    public void setPasscodeService(PasscodeService passcodeService) {
        this.passcodeService = passcodeService;
    }

    @Autowired
    public void setDoorOpener(DoorOpener doorOpener) {
        this.doorOpener = doorOpener;
    }

    @Autowired
    public void setRoomReservationRepository(RoomReservationRepository roomReservationRepository) {
        this.roomReservationRepository = roomReservationRepository;
    }

    @Autowired
    public void setTimedTokenService(TimedTokenService timedTokenService) {
        this.timedTokenService = timedTokenService;
    }

    @Autowired
    public void setLoggedInUserHolder(@SuppressWarnings("SpringJavaAutowiringInspection") LoggedInUserHolder loggedInUserHolder) {
        this.loggedInUserHolder = loggedInUserHolder;
    }

    @Autowired
    public void setDoorAuthorisationAttemptRepository(DoorAuthorisationAttemptRepository doorAuthorisationAttemptRepository) {
        this.attemptLogger = doorAuthorisationAttemptRepository;
    }
}
