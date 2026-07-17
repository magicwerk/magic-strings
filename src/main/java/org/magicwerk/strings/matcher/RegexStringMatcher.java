package org.magicwerk.strings.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magicwerk.strings.helper.RegexTools;
import org.magicwerk.strings.match.RegexMatch;
import org.magicwerk.strings.match.SimpleMatchResult;
import org.magicwerk.strings.match.SingleMatchResult;

/**
 * Class {@link RegexStringMatcher} searches for the first occurrence of the specified regular expression.
 */
public class RegexStringMatcher implements IStringMatcher, IStringIgnoreCaseMatcher {

	Pattern pattern;
	int group;

	public RegexStringMatcher setPattern(String pattern) {
		return setPattern(pattern, 0);
	}

	public RegexStringMatcher setPattern(String pattern, int flags) {
		return setPattern(RegexTools.getPattern(pattern, flags));
	}

	public RegexStringMatcher setPattern(Pattern pattern) {
		this.pattern = pattern;
		return this;
	}

	public RegexStringMatcher setGroup(int group) {
		this.group = group;
		return this;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public int getGroup() {
		return group;
	}

	@Override
	public boolean isIgnoreCase() {
		return RegexTools.isPatternWithIgnoreCase(pattern);
	}

	@Override
	public RegexMatch find(CharSequence str, int start) {
		if (start < 0) {
			start = 0;
		} else if (start >= str.length()) {
			// Matcher.find() will fail if position is outside string
			if (pattern.pattern().isEmpty()) {
				return new RegexMatch(str, new SimpleMatchResult(str, str.length(), str.length()));
			} else {
				return null;
			}
		}

		Matcher m = pattern.matcher(str);
		if (!m.find(start)) {
			return null;
		} else {
			int g = group;
			if (g == -1) {
				g = RegexTools.getSelectedGroup(m, group);
			}
			if (g == 0) {
				return new RegexMatch(str, m);
			} else {
				return new RegexMatch(str, new SingleMatchResult(m, g));
			}
		}
	}

	@Override
	public String toString() {
		return "REGEX:" + pattern + "[" + group + "]";
	}

}
