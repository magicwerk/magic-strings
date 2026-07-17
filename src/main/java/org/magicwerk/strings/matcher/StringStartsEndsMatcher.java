package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;

/**
 * Class {@link StringStartsEndsMatcher} 
 */
public interface StringStartsEndsMatcher extends IStringReverseMatcher, IStringStartsAtMatcher, IStringEndsAtMatcher {

	// IStringStartsAtMatcher

	@Override
	default boolean startsAt(CharSequence str, int start) {
		IMatch match = matchStartingAt(str, start);
		return match != null;
	}

	@Override
	default int indexOfEndStartingAt(CharSequence str, int start) {
		IMatch match = matchStartingAt(str, start);
		return (match != null) ? match.getEnd() : -1;
	}

	// IStringEndsAtMatcher

	@Override
	default boolean endsAt(CharSequence str, int end) {
		IMatch match = matchEndingAt(str, end);
		return match != null;
	}

	@Override
	default int indexOfEndingAt(CharSequence str, int end) {
		IMatch match = matchEndingAt(str, end);
		return (match != null) ? match.getStart() : -1;
	}
}
