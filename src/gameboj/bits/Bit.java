package gameboj.bits;

/**
 * Represents a Bit, i.e the value 1 at a certain index in a bit string.
 * @author Francois Burguet
 */
public interface Bit {
	int ordinal();

	/**
	 * Returns the index of the enabled bit.
	 * @return index : the index of the bit
	 */
	default int index() {
		return ordinal();
	}

	/**
	 * Returns the mask corresponding to the bit.
	 * @return mask : mask corresponding to the bit
	 */
	default int mask() {
		return 1 << ordinal();
	}
}
