package org.magicwerk.strings.matcher;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.primitive.IIntList;
import org.magicwerk.collections.primitive.IntGapList;
import org.magicwerk.strings.match.IMatch;

/**
 * Class {@link CountedStringMatcher} searches for the specified occurrence of a {@link IStringMatcher}.
 * It can search for a specific occurrence forward and backward and respects the overlap flag.
 */
public abstract class CountedStringMatcher implements IStringMatcher {

	/**
	 * Create matcher.
	 */
	public static CountedStringMatcher of(IStringMatcher matcher, int occurrence, boolean overlap) {
		if (occurrence >= 0) {
			// Forward
			if (occurrence == 0) {
				// Find first occurrence
				return new CountedStringMatcherFirst(matcher, occurrence);
			} else {
				// Find n-th occurrence
				if (!overlap) {
					return new CountedStringMatcherIndex(matcher, occurrence);
				} else {
					return new CountedStringMatcherIndexOverlap(matcher, occurrence);
				}
			}

		} else {
			// Reverse
			if (matcher instanceof IStringReverseMatcher) {
				if (occurrence == -1) {
					// Find last occurrence
					return new CountedStringMatcherReverseLast((IStringReverseMatcher) matcher, occurrence);
				} else {
					// Find n-th occurrence in reverse direction
					return new CountedStringMatcherReverseIndex((IStringReverseMatcher) matcher, occurrence);
				}
			} else {
				if (occurrence == -1) {
					// Find last occurrence through iterating
					return new CountedStringMatcherReverseLastIterate(matcher, occurrence);
				} else {
					// Find n-th occurrence in reverse direction through iterating
					return new CountedStringMatcherReverseIndexIterate(matcher, occurrence);
				}
			}
		}
	}

	//

	IStringMatcher matcher;
	int occurrence;

	//

	public CountedStringMatcher(IStringMatcher matcher, int occurrence) {
		this.matcher = matcher;
		this.occurrence = occurrence;
	}

	/** Getter for {@link #matcher} */
	public IStringMatcher getMatcher() {
		return matcher;
	}

	/** Getter for {@link #occurrence} */
	public int getOccurrence() {
		return occurrence;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (occurrence: " + occurrence + ", " + matcher + ")";
	}

	//

	/**
	 * Class {@link CountedStringMatcherFirst} retrieves the first match, so overlapping has no influence.
	 * This matcher is used if occurrence = 0.
	 */
	public static class CountedStringMatcherFirst extends CountedStringMatcher {

		public CountedStringMatcherFirst(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence == 0;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return matcher.indexOf(str, start);
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			return matcher.find(str, start);
		}
	}

	/**
	 * Class {@link CountedStringMatcherIndex} retrieves the n-th match without overlapping, specified by its index.
	 * This matcher is used if occurrence > 0.
	 */
	public static class CountedStringMatcherIndex extends CountedStringMatcher {

		public CountedStringMatcherIndex(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence > 0;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			IMatch result = null;
			int index = start;
			for (int i = 0; i <= occurrence; i++) {
				IMatch match = matcher.find(str, index);
				if (match == null) {
					return null;
				}
				result = match;
				index = match.getEnd();
			}
			return result;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			int index = start;
			for (int i = 0; i < occurrence; i++) {
				index = matcher.indexOfEnd(str, index);
				if (index == -1) {
					return -1;
				}
			}
			return matcher.indexOf(str, index);
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			int index = start;
			for (int i = 0; i <= occurrence; i++) {
				index = matcher.indexOfEnd(str, index);
				if (index == -1) {
					return -1;
				}
			}
			return index;
		}
	}

	/**
	 * Class {@link CountedStringMatcherIndexOverlap} retrieves the n-th match with overlapping, specified by its index.
	 * This matcher is used if occurrence > 0.
	 */
	public static class CountedStringMatcherIndexOverlap extends CountedStringMatcher {

		public CountedStringMatcherIndexOverlap(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence > 0;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			int index = skipOccurrences(str, start);
			return (index != -1) ? matcher.find(str, index) : null;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			int index = skipOccurrences(str, start);
			return (index != -1) ? matcher.indexOf(str, index) : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			int index = skipOccurrences(str, start);
			return (index != -1) ? matcher.indexOfEnd(str, index) : -1;
		}

		int skipOccurrences(CharSequence str, int start) {
			int index = start;
			for (int i = 0; i < occurrence; i++) {
				index = matcher.indexOf(str, index);
				if (index == -1) {
					return -1;
				}
				index++;
			}
			return index;
		}
	}

	/**
	 * Class {@link CountedStringMatcherReverse} is the abstract base class for implementations of 
	 * {@link CountedStringMatcher} working in reverse direction.
	 */
	public static abstract class CountedStringMatcherReverse extends CountedStringMatcher {

		public CountedStringMatcherReverse(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence < 0;
		}

		@Override
		public int indexOf(CharSequence str) {
			return indexOf(str, getStartPosReverse(str));
		}

		@Override
		public int indexOfEnd(CharSequence str) {
			return indexOfEnd(str, getStartPosReverse(str));
		}

		@Override
		public IMatch find(CharSequence str) {
			return find(str, getStartPosReverse(str));
		}

		int getStartPosReverse(CharSequence str) {
			return str.length();
		}
	}

	/**
	 * Class {@link CountedStringMatcherReverseLast} retrieves the last match, so overlapping has no influence.
	 * This matcher is used if occurrence = -1.
	 */
	public static class CountedStringMatcherReverseLast extends CountedStringMatcherReverse {

		public CountedStringMatcherReverseLast(IStringReverseMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence == -1;
		}

		@Override
		public IStringReverseMatcher getMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			return getMatcher().indexOfReverse(str, end);
		}

		@Override
		public int indexOfEnd(CharSequence str, int end) {
			return getMatcher().indexOfEndReverse(str, end);
		}

		@Override
		public IMatch find(CharSequence str, int end) {
			return getMatcher().findReverse(str, end);
		}
	}

	/**
	 * Class {@link CountedStringMatcherReverseIndex} retrieves the n-th match in reverse direction without overlapping,
	 * specified by its index. This matcher is used if occurrence < -1.
	 */
	public static class CountedStringMatcherReverseIndex extends CountedStringMatcherReverse {

		int count;

		public CountedStringMatcherReverseIndex(IStringReverseMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence < -1;

			count = -occurrence - 1;
		}

		@Override
		public IStringReverseMatcher getMatcher() {
			return (IStringReverseMatcher) matcher;
		}

		@Override
		public IMatch find(CharSequence str, int end) {
			int index = skipOccurrences(str, end);
			return (index != -1) ? getMatcher().findReverse(str, index) : null;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			int index = skipOccurrences(str, end);
			return (index != -1) ? getMatcher().indexOfReverse(str, index) : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int end) {
			int index = skipOccurrences(str, end);
			return (index != -1) ? getMatcher().indexOfEndReverse(str, index) : -1;
		}

		int skipOccurrences(CharSequence str, int end) {
			int index = end;
			for (int i = 0; i < count; i++) {
				index = getMatcher().indexOfReverse(str, index);
				if (index == -1) {
					return -1;
				}
				index--;
			}
			return index;
		}
	}

	/**
	 * Class {@link CountedStringMatcherReverseIndex} retrieves the n-th match in reverse direction with overlapping,
	 * specified by its index. This matcher is used if occurrence < -1.
	 */
	public static class CountedStringMatcherReverseIndexOverlap extends CountedStringMatcherReverseIndex {

		public CountedStringMatcherReverseIndexOverlap(IStringReverseMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence < -1;
		}

		@Override
		int skipOccurrences(CharSequence str, int end) {
			int index = end;
			for (int i = 0; i < count; i++) {
				index = getMatcher().indexOfEndReverse(str, index);
				if (index == -1) {
					return -1;
				}
				index--;
			}
			return index;
		}
	}

	public static class CountedStringMatcherReverseLastIterate extends CountedStringMatcherReverse {

		// Make sure to handle situation correct: AAbbAA

		public CountedStringMatcherReverseLastIterate(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence == -1;
		}

		@Override
		public IMatch find(CharSequence str, int end) {
			IMatch match = null;
			int pos = 0;
			while (true) {
				IMatch m = matcher.find(str, pos);
				if (m == null || m.getStart() >= end) {
					// No match found or match already starts after the end position
					break;
				}
				if (m.getEnd() > end) {
					// Match starts before position end, but ends after it
					// It can however be that there is a match later which fits, so just continue
					pos++;
				} else {
					// Valid match
					match = m;
					pos = m.getEnd();
				}
			}
			return match;
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			IMatch match = find(str, end);
			return (match != null) ? match.getStart() : -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int end) {
			int index = -1;
			int pos = 0;
			while (true) {
				int i = matcher.indexOfEnd(str, pos);
				if (i == -1) {
					// No match found
					break;
				}
				if (i > end) {
					// Match starts before position end, but ends after it
					// It can however be that there is a match later which fits, so just continue
					pos++;
				} else {
					// Valid match
					index = i;
					pos = i;
				}
			}
			return index;
		}
	}

	static class CountedStringMatcherReverseIndexIterate extends CountedStringMatcherReverse {
		int count;
		IIntList indexes;
		IList<IMatch> matches;

		public CountedStringMatcherReverseIndexIterate(IStringMatcher matcher, int occurrence) {
			super(matcher, occurrence);
			assert occurrence < -1;

			count = -occurrence;
			indexes = new IntGapList(count);
			matches = new GapList<>(count);
		}

		@Override
		public IMatch find(CharSequence str, int end) {
			// Basically same as CountedStringMatcherReverseLastIterate.find(), but keeping the needed number of results 
			// Changes commented with CHANGE

			matches.clear(); // CHANGE

			int pos = 0;
			while (true) {
				IMatch m = matcher.find(str, pos);
				if (m == null || m.getStart() >= end) {
					// No match found or match already starts after the end position
					break;
				}
				if (m.getEnd() > end) {
					// Match starts before position end, but ends after it
					// It can however be that there is a match later which fits, so just continue
					pos++;
				} else {
					// Valid match
					if (matches.size() == count) { // CHANGE
						matches.remove();
					}
					matches.add(m);
					pos = m.getEnd();
				}
			}

			return (matches.size() == count) ? matches.getFirst() : null; // CHANGE
		}

		@Override
		public int indexOf(CharSequence str, int end) {
			// Basically same as CountedStringMatcherReverseLastIterate.indexOf(), but keeping the needed number of results
			// Changes commented with CHANGE			

			indexes.clear(); // CHANGE

			int pos = 0;
			while (true) {
				IMatch m = matcher.find(str, pos);
				if (m == null || m.getStart() >= end) {
					// No match found or match already starts after the end position
					break;
				}
				if (m.getEnd() > end) {
					// Match starts before position end, but ends after it
					// It can however be that there is a match later which fits, so just continue
					pos++;
				} else {
					// Valid match
					if (indexes.size() == count) { // CHANGE
						indexes.remove();
					}
					indexes.add(m.getStart());
					pos = m.getEnd();
				}
			}

			return (indexes.size() == count) ? indexes.getFirst() : -1; // CHANGE
		}

		@Override
		public int indexOfEnd(CharSequence str, int end) {
			// Basically same as CountedStringMatcherReverseLastIterate.indexOf(), but keeping the needed number of results
			// Changes commented with CHANGE			

			indexes.clear(); // CHANGE

			int pos = 0;
			while (true) {
				int index = matcher.indexOfEnd(str, pos);
				if (index == -1) {
					break;
				}

				if (index > end) {
					// Match starts before position end, but ends after it
					// It can however be that there is a match later which fits, so just continue
					pos++;
				} else {
					// Valid match
					if (indexes.size() == count) { // CHANGE
						indexes.remove();
					}
					indexes.add(index);
					pos = index;
				}
			}

			return (indexes.size() == count) ? indexes.getFirst() : -1; // CHANGE
		}
	}

}
