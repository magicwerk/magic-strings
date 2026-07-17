package org.magicwerk.strings.text;

import static org.magicwerk.strings.helper.ObjectHelper.getter;
import static org.magicwerk.strings.helper.ObjectHelper.implCompare;
import static org.magicwerk.strings.helper.ObjectHelper.implEquals;
import static org.magicwerk.strings.helper.ObjectHelper.implHashCode;
import static org.magicwerk.strings.helper.ObjectHelper.implToString;

/**
 * Class {@link TextPos} implements a text position with row and column.
 * <p>
 * This class is immutable.
 */
public class TextPos implements Comparable<TextPos> {
	/** Row of text position (zero based) */
	int row;
	/** Column of text position (zero based) */
	int col;

	/**
	 * Constructor.
	 *
	 * @param row	row
	 * @param col  	column
	 */
	public TextPos(int row, int col) {
		this.row = row;
		this.col = col;
	}

	/** Getter for {@link #row} */
	public int getRow() {
		return row;
	}

	/** Getter for {@link #col} */
	public int getCol() {
		return col;
	}

	public TextPos withRow(int row) {
		return new TextPos(row, col);
	}

	public TextPos withCol(int col) {
		return new TextPos(row, col);
	}

	public TextPos withNewLine() {
		return new TextPos(row + 1, 0);
	}

	public TextPos withBeginLine() {
		return new TextPos(row, 0);
	}

	public TextPos add(TextPos move) {
		return new TextPos(row + move.row, col + move.col);
	}

	public TextPos subtract(TextPos move) {
		return new TextPos(row - move.row, col - move.col);
	}

	@Override
	public int hashCode() {
		return implHashCode(this, TextPos::getRow, TextPos::getCol);
	}

	@Override
	public boolean equals(Object that) {
		return implEquals(this, that, TextPos::getRow, TextPos::getCol);
	}

	/**
	 * Comparison is done by row and then column.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(TextPos that) {
		return implCompare(this, that, TextPos::getRow, TextPos::getCol);
	}

	@Override
	public String toString() {
		return implToString(this, getter("row", TextPos::getRow), getter("col", TextPos::getCol));
	}

}
