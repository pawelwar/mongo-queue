package wareq.mongoqueue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class SettableFixClock extends Clock {

    private Instant now = Instant.now();

    @Override
    public Instant instant() {
        return now;
    }

    public void moveForward(Duration duration) {
        this.now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }
}
