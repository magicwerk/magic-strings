package org.magicwerk.strings.match;

import java.util.function.Function;

/**
 * Class {@link IMatchApplier} applies a match by converting it into a string.
 */
public interface IMatchApplier extends Function<IMatch, String> {

	/**
	 * Convert {@link IMatch} to a string.
	 * The string is then used for replacing etc.
	 * 
	 * @param match	match to convert
	 * @return		result string
	 */
	@Override
	String apply(IMatch match);

}