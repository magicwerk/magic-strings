package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.Match;

/**
 * Class {@link EscapeStringMatcher} looks for a character but ignores them if they are escaped.
 * Example: An EscapeStringMatcher('\'', '\'') will match a single quote which is not escaped by another single quote.
 */
public class EscapeStringMatcher implements IStringMatcher {
	char match;
	char escape;
	/** If true, the esacpe character escapes itself */
	boolean selfEscape = true;

	/**
	 * Constructor.
	 *
	 * @param match		character to match
	 * @param escape	character to use for escape
	 */
	public EscapeStringMatcher(char match, char escape) {
		this.match = match;
		this.escape = escape;
	}

	@Override
	public Match find(CharSequence str, int start) {
		int len = str.length();
		int posMatch;

		while (true) {
			posMatch = -1;
			int posEscape = -1;
			for (int i = start; i < len; i++) {
				char c = str.charAt(i);
				if (c == match) {
					posMatch = i;
					break;
				} else if (c != escape) {
					posEscape = -1;
				} else if (posEscape == -1) {
					posEscape = i;
				}
			}
			if (posMatch == -1) {
				break;
			}

			// Match
			if (escape != match) {
				if (!selfEscape) {
					break;
				} else {
					if (posEscape == -1) {
						break;
					} else if ((posMatch - posEscape) % 2 == 0) {
						break;
					}
					start = posMatch + 1;
				}
			} else {
				if (posMatch == len - 1) {
					// no room for escape character
					break;
				} else {
					if (str.charAt(posMatch + 1) != escape) {
						break;
					}
					start = posMatch + 2;
					// str[pos]: escape (== match)
					// str[pos+1]: match
				}
			}
		}
		if (posMatch == -1) {
			return null;
		} else {
			return new Match(str, posMatch, posMatch + 1);
		}
	}

}
