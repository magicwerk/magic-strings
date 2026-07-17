package org.magicwerk.strings.matcher;

import java.util.function.Predicate;

import org.magicwerk.strings.match.IMatch;

/**
 * Class {@link ConditionalStringMatcher} implements matching with a {@link IStringMatcher} and a {@link Predicate}.
 * A {@link IMatch} is only returned if the predicate evaluates to true.
 * If false, the next match is tried until one evaluating to true is found.
 */
public class ConditionalStringMatcher implements IStringMatcher {

	IStringMatcher matcher;
	Predicate<IMatch> predicate;

	/**
	 * Construct a {@link ConditionalStringMatcher} with the specified {@link IStringMatcher} and {@link Predicate}.
	 */
	public ConditionalStringMatcher(IStringMatcher matcher, Predicate<IMatch> predicate) {
		this.matcher = matcher;
		this.predicate = predicate;
	}

	@Override
	public IMatch find(CharSequence str, int start) {
		while (true) {
			IMatch match = matcher.find(str, start);
			if (match == null) {
				return null;
			}
			if (predicate.test(match)) {
				return match;
			}
			start++;
		}
	}
}
