package de.computerstudienwerkstatt.tortuga.service.door;

import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;

/**
 * @author Mischa Holz
 */
public interface DoorOpener {

    void openCabinetDoor(Cabinet cabinet);

    void openRoomDoor();

    void openRoomDoorWithoutCheckingNetwork();

}
