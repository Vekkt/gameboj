package gameboj.component.apu;


import static gameboj.GameBoy.CLOCK_FREQ;
import static gameboj.bits.Bits.test;

public class LengthCounter {
    private final static int DIVIDER = (int) CLOCK_FREQ / 256;

    protected final int fullLength;
    private int length;
    private int counter;
    private boolean enabled;

    public LengthCounter(int fullLength) {
        this.fullLength = fullLength;
    }

    public void start() { counter = DIVIDER / 2; }

    public void clock() {
        if (++counter == DIVIDER) {
            counter = 0;
            if (enabled && length > 0) {
                length--;
            }
        }
    }

    public void trigger(int data) {
        boolean enable = test(data, 6);
        boolean trigger = test(data, 7);

        if (enabled && (length == 0 && trigger)) {
            if (enable && counter < DIVIDER / 2) {
                setLength(fullLength - 1);
            } else {
                setLength(fullLength);
            }
        } else if (enable) {
            if (length > 0 && counter < DIVIDER / 2) {
                length--;
            }
            if (length == 0 && trigger && counter < DIVIDER / 2) {
                setLength(fullLength - 1);
            }
        } else if (length == 0 && trigger) {
            setLength(fullLength);
        }
        enabled = enable;
    }

    public void setLength(int length) {
        this.length = (length == 0) ? fullLength : length;
    }

    public int length() { return length; }

    public boolean isEnabled() { return enabled; }
}
