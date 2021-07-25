package gameboj;

/**
 * Represents a register name
 *
 * @author Francois Burguet
 */
public interface Register {
	int ordinal();

	/**
	 * Returns the index of the register in the register file
	 *
	 * @return the index of the register
	 */
	default int index() {
		return ordinal();
	}
}
