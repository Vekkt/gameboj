package gameboj.component.sound;

import gameboj.Register;
import gameboj.RegisterFile;
import gameboj.component.Component;

import static gameboj.bits.Bits.*;
import static gameboj.AddressMap.*;
import static gameboj.component.sound.Apu.ChannelType;

public abstract class SoundChannel implements Component {
    private final static int[][] CHANNEL_MASKS = new int[][] {
            {0x80, 0x3f, 0x00, 0xff, 0xbf},
            {0xff, 0x3f, 0x00, 0xff, 0xbf},
            {0x7f, 0xff, 0x9f, 0xff, 0xbf},
            {0xff, 0xff, 0x00, 0x00, 0xbf},
    };

    protected enum Reg implements Register {
        NR0, NR1, NR2, NR3, NR4
    }

    protected final RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());

    protected final int regStartAddress;
    protected final int regEndAddress;
    protected final int[] channelMasks;

    protected final LengthCounter length;
    protected boolean dacEnabled;
    protected boolean channelEnabled;

    public SoundChannel(ChannelType type) {
        regStartAddress = REGS_CH_START[type.ordinal()];
        regEndAddress = REGS_CH_END[type.ordinal()];
        channelMasks = CHANNEL_MASKS[type.ordinal()];

        if (type == ChannelType.WAVE) length = new LengthCounter(256);
        else length = new LengthCounter(64);
    }


    // Start Abstract Methods
    /**************************************************************************/

    public abstract int clock();

    protected abstract void trigger();

    protected abstract void start();

    // End Abstract Methods
    /**************************************************************************/

    public boolean isEnabled() {
        return channelEnabled && dacEnabled;
    }

    @Override
    public int read(int address) {
        if (regStartAddress <= address && address < regEndAddress) {
            int idx = address - regStartAddress;
            return regFile.get(Reg.values()[idx]) | channelMasks[idx];
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (regStartAddress <= address && address < regEndAddress) {
            Reg reg = Reg.values()[address - regStartAddress];
            regFile.set(reg, data);
            if (reg == Reg.NR4) {
                length.updateLength(data);
                if (test(data, 7)) {
                    channelEnabled = dacEnabled;
                    trigger();
                }
            }
        }
    }

    protected boolean updateLength() {
        length.clock();
        if (!length.isEnabled()) {
            return channelEnabled;
        }
        if (channelEnabled && length.length() == 0) {
            channelEnabled = false;
        }
        return channelEnabled;
    }

    protected int frequency() {
        int lsb = regFile.get(Reg.NR3);
        int msb = clip(3, regFile.get(Reg.NR4));
        return 2048 - make16(msb, lsb);
    }

    protected void stop() {
        channelEnabled = false;
    }
}
