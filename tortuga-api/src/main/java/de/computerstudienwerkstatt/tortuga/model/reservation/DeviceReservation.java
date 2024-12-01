package de.computerstudienwerkstatt.tortuga.model.reservation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.computerstudienwerkstatt.tortuga.model.device.Device;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Mischa Holz
 */
@Entity
@DeviceReservationDoesNotOverlap
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class DeviceReservation extends Reservation<DeviceReservation> {

    @OneToOne
    @NotNull(message = "Jede Gerätereservierung braucht ein Gerät, das reserviert wird")
    @BorrowedDeviceFromActiveCategory
    private Device device;

    private Boolean borrowed;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "device_reservation_id"))
    private List<TimeSpan> borrowedTimeSpans = new ArrayList<>();

    private Date borrowedBeginning;

    public Boolean isBorrowed() {
        return borrowed;
    }

    public void setBorrowed(Boolean borrowed) {
        this.borrowed = borrowed;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @JsonIgnore
    public List<TimeSpan> getBorrowedTimeSpans() {
        return borrowedTimeSpans;
    }

    @JsonIgnore
    public void setBorrowedTimeSpans(List<TimeSpan> borrowedTimeSpans) {
        this.borrowedTimeSpans = borrowedTimeSpans;
    }

    @JsonIgnore
    public Date getBorrowedBeginning() {
        return borrowedBeginning;
    }

    @JsonIgnore
    public void setBorrowedBeginning(Date borrowedBeginning) {
        this.borrowedBeginning = borrowedBeginning;
    }

    @Override
    public boolean intersects(DeviceReservation other) {
        if(Objects.equals(this.device, other.device)) {
            return this.getTimeSpan().intersects(other.getTimeSpan());
        }
        return false;
    }
}
