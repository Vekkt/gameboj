package gameboj.component;

import gameboj.AddressMap;
import gameboj.bits.Bits;
import gameboj.component.cpu.Cpu;
import gameboj.component.cpu.Cpu.Interrupt;

import java.util.Objects;

import static gameboj.Preconditions.checkBits16;
import static gameboj.Preconditions.checkBits8;
import static gameboj.bits.Bits.clip;
import static gameboj.bits.Bits.test;

/**
 * Represents a GB timer
 * 
 * @author Francois BURGUET 288683
 * @author Gaietan Renault 283350
 */
public final class Timer implements Component, Clocked {

	private final Cpu cpu;

	private int DIV;
	private int TIMA;
	private int TMA;
	private int TAC;

	private static final int[] INDEX = { 9, 3, 5, 7 };

	private enum regName {
		DIV, TAC
	}

	/**
	 * Initialize a timer for the specified CPU
	 * 
	 * @param cpu : cpu of the GB
	 * @throws NullPointerException if the CPU is null
	 */
	public Timer(Cpu cpu) {
		this.cpu = Objects.requireNonNull(cpu);
	}

	@Override
	public void cycle(long cycle) {
		update(regName.DIV, clip(16, DIV + 4));
	}

	/**
	 * Reads the data at the specified address. If address is 0xFF04 returns the 8
	 * MSB of the main counter
	 * 
	 * @param address : address of the register to read
	 * @return the value of the register
	 * @throws IllegalArgumentException if the address is not a 16 bit value
	 */
	@Override
	public int read(int address) {
		checkBits16(address);
        return switch (address) {
            case AddressMap.REG_DIV -> Bits.extract(DIV, 8, 8);
            case AddressMap.REG_TIMA -> TIMA;
            case AddressMap.REG_TMA -> TMA;
            case AddressMap.REG_TAC -> TAC;
            default -> NO_DATA;
        };
	}

	/**
	 * Writes the data at the specified address. If address is 0xFF04, the main
	 * counter is reset
	 * 
	 * @param address : address to write at, 16 bit value
	 * @param data : data to be written, 8 bit value
	 * @throws IllegalArgumentException if the address is not a 16 bit value or the data is not an 8 bit value
	 */
	@Override
	public void write(int address, int data) {
        switch (checkBits16(address)) {
            case AddressMap.REG_DIV -> update(regName.DIV, 0);
            case AddressMap.REG_TIMA -> TIMA = checkBits8(data);
            case AddressMap.REG_TMA -> TMA = checkBits8(data);
            case AddressMap.REG_TAC -> update(regName.TAC, checkBits8(data));
        }
	}

	private boolean state() {
		return test(TAC, 2) && test(DIV, INDEX[clip(2, TAC)]);
	}

	private void incIfChange(boolean s) {
		if (s && !state()) {
			if (TIMA == 0xFF) {
				cpu.requestInterrupt(Interrupt.TIMER);
				TIMA = TMA;
			} else
				TIMA++;
		}
	}

	private void update(regName reg, int value) {
		boolean s0 = state();
		if (reg == regName.TAC)
			TAC = value;
		if (reg == regName.DIV)
			DIV = value;
		incIfChange(s0);
	}
}
