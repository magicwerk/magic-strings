package org.magicwerk.strings.match;

import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.helper.ObjectHelper;

/**
 * Class {@link MatchBase} represents a range in a {@link CharSequence} with start and length/end.
 */
public abstract class MatchBase implements IMatch {
	CharSequence input;

	protected MatchBase(CharSequence input) {
		this.input = input;
	}

	@Override
	public CharSequence getInput() {
		return input;
	}

	@Override
	public int hashCode() {
		return ObjectHelper.implHashCode(this, IMatch::getInput, IMatch::getStart, IMatch::getEnd);
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectHelper.implEquals(this, obj, MatchBase.class, IMatch::getInput, IMatch::getStart, IMatch::getEnd);
	}

	@Override
	public String toString() {
		return StringFormatter.format("{} [{}/{}]: {}", getClass().getSimpleName(), getStart(), getEnd(), getString());
	}
}