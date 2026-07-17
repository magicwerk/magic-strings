package org.magicwerk.strings.text;

import static org.magicwerk.strings.helper.ObjectHelper.getter;
import static org.magicwerk.strings.helper.ObjectHelper.implCompare;
import static org.magicwerk.strings.helper.ObjectHelper.implEquals;
import static org.magicwerk.strings.helper.ObjectHelper.implHashCode;
import static org.magicwerk.strings.helper.ObjectHelper.implToString;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link TextRange} implements a text range with start and end position.
 * Column of the end position is exclusive.
 * <p>
 * This class is immutable.
 */
public class TextRange implements Comparable<TextRange> {
	/** Start position of text range (never null, start <= end) */
	TextPos start;
	/** End position of text range (never null, start <= end) */
	TextPos end;

	/**
	 * Constructor.
	 */
	public TextRange(TextPos start, TextPos end) {
		this.start = CheckTools.checkNonNull(start);
		this.end = CheckTools.checkNonNull(end);

		CheckTools.check(start.compareTo(end) <= 0, "invalid positions, required start <= end: start= {}, end= {}", start, end);
	}

	/**
	 * Constructor.
	 */
	public TextRange(int startRow, int startCol, int endRow, int endCol) {
		this(new TextPos(startRow, startCol), new TextPos(endRow, endCol));
	}

	public TextRange withStart(TextPos start) {
		return new TextRange(start, end);
	}

	public TextRange withEnd(TextPos end) {
		return new TextRange(start, end);
	}

	public TextRange extendLines() {
		if (start.col == 0 && end.col == 0) {
			return this;
		}
		int endRow = (end.col == 0) ? end.row : end.row + 1;
		return new TextRange(start.row, 0, endRow, 0);
	}

	/** Getter for {#link #start} */
	public TextPos getStart() {
		return start;
	}

	/** Getter for {#link #end} */
	public TextPos getEnd() {
		return end;
	}

	/** Returns whether range is empty (if start equals end position) */
	public boolean isEmpty() {
		return start.equals(end);
	}

	/** Returns number of rows touched by range (0 if empty, otherwise end.row-start.row+1) */
	public int getNumRows() {
		if (isEmpty()) {
			return 0;
		} else {
			return end.row - start.row + 1;
		}
	}

	public TextRange add(TextPos pos) {
		return new TextRange(start.add(pos), end.add(pos));
	}

	public TextRange subtract(TextPos pos) {
		return new TextRange(start.subtract(pos), end.subtract(pos));
	}

	public TextPos clip(TextPos pos) {
		if (pos.compareTo(start) < 0) {
			return start;
		} else if (pos.compareTo(end) > 0) {
			return end;
		} else {
			return pos;
		}
	}

	public TextRange clip(TextRange clip) {
		return new TextRange(clip.clip(start), clip.clip(end));
	}

	@Override
	public int hashCode() {
		return implHashCode(this, TextRange::getStart, TextRange::getEnd);
	}

	@Override
	public boolean equals(Object that) {
		return implEquals(this, that, TextRange::getStart, TextRange::getEnd);
	}

	/**
	 * Comparison is done by start and then end position.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(TextRange that) {
		return implCompare(this, that, TextRange::getStart, TextRange::getEnd);
	}

	@Override
	public String toString() {
		return implToString(this, getter("start", TextRange::getStart), getter("end", TextRange::getEnd));
	}

}
