package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;

/**
 * Interface {@link IStringEndsAtMatcher} is supported by {@link IStringMatcher} if it can determine
 * whether a match ends at a specified position.
 */
public interface IStringEndsAtMatcher extends IStringMatcher {

	/** Returns true if a match ends at the specified position, false otherwise */
	boolean endsAt(CharSequence str, int end);

	/** Returns the start position if a match ends at the specified position, -1 if there is no such match */
	int indexOfEndingAt(CharSequence str, int end);

	/** Returns the match which ends at the specified position, null if there is no such match */
	IMatch matchEndingAt(CharSequence str, int end);
}
