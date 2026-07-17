package org.magicwerk.strings.match;

import static org.magicwerk.strings.helper.ObjectHelper.getter;
import static org.magicwerk.strings.helper.ObjectHelper.implCompare;
import static org.magicwerk.strings.helper.ObjectHelper.implEquals;
import static org.magicwerk.strings.helper.ObjectHelper.implHashCode;
import static org.magicwerk.strings.helper.ObjectHelper.implToString;

/**
 * Class {@link StringRange} implements a range with start and length/end.
 * <p>
 * This class is immutable.
 */
public class StringRange implements IStringRange, Comparable<StringRange> {
	final int start;
	final int end;

	/**
	 * Construct a range with start and end index.
	 *
	 * @param start	start index
	 * @param end  	end index
	 */
	public static StringRange withEnd(int start, int end) {
		return new StringRange(start, end);
	}

	/**
	 * Construct a range with start index and length.
	 *
	 * @param start   start index of match
	 * @param length  length of match
	 */
	public static StringRange withLen(int start, int length) {
		return new StringRange(start, start + length);
	}

	StringRange(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public int getLength() {
		return end - start;
	}

	/**
	 * Set start.
	 */
	public StringRange withStart(int start) {
		return withEnd(start, end);
	}

	/**
	 * Set end.
	 */
	public StringRange setEnd(int end) {
		return withEnd(start, end);
	}

	/**
	 * Set length. Setting the length will also change the end position whereas the start position remains unchanged.
	 */
	public StringRange withLength(int length) {
		return withEnd(start, start + length);
	}

	public StringRange extend(int index) {
		int s = (index < start) ? index : start;
		int e = (index > end) ? index : end;
		return withEnd(s, e);
	}

	public StringRange merge(IStringRange range) {
		int s = Math.min(start, range.getStart());
		int e = Math.max(end, range.getEnd());
		return withEnd(s, e);
	}

	@Override
	public int hashCode() {
		return implHashCode(this, StringRange::getStart, StringRange::getEnd);
	}

	@Override
	public boolean equals(Object that) {
		return implEquals(this, that, StringRange::getStart, StringRange::getEnd);
	}

	/**
	 * Comparison is done by start and then length.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(StringRange that) {
		return implCompare(this, that, StringRange::getStart, StringRange::getEnd);
	}

	@Override
	public String toString() {
		return implToString(this, getter("start", StringRange::getStart), getter("end", StringRange::getEnd));
	}

}