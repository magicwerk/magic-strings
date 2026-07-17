package org.magicwerk.strings.matcher;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link IgnoreStringMatcher} matches a specified string, except if it is part of the string to ignore.
 */
public class IgnoreStringMatcher implements IStringMatcher {

	// Note that this functionality cannot be implemented using Java regex as they do not support conditional expressions.
	// For a single ignore string where the match is contained only once, a regex with negative lookahead and lookbehind
	// can be constructed, but this will not work for several occurrences or ignore strings.

	String match;
	IList<String> ignores;

	/**
	 * Constructor.
	 *
	 * @param match		string to match
	 * @param ignores	strings which should be ignored
	 */
	public IgnoreStringMatcher(String match, String... ignores) {
		this.match = match;
		this.ignores = GapList.create(ignores);

		for (String ignore : ignores) {
			CheckTools.check(ignore.contains(match), "Literal {} not contained in {}", match, ignore);
			CheckTools.check(ignore.length() > match.length(), "Literal {} must not be equal to text", match);
		}
	}

	@Override
	public Match find(CharSequence str, int start) {
		int pos = -1;
		while (true) {
			// Find match
			pos = CharSequenceTools.indexOf(str, match, start);
			if (pos == -1) {
				break;
			}

			// Match string found, check ignores
			if (!CharSequenceTools.matchNot(ignores, str, pos, match.length())) {
				break;
			}

			start = pos + 1;
		}
		if (pos == -1) {
			return null;
		} else {
			return new Match(str, pos, pos + match.length());
		}
	}

	@Override
	public String toString() {
		return StringFormatter.format("IgnoreStringMatcher: match {}, but not {}", match, ignores);
	}

}
