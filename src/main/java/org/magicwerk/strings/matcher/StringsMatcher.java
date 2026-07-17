package org.magicwerk.strings.matcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.primitive.IIntList;
import org.magicwerk.collections.primitive.IntGapList;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.chars.CharTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharConvertEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.CodePointConvertEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.chars.CharCaseTools.ICharMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CollectionTools;
import org.magicwerk.strings.helper.CollectionTools.OrderMode;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;
import org.magicwerk.strings.matcher.AnyCharMatcher.AnyCharIgnoreCaseMatcher;

/**
 * Class {@link StringsMatcher} searches for the first occurrence of the several literal string.
 */
public interface StringsMatcher extends IStringMatcher {

	/** Create {@link StringsSimpleMatcher} with the specified strings */
	public static StringsMatcher of(String... findStrs) {
		return new StringsSimpleMatcher(GapList.create(findStrs));
	}

	//

	/** Build {@link StringsMatcher} with specified builder function */
	public static StringsMatcher build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/** Get {@link Builder} to create a {@link StringsMatcher} */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringsMatcher}.
	 */
	public static class Builder {

		static final Comparator<CharSequence> compareByLength = Comparator.comparing(CharSequence::length).reversed();

		IList<CharSequence> findStrs;
		IList<CharSequence> findNotStrs;
		ICharMode ignoreCase;
		/**
		 * If set to true, builder assumes that the passed find strings are already sorted in the correct order,
		 * i.e. one string a is a prefix of another, the longer one will come first.
		 * This flag optimizes performance of builder as the find strings do not have to be sorted during execution.
		 */
		boolean findStrsSorted;
		// TOOD should this be controlled by client
		boolean useTree;

		//

		public Builder setSearchStrs(String... finds) {
			this.findStrs = GapList.immutable(finds);
			return this;
		}

		public Builder setSearchStrs(Collection<String> finds) {
			this.findStrs = GapList.immutable(finds);
			return this;
		}

		public Builder setSearchNotStrs(String... finds) {
			this.findNotStrs = GapList.immutable(finds);
			return this;
		}

		public Builder setSearchNotStrs(Collection<String> finds) {
			this.findNotStrs = GapList.immutable(finds);
			return this;
		}

		public Builder setIgnoreCase(boolean ignoreCase) {
			return setIgnoreCase(CharMode.getCharMode(ignoreCase));
		}

		public Builder setIgnoreCase(ICharMode ignoreCase) {
			this.ignoreCase = ignoreCase;
			return this;
		}

		/** Setter for {@link #useTree} */
		public Builder setUseTree(boolean useTree) {
			this.useTree = useTree;
			return this;
		}

		//

		public StringsMatcher build() {
			check();

			if (useTree) {
				return buildStringsTreeMatcher();
			} else {
				return buildStringsFirstMatcher();
			}
		}

		void check() {
			CheckTools.check(findStrs != null, "searchStrs must be specified");
		}

		StringsTreeMatcher buildStringsTreeMatcher() {
			StringsTreeMatcher sm = StringsTreeMatcher.of(findStrs, findNotStrs, ignoreCase);
			return sm;
		}

		StringsFirstMatcher buildStringsFirstMatcher() {
			StringsFirstMatcher sm = new StringsFirstMatcher();
			sm.searchStrs = findStrs;
			sm.searchNotStrs = findNotStrs;

			if (!findStrsSorted) {
				OrderMode om = CollectionTools.getOrderMode(findStrs, compareByLength);
				if (om == OrderMode.UNORDERED || om == OrderMode.ORDERD_DESC) {
					IList<CharSequence> strs = GapList.create(findStrs);
					strs.sort(compareByLength);
					sm.searchStrs = strs;
				}
			}

			int num = sm.searchStrs.size();
			IString firstChars = new GapString(num);
			IString lastChars = new GapString(num);
			IIntList firstCodePoints = null;
			IIntList lastCodePoints = null;

			boolean hasCodePoints = false;
			for (CharSequence str : sm.searchStrs) {
				if (StringTools.isEmpty(str)) {
					sm.hasEmpty = true;
				} else {
					int firstCodePoint = CodePointTools.firstCodePoint(str);
					int lastCodePoint = CodePointTools.lastCodePoint(str);
					if (!hasCodePoints) {
						hasCodePoints = !CodePointTools.isCharCodePoint(firstCodePoint) || !CodePointTools.isCharCodePoint(lastCodePoint);
					}
					if (hasCodePoints) {
						if (firstCodePoints == null) {
							firstCodePoints = new IntGapList(num);
							for (int i = 0; i < firstChars.size(); i++) {
								firstCodePoints.add(firstChars.get(i));
							}
							lastCodePoints = new IntGapList(num);
							for (int i = 0; i < lastChars.size(); i++) {
								lastCodePoints.add(lastChars.get(i));
							}
						}
						firstCodePoints.add(CodePointTools.firstCodePoint(str));
						lastCodePoints.add(CodePointTools.lastCodePoint(str));
					} else {
						firstChars.add(StringTools.firstChar(str));
						lastChars.add(StringTools.lastChar(str));
					}
				}
			}

			// TODO handle firstCodePoints/lastCodePoints
			sm.searchFirstChars = firstChars.toString();
			sm.searchLastChars = lastChars.toString();
			if (ignoreCase != null) {
				sm.equal = ignoreCase.getCharEqual();
				sm.searchFirstCharMatcher = new AnyCharIgnoreCaseMatcher(sm.searchFirstChars, sm.equal);
				sm.searchLastCharMatcher = new AnyCharIgnoreCaseMatcher(sm.searchLastChars, sm.equal);
			} else {
				sm.searchFirstCharMatcher = new AnyCharMatcher(sm.searchFirstChars);
				sm.searchLastCharMatcher = new AnyCharMatcher(sm.searchLastChars);
			}
			return sm;
		}
	}

	//

	/**
	 * Class {@link StringsMatcherImpl} is the abstract base class for all implementations of {@link StringsMatcher}.
	 */
	abstract static class StringsMatcherImpl implements StringsMatcher {

		List<? extends CharSequence> searchStrs;
		List<? extends CharSequence> searchNotStrs;

		/** Return true if the match find[findStart..findLen] is part of string which are not allowed to match */
		boolean matchNot(CharSequence find, int findStart, int findLen) {
			if (searchNotStrs == null) {
				return false;
			}
			return CharSequenceTools.matchNot(searchNotStrs, find, findStart, findLen);
		}

		@Override
		public String toString() {
			return "StringsMatcher [searchStrs=" + searchStrs + ", searchNotStrs=" + searchNotStrs + "]";
		}
	}

	/**
	 * Class {@link StringsSimpleMatcher} implements matching several strings for easy cases.
	 * It should only be used if it is guaranteed that there are only a few short strings. 
	 */
	public static class StringsSimpleMatcher extends StringsMatcherImpl implements IStringStartsAtMatcher, IStringEndsAtMatcher {

		/** Create {@link StringsSimpleMatcher} */
		public StringsSimpleMatcher(List<? extends CharSequence> findStrs) {
			this.searchStrs = findStrs;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			int end = str.length();
			int bestIndex = -1;
			CharSequence bestFind = null;
			for (CharSequence find : searchStrs) {
				if (bestIndex != -1) {
					// No need to search until end of string if we have already a match
					end = bestIndex + find.length();
				}
				int index = CharSequenceTools.doIndexOfCharSequence(str, find, start, end);
				if (index != -1) {
					if ((bestIndex == -1) || (index < bestIndex || (index == bestIndex && find.length() > bestFind.length()))) {
						bestIndex = index;
						bestFind = find;
					}
				}
			}
			return bestIndex;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			// Same implementation as indexOf() but also maintains bestLen

			int end = str.length();
			int bestIndex = -1;
			int bestLen = -1;
			CharSequence bestFind = null;
			for (CharSequence find : searchStrs) {
				if (bestIndex != -1) {
					// No need to search until end of string if we have already a match
					end = bestIndex + find.length();
				}
				int index = CharSequenceTools.doIndexOfCharSequence(str, find, start, end);
				if (index != -1) {
					if ((bestIndex == -1) || (index < bestIndex || (index == bestIndex && find.length() > bestFind.length()))) {
						bestIndex = index;
						bestFind = find;
						bestLen = find.length();
					}
				}
			}
			return indexToMatch(bestIndex, str, bestLen);
		}

		// IStringStartsAtMatcher

		@Override
		public boolean startsAt(CharSequence str, int start) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.startsAt(str, find, start)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.startsAt(str, find, start)) {
					return start + find.length();
				}
			}
			return -1;
		}

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.startsAt(str, find, start)) {
					return indexToMatch(start, str, find.length());
				}
			}
			return null;
		}

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.endsAt(str, find, end)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.endsAt(str, find, end)) {
					return end - find.length();
				}
			}
			return -1;
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			for (CharSequence find : searchStrs) {
				if (CharSequenceTools.endsAt(str, find, end)) {
					return indexToMatch(end - find.length(), str, find.length());
				}
			}
			return null;
		}
	}

	public static class StringsFirstMatcher extends StringsMatcherImpl implements StringStartsEndsMatcher {

		String searchFirstChars;
		String searchLastChars;
		AnyCharMatcher searchFirstCharMatcher;
		AnyCharMatcher searchLastCharMatcher;
		boolean hasEmpty;
		CharEqual equal;

		//

		boolean match(char c0, char c1) {
			if (equal != null) {
				return equal.isEqualChar(c0, c1);
			} else {
				return c0 == c1;
			}
		}

		boolean matchAt(CharSequence str, CharSequence searchStr, int start) {
			if (equal != null) {
				return CharSequenceTools.startsAt(str, searchStr, start, equal);
			} else {
				return CharSequenceTools.startsAt(str, searchStr, start);
			}
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = start;
				} else {
					index = indexOfFirst(str, start);
					if (index == -1) {
						return -1;
					}
				}

				boolean match = (Boolean) matchAt(str, index, MatchAtMode.RETURN_IS_MATCH);
				if (match) {
					return index;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, start);
					return pos;
				}

				start = index + 1;
			}
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = start;
				} else {
					index = indexOfFirst(str, start);
					if (index == -1) {
						return -1;
					}
				}

				int endIndex = (Integer) matchAt(str, index, MatchAtMode.RETURN_MATCH_REVERSE_INDEX);
				if (endIndex != -1) {
					return endIndex;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, start);
					return pos;
				}

				start = index + 1;
			}
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = start;
				} else {
					index = indexOfFirst(str, start);
					if (index == -1) {
						return null;
					}
				}

				IMatch match = (IMatch) matchAt(str, index, MatchAtMode.RETURN_MATCH);
				if (match != null) {
					return match;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, start);
					return new Match(str, pos, pos);
				}

				start = index + 1;
			}
		}

		//

		@Override
		public IMatch findReverse(CharSequence str, int end) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = end;
				} else {
					index = indexOfLast(str, end);
					if (index == -1) {
						return null;
					}
				}

				IMatch match = (IMatch) lastMatchAt(str, index, MatchAtMode.RETURN_MATCH);
				if (match != null) {
					return match;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, end);
					return new Match(str, pos, pos);
				}

				end = index - 1;
			}
		}

		@Override
		public int indexOfReverse(CharSequence str, int end) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = end;
				} else {
					index = indexOfLast(str, end);
					if (index == -1) {
						return -1;
					}
				}

				int matchStart = (Integer) lastMatchAt(str, index, MatchAtMode.RETURN_MATCH_REVERSE_INDEX);
				if (matchStart != -1) {
					return matchStart;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, end);
					return pos;
				}

				end = index - 1;
			}
		}

		@Override
		public int indexOfEndReverse(CharSequence str, int end) {
			while (true) {
				int index;
				if (hasEmpty) {
					index = end;
				} else {
					index = indexOfLast(str, end);
					if (index == -1) {
						return -1;
					}
				}

				boolean match = (Boolean) lastMatchAt(str, index, MatchAtMode.RETURN_IS_MATCH);
				if (match) {
					return index;
				} else if (hasEmpty) {
					int pos = getEmptyPos(str, end);
					return pos;
				}

				end = index - 1;
			}
		}

		int indexOfFirst(CharSequence str, int start) {
			return searchFirstCharMatcher.indexOf(str, start);
		}

		int indexOfLast(CharSequence str, int end) {
			return searchLastCharMatcher.indexOfEndReverse(str, end);
		}

		//

		enum MatchAtMode {
			RETURN_MATCH,
			RETURN_IS_MATCH,
			RETURN_MATCH_REVERSE_INDEX
		}

		Object matchAt(CharSequence str, int start, MatchAtMode mode) {
			if (start >= 0 && start < str.length()) {
				char firstChar = str.charAt(start);
				for (int i = 0; i < searchFirstChars.length(); i++) {
					char c = searchFirstChars.charAt(i);
					if (!match(c, firstChar)) {
						continue;
					}

					CharSequence searchStr = searchStrs.get(i);
					int searchStrLen = searchStr.length();
					boolean match = matchAt(str, searchStr, start);
					if (match) {
						if (!matchNot(str, start, searchStrLen)) {
							return match(mode, str, start, start + searchStrLen, start + searchStrLen);
						}
					}
				}
			}
			return noMatch(mode);
		}

		Object lastMatchAt(CharSequence str, int end, MatchAtMode mode) {
			if (end > 0 && end <= str.length()) {
				char lastChar = str.charAt(end - 1);
				for (int i = 0; i < searchLastChars.length(); i++) {
					char c = searchLastChars.charAt(i);
					if (!match(c, lastChar)) {
						continue;
					}

					CharSequence searchStr = searchStrs.get(i);
					int searchStrLen = searchStr.length();
					boolean match = matchAt(str, searchStr, end - searchStrLen);
					if (match) {
						return match(mode, str, end - searchStrLen, end, end - searchStrLen);
					}
				}
			}
			return noMatch(mode);
		}

		Object match(MatchAtMode mode, CharSequence str, int start, int end, int index) {
			switch (mode) {
			case RETURN_MATCH:
				return new Match(str, start, end);
			case RETURN_IS_MATCH:
				return true;
			case RETURN_MATCH_REVERSE_INDEX:
				return index;
			default:
				throw new AssertionError();
			}
		}

		Object noMatch(MatchAtMode mode) {
			switch (mode) {
			case RETURN_MATCH:
				return null;
			case RETURN_IS_MATCH:
				return false;
			case RETURN_MATCH_REVERSE_INDEX:
				return -1;
			default:
				throw new AssertionError();
			}

		}

		// IStringStartsAtMatcher

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			IMatch match = (IMatch) matchAt(str, start, MatchAtMode.RETURN_MATCH);
			if (match == null) {
				if (hasEmpty) {
					if (start == getEmptyPos(str, start)) {
						return new Match(str, start, start);
					}
				}
			}
			return match;
		}

		@Override
		public boolean startsAt(CharSequence str, int start) {
			Boolean isMatch = (Boolean) matchAt(str, start, MatchAtMode.RETURN_IS_MATCH);
			if (!isMatch) {
				if (hasEmpty) {
					if (start == getEmptyPos(str, start)) {
						return true;
					}
				}
			}
			return isMatch;
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			Integer matchIndex = (Integer) matchAt(str, start, MatchAtMode.RETURN_MATCH_REVERSE_INDEX);
			if (matchIndex == -1) {
				if (hasEmpty) {
					if (start == getEmptyPos(str, start)) {
						return start;
					}
				}
			}
			return matchIndex;
		}

		// IStringEndsAtMatcher

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			IMatch match = (IMatch) lastMatchAt(str, end, MatchAtMode.RETURN_MATCH);
			if (match == null) {
				if (hasEmpty) {
					if (end == getEmptyPos(str, end)) {
						return new Match(str, end, end);
					}
				}
			}
			return match;
		}

		@Override
		public boolean endsAt(CharSequence str, int end) {
			Boolean isMatch = (Boolean) lastMatchAt(str, end, MatchAtMode.RETURN_IS_MATCH);
			if (!isMatch) {
				if (hasEmpty) {
					if (end == getEmptyPos(str, end)) {
						return true;
					}
				}
			}
			return isMatch;
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			Integer matchIndex = (Integer) lastMatchAt(str, end, MatchAtMode.RETURN_MATCH_REVERSE_INDEX);
			if (matchIndex == -1) {
				if (hasEmpty) {
					if (end == getEmptyPos(str, end)) {
						return end;
					}
				}
			}
			return matchIndex;
		}
	}

	/**
	 * Class {@link StringsTreeMatcher} searches for multiple strings in a tree structure.
	 */
	public abstract static class StringsTreeMatcher extends StringsMatcherImpl {

		static class MatchNode {
			/** 
			 * First character of matching string (code point used as key in allChildNodes of parent)
			 * (not used for root MatchNode).
			 */
			int firstMatch;
			/**
			 * Constant string after matchCodePoint and before the nodes allowed by allChildNodes
			 * (may be null).
			 */
			String matchString;
			/** 
			 * Allowed character after matchCodePoint/remainingString
			 * (null if matchCodePoint/remainingString is the end of the match)
			 */
			Map<Integer, MatchNode> allChildNodes;
			/** Represents the maximum length of a match which can be found starting from this node */
			int minLen;
			/** If true, there is a valid match up to this node if no longer match is found */
			boolean end;

			@Override
			public String toString() {
				return "MatchNode [matchChar=" + firstMatch + " " + printChar(firstMatch) + ", startString=" + matchString + ", allChildChars=" +
						((allChildNodes != null) ? allChildNodes.keySet() : "null") + ", end=" + end + ", minLen=" + minLen + "]";
			}

			String printChar(int c) {
				if (CharTools.isPrint(c)) {
					return Character.toString(c);
				} else {
					return CharTools.toUnicodeNumber(c);
				}
			}
		}

		//

		MatchNode root;
		/** True if there is an empty search string matching at every position */
		boolean hasEmpty;
		StringsMatcherHelperImpl helperImpl;

		//

		public String printSearchStrings() {
			return StringPrinter.formatLines(getSearchString());
		}

		public Set<String> getSearchString() {
			Set<String> searchStrings = new TreeSet<>();
			if (hasEmpty) {
				searchStrings.add("");
			}
			if (root != null) {
				doGetSearchStrings(root, null, searchStrings);
			}
			return searchStrings;
		}

		void doGetSearchStrings(MatchNode node, String prefix, Set<String> searchStrings) {
			String s;
			if (prefix == null) {
				s = "";
			} else {
				s = prefix + Character.toString(node.firstMatch);
			}
			if (node.matchString != null) {
				s += node.matchString;
			}
			if (node.end) {
				searchStrings.add(s);
			}
			if (node.allChildNodes != null) {
				for (MatchNode mn : node.allChildNodes.values()) {
					doGetSearchStrings(mn, s, searchStrings);
				}
			}
		}

		public String printTree() {
			if (root == null) {
				return null;
			}
			StringPrinter buf = new StringPrinter();
			doPrintTree(buf, root);
			return buf.toString();
		}

		void doPrintTree(StringPrinter buf, MatchNode node) {
			buf.println("{}", node);
			if (node.allChildNodes == null) {
				return;
			}
			buf.indent();
			IList<MatchNode> mns = GapList.create(node.allChildNodes.values());
			mns.sort(Comparator.comparing(mn -> mn.firstMatch));
			mns.forEach(mn -> doPrintTree(buf, mn));
			buf.unindent();
		}

		/** Create {@link MatchNode} with matchCodePoint / remainingString */
		static MatchNode createMatchNode(CharSequence str, StringsMatcherHelperImpl cpv) {
			int c = CodePointTools.firstCodePoint(str);
			c = cpv.convert(c);

			int i = CodePointTools.charCount(c);
			String rs = null;
			if (i < str.length()) {
				rs = str.subSequence(i, str.length()).toString();
			}

			MatchNode mn = new MatchNode();
			mn.firstMatch = c;
			mn.matchString = rs;
			return mn;
		}

		static MatchNode createFirstNode(CharSequence str) {
			MatchNode mn = new MatchNode();
			mn.matchString = str.toString();
			mn.minLen = str.length();
			mn.end = true;
			return mn;
		}

		static MatchNode getNodeByChar(MatchNode node, int cp, StringsMatcherHelperImpl cpv) {
			cp = cpv.convert(cp);
			return (node.allChildNodes != null) ? node.allChildNodes.get(cp) : null;
		}

		static void addStringToTree(MatchNode node, CharSequence str, StringsMatcherHelperImpl cpc) {
			int end = str.length();
			int index = -1;
			while (index < end) {
				index++;
				int len = end - index;
				if (index > 0) {
					len++;
				}
				node.minLen = Math.min(node.minLen, len);

				// firstChar has 
				if (node.matchString != null) {
					int commonLen = CharSequenceTools.commonLength(node.matchString, 0, str, index);
					if (commonLen == node.matchString.length()) {
						index += commonLen;
						if (index == end) {
							break; // same string already added
						}
						// continue with next node

					} else {
						// split existing node
						String splitHead = CharSequenceTools.substring(node.matchString, 0, commonLen);
						String splitTail = CharSequenceTools.substring(node.matchString, commonLen);
						String newTail = CharSequenceTools.substring(str, index + commonLen, end);

						MatchNode child0 = createMatchNode(splitTail, cpc);
						child0.allChildNodes = node.allChildNodes;

						node.matchString = splitHead;
						node.end = newTail.isEmpty();
						node.allChildNodes = new HashMap<>();

						child0.minLen = StringTools.length(child0.matchString) + 1;
						child0.end = true;
						node.allChildNodes.put(child0.firstMatch, child0);

						if (newTail.length() > 0) {
							MatchNode child1 = createMatchNode(newTail, cpc);
							child1.minLen = StringTools.length(child1.matchString) + 1;
							child1.end = true;
							node.allChildNodes.put(child1.firstMatch, child1);
						}
						break;
					}
				}

				// continue with next node
				int cp = str.charAt(index);
				MatchNode mn = getNodeByChar(node, cp, cpc);
				if (mn == null) {
					// create new node
					// Existing node can be remain, just add new child to it
					if (node.allChildNodes == null) {
						node.allChildNodes = new HashMap<>();
					}
					mn = createMatchNode(str.subSequence(index, end), cpc);
					mn.minLen = StringTools.length(mn.matchString) + 1;
					mn.end = true;

					node.allChildNodes.put(mn.firstMatch, mn);
					break;
				}
				node = mn;
			}
		}

		static StringsTreeMatcher of(IList<CharSequence> searchStrs, IList<CharSequence> searchNotStrs, ICharMode ignoreCase) {
			boolean hasCp = false;
			boolean hasEmpty = false;
			StringBuilder buf = new StringBuilder();
			for (CharSequence searchStr : searchStrs) {
				if (searchStr.length() == 0) {
					hasEmpty = true;
				} else {
					int cp = CodePointTools.firstCodePoint(searchStr);
					buf.appendCodePoint(cp);
					if (!CodePointTools.isCharCodePoint(cp)) {
						hasCp = true;
					}
				}
			}

			StringsTreeMatcher stm;
			if (hasCp) {
				stm = new StringsTreeCodePointMatcher();
			} else {
				stm = new StringsTreeCharMatcher();
			}

			stm.searchStrs = searchStrs;
			stm.searchNotStrs = searchNotStrs;
			stm.hasEmpty = hasEmpty;

			// TODO ignore case
			if (ignoreCase == null) {
				stm.helperImpl = new StringsMatcherHelperImpl();
			} else {
				stm.helperImpl = new StringsMatcherHelperCodePointImpl();
			}

			stm.root = createMatchNodeTree(searchStrs, stm.helperImpl);
			stm.check();
			return stm;
		}

		static MatchNode createMatchNodeTree(IList<CharSequence> searchStrs, StringsMatcherHelperImpl cpv) {
			MatchNode node = null;
			for (CharSequence str : searchStrs) {
				if (str.length() > 0) {
					if (node == null) {
						node = createFirstNode(str);
					} else {
						addStringToTree(node, str, cpv);
					}
				}
			}
			return node;
		}

		void check() {
			if (root != null) {
				Set<String> searchStrs = getSearchString();
				int minLen = CollectionTools.minInt(searchStrs, s -> (!s.isEmpty()) ? s.length() : Integer.MAX_VALUE);
				if (minLen != root.minLen) {
					System.out.println(printTree()); // FIXME
				}
				assert minLen == root.minLen;
			}
		}

		@Override
		public Match find(CharSequence str, int start) {
			if (!hasEmpty) {
				// Assert root != null (there is always at least one match string)
				if (start < 0) {
					start = 0;
				}
				return doFind(str, start);

			} else {
				Match match = null;
				if (root != null) {
					match = doMatch(str, start);
				}
				if (match == null) {
					int pos = getEmptyPos(str, start);
					match = new Match(str, pos, pos);
				}
				return match;
			}
		}

		abstract Match doFind(CharSequence str, int start);

		abstract int getCharAt(CharSequence str, int index);

		/** Try to find match at given position */
		Match doMatch(CharSequence str, int index) {
			MatchNode node = root;
			int start = index;
			int end = -1;
			int len = str.length();
			index--;
			while (index < len) {
				index++;
				if (node.matchString != null) {
					if (helperImpl.startsAt(str, node.matchString, index)) {
						index += node.matchString.length();
					} else {
						break;
					}
				}

				if (node.end) {
					// this would be a valid match, check whether we still find a longer one
					end = index;
				}

				if (index >= str.length()) {
					break;
				}

				int cp = getCharAt(str, index);
				node = getNodeByChar(node, cp);
				if (node == null) {
					// no more matching characters found, return what we have until now
					break;
				}

				int maxLen = len - index;
				if (node.minLen > maxLen) {
					// all available matches are too long
					break;
				}
			}

			if (end != -1) {
				if (!matchNot(str, start, end - start)) {
					return new Match(str, start, end);
				}
			}
			return null;
		}

		MatchNode getNodeByChar(MatchNode node, int cp) {
			cp = helperImpl.convert(cp);
			return (node.allChildNodes != null) ? node.allChildNodes.get(cp) : null;
		}

	}

	//

	static class StringsMatcherHelperImpl {
		int convert(int cp) {
			return cp;
		}

		boolean startsAt(CharSequence str, CharSequence find, int start) {
			return CharSequenceTools.startsAt(str, find, start);
		}
	}

	static class StringsMatcherHelperCharImpl extends StringsMatcherHelperImpl {
		CharEqual equal; // FIXME
		CharConvertEqual conv;

		@Override
		int convert(int cp) {
			return conv.convert((char) cp);
		}

		@Override
		boolean startsAt(CharSequence str, CharSequence find, int start) {
			return CharSequenceTools.startsAt(str, find, start, equal);
		}
	}

	static class StringsMatcherHelperCodePointImpl extends StringsMatcherHelperImpl {
		CodePointEqual equal = CodePointEqual.isEqualCodePointIgnoreCase(); // FIXME
		CodePointConvertEqual conv = CodePointConvertEqual.convertIgnoreCase();

		@Override
		int convert(int cp) {
			return conv.convert(cp);
		}

		@Override
		boolean startsAt(CharSequence str, CharSequence find, int start) {
			return CharSequenceTools.startsAt(str, find, start, equal);
		}
	}

	//

	public static class StringsTreeCharMatcher extends StringsTreeMatcher {

		@Override
		Match doFind(CharSequence str, int start) {
			int end = str.length() - root.minLen;
			while (start <= end) {
				Match match = doMatch(str, start);
				if (match != null) {
					return match;
				}
				start++;
			}
			return null;
		}

		@Override
		int getCharAt(CharSequence str, int index) {
			return str.charAt(index);
		}
	}

	public static class StringsTreeCodePointMatcher extends StringsTreeMatcher {

		@Override
		Match doFind(CharSequence str, int start) {
			int end = str.length() - root.minLen;
			while (start <= end) {
				Match match = doMatch(str, start);
				if (match != null) {
					return match;
				}
				int cp = CodePointTools.codePointAt(str, start);
				start += CodePointTools.charCount(cp);
			}
			return null;
		}

		@Override
		int getCharAt(CharSequence str, int index) {
			return CodePointTools.codePointAt(str, index);
		}
	}

}
