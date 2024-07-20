package gameboj.component.apu;

import static gameboj.GameBoy.CLOCK_FREQ;
import static gameboj.bits.Bits.clip;
import static gameboj.bits.Bits.test;

public final class VolumeEnvelope {
    private enum Direction {
        INCR, DECR
    }

    private final static int DIVIDER = (int) CLOCK_FREQ / 64;
    private final static int MIN_VOLUME = 0;
    private final static int MAX_VOLUME = 15;

    private Direction envelopeDirection;
    private int initialVolume;
    private int period;
    private int volume;
    private int counter;
    private boolean stopped;

    private boolean isEnabled() { return period > 0; }

    void updateEnvelope(int data) {
        this.initialVolume = data >> 4;
        this.envelopeDirection = test(data, 3) ? Direction.INCR : Direction.DECR;
        this.period = clip(3, data);
    }

    public void start() {
        stopped = true;
        counter = DIVIDER / 8;
    }

    void trigger() {
        volume = initialVolume;
        counter = 0;
        stopped = false;
    }

    void clock() {
        if (stopped) return;
        if ((volume == MIN_VOLUME && envelopeDirection == Direction.DECR)
            || (volume == MAX_VOLUME && envelopeDirection == Direction.INCR)) {
                stopped = true;
                return;
        }
        if (++counter == period * DIVIDER) {
            counter = 0;
            volume += envelopeDirection == Direction.DECR ? -1 : 1;
        }
    }

    int getVolume() {
        if (isEnabled()) return volume;
        else return initialVolume;
    }
}
