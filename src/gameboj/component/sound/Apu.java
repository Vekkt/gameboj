package gameboj.component.sound;

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
    public void cycle(long cycle) { // need to be optimized
        if (!enabled) return;
        for (int i = 0; i < channels.length; ++i)
            amplitudes[i] = channels[i].clock();

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

    @Override
    public int read(int address) {
        if (0xFF27 <= address && address <= 0xFF2F) {
            return 0xFF;
        }
        if (REGS_CH1_START <= address && address < REGS_CH4_END) {
            return channels[(address - REGS_CH1_START) / 5].read(address); // WARNING : CHECK THAT
        } else switch (address) {
            case REG_VIN_CONTROL:
                return regFile.get(Reg.NR50) | MASKS[Reg.NR50.ordinal()];
            case REG_OUTPUT_CONTROL:
                return regFile.get(Reg.NR51) | MASKS[Reg.NR51.ordinal()];
            case REG_STATUS:
                int result = enabled ? mask(7) : 0;
                for (int i = 0; i < channels.length; i++)
                    result |= channels[i].isEnabled() ? mask(i) : 0;
                return result | MASKS[Reg.NR52.ordinal()];
            default:
                if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
                    return channels[2].read(address);
                }
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (REGS_CH1_START <= address && address < REGS_CH4_END) {
            channels[(address - REGS_CH1_START) / 5].write(address, data); // WARNING : CHECK THAT
        } else if (REG_WAVE_TAB_START <= address && address < REG_WAVE_TAB_END) {
            channels[2].write(address, data);
        } else switch (address) {
            case REG_VIN_CONTROL:
                regFile.set(Reg.NR50, data);
                break;
            case REG_OUTPUT_CONTROL:
                regFile.set(Reg.NR51, data);
                break;
            case REG_STATUS:
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
                break;
            default:
        }
    }

    private void stop() {
        output.stop();
        for (SoundChannel c : channels)
            c.stop();
    }

    private void start() {
        for (int addr = REGS_CH1_START; addr <= REG_OUTPUT_CONTROL; ++addr) {
            if (addr == REG_CH1_LENGTH || addr == REG_CH2_LENGTH || addr == REG_CH4_LENGTH)
                write(addr, clip(6, read(addr)));
            else if (addr == REG_CH3_LENGTH)
                write(addr, read(addr));
            else
                write(addr, 0x00);
        }

        for (SoundChannel c : channels)
            c.start(); // WARNING : CHECK THAT
        output.start();
    }
}
