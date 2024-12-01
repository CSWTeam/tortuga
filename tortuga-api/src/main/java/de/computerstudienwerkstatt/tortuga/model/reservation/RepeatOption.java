package de.computerstudienwerkstatt.tortuga.model.reservation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Mischa Holz
 */
public enum RepeatOption {

    WEEKLY(1),
    BIWEEKLY(2),
    TRIWEEKLY(3),
    QUADWEEKLY(4);

    private final int weekDiff;

    RepeatOption(int weekDiff) {
        this.weekDiff = weekDiff;
    }

    public List<Date> calculateDates(Date startDate, Date endRepeatDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        List<Date> ret = new ArrayList<>();
        ret.add(startDate);

        while(true) {
            calendar.set(Calendar.WEEK_OF_YEAR, currentWeek + weekDiff);

            Date date = calendar.getTime();
            if(date.after(endRepeatDate)) {
                break;
            }

            ret.add(date);

            currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        }

        return ret;
    }
}
