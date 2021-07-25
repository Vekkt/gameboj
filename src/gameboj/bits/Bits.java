package gameboj.bits;

import gameboj.Preconditions;

import java.util.Objects;

/**
 * Methods for bits manipulation.
 * @author Francois BURGUET
 */

public final class Bits {
	private final static int[] LOOKUP = new int[] {
		0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0,
		0x70, 0xF0, 0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78,
		0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
		0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02,
		0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2, 0x0A, 0x8A,
		0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06, 0x86, 0x46,
		0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE,
		0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21,
		0xA1, 0x61, 0xE1, 0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9,
		0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
		0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED,
		0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13,
		0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B, 0x9B,
		0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57,
		0xD7, 0x37, 0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF, 0x1F, 0x9F, 0x5F, 0xDF,
		0x3F, 0xBF, 0x7F, 0xFF,
	};

	private Bits() {
	}

	/**
	 * Returns a binary number with only zeros, except a one at the
	 * position given by <code>index</code>.
	 *
	 * @param index : where the one bit has to be in the mask
	 * @return mask : the mask corresponding to the index
	 * @throws IndexOutOfBoundsException if index is not between 0 and 31 (included)
	 */
	public static int mask(int index) {
		if (0 > index || index >= 32) {
			throw new IndexOutOfBoundsException();
		}
		return 1 << index;
	}

	/**
	 * Tests if the value at the specified <code>index</code> is a one
	 * in the binary representation of <code>bits</code>.
	 *
	 * @param bits : bits to test
	 * @param index : index of the bit to test in bits
	 * @return true if the bit at index is one, else false
	 * @throws IndexOutOfBoundsException if <code>index</code> is not between 0 and 31 (included)
	 */
	public static boolean test(int bits, int index) {
		if (0 > index || index >= 32) {
			throw new IndexOutOfBoundsException();
		}
		return (bits & mask(index)) != 0;
	}

	/**
	 * Tests if the bit at the specified <code>index</code> is a one or a
	 * zero in the bit representation of the <code>value</code> parameter.
	 *
	 * @param bits : bits value on which to perform the test
	 * @param bit : bit index to test in bits
	 * @return true if the corresponding bit in the value is a one, else false
	 * @throws IndexOutOfBoundsException if the index is not between 0 and 31 (included)
	 */
	public static boolean test(int bits, Bit bit) {
		return (bits & bit.mask()) != 0;
	}

	/**
	 * Sets the value in <code>bits</code> at the specified <code>index</code>
	 * to the value given by <code>newValue</code>.
	 *
	 * @param bits : value to modify
	 * @param index : index of the bit to change
	 * @param newValue : new value of the bit at the index
	 * @return the new value containing the modified bit
	 * @throws IndexOutOfBoundsException if index is not between 0 and 31 (included)
	 */
	public static int set(int bits, int index, boolean newValue) {
		if (0 > index || index >= 32) {
			throw new IndexOutOfBoundsException();
		}
		return bits ^ (-(newValue ? 1 : 0) ^ bits) & mask(index);
	}

	/**
	 * Selects the first <code>size</code> bits from the value in <code>bits</code>.
	 *
	 * @param size : size of the clip
	 * @param bits : bits to clip on
	 * @return the clipped part of bits
	 * @throws IllegalArgumentException if the size is not between 0 and 31 (included)
	 */
	public static int clip(int size, int bits) {
		Preconditions.checkArgument(0 <= size && size <= 32);
		return size == 32 ? bits : bits & (mask(size) - 1);
	}

	/**
	 * Extracts a portion in the bit representation of <code>bits</code>
	 * from position <code>start</code> of length <code>size</code>.
	 *
	 * @param bits : value from which to extract
	 * @param start : index at which to start the extraction
	 * @param size : size of the extracted part
	 * @return the extracted part of the value of the given size
	 * @throws IndexOutOfBoundsException if the parameters are in conflict
	 */
	public static int extract(int bits, int start, int size) {
		Preconditions.checkArgument(0 <= start && 0 <= size);
		Preconditions.checkArgument(0 <= start+size && start+size < 32);
		return Bits.clip(size, bits >> start);
	}

	/**
	 * Rotates the bit representation of the value <code>bits</code> by a
	 * given <code>distance</code>, then returns the clipped value of the
	 * specified <code>size</code>. A negative distance means the direction
	 * is to the left (right otherwise).
	 *
	 * @param size : size of the clipped value
	 * @param bits : value to rotate
	 * @param distance : distance of the rotation
	 * @return the rotated bits
	 * @throws IllegalArgumentException if the size is not between 0 and 31 (included)
	 */
	public static int rotate(int size, int bits, int distance) {
		Preconditions.checkArgument(0 < size && size <= 32);
		int d = Math.floorMod(distance, size);
		return clip(size, bits << d | bits >>> (size - d));
	}

	/**
	 * Extends the sign of b by casting it to <code>byte</code>.
	 * The <code>int</code> value is supposed to be an 8-bit value.
	 *
	 * @param b : bits to extend sign of
	 * @return extended sign value of the parameter
	 * @throws IllegalArgumentException if b is not an 8 bit value
	 */
	public static int signExtend8(int b) {
		Preconditions.checkBits8(b);
		return (byte) b;
	}

	/**
	 * Exchange the bits of b symmetrically using a look-up table.
	 *
	 * @param b : bits to reverse
	 * @return the reversed value of the parameter
	 * @throws IllegalArgumentException if b is not a 8 bit value
	 */
	public static int reverse8(int b) {
		Preconditions.checkBits8(b);
		return LOOKUP[b];
	}

	/**
	 * Inverts all bits of b.
	 *
	 * @param b : bits to inverse
	 * @return the complement of the parameter
	 * @throws IllegalArgumentException if b is not an 8 bit value
	 */
	public static int complement8(int b) {
		Preconditions.checkBits8(b);
		return 0xFF ^ b;
	}

	/**
	 * Creates a new 16 bit value. The 8 most-significant-bits are
	 * taken from the first argument and the 8 least-significant-bits are
	 * from the second argument.
	 *
	 * @param highB : 8 bit value, MSB of the new value
	 * @param lowB : 8 bit value, LSB of the new value
	 * @return the 16 bit value created from highB and lowB
	 * @throws IllegalArgumentException if one of the parameters is not an 8 bit value
	 */
	public static int make16(int highB, int lowB) {
		Preconditions.checkBits8(highB);
		Preconditions.checkBits8(lowB);
		return (clip(8, highB) << 8) + clip(8, lowB);
	}
}
