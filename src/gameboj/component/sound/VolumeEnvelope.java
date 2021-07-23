package gameboj.component.sound;

import static gameboj.GameBoy.CLOCK_FREQ;
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

    public boolean isEnabled() { return period > 0; }

    public void updateEnvelope(int data) {
        this.initialVolume = data >> 4;
        this.envelopeDirection = test(data, 3) ? Direction.INCR : Direction.DECR;
        this.period = data & 0x07;
    }

    public void start() {
        stopped = true;
        counter = 8192;
    }

    public void trigger() {
        volume = initialVolume;
        counter = 0;
        stopped = false;
    }

    public void clock() {
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

    public int getVolume() {
        if (isEnabled()) return volume;
        else return initialVolume;
    }
}
