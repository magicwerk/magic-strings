package org.magicwerk.strings.match;

/**
 * Class {@link IStringRange} implements a range with start and length/end.
 */
public interface IStringRange {
	/**
	 * @return start index 
	 */
	int getStart();

	/**
	 * @return end index
	 */
	int getEnd();

	/**
	 * @return length
	 */
	default int getLength() {
		return getEnd() - getStart();
	}

	/**
	 * Returns true if index is contained in range, false otherwise.
	 */
	default boolean contains(int index) {
		return index >= getStart() && index < getEnd();
	}

}