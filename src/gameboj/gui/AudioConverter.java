package gameboj.gui;

import gameboj.component.apu.SoundOutput;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static gameboj.GameBoy.CLOCK_FREQ;

public final class AudioConverter implements SoundOutput {
    private static final int SAMPLE_RATE = 22050;
    private static final int BUFFER_SIZE = 4096;

    private static final AudioFormat FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_UNSIGNED,
            SAMPLE_RATE,
            8,
            2,
            2,
            SAMPLE_RATE,
            false
    );

    private SourceDataLine line;
    private byte[] buffer;
    private int i;
    private int tick;
    private int divider;

    @Override
    public void start() {
        if (line != null) {
            return;
        }

        try {
            line = AudioSystem.getSourceDataLine(FORMAT);
            line.open(FORMAT, BUFFER_SIZE);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        line.start();
        buffer = new byte[line.getBufferSize()];
        divider = (int) (CLOCK_FREQ / FORMAT.getSampleRate());
    }

    @Override
    public void stop() {
        if (line == null) {
            return;
        }

        line.drain();
        line.stop();
        line = null;
    }

    @Override
    public void play(int left, int right) {
        if (tick++ != 0) {
            tick %= divider;
            return;
        }

        buffer[i++] = (byte) (left);
        buffer[i++] = (byte) (right);
        if (i > BUFFER_SIZE / 2) {
            line.write(buffer, 0, i);
            i = 0;
        }
    }
}
