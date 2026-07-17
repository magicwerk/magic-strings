package org.magicwerk.strings.chars;

import java.util.List;
import java.util.stream.Collectors;

import org.magictest.client.Capture;
import org.magictest.client.Format;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.strings.escape.StringEscapeTools;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.chars.CharTools;
import org.magicwerk.strings.chars.CharsetTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link CharTools}.
 */
public class CharToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CharToolsTest().run();
	}

	void run() {
		//testSurrogate();
		//testWhiteSpaces();
		//testNewLines();
		//testArrows();

		//CodePointIteratorTestJmh.test();
	}

	/**
	 * Show that {@link String#codePoints} is slow due to the use of streams and a simple 
	 * iterator implementation is far more efficient.
	 */
	public static class CodePointIteratorTestJmh {

		// Java 21
		//		CharToolsTest.CodePointIteratorTestJmh.testCodePointIterator                     thrpt    5  18219305.661 ± 2725150.909   ops/s
		//		CharToolsTest.CodePointIteratorTestJmh.testCodePointIterator:gc.alloc.rate.norm  thrpt    5       104.000 ±       0.001    B/op
		//		CharToolsTest.CodePointIteratorTestJmh.testStringCodePoints                      thrpt    5  12815821.683 ±  486827.788   ops/s
		//		CharToolsTest.CodePointIteratorTestJmh.testStringCodePoints:gc.alloc.rate.norm   thrpt    5       312.001 ±       0.001    B/op

		static void test() {
			Options opts = new Options().includeClass(CodePointIteratorTestJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String str = "abc-def";
			String search = "+-";

			CyclicSource<String> add = new CyclicSource<>("01234", "56789");
		}

		@Benchmark
		public List<Integer> testStringCodePoints(MyState state) {
			return state.add.next().codePoints().boxed().collect(Collectors.toList());
		}

		@Benchmark
		public IList<Integer> testCodePointIterator(MyState state) {
			return CollectionTools2.collect(CodePointTools.codePointIterator(state.add.next()));
		}
	}

	@Trace
	public void testFromUnicodeChar() {
		CharTools.fromUnicodeChar("\\u1234");
	}

	@Trace(formats = { @Format(apply = Trace.PARAM0, formatter = "formatCharHex") })
	public static void testToUnicodeChar() {
		CharTools.toUnicodeChar((char) 0x000f);
		CharTools.toUnicodeChar((char) 0x00ff);
		CharTools.toUnicodeChar((char) 0x0fff);
		CharTools.toUnicodeChar((char) 0xffff);
	}

	static String formatCharHex(char c) {
		return StringEscapeTools.toHexString(c);
	}

	@Capture
	public static void testSurrogate() {
		char[] cs = Character.toChars(CodePointToolsTest.CODE_POINT_SURROGATE_0);

		CheckTools.check(Character.isHighSurrogate(cs[0]));
		CheckTools.check(Character.highSurrogate(CodePointToolsTest.CODE_POINT_SURROGATE_0) == cs[0]);

		CheckTools.check(Character.isLowSurrogate(cs[1]));
		CheckTools.check(Character.lowSurrogate(CodePointToolsTest.CODE_POINT_SURROGATE_0) == cs[1]);

		String str = CodePointToolsTest.STRING_WITH_SURROGATE;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int cp0 = str.codePointAt(i);
			int cp1 = (i > 0) ? str.codePointBefore(i) : -1;
			String type = "--";
			if (Character.isHighSurrogate(c)) {
				type = "HI";
			} else if (Character.isLowSurrogate(c)) {
				type = "LO";
			}
			StringBuilder buf = new StringBuilder();
			buf.append(type).append("/").append(Integer.toHexString(cp0)).append("/").append(Integer.toHexString(cp1))
					.append("/").append(CodePointTools.getValidStartIndexForCodePoint(str, i, false));
			LOG.info("{}", buf);
		}
	}

	static void testPunctuation() {
		// Pattern.matches("\\p{Punct}", str)
		// The first pattern matches the following 32 characters: !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~

		// Pattern.matches("\\p{IsPunctuation}", str)
		// The second pattern matches a whopping 632 unicode characters, including, for example: «, », ¿, ¡, §, ¶, ‘, ’, “, ”, and ‽.
		// Interestingly, not all of the 32 characters matched by the first pattern are matched by the second. The second pattern does not match the following 9 characters: $, +, <, =, >, ^, `, |, and ~ (which the first pattern does match).

		//If you want to match for any character from either character set, you could do:
		// Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]", str)
	}

	static void testWhiteSpaces() {
		// String.trim() treats everything as whitespace which is <=0x02
		for (char c = 0; c <= 0x20; c++) {
			boolean ws = isWhiteSpace(c);
			boolean sc = isSpaceChar(c);
			boolean cc = isControl(c);
			LOG.info("Character {}: whiteSpace={}, spaceChar={}, control={}", StringEscapeTools.toHexString(c), ws, sc, cc);
		}
	}

	public static final char CHAR_LEN_1 = 'a';
	public static final char CHAR_LEN_2 = 'ä'; // U+00E4
	public static final char CHAR_LEN_3 = '\u20AC'; // Euro
	public static final byte[] INVALID_CHAR = new byte[] { -128 };

	public static final char CHAR_LATIN1 = 'a';
	public static final char CHAR_NO_LATIN1 = '\u2192';

	// New line for both Unicode and Regex
	static final char LF = '\n'; // \u000a
	static final char CR = '\r'; // \u000d
	static final char NEL = '\u0085';
	static final char LS = '\u2028';
	static final char PS = '\u2029';
	// New line only for Unicode
	static final char VT = '\t'; // \0000b
	static final char FF = '\f'; // \u000f

	static final char LF_ARROW = '\u2192'; // arrow-down-left
	static final char CR_ARROW = '\u2193'; // arrow-down
	static final char VT_ARROW = '\u21B2'; // arrow-right

	static void testArrows() {
		CharGapList chars = CharGapList.create(LF_ARROW, CR_ARROW, VT_ARROW);
		for (int i = 0; i < chars.size(); i++) {
			char c = chars.get(i);
			LOG.info("Character {}", c);
		}
	}

	static void testNewLines() {
		// This definition of line terminators is given in
		// http://download-llnw.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
		// "\r\n|\n|\r|\u0085|\u2028|\u2029"
		// Note that this definition differs from the Unicode 4.0 standard
		// http://www.unicode.org/versions/Unicode4.0.0/ch05.pdf, 5.8 Newline Guidelines
		// which uses CR, LF, CRLF, NEL, VT, FF, LS, PS

		CharGapList chars = CharGapList.create(LF, CR, NEL, LS, PS, VT, FF);
		for (int i = 0; i < chars.size(); i++) {
			char c = chars.get(i);
			boolean ws = isWhiteSpace(c);
			boolean sc = isSpaceChar(c);
			boolean cc = isControl(c);
			LOG.info("Character {}: whiteSpace={}, spaceChar={}, control={}", StringEscapeTools.toHexString(c), ws, sc, cc);
		}
	}

	static boolean isWhiteSpace(char c) {
		boolean b1 = Character.isWhitespace(c);
		return b1;
	}

	static boolean isSpaceChar(char c) {
		boolean b1 = Character.isSpaceChar(c);
		return b1;
	}

	static boolean isControl(char c) {
		boolean b1 = Character.isISOControl(c);
		boolean b2 = Character.getType(c) == Character.CONTROL;
		assert (b1 == b2);
		return b1;
	}

	@Trace
	public static void testHasSurrogate() {
		CharTools.hasSurrogate("(x)");
		CharTools.hasSurrogate(CodePointToolsTest.STRING_WITH_SURROGATE);
	}

	@Trace
	public static void testReplaceSurrogate() {
		CharTools.replaceSurrogate("(x)");
		CharTools.replaceSurrogate(CodePointToolsTest.STRING_WITH_SURROGATE);
	}

	@Trace
	public static void testGetUtf8Len() {
		CharTools.getUtf8Len(CHAR_LEN_1);
		CharTools.getUtf8Len(CHAR_LEN_2);
		CharTools.getUtf8Len(CHAR_LEN_3);
		CharTools.getUtf8Len((char) 0xffff);
	}

	@Trace
	public static void testGetUtf8Char() {
		CharTools.getUtf8Char(CharsetTools.getBytes(CHAR_LEN_1), 0);
		CharTools.getUtf8Char(CharsetTools.getBytes(CHAR_LEN_2), 0);
		CharTools.getUtf8Char(CharsetTools.getBytes(CHAR_LEN_3), 0);
		CharTools.getUtf8Char(INVALID_CHAR, 0);
	}

	@Trace
	public static void testGetUtf8Start() {
		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_1), 0);
		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_2), 0);
		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_3), 0);
		CharTools.getUtf8Start(INVALID_CHAR, 0);

		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_1 + "a"), 1);
		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_2 + "a"), 1);
		CharTools.getUtf8Start(CharsetTools.getBytes(CHAR_LEN_3 + "a"), 1);
	}

	@Trace
	public static void testGetUtf8StartReverse() {
		CharTools.getUtf8StartReverse(CharsetTools.getBytes(CHAR_LEN_1), 0);
		CharTools.getUtf8StartReverse(CharsetTools.getBytes(CHAR_LEN_2), 1);
		CharTools.getUtf8StartReverse(CharsetTools.getBytes(CHAR_LEN_3), 2);
		CharTools.getUtf8StartReverse(INVALID_CHAR, 0);
	}

	@Trace
	public static void testIsUtf8() {
		CharTools.isUtf8(CharsetTools.getBytes("abc"));
		CharTools.isUtf8(CharsetTools.getBytes("a\u00e4c"));
		CharTools.isUtf8(new byte[] { 'a', (byte) 0xff, 'c' });
	}

	@Trace
	public static void testIsAscii() {
		CharTools.isAscii(CharsetTools.getBytes("abc"));
		CharTools.isAscii(CharsetTools.getBytes("a\u00e4c"));
		CharTools.isAscii(new byte[] { 'a', (byte) 0xff, 'c' });
	}

}
