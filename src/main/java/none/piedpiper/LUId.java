package none.piedpiper;

import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by assaf on 04/01/2015.
 *
 * Long Unique Identifier generator in java
 * inspired by a blog post by twitter about their scala snowflake generator
 *
 * date bits 41 (can probably change this to 33 bits and still cover dates till 2100, but if 12 bits of IDs per milli are enough for twitter, they're probably good enough for everyone else)
 * worker bits 10
 * sequence bits 12
 */
public class LUId {
    private static final long TWITTER_EPOCH = 1288834974657L; // found out about this later, since this is based on twitter's post I've added this too ;)

    private static final int WORKER_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final int TIMESTAMP_SHIFT_BITS = 22;
    private static final int MAX_WORKER_ID = new BigInteger("2").pow(WORKER_BITS).intValue();
    private static final int MAX_SEQUENCE_NUMBER = new BigInteger("2").pow(SEQUENCE_BITS).intValue();

    private volatile long lastTimestamp = 0;
    private volatile long sequence = 0;
    private long workerId = 1;

    public LUId(int workerId) {
        if (workerId < 0 || MAX_WORKER_ID < workerId) {
            throw new IllegalArgumentException(String.format("worker id [%d] is out of range", workerId));
        }
        this.workerId = workerId;
    }

    public synchronized long nextLUId() {
        long currentTimestamp = getCurrentTimestamp();

        // this may happen with stressed virtual machines where the clocked is synced backwards with the host
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("current system clock is behind previous measurement");
        }

        if (lastTimestamp == currentTimestamp) {
            ++sequence;
            if (sequence >= MAX_SEQUENCE_NUMBER) {
                while (lastTimestamp == currentTimestamp) {
                    currentTimestamp = getCurrentTimestamp();
                }
                sequence = 0;
            }
        }
        else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp << TIMESTAMP_SHIFT_BITS) | (workerId << SEQUENCE_BITS) | sequence);
    }

    private static long getCurrentTimestamp() {
        return DateTime.now().getMillis()-TWITTER_EPOCH;
    }

    public static long getTimeFilter(Date dt) {
        return getTimeFilter(dt.getTime());
    }

    public static long getTimeFilter(long dt) {
        return ((dt-TWITTER_EPOCH) << TIMESTAMP_SHIFT_BITS);
    }
}
