package org.magicwerk.strings.matcher;

import static org.magicwerk.strings.matcher.LinebreakMatcher.getLinebreakLengthAtEndJava;
import static org.magicwerk.strings.matcher.LinebreakMatcher.getLinebreakLengthAtEndUnicode;
import static org.magicwerk.strings.matcher.LinebreakMatcher.getLinebreakLengthJava;
import static org.magicwerk.strings.matcher.LinebreakMatcher.getLinebreakLengthUnicode;
import static org.magicwerk.strings.matcher.LinebreakMatcher.isLinebreakJava;
import static org.magicwerk.strings.matcher.LinebreakMatcher.isLinebreakUnicode;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link LinebreakMatcher} searches for line breaks.
 * The two implementations {@link LinebreakJavaMatcher} and {@link LinebreakUnicodeMatcher} implement the different
 * handling of line breaks.
 */
public interface LinebreakMatcher extends IStringMatcher, IStringStartsAtMatcher, IStringEndsAtMatcher {

	public static final LinebreakMatcher LINEBREAK_JAVA_MATCHER = new LinebreakJavaMatcher();
	public static final LinebreakMatcher LINEBREAK_UNICODE_MATCHER = new LinebreakUnicodeMatcher();

	// String.lines() / BufferedReader.readLine(): "\n", "\r", "\r\n"
	public static IList<String> LINEBREAKS_JAVA = GapList.immutable("\n", "\r", "\r\n");
	// "\R" Any Unicode linebreak sequence, is equivalent to U+000DU+000A|[U+000AU+000BU+000CU+000DU+0085U+2028U+2029]
	public static IList<String> LINEBREAKS_UNICODE = GapList.immutable("\n", "\r", "\r\n", "\u000B", "\u000C", "\u0085", "\u2028", "\u2029");

	public static boolean isLinebreakJava(char c) {
		return c == '\n' || c == '\r';
	}

	public static boolean isLinebreakUnicode(char c) {
		return c == '\n' || c == '\r' || c == '\u000B' || c == '\u000C' || c == '\u0085' || c == '\u2028' || c == '\u2029';
	}

	/** Return length of Java line break starting at specified position (1 or 2), 0 if not a line break */
	public static int getLinebreakLengthJava(CharSequence str, int start) {
		char c = str.charAt(start);
		if (c == '\r') {
			if (start < str.length() - 1 && str.charAt(start + 1) == '\n') {
				return 2;
			} else {
				return 1;
			}
		} else {
			return (isLinebreakJava(c)) ? 1 : 0;
		}
	}

	/** Return length of Unicode line break starting at specified position (1 or 2), 0 if not a line break */
	public static int getLinebreakLengthUnicode(CharSequence str, int start) {
		char c = str.charAt(start);
		if (c == '\r') {
			if (start < str.length() - 1 && str.charAt(start + 1) == '\n') {
				return 2;
			} else {
				return 1;
			}
		} else {
			return (isLinebreakUnicode(c)) ? 1 : 0;
		}
	}

	/** Return length of Java line break ending after specified position (1 or 2), 0 if not a line break */
	public static int getLinebreakLengthAtEndJava(CharSequence str, int end) {
		char c = str.charAt(end - 1);
		if (c == '\n') {
			if (end > 0 && str.charAt(end - 2) == '\r') {
				return 2;
			} else {
				return 1;
			}
		} else {
			return (isLinebreakJava(c)) ? 1 : 0;
		}
	}

	/** Return length of Unicode line break ending after specified position (1 or 2), 0 if not a line break */
	public static int getLinebreakLengthAtEndUnicode(CharSequence str, int end) {
		char c = str.charAt(end - 1);
		if (c == '\n') {
			if (end > 0 && str.charAt(end - 2) == '\r') {
				return 2;
			} else {
				return 1;
			}
		} else {
			return (isLinebreakUnicode(c)) ? 1 : 0;
		}
	}

	/**
	 * Class {@link LinebreakMatcherImpl} is the abstract base class for implementations of {@link LinebreakMatcher}.
	 */
	public abstract static class LinebreakMatcherImpl implements LinebreakMatcher {

		public abstract boolean isLinebreak(char c);

		public abstract int getLinebreakLength(CharSequence str, int start);

		public abstract int getLinebreakLengthAtEnd(CharSequence str, int start);

		// IStringMatcher

		@Override
		public int indexOf(CharSequence str, int start) {
			int end = str.length();
			for (int i = start; i < end; i++) {
				int len = getLinebreakLength(str, i);
				if (len > 0) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int indexOfEnd(CharSequence str, int start) {
			int end = str.length();
			for (int i = start; i < end; i++) {
				int len = getLinebreakLength(str, i);
				if (len > 0) {
					return i + len;
				}
			}
			return -1;
		}

		@Override
		public IMatch find(CharSequence str, int start) {
			int end = str.length();
			for (int i = start; i < end; i++) {
				int len = getLinebreakLength(str, i);
				if (len > 0) {
					return new Match(str, i, i + len);
				}
			}
			return null;
		}

		// IStringStartsAtMatcher

		@Override
		public boolean startsAt(CharSequence str, int start) {
			int len = getLinebreakLength(str, start);
			return len > 0;
		}

		@Override
		public int indexOfEndStartingAt(CharSequence str, int start) {
			int len = getLinebreakLength(str, start);
			return (len > 0) ? start + len : -1;
		}

		@Override
		public IMatch matchStartingAt(CharSequence str, int start) {
			int len = getLinebreakLength(str, start);
			return (len > 0) ? new Match(str, start, start + len) : null;
		}

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			int len = getLinebreakLengthAtEnd(str, end);
			return len > 0;
		}

		@Override
		public int indexOfEndingAt(CharSequence str, int end) {
			int len = getLinebreakLengthAtEnd(str, end);
			return (len > 0) ? end - len : -1;
		}

		@Override
		public IMatch matchEndingAt(CharSequence str, int end) {
			int len = getLinebreakLengthAtEnd(str, end);
			return (len > 0) ? new Match(str, end - len, end) : null;
		}
	}

	/**
	 * Class {@link LinebreakJavaMatcher} implements {@link LinebreakMatcher} for
	 * Java line breaks, i.e. "\n", "\r", "\r\n".
	 */
	public static class LinebreakJavaMatcher extends LinebreakMatcherImpl {

		@Override
		public boolean isLinebreak(char c) {
			return isLinebreakJava(c);
		}

		@Override
		public int getLinebreakLength(CharSequence str, int start) {
			return getLinebreakLengthJava(str, start);
		}

		@Override
		public int getLinebreakLengthAtEnd(CharSequence str, int start) {
			return getLinebreakLengthAtEndJava(str, start);
		}
	}

	/**
	 * Class {@link LinebreakJavaMatcher} implements {@link LinebreakMatcher} for
	 * Unicode line breaks, i.e. "\n", "\r", "\r\n", "\u000B", "\u000C", "\u0085", "\u2028", "\u2029".
	 */
	public static class LinebreakUnicodeMatcher extends LinebreakMatcherImpl {

		@Override
		public boolean isLinebreak(char c) {
			return isLinebreakUnicode(c);
		}

		@Override
		public int getLinebreakLength(CharSequence str, int start) {
			return getLinebreakLengthUnicode(str, start);
		}

		@Override
		public int getLinebreakLengthAtEnd(CharSequence str, int start) {
			return getLinebreakLengthAtEndUnicode(str, start);
		}
	}

}
