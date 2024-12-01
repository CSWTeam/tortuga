package de.computerstudienwerkstatt.tortuga.model.reservation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author Mischa Holz
 */
public class TimeSpanValidator implements ConstraintValidator<TimeSpanIsValid, TimeSpan> {
    @Override
    public void initialize(TimeSpanIsValid constraintAnnotation) {

    }

    @Override
    public boolean isValid(TimeSpan value, ConstraintValidatorContext context) {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        ZoneOffset zoneOffset = zdt.getOffset();

        long beginningSeconds = value.getBeginning().getTime() / 1000;
        long endSeconds = value.getEnd().getTime() / 1000;

        LocalDateTime beginning = LocalDateTime.ofEpochSecond(beginningSeconds, 0, zoneOffset);
        LocalDateTime end = LocalDateTime.ofEpochSecond(endSeconds, 0, zoneOffset);

        int beginningDayOfYear = beginning.getDayOfYear();
        int endDayOfYear = end.getDayOfYear();

        return beginningDayOfYear == endDayOfYear;
    }
}
