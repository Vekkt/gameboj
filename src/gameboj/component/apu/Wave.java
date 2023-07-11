package gameboj.component.apu;

import gameboj.component.memory.Ram;
import gameboj.component.apu.Apu.ChannelType;

import static gameboj.AddressMap.*;
import static gameboj.bits.Bits.*;

public final class Wave extends SoundChannel {
    private final Ram waveRAM;

    private boolean triggered;
    private int sinceLastRead;
    private int lastReadAddress;
    private int buffer;
    private int output;
    private int wavePosition;
    private int freqDiv;

    Wave() {
        super(ChannelType.WAVE);
        waveRAM = new Ram(REG_WAVE_TAB_SIZE);
        sinceLastRead = 65_536;
    }

    @Override
    public int read(int address) {
        if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
            if (!isEnabled()) {
                return waveRAM.read(address - REG_WAVE_TAB_START);
            } else if (sinceLastRead < 2) {
                return waveRAM.read(lastReadAddress - REG_WAVE_TAB_START);
            } else {
                return 0xFF;
            }
        }
        return super.read(address);
    }

    @Override
    public void write(int address, int data) {
        if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
            if (!isEnabled()) { // obscure behavior
                waveRAM.write(address - REG_WAVE_TAB_START, data);
            }
            else if (sinceLastRead < 2) // obscure behavior
                waveRAM.write(lastReadAddress - REG_WAVE_TAB_START, data);
        } else if (regStartAddress <= address && address < regEndAddress) {
            Reg reg = Reg.values()[address - regStartAddress];
            switch (reg) {
                case NR0 -> {
                    dacEnabled = test(data, 7);
                    channelEnabled &= dacEnabled;
                }
                case NR1 -> length.setLength(length.fullLength - data);
                case NR4 -> { // obscure behavior
                    if (test(data, 7)) {
                        if (isEnabled() && freqDiv == 2) {
                            int pos = wavePosition / 2;
                            if (pos < 4) {
                                waveRAM.write(0, waveRAM.read(pos));
                            } else {
                                pos &= 0b1100;
                                for (int j = 0; j < 4; j++) {
                                    waveRAM.write(j, waveRAM.read(((pos + j) % 0x10)));
                                }
                            }
                        }
                    }
                }
            }
            super.write(address, data);
        }
    }

    @Override
    public int clock() {
        sinceLastRead++;
        if (!(updateLength() && dacEnabled)) return 0;
        if (!test(regFile.get(Reg.NR0), 7)) return 0;

        if (--freqDiv == 0) {
            freqDiv = frequency() * 2;
            if (triggered) {
                output = extract(buffer, 4, 4);
                triggered = false;
            } else {
                output = readWave();
            }
            wavePosition = (wavePosition + 1) % 32;
        }
        return output;
    }

    @Override
    public void trigger() {
        wavePosition = 0;
        freqDiv = 6;
        triggered = true;
    }

    @Override
    public void start() {
        length.start();
        buffer = 0;
        wavePosition = 0;
    }

    private int readWave() {
        sinceLastRead = 0;
        lastReadAddress = REG_WAVE_TAB_START + wavePosition / 2;
        buffer = waveRAM.read(lastReadAddress - REG_WAVE_TAB_START);
        int b = buffer;
        if (wavePosition % 2 == 0) b = extract(b, 4, 4);
        else b = clip(4, b);

        return switch (volume()) {
            case 0 -> 0;
            case 1 -> b;
            case 2 -> b >> 1;
            case 3 -> b >> 2;
            default -> throw new IllegalArgumentException();
        };
    }

    private int volume() {
        return extract(regFile.get(Reg.NR2), 5, 2);
    }
}
