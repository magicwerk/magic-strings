package org.magicwerk.strings.matcher;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link NestedStringMatcher} implements nested matching, i.e. the second matcher is only applied to the text
 * already matched by the first matcher and so on.
 */
public class NestedStringMatcher implements IStringMatcher {

	static class MatchState {
		IMatch match;
		int pos;

		MatchState(IMatch match, int pos) {
			this.match = match;
			this.pos = pos;
		}
	}

	// Configuration
	IList<IStringMatcher> matchers;
	// State
	CharSequence input;
	IList<MatchState> matchStack;
	IMatch match;

	/**
	 * Construct a {@link NestedStringMatcher} with the specified {@link IStringMatcher}s.
	 */
	public NestedStringMatcher(IStringMatcher... matchers) {
		this.matchers = GapList.create(matchers);
	}

	public IList<IStringMatcher> getMatchers() {
		return matchers;
	}

	@Override
	public IMatch find(CharSequence str, int start) {
		// Check whether cached state can be reused
		boolean init = false;
		if (str != input) {
			init = true;
		} else if (match == null || start < match.getStart() || start > match.getEnd()) {
			init = true;
		}

		if (init) {
			// Initialize new cache state
			init(str);
		} else if (match != null && start == match.getStart()) {
			// Last match can be reused
			return match;
		}

		// Find next match
		match = findMatch(start);
		return match;
	}

	void init(CharSequence str) {
		this.input = str;

		IMatch find = new Match(str, 0, str.length());
		matchStack = GapList.create(new MatchState(find, 0));
		match = null;
	}

	IMatch findMatch(int start) {
		while (!matchStack.isEmpty()) {
			int index = matchStack.size() - 1;
			MatchState matchState = matchStack.getLast();
			IMatch match = matchState.match;
			int pos = matchState.pos;

			// We always use a string starting at the input of the base input string and not the current match.
			// Therefore it is guaranteed that index 0 (e.g. for ^ in regex) only matches at the start of the base string.
			String str = match.getInput().subSequence(0, match.getEnd()).toString();
			IStringMatcher matcher = matchers.get(index);
			IMatch newMatch = matcher.find(str, pos);

			// No match found
			if (newMatch == null) {
				matchStack.removeLast();
				continue;
			}

			// Match found
			matchState.pos = newMatch.getEnd();

			if (index != matchers.size() - 1) {
				// Not last matcher, add match to stack and continue with next matcher
				matchStack.add(new MatchState(newMatch, newMatch.getStart()));
			} else {
				// Last matcher, return match if it satisfies the start condition
				if (newMatch.getStart() >= start) {
					return newMatch;
				}
			}
		}
		return null;
	}

}
