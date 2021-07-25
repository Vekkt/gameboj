package gameboj;

/**
 * Methods for argument checking
 *
 * @author Francois BURGUET
 *
 */
public interface Preconditions {

	/**
	 * Checks if the premise is true or not
	 *
	 * @param b : premise to evaluate
	 * @throws IllegalArgumentException if the argument is false
	 */
	static void checkArgument(boolean b) {
		if (!b)
			throw new IllegalArgumentException();
	}

	/**
	 * Verifies that the given integer is between 0 and 0xFF
	 *
	 * @param v : integer to check
	 * @return the given parameter
	 * @throws IllegalArgumentException if the parameter is not an 8-bit value
	 */
	static int checkBits8(int v) {
		if (0xFF == (0xFF | v))
			return v;
		else
			throw new IllegalArgumentException();
	}

	/**
	 * Verifies that the given integer is between 0 and 0xFFFF
	 *
	 * @param v : integer to check
	 * @return the given parameter
	 * @throws IllegalArgumentException if the parameter is not a 16-bit value
	 */
	static int checkBits16(int v) {
		if (0xFFFF == (0xFFFF | v))
			return v;
		else
			throw new IllegalArgumentException();
	}
}
