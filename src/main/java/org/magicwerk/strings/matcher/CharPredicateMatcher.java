package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link CharPredicateMatcher} searches for the occurrence of the specified {@link CharPredicate}.
 */
public class CharPredicateMatcher implements StringFixedLenMatcher {

	/**
	 * Class {@link CharPredicateIgnoreCaseMatcher} searches for the occurrence of the specified {@link CharPredicate}.
	 */
	public static class CharPredicateIgnoreCaseMatcher extends CharPredicateMatcher {

		/** Constructor */
		public CharPredicateIgnoreCaseMatcher(CharPredicate predicate) {
			super(CharCaseTools.getCharPredicateIgnoreCase(predicate)); // TODO make configurable
		}
	}

	//

	final CharPredicate predicate;

	//

	/** Create {@link CharPredicateMatcher} */
	public CharPredicateMatcher(CharPredicate predicate) {
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
		return indexToMatch(index, str, 1);
	}

	@Override
	public Match findReverse(CharSequence str, int end) {
		int index = indexOfReverse(str, end);
		return indexToMatch(index, str, 1);
	}

	@Override
	public int getMatchLength() {
		return 1;
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, predicate, index);
	}

	@Override
	public String toString() {
		return predicate.toString();
	}

}
