package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link StartEndStringMatcher} defines a match with separate {@link IStringMatcher}s for matching start and end.
 */
public class StartEndStringMatcher implements IStringMatcher {
	IStringMatcher startMatcher;
	IStringMatcher endMatcher;

	/** Construct a {@link StartEndStringMatcher} */
	public StartEndStringMatcher(IStringMatcher startMatcher, IStringMatcher endMatcher) {
		this.startMatcher = startMatcher;
		this.endMatcher = endMatcher;
	}

	@Override
	public IMatch find(CharSequence str, int start) {
		IMatch startMatch = startMatcher.find(str, start);
		if (startMatch == null) {
			return null;
		}
		IMatch endMatch = endMatcher.find(str, startMatch.getEnd());
		if (endMatch == null) {
			return null;
		}
		return new Match(startMatch.getInput(), startMatch.getStart(), endMatch.getEnd());
	}
}
