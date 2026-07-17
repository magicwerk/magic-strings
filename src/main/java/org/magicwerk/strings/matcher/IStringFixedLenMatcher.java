package org.magicwerk.strings.matcher;

/**
 * Interface {@link IStringFixedLenMatcher} is implemented by {@link IStringMatcher} where each match has a the same fixed length, 
 * i.e. the length is independent of the match. Examples are matching a literal string or character.
 */
public interface IStringFixedLenMatcher extends IStringMatcher {

	/** Returns the fixed length of a match */
	int getMatchLength();

	@Override
	default int indexOfEnd(CharSequence str) {
		int index = indexOf(str);
		return (index != -1) ? index + getMatchLength() : -1;
	}

	@Override
	default int indexOfEnd(CharSequence str, int start) {
		int index = indexOf(str, start);
		return (index != -1) ? index + getMatchLength() : -1;
	}
}
