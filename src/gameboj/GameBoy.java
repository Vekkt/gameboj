package gameboj;

import gameboj.component.Joypad;
import gameboj.component.Timer;
import gameboj.component.cartridge.Cartridge;
import gameboj.component.cpu.Cpu;
import gameboj.component.lcd.LcdController;
import gameboj.component.memory.BootRomController;
import gameboj.component.memory.Ram;
import gameboj.component.memory.RamController;
import gameboj.component.sound.Apu;
import gameboj.gui.AudioConverter;

import java.util.Objects;

/**
 * Represents the GameBoy system, with all its components attached
 *
 * @author Francois BURGUET
 */

public final class GameBoy {

	private final Cartridge rom;
	private final Bus bus;
	private final Cpu cpu;
	private final Apu apu;
	private final Timer timer;
	private final Joypad joypad;
	private final LcdController lcd;
	private int cycle;
	private int tick;

	public static final long CLOCK_FREQ = (long) Math.pow(2, 22);
	public static final double CLOCK_NANO_FREQ = CLOCK_FREQ / 1e9;

	/**
	 * Initialize the GameBoy and all its components
	 *
	 * @param cartridge : a virtual game cartridge, non-null
	 * @throws NullPointerException if cartridge is null
	 */
	public GameBoy(Cartridge cartridge) {
		rom = Objects.requireNonNull(cartridge);

		bus = new Bus();
		cpu = new Cpu();
		apu = new Apu(new AudioConverter());
		timer = new Timer(cpu);
		joypad = new Joypad(cpu);
		lcd = new LcdController(cpu);

		Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
		bus.attach(new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END));
		bus.attach(new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END));

		bus.attach(new BootRomController(rom));
		bus.attach(timer);
		bus.attach(joypad);

		cpu.attachTo(bus);
		apu.attachTo(bus);
		lcd.attachTo(bus);
	}

	public Cartridge getRom() {
		return this.rom;
	}

	/**
	 * Returns the joypad attached to the GB
	 * @return joypad : the joypad of the GB
	 */
	public Joypad joypad() {
		return joypad;
	}

	/**
	 * Returns the CPU attached to the bus of the GB
	 * @return cpu : the cpu of the GB
	 */
	public Cpu cpu() {
		return cpu;
	}

	/**
	 * Returns the bus of the GB
	 * @return bus : the bus of the GB
	 */
	public Bus bus() {
		return bus;
	}

	/**
	 * Returns the current clock tick
	 * @return tick : the current tick
	 */
	public long ticks() { return tick; }

	/**
	 * Returns the timer attached to the bus of the GB
	 * @return timer : the timer of the GB
	 */
	public Timer timer() {
		return timer;
	}

	/**
	 * Returns the LCD controller attached to the bus of the GB
	 * @return timer : the LCD controller of the GB
	 */
	public LcdController lcdController() {
		return lcd;
	}

	/**
	 * Runs the GB until the specified clock tick.
	 * Instructions for CPU, LCD controller and timer
	 * always take 4 clock ticks to execute.
	 * @param tick : clock tick until which the simulation is executed
	 * @throws IllegalArgumentException if invalid tick value
	 */
	public void runUntil(long tick) {
		Preconditions.checkArgument(0 <= tick && this.tick <= tick);
		long current = ticks();
		for (long i = current; i < tick; i++) {
			if (i % 4 == 0) {
				timer.cycle(this.cycle);
				lcd.cycle(this.cycle);
				cpu.cycle(this.cycle);
				++this.cycle;
			}
			apu.cycle(this.cycle);
			++this.tick;
		}
	}
}
