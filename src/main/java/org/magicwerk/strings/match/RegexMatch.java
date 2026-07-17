package org.magicwerk.strings.match;

import java.util.regex.MatchResult;

/**
 * Class {@link RegexMatch} represents a match of regex.
 */
public class RegexMatch extends MatchBase {
	MatchResult matchResult;

	public RegexMatch(CharSequence input, MatchResult matchResult) {
		super(input);
		this.matchResult = matchResult;
	}

	public MatchResult getMatchResult() {
		return matchResult;
	}

	@Override
	public int getStart() {
		return matchResult.start();
	}

	@Override
	public int getEnd() {
		return matchResult.end();
	}

	@Override
	public int getLength() {
		return matchResult.end() - matchResult.start();
	}

	@Override
	public String getString() {
		return matchResult.group();
	}

}