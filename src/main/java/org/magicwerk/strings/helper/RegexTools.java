/*
 * Copyright 2010 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.strings.helper;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.text.TextTools;

/**
 * Class {@link RegexTools} provides helper functions for regular expressions.
 */
public class RegexTools {

	/**
	 * Determines whether the passed regular expression can be used unchanged as string literal.
	 */
	public static boolean isSimpleStringLiteral(String regex) {
		for (int i = 0; i < regex.length(); i++) {
			char c = regex.charAt(i);
			if (REGEX_SPECIAL_CHARS.indexOf(c) != -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns string literal which is equal to the passed regular expression,
	 * null if the regex does not represent a string literal. 
	 */
	public static String getStringLiteral(String regex) {
		StringBuilder buf = null;
		int len = regex.length();
		for (int i = 0; i < len; i++) {
			char c = regex.charAt(i);
			if (c == REGEX_ESCAPE_CHAR) {
				if (buf == null) {
					buf = new StringBuilder(len);
					if (i > 0) {
						buf.append(regex.substring(0, i));
					}
					if (i + 1 < len) {
						buf.append(regex.charAt(i + 1));
						i++;
					}
				}
			} else if (REGEX_SPECIAL_CHARS.indexOf(c) != -1) {
				return null;
			} else {
				if (buf != null) {
					buf.append(c);
				}
			}
		}
		return (buf != null) ? buf.toString() : regex;
	}

	/**
	 * Class {@link BoundedCharSequence} acts as bounded wrapper around an existing {@link CharSequence}.
	 * This can be used to detect access outside the declared boundary, e.g. by using a greedy regex. 
	 */
	public static class BoundedCharSequence implements CharSequence {

		CharSequence str;

		public BoundedCharSequence(CharSequence str) {
			this.str = str;
		}

		@Override
		public int length() {
			return str.length() + 1;
		}

		@Override
		public char charAt(int index) {
			if (index == str.length()) {
				errorInvalidBound();
			}
			return str.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start == str.length() || end == str.length()) {
				errorInvalidBound();
			}
			return str.subSequence(start, end);
		}

		@Override
		public String toString() {
			return str.toString();
		}

		void errorInvalidBound() {
			CheckTools.error("BoundedCharSequence tries to access index {}", str.length());
		}

	}

	/**
	 * Allows to create a complex regex composed of multiple parts.
	 * If matching fails, a detailed message indicating the problem can be produced.
	 */
	public static class ConcatRegex {
		IList<String> parts;
		int flags;

		// State
		String regex;
		Pattern pattern;

		public static ConcatRegex of(String... parts) {
			ConcatRegex builder = new ConcatRegex();
			builder.parts = GapList.create(parts);
			return builder;
		}

		public ConcatRegex setFlags(int flags) {
			this.flags = flags;
			return this;
		}

		public String getRegex() {
			if (regex == null) {
				regex = new StringPrinter().addAll(parts).toString();
			}
			return regex;
		}

		public Pattern getPattern() {
			if (pattern == null) {
				pattern = Pattern.compile(getRegex(), flags);
			}
			return pattern;
		}

		public String getRegex(int size) {
			return new StringPrinter().addAll(parts.subList(0, size)).toString();
		}

		public Pattern getPattern(int size) {
			return Pattern.compile(getRegex(size));
		}

		public int getNumParts() {
			return parts.size();
		}

		/**
		 * Check whether the regex matches the input using the find approach.
		 * If it matches, everything is ok.
		 * If it does not match, the method will fail with a detailed exception explaining which parts of the regex still match.
		 * 
		 * @param input input string
		 */
		public void checkFind(String input) {
			StringPrinter buf = null;
			boolean first = true;
			int index = parts.size() - 1;
			while (index > 0) {
				String regex = getRegex(index);
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(input);
				if (first) {
					if (matcher.find()) {
						return;
					}
					buf = new StringPrinter().setElemMarker(TextTools.NL);
					buf.addParts("Input: ", input);
					first = false;
				}
				if (matcher.find()) {
					buf.addParts("- ", regex, " -> matches");
					break;
				}
				buf.addParts("- ", regex, " -> fails");
				index--;
			}
			CheckTools.error(buf.toString());
		}
	}

	/** Regex which matches everything */
	public static final String REGEX_EVERYTHING = "(?s).*";
	/** Pattern which matches everything */
	public static final Pattern PATTERN_EVERYTHING = Pattern.compile(REGEX_EVERYTHING);

	/** Regex which matches never */
	public static final String REGEX_NEVER = "$.";
	/** Pattern which matches never */
	public static final Pattern PATTERN_NEVER = Pattern.compile(REGEX_NEVER);

	/**
	 * Special characters in regular expressions which need to be escaped if they should be used for a literal match.
	 */
	// Note that when character needs to be escaped also depends on the context: so ']' is fine outside a character class,
	// but needs escaping inside a character class. The question does it need quoting is also different from does it harm when quoting...
	public static final String REGEX_SPECIAL_CHARS = "[\\^$.|?*+(){}";
	public static final char REGEX_ESCAPE_CHAR = '\\';

	/** \R: any Unicode linebreak sequence, is equivalent to +u000D+u000A|[+u000A+u000B+u000C+u000D+u0085+u2028+u2029] */
	public static final String REGEX_EOL = "\\R";

	// Non capturing regex group (?:): match will not be added to a group
	// Positive lookahead (?=): you want to match something followed by something else which is not part of the match
	// Positive lookbehind (?<=): you want to match something preceded by something else
	// Use \n for \U000A and \r for \U000D
	public static final String REGEX_NO_EOL = "[^\n\u000B\u000C\r\u0085\u2028\u2029]*";
	//public static final String REGEX_NO_EOL = ".*";
	public static final String REGEX_EOL_OR_START = "(?:\\R|^)";
	/** Match EOL or end of text, which will be part of the returned match */
	public static final String REGEX_EOL_OR_END = "(?:\\R|$)";
	/** Match EOL or end of text, but it will not be part of the returned match */
	public static final String REGEX_EOL_OR_END_EXCLUDING = "(?:(?=\\R)|$)";

	/** \s: A whitespace character: [ \t\n\x0B\f\r] or \p{IsWhite_Space} (if UNICODE_CHARACTER_CLASS is set) */
	public static final String REGEX_WHITESPACE = "\\s";

	// MULTILINE or (?m): symbols ^ and $ match just after or just before, respectively, a line terminator or the end of the input sequence. 
	// \z: the end of the input
	// \R: linebreak matcher: any Unicode linebreak sequence, is equivalent to +U000D+U000A|[+U000A+U000B_U000C+U000D+U0085+U2028+U2029]
	// \h: a horizontal whitespace character: [ \t\xA0+U1680+U180e+U2000-+U200a+U202f+U205f+U3000]

	/** Match a single empty line */
	public static final Pattern PATTERN_ONE_EMPTY_LINE = Pattern.compile("^\\h*$(\\R?)", Pattern.MULTILINE);
	/** Match one or more consecutive empty lines */
	public static final Pattern PATTERN_ONE_OR_MORE_EMPTY_LINES = Pattern.compile("^(\\h*\\R)*(\\h*\\R|\\h+\\z)", Pattern.MULTILINE);
	/** Match two or more consecutive empty lines */
	public static final Pattern PATTERN_TWO_OR_MORE_EMPTY_LINES = Pattern.compile("^(\\h*\\R)+(\\h*\\R|\\h+\\z)", Pattern.MULTILINE);

	// --- Regex ---

	/**
	 * Creates a regex pattern out of a regex string.
	 * If the input is invalid, an exception is thrown.
	 *
	 * @param regex	regex string
	 * @return		created regex pattern
	 */
	public static Pattern getPattern(String regex) {
		return getPattern(regex, 0);
	}

	/**
	 * Creates a regex pattern out of a regex string.
	 * If the input is invalid, an exception is thrown.
	 *
	 * @param regex	regex string
	 * @param flags	regex flags
	 * @return		created regex pattern (never null)
	 */
	public static Pattern getPattern(String regex, int flags) {
		return Pattern.compile(regex, flags);
	}

	/**
	 * Returns true if the regex matches the input string completely.
	 *
	 * @param regex		regular expression
	 * @param input		string to be matched
	 * @return			true if the input string matches the regex completely,
	 * 					false otherwise
	 */
	public static boolean matches(String regex, String input) {
		// Note: the following statements are equivalent:
		//   Pattern.matches(regex, input) === Pattern.compile(regex).matcher(input).matches()
		Pattern pattern = Pattern.compile(regex);
		return matches(pattern, input);
	}

	/**
	 * Returns true if the compiled regex pattern matches the input string completely.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @return			true if the input string matches the regex completely,
	 * 					false otherwise
	 */
	public static boolean matches(Pattern pattern, String input) {
		return pattern.matcher(input).matches();
	}

	/**
	 * Returns true if the regex pattern is found in the input string.
	 *
	 * @param regex		regular expression
	 * @param input		string to be matched
	 * @return			true if the regex is found in the input string,	false otherwise
	 */
	public static boolean find(String regex, String input) {
		Pattern pattern = Pattern.compile(regex);
		return find(pattern, input);
	}

	/**
	 * Returns true if the compiled regex pattern is found in the input string.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @return			true if the regex is found in the input string,	false otherwise
	 */
	public static boolean find(Pattern pattern, String input) {
		return pattern.matcher(input).find();
	}

	/**
	 * Returns index of start position if the compiled regex pattern is found in the input string.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @return			index of start position where regex is found in the input string, -1 if not found
	 */
	public static int indexOf(Pattern pattern, String input) {
		return indexOf(pattern, input, 0);
	}

	/**
	 * Returns index of start position if the compiled regex pattern is found in the input string.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @param start		start position 
	 * @return			index of start position where regex is found in the input string, -1 if not found
	 */
	public static int indexOf(Pattern pattern, String input, int start) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find(start)) {
			return matcher.start();
		} else {
			return -1;
		}
	}

	/**
	 * Returns index of end position if the compiled regex pattern is found in the input string.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @return			index of end position where regex is found in the input string, -1 if not found
	 */
	public static int endIndexOf(Pattern pattern, String input) {
		return endIndexOf(pattern, input, 0);
	}

	/**
	 * Returns index of end position if the compiled regex pattern is found in the input string.
	 *
	 * @param pattern	compiled regular expression
	 * @param input		string to be matched
	 * @param start		start position 
	 * @return			index of end position where regex is found in the input string, -1 if not found
	 */
	public static int endIndexOf(Pattern pattern, String input, int start) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find(start)) {
			return matcher.end();
		} else {
			return -1;
		}
	}

	/**
	 * Return matched part of string.
	 * If a matching group is defined, result of group 1 is returned, otherwise the whole matched string (i.e. matching group 0).
	 *
	 * @param regex		regex
	 * @param input		input string
	 * @return			matched part of string or null if there is no match
	 */
	public static String get(String regex, CharSequence input) {
		Pattern pattern = Pattern.compile(regex);
		return get(pattern, input);
	}

	/**
	 * Return matched part of string.
	 * 
	 * @param regex		regex
	 * @param input		input string
	 * @param group		index of matching group, -1 for matching group 1 (if defined) or matching group 0 (otherwise)
	 * @return			matched part of string or null if there is no match
	 */
	public static String get(String regex, CharSequence input, int group) {
		Pattern pattern = Pattern.compile(regex);
		return get(pattern, input, group);
	}

	/**
	 * Return matched part of string.
	 * If a matching group is defined, result of matching group 1 is returned,
	 * otherwise the whole matched string (i.e. matching group 0).
	 *
	 * @param pattern	pattern
	 * @param input		input string
	 * @return			matched part of string or null if there is no match
	 */
	public static String get(Pattern pattern, CharSequence input) {
		return get(pattern, input, -1);
	}

	/**
	 * Return matched part of string.
	 * 
	 * @param pattern	pattern
	 * @param input		input string
	 * @param group		index of matching group, -1 for matching group 1 (if only 1 group is defined) or matching group 0 (otherwise)
	 * @return			matched part of string or null if there is no match
	 */
	public static String get(Pattern pattern, CharSequence input, int group) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return getMatch(matcher, group);
		}
		return null;
	}

	/**
	 * Return list with string before match, the matched part of string, string after match.
	 * 
	 * @param pattern	pattern
	 * @param input		input string
	 * @return			matched part of string or null if there is no match
	 */
	public static IList<String> getWith(Pattern pattern, String input) {
		return getWith(pattern, input, -1);
	}

	/**
	 * Return list with string before match, the matched part of string, string after match.
	 * 
	 * @param pattern	pattern
	 * @param input		input string
	 * @param group		index of matching group, -1 for matching group 1 (if only 1 group is defined) or matching group 0 (otherwise)
	 * @return			matched part of string or null if there is no match
	 */
	public static IList<String> getWith(Pattern pattern, String input, int group) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			group = getSelectedGroup(matcher, group);
			String head = input.substring(0, matcher.start(group));
			String match = matcher.group(group);
			String tail = input.substring(matcher.end(group));
			return GapList.create(head, match, tail);
		}
		return null;
	}

	public static String getMatch(Matcher matcher, int group) {
		group = getSelectedGroup(matcher, group);
		return matcher.group(group);
	}

	/**
	 * Determine group to use.
	 * If -1 is used as group, group 1 is used if there is only one group, otherwise group 0.
	 * 
	 * @param matcher	matcher
	 * @param group		requested group, use -1 for automatic selection
	 * @return			group to use
	 */
	public static int getSelectedGroup(MatchResult matcher, int group) {
		if (group == -1) {
			if (matcher.groupCount() == 1) {
				group = 1;
			} else {
				group = 0;
			}
		}
		return group;
	}

	public static String getIf(Pattern pattern, String input, int group) {
		String get = get(pattern, input, group);
		if (get != null) {
			return get;
		}
		return input;
	}

	/**
	 * If the string matches, all matches are returned.
	 * The match is performed using the find() method.
	 * Index 0 contains the whole matched string, index 1 the first matching group, etc.
	 *
	 * @param regex 	regex string
	 * @param input		input string
	 * @return			list with extracted strings or null if there is no match
	 */
	public static IList<String> getAll(String regex, String input) {
		Pattern pattern = Pattern.compile(regex);
		return getAll(pattern, input);
	}

	/**
	 * If the string matches, all matches are returned.
	 * The match is performed using the find() method.
	 * Index 0 contains the whole matched string, index 1 the first matching group, etc.
	 *
	 * @param pattern	regex pattern
	 * @param input		input string
	 * @return			list with extracted strings or null if there is no match
	 */
	public static IList<String> getAll(Pattern pattern, String input) {
		Matcher matcher = pattern.matcher(input);
		if (!matcher.find()) {
			return null;
		} else {
			return getMatchedGroups(matcher);
		}
	}

	// Set

	public static String set(String regex, String input, String value) {
		Pattern pattern = getPattern(regex);
		return set(pattern, input, -1, value);
	}

	public static String set(String regex, String input, int group, String value) {
		Pattern pattern = getPattern(regex);
		return set(pattern, input, group, value);
	}

	/**
	 * Matches the input string against the pattern.
	 * If it matches, the first matching group is replaced with the value.
	 * If the pattern does not define a matching group, the whole matching region (group 0) is used. 
	 *
	 * @param pattern  regex pattern
	 * @param input    input string
	 * @param value    string to use as replacement
	 * @return         string with replacement made or unchanged string
	 */
	public static String set(Pattern pattern, String input, String value) {
		return set(pattern, input, -1, value);
	}

	/**
	 * Matches the input string against the pattern.
	 * If it matches, the specified matching group is replaced with the value.
	 * If -1 is used as group, group 1 is used if it exists, otherwise group 0.
	 *
	 * @param pattern  regex pattern
	 * @param input    input string
	 * @param group    matching group to replace
	 * @param value    string to use as replacement
	 * @return		   string with replacement made or unchanged string
	 */
	public static String set(Pattern pattern, String input, int group, String value) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			input = set(matcher, input, group, value, true);
		}
		return input;
	}

	public static String set(MatchResult matcher, String input, int group, String value, boolean inInput) {
		int start;
		int end;
		if (inInput) {
			start = 0;
			end = input.length();
		} else {
			start = matcher.start(0);
			end = matcher.end(0);
		}
		group = getSelectedGroup(matcher, group);
		input = input.substring(start, getValidGroupStart(matcher, group)) + value + input.substring(getValidGroupEnd(matcher, group), end);
		return input;
	}

	// setAll

	public static String removeAll(String regex, String input) {
		return setAll(regex, input, -1, "");
	}

	public static String removeAll(String regex, String input, int group) {
		return setAll(regex, input, group, "");
	}

	public static String removeAll(Pattern pattern, String input) {
		return setAll(pattern, input, -1, "");
	}

	public static String removeAll(Pattern pattern, String input, int group) {
		return setAll(pattern, input, group, "");
	}

	// setAll

	public static String setAll(String regex, String input, String value) {
		Pattern pattern = getPattern(regex);
		return setAll(pattern, input, -1, value);
	}

	public static String setAll(String regex, String input, int group, String value) {
		Pattern pattern = getPattern(regex);
		return setAll(pattern, input, group, value);
	}

	public static String setAll(Pattern pattern, String input, String value) {
		return setAll(pattern, input, -1, value);
	}

	public static String setAll(Pattern pattern, String input, int group, String value) {
		Matcher matcher = pattern.matcher(input);
		if (!matcher.find()) {
			return input;
		}

		group = getSelectedGroup(matcher, group);
		StringBuilder buf = new StringBuilder();
		int start = 0;
		while (true) {
			int index = getValidGroupStart(matcher, group);
			buf.append(input, start, index);
			buf.append(value);
			start = getValidGroupEnd(matcher, group);
			if (!matcher.find()) {
				break;
			}
		}
		return buf.toString();
	}

	//

	public static int getValidGroupStart(MatchResult matcher, int group) {
		// Even if a pattern matches, some groups may report group(i)==null and start(i)==end(i)==-1, e.g. for an unselected alternative
		int start = matcher.start(group);
		CheckTools.check(start != -1, "No data for group {}", group);
		return start;
	}

	public static int getValidGroupEnd(MatchResult matcher, int group) {
		// Even if a pattern matches, some groups may report group(i)==null and start(i)==end(i)==-1, e.g. for an unselected alternative
		int end = matcher.end(group);
		CheckTools.check(end != -1, "No data for group {}", group);
		return end;
	}

	public static String getValidGroup(MatchResult matcher, int group) {
		// Even if a pattern matches, some groups may report group(i)==null and start(i)==end(i)==-1, e.g. for an unselected alternative
		String str = matcher.group(group);
		CheckTools.check(str != null, "No data for group {}", group);
		return str;
	}

	// Remove

	/**
	 * Remove the full content matching the regex (i.e. matching group 0) from the input.
	 * If there is not match, the input string is returned unchanged.
	 */
	public static String remove(String regex, String input) {
		return set(regex, input, "");
	}

	/**
	 * Remove the specified matching group of the regex from the input.
	 * If there is not match, the input string is returned unchanged.
	 */
	public static String remove(String regex, String input, int group) {
		return set(regex, input, group, "");
	}

	/**
	 * Matches the input string against the pattern.
	 * If it matches, the first matching group is removed.
	 * If the pattern does not define a matching group, the whole matching region (group 0) is used. 
	 *
	 * @param pattern  regex pattern
	 * @param input    input string
	 * @return         string with replacement made or unchanged string
	 */
	public static String remove(Pattern pattern, String input) {
		return set(pattern, input, "");
	}

	/**
	 * Matches the input string against the pattern.
	 * If it matches, the specified matching group is removed.
	 * If -1 is used as group, group 1 is used if it exists, otherwise group 0.
	 *
	 * @param pattern  regex pattern
	 * @param input    input string
	 * @param group    matching group to replace
	 * @return		   string with replacement made or unchanged string
	 */
	public static String remove(Pattern pattern, String input, int group) {
		return set(pattern, input, group, "");
	}

	//

	/** Return all matches group in the {@link MatchResult} and return them as list */
	public static IList<String> getMatchedGroups(MatchResult matcher) {
		IList<String> groups = new GapList<>(matcher.groupCount() + 1);
		for (int i = 0; i <= matcher.groupCount(); i++) {
			groups.add(matcher.group(i));
		}
		return groups;
	}

	/**
	 * Count the number of occurrences of the regex in the input string.
	 *
	 * @param input		input string
	 * @param pattern	regex
	 * @return			number of occurrences (0 for null)
	 */
	public static int count(String input, Pattern pattern) {
		if (input == null) {
			return 0;
		}
		Matcher matcher = pattern.matcher(input);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * Splits the input string by the specified regex.
	 * The number of string parts is returned is always the number of
	 * occurrences of the regex plus 1, so all matches (at the beginning,
	 * in the middle, at the end) are treated uniformally.
	 * The method String.split() in contrary ignores matches at the end.
	 *
	 * @param input		input string
	 * @param pattern	regex
	 * @return			list of strings (only null if input is null)
	 */
	public static IList<String> split(String input, Pattern pattern) {
		return split(input, pattern, false);
	}

	/**
	 * Splits the input string by the specified regex. If the regex does not match, the returned list contains the input string.
	 * The number of string parts is returned is always the number of occurrences of the regex plus 1, so all matches (at the beginning,
	 * in the middle, at the end) are treated uniformally. The method String.split() in contrary ignores matches at the end.
	 *
	 * @param input		input string
	 * @param pattern	regex
	 * @param include	true to include the matched pattern, false otherwise
	 * @return			list of strings (only null if input is null)
	 */
	public static IList<String> split(String input, Pattern pattern, boolean include) {
		if (input == null) {
			return null;
		}
		IList<String> strs = GapList.create();
		Matcher matcher = pattern.matcher(input);
		int pos = 0;
		while (true) {
			boolean found = matcher.find();
			int start = (found ? matcher.start() : input.length());
			String str = input.substring(pos, start);
			if (found && include) {
				str += matcher.group();
			}
			strs.add(str);
			if (!found) {
				break;
			}
			pos = matcher.end();
		}
		return strs;
	}

	public static String getFirstSplit(String input, Pattern pattern) {
		List<String> strs = split(input, pattern);
		if (strs == null) {
			return null;
		}
		return strs.get(0);
	}

	// Ignore Case

	// Flags provided as embedded flags (?i)(?u) are also reported by Pattern.flags() 
	// public static final int CASE_INSENSITIVE = 0x02;
	// public static final int UNICODE_CASE = 0x40;

	public static final int IGNORE_CASE = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

	public static boolean isPatternWithIgnoreCase(Pattern pattern) {
		return (pattern.flags() & Pattern.CASE_INSENSITIVE) != 0;
	}

	public static Pattern getPatternIgnoreCase(Pattern pattern, boolean ignoreCase) {
		int addFlags = (ignoreCase) ? IGNORE_CASE : 0;
		int flags = pattern.flags() | addFlags;
		return getPatternWithFlags(pattern, flags);
	}

	public static Pattern getPatternIgnoreCase(String regex, boolean ignoreCase) {
		int flags = (ignoreCase) ? IGNORE_CASE : 0;
		return Pattern.compile(regex, flags);
	}

	public static Pattern getPatternIgnoreCase(String regex, int flags, boolean ignoreCase) {
		int addFlags = (ignoreCase) ? IGNORE_CASE : 0;
		return Pattern.compile(regex, flags | addFlags);
	}

	public static Pattern getPatternWithFlags(Pattern pattern, int flags) {
		if (pattern.flags() == flags) {
			return pattern;
		} else {
			return Pattern.compile(pattern.pattern(), flags);
		}
	}

}
