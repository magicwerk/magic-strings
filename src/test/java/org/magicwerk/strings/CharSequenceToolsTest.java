package org.magicwerk.strings;

import java.util.Comparator;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.InheritTrace;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.CompareExecutor;
import org.magicwerk.brownies.core.conditional.ConditionalEvaluator.Mode;
import org.magicwerk.brownies.core.conditional.ConditionalPredicateSupplier;
import org.magicwerk.brownies.core.diff.ListDiff;
import org.magicwerk.brownies.core.diff.ObjectDiff;
import org.magicwerk.brownies.core.regex.RegexBuilder;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.javassist.BytecodeTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.javassist.analyzer.ClassDef;
import org.magicwerk.brownies.javassist.analyzer.JavaAnalyzer;
import org.magicwerk.brownies.javassist.analyzer.MethodDef;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesTestEnv.JavaGradleEclipseProject;
import org.magicwerk.brownies.tools.dev.jvm.JacocoRunner;
import org.magicwerk.brownies.tools.dev.jvm.JacocoRunner.InstructionStats;
import org.magicwerk.brownies.tools.dev.jvm.JacocoRunner.MethodInstructionStats;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationFreeRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.Key2List;
import org.magicwerk.strings.CharSequenceInlineTools;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.CharSequenceTools.AppendCharReplacer;
import org.magicwerk.strings.CharSequenceTools.CharFunctionTEST;
import org.magicwerk.strings.CharSequenceTools.CharSequenceView;
import org.magicwerk.strings.CharSequenceTools.IndexOfString;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.chars.CharOperator;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CharTools;
import org.magicwerk.strings.chars.CodePointPredicates;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharIndexEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.AnyCharMatcher;
import org.magicwerk.strings.matcher.AnyCodePointMatcher;
import org.magicwerk.strings.matcher.CharIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CharMatcher;
import org.magicwerk.strings.matcher.CharPredicateMatcher;
import org.magicwerk.strings.matcher.CodePointIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CodePointMatcher;
import org.magicwerk.strings.matcher.CodePointPredicateMatcher;
import org.magicwerk.strings.matcher.IStringEndsAtMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringReverseMatcher;
import org.magicwerk.strings.matcher.IStringStartsAtMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.magicwerk.strings.matcher.AnyCharMatcher.AnyCharIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.AnyCodePointMatcher.AnyCodePointIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CharPredicateMatcher.CharPredicateIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CodePointPredicateMatcher.CodePointPredicateIgnoreCaseMatcher;
import org.magicwerk.strings.objects.Tuple;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;
import org.testng.annotations.Test;

/**
 * Test of class {@link CharSequenceTools}.
 */
public class CharSequenceToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CharSequenceToolsTest().run();
	}

	void run() {
		//testManual();

		//testDoReplaceInlineCodePointWithChar();
		//testDoReplaceInlineCharWithCodePoint();
		testDoReplaceInlineString();
		//testCharSequenceView();
		//testChecked();
		//testCodePointCount();
		//testCodePointAt();
		//testContains();
		//testEndsWith();
		//testIndexOf();
		//testIndexOfReverse();
		//testLastIndexOf();
		//testMatchNot();
		//testMatchNotAllocationFree();
		//testReplaceCodePoint();
		//testRetainCharPredicate();

		//testVerifyIStringMatcher();
		//testVerifyStringsMatcher();
		//testVerifyEquals();
		//testVerifyIndexOf();
		//testVerify();
		//testVerifyCoverage();
		//testVerifyBytecodeSize();

		//new ReplaceAnyCharTestJmh().test();
		//new IndexOfChar2TestJmh().test();
		//new ReplaceStringTestJmh().test();
		//new ReplaceInlineCharWithCodePointTestJmh().test();
		//new RemoveCodePointPredicateTestJmh().test();
		//new RemoveCharTestJmh().test();
		//new IndexOfCodePointTestJmh().test();
		//new ReplaceCharIgnoreCaseTestJmh().test();
		//new IndexOfIgnoreCaseTestJmh().test();
		//new IndexOfAnyCharTestJmh().test();
		//new IndexOfCharTestJmh().test();
	}

	@Test(groups = { "slow" })
	@Capture(source = Source.NONE)
	public void testMatchNotAllocationFree() {
		Options opts = new Options().includeClass(CheckMatchNotAllocationFree.class);
		new JmhAllocationFreeRunner().checkAllocationFree(opts);
	}

	@State(Scope.Benchmark)
	public static class CheckMatchNotAllocationFree {
		GapList<String> nots = GapList.create("0abc", "abc1", "0abc1");

		@Benchmark
		public void test() {
			CharSequenceTools.matchNot(nots, "abc", 0, 3);
			CharSequenceTools.matchNot(nots, "0abc", 1, 3);
			CharSequenceTools.matchNot(nots, "abc1", 0, 3);
			CharSequenceTools.matchNot(nots, "0abc1", 1, 3);
		}
	}

	@Trace
	public void testMatchNot() {
		GapList<String> nots = GapList.create("0abc", "abc1", "0abc1");
		CharSequenceTools.matchNot(nots, "abc", 0, 3);
		CharSequenceTools.matchNot(nots, "0abc", 1, 3);
		CharSequenceTools.matchNot(nots, "abc1", 0, 3);
		CharSequenceTools.matchNot(nots, "0abc1", 1, 3);
	}

	void testManual() {
		IString s = new GapString("abXcd");
		LOG.info("{}", s);
		s.rotate(-2);
		LOG.info("{}", s);
		s.remove(0);
		s.add('0');
		s.add('1');
		LOG.info("{}", s);
		s.rotate(-1);
		LOG.info("{}", s);
		s.rotate(-1);
		LOG.info("{}", s);
	}

	void testVerifyBytecodeSize() {
		JavaAnalyzer ja = new JavaAnalyzer().setUseCurrentClassPath();
		ClassDef cd = ja.analyzeClass(CharSequenceTools.class);
		Key2List<MethodDef, String, String> mds = cd.getMethodDefs();
		IList<Tuple<MethodDef, Integer>> mds2 = GapList.create();
		mds.forEach(md -> mds2.add(Tuple.of(md, BytecodeTools.getBytecodeSize(md.getCtBehavior()))));
		mds2.sort(Comparator.comparing(Tuple<MethodDef, Integer>::getLast).reversed());
		LOG.info("{}", StringPrinter.formatLines(mds2));
	}

	@Trace
	public void testDoReplaceInlineCharWithCodePoint() {
		CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(new GapString("abXcd"), 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00);
		CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(new GapString("X"), 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00);
		CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(new GapString("XX"), 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00);
		CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(new GapString("aXbXc"), 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00);
		CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(new GapString("XbX"), 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00);
	}

	@Trace
	public void testDoReplaceInlineCodePointWithChar() {
		CharSequenceInlineTools.doReplaceInlineCodePointWithChar(createGapString("abXcd"), 0, CodePointToolsTest.CODE_POINT_SURROGATE_00, 'X');
		CharSequenceInlineTools.doReplaceInlineCodePointWithChar(createGapString("X"), 0, CodePointToolsTest.CODE_POINT_SURROGATE_00, 'X');
		CharSequenceInlineTools.doReplaceInlineCodePointWithChar(createGapString("XX"), 0, CodePointToolsTest.CODE_POINT_SURROGATE_00, 'X');
		CharSequenceInlineTools.doReplaceInlineCodePointWithChar(createGapString("aXbXc"), 0, CodePointToolsTest.CODE_POINT_SURROGATE_00, 'X');
		CharSequenceInlineTools.doReplaceInlineCodePointWithChar(createGapString("XbX"), 0, CodePointToolsTest.CODE_POINT_SURROGATE_00, 'X');
	}

	GapString createGapString(String s) {
		return new GapString(s.replace("X", CodePointToolsTest.STRING_SURROGATE_00));
	}

	@Trace(traceClass = "CharSequenceInlineTools")
	public void testDoReplaceInlineString() {
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("abXcd"), 0, "X", "01");
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("X"), 0, "X", "01");
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("XX"), 0, "X", "01");
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("aXbXc"), 0, "X", "01");
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("XbX"), 0, "X", "01");
		CharSequenceInlineTools.doReplaceInlineString(new GapString("XbX"), 0, "", "01");
		//CharSequenceInlineTools.doReplaceInlineString(new GapString("a-b-c"), 0, "-", "--");
	}

	public static class ReplaceStringTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			CharEqual equal = CharEqual.isEqualChar();
			IndexOfString io = IndexOfString.indexOfChar(equal);

			CyclicSource<String> strs = new CyclicSource<>("abc", "aba", "bcd");
			CyclicSource<String> finds = new CyclicSource<>("a", "x");
			CyclicSource<String> replaces = new CyclicSource<>("A", "X");
		}

		@Benchmark
		public CharSequence testReplace2(MyState state) {
			String str = state.strs.next();
			String find = state.finds.next();
			String replace = state.replaces.next();
			return CharSequenceTools.doReplaceString(str, find, replace, 0, state.io);
		}

		@Benchmark
		public CharSequence testReplace1(MyState state) {
			String str = state.strs.next();
			String find = state.finds.next();
			String replace = state.replaces.next();
			return doReplaceString(str, 0, find, true, replace, state.equal);
		}

		static CharSequence doReplaceString(CharSequence str, int start, CharSequence find, boolean replace, CharSequence replaceStr, CharEqual equal) {
			int index = CharSequenceTools.indexOf(str, find, start, equal);
			if (index == -1) {
				return str; // not found at all -> no change to do
			}

			int strLen = str.length();
			int findLen = find.length();
			int replaceLen = (replace) ? replaceStr.length() : 0;
			int newLen = strLen - findLen + replaceLen;
			int startOffset = (findLen == 0) ? 1 : 0; // guarantee termination if findStr is empty

			StringBuilder buf = new StringBuilder(newLen);
			while (index != -1) {
				buf.append(str, start, index);
				if (replace) {
					buf.append(replaceStr);
				}

				start = index + findLen;
				index = CharSequenceTools.indexOf(str, find, start + startOffset, equal);
			}
			buf.append(str, start, strLen);
			return buf.toString();
		}

	}

	public static class ReplaceInlineCharWithCodePointTestJmh extends StringJmhBenchmark {
		{
			setRunVerify(false);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			IList<String> strs = GapList.create("abXcd", "X", "XX", "aXbXc", "XbX");
			//String prefix = StringTools.repeat('-', 0);
			String prefix = StringTools.repeat(CodePointToolsTest.STRING_SURROGATE_00, 1000);
			IList<String> strs2 = strs.map(s -> prefix + s);
			CyclicSource<String> source = new CyclicSource<>(strs2);
		}

		@Benchmark
		public CharSequence testReplace(MyState state) {
			// Create GapString which will be modified by doReplaceInlineCharWithCodePoint()
			IString str = new GapString(state.source.next());
			return CharSequenceTools.replaceCodePoint(str, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00, CodePointEqual.isEqualCodePoint(), 0);
		}

		@Benchmark
		public CharSequence testReplaceInline(MyState state) {
			// Create GapString which will be modified by doReplaceInlineCharWithCodePoint()
			IString str = new GapString(state.source.next());
			return CharSequenceInlineTools.doReplaceInlineCharWithCodePoint(str, 0, 'X', CodePointToolsTest.CODE_POINT_SURROGATE_00, CharEqual.isEqualChar());
		}
	}

	@Trace
	public void testRetainCharPredicate() {
		CharPredicate cp = c -> c >= '0' && c <= '9';
		CharSequenceTools.retainCharPredicate("abc", 0, cp);
		CharSequenceTools.retainCharPredicate("abc012def", 0, cp);
		CharSequenceTools.retainCharPredicate("abc012def345ghi", 0, cp);
		CharSequenceTools.retainCharPredicate("abc012", 0, cp);
		CharSequenceTools.retainCharPredicate("012def", 0, cp);
		CharSequenceTools.retainCharPredicate("012", 0, cp);
		CharSequenceTools.retainCharPredicate("", 0, cp);
	}

	@Capture
	public void testChecked() {
		String s = "a-b-c-d";
		{
			LOG.info("=== Replace");
			String find = "-";
			String replace = "X";
			LOG.info("{}", s.replace(find, replace));
			LOG.info("{}", CharSequenceTools.replaceString(s, find, replace, 0));

			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 0, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 1, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 2, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 3, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 4, false));

			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 0, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 1, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 2, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 3, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 4, true));
		}
		{
			LOG.info("=== No change");
			String find = "-";
			String replace = "-";

			// String and StringUtils create a new string even if no change is done
			String s0 = s.replace(find, replace);
			CheckTools.check(s0 != s);
			String s1 = StringUtils.replace(s, find, replace);
			CheckTools.check(s1 != s);

			// CharSequenceTools detects that no new string is needed
			String s2 = (String) CharSequenceTools.replaceString(s, find, replace, 0);
			CheckTools.check(s2 == s);

			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 0, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 1, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 2, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 3, false));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 4, false));

			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 0, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 1, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 2, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 3, true));
			LOG.info("{}", CharSequenceTools.replaceStringChecked(s, find, replace, 0, 4, true));
		}

		{
			LOG.info("=== Remove");
			String find = "-";
			LOG.info("{}", s.replace(find, ""));
			LOG.info("{}", CharSequenceTools.removeString(s, find, 0));
		}
		{
			LOG.info("=== Empty");
			String find = "";
			String replace = "X";
			String s1 = s.replace(find, replace);
			CheckTools.check(s1.equals("XaX-XbX-XcX-XdX"));

			String s2 = (String) CharSequenceTools.replaceString(s, find, replace, 0);
			CheckTools.check(s2.equals(s1));

			String s3 = (String) CharSequenceTools.replaceStringChecked(s, find, replace, 0, 9, false);
			CheckTools.check(s3.equals(s1));
		}
	}

	@Capture
	public void testReplaceCodePoint() {
		{
			int cp = CodePointToolsTest.CODE_POINT_SURROGATE_0;
			assert !CodePointTools.isCharCodePoint(cp);
			String str = CodePointTools.codePointToString(cp);
			assert str.equals(CodePointToolsTest.STRING_SURROGATE_0);
			int cp2 = CodePointTools.codePointAt(str, 0);
			assert cp2 == cp;
		}
		String str = CodePointToolsTest.STRING_SURROGATE_0 + CodePointToolsTest.STRING_SURROGATE_1 + CodePointToolsTest.STRING_SURROGATE_0;
		LOG.info("{}", formatCodePointString(str));

		String str2 = (String) CharSequenceTools.doReplaceCodePoint(str, CodePointToolsTest.CODE_POINT_SURROGATE_1,
				CodePointToolsTest.CODE_POINT_SURROGATE_0, 0);
		LOG.info("{}", formatCodePointString(str2));

		String str3 = (String) CharSequenceTools.doReplaceCodePointChecked(str, CodePointToolsTest.CODE_POINT_SURROGATE_1,
				CodePointToolsTest.CODE_POINT_SURROGATE_0, 0, -1, false);
		LOG.info("{}", formatCodePointString(str3));
	}

	public static String formatCodePointString(CharSequence str) {
		int len = str.length();
		int cpLen = CodePointTools.codePointCount(str);
		StringPrinter buf = new StringPrinter().setElemMarker(", ");
		if (len == cpLen) {
			buf.print("charLen: {}: ", len);
			for (int i = 0; i < len; i++) {
				char c = str.charAt(i);
				buf.add(formatChar(c));
			}
		} else {
			buf.print("codePointLen: {} (charLen : {}): ", cpLen, len);
			for (int i = 0; i < len;) {
				int cp = -1;
				char c1 = str.charAt(i);
				if (i < len - 1 && Character.isHighSurrogate(c1)) {
					char c2 = str.charAt(i + 1);
					if (Character.isLowSurrogate(c2)) {
						cp = Character.toCodePoint(c1, c2);
					}
				}
				if (cp != -1) {
					buf.add(formatCodePoint(cp));
					i += CodePointTools.charCount(cp);
				} else {
					buf.add(formatChar(c1));
					i++;
				}
			}

		}
		return buf.toString();
	}

	public static String formatCodePoint(int cp) {
		if (CodePointTools.isCharCodePoint(cp)) {
			return formatChar((char) cp);
		} else {
			boolean print = CharTools.isPrint(cp);
			String s1 = CharTools.toUnicodeChars(cp);
			String s2 = CharTools.toUnicodeNumber(cp);
			if (print) {
				String s3 = Character.toString(cp);
				return s1 + " (" + s2 + ", '" + s3 + "')";
			} else {
				return s1 + " (" + s2 + ")";
			}
		}
	}

	public static String formatChar(char c) {
		boolean print = CharTools.isPrint(c);
		String s = CharTools.toUnicodeChar(c);
		if (print) {
			return s + " ('" + c + "')";
		} else {
			return s;
		}
	}

	/** 
	 * Verify full test coverage (100%, no missed instructions) of methods <br> 
	 * indexOf, lastIndexOf, reverseIndexOf, contains, startsWith, startsAt, endsWith, endsAt, equals
	 */
	@Capture(source = Source.NONE)
	public void testVerifyCoverage() {
		JavaGradleEclipseProject jbp = new JavaGradleEclipseProject();
		jbp.setDir(FilePath.CURRENT_PATH);
		JacocoRunner jc = new JacocoRunner(jbp);

		jc.setExecuteMethod(CharSequenceToolsTest.class, "testVerify");
		InstructionStats is = jc.getJacocoInstructionStats();
		IList<String> mns = GapList.create(
				// testVerifyIndexOf
				"indexOf", "doIndexOf", "lastIndexOf", "doLastIndexOf", "reverseIndexOf", "doReverseIndexOf",
				"contains", "startsWith", "startsAt", "endsWith", "endsAt",
				// testVerifyEquals
				"equals", "doEquals");
		ConditionalPredicateSupplier<String> cs = new ConditionalPredicateSupplier<>(Mode.STATS);
		cs.addFilterValues(mns);
		Predicate<String> pred = cs.getConditionalPredicateEval();

		// Format "pkg/cls$inner"
		String cn = "org/magicwerk/brownies/core/strings/CharSequenceTools";
		//String desc = "(Ljava/lang/CharSequence;Ljava/util/Collection;)I"; // TODO remove
		// desc.equals(m.getMethodDesc()
		IList<MethodInstructionStats> mis = is.getMethodStats()
				.filter(m -> m.getClassName().equals(cn) && pred.test(m.getMethodName()));
		IList<MethodInstructionStats> mis2 = mis.filter(m -> m.getMissed() > 0);

		try {
			CheckTools.check(mis2.isEmpty());
			cs.errorOnUnusedFilterValues();
			return;
		} catch (Exception e) {
			LOG.info(StringPrinter.formatLines(mis2));
			// Show HTML report for analysis
			jc.showJacocoHtmlReport("org.magicwerk.brownies.core.strings/CharSequenceTools");
		}
	}

	// Code point

	/**
	 * String.indexOf(char) is about 10% faster than CharSequenceTools.indexOf(CharSequence, char)
	 * Handling code points instead of characters is about 20% slower
	 * Handling case insensitivity is about 50% slower, in fast mode about 30% 
	 */
	public static class IndexOfCharTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			final char CHAR = '\n';
			final int CODEPOINT = CHAR;
			CyclicSource<String> source = new CyclicSource<>("ab", "ab\rcd", "ab\ncd");
		}

		// Char

		@Benchmark
		public int testStringIndexOf(MyState state) {
			return state.source.next().indexOf(state.CHAR);
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfChar(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.CHAR);
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCharCaseSensitive(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.CHAR, CharEqual.isEqualChar());
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCharCaseInsensitive(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.CHAR, CharEqual.isEqualCharIgnoreCase());
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCharCaseInsensitiveFast(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.CHAR, CharEqual.isEqualCharIgnoreCaseFast());
		}

		// Code point

		@Benchmark
		public int testCharSequenceToolsIndexOfCodePoint(MyState state) {
			return CharSequenceTools.doIndexOfCodePoint(state.source.next(), state.CODEPOINT, 0);
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCodePointCaseSensitive(MyState state) {
			return CharSequenceTools.doIndexOfCodePoint(state.source.next(), state.CODEPOINT, 0, CodePointEqual.isEqualCodePoint());
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCodePointCaseInsensitive(MyState state) {
			return CharSequenceTools.doIndexOfCodePoint(state.source.next(), state.CODEPOINT, 0, CodePointEqual.isEqualCodePointIgnoreCase());
		}

		@Benchmark
		public int testCharSequenceToolsIndexOfCodePointCaseInsensitiveFast(MyState state) {
			return CharSequenceTools.doIndexOfCodePoint(state.source.next(), state.CODEPOINT, 0, CodePointEqual.isEqualCodePointIgnoreCaseFast());
		}

	}

	/**
	 * Show that indexOf0() is fastest.
	 */
	public static class IndexOfCodePointTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			final int FIND = CodePointToolsTest.CODE_POINT_SURROGATE_0;
			CyclicSource<String> source = new CyclicSource<>(StringTools.repeat("x", 100) + CodePointToolsTest.STRING_SURROGATE_0);
			CyclicSource<String> source2 = new CyclicSource<>(
					StringTools.repeat(CodePointToolsTest.STRING_SURROGATE_1, 100) + CodePointToolsTest.STRING_SURROGATE_0);
		}

		@Benchmark
		public int testIndexOf0(MyState state) {
			String s = state.source.next();
			return indexOf0(s, state.FIND);
		}

		@Benchmark
		public int testIndexOf1(MyState state) {
			String s = state.source.next();
			return indexOf1(s, state.FIND);
		}

		@Benchmark
		public int testIndexOf2(MyState state) {
			String s = state.source.next();
			return indexOf2(s, state.FIND);
		}

		@Benchmark
		public int testIndexOf3(MyState state) {
			String s = state.source.next();
			return indexOf3(s, state.FIND);
		}

		// TODO add test for code point, see java.lang.Character with 
		// isBmpCodePoint()/isSupplementaryCodePoint()/charCount()/isValidCodePoint()

		static int indexOf0(CharSequence str, int cp) {
			char c0 = Character.highSurrogate(cp);
			char c1 = Character.lowSurrogate(cp);
			int len = str.length();
			for (int i = 0; i < len; i++) {
				char high = str.charAt(i);
				if (high == c0) {
					char low = str.charAt(i + 1);
					if (low == c1) {
						return i;
					}
				}
			}
			return -1;
		}

		static int indexOf1(CharSequence str, int cp) {
			char c0 = Character.highSurrogate(cp);
			char c1 = Character.lowSurrogate(cp);
			int len = str.length();
			for (int i = 0; i < len; i++) {
				char high = str.charAt(i);
				char low = str.charAt(i + 1);
				if (high == c0 && low == c1) {
					return i;
				}
			}
			return -1;
		}

		static int indexOf2(CharSequence str, int cp) {
			char c0 = Character.highSurrogate(cp);
			char c1 = Character.lowSurrogate(cp);
			int len = str.length();
			int i1 = str.charAt(0);
			for (int i = 0; i < len; i++) {
				int i0 = i1;
				i1 = str.charAt(i);
				if (i0 == c0 && i1 == c1) {
					return i - 1;
				}
			}
			return -1;
		}

		static int indexOf3(CharSequence str, int cp) {
			int i = 0;
			int len = str.length();
			while (i < len) {
				int cp2 = CodePointTools.codePointAt(str, i);
				if (cp2 == cp) {
					return i;
				}
				i += CodePointTools.charCount(cp2);
			}
			return -1;
		}
	}

	/**
	 * Show that doRemoveCodePointPredicate() is faster than doRemoveCodePointPredicate2().
	 */
	public static class RemoveCodePointPredicateTestJmh extends StringJmhBenchmark {
		{
			//setRunTime(1000);
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			int findChar = '0';
			//int findChar = CharToolsTest.CHAR_LEN_2;
			//int findChar = CodePointToolsTest.CODE_POINT_SURROGATE_0;
			String str = CodePointTools.codePointToString(findChar);
			IntPredicate findCodePointPredicate = CodePointPredicates.equals(findChar);
			CyclicSource<String> source = new CyclicSource<>(str + "1234", str + "1234" + str + "1234");
		}

		@Benchmark
		public CharSequence testRemoveCodePoint(MyState state) {
			return doRemoveCodePointPredicate(state.source.next(), 0, state.findCodePointPredicate);
		}

		@Benchmark
		public CharSequence testRemoveCodePoint2(MyState state) {
			return doRemoveCodePointPredicate2(state.source.next(), 0, state.findCodePointPredicate);
		}

		// Identical code as in CharSequenceTools.doRemoveCodePointPredicate
		static CharSequence doRemoveCodePointPredicate(CharSequence str, int start, IntPredicate predicate) {
			int pos = CharSequenceTools.doIndexOfCodePointPredicate(str, predicate, start);
			if (pos == -1) {
				return str;
			}

			// Use pos/2 as start position as pos is in chars, but cps in code points
			final int[] cps = CodePointTools.getCodePointArray(str);
			for (int i = pos / 2; i < cps.length; i++) {
				if (!predicate.test(cps[i])) {
					cps[pos++] = cps[i];
				}
			}
			return new String(cps, 0, pos);
		}

		static CharSequence doRemoveCodePointPredicate2(CharSequence str, int start, IntPredicate predicate) {
			int index = CharSequenceTools.doIndexOfCodePointPredicate(str, predicate, start);
			if (index == -1) {
				return str;
			}

			int len = str.length();
			StringBuilder buf = new StringBuilder(len);
			int base = 0;
			while (index != -1) {
				buf.append(str, base, index);
				int cp = CodePointTools.codePointAt(str, index);
				base = index + CodePointTools.charCount(cp);
				index = CharSequenceTools.doIndexOfCodePointPredicate(str, predicate, base);
			}
			buf.append(str, base, len);
			return buf.toString();
		}
	}

	/**
	 * Show that the special loop coming from Guava is not faster.
	 */
	public static class RemoveCharTestJmh extends StringJmhBenchmark {
		{
			//setRunTime(1000);
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char findChar = '0';
			CharPredicate findCharPredicate = CharPredicates.equals(findChar);
			CyclicSource<String> source = new CyclicSource<>("01234", "0123401234");
		}

		@Benchmark
		public CharSequence testRemoveChar0(MyState state) {
			return doRemoveChar0(state.source.next(), 0, state.findCharPredicate);
		}

		@Benchmark
		public CharSequence testRemoveChar1(MyState state) {
			return doRemoveChar1(state.source.next(), 0, state.findChar);
		}

		@Benchmark
		public CharSequence testRemoveChar2(MyState state) {
			return doRemoveChar2(state.source.next(), 0, state.findChar);
		}

		// Code identical to CharSequenceTools.doRemoveChar
		public static CharSequence doRemoveChar0(CharSequence str, int start, CharPredicate predicate) {
			int pos = CharSequenceTools.doIndexOfCharPredicate(str, predicate, start);
			if (pos == -1) {
				return str;
			}

			final char[] chars = CharSequenceTools.toCharArray(str);
			for (int i = pos + 1; i < chars.length; i++) {
				if (!predicate.test(chars[i])) {
					chars[pos++] = chars[i];
				}
			}
			return new String(chars, 0, pos);
		}

		public static CharSequence doRemoveChar1(CharSequence str, int start, char findChar) {
			int pos = CharSequenceTools.doIndexOfChar(str, findChar, start);
			if (pos == -1) {
				return str;
			}

			final char[] chars = CharSequenceTools.toCharArray(str);
			for (int i = pos + 1; i < chars.length; i++) {
				if (chars[i] != findChar) {
					chars[pos++] = chars[i];
				}
			}
			return new String(chars, 0, pos);
		}

		public static CharSequence doRemoveChar2(CharSequence str, int start, char findChar) {
			int pos = CharSequenceTools.doIndexOfChar(str, findChar, start);
			if (pos == -1) {
				return str;
			}

			// This unusual loop comes from extensive benchmarking (Guava)
			final char[] chars = CharSequenceTools.toCharArray(str);
			int spread = 1;
			OUT: while (true) {
				pos++;
				while (true) {
					if (pos == chars.length) {
						break OUT;
					}
					if (chars[pos] == findChar) {
						break;
					}
					chars[pos - spread] = chars[pos];
					pos++;
				}
				spread++;
			}
			return new String(chars, 0, pos - spread);
		}

	}

	/**
	 * Show that implementation of character comparison with a lambda checking for equality is as fast
	 * as coding it inline manually by writing simply "c1 == c2".
	 */
	public static class ReplaceCharIgnoreCaseTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			char findChar = 'a';
			char replaceChar = 'B';
			CyclicSource<String> source = new CyclicSource<>("01234", "0123456789");
		}

		@Benchmark
		public CharSequence testReplaceChar(MyState state) {
			return replaceChar(state.source.next(), 0, state.findChar, state.replaceChar);
		}

		@Benchmark
		public CharSequence testReplaceChar2(MyState state) {
			return replaceChar2(state.source.next(), 0, state.findChar, state.replaceChar);
		}

		//

		public static CharSequence replaceChar(CharSequence str, int start, char searchChar, char replaceChar) {
			if (searchChar == replaceChar) {
				return str;
			}

			StringBuilder buf = null;
			int len = str.length();
			int base = 0;
			for (int i = start; i < len; i++) {
				if (str.charAt(i) == searchChar) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					if (base < i) {
						buf.append(str, base, i);
					}
					buf.append(replaceChar);
					base = i + 1;
				}
			}

			if (buf != null) {
				if (base < len) {
					buf.append(str, base, len);
				}
				return buf.toString();
			} else {
				return str;
			}
		}

		public static CharSequence replaceChar2(CharSequence str, int start, char searchChar, char replaceChar) {
			return replaceChar2(str, start, searchChar, replaceChar, CharEqual.isEqualChar());
		}

		public static CharSequence replaceChar2(CharSequence str, int start, char searchChar, char replaceChar, CharEqual equals) {
			if (searchChar == replaceChar) {
				return str;
			}

			StringBuilder buf = null;
			int len = str.length();
			int base = 0;
			for (int i = start; i < len; i++) {
				if (str.charAt(i) == searchChar) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					if (base < i) {
						buf.append(str, base, i);
					}
					buf.append(replaceChar);
					base = i + 1;
				}
			}

			if (buf != null) {
				if (base < len) {
					buf.append(str, base, len);
				}
				return buf.toString();
			} else {
				return str;
			}
		}

	}

	/**
	 * Show that case sensitive search is nearly as twice as fast as StringUtils (using own implementation of regionMatches).
	 * Show that case insensitive matching can be vastly improved by just checking the uppercase characters and ignore the lowercase ones.
	 */
	public static class IndexOfIgnoreCaseTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String fincCS = "abc";
			String fincCI = "ABC";
			CyclicSource<String> source = new CyclicSource<>("01234abc", "01234");
		}

		@Benchmark
		public int testStringUtils(MyState state) {
			return StringUtils.indexOfIgnoreCase(state.source.next(), state.fincCI, 0);
		}

		@Benchmark
		public int testCaseSensitive(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.fincCS, 0);
		}

		@Benchmark
		public int testIgnoreCaseUpperLower(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.fincCI, 0, CharEqual.isEqualCharIgnoreCase());
		}

		@Benchmark
		public int testIgnoreCaseUpper(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.fincCI, 0, CharEqual.isEqualCharIgnoreCase());
		}
	}

	public static class IndexOfChar2TestJmh extends StringJmhBenchmark {
		{
			//setRunTime(1000);
			setJavaVersions(JavaVersion.JAVA_17);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			//String str = "012";
			//String str = "01234";
			String str = "0123456";
			char[] cs = str.toCharArray();
			CharPredicate pred = CharPredicates.oneOf(str);
			CyclicSource<String> source = new CyclicSource<>("abcde", "ab1cd", "abcd5");
		}

		@Benchmark
		public int testIndexOfString(MyState state) {
			String s = state.source.next();
			int n = 0;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				n += (indexOf(state.str, c) != -1 ? 1 : 0);
			}
			return n;
		}

		@Benchmark
		public int testIndexOfChars(MyState state) {
			String s = state.source.next();
			int n = 0;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				n += (indexOf(state.cs, c) != -1 ? 1 : 0);
			}
			return n;
		}

		@Benchmark
		public int testIndexOfCharPredicate(MyState state) {
			String s = state.source.next();
			int n = 0;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				n += (contains(state.pred, c) ? 1 : 0);
			}
			return n;
		}

		static int indexOf(String s, char c) {
			return s.indexOf(c);
		}

		static int indexOf(char[] cs, char c) {
			for (int i = 0; i < cs.length; i++) {
				if (cs[i] == c) {
					return i;
				}
			}
			return -1;
		}

		static boolean contains(CharPredicate p, char c) {
			return p.test(c);
		}

	}

	public static class IndexOfAnyCharTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String CHARS = "01234";
			CharPredicate PREDICATE1 = CharPredicates.oneOf(CHARS);
			CharPredicate PREDICATE2 = c -> c == '0' || c == '1' || c == '2' || c == '3' || c == '4';
			Pattern PATTERN = Pattern.compile(RegexBuilder.regexCharacterClassForChars(CHARS));
			CyclicSource<String> source = new CyclicSource<>("ab2cd", "abcd", "abcd4");
		}

		@Benchmark
		public int testIndexOfPredicate1(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.PREDICATE1, 0);
		}

		@Benchmark
		public int testIndexOfPredicate2(MyState state) {
			return CharSequenceTools.indexOf(state.source.next(), state.PREDICATE2, 0);
		}

		@Benchmark
		public int testIndexOfPattern(MyState state) {
			Matcher matcher = state.PATTERN.matcher(state.source.next());
			return (matcher.find()) ? matcher.start() : -1;
		}
	}

	public static class ReplaceAnyCharTestJmh extends StringJmhBenchmark {
		{
			setJavaVersions(JavaVersion.JAVA_21);
			setRunTime(1000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String findAnyChar = "xxxxxxxxxx01234";
			String replaceAnyChar = "xxxxxxxxxx56789";
			CharPredicate findCharPredicate = CharPredicates.oneOf(findAnyChar);
			CharOperator charOperator = c -> {
				int i = findAnyChar.indexOf(c);
				return replaceAnyChar.charAt(i);
			};
			CharFunctionTEST charFunction = c -> {
				int i = findAnyChar.indexOf(c);
				return (i != -1) ? replaceAnyChar.charAt(i) : c;
			};
			CharFunctionTEST cf = c -> {
				return findAnyChar.indexOf(c);
			};
			IntUnaryOperator cf2 = i -> {
				return (i < replaceAnyChar.length()) ? replaceAnyChar.charAt(i) : -1;
			};
			CharPredicate finder = c -> charFunction.apply(c) != c;
			AppendCharReplacer charReplacer = (char c, StringBuilder buf) -> {
				int i = findAnyChar.indexOf(c);
				c = replaceAnyChar.charAt(i);
				buf.append(c);
			};
			CharFunctionTEST charFunction2 = c -> {
				int i = findAnyChar.indexOf(c);
				if (i == -1) {
					return c;
				}
				return (i < replaceAnyChar.length()) ? replaceAnyChar.charAt(i) : -1;
			};
			CharPredicate finder2 = c -> charFunction2.apply(c) != c;

			CyclicSource<String> source = new CyclicSource<>("ab2cd", "abc3d", "abcd4");
		}

		@Benchmark
		public CharSequence testReplaceAny1(MyState state) {
			return CharSequenceTools.doReplaceAnyChar(state.source.next(), 0, state.findAnyChar, state.replaceAnyChar, CharIndexEqual.indexOf());
		}

		@Benchmark
		public CharSequence testReplaceAny1b(MyState state) {
			return CharSequenceTools.doReplaceAnyCharTEST(state.source.next(), 0, state.cf, state.cf2);
		}

		@Benchmark
		public CharSequence testReplaceAny2(MyState state) {
			return CharSequenceTools.doReplaceCharPredicateTEST(state.source.next(), state.finder, state.charFunction, 0);
		}

		//@Benchmark
		public CharSequence testReplaceAny3(MyState state) {
			return CharSequenceTools.doReplaceCharPredicate(state.source.next(), state.findCharPredicate, state.charOperator, 0);
		}

		//@Benchmark
		public CharSequence testReplaceAny4(MyState state) {
			return CharSequenceTools.doReplaceCharPredicate(state.source.next(), state.findCharPredicate, state.charReplacer, 0);
		}

	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.CharSequenceTools$CharSequenceView", traceMethod = "/.*/")
	public void testCharSequenceView() {
		{
			String s0 = "012345";
			CharSequenceView s1 = new CharSequenceView(s0, 1, 5);
			LOG.info("{}", s1);
			CharSequenceView s2 = new CharSequenceView(s1, 1, 3);
			LOG.info("{}", s2.toString());
		}

		String str = "abcd";
		CharSequenceTest.testCharSequence(new CharSequenceView(str, 0, -1));
		CharSequenceTest.testCharSequence(new CharSequenceView(str, 1, 3));
		CharSequenceTest.testCharSequence(new CharSequenceView(str, 1, 4));

		// Errors
		new CharSequenceView(null, 0, 0);
		new CharSequenceView(str, -1, 1);
		new CharSequenceView(str, 9, 1);
		new CharSequenceView(str, 0, -2);
		new CharSequenceView(str, 0, 9);
		new CharSequenceView(str, 2, 1);
	}

	@Trace
	public void testStartsWith() {
		// true
		CharSequenceTools.startsWith("abcd", "abc");
		CharSequenceTools.startsWith("abcd", "abcd");

		// false
		CharSequenceTools.startsWith("abcd", "abdec");
		CharSequenceTools.startsWith("abcd", "aBc");
	}

	@Trace
	public void testEndsWith() {
		// true
		CharSequenceTools.endsWith("abcd", "bcd");
		CharSequenceTools.endsWith("abcd", "abcd");

		// false
		CharSequenceTools.endsWith("abcd", "abdec");
		CharSequenceTools.endsWith("abcd", "bCd");
	}

	//

	@Trace
	public void testLastIndexOf() {
		// found
		doTestLastIndexOf("abcd", "cd", 5);
		doTestLastIndexOf("abcd", "cd", 4);
		doTestLastIndexOf("abcd", "cd", 3);
		doTestLastIndexOf("abcd", "cd", 2);

		// not found
		doTestLastIndexOf("abcd", "cd", 1);
		doTestLastIndexOf("abcd", "cd", 0);
		doTestLastIndexOf("abcd", "cd", -1);
	}

	@InheritTrace
	void doTestLastIndexOf(String str, String find, int start) {
		int i0 = CharSequenceTools.lastIndexOf(str, find, start);
		int i1 = str.lastIndexOf(find, start);
		ObjectDiff.checkEqual(i0, i1);
	}

	//

	public void testVerify() {
		testVerifyEquals();
		testVerifyIndexOf();
	}

	/** 
	 * Test methods systematically to get full test coverage (100%, no missed instructions) for <br> 
	 * equals
	 */
	public void testVerifyEquals() {

		String lo = "abc";
		String up = "aBc";
		String diff = "aXc";

		// equals(CharSequence str0, CharSequence str1) 
		CheckTools.check(CharSequenceTools.equals(lo, lo) == true);
		CheckTools.check(CharSequenceTools.equals(lo, up) == false);
		CheckTools.check(CharSequenceTools.equals(lo, diff) == false);

		CheckTools.check(CharSequenceTools.equals(lo, lo + "x") == false);

		// equals(CharSequence str0, CharSequence str1, CharsEqual equal)
		CheckTools.check(CharSequenceTools.equals(lo, lo, CharEqual.isEqualCharIgnoreCase()) == true);
		CheckTools.check(CharSequenceTools.equals(lo, up, CharEqual.isEqualCharIgnoreCase()) == true);
		CheckTools.check(CharSequenceTools.equals(lo, diff, CharEqual.isEqualCharIgnoreCase()) == false);

		CheckTools.check(CharSequenceTools.equals(lo, lo + "x", CharEqual.isEqualCharIgnoreCase()) == false);

		// equals(CharSequence str0, CharSequence str1, CodePointsEqual equal)
		CheckTools.check(CharSequenceTools.equals(lo, lo, CodePointEqual.isEqualCodePointIgnoreCase()) == true);
		CheckTools.check(CharSequenceTools.equals(lo, up, CodePointEqual.isEqualCodePointIgnoreCase()) == true);
		CheckTools.check(CharSequenceTools.equals(lo, diff, CodePointEqual.isEqualCodePointIgnoreCase()) == false);

		CheckTools.check(CharSequenceTools.equals(lo, lo + "x", CodePointEqual.isEqualCodePointIgnoreCase()) == false);

		String lo1 = "1abc1";
		String lo2 = "22abc22";
		String up2 = "22aBc22";

		// equals(CharSequence str0, int index0, CharSequence str1, int index1, int len)
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo1, 1, 3) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo2, 2, 3) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, up2, 2, 3) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo1, 99, 3) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 99, lo2, 2, 3) == false);

		// equals(CharSequence str0, int index0, CharSequence str1, int index1, int len, CharEqual equal)
		CharEqual charEqual = CharEqual.isEqualCharIgnoreCase();
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo2, 2, 3, charEqual) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, up2, 2, 3, charEqual) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, up2, 1, 3, charEqual) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo2, 99, 3, charEqual) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 99, up2, 2, 3, charEqual) == false);

		// equals(CharSequence str0, int index0, CharSequence str1, int index1, int len, CodePointEqual equal)
		CodePointEqual codePointEqual = CodePointEqual.isEqualCodePointIgnoreCase();
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo2, 2, 3, codePointEqual) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, up2, 2, 3, codePointEqual) == true);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, up2, 1, 3, codePointEqual) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 1, lo2, 99, 3, codePointEqual) == false);
		CheckTools.check(CharSequenceTools.equals(lo1, 99, up2, 2, 3, codePointEqual) == false);
	}

	@Capture
	public void testVerifyIStringMatcher() {
		String input = "abXcdXef";
		{
			IList<IStringMatcher> isms = GapList.create(
					// char
					new CharMatcher('X'),
					new CharIgnoreCaseMatcher('x', CharEqual.isEqualCharIgnoreCase()),
					new AnyCharMatcher("XZ"),
					new AnyCharIgnoreCaseMatcher("xz", CharEqual.isEqualCharIgnoreCase()),
					new CharPredicateMatcher(x -> x == 'X'),
					new CharPredicateIgnoreCaseMatcher(x -> x == 'x'),

					// code point
					new CodePointMatcher('X'),
					new CodePointIgnoreCaseMatcher('x', CodePointEqual.isEqualCodePointIgnoreCase()),
					new AnyCodePointMatcher("XZ"),
					new AnyCodePointIgnoreCaseMatcher("XZ", CodePointEqual.isEqualCodePointIgnoreCase()),
					new CodePointPredicateMatcher(x -> x == 'X'),
					new CodePointPredicateIgnoreCaseMatcher(x -> x == 'x'),

					// string
					StringMatcher.of("X"),
					StringMatcher.of("x", CharEqual.isEqualCharIgnoreCase()),

					// strings: default
					StringsMatcher.builder().setSearchStrs("X", "Z").build(),
					StringsMatcher.builder().setSearchStrs("XX", "X").build(),
					StringsMatcher.builder().setSearchStrs("x", "Z").setIgnoreCase(true).build(),
					StringsMatcher.builder().setSearchStrs("x", "XX").setIgnoreCase(true).build(),

					// strings: tree
					StringsMatcher.builder().setSearchStrs("X", "Z").setUseTree(true).build(),
					StringsMatcher.builder().setSearchStrs("X", "Z").setSearchNotStrs("Y").setUseTree(true).build(),
					StringsMatcher.builder().setSearchStrs("x", "Z").setUseTree(true).setIgnoreCase(true).build(),
					StringsMatcher.builder().setSearchStrs("x", "Z").setSearchNotStrs("Y").setUseTree(true).setIgnoreCase(true).build(),

					// regex
					new RegexStringMatcher().setPattern("X")
			//
			);

			IList<IMatch> matches = null;
			for (IStringMatcher ism : isms) {
				IList<IMatch> ms = testIStringMatcher(ism, input);
				if (matches == null) {
					matches = ms;
				} else {
					new ListDiff<>(ms, matches).checkEqual();
				}
			}
		}
		{
			// "abc".indexOf("", -5) == 0
			// "abc".indexOf("", 5) == 3
			// "abc".lastIndexOf("", -5) == -1
			// "abc".lastIndexOf("", 5) == 3

			// empty search string
			IList<IStringMatcher> isms = GapList.create(
					// string
					StringMatcher.of(""),
					StringMatcher.of("", CharEqual.isEqualCharIgnoreCase()),
					StringsMatcher.builder().setSearchStrs("", "Z").build(),
					StringsMatcher.builder().setSearchStrs("", "Z").setUseTree(true).build(),
					new RegexStringMatcher().setPattern("")
			//
			);

			IList<IMatch> matches = null;
			for (IStringMatcher ism : isms) {
				IList<IMatch> ms = testIStringMatcher(ism, input);
				if (matches == null) {
					matches = ms;
				} else {
					new ListDiff<>(ms, matches).checkEqual();
				}
			}
		}
	}

	@Capture
	public void testVerifyStringsMatcher() {
		String input = "abXcdXef";
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("X", "Z").build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("X", "").build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("XX", "Z").build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("XX", "").build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("x", "Z").setIgnoreCase(true).build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("x", "").setIgnoreCase(true).build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("XX", "Z").setIgnoreCase(true).build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("XX", "").setIgnoreCase(true).build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
		// Tree
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("X", "Z").setUseTree(true).build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("X", "").setUseTree(true).build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
		{
			IStringMatcher sm0 = StringsMatcher.builder().setSearchStrs("XX", "Z").setUseTree(true).build();
			IStringMatcher sm1 = StringsMatcher.builder().setSearchStrs("XX", "").setUseTree(true).build();
			doVerifyStringsMatcher(input, sm0, sm1);
		}
	}

	void doVerifyStringsMatcher(String input, IStringMatcher sm0, IStringMatcher sm1) {
		IList<IMatch> ms0 = testIStringMatcher(sm0, input);
		IList<IMatch> ms1 = testIStringMatcher(sm1, input);
		for (int i = 0; i < ms0.size(); i++) {
			IMatch m0 = ms0.get(i);
			IMatch m1 = ms1.get(i);
			int start = i - 2;
			if (m0 != null && m0.getStart() == start) {
				CheckTools.check(m0.equals(m1));
			} else {
				int pos = start;
				if (start < 0) {
					pos = 0;
				} else if (start > input.length()) {
					pos = input.length();
				}
				CheckTools.check(m1.getStart() == pos && m1.getEnd() == pos);
			}
		}
	}

	IList<IMatch> testIStringMatcher(IStringMatcher ism, String input) {
		IList<IMatch> matches = GapList.create();
		for (int i = -2; i <= input.length() + 2; i++) {
			IMatch match = testIStringMatcher(ism, input, i);
			matches.add(match);
		}
		return matches;
	}

	IMatch testIStringMatcher(IStringMatcher sf, String str, int pos) {
		// IStringMatcher
		boolean defaultStart = (pos == 0);

		// IStringMatcher.indexOf
		int index = sf.indexOf(str, pos);

		if (defaultStart) {
			int index2 = sf.indexOf(str);
			CheckTools.check(index2 == index);
		}

		// IStringMatcher.find
		IMatch match = sf.find(str, pos);
		if (index == -1) {
			CheckTools.check(match == null);
		} else {
			CheckTools.check(match.getStart() == index);
		}

		if (defaultStart) {
			IMatch match2 = sf.find(str);
			CheckTools.check(ObjectTools.equals(match2, match));
		}

		// IStringMatcher.indexOfEnd
		int endIndex = sf.indexOfEnd(str, pos);
		if (endIndex == -1) {
			CheckTools.check(match == null);
		} else {
			CheckTools.check(endIndex == match.getEnd());
		}

		if (defaultStart) {
			int endIndex2 = sf.indexOfEnd(str);
			CheckTools.check(ObjectTools.equals(endIndex2, endIndex));
		}

		// IStringReverseMatcher
		IMatch matchRev = null;
		if (sf instanceof IStringReverseMatcher) {
			IStringReverseMatcher isrm = (IStringReverseMatcher) sf;

			boolean defaultEnd = (pos == str.length());

			// IStringReverseMatcher.indexOfReverse
			int indexRev = isrm.indexOfReverse(str, pos);

			if (defaultEnd) {
				int indexRev2 = isrm.indexOfReverse(str);
				CheckTools.check(indexRev2 == indexRev);
			}

			// IStringReverseMatcher.findReverse
			matchRev = isrm.findReverse(str, pos);
			if (indexRev == -1) {
				CheckTools.check(matchRev == null);
			} else {
				CheckTools.check(matchRev.getStart() == indexRev);
			}

			if (defaultEnd) {
				IMatch matchRev2 = isrm.findReverse(str);
				CheckTools.check(ObjectTools.equals(matchRev2, matchRev));
			}

			// IStringReverseMatcher.indexOfEndReverse
			int endIndexRev = isrm.indexOfEndReverse(str, pos);
			if (endIndexRev == -1) {
				CheckTools.check(matchRev == null);
			} else {
				CheckTools.check(endIndexRev == matchRev.getEnd());
			}

			if (defaultEnd) {
				int endIndexRev2 = isrm.indexOfEndReverse(str);
				CheckTools.check(ObjectTools.equals(endIndexRev2, endIndexRev));
			}

			if (indexRev != -1) {
				CheckTools.check(indexRev != index || matchRev.getStart() == matchRev.getEnd());
			}
		}

		// IStringStartsAtMatcher
		if (sf instanceof IStringStartsAtMatcher) {
			IStringStartsAtMatcher issm = (IStringStartsAtMatcher) sf;

			// IStringStartsAtMatcher.startsAt
			boolean starts = issm.startsAt(str, pos);
			if (starts) {
				CheckTools.check(match.getStart() == pos);
			} else {
				CheckTools.check(match == null || match.getStart() != pos);
			}

			// IStringStartsAtMatcher.indexOfEndStartingAt
			int startEnds = issm.indexOfEndStartingAt(str, pos);
			if (starts) {
				CheckTools.check(startEnds == endIndex);
			} else {
				CheckTools.check(startEnds == -1);
			}

			// IStringStartsAtMatcher.matchStartingAt
			IMatch startMatch = issm.matchStartingAt(str, pos);
			if (starts) {
				CheckTools.check(ObjectTools.equals(startMatch, match));
			} else {
				CheckTools.check(startMatch == null);
			}
		}

		// IStringEndsAtMatcher
		if (sf instanceof IStringEndsAtMatcher) {
			IStringEndsAtMatcher isem = (IStringEndsAtMatcher) sf;

			// IStringEndsAtMatcher.endsAt
			boolean ends = isem.endsAt(str, pos);
			CheckTools.check(ends == (matchRev != null && matchRev.getEnd() == pos));

			// IStringEndsAtMatcher.indexOfEndingAt
			int endStarts = isem.indexOfEndingAt(str, pos);
			if (ends) {
				CheckTools.check(endStarts == matchRev.getStart());
			} else {
				CheckTools.check(endStarts == -1);
			}

			// IStringEndsAtMatcher.matchEndingAt
			IMatch endMatch = isem.matchEndingAt(str, pos);
			if (ends) {
				CheckTools.check(ObjectTools.equals(endMatch, matchRev));
			} else {
				CheckTools.check(endMatch == null);
			}
		}

		return match;
	}

	/** 
	 * Test methods systematically to get full test coverage (100%, no missed instructions) for <br> 
	 * indexOf, lastIndexOf, reverseIndexOf, contains, startsWith, startsAt, endsWith, endsAt
	 */
	@Capture
	public void testVerifyIndexOf() {
		{
			// char
			String input = "abXcdXef";
			char c = 'X';
			char cc = 'Y';
			int len = 1;

			CheckTools.check(callContainsChar(input, c));
			CheckTools.check(!callContainsChar(input, cc));

			for (int i = -2; i <= input.length() + 2; i++) {
				int i0 = callIndexOfChar(input, c, i);
				int i1 = callLastIndexOfChar(input, c, i);
				int i2 = callReverseIndexOfChar(input, c, i + len);

				if (i0 == i) {
					// match
					CheckTools.check(i1 == i && i2 == i + len);
					CheckTools.check(callStartsAtChar(input, c, i));
					CheckTools.check(callEndsAtChar(input, c, i + 1));
				} else {
					// no match
					CheckTools.check((i1 == -1 && i2 == -1) || (i0 == -1 || (i1 < i0 && i2 < i0) && i2 == i1 + len));
					CheckTools.check(!callStartsAtChar(input, c, i));
					CheckTools.check(!callEndsAtChar(input, c, i + 1));
				}
			}

			// empty input
			input = "";
			int pos = 1;
			CheckTools.check(!callContainsChar(input, cc));
			CheckTools.check(callIndexOfChar(input, c, pos) == -1);
			CheckTools.check(callLastIndexOfChar(input, c, pos) == -1);
			CheckTools.check(callReverseIndexOfChar(input, c, pos) == -1);
			CheckTools.check(!callStartsAtChar(input, c, pos));
			CheckTools.check(!callEndsAtChar(input, c, pos));
		}
		{
			// code point
			String input = "ab" + CodePointToolsTest.STRING_SURROGATE_0 + "cd" + CodePointToolsTest.STRING_SURROGATE_0 + "ef";
			int c = CodePointToolsTest.CODE_POINT_SURROGATE_0;
			int cc = CodePointToolsTest.CODE_POINT_SURROGATE_1;
			int len = 2;

			CheckTools.check(!callContainsCodePoint("", cc));

			CheckTools.check(callContainsCodePoint(input, c));
			CheckTools.check(!callContainsCodePoint(input, cc));

			for (int i = -2; i <= input.length() + 2; i++) {
				int i0 = callIndexOfCodePoint(input, c, i);
				int i1 = callLastIndexOfCodePoint(input, c, i);
				int i2 = callReverseIndexOfCodePoint(input, c, i + len);

				if (i0 == i) {
					// match
					CheckTools.check(i1 == i && i2 == i + len);
					CheckTools.check(callStartsAtCodePoint(input, c, i));
					CheckTools.check(callEndsAtCodePoint(input, c, i + len));
				} else {
					// no match
					CheckTools.check((i1 == -1 && i2 == -1) || (i0 == -1 || (i1 < i0 && i2 < i0) && i2 == i1 + len));
					CheckTools.check(!callStartsAtCodePoint(input, c, i));
					CheckTools.check(!callEndsAtCodePoint(input, c, i + len));
				}
			}

			// empty input
			input = "";
			int pos = 1;
			CheckTools.check(!callContainsCodePoint(input, cc));
			CheckTools.check(callIndexOfCodePoint(input, c, pos) == -1);
			CheckTools.check(callLastIndexOfCodePoint(input, c, pos) == -1);
			CheckTools.check(callReverseIndexOfCodePoint(input, c, pos) == -1);
			CheckTools.check(!callStartsAtCodePoint(input, c, pos));
			CheckTools.check(!callEndsAtCodePoint(input, c, pos));
		}
		{
			// string
			String input = "abXYcdXYef";
			String s = "XY";
			String ss = "01";
			int len = 2;

			CheckTools.check(!callContainsString("", s));

			CheckTools.check(callContainsString(input, s));
			CheckTools.check(!callContainsString(input, ss));

			for (int i = -2; i <= input.length() + 2; i++) {
				int i0 = callIndexOfString(input, s, i);
				int i1 = callLastIndexOfString(input, s, i);
				int i2 = callReverseIndexOfString(input, s, i + len);

				if (i0 == i) {
					// match
					CheckTools.check(i1 == i && i2 == i + len);
					CheckTools.check(callStartsAtString(input, s, i));
					CheckTools.check(callEndsAtString(input, s, i + len));
				} else {
					// no match
					CheckTools.check((i1 == -1 && i2 == -1) || (i0 == -1 || (i1 < i0 && i2 < i0) && i2 == i1 + len));
					CheckTools.check(!callStartsAtString(input, s, i));
					CheckTools.check(!callEndsAtString(input, s, i + len));
				}
			}

			// empty input
			input = "";
			int pos = 1;
			CheckTools.check(!callContainsString(input, s));
			CheckTools.check(callIndexOfString(input, s, pos) == -1);
			CheckTools.check(callLastIndexOfString(input, s, pos) == -1);
			CheckTools.check(callReverseIndexOfString(input, s, pos) == -1);
			CheckTools.check(!callStartsAtString(input, s, pos));
			CheckTools.check(!callEndsAtString(input, s, pos));
		}
		{
			// string with code point
			String input = "ab" + CodePointToolsTest.STRING_SURROGATE_0 + CodePointToolsTest.STRING_SURROGATE_0 + "cd" + CodePointToolsTest.STRING_SURROGATE_0
					+ CodePointToolsTest.STRING_SURROGATE_0 + "ef";
			String s = CodePointToolsTest.STRING_SURROGATE_0 + CodePointToolsTest.STRING_SURROGATE_0;
			String ss = CodePointToolsTest.STRING_SURROGATE_1 + CodePointToolsTest.STRING_SURROGATE_1;
			int len = 2 * 2;

			CheckTools.check(!callContainsString("", s));

			CheckTools.check(callContainsString(input, s));
			CheckTools.check(!callContainsString(input, ss));

			for (int i = -2; i <= input.length() + 2; i++) {
				int i0 = callIndexOfString(input, s, i);
				int i1 = callLastIndexOfString(input, s, i);
				int i2 = callReverseIndexOfString(input, s, i + len);

				if (i0 == i) {
					// match
					CheckTools.check(i1 == i && i2 == i + len);
					CheckTools.check(callStartsAtString(input, s, i));
					CheckTools.check(callEndsAtString(input, s, i + len));
				} else {
					// no match
					CheckTools.check((i1 == -1 && i2 == -1) || (i0 == -1 || (i1 < i0 && i2 < i0) && i2 == i1 + len));
					CheckTools.check(!callStartsAtString(input, s, i));
					CheckTools.check(!callEndsAtString(input, s, i + len));
				}
			}

			// empty input
			input = "";
			int pos = 1;
			CheckTools.check(!callContainsString(input, s));
			CheckTools.check(callIndexOfString(input, s, pos) == -1);
			CheckTools.check(callLastIndexOfString(input, s, pos) == -1);
			CheckTools.check(callReverseIndexOfString(input, s, pos) == -1);
			CheckTools.check(!callStartsAtString(input, s, pos));
			CheckTools.check(!callEndsAtString(input, s, pos));
		}
		{
			// search string too long -> no match
			String input = "abc";
			String s = "abcd";
			int pos = 0;
			CheckTools.check(!callContainsString(input, s));
			CheckTools.check(callIndexOfString(input, s, pos) == -1);
			CheckTools.check(callLastIndexOfString(input, s, pos) == -1);
			CheckTools.check(callReverseIndexOfString(input, s, pos) == -1);
			CheckTools.check(!callStartsAtString(input, s, pos));
			CheckTools.check(!callEndsAtString(input, s, pos));
		}
		{
			// search string empty -> every position matches
			String input = "abc";
			String s = "";
			int pos = 0;
			CheckTools.check(callContainsString(input, s));
			CheckTools.check(callIndexOfString(input, s, pos) == pos);
			CheckTools.check(callLastIndexOfString(input, s, pos) == pos);
			CheckTools.check(callReverseIndexOfString(input, s, pos) == pos);
			CheckTools.check(callStartsAtString(input, s, pos));
			CheckTools.check(callEndsAtString(input, s, pos));
		}
	}

	// char

	static class CharData {
		char c;
		CharPredicate cp;
		int i;
		IntPredicate ip;
		String s;

		CharData(char c) {
			this.c = c;
			this.cp = CharPredicates.of(c);
			this.i = c;
			this.ip = x -> x == i;
			this.s = String.valueOf(c);
		}
	}

	boolean callContainsChar(CharSequence input, char c) {
		CharData data = new CharData(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.contains(input, data.c),
				() -> CharSequenceTools.contains(input, data.cp),
				() -> CharSequenceTools.contains(input, data.i),
				() -> CharSequenceTools.contains(input, data.ip),
				() -> CharSequenceTools.contains(input, data.s),
				// ignore case
				() -> CharSequenceTools.contains(input, data.c, ic.getCharEqual()),
				() -> CharSequenceTools.contains(input, data.i, ic.getCodePointEqual()),
				() -> CharSequenceTools.contains(input, data.s, ic.getCharEqual()),
				() -> CharSequenceTools.contains(input, data.s, ic.getCodePointEqual()));

		return CharSequenceTools.contains(input, c);
	}

	boolean callStartsAtChar(CharSequence input, char c, int pos) {
		CharPredicate cp = CharPredicates.of(c);
		int i = c;
		IntPredicate ip = x -> x == i;
		String s = String.valueOf(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.startsAt(input, c, pos),
				() -> CharSequenceTools.startsAt(input, cp, pos),
				() -> CharSequenceTools.startsAt(input, i, pos),
				() -> CharSequenceTools.startsAt(input, ip, pos),
				() -> CharSequenceTools.startsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.startsAt(input, c, pos, ic.getCharEqual()),
				() -> CharSequenceTools.startsAt(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.startsWith(input, c),
					() -> CharSequenceTools.startsWith(input, cp),
					() -> CharSequenceTools.startsWith(input, i),
					() -> CharSequenceTools.startsWith(input, ip),
					() -> CharSequenceTools.startsWith(input, s),
					// ignore case
					() -> CharSequenceTools.startsWith(input, c, ic.getCharEqual()),
					() -> CharSequenceTools.startsWith(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.startsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.startsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.startsAt(input, c, pos);
	}

	boolean callEndsAtChar(CharSequence input, char c, int pos) {
		CharPredicate cp = CharPredicates.of(c);
		int i = c;
		IntPredicate ip = x -> x == i;
		String s = String.valueOf(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.endsAt(input, c, pos),
				() -> CharSequenceTools.endsAt(input, cp, pos),
				() -> CharSequenceTools.endsAt(input, i, pos),
				() -> CharSequenceTools.endsAt(input, ip, pos),
				() -> CharSequenceTools.endsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.endsAt(input, c, pos, ic.getCharEqual()),
				() -> CharSequenceTools.endsAt(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.endsWith(input, c),
					() -> CharSequenceTools.endsWith(input, cp),
					() -> CharSequenceTools.endsWith(input, i),
					() -> CharSequenceTools.endsWith(input, ip),
					() -> CharSequenceTools.endsWith(input, s),
					// ignore case
					() -> CharSequenceTools.endsWith(input, c, ic.getCharEqual()),
					() -> CharSequenceTools.endsWith(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.endsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.endsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.endsAt(input, c, pos);
	}

	int callIndexOfChar(CharSequence input, char c, int pos) {
		CharPredicate cp = CharPredicates.of(c);
		int i = c;
		IntPredicate ip = x -> x == i;
		String s = String.valueOf(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.indexOf(input, c, pos),
				() -> CharSequenceTools.indexOf(input, cp, pos),
				() -> CharSequenceTools.indexOf(input, i, pos),
				() -> CharSequenceTools.indexOf(input, ip, pos),
				() -> CharSequenceTools.indexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.indexOf(input, c, pos, ic.getCharEqual()),
				() -> CharSequenceTools.indexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.indexOf(input, c),
					() -> CharSequenceTools.indexOf(input, cp),
					() -> CharSequenceTools.indexOf(input, i),
					() -> CharSequenceTools.indexOf(input, ip),
					() -> CharSequenceTools.indexOf(input, s),
					// ignore case
					() -> CharSequenceTools.indexOf(input, c, ic.getCharEqual()),
					() -> CharSequenceTools.indexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.indexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.indexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.indexOf(input, c, pos);
	}

	int callLastIndexOfChar(CharSequence input, char c, int pos) {
		CharPredicate cp = CharPredicates.of(c);
		int i = c;
		IntPredicate ip = x -> x == i;
		String s = String.valueOf(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.lastIndexOf(input, c, pos),
				() -> CharSequenceTools.lastIndexOf(input, cp, pos),
				() -> CharSequenceTools.lastIndexOf(input, i, pos),
				() -> CharSequenceTools.lastIndexOf(input, ip, pos),
				() -> CharSequenceTools.lastIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.lastIndexOf(input, c, pos, ic.getCharEqual()),
				() -> CharSequenceTools.lastIndexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length() - 1) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.lastIndexOf(input, c),
					() -> CharSequenceTools.lastIndexOf(input, cp),
					() -> CharSequenceTools.lastIndexOf(input, i),
					() -> CharSequenceTools.lastIndexOf(input, ip),
					() -> CharSequenceTools.lastIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.lastIndexOf(input, c, ic.getCharEqual()),
					() -> CharSequenceTools.lastIndexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.lastIndexOf(input, c, pos);
	}

	int callReverseIndexOfChar(CharSequence input, char c, int pos) {
		CharPredicate cp = CharPredicates.of(c);
		int i = c;
		IntPredicate ip = x -> x == i;
		String s = String.valueOf(c);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.reverseIndexOf(input, c, pos),
				() -> CharSequenceTools.reverseIndexOf(input, cp, pos),
				() -> CharSequenceTools.reverseIndexOf(input, i, pos),
				() -> CharSequenceTools.reverseIndexOf(input, ip, pos),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.reverseIndexOf(input, c, pos, ic.getCharEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.reverseIndexOf(input, c),
					() -> CharSequenceTools.reverseIndexOf(input, cp),
					() -> CharSequenceTools.reverseIndexOf(input, i),
					() -> CharSequenceTools.reverseIndexOf(input, ip),
					() -> CharSequenceTools.reverseIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.reverseIndexOf(input, c, ic.getCharEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.reverseIndexOf(input, c, pos);
	}

	// code point

	boolean callContainsCodePoint(CharSequence input, int i) {
		IntPredicate ip = CodePointPredicates.of(i);
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.contains(input, i),
				() -> CharSequenceTools.contains(input, ip),
				() -> CharSequenceTools.contains(input, s),
				// ignore case
				() -> CharSequenceTools.contains(input, i, ic.getCodePointEqual()),
				() -> CharSequenceTools.contains(input, s, ic.getCharEqual()),
				() -> CharSequenceTools.contains(input, s, ic.getCodePointEqual()));

		return CharSequenceTools.contains(input, i);
	}

	boolean callStartsAtCodePoint(CharSequence input, int i, int pos) {
		IntPredicate ip = x -> x == i;
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.startsAt(input, i, pos),
				() -> CharSequenceTools.startsAt(input, ip, pos),
				() -> CharSequenceTools.startsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.startsAt(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.startsWith(input, i),
					() -> CharSequenceTools.startsWith(input, ip),
					() -> CharSequenceTools.startsWith(input, s),
					// ignore case
					() -> CharSequenceTools.startsWith(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.startsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.startsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.startsAt(input, i, pos);
	}

	boolean callEndsAtCodePoint(CharSequence input, int i, int pos) {
		IntPredicate ip = x -> x == i;
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.endsAt(input, i, pos),
				() -> CharSequenceTools.endsAt(input, ip, pos),
				() -> CharSequenceTools.endsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.endsAt(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.endsWith(input, i),
					() -> CharSequenceTools.endsWith(input, ip),
					() -> CharSequenceTools.endsWith(input, s),
					// ignore case
					() -> CharSequenceTools.endsWith(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.endsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.endsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.endsAt(input, i, pos);
	}

	int callIndexOfCodePoint(CharSequence input, int i, int pos) {
		IntPredicate ip = CodePointPredicates.of(i);
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.indexOf(input, i, pos),
				() -> CharSequenceTools.indexOf(input, ip, pos),
				() -> CharSequenceTools.indexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.indexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.indexOf(input, i),
					() -> CharSequenceTools.indexOf(input, ip),
					() -> CharSequenceTools.indexOf(input, s),
					// ignore case
					() -> CharSequenceTools.indexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.indexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.indexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.indexOf(input, i, pos);
	}

	int callLastIndexOfCodePoint(CharSequence input, int i, int pos) {
		IntPredicate ip = CodePointPredicates.of(i);
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.lastIndexOf(input, i, pos),
				() -> CharSequenceTools.lastIndexOf(input, ip, pos),
				() -> CharSequenceTools.lastIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.lastIndexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length() - 1) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.lastIndexOf(input, i),
					() -> CharSequenceTools.lastIndexOf(input, ip),
					() -> CharSequenceTools.lastIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.lastIndexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.lastIndexOf(input, i, pos);
	}

	int callReverseIndexOfCodePoint(CharSequence input, int i, int pos) {
		IntPredicate ip = CodePointPredicates.of(i);
		String s = CodePointTools.codePointToString(i);

		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.reverseIndexOf(input, i, pos),
				() -> CharSequenceTools.reverseIndexOf(input, ip, pos),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.reverseIndexOf(input, i, pos, ic.getCodePointEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.reverseIndexOf(input, i),
					() -> CharSequenceTools.reverseIndexOf(input, ip),
					() -> CharSequenceTools.reverseIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.reverseIndexOf(input, i, ic.getCodePointEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.reverseIndexOf(input, i, pos);
	}

	// string

	boolean callContainsString(CharSequence input, String s) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.contains(input, s),
				// ignore case
				() -> CharSequenceTools.contains(input, s, ic.getCharEqual()),
				() -> CharSequenceTools.contains(input, s, ic.getCodePointEqual()));

		return CharSequenceTools.contains(input, s);
	}

	boolean callStartsAtString(CharSequence input, String s, int pos) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.startsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.startsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.startsWith(input, s),
					// ignore case
					() -> CharSequenceTools.startsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.startsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.startsAt(input, s, pos);
	}

	boolean callEndsAtString(CharSequence input, String s, int pos) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.endsAt(input, s, pos),
				// ignore case
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.endsAt(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.endsWith(input, s),
					// ignore case
					() -> CharSequenceTools.endsWith(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.endsWith(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.endsAt(input, s, pos);
	}

	int callIndexOfString(CharSequence input, String s, int pos) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.indexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.indexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == 0) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.indexOf(input, s),
					// ignore case
					() -> CharSequenceTools.indexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.indexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.indexOf(input, s, pos);
	}

	int callLastIndexOfString(CharSequence input, String s, int pos) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.lastIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.lastIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length() - 1) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.lastIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.lastIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.lastIndexOf(input, s, pos);
	}

	int callReverseIndexOfString(CharSequence input, String s, int pos) {
		CharMode ic = CharMode.getCharMode(true);

		new CompareExecutor().checkEqual(
				() -> CharSequenceTools.reverseIndexOf(input, s, pos),
				// ignore case
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCharEqual()),
				() -> CharSequenceTools.reverseIndexOf(input, s, pos, ic.getCodePointEqual()));

		if (pos == s.length()) {
			new CompareExecutor().checkEqual(
					() -> CharSequenceTools.reverseIndexOf(input, s),
					// ignore case
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCharEqual()),
					() -> CharSequenceTools.reverseIndexOf(input, s, ic.getCodePointEqual()));
		}

		return CharSequenceTools.reverseIndexOf(input, s, pos);
	}

	//

	@Trace
	public void testIndexOf() {
		// found
		doTestIndexOf("abcd", "ab");
		doTestIndexOf("abcd", "bc");
		doTestIndexOf("abcd", "cd");
		doTestIndexOf("abcabc", "ab", 1);

		// not found
		doTestIndexOf("abcd", "abcde");
		doTestIndexOf("abc", "abc", 1);
		doTestIndexOf("abcabc", "xy", 9);
		doTestIndexOf("abcabc", "", 9);
	}

	@InheritTrace
	void doTestIndexOf(String str, String find) {
		int i0 = CharSequenceTools.indexOf(str, find);
		int i1 = str.indexOf(find);
		ObjectDiff.checkEqual(i0, i1);
	}

	@InheritTrace
	void doTestIndexOf(String str, String find, int start) {
		int i0 = CharSequenceTools.indexOf(str, find, start);
		int i1 = CharSequenceTools.doIndexOfString(str, find, start);
		ObjectDiff.checkEqual(i0, i1);
	}

	//

	@Trace
	public void testContains() {
		// found
		CharSequenceTools.contains("abcd", "ab");
		CharSequenceTools.contains("abcd", "bc");
		CharSequenceTools.contains("abcd", "cd");

		// not found
		CharSequenceTools.contains("abcd", "abcde");
	}

	@Trace
	public void testStartsAt() {
		// found
		CharSequenceTools.startsAt("abcd", "ab", 0);
		CharSequenceTools.startsAt("abcd", "bc", 1);
		CharSequenceTools.startsAt("abcd", "cd", 2);

		// not found
		CharSequenceTools.startsAt("abcd", "abcde", 0);
		CharSequenceTools.startsAt("abcd", "ab", 1);
	}

	@Trace(traceMethod = "/.*/")
	public void testIndexOfReverse() {
		// String
		CharSequenceTools.lastIndexOf("aXaX", "aX", 9);
		CharSequenceTools.reverseIndexOf("aXaX", "aX", 9);
		CharSequenceTools.lastIndexOf("aXaX", "aX", 3);
		CharSequenceTools.reverseIndexOf("aXaX", "aX", 3);

		// char
		CharSequenceTools.lastIndexOf("aXaX", 'X', 3);
		CharSequenceTools.reverseIndexOf("aXaX", 'X', 3);
	}

	@Trace(traceMethod = "/.*/")
	public void testReplace() {
		String input = "a0b0c0d";

		// No match, replace none, return input string
		CharSequence result = doReplace(input, 0, '1', '0', -1, false);
		CheckTools.check(result == input);
		// No match, replace none, return null
		result = doReplace(input, 0, '1', '0', -1, true);
		CheckTools.check(result == null);

		// Replace all
		doReplace(input, 0, '0', '1', -1, false);
		// Replace first 3 (i.e. all) matches
		doReplace(input, 0, '0', '1', 3, true);

		// Replace 0 matches
		doReplace(input, 0, '0', '1', 0, false);
		// Replace 1 match
		doReplace(input, 0, '0', '1', 1, false);

		// Replace first 2 matches
		doReplace(input, 0, '0', '1', 2, false);
		// Replace exactly 2 matches, else return null
		doReplace(input, 0, '0', '1', 2, true);
	}

	@InheritTrace
	CharSequence doReplace(CharSequence str, int start, char findChar, char replaceChar, int numReplace, boolean checkReplace) {
		// char
		CharSequence res = CharSequenceTools.replaceCharChecked(str, findChar, replaceChar, CharEqual.isEqualChar(), start, numReplace, checkReplace);

		Report.setAutoTrace(false);

		// code point
		int findCodePoint = findChar;
		int replaceCodePoint = replaceChar;
		CharSequence res2 = CharSequenceTools.replaceCodePointChecked(str, findCodePoint, replaceCodePoint, CodePointEqual.isEqualCodePoint(), start,
				numReplace,
				checkReplace);
		CheckTools.check((res == null && res2 == null) || CharSequenceTools.equals(res2, res));

		// any char FIXME
		//		String findAnyChar = "" + findChar;
		//		String replaceAnyChar = "" + replaceChar;
		//		CharSequence res3 = CharSequenceTools.replaceAnyChar(str, start, findAnyChar, replaceAnyChar, CharIndexEqual.indexOf(), numReplace, checkReplace);
		//		CheckTools.check((res == null && res3 == null) || CharSequenceTools.equals(res3, res));

		// string
		String findString = "" + findChar;
		String replaceString = "" + replaceChar;
		CharSequence res4 = CharSequenceTools.replaceStringChecked(str, findString, replaceString, CharEqual.isEqualChar(), start, numReplace, checkReplace);
		CheckTools.check((res == null && res4 == null) || CharSequenceTools.equals(res4, res));

		// char predicate
		CharPredicate findPredicate = CharPredicates.of(findChar);
		CharOperator replacer = c -> replaceChar;
		CharSequence res5 = CharSequenceTools.replaceCharPredicateChecked(str, findPredicate, replacer, start, numReplace, checkReplace);
		CheckTools.check((res == null && res5 == null) || CharSequenceTools.equals(res5, res));

		Report.setAutoTrace(true);
		return res;
	}

}
