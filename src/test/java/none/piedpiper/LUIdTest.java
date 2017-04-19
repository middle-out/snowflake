package com.github.middle-out;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by assaf on 04/01/2015.
 */
public class LUIdTest {
    @Test(expected=IllegalArgumentException.class)
    public void testConstructor1() throws Exception {
        new LUId(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructor2() throws Exception {
        new LUId(4096);
    }

    @Test
    public void nextLUId() throws Exception {
        long millis = DateTime.now().getMillis();
        DateTimeUtils.setCurrentMillisFixed(millis);

        LUId luid = new LUId(0);

        verifyValue(millis, 0, luid);
        verifyValue(millis, 1, luid);

        // move the clock
        ++millis;
        verifyValue(millis, 0, luid);
    }

    private void verifyValue(long ts, long currentSequence, LUId luid) {
        DateTimeUtils.setCurrentMillisFixed(ts);
        long res = luid.nextLUId();
        long expected = ((ts-1288834974657L) << 22) + currentSequence;

        assertEquals(expected, res);
    }

    @Test
    public void illegalClockReading() throws Exception {
        long millis = DateTime.now().getMillis();
        DateTimeUtils.setCurrentMillisFixed(millis);

        LUId luid = new LUId(15);

        long garbage = luid.nextLUId();

        DateTimeUtils.setCurrentMillisFixed(millis-1);

        boolean exceptionOccurred = false;
        try {
            garbage = luid.nextLUId();
        }
        catch (IllegalStateException e) {
            exceptionOccurred = true;
        }

        assertTrue(exceptionOccurred);
    }

    @Test
    public void testStaticTimeFilters() throws Exception {
        long millis = DateTime.now().getMillis();

        long res = LUId.getTimeFilter(millis);
        assertEquals(((millis-1288834974657L) << 22), res);

        long res2 = LUId.getTimeFilter(new Date(millis));

        assertEquals(res, res2);
    }
}
