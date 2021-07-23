package gameboj.component.sound;

import static gameboj.GameBoy.CLOCK_FREQ;
import static gameboj.bits.Bits.*;
import static gameboj.component.sound.Apu.ChannelType;

public final class Sweep extends Square {
    private final static int DIVIDER = (int) CLOCK_FREQ / 128;

    private boolean counterEnabled;
    private boolean isIncrementing;
    private boolean overflow;
    private boolean negate;

    private int shadowFrequency;
    private int sweepPeriod;
    private int counter;
    private int timer;
    private int shift;

    public Sweep() {
        super(ChannelType.SQUARE_A);
    }

    @Override
    public void write(int address, int data) {
        if (regStartAddress <= address && address < regEndAddress) {
            super.write(address, data);
            Reg reg = Reg.values()[address - regStartAddress];
            switch (reg) {
                case NR0:
                    sweepPeriod = extract(data, 5, 3);
                    negate = test(data, 3);
                    shift = clip(3, data);
                    if (isIncrementing && !negate) overflow = true;
                    break;
                case NR4:
                    if (test(data, 7)) triggerSweep();
                    break;
                default:
            }
        }
    }

    // Check reading at NR4

    @Override
    public void start() {
        super.start();
        startSweep();
    }

    @Override
    public int clock() {
        envelope.clock();
        if (!updateSweep()) return 0;
        return reallyClock();
    }

    public void startSweep() {
        counterEnabled = false;
        counter = 8192;
    }

    public void clockSweep() {
        if (++counter == DIVIDER) {
            counter = 0;
            if (counterEnabled && --timer == 0) {
                timer = sweepPeriod == 0 ? 8 : sweepPeriod;
                if (sweepPeriod != 0) {
                    int newFreq = updateShadowFrequency();
                    if (!overflow && shift != 0) {
                        shadowFrequency = newFreq;
                        regFile.set(Reg.NR3, shadowFrequency & 0xFF);
                        regFile.set(Reg.NR4, (regFile.get(Reg.NR4) & 0xF8) | extract(shadowFrequency, 8, 3));
                        updateShadowFrequency();
                    }
                }
            }
        }
    }

    public void triggerSweep() {
        isIncrementing = false;
        overflow = false;

        shadowFrequency = getFrequency();
        timer = sweepPeriod == 0 ? 8 : sweepPeriod;
        counterEnabled = sweepPeriod != 0 || shift != 0;

        if (shift > 0) updateShadowFrequency();
    }

    private int updateShadowFrequency() {
        int newFreq = shadowFrequency >> shift;
        if (negate) {
            newFreq = shadowFrequency - newFreq;
            isIncrementing = true;
        } else {
            newFreq = shadowFrequency + newFreq;
        }
        if (newFreq > 2047) overflow = true;
        return newFreq;
    }

    private boolean updateSweep() {
        clockSweep();
        if (channelEnabled && overflow) channelEnabled = false;
        return channelEnabled;
    }
}
