package de.computerstudienwerkstatt.tortuga;

import org.junit.Test;
import de.computerstudienwerkstatt.tortuga.model.reservation.RepeatOption;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Mischa Holz
 */
public class RepeatOptionTest {

    @Test
    public void testCalculateDates() throws Exception {
        List<Date> dateList = RepeatOption.WEEKLY.calculateDates(new Date(0), new Date(3 * 7 * 24 * 60 * 60 * 1000 + 5));

        assertTrue(dateList.size() == 4);

        assertTrue(dateList.get(0).getTime() == 0);

        //noinspection PointlessArithmeticExpression
        assertTrue(dateList.get(1).getTime() == 1 * 7 * 24 * 60 * 60 * 1000);
        assertTrue(dateList.get(2).getTime() == 2 * 7 * 24 * 60 * 60 * 1000);
        assertTrue(dateList.get(3).getTime() == 3 * 7 * 24 * 60 * 60 * 1000);
    }
}
