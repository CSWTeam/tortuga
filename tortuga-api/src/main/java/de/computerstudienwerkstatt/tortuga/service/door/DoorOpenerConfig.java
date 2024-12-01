package de.computerstudienwerkstatt.tortuga.service.door;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;

/**
 * @author Mischa Holz
 */
@Configuration
public class DoorOpenerConfig {

    private static Logger logger = LoggerFactory.getLogger(DoorOpener.class);

    @Bean
    public static DoorOpener doorOpener(@Value("${RMS_SSH_DOOR_HOST:NO_HOST}") String sshHost,
                                        @Value("${RMS_SSH_DOOR_USER:NO_USER}") String sshUser,
                                        @Value("${RMS_SSH_DOOR_PASSWORD:NO_PASSWORD}") String sshPassword,
                                        @Value("${RMS_SSH_FINGER_PRINT:PRINT}") String fingerPrint) {
        if(sshHost != null && !sshHost.equals("NO_HOST")) {
            return new SSHDoorOpener(sshHost, sshUser, sshPassword, fingerPrint);
        } else {
            return new DoorOpener() {
                @Override
                public void openCabinetDoor(Cabinet cabinet) {
                    logger.warn("OPENING DOOR FOR {} (FAKE)", cabinet);
                }

                @Override
                public void openRoomDoor() {
                    logger.warn("OPENING ROOM DOOR (FAKE)");
                }

                @Override
                public void openRoomDoorWithoutCheckingNetwork() {
                    logger.warn("OPENING ROOM DOOR (FAKE)");
                }
            };
        }
    }

}
