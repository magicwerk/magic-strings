package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link StringFixedLenMatcher} searches for the occurrence of the specified literal string.
 */
public interface StringFixedLenMatcher extends IStringReverseMatcher, IStringFixedLenMatcher, IStringStartsAtMatcher, IStringEndsAtMatcher {

	boolean matchAt(CharSequence str, int index);

	// IStringReverseMatcher

	@Override
	default int indexOfReverse(CharSequence str, int end) {
		int index = indexOfEndReverse(str, end);
		return (index != -1) ? index - getMatchLength() : -1;
	}

	// IStringStartsAtMatcher

	@Override
	default boolean startsAt(CharSequence str, int start) {
		return matchAt(str, start);
	}

	@Override
	default int indexOfEndStartingAt(CharSequence str, int start) {
		boolean startsAt = startsAt(str, start);
		return (startsAt) ? start + getMatchLength() : -1;
	}

	@Override
	default IMatch matchStartingAt(CharSequence str, int start) {
		int end = indexOfEndStartingAt(str, start);
		return (end != -1) ? new Match(str, start, end) : null;
	}

	// IStringEndsAtMatcher

	@Override
	default boolean endsAt(CharSequence str, int end) {
		return matchAt(str, end - getMatchLength());
	}

	@Override
	default int indexOfEndingAt(CharSequence str, int end) {
		boolean endsAt = endsAt(str, end);
		return (endsAt) ? end - getMatchLength() : -1;
	}

	@Override
	default IMatch matchEndingAt(CharSequence str, int end) {
		int start = indexOfEndingAt(str, end);
		return (start != -1) ? new Match(str, start, end) : null;
	}

}
