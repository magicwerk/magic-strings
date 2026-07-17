package org.magicwerk.strings.match;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link Match} represents a range in a {@link CharSequence} with start and length/end.
 */
public class Match extends MatchBase {
	int start;
	int end;

	/**
	 * Create specified match. Use -1 for end to specify input.length() as end.
	 */
	public Match(CharSequence input, int start, int end) {
		super(input);

		if (end == -1) {
			end = input.length();
		}
		CheckTools.check(start >= 0 && end <= input.length() && start <= end);
		this.start = start;
		this.end = end;
	}

	public StringRange getRange() {
		return StringRange.withEnd(start, end);
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public String getString() {
		return input.subSequence(getStart(), getEnd()).toString();
	}

}