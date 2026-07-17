package org.magicwerk.strings;

import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.BuilderHelper.BuilderFinderBase;
import org.magicwerk.strings.function.MultiPredicate.Mode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CheckTools.Check;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringFixedLenMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringReverseMatcher;
import org.magicwerk.strings.matcher.IStringStartsAtMatcher;
import org.magicwerk.strings.objects.Pair;

/**
 * Class {@link StringSplitter} implements splitting of strings.
 * Use one of the methods {@link #split}, {@link #splitFirst}, or {@link #splitLast} to get the splitted parts.
 */
public interface StringSplitter {

	/**
	 * Split string as configured and return the parts as list.
	 * if {@link #returnNullIfNotSplit} is set, null is returned if the string cannot be split.
	 */
	public IList<String> split(CharSequence str);

	/**
	 * Split string as configured and return the first part as first string of the pair, the second part will contain the rest of the string.
	 * If the string cannot be split, the first part of the returned pair will contain the input and the second part will be null.
	 */
	public Pair<CharSequence> splitFirst(CharSequence str);

	default Pair<String> splitFirst(String str) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Pair<String> ps = (Pair) splitFirst((CharSequence) str);
		return ps;
	}

	/**
	 * Split string as configured and return the last part as second string of the pair, the first part will contain the rest of the string.
	 * If the string cannot be split, the first part of the returned pair will be null and the second part will contain the input.
	 */
	public Pair<CharSequence> splitLast(CharSequence str);

	default Pair<String> splitLast(String str) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Pair<String> ps = (Pair) splitLast((CharSequence) str);
		return ps;
	}

	/**
	 * Split string as configured and return the first part.
	 */
	public String getFirst(CharSequence str);

	/**
	 * Split string as configured and return the last part.
	 */
	public String getLast(CharSequence str);

	/**
	 * Split string as configured and return the part after the first match.
	 */
	public String getAfterFirst(CharSequence str);

	/**
	 * Split string as configured and return the part before the last match.
	 */
	public String getBeforeLast(CharSequence str);

	//

	/** Build {@link StringSplitter} with specified builder function */
	public static StringSplitter build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/** Get {@link Builder} to create a {@link StringSplitter} */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringSplitter}.
	 */
	public static class Builder extends BuilderFinderBase<Builder> {
		SeparatorMode separatorMode = SeparatorMode.REMOVE;
		/** If true adjacent separators are combined to a single one, also leading or trailing separators do not create empty elements */
		boolean combineSeparators;
		/** True to ignore unmatched text before/after start/end match (otherwise an exception is thrown, only evaluated if separatorMode is STARTS or ENDS) */
		boolean ignoreUnmatchedText;
		boolean returnNullIfNotSplit;
		/** If there is no match and this flag is true, null will be returned to report this, otherwise an empty string */
		boolean partNullIfNoMatch = true;
		BiPredicate<Character, Character> detector;
		// TODO codePointDetector

		//

		public Builder setSplitDetector(BiPredicate<Character, Character> detector) {
			this.detector = detector;
			return this;
		}

		public Builder setSeparatorMode(SeparatorMode separatorMode) {
			this.separatorMode = separatorMode;
			return this;
		}

		/** Setter for {@link #combineSeparators} */
		public Builder setCombineSeparators(boolean combineSeparators) {
			this.combineSeparators = combineSeparators;
			return this;
		}

		/** Setter for {@link #ignoreUnmatchedText} */
		public Builder setIgnoreUnmatchedText(boolean ignoreUnmatchedText) {
			this.ignoreUnmatchedText = ignoreUnmatchedText;
			return this;
		}

		public Builder setReturnNullIfNotSplit(boolean returnNullIfNotSplit) {
			this.returnNullIfNotSplit = returnNullIfNotSplit;
			return this;
		}

		public Builder setPartNullIfNoMatch(boolean partNullIfNotSplit) {
			this.partNullIfNoMatch = partNullIfNotSplit;
			return this;
		}

		/** Build an instance of {@link StringSplitter} with the specified configuration */
		public StringSplitter build() {
			check();

			StringSplitterImpl s;
			if (isDefined()) {
				IStringMatcher matcher = getAsStringMatcher();

				StringSplitterFinderImpl ss = new StringSplitterFinderImpl();
				ss.matcher = matcher;
				ss.separatorMode = separatorMode;
				ss.combineSeparators = combineSeparators;
				ss.ignoreUnmatchedText = ignoreUnmatchedText;
				ss.returnNullIfNotSplit = returnNullIfNotSplit;
				s = ss;
			} else {
				s = new StringSplitterDetectorImpl(detector);
			}
			s.partNull = (partNullIfNoMatch) ? null : "";
			return s;
		}

		@Override
		void check() {
			Check.forTrue(Mode.ONE).check(isDefined(), detector != null);
		}
	}

	/** Enum {@link SeparatorMode} controls how the found separators are handled */
	public enum SeparatorMode {
		/** The separators found are removed and not included in the returned parts (default mode) */
		REMOVE,
		/** The separators found are included as own strings in the returned parts */
		OWN,
		/** The separators found are appended to the left string of the separated parts */
		ADD_LEFT,
		/** The separators found are prepended to the right string of the separated parts */
		ADD_RIGHT,
		/** 
		 * A separator is starting a string part. If there is non-empty text before the first match,
		 * it is discarded if {@link StringSplitter#ignoreUnmatchedText} it set, otherwise an exception is thrown.
		 */
		STARTS,
		/**
		 * A separator is ending a string part. If there is non-empty text after the last match,
		 * it is discarded if {@link StringSplitter#ignoreUnmatchedText} it set, otherwise an exception is thrown.
		 */
		ENDS
	}

	abstract static class StringSplitterImpl implements StringSplitter {
		/** String to use to report that no match was found, e.g. in splitFirst() (either null or empty string) */
		String partNull;
	}

	static class StringSplitterDetectorImpl extends StringSplitterImpl {

		BiPredicate<Character, Character> detector;

		StringSplitterDetectorImpl(BiPredicate<Character, Character> detector) {
			this.detector = detector;
		}

		@Override
		public IList<String> split(CharSequence str) {
			IList<String> splits = GapList.create();
			int end = str.length();
			int start = 0;
			for (int i = 1; i < end; i++) {
				char c0 = str.charAt(i - 1);
				char c1 = str.charAt(i);
				if (!detector.test(c0, c1)) {
					String split = str.subSequence(start, i).toString();
					splits.add(split);
					start = i;
				}
			}
			String split = str.subSequence(start, end).toString();
			splits.add(split);
			return splits;
		}

		@Override
		public Pair<CharSequence> splitFirst(CharSequence str) {
			int index = findFirst(str);
			if (index != -1) {
				String first = str.subSequence(0, index).toString();
				String rest = str.subSequence(index, str.length()).toString();
				return Pair.of(first, rest);
			} else {
				return Pair.of(str, partNull);
			}
		}

		@Override
		public Pair<CharSequence> splitLast(CharSequence str) {
			int index = findLast(str);
			if (index != -1) {
				String rest = str.subSequence(index, str.length()).toString();
				String last = str.subSequence(0, index).toString();
				return Pair.of(rest, last);
			} else {
				return Pair.of(partNull, str);
			}
		}

		@Override
		public String getFirst(CharSequence str) {
			int index = findFirst(str);
			if (index != -1) {
				String first = str.subSequence(0, index).toString();
				return first;
			} else {
				return partNull;
			}
		}

		int findFirst(CharSequence str) {
			int end = str.length();
			for (int i = 1; i < end; i++) {
				char c0 = str.charAt(i - 1);
				char c1 = str.charAt(i);
				if (!detector.test(c0, c1)) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public String getLast(CharSequence str) {
			int index = findLast(str);
			if (index != -1) {
				String last = str.subSequence(index, str.length()).toString();
				return last;
			} else {
				return partNull;
			}
		}

		int findLast(CharSequence str) {
			for (int i = str.length() - 1; i > 0; i--) {
				char c0 = str.charAt(i - 1);
				char c1 = str.charAt(i);
				if (!detector.test(c0, c1)) {
					return i - 1;
				}
			}
			return -1;
		}

		@Override
		public String getAfterFirst(CharSequence str) {
			int index = findFirst(str);
			if (index != -1) {
				String first = str.subSequence(index, str.length()).toString();
				return first;
			} else {
				return partNull;
			}
		}

		@Override
		public String getBeforeLast(CharSequence str) {
			int index = findLast(str);
			if (index != -1) {
				String first = str.subSequence(0, index).toString();
				return first;
			} else {
				return partNull;
			}
		}

	}

	static class StringSplitterFinderImpl extends StringSplitterImpl {

		IStringMatcher matcher;
		SeparatorMode separatorMode = SeparatorMode.REMOVE;
		/** If true adjacent separators are combined to a single one, also leading or trailing separators do not create empty elements */
		boolean combineSeparators;
		boolean ignoreUnmatchedText;
		boolean returnNullIfNotSplit;

		//

		@Override
		@SuppressWarnings("unchecked")
		public IList<String> split(CharSequence str) {
			return (IList<String>) doSplit(str, Operation.Split);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Pair<CharSequence> splitFirst(CharSequence str) {
			return (Pair<CharSequence>) doSplit(str, Operation.SplitFirst);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Pair<CharSequence> splitLast(CharSequence str) {
			return (Pair<CharSequence>) doSplit(str, Operation.SplitLast);
		}

		@Override
		public String getFirst(CharSequence str) {
			return (String) doSplit(str, Operation.GetFirst);
		}

		@Override
		public String getLast(CharSequence str) {
			return (String) doSplit(str, Operation.GetLast);
		}

		@Override
		public String getAfterFirst(CharSequence str) {
			return (String) doSplit(str, Operation.GetAfterFirst);
		}

		@Override
		public String getBeforeLast(CharSequence str) {
			return (String) doSplit(str, Operation.GetBeforeLast);
		}

		enum Operation {
			/** Do full split, return {@literal IList<String>} */
			Split,
			/** Split by first separator, return {@literal Pair<String>} */
			SplitFirst,
			/** Split by last separator, return {@literal Pair<String>} */
			SplitLast,
			/** Split by first separator, return String */
			GetFirst,
			/** Split by last separator, return String */
			GetLast,
			/** Split by first separator, return String */
			GetAfterFirst,
			/** Split by last separator, return String */
			GetBeforeLast
		}

		Object doSplit(CharSequence str, Operation op) {
			CheckTools.checkNonNull(matcher, "matcher");

			if (str == null) {
				return null;
			}

			// If combineSeparators is true, string "-a" with separator "-" must ignore the leading "-"
			if (op == Operation.GetFirst && separatorMode == SeparatorMode.REMOVE && !combineSeparators) {
				int pos = matcher.indexOf(str);
				return (pos != -1) ? str.subSequence(0, pos).toString() : str.toString();
			} else if (op == Operation.GetLast && separatorMode == SeparatorMode.REMOVE && !combineSeparators && matcher instanceof IStringReverseMatcher) {
				IStringReverseMatcher srm = (IStringReverseMatcher) matcher;
				int pos = srm.indexOfReverse(str);
				return (pos != -1) ? CharSequenceTools.substring(str, pos + 1) : str.toString();
			}

			String result = null;
			Pair<String> resultPair = null;
			IList<String> resultList = null;

			Operation op2 = null;
			SeparatorMode sm = null;
			boolean fast;
			if (op == Operation.Split && separatorMode == SeparatorMode.REMOVE) {
				op2 = op;
				sm = separatorMode;
				fast = true;
			} else {
				op2 = (combineSeparators) ? Operation.Split : op;
				sm = (combineSeparators) ? SeparatorMode.OWN : separatorMode;
				fast = false;
			}

			if (op2 == Operation.Split) {
				resultList = GapList.create();
			}

			boolean anyMatch = false;
			int lastFindPos = -1;
			int findPos = 0;
			int textStart = 0;
			int sepStart = 0;
			int lastSepStart = 0;

			while (true) {
				// Guarantee that processing advances if an empty match is encountered
				if (findPos == lastFindPos) {
					findPos++;
				}
				lastFindPos = findPos;

				// Find next match (try to avoid allocation of unneeded IMatch elements) 
				int matchStart;
				int matchEnd;
				if (matcher instanceof IStringFixedLenMatcher) {
					IStringFixedLenMatcher matcher2 = (IStringFixedLenMatcher) matcher;
					matchStart = matcher.indexOf(str, findPos);
					matchEnd = (matchStart != -1) ? matchStart + matcher2.getMatchLength() : -1;
				} else if (matcher instanceof IStringStartsAtMatcher) {
					IStringStartsAtMatcher matcher2 = (IStringStartsAtMatcher) matcher;
					matchStart = matcher.indexOf(str, findPos);
					matchEnd = (matchStart != -1) ? matcher2.indexOfEndStartingAt(str, matchStart) : -1;
				} else {
					IMatch match = matcher.find(str, findPos);
					matchStart = (match != null) ? match.getStart() : -1;
					matchEnd = (match != null) ? match.getEnd() : -1;
				}
				int textEnd = (matchStart != -1) ? matchStart : str.length();

				if (matchStart != -1) {
					if (combineSeparators) {
						if (textEnd == textStart) {
							// match with no text to add before it
							textStart = matchEnd;
							findPos = textStart;
							continue;
						} else if (textEnd == str.length()) {
							// match at end of string
							matchStart = -1;
						} else {
							sepStart = matchStart;
						}
					} else {
						sepStart = matchStart;
					}
				}

				String text = str.subSequence(textStart, textEnd).toString();
				int lastTextStart = textStart;

				if (matchStart != -1) {
					anyMatch = true;
					if (sm == SeparatorMode.REMOVE) {
						textStart = matchEnd;
						findPos = textStart;
					} else if (sm == SeparatorMode.OWN) {
						textStart = matchEnd;
						findPos = textStart;
					} else if (sm == SeparatorMode.ADD_LEFT || sm == SeparatorMode.ENDS) {
						String matchStr = str.subSequence(matchStart, matchEnd).toString();
						text += matchStr;
						textStart = matchEnd;
						findPos = textStart;
					} else if (sm == SeparatorMode.ADD_RIGHT || sm == SeparatorMode.STARTS) {
						textStart = matchStart;
						findPos = matchEnd;
					} else {
						throw new AssertionError();
					}
				} else {
					textStart = str.length();
				}

				if (op2 == Operation.Split) {
					resultList.add(text);
					if (sm == SeparatorMode.OWN && matchStart != -1) {
						String matchStr = str.subSequence(matchStart, matchEnd).toString();
						resultList.add(matchStr);
					}
				} else if (op2 == Operation.GetFirst) {
					result = text;
					break;
				} else if (op2 == Operation.GetAfterFirst) {
					int start = (sm == SeparatorMode.OWN) ? sepStart : textStart;
					String endText = (anyMatch) ? str.subSequence(start, str.length()).toString() : partNull;
					result = endText;
					break;
				} else if (op2 == Operation.SplitFirst) {
					int start = (sm == SeparatorMode.OWN) ? sepStart : textStart;
					String endText = (anyMatch) ? str.subSequence(start, str.length()).toString() : partNull;
					resultPair = Pair.of(text, endText);
					break;
				}

				if (matchStart == -1) {
					if (op2 == Operation.GetLast) {
						result = text;
					} else if (op2 == Operation.GetBeforeLast) {
						int end = (sm == SeparatorMode.REMOVE) ? lastSepStart : lastTextStart;
						String startText = (anyMatch) ? str.subSequence(0, end).toString() : partNull;
						result = startText;
					} else if (op2 == Operation.SplitLast) {
						int end = (sm == SeparatorMode.REMOVE) ? lastSepStart : lastTextStart;
						String startText = (anyMatch) ? str.subSequence(0, end).toString() : partNull;
						resultPair = Pair.of(startText, text);
					}
					break;
				}

				lastSepStart = sepStart;
			}

			if (!anyMatch && returnNullIfNotSplit) {
				return null;
			}

			if (combineSeparators && !fast) {
				return doSplitCombineSeparators(resultList, op, separatorMode);
			}

			if (op2 == Operation.Split) {
				if (!resultList.isEmpty()) {
					if (separatorMode == SeparatorMode.STARTS || separatorMode == SeparatorMode.ENDS) {
						String remove;
						if (separatorMode == SeparatorMode.STARTS) {
							remove = resultList.removeFirst();
						} else {
							remove = resultList.removeLast();
						}
						CheckTools.check(ignoreUnmatchedText || StringTools.isEmpty(remove), "set ignoreUnmatchedText to ignore text: {}", remove);
					}
				}
				return resultList;
			} else if (op2 == Operation.SplitFirst || op2 == Operation.SplitLast) {
				return resultPair;
			} else if (op2 == Operation.GetFirst || op2 == Operation.GetLast || op2 == Operation.GetAfterFirst || op2 == Operation.GetBeforeLast) {
				return result;
			} else {
				throw new AssertionError();
			}
		}

		Object doSplitCombineSeparators(IList<String> split, Operation op, SeparatorMode sm) {
			// Combine separators
			int size = split.size();
			IList<String> split2 = new GapList<>(size);
			if (sm != SeparatorMode.ADD_RIGHT) {
				int i = 0;
				while (i < size - 1) {
					String text = split.get(i);
					if (!text.isEmpty()) {
						String sep = split.get(i + 1);
						if (sm == SeparatorMode.REMOVE && op == Operation.Split) {
							split2.add(text);
						} else if (sm == SeparatorMode.ADD_LEFT) {
							split2.add(text + sep);
						} else {
							split2.add(text);
							split2.add(sep);
						}
					}
					i += 2;
				}
				split2.add(split.get(i));
			} else {
				int i = size - 1;
				while (i > 0) {
					String text = split.get(i);
					if (!text.isEmpty()) {
						String sep = split.get(i - 1);
						split2.addFirst(sep + text);
					}
					i -= 2;
				}
				split2.addFirst(split.get(i));
			}

			if (op == Operation.Split) {
				return split2;
			} else if (op == Operation.GetFirst) {
				return split2.getFirst();
			} else if (op == Operation.GetLast) {
				return split2.getLast();
			} else if (op == Operation.SplitFirst) {
				String first = split2.get(0);
				String rest = null;
				if (split2.size() > 1) {
					int d = 0;
					if (sm == SeparatorMode.REMOVE) {
						d = 1;
					}
					rest = StringTools.join(split2.getAll(1 + d, split2.size() - 1 - d));
				}
				return Pair.of(first, rest);
			} else if (op == Operation.SplitLast) {
				String last = split2.getLast();
				String rest = null;
				if (split2.size() > 1) {
					int d = 0;
					if (sm == SeparatorMode.REMOVE) {
						d = 1;
					}
					rest = StringTools.join(split2.getAll(0, split2.size() - 1 - d));
				}
				return Pair.of(rest, last);
			} else {
				throw new AssertionError();
			}
		}
	}
}