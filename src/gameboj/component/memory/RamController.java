package gameboj.component.memory;

import gameboj.Preconditions;
import gameboj.component.Component;

/**
 * Represents a ram controller
 * 
 * @author Francois BURGUET 288683
 * @author Gaietan Renault 283350
 */
public final class RamController implements Component {

	private final Ram ram;
	private final int startAddress;
	private final int endAddress;

	/**
	 * Constructor of RamController
	 * 
	 * @param ram : RAM to control
	 * @param startAddress : starting address of the RAM, 16 bits value
	 * @param endAddress : ending address of the RAM, 16 bits value
	 * @throws IllegalArgumentException if one of the address is not a 16 bit value or if the length of
	 *             the memory controlled is negative or greater than the RAM itself
	 */
	public RamController(Ram ram, int startAddress, int endAddress) {
		if (ram == null) {
			throw new NullPointerException();
		}
		int length = endAddress - startAddress;
		Preconditions.checkBits16(startAddress);
		Preconditions.checkBits16(endAddress);
		Preconditions.checkArgument(0 <= length && length <= ram.size());

		this.ram = ram;
		this.startAddress = startAddress;
		this.endAddress = endAddress - 1;
	}

	/**
	 * Constructor of RamController such as all ram accessible
	 * 
	 * @param ram : RAM to control
	 * @param startAddress : starting address of the RAM, 16 bits value
	 */
	public RamController(Ram ram, int startAddress) {
		this(ram, startAddress, startAddress + ram.size());
	}

	@Override
	public int read(int address) {
		Preconditions.checkArgument(0 <= address && address <= 0xFFFF);
		if (address < startAddress || address > endAddress) {
			return NO_DATA;
		}
		return ram.read(address - startAddress);
	}

	@Override
	public void write(int address, int data) {
		if (startAddress <= address && address <= endAddress) {
			ram.write(Preconditions.checkBits16(address) - startAddress, Preconditions.checkBits8(data));
		}
	}

}
