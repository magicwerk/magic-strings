package org.magicwerk.strings.chars;

/**
 * Interface {@link CharOperator} defines an operator interface for type char.
 */
public interface CharOperator {

	/**
	 * Apply conversion to char.
	 * 
	 * @param c	source character
	 * @return character after conversion
	 */
	char apply(char c);
}