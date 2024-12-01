package de.computerstudienwerkstatt.tortuga.model.reservation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Mischa Holz
 */
@Embeddable
public class TimeSpan {

    @Column(name = "beginning_timestamp")
    @NotNull(message = "Zeiträume brauchen einen Zeitstempel für den Anfang")
    private Date beginning;

    @Column(name = "end_timestamp")
    @NotNull(message = "Zeiträume brauchen einen Zeitstempel für das Ende")
    private Date end;

    public TimeSpan() {
    }

    public TimeSpan(Date beginning, Date end) {
        this.beginning = beginning;
        this.end = end;
    }

    public Date getBeginning() {
        return beginning;
    }

    public void setBeginning(Date beginning) {
        this.beginning = beginning;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean intersects(TimeSpan other) {
        TimeSpan one = this;
        TimeSpan two = other;

        return checkIntersectInOneDirection(one, two) || checkIntersectInOneDirection(two, one);
    }

    public TimeSpan expand(long millis) {
        return new TimeSpan(new Date(this.getBeginning().getTime() - millis), new Date(this.getEnd().getTime() + millis));
    }

    private boolean checkIntersectInOneDirection(TimeSpan one, TimeSpan two) {
        long unixStartOne = one.beginning.getTime();
        long unixEndOne = one.end.getTime();

        long unixStartTwo = two.beginning.getTime();
        long unixEndTwo = two.end.getTime();

        if(unixStartOne >= unixStartTwo && unixStartOne <= unixEndTwo) {
            return true;
        }

        if(unixEndOne <= unixEndTwo && unixEndOne >= unixStartTwo) {
            return true;
        }

        return false;
    }

    @JsonIgnore
    public boolean endIsInPast() {
        return new Date().getTime() > getEnd().getTime();
    }

    public boolean isCurrent() {
        return intersects(new TimeSpan(new Date(), new Date()));
    }
}
