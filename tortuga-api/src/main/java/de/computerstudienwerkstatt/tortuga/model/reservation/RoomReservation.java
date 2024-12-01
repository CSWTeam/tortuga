package de.computerstudienwerkstatt.tortuga.model.reservation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import java.util.Date;
import java.util.Optional;

/**
 * @author Mischa Holz
 */
@Entity
@RoomReservationDoesNotIntersect
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RoomReservation extends Reservation<RoomReservation> {

    private static final long OPEN_EXPAND_MILLIS = 15 * 60 * 1000;

    private Boolean approved;

    @NotEmpty(message = "Raumbuchungen brauchen einen Titel")
    private String title;

    private Boolean open;

    @Access(AccessType.FIELD)
    private RepeatOption repeatOption;

    @Access(AccessType.FIELD)
    private Date repeatUntil;

    @Access(AccessType.FIELD)
    private String sharedId;

    public Boolean isOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public TimeSpan getOpenedTimeSpan() {
        if(this.getTimeSpan() == null) {
            return null;
        }

        return this.getTimeSpan().expand(OPEN_EXPAND_MILLIS);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @JsonProperty("sharedId")
    public Optional<String> getSharedId() {
        return Optional.ofNullable(sharedId);
    }

    @JsonIgnore
    public void setSharedId(Optional<String> sharedId) {
        this.sharedId = sharedId.orElse(null);
    }

    public Optional<Date> getRepeatUntil() {
        return Optional.ofNullable(repeatUntil);
    }

    public void setRepeatUntil(Optional<Date> repeatUntil) {
        this.repeatUntil = repeatUntil.orElse(null);
    }

    public Optional<RepeatOption> getRepeatOption() {
        return Optional.ofNullable(repeatOption);
    }

    public void setRepeatOption(Optional<RepeatOption> repeatOption) {
        this.repeatOption = repeatOption.orElse(null);
    }

    @Override
    public boolean intersects(RoomReservation other) {
        return this.getTimeSpan().intersects(other.getTimeSpan());
    }
}
