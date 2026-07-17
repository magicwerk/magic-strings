package org.magicwerk.strings.matcher;

import java.util.Collection;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.CharSequenceTools.CharSequenceView;
import org.magicwerk.strings.match.IMatch;

/**
 * Class {@link MultiStringMatcher} implements matching with multiple {@link IStringMatcher}.
 */
public class MultiStringMatcher implements IStringMatcher {

	IList<IStringMatcher> matchers;
	boolean fastFind;
	boolean preferLong;

	/**
	 * Construct a {@link MultiStringMatcher} with the specified {@link IStringMatcher}s.
	 */
	public MultiStringMatcher(IStringMatcher... matchers) {
		this.matchers = GapList.immutable(matchers);
	}

	/**
	 * Construct a {@link MultiStringMatcher} with the specified {@link IStringMatcher}s.
	 */
	public MultiStringMatcher(Collection<IStringMatcher> matchers) {
		this.matchers = GapList.immutable(matchers);
	}

	public IList<IStringMatcher> getMatchers() {
		return matchers;
	}

	public MultiStringMatcher setFastFind(boolean fastFind) {
		this.fastFind = fastFind;
		return this;
	}

	public MultiStringMatcher setPreferLong(boolean preferLong) {
		this.preferLong = preferLong;
		return this;
	}

	@Override
	public IMatch find(CharSequence str, int start) {
		IMatch bestMatch = null;
		for (int i = 0; i < matchers.size(); i++) {
			IStringMatcher matcher = matchers.get(i);
			CharSequence cs = str;
			if (fastFind) {
				if (bestMatch != null) {
					cs = new CharSequenceView(str, 0, bestMatch.getEnd());
				}
			}

			IMatch match = matcher.find(cs, start);
			if (match != null) {
				boolean set = false;
				if (bestMatch == null) {
					set = true;
				} else {
					int diff = match.getStart() - bestMatch.getStart();
					if (diff < 0) {
						// New best match which starts before the previous one
						set = true;
					} else if (diff == 0) {
						if (preferLong && match.getLength() > bestMatch.getLength()) {
							// New best match which starts at the same position as the previous one but is longer
							set = true;
						}
					}
				}
				if (set) {
					bestMatch = match;

					// If a match is at start position and we don't need to look for the longest match, we stop processing 
					if (!preferLong && bestMatch.getStart() == start) {
						break;
					}
				}
			}
		}
		return bestMatch;
	}

}
