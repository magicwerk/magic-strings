package org.magicwerk.strings;

import java.util.function.Supplier;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.EqualsTools.EqualsByString;
import org.magicwerk.brownies.core.EqualsTools.EqualsException;
import org.magicwerk.brownies.core.EqualsTools.EqualsResult;
import org.magicwerk.brownies.core.EqualsTools.IEquals;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.objects.Result;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.StringRepeater;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.chars.CharToolsTest;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.text.TextTools;
import org.magicwerk.strings.text.TextTools.LineIterator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link GapString}.
 */
public class GapStringTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new GapStringTest().run();
	}

	void run() {
		//testGetCodePoint();

		testStringEqualsCompare();
		//testStringIndexOf();
		//testStringLastIndexOf();
		//testStringRepeat();
		//testStringReplace();
		//testStringStartsEndsWith();
		//testStringStripTrim();
		//testStringSubstring();

		//new NormalizeEolsJmhTest().test();
		//new GapStringValueOfIntJmhTest().test();
	}

	/**
	 * Show that GapString.valueOf(int) outperforms the simple approach using Integer.toString()
	 */
	public static class GapStringValueOfIntJmhTest extends StringJmhBenchmark {

		public GapStringValueOfIntJmhTest() {
			//setJavaVersions(JavaVersion.JAVA_25);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> ints = new CyclicSource<>(1, 10, 100, 1000);
		}

		@Benchmark
		public GapString test1(MyState state) {
			int i = state.ints.next();
			return GapString.valueOf(i);
		}

		@Benchmark
		public GapString test2(MyState state) {
			int i = state.ints.next();
			String s = Integer.toString(i);
			return new GapString(s);
		}
	}

	@Trace
	public void testGetCodePoint() {
		{
			GapString str = new GapString("a");
			int cp = str.getCodePoint(0);
			CheckTools.check(CodePointTools.charCount(cp) == 1);
		}
		{
			GapString str = new GapString(CodePointToolsTest.STRING_SURROGATE_00);
			int cp = str.getCodePoint(0);
			CheckTools.check(CodePointTools.charCount(cp) == 2);
		}
	}

	// String

	@Capture
	public void testStringEqualsCompare() {
		String ss0 = "abc";
		String ss1 = "bcd"; // different string
		String ss2 = "abcd"; // different length
		String ss3 = "aBc"; // different case
		IList<String> ss = GapList.create(ss0, ss1, ss2, ss3);
		for (String s0 : ss) {
			for (String s1 : ss) {
				String t0 = s0;
				String t1 = s1;
				GapString gt0 = new GapString(t0);
				GapString gt1 = new GapString(t1);

				checkEqualsBoolean(() -> t0.equals(t1), () -> gt0.equals(gt1));
				checkEqualsBoolean(() -> t1.equals(t0), () -> gt1.equals(gt0));
				checkEqualsBoolean(() -> t0.equalsIgnoreCase(t1), () -> gt0.equalsIgnoreCase(gt1));
				checkEqualsBoolean(() -> t1.equalsIgnoreCase(t0), () -> gt1.equalsIgnoreCase(gt0));

				checkEqualsInt(() -> t0.compareTo(t1), () -> gt0.compareTo(gt1));
				checkEqualsInt(() -> t1.compareTo(t0), () -> gt1.compareTo(gt0));
				checkEqualsInt(() -> t0.compareToIgnoreCase(t1), () -> gt0.compareToIgnoreCase(gt1));
				checkEqualsInt(() -> t1.compareToIgnoreCase(t0), () -> gt1.compareToIgnoreCase(gt0));
			}
		}
	}

	@Capture
	public void testStringStartsEndsWith() {
		String starts = "a";
		String ends = "c";
		String s = "abc";
		GapString gs = new GapString(s);

		checkEqualsBoolean(() -> s.startsWith(starts), () -> gs.startsWith(starts));
		checkEqualsBoolean(() -> s.startsWith(ends), () -> gs.startsWith(ends));

		checkEqualsBoolean(() -> s.endsWith(ends), () -> gs.endsWith(ends));
		checkEqualsBoolean(() -> s.endsWith(starts), () -> gs.endsWith(starts));

		for (int i = -1; i < s.length() + 1; i++) {
			int pos = i;
			checkEqualsBoolean(() -> s.startsWith(starts, pos), () -> gs.startsWith(starts, pos));
			checkEqualsBoolean(() -> s.startsWith(ends, pos), () -> gs.startsWith(ends, pos));
		}
	}

	@Capture
	public void testStringIndexOf() {
		// indexOf(string)
		{
			String find = "b";
			String noFind = "x";
			String s = "abc";
			GapString gs = new GapString(s);

			checkEqualsInt(() -> s.indexOf(find), () -> gs.indexOf(find));
			checkEqualsInt(() -> s.indexOf(noFind), () -> gs.indexOf(noFind));

			for (int i = -1; i < s.length() + 1; i++) {
				int pos = i;
				checkEqualsInt(() -> s.indexOf(find, pos), () -> gs.indexOf(find, pos));
				checkEqualsInt(() -> s.indexOf(noFind, pos), () -> gs.indexOf(noFind, pos));
			}
		}

		// indexOf(codePoint)
		{
			int find = CodePointToolsTest.CODE_POINT_SURROGATE_00;
			int noFind = 'x';
			String s = "a" + CodePointToolsTest.STRING_SURROGATE_00 + "c";
			GapString gs = new GapString(s);

			checkEqualsInt(() -> s.indexOf(find), () -> gs.indexOf(find));
			checkEqualsInt(() -> s.indexOf(noFind), () -> gs.indexOf(noFind));

			for (int i = -1; i < s.length() + 1; i++) {
				int pos = i;
				checkEqualsInt(() -> s.indexOf(find, pos), () -> gs.indexOf(find, pos));
				checkEqualsInt(() -> s.indexOf(noFind, pos), () -> gs.indexOf(noFind, pos));
			}
		}
	}

	@Capture
	public void testStringLastIndexOf() {
		// indexOf(string)
		{
			String find = "b";
			String noFind = "x";
			String s = "abc";
			GapString gs = new GapString(s);

			checkEqualsInt(() -> s.lastIndexOf(find), () -> gs.lastIndexOf(find));
			checkEqualsInt(() -> s.lastIndexOf(noFind), () -> gs.lastIndexOf(noFind));

			for (int i = -1; i < s.length() + 1; i++) {
				int pos = i;
				checkEqualsInt(() -> s.lastIndexOf(find, pos), () -> gs.lastIndexOf(find, pos));
				checkEqualsInt(() -> s.lastIndexOf(noFind, pos), () -> gs.lastIndexOf(noFind, pos));
			}
		}

		// indexOf(codePoint)
		{
			int find = CodePointToolsTest.CODE_POINT_SURROGATE_00;
			int noFind = 'x';
			String s = "a" + CodePointToolsTest.STRING_SURROGATE_00 + "c";
			GapString gs = new GapString(s);

			checkEqualsInt(() -> s.lastIndexOf(find), () -> gs.lastIndexOf(find));
			checkEqualsInt(() -> s.lastIndexOf(noFind), () -> gs.lastIndexOf(noFind));
		}
	}

	@Capture
	public void testStringRepeat() {
		String s = "abc";

		checkEqualsString(() -> s.repeat(2), () -> new GapString(s).repeat(2));
		checkEqualsString(() -> s.repeat(1), () -> new GapString(s).repeat(1));
		checkEqualsString(() -> s.repeat(0), () -> new GapString(s).repeat(0));
		checkEqualsString(() -> s.repeat(-1), () -> new GapString(s).repeat(-1));
	}

	@Capture
	public void testStringStripTrim() {
		String Ctrl_NoWS = "\u0000";
		String WS_Ctrl = "\t";
		String WS_NoCtrl = "\u0085";

		String Ctrl_NoWs_2 = Ctrl_NoWS + Ctrl_NoWS;
		String WS_Ctrl2 = WS_Ctrl + WS_Ctrl;
		String WS_NoCtrl2 = WS_NoCtrl + WS_NoCtrl;

		String s0 = "abc";
		IList<String> ss = GapList.create(Ctrl_NoWs_2 + s0 + Ctrl_NoWs_2,
				WS_Ctrl2 + s0 + WS_Ctrl2, WS_NoCtrl2 + s0 + WS_NoCtrl2);

		for (String s : ss) {
			checkEqualsString(() -> s.strip(), () -> new GapString(s).strip());
			checkEqualsString(() -> s.stripLeading(), () -> new GapString(s).stripLeading());
			checkEqualsString(() -> s.stripTrailing(), () -> new GapString(s).stripTrailing());
			checkEqualsString(() -> s.trim(), () -> new GapString(s).trim());
		}
	}

	@Capture
	public void testStringReplace() {
		// char
		{
			String s = "abc";
			char findChar = 'b';
			char noFindChar = 'x';
			char replaceChar = 'B';

			checkEqualsString(() -> s.replace(findChar, replaceChar), () -> new GapString(s).replace(findChar, replaceChar));
			checkEqualsString(() -> s.replace(noFindChar, replaceChar), () -> new GapString(s).replace(noFindChar, replaceChar));
		}
		// String
		{
			String s = "abcd";
			String findStr = "bc";
			String noFindStr = "x";
			String emptyFindStr = "";
			String replaceStr = "BC";

			checkEqualsString(() -> s.replace(findStr, replaceStr), () -> new GapString(s).replace(findStr, replaceStr));
			checkEqualsString(() -> s.replace(noFindStr, replaceStr), () -> new GapString(s).replace(noFindStr, replaceStr));
			checkEqualsString(() -> s.replace(emptyFindStr, replaceStr), () -> new GapString(s).replace(emptyFindStr, replaceStr));
		}
	}

	@Capture
	public void testStringSubstring() {
		String s = "abc";

		int min = -1;
		int max = s.length() + 2;
		for (int i = min; i < max; i++) {
			int start = i;
			checkEqualsString(() -> s.substring(start), () -> new GapString(s).substring(start));

			for (int j = min; j < max; j++) {
				int end = j;
				checkEqualsString(() -> s.substring(start, end), () -> new GapString(s).substring(start, end));
			}
		}
	}

	void checkEqualsBoolean(Supplier<Boolean> f0, Supplier<Boolean> f1) {
		checkEquals(f0, f1, getEqualsBoolean());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	IEquals<Result<Boolean>> getEqualsBoolean() {
		EqualsResult eq2 = EqualsResult.byError(EqualsException.BY_TYPE);
		return (IEquals) eq2;
	}

	void checkEqualsInt(Supplier<Integer> f0, Supplier<Integer> f1) {
		checkEquals(f0, f1, getEqualsInt());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	IEquals<Result<Integer>> getEqualsInt() {
		EqualsResult eq2 = EqualsResult.byError(EqualsException.BY_TYPE);
		return (IEquals) eq2;
	}

	void checkEqualsString(Supplier<CharSequence> f0, Supplier<CharSequence> f1) {
		checkEquals(f0, f1, getEqualsString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	IEquals<Result<CharSequence>> getEqualsString() {
		EqualsResult eq2 = new EqualsResult(new EqualsByString<>(), EqualsException.BY_TYPE);
		return (IEquals) eq2;
	}

	<R> void checkEquals(Supplier<R> f0, Supplier<R> f1, IEquals<Result<R>> eq) {
		Result<R> r0 = Result.eval(f0);
		Result<R> r1 = Result.eval(f1);
		boolean r = eq.isEqual(r0, r1);
		CheckTools.check(r, "Error: {} / {}", r0, r1);
	}

	/**
	 * Show that normalizing line ends is slightly faster with GapString only if the strings
	 * contain non-byte characters, otherwise String is faster.
	 */
	public static class NormalizeEolsJmhTest extends StringJmhBenchmark {

		public NormalizeEolsJmhTest() {
			setRunVerify(false);
			setJavaVersions(JavaVersion.JAVA_17);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final int numLines = 1000;
			final int lineLen = 100;
			//final char c = '-';
			final char c = CharToolsTest.CHAR_NO_LATIN1;
			final StringRepeater repeater = StringRepeater.build(b -> b.setRepeat(c));
			final String eol = TextTools.NL_WINDOWS;
			String text;
			{
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < numLines; i++) {
					buf.append(repeater.repeat(lineLen)).append(eol);
				}
				text = buf.toString();
			}
		}

		@Benchmark
		public IList<CharSequence> testNormalizeEolsString(MyState state) {
			String str = state.text;
			return normalizeEolsString(str);
		}

		@Benchmark
		public IList<CharSequence> testNormalizeEolsGapStringReplace(MyState state) {
			String str = state.text;
			return normalizeEolsGapStringReplace(str);
		}

		@Benchmark
		public IList<CharSequence> testNormalizeEolsGapStringRemoveAdd(MyState state) {
			String str = state.text;
			return normalizeEolsGapStringRemoveAdd(str);
		}

		static IList<CharSequence> normalizeEolsString(String s) {
			IList<String> lines = GapList.create();
			LineIterator li = new LineIterator(s);
			while (li.hasNext()) {
				String line = li.next();
				line = "<<" + line.substring(2, line.length() - 2) + ">>";
				//int remove = TextTools.getLenEndsWithEol(line);
				//line = line.substring(0, line.length() - remove) + '\n';
				lines.add(line);
			}
			return (IList) lines;
		}

		static IList<CharSequence> normalizeEolsGapStringReplace(String s) {
			IList<GapString> lines = GapList.create();
			LineIterator li = new LineIterator(s);
			while (li.hasNext()) {
				li.nextEntry();
				GapString line = li.getGapString(true);
				//int remove = TextTools.getLenEndsWithEol(line);
				//line.replaceArray(line.length() - remove, remove, '\n');
				line.replace(0, 2, "<<");
				line.replace(line.length() - 2, 2, ">>");
				lines.add(line);
			}
			return (IList) lines;
		}

		static IList<CharSequence> normalizeEolsGapStringRemoveAdd(String s) {
			IList<GapString> lines = GapList.create();
			LineIterator li = new LineIterator(s);
			while (li.hasNext()) {
				li.nextEntry();
				GapString line = li.getGapString(true);
				line.retain(2, line.length() - 4);
				line.addString(0, "<<");
				line.addString(">>");
				//int remove = TextTools.getLenEndsWithEol(line);
				//line.remove(line.length() - remove, remove);
				//line.add('\n');
				lines.add(line);
			}
			return (IList) lines;
		}
	}

}
