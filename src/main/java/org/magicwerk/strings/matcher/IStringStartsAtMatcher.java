package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;

/**
 * Interface {@link IStringStartsAtMatcher} is supported by {@link IStringMatcher} if it can determine
 * whether a match starts at a specified position.
 */
public interface IStringStartsAtMatcher extends IStringMatcher {

	/** Returns true if a match starts at the specified position, false otherwise */
	boolean startsAt(CharSequence str, int start);

	/** Returns the end position if a match starts at the specified position, -1 if there is no such match */
	int indexOfEndStartingAt(CharSequence str, int start);

	/** Returns the match which starts at the specified position, null if there is no such match */
	IMatch matchStartingAt(CharSequence str, int start);
}
