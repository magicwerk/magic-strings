package org.magicwerk.strings;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.BuilderHelper.BuilderFinderBase;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.ICharMode;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;
import org.magicwerk.strings.matcher.AnchoredStringMatcher;
import org.magicwerk.strings.matcher.CountedStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringReverseMatcher;

/**
 * Class {@link StringFinder} implements finding string with various options.
 * Methods:
 * - contains
 * - count
 * - find
 * - indexOf
 * - indexOfEnd
 * - matches
 * - matchIterator
 * Options:
 * - reverse, occurrence, ignoreCase, returnEnd, overlap
 */
public interface StringFinder {

	// Design:
	// CharSequence: needed for usage without constructing temporary strings
	// String: needed to bind at compile time to call like String.indexOf()

	// indexOf

	/** Returns start position of first occurrence of configured find text in input string starting at position 0, returns -1 if not found */
	int indexOf(CharSequence str);

	/** Returns start position of first occurrence of configured find text in input string starting at the specified position, returns -1 if not found */
	int indexOf(CharSequence str, int start);

	default int indexOf(String str) {
		return indexOf((CharSequence) str, 0);
	}

	default int indexOf(String str, int start) {
		return indexOf((CharSequence) str, start);
	}

	// indexOfEnd

	/** Returns end position of first occurrence of configured find text in input string starting at position 0, returns -1 if not found */
	int indexOfEnd(CharSequence str);

	/** Returns end position of first occurrence of configured find text in input string starting at the specified position, returns -1 if not found */
	int indexOfEnd(CharSequence str, int start);

	default int indexOfEnd(String str) {
		return indexOfEnd((CharSequence) str, 0);
	}

	default int indexOfEnd(String str, int start) {
		return indexOfEnd((CharSequence) str, start);
	}

	// find

	/** Returns {@link IMatch} of first occurrence of configured find text in input string starting at position 0, returns null if not found */
	IMatch find(CharSequence str);

	/** Returns {@link IMatch} of first occurrence of configured find text in input string starting at the specified position, returns null if not found */
	IMatch find(CharSequence str, int start);

	default IMatch find(String str) {
		return find((CharSequence) str, 0);
	}

	default IMatch find(String str, int start) {
		return find((CharSequence) str, start);
	}

	// contains

	default boolean contains(CharSequence str) {
		return indexOf(str) != -1;
	}

	default boolean contains(CharSequence str, int start) {
		return indexOf(str, start) != -1;
	}

	default boolean contains(String str) {
		return indexOf(str) != -1;
	}

	default boolean contains(String str, int start) {
		return indexOf(str, start) != -1;
	}

	// count

	// Implementation: for count() it does not matter whether we search forward or backward

	int count(CharSequence str);

	int count(CharSequence str, int start);

	// matches

	/**
	 * Return all matches in the passed string.
	 */
	IList<IMatch> matches(CharSequence str);

	/**
	 * Return all matches in the passed string starting at the specified position.
	 */
	IList<IMatch> matches(CharSequence str, int start);

	// matchIterator

	/**
	 * Return iterator for all matches in the passed string.
	 */
	Iterator<IMatch> matchIterator(CharSequence str);

	/**
	 * Return iterator for all matches in the passed string starting at the specified position.
	 */
	Iterator<IMatch> matchIterator(CharSequence str, int start);

	//

	/** Build {@link StringFinder} with specified builder function */
	public static StringFinder build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/** Get {@link Builder} to create a {@link StringFinder} */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringFinder}.
	 */
	public static class Builder extends BuilderFinderBase<Builder> {

		static final FindNoneImpl findNone = new FindNoneImpl();
		static final FindEmptyStringImpl findEmptyString = new FindEmptyStringImpl();
		static final FindEmptyStringReverseImpl findEmptyStringReverse = new FindEmptyStringReverseImpl();

		// Options
		/** True to find in reverse direction */
		boolean reverse = false;
		/** Defines which occurrence of the find operation is returned, default is 0 for the first occurrence */
		int occurrence = 0;
		/** True to look for an anchored match, i.e. either at start or at end (if reverse is true) */
		boolean anchored = false;
		/** True to allow overlapping of matches, e.g. if occurrence is used or count() or matches() is called */
		boolean overlap = false;

		//

		/** Setter for {@link #occurrence} */
		public Builder setOccurrence(int occurrence) {
			this.occurrence = occurrence;
			return this;
		}

		/** Setter for {@link #reverse} */
		public Builder setReverse(boolean reverse) {
			this.reverse = reverse;
			return this;
		}

		/** Setter for {@link #overlap} */
		public Builder setOverlap(boolean overlap) {
			this.overlap = overlap;
			return this;
		}

		/** Setter for {@link #anchored} */
		public Builder setAnchored(boolean anchored) {
			this.anchored = anchored;
			return this;
		}

		//

		/**
		 * Build an instance of {@link StringFinder} with the specified configuration.
		 * The created instance must be of the specified type or an exception is thrown.
		 */
		<T extends StringFinder> T build(Class<T> clazz) {
			@SuppressWarnings("unchecked")
			T finder = (T) build();
			return finder;
		}

		/** Build an instance of {@link StringFinder} with the specified configuration */
		public StringFinder build() {
			check();

			// Check whether there is a specific implementation for the requested configuration
			int charsSupport = getCharSupport();
			ICharMode charMode = CharCaseTools.getCharModeFromCharSupport(charsSupport);
			if (charMode == CharMode.CS_CHAR) {
				charMode = null;
			}
			StringFinder finderImpl = getStringFinder(charMode);
			if (finderImpl != null) {
				return finderImpl;
			}

			// Get matcher for requested search pattern
			IStringMatcher matcherImpl = getAsStringMatcher(charMode);

			// Use matcher in generic implementations to support reverse/occurrence/returnEnd
			return doBuild(matcherImpl);
		}

		boolean isFindEmpty() {
			if (string != null) {
				if (StringTools.isEmpty(string)) {
					return true;
				}
			} else if (anyChar != null) {
				if (StringTools.isEmpty(anyChar)) {
					return true;
				}
			}
			return false;
		}

		int getOccurrence() {
			if (reverse) {
				return -occurrence - 1;
			} else {
				return occurrence;
			}
		}

		/**
		 * Returns an instance of {@link StringFinder} which implements the specified configuration.
		 * If none is available, null is returned.
		 */
		StringFinder getStringFinder(ICharMode useIgnoreCase) {
			int occurrence = getOccurrence();
			if (isFindEmpty()) {
				return (occurrence >= 0) ? findEmptyString : findEmptyStringReverse;
			}

			if (useIgnoreCase != null) {
				return null;
			}

			if (anchored) {
				if (occurrence != 0 && occurrence != -1) {
					return findNone;
				}
				if (string instanceof String) {
					if (occurrence == 0) {
						return new FindStringStartsWithImpl(string, overlap);
					} else if (occurrence == -1) {
						return new FindStringEndsWithImpl(string, overlap);
					}
				}
			}

			// Specialized implementations for string, character, code point
			if (string != null) {
				if (occurrence == 0) {
					return new FindStringImpl(string, overlap);
				} else if (occurrence == -1) {
					return new FindStringReverseImpl(string, overlap);
				}
			} else if (character != null) {
				if (occurrence == 0) {
					return new FindCharImpl(character, overlap);
				} else if (occurrence == -1) {
					return new FindCharReverseImpl(character, overlap);
				}
			} else if (codePoint != null) {
				if (occurrence == 0) {
					return new FindCodePointImpl(codePoint, overlap);
				} else if (occurrence == -1) {
					return new FindCodePointReverseImpl(codePoint, overlap);
				}
			}
			return null;
		}

		StringFinder doBuild(IStringMatcher matcherImpl) {
			int occurrence = getOccurrence();
			if (anchored) {
				assert occurrence == 0 || occurrence == -1;
				AnchoredStringMatcher asm = AnchoredStringMatcher.of(matcherImpl, occurrence);
				return new FindMatcherImpl(asm, overlap);

			} else {
				if (occurrence >= 0) {
					// Forward
					if (occurrence == 0) {
						// Find first occurrence
						return new FindMatcherImpl(matcherImpl, overlap);
					} else {
						CountedStringMatcher csm = CountedStringMatcher.of(matcherImpl, occurrence, overlap);
						return new FindMatcherImpl(csm, overlap);
					}
				} else {
					// Backward
					if (occurrence == -1 && matcherImpl instanceof IStringReverseMatcher) {
						// Find last occurrence
						return new FindReverseMatcherReverseImpl((IStringReverseMatcher) matcherImpl, overlap);
					} else {
						CountedStringMatcher csm = CountedStringMatcher.of(matcherImpl, occurrence, overlap);
						return new FindMatcherReverseImpl(csm, overlap);
					}
				}
			}
		}
	}

	//

	public static class FindNoneImpl implements StringFinder {

		static class FindNoneIterator implements Iterator<IMatch> {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public IMatch next() {
				throw new NoSuchElementException();
			}
		}

		static final FindNoneIterator ITERATOR = new FindNoneIterator();

		@Override
		public int indexOf(CharSequence str) {
			return -1;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return -1;
		}

		@Override
		public int indexOfEnd(CharSequence str) {
			return -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			return -1;
		}

		@Override
		public IMatch find(CharSequence str) {
			return null;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			return null;
		}

		@Override
		public int count(CharSequence str) {
			return 0;
		}

		@Override
		public int count(CharSequence str, int start) {
			return 0;
		}

		@Override
		public IList<IMatch> matches(CharSequence str) {
			return GapList.create();
		}

		@Override
		public IList<IMatch> matches(CharSequence str, int start) {
			return GapList.create();
		}

		@Override
		public Iterator<IMatch> matchIterator(CharSequence str) {
			return ITERATOR;
		}

		@Override
		public Iterator<IMatch> matchIterator(CharSequence str, int start) {
			return ITERATOR;
		}
	}

	public interface IStringFinderImpl extends StringFinder {

		// CharSequence

		@Override
		default IMatch find(CharSequence str) {
			return find(str, getStartPos(str));
		}

		@Override
		default int indexOf(CharSequence str) {
			return indexOf(str, getStartPos(str));
		}

		@Override
		default int indexOfEnd(CharSequence str) {
			return indexOfEnd(str, getStartPos(str));
		}

		@Override
		default boolean contains(CharSequence str) {
			return contains(str, getStartPos(str));
		}

		// String

		@Override
		default IMatch find(String str) {
			return find(str, getStartPos(str));
		}

		@Override
		default int indexOf(String str) {
			return indexOf(str, getStartPos(str));
		}

		@Override
		default int indexOfEnd(String str) {
			return indexOfEnd(str, getStartPos(str));
		}

		@Override
		default boolean contains(String str) {
			return contains(str, getStartPos(str));
		}

		// count

		boolean isFindOverlap();

		@Override
		default int count(CharSequence str) {
			return count(str, getStartPos(str));
		}

		@Override
		default int count(CharSequence str, int start) {
			if (!isFindOverlap()) {
				return doCount(str, start);
			} else {
				return doCountOverlap(str, start);
			}
		}

		default int doCount(CharSequence str, int start) {
			int count = 0;
			int index = start;
			while (true) {
				int newIndex = indexOfEnd(str, index);
				if (newIndex == -1) {
					break;
				}
				count++;
				index = (newIndex != index) ? newIndex : index + 1; // overlap = false
			}
			return count;
		}

		default int doCountOverlap(CharSequence str, int start) {
			int count = 0;
			int index = start;
			while (true) {
				index = indexOf(str, index);
				if (index == -1) {
					break;
				}
				count++;
				index++; // overlap = true
			}
			return count;
		}

		// matchIterator

		@Override
		default Iterator<IMatch> matchIterator(CharSequence str) {
			return matchIterator(str, getStartPos(str));
		}

		@Override
		default Iterator<IMatch> matchIterator(CharSequence str, int start) {
			return new MatchIterator(this, str, start);
		}

		static class MatchIterator implements Iterator<IMatch> {

			// Config
			IStringFinderImpl finder;
			CharSequence str;
			int index;
			// State
			boolean hasRead;
			IMatch match;

			//

			public MatchIterator(IStringFinderImpl finder, CharSequence str, int start) {
				this.finder = finder;
				this.str = str;
				this.index = start;
			}

			@Override
			public boolean hasNext() {
				if (!hasRead) {
					read();
					hasRead = true;
				}
				return match != null;
			}

			@Override
			public IMatch next() {
				if (!hasRead) {
					read();
				} else {
					hasRead = false;
				}
				if (match == null) {
					throw new NoSuchElementException();
				}
				return match;
			}

			void read() {
				match = finder.find(str, index);
				if (match != null) {
					if (finder.isFindOverlap()) {
						index = match.getStart() + 1;
					} else {
						index = (match.getEnd() != match.getStart()) ? match.getEnd() : match.getStart() + 1;
					}
				}
			}
		}

		// matches

		@Override
		default IList<IMatch> matches(CharSequence str) {
			return matches(str, getStartPos(str));
		}

		@Override
		default IList<IMatch> matches(CharSequence str, int start) {
			if (!isFindOverlap()) {
				return doMatches(str, start);
			} else {
				return doMatchesOverlap(str, start);
			}
		}

		default IList<IMatch> doMatches(CharSequence str, int start) {
			IList<IMatch> matches = new GapList<>();
			int index = start;
			while (true) {
				IMatch match = find(str, index);
				if (match == null) {
					break;
				}
				matches.add(match);
				index = (match.getEnd() != match.getStart()) ? match.getEnd() : match.getStart() + 1; // overlap = false
			}
			return matches;
		}

		default IList<IMatch> doMatchesOverlap(CharSequence str, int start) {
			IList<IMatch> matches = new GapList<>();
			int index = start;
			while (true) {
				IMatch match = find(str, index);
				if (match == null) {
					break;
				}
				matches.add(match);
				index = match.getStart() + 1; // overlap = true
			}
			return matches;
		}

		//

		default int getStartPos(CharSequence str) {
			return 0;
		}
	}

	public interface IStringFinderReverseImpl extends IStringFinderImpl {

		@Override
		default int count(CharSequence str, int end) {
			if (!isFindOverlap()) {
				return doCount(str, end);
			} else {
				return doCountOverlap(str, end);
			}
		}

		@Override
		default int doCount(CharSequence str, int end) {
			int count = 0;
			int index = end;
			while (true) {
				int newIndex = indexOf(str, index);
				if (newIndex == -1) {
					break;
				}
				count++;
				index = (newIndex != index) ? newIndex : index - 1; // overlap = false
			}
			return count;
		}

		@Override
		default int doCountOverlap(CharSequence str, int end) {
			int count = 0;
			int index = 0;
			while (true) {
				index = indexOfEnd(str, index);
				if (index == -1) {
					break;
				}
				count++;
				index--; // overlap = true
			}
			return count;
		}

		@Override
		default IList<IMatch> matches(CharSequence str, int end) {
			if (!isFindOverlap()) {
				return doMatches(str, end);
			} else {
				return doMatchesOverlap(str, end);
			}
		}

		@Override
		default IList<IMatch> doMatches(CharSequence str, int end) {
			IList<IMatch> matches = new GapList<>();
			int index = end;
			while (true) {
				IMatch match = find(str, index);
				if (match == null) {
					break;
				}
				matches.add(match);
				index = (match.getEnd() != match.getStart()) ? match.getStart() : match.getStart() - 1; // overlap = false
			}
			return matches;
		}

		@Override
		default IList<IMatch> doMatchesOverlap(CharSequence str, int end) {
			IList<IMatch> matches = new GapList<>();
			int index = 0;
			while (true) {
				IMatch match = find(str, index);
				if (match == null) {
					break;
				}
				matches.add(match);
				index = match.getEnd() - 1; // overlap = true
			}
			return matches;
		}

		//

		@Override
		default Iterator<IMatch> matchIterator(CharSequence str, int start) {
			return new MatchReverseIterator(this, str, start);
		}

		static class MatchReverseIterator extends MatchIterator {

			public MatchReverseIterator(IStringFinderImpl finder, CharSequence str, int start) {
				super(finder, str, start);
			}

			@Override
			void read() {
				match = finder.find(str, index);
				if (match != null) {
					if (finder.isFindOverlap()) {
						index = match.getEnd() - 1;
					} else {
						index = (match.getEnd() != match.getStart()) ? match.getStart() : match.getStart() - 1;
					}
				}
			}
		}

		//

		@Override
		default int getStartPos(CharSequence str) {
			return str.length();
		}
	}

	public interface IStringFinderMatchLength extends IStringFinderImpl {

		int getMatchLength();

	}

	public interface IStringFinderMatchLengthImpl extends IStringFinderImpl, IStringFinderMatchLength {

		@Override
		default int indexOfEnd(CharSequence str, int start) {
			int index = indexOf(str, start);
			return (index != -1) ? index + getMatchLength() : -1;
		}

		@Override
		default IMatch find(CharSequence str, int start) {
			int index = indexOf(str, start);
			return (index != -1) ? new Match(str, index, index + getMatchLength()) : null;
		}

		@Override
		default IMatch find(String str, int start) {
			int index = indexOf(str, start);
			return (index != -1) ? new Match(str, index, index + getMatchLength()) : null;
		}
	}

	public interface IStringFinderMatchLengthReverseImpl extends IStringFinderReverseImpl, IStringFinderMatchLength {

		@Override
		default int indexOfEnd(String str, int end) {
			int index = indexOf(str, end);
			return (index != -1) ? (index + getMatchLength()) : -1;
		}

		@Override
		default int indexOfEnd(CharSequence str, int end) {
			int index = indexOf(str, end);
			return (index != -1) ? (index + getMatchLength()) : -1;
		}

		@Override
		default IMatch find(String str, int end) {
			int index = indexOf(str, end);
			return (index != -1) ? new Match(str, index, index + getMatchLength()) : null;
		}

		@Override
		default IMatch find(CharSequence str, int end) {
			int index = indexOf(str, end);
			return (index != -1) ? new Match(str, index, index + getMatchLength()) : null;
		}

	}

	// Find empty string
	// An empty string matches at each position including the end, i.e. a string with length 3 will have 4 matches.

	public static abstract class IFindEmptyStringImpl implements IStringFinderMatchLength {

		@Override
		public int getMatchLength() {
			return 0;
		}

		@Override
		public boolean isFindOverlap() {
			throw new AssertionError(); // not used, derived classes provide specialized implementations of count()/matches()
		}
	}

	public static class FindEmptyStringImpl extends IFindEmptyStringImpl implements IStringFinderMatchLengthImpl {

		@Override
		public int indexOf(CharSequence str, int start) {
			return start;
		}

		@Override
		public int count(CharSequence str, int start) {
			return str.length() - start + 1;
		}

		@Override
		public IList<IMatch> matches(CharSequence str, int start) {
			int num = count(str, start);
			IList<IMatch> matches = new GapList<>(num);
			for (int i = 0; i < num; i++) {
				IMatch match = new Match(str, start + i, start + i);
				matches.add(match);
			}
			return matches;
		}
	}

	public static class FindEmptyStringReverseImpl extends IFindEmptyStringImpl implements IStringFinderMatchLengthReverseImpl {

		@Override
		public int indexOf(CharSequence str, int end) {
			return str.length();
		}

		@Override
		public int count(CharSequence str, int end) {
			return end + 1;
		}

		@Override
		public IList<IMatch> matches(CharSequence str, int start) {
			int num = count(str, start);
			IList<IMatch> matches = new GapList<>(num);
			for (int i = 0; i < num; i++) {
				IMatch match = new Match(str, start - i, start - i);
				matches.add(match);
			}
			return matches;
		}
	}

	// Find string

	public static abstract class IFindStringImpl implements IStringFinderMatchLength {
		String searchStr;
		boolean overlap;

		public IFindStringImpl(String searchStr, boolean overlap) {
			this.searchStr = searchStr;
			this.overlap = overlap;
		}

		@Override
		public int getMatchLength() {
			return searchStr.length();
		}

		@Override
		public boolean isFindOverlap() {
			return overlap;
		}
	}

	public static class FindStringImpl extends IFindStringImpl implements IStringFinderMatchLengthImpl {

		public FindStringImpl(String searchStr, boolean overlap) {
			super(searchStr, overlap);
		}

		@Override
		public int indexOf(String str, int start) {
			return str.indexOf(searchStr, start);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return CharSequenceTools.indexOf(str, searchStr, start);
		}
	}

	public static class FindStringReverseImpl extends IFindStringImpl implements IStringFinderMatchLengthReverseImpl {

		public FindStringReverseImpl(String searchStr, boolean overlap) {
			super(searchStr, overlap);
		}

		@Override
		public int indexOf(String str, int end) {
			int startIndex = str.lastIndexOf(searchStr, end - getMatchLength());
			return startIndex;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			int endIndex = CharSequenceTools.reverseIndexOf(str, searchStr, end);
			return (endIndex != -1) ? (endIndex - getMatchLength()) : -1;
		}
	}

	public static class FindStringStartsWithImpl extends IFindStringImpl implements IStringFinderMatchLengthImpl {

		public FindStringStartsWithImpl(String searchStr, boolean overlap) {
			super(searchStr, overlap);
		}

		@Override
		public int indexOf(String str) {
			return str.startsWith(searchStr) ? 0 : -1;
		}

		@Override
		public int indexOf(String str, int start) {
			if (start != 0) {
				return -1;
			}
			return indexOf(str);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			if (start != 0) {
				return -1;
			}
			return CharSequenceTools.startsWith(str, searchStr) ? 0 : -1;
		}
	}

	public static class FindStringEndsWithImpl extends IFindStringImpl implements IStringFinderMatchLengthImpl {

		public FindStringEndsWithImpl(String searchStr, boolean overlap) {
			super(searchStr, overlap);
		}

		int getMatchStart(CharSequence str) {
			return str.length() - getMatchLength();
		}

		@Override
		public int indexOf(String str) {
			return str.endsWith(searchStr) ? 0 : -1;
		}

		@Override
		public int indexOf(String str, int start) {
			if (start != getMatchStart(str)) {
				return -1;
			}
			return indexOf(str);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			if (start != getMatchStart(str)) {
				return -1;
			}
			return CharSequenceTools.endsWith(str, searchStr) ? 0 : -1;
		}
	}

	// Find char

	public static abstract class IFindCharImpl implements IStringFinderMatchLength {
		char searchChar;
		boolean overlap;

		public IFindCharImpl(char searchChar, boolean overlap) {
			this.searchChar = searchChar;
			this.overlap = overlap;
		}

		@Override
		public int getMatchLength() {
			return 1;
		}

		@Override
		public boolean isFindOverlap() {
			return overlap;
		}
	}

	public static class FindCharImpl extends IFindCharImpl implements IStringFinderMatchLengthImpl {

		public FindCharImpl(char searchChar, boolean overlap) {
			super(searchChar, overlap);
		}

		@Override
		public int indexOf(String str, int start) {
			return str.indexOf(searchChar, start);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return CharSequenceTools.indexOf(str, searchChar, start);
		}

		// TODO verify
		@Override
		public int count(CharSequence str, int start) {
			int count = 0;
			int end = str.length();
			for (int i = start; i < end; i++) {
				if (str.charAt(i) == searchChar) {
					count++;
				}
			}
			return count;
		}
	}

	public static class FindCharReverseImpl extends IFindCharImpl implements IStringFinderMatchLengthReverseImpl {

		FindCharReverseImpl(char searchChar, boolean overlap) {
			super(searchChar, overlap);
		}

		@Override
		public int indexOf(String str, int end) {
			int startIndex = str.lastIndexOf(searchChar, end - getMatchLength());
			return startIndex;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			int endIndex = CharSequenceTools.reverseIndexOf(str, searchChar, end);
			return (endIndex != -1) ? (endIndex - getMatchLength()) : -1;
		}
	}

	// Find codePoint

	public static abstract class IFindCodePointImpl implements IStringFinderMatchLength {
		final int searchCodePoint;
		final int codePointLen;
		boolean overlap;

		public IFindCodePointImpl(int searchCodePoint, boolean overlap) {
			this.searchCodePoint = searchCodePoint;
			this.codePointLen = Character.charCount(searchCodePoint);
			this.overlap = overlap;
		}

		@Override
		public int getMatchLength() {
			return codePointLen;
		}

		@Override
		public boolean isFindOverlap() {
			return overlap;
		}
	}

	public static class FindCodePointImpl extends IFindCodePointImpl implements IStringFinderMatchLengthImpl {

		public FindCodePointImpl(int searchCodePoint, boolean overlap) {
			super(searchCodePoint, overlap);
		}

		@Override
		public int indexOf(String str, int start) {
			return str.indexOf(searchCodePoint, start);
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return CharSequenceTools.indexOf(str, searchCodePoint, start);
		}
	}

	public static class FindCodePointReverseImpl extends IFindCodePointImpl implements IStringFinderMatchLengthReverseImpl {

		FindCodePointReverseImpl(int searchCodePoint, boolean overlap) {
			super(searchCodePoint, overlap);
		}

		@Override
		public int indexOf(String str, int end) {
			int startIndex = str.lastIndexOf(searchCodePoint, end - getMatchLength());
			return startIndex;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			int endIndex = CharSequenceTools.reverseIndexOf(str, searchCodePoint, end);
			return (endIndex != -1) ? (endIndex - getMatchLength()) : -1;
		}
	}

	// Find matcher

	public static class FindMatcherImpl implements IStringFinderImpl {
		IStringMatcher matcher;
		boolean overlap;

		public FindMatcherImpl(IStringMatcher matcher, boolean overlap) {
			this.matcher = matcher;
			this.overlap = overlap;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return matcher.indexOf(str, start);
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			return matcher.indexOfEnd(str, start);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			return matcher.find(str, start);
		}

		@Override
		public boolean isFindOverlap() {
			return overlap;
		}
	}

	public static class FindMatcherReverseImpl extends FindMatcherImpl implements IStringFinderReverseImpl {

		public FindMatcherReverseImpl(CountedStringMatcher matcher, boolean overlap) {
			super(matcher, overlap);
		}
	}

	public static class FindReverseMatcherReverseImpl extends FindMatcherImpl implements IStringFinderReverseImpl {

		public FindReverseMatcherReverseImpl(IStringReverseMatcher matcher, boolean overlap) {
			super(matcher, overlap);
		}

		IStringReverseMatcher getMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return getMatcher().indexOfReverse(str, start);
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			return getMatcher().indexOfEndReverse(str, start);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			return getMatcher().findReverse(str, start);
		}
	}

}
