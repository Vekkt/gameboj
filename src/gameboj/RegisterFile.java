package gameboj;

import gameboj.bits.Bit;
import gameboj.bits.Bits;


/**
 * Represents a register file
 *
 * @author Francois Burguet
 */
public final class RegisterFile<E extends Register> {
	private final byte[] file;

	/**
	 * Creates a register file with the specified registers
	 *
	 * @param allRegs : registers to group
	 */
	public RegisterFile(E[] allRegs) {
		this.file = new byte[allRegs.length];
	}

	/**
	 * Returns the data in the register at index in the register file
	 *
	 * @param reg : a 8-bit register
	 * @return the value in reg, must be an 8-bit value
	 */
	public int get(E reg) {
		return Bits.clip(Byte.SIZE, Byte.toUnsignedInt(file[reg.index()]));
	}

	/**
	 * Sets the value of the specified register
	 *
	 * @param reg : 8-bit register to write in
	 * @param newValue : new value of the register, must be an 8-bit value
	 * @throws IllegalArgumentException if <code>newValue</code> is not 8-bit value
	 */
	public void set(E reg, int newValue) {
		file[reg.index()] = (byte) Preconditions.checkBits8(newValue);
	}

	/**
	 * Tests the specified bit in the register
	 *
	 * @param reg : 8-bit register to test
	 * @param b : the bit to test
	 * @return the boolean output of the bit test
	 */
	public boolean testBit(E reg, Bit b) {
		return Bits.test(get(reg), b);
	}

	/**
	 * Sets the bit of the register to the specified value
	 *
	 * @param reg : 8-bit register to modify
	 * @param bit : the bit to change
	 * @param newValue : true (1) or false (0)
	 */
	public void setBit(E reg, Bit bit, boolean newValue) {
		set(reg, Bits.set(get(reg), bit.index(), newValue));
	}
}
