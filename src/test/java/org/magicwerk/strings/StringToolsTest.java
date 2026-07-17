package org.magicwerk.strings;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.collections.Sources.ResetSource;
import org.magicwerk.brownies.core.strings.escape.CsvEscaper;
import org.magicwerk.brownies.core.strings.escape.StringBuilders.FixedStringBuilder;
import org.magicwerk.brownies.core.time.Timer;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmark;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.format.StringFormat;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;
import org.magicwerk.brownies.tools.dev.jvm.JmhTool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringTools}.
 */
public class StringToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringToolsTest().run();
	}

	void run() {
		testManual();

		//testNormalizeEOL();
		//testLineIterator();
		//testUnesacpeJava();
		//testGetDisplayName();
		//testJoinFunctionality();
		//testSplitFunctionality();
		//testPerformancePad();
		//testSubstringBefore();
		//testSubstringAfter();

		//new StringToolsConcatJmhTest().test();
		//new CommonPrefixJmhTest().test();
		//ReturnIndexOrMatchJmhTest.test();		
		//ReturnLengthOrStringJmhTest.test();
		//StringSubstitutorTestPerformanceJmh.test();
		//StringEscapeUtilsTestPerformanceJmh.test();
		//StringBuilderTestPerformanceJmh.test();
		//ContainsAnyTestJmh.test();
		//ReplaceCharsTestJmh.test();
		//StringTestPerformanceJmh.test();
		//SplitTestPerformanceJmh.test();
	}

	void testManual() {
		StringTools.replaceChars("ab", "./[();", "_$$$$");
	}

	/**
	 * Show that the allocation of a Match which is just needed for transporting the end position cannot be optimized away.
	 */
	public static class ReturnIndexOrMatchJmhTest {
		static void test() {
			Options opts = new Options().includeClass(ReturnIndexOrMatchJmhTest.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "b";
			CyclicSource<String> source = new CyclicSource<>("a", "ab", "abc");
		}

		@Benchmark
		public int testFindEnd(MyState state) {
			return findEnd(state.source.next(), state.FIND);
		}

		@Benchmark
		public int testFindMatch(MyState state) {
			IMatch match = findMatch(state.source.next(), state.FIND);
			return (match != null) ? match.getEnd() : -1;
		}

		static int findEnd(String str, String search) {
			int index = str.indexOf(search);
			return (index != -1) ? index + search.length() : -1;
		}

		static IMatch findMatch(String str, String search) {
			int index = str.indexOf(search);
			return (index != -1) ? new Match(str, index, index + search.length()) : null;
		}
	}

	/**
	 * Show that the allocation of a String which is just needed for transporting the length cannot be optimized away.
	 */
	public static class ReturnLengthOrStringJmhTest {
		static void test() {
			Options opts = new Options().includeClass(ReturnLengthOrStringJmhTest.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char START = '<';
			char END = '>';
			CyclicSource<String> source = new CyclicSource<>("a<b>c", "a<bc>");
		}

		@Benchmark
		public int testFindLength(MyState state) {
			return findLength(state.source.next(), state.START, state.END);
		}

		@Benchmark
		public int testFindMatch(MyState state) {
			String str = findMatch(state.source.next(), state.START, state.END);
			return (str != null) ? str.length() : 0;
		}

		static int findLength(String str, char searchStart, char searchEnd) {
			int start = str.indexOf(searchStart);
			int end = str.indexOf(searchEnd, start + 1);
			return end - start;
		}

		static String findMatch(String str, char searchStart, char searchEnd) {
			int start = str.indexOf(searchStart);
			int end = str.indexOf(searchEnd, start + 1);
			return str.substring(start, end);
		}
	}

	/**
	 * StringTools.concat() uses more memory and is about 33% slower than using the + operator / StringBuilder.
	 * Also the inline implementation without varargs uses more memory and is somewhat slower.
	 * The benchmarks also reports testStringBuilderAppend() and testStringToolsConcatInline() to consume the same amount of memory
	 * which is unlikely as the the StringBuilder size is presized correctly in one case and multiple allocations are needed in the other.
	 * testStringToolsConcat() on the other hand use much more memory which is also strange as the varargs invocation seems to be
	 * optimized away according to JmhAllocationJfrObserverState.
	 */
	public static class StringToolsConcatJmhTest extends JmhBenchmark {

		public StringToolsConcatJmhTest() {
			setJavaVersions(JavaVersion.JAVA_21);
			//setRunVerify(false); // if JmhAllocationJfrObserverState is used
			//setJvmArgs(JavaTool.JvmUseGcEpsilon("8g"));
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> source = new CyclicSource<>(
					StringTools.repeat("a", 10), StringTools.repeat("b", 20), StringTools.repeat("c", 30),
					StringTools.repeat("a", 100), StringTools.repeat("b", 200), StringTools.repeat("c", 300),
					StringTools.repeat("a", 1000), StringTools.repeat("b", 2000), StringTools.repeat("c", 3000));
		}

		@Benchmark
		//public String testStringToolsConcat(MyState state, JmhAllocationJfrObserverState x) {
		//public String testStringToolsConcat(MyState state, JmhProgressState x) {
		//x.invocation();
		public String testStringToolsConcat(MyState state) {

			String s0 = state.source.next();
			String s1 = state.source.next();
			String s2 = state.source.next();

			String s = StringTools.concat(s0, s1, s2);
			return s;
		}

		@Benchmark
		//public String testStringToolsConcat(MyState state, JmhAllocationJfrObserverState x) {
		//public String testStringToolsConcat(MyState state, JmhProgressState x) {
		//x.invocation();
		public String testStringToolsConcat3(MyState state) {

			String s0 = state.source.next();
			String s1 = state.source.next();
			String s2 = state.source.next();

			String s = StringTools.concat(s0, s1, s2);
			return s;
		}

		@Benchmark
		//public String testStringToolsConcatInline(MyState state, JmhAllocationJfrObserverState x) {
		//public String testStringToolsConcatInline(MyState state, JmhProgressState x) {
		//x.invocation();
		public String testStringToolsConcatInline(MyState state) {

			String s0 = state.source.next();
			String s1 = state.source.next();
			String s2 = state.source.next();

			//String s = concatInline(s0, s1, s2);
			int len = s0.length() + s1.length() + s2.length();
			StringBuilder buf = new StringBuilder(len).append(s0).append(s1).append(s2);
			String s = buf.toString();
			return s;
		}

		@Benchmark
		//public String testStringBuilderAppend(MyState state, JmhProgressState x) {
		//x.invocation();
		public String testStringBuilderAppend(MyState state) {

			String s0 = state.source.next();
			String s1 = state.source.next();
			String s2 = state.source.next();

			StringBuilder buf = new StringBuilder().append(s0).append(s1).append(s2);
			String s = buf.toString();
			return s;
		}

		//@Benchmark
		public String testStringOperatorPlus(MyState state) {
			String s0 = state.source.next();
			String s1 = state.source.next();
			String s2 = state.source.next();

			String s = s0 + s1 + s2;
			return s;
		}

		static String concatInline(String s0, String s1, String s2) {
			int len = 0;
			len += s0.length();
			len += s1.length();
			len += s2.length();

			StringBuilder buf = new StringBuilder(len).append(s0).append(s1).append(s2);
			String s = buf.toString();
			return s;
		}
	}

	public static class StringJmhTest {

		//		String:
		//			- isLatin1 (since 11)
		//			- repeat (since 11)
		//			- replace (fast since 11, 8 used regex)

		static void test() {
			Options opts = new Options().includeClass(StringJmhTest.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands8, BrowniesJavaEnv.JdkCommands11));
			opts.setUseGcProfiler(true);
			//opts.setJvmArgs(JavaTool.JvmUseEpsilonGc);
			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char FIND = '-';
			CyclicSource<String> source = new CyclicSource<>("a", "a-", "abc-");
		}

		@Benchmark
		public Object testIndexOf(MyState state) {
			return state.source.next().indexOf(state.FIND);
		}

		@Benchmark
		public int testIndexOf1(MyState state) {
			return indexOf1(state.source.next(), state.FIND);
		}

		@Benchmark
		public int testIndexOf2(MyState state) {
			return indexOf2(state.source.next(), state.FIND);
		}

		static int indexOf1(String str, char c) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == c) {
					return i;
				}
			}
			return -1;
		}

		static int indexOf2(String str, char c) {
			char[] cs = str.toCharArray();
			for (int i = 0; i < cs.length; i++) {
				if (cs[i] == c) {
					return i;
				}
			}
			return -1;
		}
	}

	/** Evaluate performance of different ways to split a string */
	public static class StringBuilderTestPerformanceJmh {

		static void test() {
			Options opts = new Options().includeClass(StringBuilderTestPerformanceJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class TestState {
			ResetSource<StringBuilder> builder = new ResetSource<StringBuilder>(new StringBuilder(1000), sb -> sb.setLength(0), 10);
			CyclicSource<String> add = new CyclicSource<>("01234", "56789");
		}

		@Benchmark
		public Object testSubstring(TestState state) {
			StringBuilder sb = state.builder.next();
			return sb.append(state.add.next().substring(1, 3));
		}

		@Benchmark
		public Object testAppend(TestState state) {
			StringBuilder sb = state.builder.next();
			return sb.append(state.add.next(), 1, 3);
		}
	}

	/** Evaluate performance of StringSubstitutor */
	public static class StringSubstitutorTestPerformanceJmh {

		static void test() {
			Options opts = new Options().includeClass(StringSubstitutorTestPerformanceJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			//runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class TestState {
			Map<String, Object> valuesMap = new HashMap<>();
			{
				valuesMap.put("animal", "quick brown fox");
				valuesMap.put("target", "lazy dog");
			}
			//String templateString = "The ${animal} jumped over the ${target} ${undefined.number:-1234567890} times.";
			String templateString = "The ${animal} jumped over the ${target}";

			StringSubstitutor sub = new StringSubstitutor(valuesMap);
			StringFormat sf2 = new StringFormat(templateString);
		}

		@Benchmark
		public String testStringSubstitutor(TestState state) {
			return state.sub.replace(state.templateString);
		}

		@Benchmark
		public String testStringFormat(TestState state) {
			return state.sf2.formatMap(state.valuesMap);
		}

	}

	/** Evaluate performance of StringEscapeUtils */
	public static class StringEscapeUtilsTestPerformanceJmh {

		// StringEscapeUtils needs classes in org.apache.commons.text.translate for implementation

		static void test() {
			Options opts = new Options().includeClass(StringEscapeUtilsTestPerformanceJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			//runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class TestState {
			CsvEscaper esc = new CsvEscaper();
			//CyclicSource<String> add = new CyclicSource<>("test with special character \" and & ");
			CyclicSource<String> add = new CyclicSource<>("test with no special characters");
		}

		//@Benchmark
		public Object testEscapeXml(TestState state) {
			return StringEscapeUtils.escapeXml10(state.add.next());
		}

		@Benchmark
		public Object testCvsEscaperEncodeRaw(TestState state) {
			return state.esc.encodeRaw(state.add.next());
		}

		@Benchmark
		public Object testCvsEscaperEncode(TestState state) {
			return state.esc.encode(state.add.next());
		}

		@Benchmark
		public Object testStringEscapeUtilsEscapeCsv(TestState state) {
			return StringEscapeUtils.escapeCsv(state.add.next());
		}
	}

	/** Evaluate performance of different ways to split a string */
	public static class SplitTestPerformanceJmh {

		static void test() {
			Options opts = new Options().includeClass(SplitTestPerformanceJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands11));
			opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class TestState {
			String str = "a,b,c";
			String split = "|";
			Pattern splitRx = Pattern.compile("\\|");
		}

		@Benchmark
		public Object testSplit(TestState state) {
			return state.str.split(state.split);
		}

		@Benchmark
		public Object testPattern(TestState state) {
			return state.splitRx.split(state.str);
		}

		@Benchmark
		public Object testStringUtils(TestState state) {
			return StringUtils.split(state.str, state.split);
		}
	}

	public static class ContainsAnyTestJmh {

		static void test() {
			Options opts = new Options().includeClass(ContainsAnyTestJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands17));

			//opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 1);

			// Analysis
			//opts.setRunTimeMillis(100);
			//opts.setJvmArgs(JavaTool.JvmUseGcEpsilon("6g"));

			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class TestState {
			String str = "abc-def";
			String search = "+-";

			StringFinder finder = StringFinder.builder().setFindAnyChar("+-").build();
		}

		@Benchmark
		public boolean testCommonsUtil(TestState state) {
			return StringUtils.containsAny(state.str, state.search);
		}

		@Benchmark
		public boolean testContainsAny(TestState state) {
			return containsAny(state.str, state.search);
		}

		//@Benchmark
		public boolean testStringFinder(TestState state) {
			return StringFinder.builder().setFindAnyChar(state.search).build().contains(state.str);
		}

		//@Benchmark
		public boolean testStringFinder2(TestState state) {
			return state.finder.contains(state.str);
		}

		public static boolean containsAny(String str, String searchChars) {
			if (StringTools.isEmpty(str) || StringTools.isEmpty(searchChars)) {
				return false;
			}

			int strLen = str.length();
			for (int i = 0; i < strLen; i++) {
				char c = str.charAt(i);
				int pos = searchChars.indexOf(c);
				if (pos >= 0) {
					return true;
				}
			}
			return false;
		}

		public static String replaceChars(String str, String searchChars, String replaceChars) {
			if (StringTools.isEmpty(str) || StringTools.isEmpty(searchChars)) {
				return str;
			}
			if (replaceChars == null) {
				replaceChars = "";
			}

			int replaceCharsLen = replaceChars.length();
			int strLen = str.length();
			FixedStringBuilder buf = null;
			for (int i = 0; i < strLen; i++) {
				char c = str.charAt(i);
				int pos = searchChars.indexOf(c);
				if (pos >= 0) {
					if (buf == null) {
						buf = new FixedStringBuilder(strLen);
						if (i > 0) {
							buf.append(str, 0, i);
						}
					}
					if (pos < replaceCharsLen) {
						buf.append(replaceChars.charAt(pos));
					}
				} else {
					if (buf != null) {
						buf.append(c);
					}
				}
			}

			if (buf != null) {
				return buf.toString();
			}
			return str;
		}
	}

	public static class ReplaceCharsTestJmh {

		static void test() {
			Options opts = new Options().includeClass(ReplaceCharsTestJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands17));

			//opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 1);

			// Analysis
			//opts.setRunTimeMillis(100);
			//opts.setJvmArgs(JavaTool.JvmUseGcEpsilon("6g"));

			runner.runJmh(opts);
		}

		static class Replace {
			String str = ",,,,,,,,,,";
			String search = ",";
			String replace = ";";

			Replace(String str, String search, String replace) {
				this.str = str;
				this.search = search;
				this.replace = replace;
			}
		}

		static Replace ReplaceAll = new Replace(",,,,,,,,,,", ",", ";");
		static Replace ReplaceNormal = new Replace("012,456,89", ",", ";");
		static Replace ReplaceLate = new Replace("012345678,", ",", ";");
		static Replace ReplaceNone = new Replace("0123456789", ";", ";");

		@State(Scope.Benchmark)
		public static class TestState {
			IList<Replace> replaces = GapList.create(ReplaceNormal, ReplaceLate, ReplaceNone, ReplaceAll);

			//@Param({ "0", "1", "2", "3" })
			@Param({ "3" })
			public int param;

			Replace get() {
				return replaces.get(param);
			}
		}

		@Benchmark
		//public String testOld(TestState state, JmhAllocationObserverState j) {
		public String testCommonsUtil(TestState state) {
			Replace replace = state.get();
			return replaceCharsCommonsUtil(replace.str, replace.search, replace.replace);
		}

		@Benchmark
		//public String testNew(TestState state, JmhAllocationObserverState j) {
		public String testStringTools(TestState state) {
			Replace replace = state.get();
			return StringTools.replaceChars(replace.str, replace.search, replace.replace);
		}

		public static String replaceCharsCommonsUtil(final String str, final String searchChars, String replaceChars) {
			if (StringUtils.isEmpty(str) || StringUtils.isEmpty(searchChars)) {
				return str;
			}
			if (replaceChars == null) {
				replaceChars = StringUtils.EMPTY;
			}
			boolean modified = false;
			final int replaceCharsLength = replaceChars.length();
			final int strLength = str.length();
			final StringBuilder buf = new StringBuilder(strLength);
			for (int i = 0; i < strLength; i++) {
				final char ch = str.charAt(i);
				final int index = searchChars.indexOf(ch);
				if (index >= 0) {
					modified = true;
					if (index < replaceCharsLength) {
						buf.append(replaceChars.charAt(index));
					}
				} else {
					buf.append(ch);
				}
			}
			if (modified) {
				return buf.toString();
			}
			return str;
		}

	}

	static void testReplaceAll() {
		System.out.println("(" + "a b c".replaceAll("\\s+", " ") + ")");
		System.out.println("(" + "a  b  c".replaceAll("\\s+", " ") + ")");
		System.out.println("(" + " a b c ".replaceAll("\\s+", " ") + ")");
		System.out.println("(" + " a\nb c ".replaceAll("\\s+", " ") + ")");
	}

	//	US-ASCII  	Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
	//	ISO-8859-1   	ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
	//	UTF-8 	Eight-bit UCS Transformation Format
	//	UTF-16BE 	Sixteen-bit UCS Transformation Format, big-endian byte order
	//	UTF-16LE 	Sixteen-bit UCS Transformation Format, little-endian byte order
	//	UTF-16 	Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
	// cp850, cp1252i
	//from = System.getProperty("file.encoding");
	public static void showCharsets() {
		SortedMap<String, Charset> charsets = Charset.availableCharsets();
		for (Charset charset : charsets.values()) {
			System.out.println(charset.name());
			for (String alias : charset.aliases()) {
				System.out.println("   " + alias);
			}
		}
	}

	@Trace
	public void testGetHead() {
		StringTools.getHead("01ab", c -> Character.isDigit(c));
	}

	static void testPerformanceSplit() {
		String str = "123,456,789";
		char sep = ',';
		String sepStr = "" + sep;
		int count = 1000 * 1000;
		Timer t;

		t = new Timer();
		for (int i = 0; i < count; i++) {
			StringUtils.split(str, sep);
		}
		t.printElapsed("StringUtils.split");

		t = new Timer();
		for (int i = 0; i < count; i++) {
			StringTools.split(str, sep);
		}
		t.printElapsed("StringTools.split");

		t = new Timer();
		for (int i = 0; i < count; i++) {
			str.split(sepStr);
		}
		t.printElapsed("String.split");
	}

	@Trace
	public static void testJoin() {
		StringTools.join(".", "a");
		StringTools.join(".", "a", "b");
		StringTools.join(".", "a", "b", "c");
		StringTools.join(".", "a", null);
		StringTools.join(".", null, "b");
		StringTools.join(".", null, null);
		StringTools.join(".", "a", null, "c");
	}

	@Trace
	public static void testRemove() {
		StringTools.remove("ab-cd-ef", "-");
		StringTools.remove("--ab--cd--", "-");
		StringTools.remove("abcd", "-");
	}

	@Trace
	public static void testRemoveChar() {
		StringTools.removeChar("abc", c -> Character.isDigit(c));
		StringTools.removeChar("a0b1c", c -> Character.isDigit(c));
	}

	@Trace
	public void testSubstringBefore() {
		String input = "ab-de-fg";
		IList<String> strs = GapList.create("-", "a", "g", "X");
		for (String str : strs) {
			String s = StringTools.substringBefore(input, str);
			String s2 = StringUtils.substringBefore(input, str);
			CheckTools.check(ObjectTools.equals(s, s2));
		}
	}

	@Trace
	public void testSubstringAfter() {
		String input = "ab-de-fg";
		IList<String> strs = GapList.create("-", "a", "g", "X");
		for (String str : strs) {
			String s = StringTools.substringAfter(input, str);
			String s2 = StringUtils.substringAfter(input, str);
			CheckTools.check(ObjectTools.equals(s, s2));
		}
	}

	@Trace
	public static void testLeft() {
		StringTools.left(null, 2);
		StringTools.left("abc", 2);
		StringTools.left("abc", 4);
		StringTools.left("abc", 0);
		StringTools.left("abc", -1);
		StringTools.left("abc", -2);
		StringTools.left("abc", -4);
	}

	@Trace
	public static void testRight() {
		StringTools.right(null, 2);
		StringTools.right("abc", 2);
		StringTools.right("abc", 4);
		StringTools.right("abc", 0);
		StringTools.right("abc", -1);
		StringTools.right("abc", -2);
		StringTools.right("abc", -4);
	}

	@Trace
	public static void testRemoveLeft() {
		StringTools.removeLeft(null, 2);
		StringTools.removeLeft("abc", 0);
		StringTools.removeLeft("abc", 2);
		StringTools.removeLeft("abc", 4);
		StringTools.removeLeft("abc", -1);
	}

	@Trace
	public static void testRemoveRight() {
		StringTools.removeRight(null, 2);
		StringTools.removeRight("abc", 0);
		StringTools.removeRight("abc", 2);
		StringTools.removeRight("abc", 4);
		StringTools.removeRight("abc", -1);
	}

	@Trace
	public static void testMid() {
		StringTools.mid(null, 2, 2);
		StringTools.mid("abcde", 1, 3);
		StringTools.mid("abcde", 1, 5);
		StringTools.mid("abcde", 2, -1);
		StringTools.mid("abcde", 2, -3);
		StringTools.mid("abcde", -2, 4);
		StringTools.mid("abcde", 7, -4);
	}

	@Trace
	public static void testSplit() {
		StringTools.split("123456", ',');
		StringTools.split("12,34,56", ',');
		StringTools.split("12,34,,56", ',');
		StringTools.split(",12,34,56,", ',');

		StringTools.split("123456", "<>");
		StringTools.split("12<>34<>56", "<>");
		StringTools.split("12<>34<><>56", "<>");
		StringTools.split("<>12<>34<>56<>", "<>");
	}

}
