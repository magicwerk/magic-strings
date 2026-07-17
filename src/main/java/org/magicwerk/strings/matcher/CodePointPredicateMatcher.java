package org.magicwerk.strings.matcher;

import java.util.function.IntPredicate;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link CodePointPredicateMatcher} searches for the occurrence of the specified {@link IntPredicate}.
 */
public class CodePointPredicateMatcher implements StringStartsEndsMatcher {

	/**
	 * Class {@link CodePointPredicateIgnoreCaseMatcher} searches for the occurrence of the specified {@link CharPredicate}.
	 */
	public static class CodePointPredicateIgnoreCaseMatcher extends CodePointPredicateMatcher {

		/** Constructor */
		public CodePointPredicateIgnoreCaseMatcher(IntPredicate predicate) {
			super(CharCaseTools.getCodePointPredicateIgnoreCase(predicate)); // TODO make configurable
		}
	}

	//

	final IntPredicate predicate;

	//

	/** Create {@link CodePointPredicateMatcher} */
	public CodePointPredicateMatcher(IntPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, predicate, start);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		return CharSequenceTools.reverseIndexOf(str, predicate, end);
	}

	@Override
	public Match find(CharSequence str, int start) {
		int index = indexOf(str, start);
		if (index == -1) {
			return null;
		}
		int cp = CodePointTools.codePointAt(str, index);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(index, str, len);
	}

	@Override
	public Match findReverse(CharSequence str, int end) {
		int index = indexOfEndReverse(str, end);
		if (index == -1) {
			return null;
		}
		int cp = CodePointTools.codePointBefore(str, index);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(index - len, str, len);
	}

	@Override
	public String toString() {
		return predicate.toString();
	}

	@Override
	public IMatch matchStartingAt(CharSequence str, int start) {
		boolean starts = CharSequenceTools.startsAt(str, predicate, start);
		if (!starts) {
			return null;
		}

		int cp = CodePointTools.codePointAt(str, start);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(start, str, len);
	}

	@Override
	public IMatch matchEndingAt(CharSequence str, int end) {
		boolean ends = CharSequenceTools.endsAt(str, predicate, end);
		if (!ends) {
			return null;
		}

		int cp = CodePointTools.codePointBefore(str, end);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(end - len, str, len);
	}

}
