package de.computerstudienwerkstatt.tortuga;

import de.computerstudienwerkstatt.tortuga.model.major.Major;
import de.computerstudienwerkstatt.tortuga.model.cabinet.Cabinet;
import de.computerstudienwerkstatt.tortuga.model.device.Device;
import de.computerstudienwerkstatt.tortuga.model.devicecategory.DeviceCategory;
import de.computerstudienwerkstatt.tortuga.model.user.Gender;
import de.computerstudienwerkstatt.tortuga.model.user.Role;
import de.computerstudienwerkstatt.tortuga.model.user.User;

import java.util.Date;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
public class TestHelper {

    private static long start;

    static {
        start = System.currentTimeMillis();
    }

    public static User createLoginUser() {
        User user = createUser();
        user.setLoginName("eirungirungvije");
        user.setEmail("someothermail@ilu.st");
        user.setEnabled(true);
        return user;
    }

    public static Major createMajor() {
        Major major = new Major();
        major.setName("some major");
        return major;
    }

    public static User createUser() {
        User user = new User();
        user.setExpirationDate(Optional.empty());
        user.setPhoneNumber("123456789");
        user.setRole(Role.ADMIN);
        user.setFirstName("Admin");
        user.setLastName("Admington");
        user.setGender(Optional.of(Gender.FEMALE));
        user.setStudentId(Optional.empty());
        user.setMajor(Optional.empty());
        user.setEmail("admin@ilu.st");
        user.setLoginName("admin");
        user.setPassword("change me.");
        user.setEnabled(true);
        return user;
    }

    public static Device createDevice(DeviceCategory deviceCategory) {
        Device device = new Device();
        device.setInventoryNumber("inv number");
        device.setDescription("beschreibung");
        device.setCategory(deviceCategory);
        device.setName("name");
        device.setAccessories("");
        device.setAcquisitionDate(Optional.empty());
        device.setCabinet(Cabinet.CABINET_6);

        return device;
    }

    public static Device createOtherDevice(DeviceCategory deviceCategory) {
        Device device = new Device();
        device.setInventoryNumber("other number");
        device.setDescription("andere beschreibung");
        device.setCategory(deviceCategory);
        device.setName("anderer name");
        device.setAccessories("");
        device.setAcquisitionDate(Optional.empty());
        device.setCabinet(Cabinet.CABINET_7);

        return device;
    }

    public static DeviceCategory createDeviceCategory() {
        DeviceCategory deviceCategory = new DeviceCategory();
        deviceCategory.setActive(true);
        deviceCategory.setName("Kategorie");

        return deviceCategory;
    }

    public static Date getDate() {
        return getDate(0);
    }

    public static Date getDate(long offset) {
        return new Date(start + 200_000_000L + offset);
    }

    public static Date getDateInPast(long offset) {
        return new Date(start - 200_000_000L + offset);
    }

}
