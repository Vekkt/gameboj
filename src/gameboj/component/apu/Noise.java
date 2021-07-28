package gameboj.component.apu;

import static gameboj.bits.Bits.*;
import static gameboj.component.apu.Apu.ChannelType;

public final class Noise extends SoundChannel {
    private final static int LFSR_INITIAL = 0x7FF;

    private final VolumeEnvelope envelope;

    private int lfsr;
    private int lastLFSR;
    private int frequencyTimer;
    private int frequencyDivisor;

    Noise() {
        super(ChannelType.NOISE);
        envelope = new VolumeEnvelope();
        lfsr = LFSR_INITIAL;
    }

    @Override
    public int clock() {
        envelope.clock();

        if (!(updateLength() && dacEnabled)) return 0;
        if (--frequencyTimer == 0) {
            frequencyTimer = frequencyDivisor;
            lastLFSR = updateLFSR();
        }

        return lastLFSR * envelope.getVolume();
    }

    @Override
    public void write(int address, int data) {
        if (regStartAddress <= address && address < regEndAddress) {
            Reg reg = Reg.values()[address - regStartAddress];
            super.write(address, data);
            switch (reg) {
                case NR1:
                    length.setLength(64 - clip(6, data));
                    break;
                case NR2:
                    envelope.updateEnvelope(data);
                    dacEnabled = extract(data, 3, 5) != 0;
                    channelEnabled &= dacEnabled;
                    break;
                case NR3:
                    int shift = data >> 4;
                    int divCode = (data & 0x07);
                    frequencyDivisor = (divCode == 0 ? 0 : divCode << 4) << shift;
                    frequencyTimer = 1;
                default:
            }
        }
    }

    @Override
    protected void trigger() {
        lfsr = LFSR_INITIAL;
        envelope.trigger();
    }

    @Override
    protected void start() {
        lfsr = LFSR_INITIAL;
        length.start();
        envelope.start();
    }

    private int updateLFSR() {
        int xor = ((lfsr & 0b01) ^ ((lfsr & 0b10) >> 1)) & 1;
        lfsr = (lfsr >> 1) | (xor << 14);
        if (widthMode()) {
            lfsr |= xor << 6;
        }
        return 1 & ~lfsr;
    }

    private boolean widthMode() { return test(regFile.get(Reg.NR3), 3); }
}
