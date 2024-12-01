package de.computerstudienwerkstatt.tortuga;

import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.model.reservation.DeviceReservation;
import de.computerstudienwerkstatt.tortuga.model.reservation.RoomReservation;
import de.computerstudienwerkstatt.tortuga.model.reservation.TimeSpan;
import de.computerstudienwerkstatt.tortuga.model.user.User;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mischa Holz
 */
public class ReservationTests {

    private static User user;

    private RoomReservation makeRoomReservation(TimeSpan timeSpan) {
        RoomReservation reservation = new RoomReservation();
        reservation.setTimeSpan(timeSpan);
        reservation.setUser(user);
        reservation.setApproved(false);
        reservation.setTitle("I want the room for stuff");

        return reservation;
    }

    private DeviceReservation makeDeviceReservation(Device device, TimeSpan timeSpan) {
        DeviceReservation reservation = new DeviceReservation();
        reservation.setTimeSpan(timeSpan);
        reservation.setUser(user);
        reservation.setDevice(device);

        return reservation;
    }

    @BeforeClass
    public static void setUp() {
        user = TestHelper.createUser();
    }

    @Test
    public void testIntersectionOfSameReservation() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));

        RoomReservation reservation = makeRoomReservation(one);

        assertTrue("The same reservation needs to intersect with itself", reservation.intersects(reservation));
    }

    @Test
    public void testIntersectionOfReservations() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(150), new Date(250));

        RoomReservation rOne = makeRoomReservation(one);
        RoomReservation rTwo = makeRoomReservation(two);

        assertTrue("These two reservations need to intersect", rOne.intersects(rTwo));

        assertTrue("These two reservations need to intersect", rTwo.intersects(rOne));
    }

    @Test
    public void testOtherIntersectionOfReservations() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(50), new Date(150));

        RoomReservation rOne = makeRoomReservation(one);
        RoomReservation rTwo = makeRoomReservation(two);

        assertTrue("These two reservations need to intersect", rOne.intersects(rTwo));

        assertTrue("These two reservations need to intersect", rTwo.intersects(rOne));
    }

    @Test
    public void testSuperPositionOfReservations() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(50), new Date(250));

        RoomReservation rOne = makeRoomReservation(one);
        RoomReservation rTwo = makeRoomReservation(two);

        assertTrue("These two reservations need to intersect", rOne.intersects(rTwo));

        assertTrue("These two reservations need to intersect", rTwo.intersects(rOne));
    }

    @Test
    public void testReservationsNotIntersectingBecauseDifferentTime() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(201), new Date(250));

        RoomReservation rOne = makeRoomReservation(one);
        RoomReservation rTwo = makeRoomReservation(two);

        assertFalse("These two reservations should not intersect", rOne.intersects(rTwo));

        assertFalse("These two reservations should not intersect", rTwo.intersects(rOne));
    }

    @Test
    public void testReservationsNotIntersectingBecauseDifferentObjects() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(100), new Date(200));

        DeviceReservation rOne = makeDeviceReservation(new Device(), one);
        DeviceReservation rTwo = makeDeviceReservation(new Device(), two);

        assertFalse("These two reservations should not intersect", rOne.intersects(rTwo));

        assertFalse("These two reservations should not intersect", rTwo.intersects(rOne));
    }

    @Test
    public void testSuperPositionOfReservationsAndSameDevice() {
        TimeSpan one = new TimeSpan(new Date(100), new Date(200));
        TimeSpan two = new TimeSpan(new Date(50), new Date(250));

        Device device = new Device();

        DeviceReservation rOne = makeDeviceReservation(device, one);
        DeviceReservation rTwo = makeDeviceReservation(device, two);

        assertTrue("These two reservations need to intersect", rOne.intersects(rTwo));

        assertTrue("These two reservations need to intersect", rTwo.intersects(rOne));
    }
}
