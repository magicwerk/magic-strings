package org.magicwerk.strings.match;

/**
 * Class {@link IMatch} implements a range with start and length/end.
 */
public interface IMatch extends IStringRange {

	/**
	 * @return whole input string
	 */
	public CharSequence getInput();

	/**
	 * @return matched string (part of {@link #getInput})
	 */
	public String getString();

}