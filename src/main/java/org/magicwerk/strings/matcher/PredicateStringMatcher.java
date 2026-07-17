package org.magicwerk.strings.matcher;

import java.util.function.Predicate;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link PredicateStringMatcher} creates a match for the whole string if the string matches the predicate.
 */
public class PredicateStringMatcher implements IStringMatcher {
	Predicate<String> predicate;

	public PredicateStringMatcher setPredicate(Predicate<String> predicate) {
		this.predicate = predicate;
		return this;
	}

	public Predicate<String> getPredicate() {
		return predicate;
	}

	@Override
	public Match find(CharSequence str, int start) {
		String s = CharSequenceTools.substring(str, start);
		if (predicate.test(s)) {
			return new Match(s, 0, s.length());
		} else {
			return null;
		}
	}

}
