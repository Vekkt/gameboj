package gameboj.component.sound;


import static gameboj.GameBoy.CLOCK_FREQ;
import static gameboj.bits.Bits.test;

public class LengthCounter {
    private final static int DIVIDER = (int) CLOCK_FREQ / 256;

    private final int fullLength;
    private int length;
    private int counter;
    private boolean enabled;

    public LengthCounter(int fulLength) {
        this.fullLength = fulLength;
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

        if (trigger && length == 0) setLength(fullLength);
        enabled = enable;
    }

    public void setLength(int length) {
        this.length = (length == 0) ? fullLength : length;
    }

    public int length() { return length; }

    public boolean isEnabled() { return enabled; }
}
