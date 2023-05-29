package gameboj.component.apu;

import gameboj.Register;
import gameboj.RegisterFile;
import gameboj.component.Clocked;
import gameboj.component.Component;

import static gameboj.AddressMap.*;
import static gameboj.bits.Bits.*;

public final class Apu implements Component, Clocked {
    public enum ChannelType {
        SQUARE_A, SQUARE_B, WAVE, NOISE
    }

    private enum Reg implements Register {
        NR50, NR51, NR52
    }

    private final static int[] MASKS = {
        0x00, 0x00, 0x70,
    };

    private final RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());

    private final SoundChannel[] channels;
    private final SoundOutput output;
    private final int[] amplitudes;

    private boolean enabled;

    public Apu(SoundOutput output) {
        Sweep sweepChannel    = new Sweep();
        Square squareBChannel = new Square();
        Wave waveChannel      = new Wave();
        Noise noiseChannel    = new Noise();

        this.channels = new SoundChannel[] {
                sweepChannel,
                squareBChannel,
                waveChannel,
                noiseChannel,
        };
        this.amplitudes = new int[4];
        this.output = output;
    }

    @Override
    public void cycle(long cycle) {
        if (!enabled) return;
        for (int i = 0; i < channels.length; ++i)
            amplitudes[i] = channels[i].clock();

        /* Needs to be optimized */
        int outputSelect = regFile.get(Reg.NR51);
        int left = 0;
        int right = 0;

        for (int i = 0; i < channels.length; i++) {
            if (test(outputSelect, i + 4)) left += amplitudes[i];
            if (test(outputSelect, i)) right += amplitudes[i];
        }
        left /= 4;
        right /= 4;

        int volumes = regFile.get(Reg.NR50);
        left *= extract(volumes, 4, 3);
        right *= clip(3, volumes);

//        output.play((byte) left, (byte) right);
    }

    @Override public int read(int address) {
        return read(address, false);
    }

    public int read(int address, boolean unmasked) {
        if (REG_STATUS < address && address < REG_WAVE_TAB_START) {
            return 0xFF;
        }
        if (REGS_CH1_START <= address && address < REGS_CH4_END) {
            if (unmasked) {
                return channels[(address - REGS_CH1_START) / 5].read(address, true);
            } else {
                return channels[(address - REGS_CH1_START) / 5].read(address);
            }
        } else switch (address) {
            case REG_VIN_CONTROL -> {
                return regFile.get(Reg.NR50) | MASKS[Reg.NR50.ordinal()];
            }
            case REG_OUTPUT_CONTROL -> {
                return regFile.get(Reg.NR51) | MASKS[Reg.NR51.ordinal()];
            }
            case REG_STATUS -> {
                int data = enabled ? mask(7) : 0;
                for (int i = 0; i < channels.length; i++)
                    data |= channels[i].isEnabled() ? mask(i) : 0;
                return data | MASKS[Reg.NR52.ordinal()];
            }
            default -> {
                if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
                    return channels[2].read(address);
                }
            }
        }
        return NO_DATA;
    }

    @Override public void write(int address, int data) {
        if (REGS_CH1_START <= address && address < REGS_CH4_END) {
            channels[(address - REGS_CH1_START) / 5].write(address, data);
        } else if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
            channels[2].write(address, data);
        } else switch (address) {
            case REG_VIN_CONTROL -> regFile.set(Reg.NR50, data);
            case REG_OUTPUT_CONTROL -> regFile.set(Reg.NR51, data);
            case REG_STATUS -> {
                if (!test(data, 7)) {
                    if (enabled) {
                        enabled = false;
                        stop();
                    }
                } else {
                    if (!enabled) {
                        enabled = true;
                        start();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void stop() {
        output.stop();
        for (SoundChannel c : channels)
            c.stop();
    }

    private void start() {
        for (int address = REGS_CH1_START; address <= REG_OUTPUT_CONTROL; ++address) {
            if (address == REG_CH1_LENGTH || address == REG_CH2_LENGTH || address == REG_CH4_LENGTH)
                write(address, clip(6, read(address, true)));
            else if (address == REG_CH3_LENGTH)
                write(address, read(address, true));
            else
                write(address, 0x00);
        }

        for (SoundChannel c : channels)
            c.start();
        output.start();
    }
}
