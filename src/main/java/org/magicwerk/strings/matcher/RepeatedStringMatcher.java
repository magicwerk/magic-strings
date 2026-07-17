package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link RepeatedStringMatcher} searches for repeated matches of a {@link IStringMatcher}.
 */
public abstract class RepeatedStringMatcher implements IStringMatcher {

	/**
	 * Create matcher looking for repeated matches.
	 */
	public static RepeatedStringMatcher of(IStringMatcher matcher, boolean reverse) {
		if (!reverse) {
			// Forward
			if (matcher instanceof IStringStartsAtMatcher) {
				return new RepeatedStringStartsAtMatcherImpl((IStringStartsAtMatcher) matcher);
			} else {
				return new RepeatedStringMatcherImpl(matcher);
			}
		} else {
			// Backward
			if (matcher instanceof IStringReverseMatcher && matcher instanceof IStringEndsAtMatcher) {
				return new RepeatedStringReverseEndsAtMatcherImpl((IStringEndsAtMatcher) matcher);
			} else if (matcher instanceof IStringReverseMatcher) {
				return new RepeatedStringReverseMatcherImpl((IStringReverseMatcher) matcher);
			} else {
				return new RepeatedStringReverseIterateMatcherImpl(matcher);
			}
		}
	}

	//

	final IStringMatcher matcher;

	//

	public RepeatedStringMatcher(IStringMatcher matcher) {
		this.matcher = matcher;
	}

	/** Getter for {@link #matcher} */
	public IStringMatcher getMatcher() {
		return matcher;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + matcher + ")";
	}

	//

	static class RepeatedStringMatcherImpl extends RepeatedStringMatcher {

		public RepeatedStringMatcherImpl(IStringMatcher matcher) {
			super(matcher);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch match = matcher.find(str, start);
			if (match == null) {
				return null;
			}
			int end = match.getEnd();
			while (true) {
				IMatch m = matcher.find(str, end);
				if (m == null || m.getStart() != end) {
					break;
				}
				end = m.getEnd();
			}
			return new Match(str, match.getStart(), end);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getStart() : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getEnd() : -1;
		}
	}

	/**
	 * Class {@link RepeatedStringStartsAtMatcherImpl} searches for repeated matches by finding the first match
	 * and then checking whether the match can start immediately again using the {@link IStringStartsAtMatcher} interface.
	 */
	static class RepeatedStringStartsAtMatcherImpl extends RepeatedStringMatcher {

		public RepeatedStringStartsAtMatcherImpl(IStringStartsAtMatcher matcher) {
			super(matcher);
		}

		@Override
		public IStringStartsAtMatcher getMatcher() {
			return (IStringStartsAtMatcher) matcher;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch match = getMatcher().find(str, start);
			if (match == null) {
				return null;
			}
			int end = match.getEnd();
			while (end != -1) {
				int index = getMatcher().indexOfEndStartingAt(str, end);
				if (index == -1) {
					break;
				}
				end = index;
			}
			return new Match(str, match.getStart(), end);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return getMatcher().indexOf(str, start);
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			int end = getMatcher().indexOfEnd(str, start);
			while (end != -1) {
				int index = getMatcher().indexOfEndStartingAt(str, end);
				if (index == -1) {
					break;
				}
				end = index;
			}
			return end;
		}
	}

	abstract static class RepeatedStringReverseMatcher extends RepeatedStringMatcher {

		public RepeatedStringReverseMatcher(IStringMatcher matcher) {
			super(matcher);
		}

		@Override
		public IMatch find(CharSequence str) {
			return find(str, getStartPos(str));
		}

		@Override
		public int indexOf(CharSequence str) {
			return indexOf(str, getStartPos(str));
		}

		@Override
		public int indexOfEnd(CharSequence str) {
			return indexOfEnd(str, getStartPos(str));
		}

		int getStartPos(CharSequence str) {
			return str.length() - 1;
		}
	}

	static class RepeatedStringReverseEndsAtMatcherImpl extends RepeatedStringReverseMatcher {

		public RepeatedStringReverseEndsAtMatcherImpl(IStringEndsAtMatcher matcher) {
			super(matcher);
		}

		public IStringReverseMatcher getReverseMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		public IStringEndsAtMatcher getEndsAtMatcher() {
			return (IStringEndsAtMatcher) matcher;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch match = getReverseMatcher().findReverse(str, start);
			if (match == null) {
				return null;
			}

			int index = match.getStart();
			while (index > 0) {
				int pos = getEndsAtMatcher().indexOfEndingAt(str, index);
				if (pos == -1) {
					break;
				}
				index = pos;
			}
			return new Match(str, index, match.getEnd());
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			return getReverseMatcher().indexOfEndReverse(str, start);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			int index = getReverseMatcher().indexOfReverse(str, start);
			while (index != -1) {
				int pos = getEndsAtMatcher().indexOfEndingAt(str, index);
				if (pos == -1) {
					break;
				}
				index = pos;
			}
			return index;
		}
	}

	static class RepeatedStringReverseMatcherImpl extends RepeatedStringReverseMatcher {

		public RepeatedStringReverseMatcherImpl(IStringReverseMatcher matcher) {
			super(matcher);
		}

		@Override
		public IStringReverseMatcher getMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch match = getMatcher().findReverse(str, start);
			if (match == null) {
				return null;
			}

			int index = match.getStart();
			while (index > 0) {
				IMatch m = getMatcher().findReverse(str, index - 1);
				if (m == null || m.getEnd() != index) {
					break;
				}
				index = m.getStart();
			}
			return new Match(str, index, match.getEnd());
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			return getMatcher().indexOfEndReverse(str, start);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getStart() : -1;
		}
	}

	static class RepeatedStringReverseIterateMatcherImpl extends RepeatedStringReverseMatcher {

		public RepeatedStringReverseIterateMatcherImpl(IStringMatcher matcher) {
			// TODO support IStringStartsAtMatcher?
			super(matcher);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch match = getMatcher().find(str, 0);
			if (match == null) {
				return null;
			}

			int matchStart = match.getStart();
			int matchEnd = match.getEnd();

			while (true) {
				match = getMatcher().find(str, matchEnd);
				if (match == null || match.getEnd() > start) {
					// No more matches or match found is outside of bounds
					break;
				}

				if (match.getStart() != matchEnd) {
					// Matches are not adjacent so restart with new match
					matchStart = match.getStart();
				}
				matchEnd = match.getEnd();
			}
			return new Match(str, matchStart, matchEnd);
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getEnd() : -1;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getStart() : -1;
		}
	}

}
