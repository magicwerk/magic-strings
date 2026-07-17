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
package org.magicwerk.strings.text;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.StringRepeater;
import org.magicwerk.strings.StringRoots;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.StringTrimmer;
import org.magicwerk.strings.CharSequenceTools.CharSequenceView;
import org.magicwerk.strings.StringTrimmer.TrimMode;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.IteratorTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IStringRange;
import org.magicwerk.strings.matcher.IStringMatcher;

/**
 * Class {@link TextTools} contains tools for working with text, i.e. strings with line structure. 
 */
public class TextTools {

	/**
	 * Enum {@link PosMode} determines how to handle out of bounds situations.
	 */
	public enum PosMode {
		/** If a position is out of bounds, the nearest position is used */
		NEAREST,
		/** If a position is out of bounds, the default value null is returned */
		DEFAULT,
		/** If a position is out of bounds, an error is thrown */
		ERROR
	}

	public static class TextLine {
		String text;
		String eol;

		public TextLine(String line) {
			setLine(line);
		}

		public TextLine(String text, String eol) {
			this.text = text;
			this.eol = eol;
		}

		public String getText() {
			return text;
		}

		public String getEol() {
			return eol;
		}

		public String getLine() {
			return text + eol;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setEol(String eol) {
			this.eol = eol;
		}

		public void setLine(String line) {
			this.eol = TextTools.getEol(line);
			this.text = line.substring(0, line.length() - eol.length());
		}

		@Override
		public String toString() {
			return getLine();
		}
	}

	/**
	 * If we would like to handle a string with line separators as lines, there are two different interpretation: <br>
	 * - a line ending is an end marker, it ends a line <br>
	 * - a line ending is a separator, it ends one line and already starts a new one <br>
	 * Note that e.g. the command line utility interprets line count even differently, it basically only counts
	 * the number of line terminators. This means that "wc -l FILE" reports 0 if the file contains "text" without line terminator.
	 * Examples: <br>
	 * - null: no line <br>
	 * - "": one line with "" <br>
	 * - "\n": one line with "" <br>
	 * - "a": one line with "a" <br>
	 * - "a\n": one line with "a" <br>
	 */
	public static class LineIterator implements Iterator<String>, Iterable<String> {
		// Configuration
		/** String to split */
		CharSequence str;
		IStringMatcher eolMatcher;
		boolean includeEol = true;
		boolean eolStartsLine;
		boolean reverse;

		// State
		/** Length of string */
		int len;
		// Current fields
		int startLine;
		int startEol;
		int endLine;

		// Fields for next line (filled by hasNext, become active with next)
		boolean hasNext;
		int nextStartLine;
		int nextStartEol;
		int nextEndLine;

		/**
		 * Create {@link LineIterator} to split a string into lines.
		 *
		 * @param str string to split in lines (may be null)
		 */
		public LineIterator(CharSequence str) {
			this(str, false);
		}

		/**
		 * Create {@link LineIterator} to split a string into lines.
		 *
		 * @param str string to split in lines (may be null)
		 * @param reverse true to start iteration at the end, false for default
		 */
		public LineIterator(CharSequence str, boolean reverse) {
			this.str = str;
			this.len = CharSequenceTools.length(str);
			this.reverse = reverse;

			if (str != null) {
				len = str.length();
				if (reverse) {
					startLine = len;
				}
			} else {
				hasNext = true;
				len = -1;
			}
		}

		public LineIterator setEolMatcher(IStringMatcher eolMatcher) {
			this.eolMatcher = eolMatcher;
			return this;
		}

		public LineIterator setEolStartsLine(boolean eolStartsLine) {
			this.eolStartsLine = eolStartsLine;
			return this;
		}

		public LineIterator setIncludeEol(boolean includeEol) {
			this.includeEol = includeEol;
			return this;
		}

		public Stream<String> stream() {
			return IteratorTools.asStream(iterator());
		}

		// Iterable

		@Override
		public Iterator<String> iterator() {
			return this;
		}

		// Iterator

		@Override
		public boolean hasNext() {
			if (!hasNext) {
				prepareNext();
			}
			return len != -1;
		}

		/**
		 * Returns next line from string (including EOL)
		 * If there is no next line, an exception is thrown.
		 */
		@Override
		public String next() {
			if (!nextEntry()) {
				throw new NoSuchElementException();
			}
			return getLine(includeEol);
		}

		//

		/**
		 * Returns next line from string.
		 *
		 * @param withEol	true to include EOL, false to exclude
		 * @return    		next line from string or null if there are no more lines
		 */
		public String nextLine(boolean withEol) {
			if (!nextEntry()) {
				return null;
			}
			return getLine(withEol);
		}

		/**
		 * Returns next {@link CharSequence} from string.
		 *
		 * @param withEol	true to include EOL, false to exclude
		 * @return    		next line from string or null if there are no more lines
		 */
		public CharSequence nextCharSequence(boolean withEol) {
			if (!nextEntry()) {
				return null;
			}
			return getCharSequence(withEol);
		}

		public CharSequenceView nextCharSequenceView(boolean withEol) {
			if (!nextEntry()) {
				return null;
			}
			return getCharSequenceView(withEol);
		}

		/**
		 * Advances iterator to next line from string.
		 * 
		 * @return true if there is a next line, false if there are no more lines
		 */
		public boolean nextEntry() {
			if (!hasNext()) {
				return false;
			}
			activateNext();
			return true;
		}

		/**
		 * Goto next line, copy nextStartLine/nextStartEol/nextEndLine to startLine/startEol/endLine
		 */
		void activateNext() {
			assert (hasNext);

			hasNext = false;
			this.startLine = this.nextStartLine;
			this.startEol = this.nextStartEol;
			this.endLine = this.nextEndLine;

			if (reverse) {
				if (startLine == 0) {
					len = -1;
				}
			} else {
				if (endLine == len) {
					if (!eolStartsLine || startEol == endLine) {
						len = -1;
					}
				}
			}
		}

		/**
		 * Fetch next line, set nextStartLine/nextStartEol/nextEndLine
		 */
		void prepareNext() {
			hasNext = true;
			if (reverse) {
				if (startLine == 0) {
					return;
				}
			} else {
				if (startLine == len) {
					return;
				}
			}
			if (reverse) {
				nextEndLine = startLine;
				if (endLine == 0 && eolStartsLine && isEol(str.charAt(nextEndLine - 1))) {
					nextStartLine = nextEndLine;
					nextStartEol = nextEndLine;
				} else {
					nextStartEol = prevStartEol(str, nextEndLine);
					nextStartLine = prevStartLine(str, nextStartEol);
				}
			} else {
				nextStartLine = endLine;
				if (eolMatcher != null) {
					nextStartEol = eolMatcher.indexOf(str, nextStartLine);
				} else {
					nextStartEol = nextEolStart(str, nextStartLine);
				}
				if (nextStartEol == -1) {
					nextEndLine = len;
					nextStartEol = len;
				} else {
					nextEndLine = nextEolEnd(str, nextStartEol);
					assert (nextEndLine != -1);
				}
			}
		}

		// Getter for current fields

		/**
		 * Returns true if the EOL returned by {@link #getEol} is not empty.
		 */
		public boolean hasEol() {
			return startEol != endLine;
		}

		/**
		 * Returns end of line of current line (may be empty, never null).
		 */
		public String getEol() {
			if (startEol == endLine) {
				return "";
			} else {
				return str.subSequence(startEol, endLine).toString();
			}
		}

		/**
		 * Returns current line with/without line ending (may be empty, never null)
		 * 
		 * @param withEol true to include EOL characters, false to exclude them
		 */
		public String getLine(boolean withEol) {
			return getCharSequence(withEol).toString();
		}

		/**
		 * Returns current line with/without line ending (may be empty, never null)
		 * 
		 * @param withEol true to include EOL characters, false to exclude them
		 */
		public CharSequence getCharSequence(boolean withEol) {
			if (withEol) {
				return str.subSequence(startLine, endLine);
			} else {
				return str.subSequence(startLine, startEol);
			}
		}

		public CharSequenceView getCharSequenceView(boolean withEol) {
			if (withEol) {
				return new CharSequenceView(str, startLine, endLine);
			} else {
				return new CharSequenceView(str, startLine, startEol);
			}
		}

		public GapString getGapString(boolean withEol) {
			if (withEol) {
				return new GapString(str, startLine, endLine - startLine);
			} else {
				return new GapString(str, startLine, startEol - startLine);
			}
		}

	}

	public static final String NL = "\n";

	public static final String NL_WINDOWS = "\r\n";

	public static final String SPACE = " ";

	public static final String NBSP = "\u00a0";

	// This definition of line terminators used here is specified in the section line terminators of the Pattern javadoc:
	// - http://download-llnw.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#lt
	// The method BufferedReader.readLine() however treats only
	// - "\n", "\r\n", "\r"
	// as line terminators.
	// Note that this definition differs from the Unicode 4.0 standard
	// - http://www.unicode.org/versions/Unicode4.0.0/ch05.pdf, 5.8 Newline Guidelines
	// which uses CR, LF, CRLF, NEL, VT, FF, LS, PS
	//
	// CR carriage return 000D
	// LF line feed 000A
	// NEL next line 0085
	// VT vertical tab 000B
	// FF form feed 000C
	// LS line separator 2028
	// PS paragraph separator 2029

	/**
	 * @return a string with all characters treated as end of line
	 */
	public static String getEolChars() {
		return "\n\r\u0085\u2028\u2029";
	}

	static final IList<String> EOL_STRING = GapList.create("\n", "\r\n", "\r", "\u0085", "\u2028", "\u2029").unmodifiableList();

	public static IList<String> getEolStrings() {
		return EOL_STRING;
	}

	/**
	 * Checks whether character is part of a valid end of line string (e.g. '\n' and '\r', but not ' ' or '\t').
	 *
	 * @param c	character to check
	 * @return	true if character is part of a valid end of line string
	 */
	public static boolean isEol(char c) {
		return c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028' || c == '\u2029';
	}

	/**
	 * Checks whether string is a valid end of line string (e.g. '\n' and '\r', but not ' ' or '\t').
	 *
	 * @param str	string to check
	 * @return		true if string is a valid end of line string
	 */
	public static boolean isEol(String str) {
		return ObjectTools.isOneOf(str, "\r\n", "\n", "\r", "\u0085", "\u2028", "\u2029");
	}

	/** 
	 * Check whether string is blank, i.e. is empty (as determined by {@link StringTools#isEmpty} or contains only blank characters
	 * as determined by {@link #isBlank}.
	 */
	public static boolean isBlank(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!isBlank(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Add EOL marker "\n" to string if there is none.
	 */
	public static String addEol(String str) {
		return addEol(str, NL);
	}

	/**
	 * Add EOL marker to string if there is none.
	 */
	public static String addEol(String str, String eol) {
		int eolLen = getLenEndsWithEol(str);
		if (eolLen > 0) {
			return str;
		}
		return str + eol;
	}

	/**
	 * Remove EOL marker from string if there is one.
	 * The method only handles the last EOL, if there are more the previous ones are ignored.
	 */
	public static String removeEol(String str) {
		int remove = getLenEndsWithEol(str);
		if (remove == 0) {
			return str;
		}
		return str.substring(0, str.length() - remove);
	}

	/**
	 * Replace end of line marker in string with standard EOL.
	 * Return string unchanged if there is none.
	 */
	public static String setEol(String str, String eol) {
		// TODO performance
		if (StringTools.isEmpty(TextTools.getEol(str))) {
			return str;
		} else {
			return removeEol(str) + eol;
		}
	}

	public static int nextEolStart(CharSequence str, int index) {
		int pos = index;
		int len = str.length();
		while (pos < len && !isEol(str.charAt(pos))) {
			pos++;
		}
		return (pos < len) ? pos : -1;
	}

	public static int nextEolEnd(CharSequence str, int index) {
		int pos = nextEolStart(str, index);

		int len = str.length();
		if (pos == -1) {
			pos = len;
		}

		int eolLen;
		if (pos == len) {
			eolLen = 0;
		} else if (pos < len - 1 && str.charAt(pos) == '\r' && str.charAt(pos + 1) == '\n') {
			eolLen = 2;
		} else {
			eolLen = 1;
		}

		return pos + eolLen;
	}

	public static int prevStartLine(CharSequence str, int index) {
		int pos = index - 1;
		while (pos >= 0 && !isEol(str.charAt(pos))) {
			pos--;
		}
		return (pos >= 0) ? pos + 1 : 0;
	}

	public static int prevStartEol(CharSequence str, int index) {
		int pos = index - 1;
		if (!isEol(str.charAt(pos))) {
			return index;
		}
		if (pos > 0 && str.charAt(pos - 1) == '\r' && str.charAt(pos) == '\n') {
			return pos - 1;
		} else {
			return pos;
		}
	}

	/**
	 * Returns end of line marker used for specified string.
	 * Only markers at the end of the text are recognized.
	 *
	 * @param text text to inspect
	 * @return     end of line marker used for string, defaultEol if none, null if input is null
	 */
	public static String getEol(CharSequence text, String defaultEol) {
		if (text == null) {
			return null;
		}
		String eol = getEol(text);
		return (eol.isEmpty()) ? defaultEol : eol;
	}

	/**
	 * Returns end of line marker used for specified string.
	 * Only markers at the end of the text are recognized.
	 *
	 * @param text text to inspect
	 * @return     end of line marker used for string, "" if none, null if input is null
	 */
	public static String getEol(CharSequence text) {
		// TODO performance
		if (text == null) {
			return null;
		}
		for (String eol : getEolStrings()) {
			if (CharSequenceTools.endsWith(text, eol)) {
				return eol;
			}
		}
		return "";
	}

	public static int getEolStart(CharSequence text, int index) {
		char c = text.charAt(index);
		if (!isEol(c)) {
			return -1;
		}
		if (c == '\n') {
			if (index > 0 && text.charAt(index - 1) == '\r') {
				return index - 1;
			}
		}
		return index;
	}

	public static int getEolEnd(CharSequence text, int index) {
		char c = text.charAt(index);
		if (!isEol(c)) {
			return -1;
		}
		if (c == '\r') {
			if (index < text.length() - 1 && text.charAt(index + 1) == '\n') {
				return index + 2;
			}
		}
		return index + 1;
	}

	/**
	 * Returns true if string ends with a line marker.
	 * Only markers at the end of the text are recognized.
	 *
	 * @param text text to inspect
	 * @return     end of line marker used for string, "" if none, null if input is null
	 */
	public static boolean hasEol(CharSequence text) {
		// TODO performance
		return !getEol(text).isEmpty();
	}

	/**
	 * Checks whether string ends with an end of line string and returns its length.
	 * The method only handles the last EOL, if there are more the previous ones are ignored.
	 *
	 * @param str	string to check
	 * @return		length of end of line string at end of string
	 * 				(0 if string does not end with an end of line string)
	 */
	public static int getLenEndsWithEol(CharSequence str) {
		if (str == null) {
			return 0;
		}
		// TODO performance
		if (CharSequenceTools.endsWith(str, "\r\n")) {
			return 2;
		}
		if (CharSequenceTools.endsWith(str, "\n") || CharSequenceTools.endsWith(str, "\r") || CharSequenceTools.endsWith(str, "\u0085") ||
				CharSequenceTools.endsWith(str, "\u2028") || CharSequenceTools.endsWith(str, "\u2029")) {
			return 1;
		}
		return 0;
	}

	//

	public static String getString(String str, TextPos startPos, TextPos endPos, PosMode mode) {
		Integer startIndex = getStringPos(str, startPos, mode);
		Integer endIndex = getStringPos(str, endPos, mode);
		if (startIndex == null || endIndex == null) {
			return null;
		}
		return str.substring(startIndex, endIndex);
	}

	public static int getStringPos(String str, TextPos pos) {
		return getStringPos(str, pos, PosMode.ERROR);
	}

	/**
	 * Converts line and column position into character offset.
	 * See {@link #getTextPos} for reverse operation.
	 *
	 * @param str		string
	 * @param pos		position
	 * @return			character offset
	 */
	public static Integer getStringPos(String str, TextPos pos, PosMode mode) {
		CheckTools.checkNonNull(str, "str");
		CheckTools.checkNonNull(pos, "pos");
		CheckTools.checkNonNull(mode, "mode");

		int posRow = pos.getRow();
		if (posRow < 0) {
			if (handleOutOfBounds(pos, mode)) {
				return null;
			}
			return 0;
		}

		int row = 0;
		int index = 0;
		for (String line : new TextTools.LineIterator(str)) {
			int lineLen = line.length();
			if (row == posRow) {
				if (pos.getCol() < lineLen) {
					return index + pos.getCol();
				} else if (mode == PosMode.NEAREST) {
					return index + lineLen - 1;
				}
			}
			index += lineLen;
			row++;
		}

		assert (index == str.length());
		if (handleOutOfBounds(pos, mode)) {
			return null;
		}
		return index;
	}

	public static TextRange getTextRange(String str, IStringRange stringRange) {
		return new TextRange(getTextPos(str, stringRange.getStart()), getTextPos(str, stringRange.getEnd()));
	}

	public static TextRange getTextRange(String str, int start, int end) {
		return new TextRange(getTextPos(str, start), getTextPos(str, end));
	}

	public static TextPos getTextPos(String str, int pos) {
		return getTextPos(str, pos, PosMode.ERROR);
	}

	/**
	 * Converts character offset into line and column position.
	 * See {@link #getStringPos} for reverse operation.
	 *
	 * @param str	string
	 * @param pos	character offset
	 * @return 		text position
	 */
	public static TextPos getTextPos(String str, int pos, PosMode mode) {
		CheckTools.checkNonNull(str, "str");
		CheckTools.checkNonNull(mode, "mode");

		int len = str.length();
		if (pos >= len) {
			if (handleOutOfBounds(pos, mode)) {
				return null;
			}
		}

		int row = 0;
		int col = 0;
		for (String line : new TextTools.LineIterator(str)) {
			int c = pos - col;
			if (c < line.length()) {
				col = c;
				break;
			}
			row++;
			col += line.length();
		}
		if (col < 0) {
			if (handleOutOfBounds(pos, mode)) {
				return null;
			}
			col = 0;
		}
		return new TextPos(row, col);
	}

	static boolean handleOutOfBounds(Object pos, PosMode mode) {
		if (mode == PosMode.ERROR) {
			throw CheckTools.error("Position out of bounds: {}", pos);
		} else if (mode == PosMode.DEFAULT) {
			return true;
		} else {
			return false;
		}
	}

	static final StringRoots commonStart = StringRoots.build(b -> b);

	/** Returns the common indentation string used by all lines in the text */
	public static String getCommonHead(String text, CharPredicate cp) {
		String indent = null;
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			String line = iter.nextLine(false);
			if (indent == null) {
				indent = StringTools.getHead(line, cp);
			} else {
				indent = commonStart.getCommonRoot(indent, line);
				if (indent.isEmpty()) {
					break;
				}
			}
		}
		return indent;
	}

	/** Determine common indent and remove it from the beginning of each line */
	public static String removeCommonHead(String text, CharPredicate cp) {
		String indent = getCommonHead(text, cp);
		return removeHead(text, indent);
	}

	/** Remove head from the beginning of each line (if it is present) */
	public static String removeHead(String text, String indent) {
		if (StringTools.length(indent) == 0) {
			return text;
		}

		StringBuilder buf = new StringBuilder();
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			String line = iter.nextLine(true);
			line = StringTools.removeHeadIf(line, indent);
			buf.append(line);
		}
		return buf.toString();
	}

	public static String updateTextLines(String text, Consumer<List<TextLine>> updater) {
		IList<TextLine> lines = splitLines(text, true).map(TextLine::new);
		updater.accept(lines);
		return joinLines(lines);
	}

	public static String joinLines(Collection<TextLine> lines) {
		// TODO for join
		// what if line is null, line contains eol in the middle, no eol, replace eol, special for last line
		return new StringPrinter().addAllIf(lines).toString();
	}

	/** 
	 * Remove all lines of the text where the predicate evaluates to true.
	 * The line will contain the EOL marker if passed to the operator.
	 */
	public static String removeLines(String text, Predicate<String> remove) {
		return removeLines(text, remove, true);
	}

	/** 
	 * Remove all lines of the text where the predicate evaluates to true.
	 * If withEol is true, the line will contain the EOL marker if passed to the predicate, with false the marker is removed first.
	 */
	public static String removeLines(String text, Predicate<String> remove, boolean withEol) {
		StringBuilder buf = new StringBuilder();
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			String line = iter.nextLine(withEol);
			if (!remove.test(line)) {
				buf.append(line);
			}
		}
		return buf.toString();
	}

	public static String removeLine(String text, int index) {
		StringBuilder buf = new StringBuilder();
		int num = 0;
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			String str = iter.nextLine(true);
			if (num != index) {
				buf.append(str);
			}
			num++;
		}
		CheckTools.check(index >= 0 && index < num);
		return buf.toString();
	}

	public static String addLine(String text, int index, String line) {
		String eol = getEol(line);
		if (eol.isEmpty()) {
			eol = getFirstEol(text);
			if (eol.isEmpty()) {
				eol = TextTools.NL;
			}
			line += eol;
		}

		StringBuilder buf = new StringBuilder();
		int num = 0;
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			if (num == index) {
				buf.append(line);
			}
			String str = iter.nextLine(true);
			buf.append(str);
			num++;
		}
		CheckTools.check(index >= 0 && index <= num);
		return buf.toString();
	}

	/** 
	 * Change each line of the text by applying the specified operator.
	 * If the operator returns null, the line is removed.
	 * The line will contain the EOL marker if passed to the operator.
	 */
	public static String changeLines(String text, UnaryOperator<String> change) {
		return changeLines(text, change, true);
	}

	/** 
	 * Change each line of the text by applying the specified operator.
	 * If the operator returns null, the line is removed.
	 * If withEol is true, the line will contain the EOL marker if passed to the operator,
	 * with false the marker is removed first, then the operator is called, and then the marker is added again.
	 */
	public static String changeLines(String text, UnaryOperator<String> change, boolean withEol) {
		StringBuilder buf = new StringBuilder();
		for (LineIterator iter = new LineIterator(text); iter.hasNext();) {
			String line = iter.nextLine(true);
			String eol = null;
			if (!withEol) {
				eol = TextTools.getEol(line);
				line = TextTools.removeEol(line);
			}
			line = change.apply(line);
			if (line != null) {
				buf.append(line);
				if (eol != null) {
					buf.append(eol);
				}
			}
		}
		return buf.toString();
	}

	public static String removeEmptyLines(String text) {
		return removeLines(text, s -> isBlank(s));
	}

	public static String removeEmptyLinesAtStartEnd(String text) {
		text = removeEmptyLinesAtStart(text);
		text = removeEmptyLinesAtEnd(text);
		return text;
	}

	public static String removeEmptyLinesAtStart(String text) {
		if (text == null) {
			return null;
		}

		int i;
		for (i = 0; i < text.length(); i++) {
			if (!isEol(text.charAt(i))) {
				break;
			}
		}

		if (i > 0) {
			return text.substring(i);
		} else {
			return text;
		}
	}

	public static String removeEmptyLinesAtEnd(String text) {
		if (text == null) {
			return null;
		}

		int i;
		int len = text.length();
		for (i = len - 1; i >= 0; i--) {
			if (!isEol(text.charAt(i))) {
				break;
			}
		}

		if (i == len - 1) {
			return text;
		}

		i = nextEolEnd(text, i);
		return text.substring(0, i);
	}

	/**
	 * Insert given indent before every line.
	 * If the input string is null, null is returned.
	 *
	 * @param str string to split into lines
	 * @param indent indent to insert before every line
	 * @return string with indented lines
	 */
	public static String indentLines(String str, String indent) {
		return indentLines(str, indent, true);
	}

	public static String indentLines(String str, String indent, boolean indentFirst) {
		if (str == null) {
			return null;
		}
		if (indent == null || indent.length() == 0) {
			return str;
		}

		// Indent all lines
		boolean first = true;
		String indent0 = (indentFirst) ? indent : "";
		StringBuilder buf = new StringBuilder();
		for (String line : new LineIterator(str)) {
			if (first) {
				buf.append(indent0);
			} else {
				buf.append(indent);
			}
			buf.append(line);
		}
		return buf.toString();
	}

	static final StringTrimmer trimBlank = StringTrimmer.build(b -> b.setTrimMode(TrimMode.HEAD_TAIL).setFindCharPredicate(TextTools::isBlank));

	public static String trimBlank(String str) {
		return trimBlank.trim(str);
	}

	/** Return true if char is either {@link #isWhitespace} or {@link #isEol} */
	public static boolean isBlank(char c) {
		return isWhitespace(c) || isEol(c);
	}

	/** Return true if char is either a space ' ' or a tabulator '\t' */
	public static boolean isWhitespace(char c) {
		return c == ' ' || c == '\t';
	}

	public static boolean isTab(char c) {
		return c == '\t';
	}

	/** Return true if char is a space ' ' */
	public static boolean isSpace(char c) {
		return c == ' ';
	}

	public static String getTabChars() {
		return "\t";
	}

	/** Remove trailing whitespaces before EOL which is left intact */
	public static String removeTrailingWhitespaces(String str) {
		int len = getLenEndsWithEol(str);
		int end = str.length() - len - 1;
		int start = end;
		while (start >= 0 && isWhitespace(str.charAt(start))) {
			start--;
		}

		if (start == end) {
			return str;
		}

		if (len == 0) {
			return str.substring(0, start + 1);
		} else {
			return str.substring(0, start + 1) + str.substring(end + 1);
		}
	}

	/**
	 * Returns number of lines which would be created if splitLines() would be called.
	 *
	 * @param str string with lines
	 * @return number of lines
	 */
	@SuppressWarnings("unused") // line
	public static int getNumLines(String str) {
		int count = 0;
		for (String line : new LineIterator(str)) {
			count++;
		}
		return count;
	}

	public static int getNumLines(String str, Predicate<String> filter) {
		int count = 0;
		for (String line : new LineIterator(str)) {
			if (filter.test(line)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Determines whether the string has more lines than specified, i.e. getNumLines() is greater than lines.
	 */
	@SuppressWarnings("unused") // line
	public static boolean hasMoreLines(String str, int lines) {
		int num = 1;
		for (String line : new LineIterator(str)) {
			if (num > lines) {
				return true;
			}
			num++;
		}
		return false;
	}

	/**
	 * Returns specified line of string.
	 *
	 * @param str			source string
	 * @param line			line number (starting from 0)
	 * @param includeEOL	true to include EOL characters at end of line
	 * @return				specified line
	 * @throws				IllegalArgumentException if line index is invalid
	 */
	public static String getLine(String str, int line, boolean includeEOL) {
		String s = null;
		if (line >= 0) {
			LineIterator iter = new LineIterator(str);
			for (int i = 0; i <= line; i++) {
				s = iter.nextLine(includeEOL);
			}
		}
		CheckTools.check(s != null, "Invalid line index: {}", line);
		return s;
	}

	static final StringRepeater repeater = StringRepeater.build(" ");

	/**
	 * Returns a string with all characters - except the end of line characters - replaced by spaces. 
	 * The returned string has therefore the same line structure and length as the input string.
	 *
	 * @param str	input string
	 * @return		string with space characters
	 */
	public static String getLineStructure(String str) {
		if (str == null) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		for (LineIterator iter = new LineIterator(str); iter.hasNext();) {
			String text = iter.nextLine(false);
			String eol = iter.getEol();
			buf.append(repeater.repeat(text.length()));
			buf.append(eol);
		}
		String out = buf.toString();
		assert (out.length() == str.length());
		return out;
	}

	static final CharPredicate SPACE_TRIM_PREDICATE = c -> c <= 0x20;

	public static String trimSpaces(String str) {
		return trimSpaces(str, SPACE_TRIM_PREDICATE);
	}

	public static String trimSpaces(String str, CharPredicate predicate) {
		if (str == null) {
			return null;
		}

		int len = str.length();
		int start;
		for (start = 0; start < len && predicate.test(str.charAt(start)); start++) {
		}
		if (start == len) {
			return "";
		}
		int end;
		for (end = len; predicate.test(str.charAt(end - 1)); end--) {
		}

		if (start == 0 && end == len) {
			return str;
		} else {
			return str.substring(start, end);
		}
	}

	public static String normalizeSpaces(String str) {
		return normalizeSpaces(str, SPACE_TRIM_PREDICATE, ' ');
	}

	public static String normalizeSpaces(String str, CharPredicate predicate, char c) {
		if (str == null) {
			return null;
		}

		// Trim
		int len = str.length();
		int start;
		for (start = 0; start < len && predicate.test(str.charAt(start)); start++) {
		}

		int end;
		if (start == len) {
			end = len;
		} else {
			for (end = len; predicate.test(str.charAt(end - 1)); end--) {
			}
		}

		// Normalize
		StringBuilder buf = null;
		int i = start;
		while (true) {
			// Skip non-spaces
			int s = i;
			for (; i < end && !predicate.test(str.charAt(i)); i++) {
			}

			// Add non spaces
			if (i == end) {
				if (buf != null) {
					buf.append(str.substring(s, i));
				}
				break;
			}

			if (buf == null) {
				buf = new StringBuilder();
			}
			buf.append(str.substring(s, i));

			// Add single space
			buf.append(c);

			// Skip spaces
			for (; i < end && predicate.test(str.charAt(i)); i++) {
			}
		}

		if (buf != null) {
			return buf.toString();
		} else {
			if (start == 0 && end == len) {
				return str;
			} else {
				return str.substring(start, end);
			}
		}
	}

	/**
	 * Replace all line endings in the given string with "\n".
	 *
	 * @param str string with line endings to change
	 * @return string with changed line endings, null if input string is null
	 */
	public static String normalizeEol(String str) {
		return normalizeEol(str, TextTools.NL);
	}

	/**
	 * Replace all line endings in the given string with the given line ending.
	 *
	 * @param str string with line endings to change
	 * @param eol line ending to use, null to not change the line endings
	 * @return string with changed line endings, null if input string is null
	 */
	public static String normalizeEol(String str, String eol) {
		if (str == null || eol == null) {
			return str;
		}

		StringBuilder buf = new StringBuilder();
		for (LineIterator iter = new LineIterator(str); iter.hasNext();) {
			buf.append(iter.nextLine(false));
			if (iter.hasEol()) {
				buf.append(eol);
			}
		}
		return buf.toString();
	}

	/**
	 * @return string containing space and tab (" \t")
	 */
	public static String getWhitespaceChars() {
		return " \t";
	}

	/**
	 * Return the common EOL string used in specified string.
	 *
	 * @param str	string to analyze
	 * @return		common EOL string used (or null if there is no common EOL)
	 */
	public static String getCommonEol(String str) {
		if (str == null) {
			return null;
		}

		String eol = null;
		for (LineIterator iter = new LineIterator(str); iter.hasNext();) {
			iter.next();
			String newEol = iter.getEol();
			if (eol == null) {
				eol = newEol;
			} else if (newEol.isEmpty()) {
				break;
			} else if (!newEol.equals(eol)) {
				return null;
			}
		}
		return eol;
	}

	/**
	 * Return the EOL string used in the first line of string
	 *
	 * @param str	string to analyze
	 * @return		EOL of first list in string (may be empty if none), null if input is null
	 */
	public static String getFirstEol(String str) {
		if (str == null) {
			return null;
		}

		LineIterator iter = new LineIterator(str);
		iter.next();
		return iter.getEol();
	}

	/**
	 * Return all EOL strings used in specified string.
	 *
	 * @param str	string to analyze
	 * @return		list with all EOL strings
	 */
	public static Set<String> getDistinctEol(String str) {
		if (str == null) {
			return null;
		}
		Set<String> eols = new HashSet<String>();
		for (LineIterator iter = new LineIterator(str); iter.hasNext();) {
			iter.next();
			eols.add(iter.getEol());
		}
		return eols;
	}

	public static IList<String> splitLines(String str, IStringMatcher eolMatcher, boolean includeEol) {
		IList<String> lines = GapList.create();
		for (LineIterator iter = new LineIterator(str).setEolMatcher(eolMatcher); iter.hasNext();) {
			lines.add(iter.nextLine(includeEol));
		}
		return lines;
	}

	/**
	 * Split string into lines.
	 * The line ending characters are removed.
	 * If the input string is null, null is returned.
	 * This method is equivalent to str.split("\r\n|\n|\r|\u0085|\u2028|\u2029"), but about 4 times faster.
	 * The end of line characters are removed from the strings.
	 *
	 * @param str 	source string
	 * @return 		lines as string array, null if input string is null
	 */
	public static IList<String> splitLines(String str) {
		return splitLines(str, false);
	}

	/**
	 * Split string into lines.
	 * If the input string is null, null is returned.
	 * This method is equivalent to str.split("\r\n|\n|\r|\u0085|\u2028|\u2029"), but about 4 times faster.
	 *
	 * @param str			source string
	 * @param includeEol	true to include EOL characters at end of each line
	 * @return				lines as string array, null if input string is null
	 */
	public static IList<String> splitLines(String str, boolean includeEol) {
		return splitLines(str, includeEol, false);
	}

	public static IList<String> splitLines(String str, boolean includeEol, boolean eolStartsLine) {
		// The call "str.split(regex, n)" is equal to "Pattern.compile(regex).split(str, n)".
		// It discards trailing empty strings, i.e. we cannot distinguish between the strings "alpha\nbeta" and "alpha\nbeta\n" 
		// (this distinction is however hard to express in the returned String[] array). 
		// It matches all line terminators as described in the API of the Pattern class. 
		// Note that the "\r\n" subpattern must come before the "\n" and "\r" subpattern.
		// The implementation using split is about 4 times slower than the one presented below...
		// return str.split("\r\n|\n|\r|\u0085|\u2028|\u2029");
		// This definition of line terminators is given in
		// https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lt
		// Note that this definition differs from the Unicode 4.0 standard
		// Unicode Newline Guidelines https://www.unicode.org/standard/reports/tr13/tr13-5.html
		// which uses CR, LF, CRLF, NEL (Next line, U+0085), VT (Line tabulation, U+000B), FF (Form feed, U+000C) , LS (Line separator, U+2028), PS (Paragraph separator, U+2029)
		if (str == null) {
			return null;
		}

		IList<String> lines = GapList.create();
		for (LineIterator iter = new LineIterator(str).setEolStartsLine(eolStartsLine); iter.hasNext();) {
			lines.add(iter.nextLine(includeEol));
		}
		return lines;
	}

	/**
	 * Joins the given lines of string into a single string where the lines are joined with '\n'.
	 */
	public static String joinLines(List<String> lines) {
		return joinLines(lines, TextTools.NL);
	}

	/**
	 * Joins the given lines of string into a single string where the lines are joined by the given line ending.
	 *
	 * @param lines Array of lines
	 * @param lineEnding Line ending to use
	 * @return Joined string
	 */
	public static String joinLines(List<String> lines, String lineEnding) {
		return joinLines(lines, lineEnding, false);
	}

	public static String joinLines(List<String> lines, String lineEnding, boolean onlyIfNeeded) {
		StringBuilder buf = new StringBuilder();
		boolean hasEol = true;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (!hasEol) {
				buf.append(lineEnding);
			}
			if (onlyIfNeeded) {
				hasEol = hasEol(line);
			} else {
				hasEol = false;
			}
			buf.append(line);
		}
		return buf.toString();
	}

	public static <T extends CharSequence> int getTextLength(Collection<T> lines) {
		int len = 0;
		for (T line : lines) {
			len += line.length();
		}
		return len;
	}

	//

	/**
	 * Build a marker message like
	 * <pre>
	 * {@code MyTitle (pos: 99 -> row: 1 / col: 2)}
	 * abcXefg
	 *    ^
	 * </pre>
	 */
	public static String getMarkerMessage(String title, String text, int pos) {
		TextPos tp = TextTools.getTextPos(text, pos, PosMode.ERROR);
		String posStr = StringFormatter.format("(pos: {} -> row: {} / col: {})", pos, tp.getRow(), tp.getCol());
		return getMarkerMessage(title, text, posStr, tp);
	}

	/**
	 * Build a marker message like
	 * <pre>
	 * MyTitle (row: 1 / col: 2)
	 * abcXefg
	 *    ^
	 * </pre>
	 */
	public static String getMarkerMessage(String title, String text, TextPos pos) {
		String posStr = StringFormatter.format("(row: {} / col: {})", pos, pos.getRow(), pos.getCol());
		return getMarkerMessage(title, text, posStr, pos);
	}

	static String getMarkerMessage(String title, String text, String posStr, TextPos pos) {
		String head = title + " " + posStr + ":" + TextTools.NL;
		String line = TextTools.getLine(text, pos.getRow(), true);
		line = TextTools.addEol(line);
		String marker = StringTools.repeat(" ", pos.getCol()) + "^";
		String msg = head + line + marker;
		return msg;
	}

}
