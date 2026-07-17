package org.magicwerk.strings.text;

import org.magictest.client.Capture;
import org.magictest.client.Format;
import org.magictest.client.Format.OutputType;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.strings.escape.StringEscapeTools;
import org.magicwerk.brownies.core.strings.text2.Text;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.text.TextPos;
import org.magicwerk.strings.text.TextRange;
import org.magicwerk.strings.text.TextTools;
import org.magicwerk.strings.text.TextTools.LineIterator;
import org.magicwerk.strings.text.TextTools.PosMode;
import org.slf4j.Logger;

/**
 * Test of {@link TextTools}.
 */
public class TextToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final String[] texts = new String[] {
			null,
			"",
			"\n",
			"\n\n",
			"Alpha",
			"Alpha\n",
			"Alpha\nBeta",
			"Alpha\nBeta\n",
			"Alpha\r\nBeta",
			"Alpha\rBeta"
	};

	static String str = "line1\n" + // 6 chars
			"\r\n" + // 2 chars
			"line3\r\n"; // 7 chars

	public static void main(String[] args) {
		new TextToolsTest().run();
	}

	void run() {
		//testGetCommonIndent();
		//testGetSingleIndent();
		//testGetStringPos();
		//testGetTextPos();
		//testLineIterator();
		//testNormalizeEol();
		//testRemoveCommonIndent();
		//testRemoveTrailingWhitespaces();
		testTrimSpaces();
	}

	@Trace
	public void testSplitLines() {
		for (String text : texts) {
			TextTools.splitLines(text);
		}
	}

	@Trace
	public void testTrimSpaces() {
		IList<String> strs = GapList.create(null, "", "abc", " abc", "abc ", " abc ", "  abc", "abc  ", "  abc  ");
		for (String str : strs) {
			TextTools.trimSpaces(str);
		}
	}

	@Trace
	public void testNormalizeSpaces() {
		IList<String> strs = GapList.create(null, "", "abc", " abc", "abc ", " abc ", "  abc", "abc  ", "  abc  ",
				"a b", "a  b", "a b c", "a  b  c",
				" a b ", "  a  b  ", " a b c ", "  a  b  c  ");
		for (String str : strs) {
			TextTools.normalizeSpaces(str);
		}
	}

	@Trace
	public void testNormalizeEol() {
		for (String s : EOL_LINES) {
			TextTools.normalizeEol(s, "\r");
		}

		Report.setAutoTrace(false);
		for (String s : EOL_LINES) {
			String s2 = TextTools.normalizeEol(s, null);
			CheckTools.check(ObjectTools.equals(s2, s));
		}
	}

	@Trace
	public void testGetFirstEol() {
		for (String s : EOL_LINES) {
			TextTools.getFirstEol(s);
		}
	}

	@Trace
	public void testGetCommonEol() {
		for (String s : EOL_LINES) {
			TextTools.getCommonEol(s);
		}
	}

	@Trace
	public void testGetDistinctEol() {
		for (String s : EOL_LINES) {
			TextTools.getDistinctEol(s);
		}
	}

	@Trace
	public static void testTrimBlank() {
		TextTools.trimBlank(null);
		TextTools.trimBlank("");
		TextTools.trimBlank(" ");
		TextTools.trimBlank(" x");
		TextTools.trimBlank("x");
		TextTools.trimBlank(" x ");
		TextTools.trimBlank("  abc  ");
	}

	//

	static final IList<String> EOL_LINES = GapList.create(
			null, "", "a", "\n", "\r\n", "a\n", "a\nb", "a\r\nb", "a\nb\n", "\n\n", "\r\n\r\r");

	@Capture
	public void testLineIterator() {
		EOL_LINES.forEach(s -> TextToolsTest.doTestLineIterator(s));
	}

	static void doTestLineIterator(String str) {
		doTestLineIterator(str, false);
		doTestLineIterator(str, true);
	}

	static void doTestLineIterator(String str, boolean eolStartsLine) {
		LineIterator iter1 = new LineIterator(str).setEolStartsLine(eolStartsLine);
		IList<String> strs1 = GapList.create(str);
		iter1.forEach(s -> strs1.add(s));

		LineIterator iter1R = new LineIterator(str, true).setEolStartsLine(eolStartsLine);
		IList<String> strs1R = GapList.create();
		iter1R.forEach(s -> strs1R.addFirst(s));
		strs1R.addFirst(str);
		CheckTools.check(strs1R.equals(strs1));

		StringPrinter buf1 = new StringPrinter().setElemMarker(", ");
		buf1.addAll(strs1.stream().map(s -> StringEscapeTools.toJavaString(s)));
		LOG.info("{}", buf1);

		LineIterator iter2 = new LineIterator(str).setEolStartsLine(eolStartsLine);
		IList<String> strs2 = GapList.create(str);
		while (true) {
			String s = iter2.nextLine(true);
			if (s == null) {
				break;
			}
			strs2.add(s);
		}

		LineIterator iter2R = new LineIterator(str, true).setEolStartsLine(eolStartsLine);
		IList<String> strs2R = GapList.create();
		while (true) {
			String s = iter2R.nextLine(true);
			if (s == null) {
				break;
			}
			strs2R.addFirst(s);
		}
		strs2R.addFirst(str);
		CheckTools.check(strs2R.equals(strs2));

		if (!strs1.equals(strs2)) {
			StringPrinter buf2 = new StringPrinter().setElemMarker(", ");
			buf2.addAll(strs2.stream().map(s -> StringEscapeTools.toJavaString(s)));
			LOG.info("Diff: {}", buf2);
		}
		CheckTools.check(strs1.equals(strs2));
	}

	//

	@Trace
	public void testGetLineStructure() {
		TextTools.getLineStructure("abc\ndef");
	}

	@Trace
	public void testGetLine() {
		TextTools.getLine(str, 2, true);
		TextTools.getLine(str, 2, false);

		TextTools.getLine(str, -1, false);
		TextTools.getLine(str, 5, false);
	}

	@Trace
	public void testGetNumLines() {
		for (String text : texts) {
			TextTools.getNumLines(text);
		}
	}

	@Trace
	public void testHasMoreLines() {
		TextTools.hasMoreLines("a\nb", 1);
		TextTools.hasMoreLines("a\nb", 2);
		TextTools.hasMoreLines("a\nb", 3);
	}

	@Trace
	public void testIndentLines() {
		TextTools.indentLines(null, "-");
		TextTools.indentLines("Alpha", "-");
		TextTools.indentLines("Alpha\nBeta", "-");
		TextTools.indentLines("Alpha\n\nBeta", "-");
		TextTools.indentLines("Alpha\nBeta\n", "-");
		TextTools.indentLines("Alpha\r\nBeta", "-");
		TextTools.indentLines("Alpha\rBeta", "-");
	}

	@Trace
	public void testGetTextPos() {
		PosMode mode = PosMode.NEAREST;
		TextTools.getTextPos(str, 0, mode);
		TextTools.getTextPos(str, 2, mode);
		TextTools.getTextPos(str, 5, mode);
		TextTools.getTextPos(str, 6, mode);
		TextTools.getTextPos(str, 7, mode);
		TextTools.getTextPos(str, 9, mode);
		TextTools.getTextPos(str, 20, mode);
		TextTools.getTextPos(str, -1, mode);

		mode = PosMode.DEFAULT;
		TextTools.getTextPos(str, 20, mode);
		TextTools.getTextPos(str, -1, mode);
	}

	@Trace
	public void testGetStringPos() {
		PosMode mode = PosMode.NEAREST;
		TextTools.getStringPos(str, new TextPos(0, 0), mode);
		TextTools.getStringPos(str, new TextPos(0, 2), mode);
		TextTools.getStringPos(str, new TextPos(2, 1), mode);
		TextTools.getStringPos(str, new TextPos(4, 0), mode);

		// position out of bounds returns best possible result
		TextTools.getStringPos(str, new TextPos(-1, -1), mode);
		TextTools.getStringPos(str, new TextPos(0, 10), mode);
		TextTools.getStringPos(str, new TextPos(10, 0), mode);

		mode = PosMode.DEFAULT;
		TextTools.getStringPos(str, new TextPos(-1, -1), mode);
		TextTools.getStringPos(str, new TextPos(0, 10), mode);
		TextTools.getStringPos(str, new TextPos(10, 0), mode);
	}

	@Trace(formats = { @Format(apply = Trace.RESULT, outputType = OutputType.PRE) })
	public void testGetMarkerMessage() {
		TextTools.getMarkerMessage("Invalid character", "abXcd", 2);
	}

	@Trace
	public void testGetString() {
		PosMode mode = PosMode.NEAREST;
		TextTools.getString(str, new TextPos(0, 1), new TextPos(0, 3), mode);
		TextTools.getString(str, new TextPos(0, 1), new TextPos(0, 1), mode);
		TextTools.getString(str, new TextPos(0, 1), new TextPos(0, 0), mode);
	}

	@Capture
	public void testGetText() {
		doTestText("abc");
		doTestText("abc\n");
		doTestText("abc\ndef");
		doTestText("abc\ndef\n");
	}

	static void doTestText(String s) {
		Text t = Text.of(s);
		TextRange tr = new TextRange(t.getStartPos(), t.getEndPos());
		Text t2 = t.getText(tr);
		String s2 = t2.getString();
		CheckTools.check(s2.equals(s));
	}

	@Trace
	public void testRemoveLines() {
		TextTools.removeLines("1a\n2b\n3ac\n4d", s -> s.contains("a"));
	}

	@Trace
	public void testRemoveTrailingWhitespaces() {
		TextTools.removeTrailingWhitespaces("abc");
		TextTools.removeTrailingWhitespaces("abc ");
		TextTools.removeTrailingWhitespaces("abc  ");
		TextTools.removeTrailingWhitespaces("abc\n");
		TextTools.removeTrailingWhitespaces("abc \n");
		TextTools.removeTrailingWhitespaces("abc  \n");
	}

	@Trace
	public void testChangeLines() {
		TextTools.changeLines("1a\n2b\n3ac\n4d", s -> s.startsWith("2b") ? null : "[" + s + "]", true);
		TextTools.changeLines("1a\n2b\n3ac\n4d", s -> s.startsWith("2b") ? null : "[" + s + "]", false);
	}

	@Trace
	public void testRemoveEmptyLinesAtStart() {
		TextTools.removeEmptyLinesAtStart("\r\n\nabc");
		TextTools.removeEmptyLinesAtStart("abc");
		TextTools.removeEmptyLinesAtStart(null);
	}

	@Trace
	public void testRemoveEmptyLinesAtEnd() {
		TextTools.removeEmptyLinesAtEnd("abc\n\r\n\n");
		TextTools.removeEmptyLinesAtEnd("abc\n");
		TextTools.removeEmptyLinesAtEnd("abc");
		TextTools.removeEmptyLinesAtEnd(null);
	}
}
