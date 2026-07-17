package org.magicwerk.strings.matcher;

import org.magicwerk.collections.IList;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link ExpressionStringMatchers} contains classes which combine instances of {@link IStringMatcher} in a logical operation.
 * Due to the logical operation, they either match the full input string or do not match at all.
 */
public class ExpressionStringMatchers {

	/**
	 * Class {@link NotMatcher} matches if the passed {@link IStringMatcher} does not match.
	 * As there is no match to determine, the whole input string is reported as matching.
	 */
	public static class NotMatcher implements IStringMatcher {
		IStringMatcher matcher;

		public NotMatcher(IStringMatcher matcher) {
			this.matcher = CheckTools.checkNonNull(matcher);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			if (matcher.find(str, start) == null) {
				return new Match(str, 0, -1);
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return "[NotMatcher: " + matcher + "]";
		}
	}

	/**
	 * Class {@link AndMatcher} matches if all of the passed {@link IStringMatcher} do match.
	 * As there is no match to determine, the whole input string is reported as matching.
	 */
	public static class AndMatcher implements IStringMatcher {
		IList<IStringMatcher> matchers;

		public AndMatcher(IList<IStringMatcher> matchers) {
			this.matchers = CheckTools.checkNonNull(matchers);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			for (IStringMatcher matcher : matchers) {
				if (matcher.find(str, start) == null) {
					return null;
				}
			}
			return new Match(str, 0, -1);
		}

		@Override
		public String toString() {
			return "[AndMatcher: " + matchers + "]";
		}
	}

	/**
	 * Class {@link OrMatcher} matches if any of the passed {@link IStringMatcher} do match.
	 * As there is no match to determine, the whole input string is reported as matching.
	 */
	public static class OrMatcher implements IStringMatcher {
		IList<IStringMatcher> matchers;

		public OrMatcher(IList<IStringMatcher> matchers) {
			this.matchers = CheckTools.checkNonNull(matchers);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			for (IStringMatcher matcher : matchers) {
				if (matcher.find(str, start) != null) {
					return new Match(str, 0, -1);
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return "[OrMatcher: " + matchers + "]";
		}
	}

}
