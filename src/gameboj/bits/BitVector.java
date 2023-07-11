package gameboj.bits;

import gameboj.Preconditions;

import java.util.Arrays;

/**
 * Represents a vector of <code>bits</code>, of length multiple of Integer.SIZE.
 *
 * @author Francois BURGUET
 */

public final class BitVector {

	private enum booleanOp {
		OR {
			@Override public int apply(int b1, int b2) {
				return b1 | b2;
			}
		},
		AND {
			@Override public int apply(int b1, int b2) {
				return b1 & b2;
			}
		};

		abstract int apply(int b1, int b2);
	}

	private enum extractOp {
		ZERO, WRAP
	}

	private final int[] vector;
	private final int size;

	/**
	 * Constructs a new <code>BitVector</code> of <code>bits</code> of the specified <code>size</code>
	 * where all bits are initialized at the specified value <code>val</code>.
	 *
	 * @param size : size of the vector, multiple of Integer.SIZE
	 * @param val : value at which the bits should be initialized
	 * @throws IllegalArgumentException if the size is not strictly positive or not a multiple of Integer.SIZE
	 */
	public BitVector(int size, boolean val) {
		Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);

		vector = new int[size / Integer.SIZE];
		this.size = vector.length * Integer.SIZE;
		if (val) Arrays.fill(vector, -1);
	}

	/**
	 * Constructs a new <code>BitVector</code> of bits of the specified <code>size</code> where all bits are
	 * initialized at zero.
	 *
	 * @param size : size of the vector, multiple of Integer.SIZE, strictly positive
	 */
	public BitVector(int size) {
		this(size, false);
	}

	private BitVector(int[] vector) {
		this.vector = vector;
		this.size = vector.length * Integer.SIZE;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i : this.vector)
			str.insert(0, String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0'));
		return str.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BitVector that))
			return false;
		return (this.size() == that.size() && Arrays.equals(this.vector, that.vector));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(vector);
	}

	/**
	 * Returns the size, in bits, of the vector.
	 *
	 * @return the size of the vector
	 */
	public int size() {
		return size;
	}

	/**
	 * Tests the bit at the specified <code>index</code> in the vector.
	 *
	 * @param index : index of the bit to test
	 * @return true if the bit is a 1, false if it's a 0
	 * @throws IllegalArgumentException if the index is negative or greater (or equal) than the vector size
	 */
	public boolean testBit(int index) {
		Preconditions.checkArgument(0 <= index && index < size());
		return Bits.test(vector[index / Integer.SIZE], index % Integer.SIZE);
	}

	/**
	 * Creates a new <code>BitVector</code> corresponding ot the logic negation of the current
	 * vector.
	 *
	 * @return a new vector, which corresponds to the logic negation of this instance.
	 */
	public BitVector not() {
		int[] vector = new int[size() / Integer.SIZE];
		for (int i = 0; i < size() / Integer.SIZE; i++)
			vector[i] = ~this.vector[i];
		return new BitVector(vector);
	}

	/**
	 * Returns a new <code>BitVector</code> corresponding to the logic conjunction between this
	 * current instance and the vector <code>that</code>.
	 *
	 * @param that : the second operand
	 * @return a new vector, conjunction of the current vector and the parameter
	 * @throws IllegalArgumentException if the two vectors are not of the same size
	 */
	public BitVector and(BitVector that) {
		return booleanOperation(booleanOp.AND, that);
	}

	/**
	 * Returns a new <code>BitVector</code> corresponding to the logic disjunction between this
	 * current instance and the vector <code>that</code>.
	 *
	 * @param that : the second operand
	 * @return a new vector, disjunction of the current vector and the parameter
	 * @throws IllegalArgumentException if the two vectors are not of the same size
	 */
	public BitVector or(BitVector that) {
		return booleanOperation(booleanOp.OR, that);
	}

	/**
	 * Extracts from this current instance of a new <code>BitVector</code>, starting from
	 * <code>start</code>, of specified <code>size</code>, with the zero extended convention.
	 *
	 * @param start : starting bit in the extended vector
	 * @param size : size of the extraction
	 * @return a new vector, the extracted part of the extended vector
	 * @throws IllegalArgumentException if the size is not a multiple of Integer.SIZE
	 */
	public BitVector extractZeroExtended(int start, int size) {
		return extract(extractOp.ZERO, start, size);
	}

	/**
	 * Extracts from the current vector a new <code>BitVector</code>, starting from <code>start</code>, of
	 * specified <code>size</code>, with the wrap extended convention.
	 *
	 * @param start : starting bit in the extended vector
	 * @param size : size of the extraction
	 * @return a new vector, the extracted part of the extended vector
	 * @throws IllegalArgumentException if the size is not a multiple of Integer.SIZE
	 */
	public BitVector extractWrapped(int start, int size) {
		return extract(extractOp.WRAP, start, size);
	}

	/**
	 * Creates a new <code>BitVector</code> corresponding to this current instance shifted on a
	 * specified <code>distance</code>.
	 *
	 * @param distance : distance of the shift, to the left if positive, else to the right
	 * @return a new shifted vector
	 * @throws IllegalArgumentException if the two vectors are not of the same size
	 */
	public BitVector shift(int distance) {
		return extractZeroExtended(distance, size());
	}

	private BitVector booleanOperation(booleanOp operator, BitVector that) {
		Preconditions.checkArgument(this.size() == that.size());
		int[] vector = new int[size() / Integer.SIZE];

		for (int i = 0; i < size() / Integer.SIZE; i++) {
			vector[i] = operator.apply(this.vector[i], that.vector[i]);
		}
		return new BitVector(vector);
	}

	private BitVector extract(extractOp type, int start, int size) {
		Preconditions.checkArgument(size % Integer.SIZE == 0 && size >= 0);
		int[] newVector = new int[size / Integer.SIZE];

		if (start % Integer.SIZE == 0) {
			for (int i = start; i < start + size; i += Integer.SIZE)
				newVector[(i - start) / Integer.SIZE] = getChunk(type, i);
		} else {
			for (int i = start; i < start + size; i += Integer.SIZE) {
				int prevChunk = getChunk(type, i);
				int nextChunk = getChunk(type, i + Integer.SIZE);
				int shift = Math.floorMod(start, Integer.SIZE);
				newVector[(i - start) / Integer.SIZE] = nextChunk << (Integer.SIZE - shift) | prevChunk >>> shift;
			}
		}
		return new BitVector(newVector);
	}

	private int getChunk(extractOp type, int index) {
		if (type == extractOp.ZERO && !(0 <= index && index < size()))
			return 0;
		return this.vector[Math.floorMod(Math.floorDiv(index, Integer.SIZE), size() / Integer.SIZE)];
	}

	/**
	 * Static builder of the class BitVector. Allows construction of a <code>BitVector</code>, byte
	 * by byte.
	 */
	public final static class Builder {
		private int[] vector;

		/**
		 * Creates a new builder for this instance of <code>BitVector</code>.
		 *
		 * @param size : size of the vector in bits. Must be a multiple of <code>Integer.SIZE</code>.
		 * @throws IllegalArgumentException if the size is not a multiple of <code>Integer.SIZE</code>
		 * 		   or the size is negative.
		 */
		public Builder(int size) {
			Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);
			vector = new int[size / Integer.SIZE];
		}

		/**
		 * Sets the desired byte to the specified value at <code>index</code>.
		 * The value must be an 8-bit value.
		 *
		 * @param index : index of the byte
		 * @param val : value of the byte
		 * @throws IllegalStateException if the vector has already been built
		 * @throws IndexOutOfBoundsException if the index of the byte is negative or greater (or equal) to the
		 * 		   number of possible bytes in the vector
		 * @throws IllegalArgumentException if the byte is not an 8 bit value
		 */
		public void setByte(int index, int val) {
			if (vector == null)
				throw new IllegalStateException();
			if (index < 0 || vector.length * 4 <= index)
				throw new IndexOutOfBoundsException();

			Preconditions.checkBits8(val);

			for (int i = 0; i < Byte.SIZE; i++) {
				boolean b = Bits.test(val, i);
				vector[index / 4] = Bits.set(vector[index / 4], (index * Byte.SIZE) % Integer.SIZE + i, b);
			}
		}

		/**
		 * Builds a new <code>BitVector</code> from this builder.
		 * Can only be called once, has the original builder is destroyed
		 * on the call of this method.
		 *
		 * @return the new vector.
		 * @throws IllegalStateException if the vector has already been built.
		 */
		public BitVector build() {
			if (vector == null)
				throw new IllegalStateException();
			BitVector bitVector = new BitVector(vector);
			vector = null;
			return bitVector;
		}
	}

}
