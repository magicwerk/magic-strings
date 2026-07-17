package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;

/**
 * Class {@link AnchoredStringMatcher} searches for the occurrence of the specified literal string at an anchor location,
 * typically start or end of a string.
 */
public abstract class AnchoredStringMatcher implements IStringStartsAtMatcher, IStringEndsAtMatcher {

	// Even if AnchoredStringMatcher implements both IStringStartsAtMatcher and IStringEndsAtMatcher,
	// only a single interface will actually be ready to use at the same time:
	// IStringStartsAtMatcher if the match is anchored at the start, IStringEndsAtMatcher if anchored at the end.

	static final int POS_START = 0;
	static final int POS_END = -1;

	/**
	 * Create a {@link AnchoredStringMatcher} which matches at the start.
	 */
	public static AnchoredStringMatcher startsWith(IStringMatcher matcher) {
		return of(matcher, POS_START);
	}

	/**
	 * Create a {@link AnchoredStringMatcher} which matches at the end.
	 */
	public static AnchoredStringMatcher endsWith(IStringMatcher matcher) {
		return of(matcher, POS_END);
	}

	/**
	 * Create a {@link AnchoredStringMatcher} which matches at the start.
	 */
	public static AnchoredStringMatcher startsWith(String str) {
		return of(StringMatcher.of(str), POS_START);
	}

	/**
	 * Create a {@link AnchoredStringMatcher} which matches at the end.
	 */
	public static AnchoredStringMatcher endsWith(String str) {
		return of(StringMatcher.of(str), POS_END);
	}

	/**
	 * Create {@link AnchoredStringMatcher} for the specified position.
	 * Use 0 for matching at the start, -1 for the end.
	 */
	public static AnchoredStringMatcher of(IStringMatcher matcher, int pos) {
		if (pos >= 0) {
			// match forward from start
			if (matcher instanceof IStringStartsAtMatcher) {
				return new AnchoredStringStartsAtMatcher((IStringStartsAtMatcher) matcher, pos);
			} else {
				return new AnchoredStringFindStartsAtMatcher(matcher, pos);
			}
		} else {
			// match backward from end
			if (matcher instanceof IStringEndsAtMatcher) {
				return new AnchoredStringEndsAtMatcher((IStringEndsAtMatcher) matcher, pos);
			} else if (matcher instanceof IStringReverseMatcher) {
				return new AnchoredStringFindEndsAtReverseMatcher((IStringReverseMatcher) matcher, pos);
			} else {
				return new AnchoredStringFindEndsAtMatcher(matcher, pos);
			}
		}
	}

	//

	IStringMatcher matcher;
	int pos;

	//

	/**
	 * Create a {@link AnchoredStringMatcher}.
	 */
	public AnchoredStringMatcher(IStringMatcher matcher, int pos) {
		this.matcher = matcher;
		this.pos = pos;
	}

	/** Getter for {@link #matcher} */
	public IStringMatcher getMatcher() {
		return matcher;
	}

	/** Getter for {@link #pos} */
	public int getPos() {
		return pos;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (pos: " + pos + ", " + matcher + ")";
	}

	// Start

	/**
	 * Class {@link AnchoredStringStartsAtMatcherBase} looks for a match starting at a specified position.
	 */
	abstract static class AnchoredStringStartsAtMatcherBase extends AnchoredStringMatcher {

		public AnchoredStringStartsAtMatcherBase(IStringMatcher matcher, int pos) {
			super(matcher, pos);
		}

		// IStringEndsAtMatcher (not supported for matching at start)

		@Override
		public boolean endsAt(CharSequence str, int end) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int start) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Class {@link AnchoredStringStartsAtMatcher} looks for a match at a start position by using a {@link IStringStartsAtMatcher}.
	 */
	public static class AnchoredStringStartsAtMatcher extends AnchoredStringStartsAtMatcherBase {

		public AnchoredStringStartsAtMatcher(IStringStartsAtMatcher matcher, int pos) {
			super(matcher, pos);
		}

		@Override
		public IStringStartsAtMatcher getMatcher() {
			return (IStringStartsAtMatcher) matcher;
		}

		// IStringMatcher

		@Override
		public IMatch find(CharSequence str, int start) {
			if (start > pos) {
				return null;
			}
			return getMatcher().matchStartingAt(str, pos);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			if (start > pos) {
				return -1;
			}
			return (getMatcher().startsAt(str, pos)) ? pos : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			if (start > pos) {
				return -1;
			}
			return getMatcher().indexOfEndStartingAt(str, pos);
		}

		// IStringStartsAtMatcher

		@Override
		public boolean startsAt(CharSequence str, int start) {
			if (start != pos) {
				return false;
			}
			return getMatcher().startsAt(str, start);
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			if (start != pos) {
				return -1;
			}
			return getMatcher().indexOfEndStartingAt(str, start);
		}

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			if (start != pos) {
				return null;
			}
			return getMatcher().matchStartingAt(str, start);
		}

	}

	/**
	 * Class {@link AnchoredStringFindStartsAtMatcher} looks for a match at a start position by iterating through the matches.
	 */
	public static class AnchoredStringFindStartsAtMatcher extends AnchoredStringStartsAtMatcherBase {

		public AnchoredStringFindStartsAtMatcher(IStringMatcher matcher, int pos) {
			super(matcher, pos);
		}

		// IStringMatcher

		@Override
		public IMatch find(CharSequence str, int start) {
			int index = start;
			while (true) {
				IMatch match = matcher.find(str, index);
				if (match == null || match.getStart() > pos) {
					return null;
				}
				if (match.getStart() == pos) {
					return match;
				}
				index = match.getEnd();
			}
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

		// IStringStartsAtMatcher

		@Override
		public boolean startsAt(CharSequence str, int start) {
			if (start != pos) {
				return false;
			}
			return indexOf(str, start) == start;
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			if (start != pos) {
				return -1;
			}
			return indexOfEnd(str, start);
		}

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			if (start != pos) {
				return null;
			}
			return find(str, start);
		}
	}

	// End

	/**
	 * Class {@link AnchoredStringEndsAtMatcherBase} looks for a match ending at a specified position.
	 */
	public abstract static class AnchoredStringEndsAtMatcherBase extends AnchoredStringMatcher {

		public AnchoredStringEndsAtMatcherBase(IStringMatcher matcher, int pos) {
			super(matcher, pos);
			assert pos < 0;
		}

		int getEndPos(CharSequence str) {
			return str.length() + pos + 1;
		}

		// IStringStartsAtMatcher (not supported for matching at end)

		@Override
		public boolean startsAt(CharSequence str, int start) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			throw new UnsupportedOperationException();
		}
	}

	public static class AnchoredStringEndsAtMatcher extends AnchoredStringEndsAtMatcherBase {

		public AnchoredStringEndsAtMatcher(IStringEndsAtMatcher matcher, int pos) {
			super(matcher, pos);
		}

		@Override
		public IStringEndsAtMatcher getMatcher() {
			return (IStringEndsAtMatcher) matcher;
		}

		// IStringMatcher

		@Override
		public IMatch find(CharSequence str, int start) {
			int end = getEndPos(str);
			IMatch match = getMatcher().matchEndingAt(str, end);
			return (match != null && match.getStart() >= start) ? match : null;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			int end = getEndPos(str);
			int index = getMatcher().indexOfEndingAt(str, end);
			return (index != -1 && index >= start) ? index : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getEnd() : -1;
		}

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return false;
			}
			return getMatcher().endsAt(str, end);
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return -1;
			}
			return getMatcher().indexOfEndingAt(str, end);
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return null;
			}
			return getMatcher().matchEndingAt(str, end);
		}
	}

	public static class AnchoredStringFindEndsAtReverseMatcher extends AnchoredStringEndsAtMatcherBase {

		public AnchoredStringFindEndsAtReverseMatcher(IStringReverseMatcher matcher, int pos) {
			super(matcher, pos);
		}

		@Override
		public IStringReverseMatcher getMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		// IStringMatcher

		@Override
		public IMatch find(CharSequence str, int start) {
			int index = str.length() - 1;
			int end = getEndPos(str);
			while (true) {
				IMatch match = getMatcher().findReverse(str, index);
				if (match == null || match.getEnd() < end) {
					return null;
				}
				if (match.getEnd() == end) {
					return match;
				}
				index = match.getStart() - 1;
			}
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

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return false;
			}
			return getMatcher().indexOfEndReverse(str, end) == end;
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			IMatch match = matchEndingAt(str, end);
			return (match != null) ? match.getStart() : -1;
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return null;
			}
			return getMatcher().findReverse(str, end);
		}
	}

	public static class AnchoredStringFindEndsAtMatcher extends AnchoredStringEndsAtMatcherBase {

		public AnchoredStringFindEndsAtMatcher(IStringMatcher matcher, int pos) {
			super(matcher, pos);
		}

		// IStringMatcher

		@Override
		public IMatch find(CharSequence str, int start) {
			int index = start;
			int end = getEndPos(str);
			while (true) {
				IMatch match = matcher.find(str, index);
				if (match == null || match.getEnd() > end) {
					return null;
				}
				if (match.getEnd() == end) {
					return match;
				}
				index = match.getEnd();
			}
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			IMatch match = find(str, start);
			return (match != null) ? match.getStart() : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			int index = start;
			int end = getEndPos(str);
			while (true) {
				index = matcher.indexOfEnd(str, index);
				if (index == -1 || index > end) {
					return -1;
				}
				if (index == end) {
					return end;
				}
			}
		}

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return false;
			}
			IMatch match = find(str, 0);
			return (match != null && match.getEnd() == end);
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return -1;
			}
			IMatch match = find(str, 0);
			return (match != null && match.getEnd() == end) ? match.getStart() : -1;
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			if (end != getEndPos(str)) {
				return null;
			}
			IMatch match = find(str, 0);
			return match;
		}
	}

}
