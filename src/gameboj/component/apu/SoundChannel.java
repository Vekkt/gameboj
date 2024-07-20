package gameboj.component.apu;

import gameboj.Register;
import gameboj.RegisterFile;
import gameboj.component.Component;

import static gameboj.bits.Bits.*;
import static gameboj.AddressMap.*;
import static gameboj.component.apu.Apu.ChannelType;

public abstract class SoundChannel implements Component {
    private final static int[][] CHANNEL_MASKS = new int[][] {
            {0x80, 0x3F, 0x00, 0xFF, 0xBF},
            {0xFF, 0x3F, 0x00, 0xFF, 0xBF},
            {0x7F, 0xFF, 0x9F, 0xFF, 0xBF},
            {0xFF, 0xFF, 0x00, 0x00, 0xBF},
    };

    private final static int WAVE_FULL_LENGTH = 256;
    private final static int DEFAULT_FULL_LENGTH = 64;

    protected enum Reg implements Register {
        NR0, NR1, NR2, NR3, NR4
    }

    final RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());

    final int regStartAddress;
    final int regEndAddress;
    private final int[] channelMasks;

    final LengthCounter length;
    boolean dacEnabled;
    boolean channelEnabled;

    SoundChannel(ChannelType type) {
        regStartAddress = REGS_CH_START[type.ordinal()];
        regEndAddress = REGS_CH_END[type.ordinal()];
        channelMasks = CHANNEL_MASKS[type.ordinal()];

        if (type == ChannelType.WAVE) length = new LengthCounter(WAVE_FULL_LENGTH);
        else length = new LengthCounter(DEFAULT_FULL_LENGTH);
    }

    public abstract int clock();

    protected abstract void trigger();

    protected abstract void start();

    @Override public int read(int address) {
        if (regStartAddress <= address && address < regEndAddress) {
            int idx = address - regStartAddress;
            return regFile.get(Reg.values()[idx]) | channelMasks[idx];
        }
        return NO_DATA;
    }

    public int read(int address, boolean unmasked) {
        if (regStartAddress <= address && address < regEndAddress) {
            int idx = address - regStartAddress;
            int value = regFile.get(Reg.values()[idx]);

            if (unmasked) { return value; }
            else { return value | channelMasks[idx]; }

        }
        return NO_DATA;
    }

    @Override public void write(int address, int data) {
        if (regStartAddress <= address && address < regEndAddress) {
            Reg reg = Reg.values()[address - regStartAddress];
            regFile.set(reg, data);
            if (reg == Reg.NR4) {
                length.trigger(data);
                if (test(data, 7)) {
                    channelEnabled = dacEnabled;
                    trigger();
                }
            }
        }
    }

    boolean updateLength() {
        length.clock();
        if (!length.isEnabled()) {
            return channelEnabled;
        }
        if (channelEnabled && length.length() == 0) {
            channelEnabled = false;
        }
        return channelEnabled;
    }

    int frequency() {
        int lsb = regFile.get(Reg.NR3);
        int msb = clip(3, regFile.get(Reg.NR4));
        return 2048 - make16(msb, lsb);
    }

    void stop() {
        channelEnabled = false;
    }

    boolean isEnabled() {
        return channelEnabled && dacEnabled;
    }
}
