package org.magicwerk.strings;

import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.core.bytes.BitSet64Tools;
import org.magicwerk.brownies.core.cache.ThreadCache;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.diff.ListDiff;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringPadder;
import org.magicwerk.strings.StringRepeater;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringFinder.FindCharImpl;
import org.magicwerk.strings.StringFinder.FindStringImpl;
import org.magicwerk.strings.StringPadder.PadMode;
import org.magicwerk.strings.StringReplacerAppender.ConstStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.IStringReplaceAppender;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.FuncTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.AnyCharMatcher;
import org.magicwerk.strings.matcher.CharPredicateMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.RepeatedStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.magicwerk.brownies.tools.dev.jvm.JmhTool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.CharMatcher;

/**
 * Test of class {@link StringFinder}.
 */
public class StringFinderTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringFinderTest().run();
	}

	void run() {
		//runManual();

		//testContains();
		//testCount();
		//testEmpty();
		//testFindReverseJdk();
		testIndexOf();
		//testIndexOfPattern();
		//testIndexOfRepeat();
		//testIndexOfOccurrence();
		//testOverlap();
		//testXStringFinder();

		//testBenchmarks();
		//runBenchmarks();
		//runBenchmark(StringFinderCollapseTestCharSequenceChange.class);
		//runBenchmark(StringFinderCollapseTestStringChange.class);

		//new StringFinderCharPredicateTestJmh().test();
		//new StringFinderTestJmh().test();
		//new StringFinderTest2Jmh().test();
		//new FindCharSequenceTestJmh().test();
		//new ContainsCharSequenceTestJmh().test();
		//new FindStringTestJmh().test();
		//StringFindMultipleJmhTest.test();
		//StringFindJmhTest.test();
		//PerformanceBuilderJmhTest.test();
		//PerformanceCreateJmhTest.test();
		//testPerformanceIndexOf();
		//IndexOfCharSequenceJmhTest.test();
		//StringContainsJmhTest.test();
		//XStringFinderStringsJmhTest.test();
		//XStringFinderReturnEndJmhTest.test();
		//new XStringFinder1JmhTest().test();
		//new XStringFinder2JmhTest().test();
		//EscapeAnalysisStringJmhTest.test();
		//RegexPerformanceJmhTest.test();
		//IndexOfCharSequenceJmhTest.test();
	}

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringFinderAnyCharBenchmarkTest.class);
	}

	public static class StringFinderAnyCharBenchmarkTest {

		String anyChar = "ab";

		StringFinder stringFinder = StringFinder.builder().setFindAnyChar(anyChar).build();

		@TestData
		IList<String> inputs = GapList.create("00a11", "00b11");

		@TestMethod
		public int testStringFinder(String str) {
			return stringFinder.indexOf(str);
		}

		@TestMethod
		public int testCommonsStringUtils(String str) {
			return StringUtils.indexOfAny(str, anyChar);
		}
	}

	public static class StringFinderCollapseTest {

		// Test setup

		String removeChars = "xyz";
		char replaceChar = '0';

		AnyCharMatcher am = new AnyCharMatcher(removeChars);
		RepeatedStringMatcher sm = RepeatedStringMatcher.of(am, false);
		IStringReplaceAppender sra = new ConstStringReplaceAppender(replaceChar);
		StringReplacer sr = StringReplacer.builder().replace(sm, sra).build();
		CharMatcher cm = CharMatcher.anyOf(removeChars);

		// Test methods

		@TestMethod
		public CharSequence testStringReplacer(CharSequence str) {
			return sr.replace(str);
		}

		@TestMethod
		public CharSequence testGuava(CharSequence str) {
			return cm.collapseFrom(str, replaceChar);
		}

		@TestMethod
		public CharSequence testGuavaInline(CharSequence str) {
			CharMatcher cm = CharMatcher.anyOf(removeChars);
			return cm.collapseFrom(str, replaceChar);
		}
	}

	/** Performance: Guava= 100%, Guava-inline= 90%, StringReplaer=33% */
	public static class StringFinderCollapseTestStringChange extends StringFinderCollapseTest {
		@TestData
		IList<String> inputs = GapList.create("xyabcyz", "xyaxcyz");
	}

	public static class StringFinderCollapseTestStringNoChange extends StringFinderCollapseTest {
		@TestData
		IList<String> inputs = GapList.create("abc", "def");
	}

	public static class StringFinderCollapseTestCharSequenceChange extends StringFinderCollapseTest {
		@TestData
		IList<CharSequence> inputs = GapList.create(new GapString("xyabcyz"), new GapString("xyaxcyz"));
	}

	public static class StringFinderCollapseTestCharSequenceNoChange extends StringFinderCollapseTest {
		@TestData
		IList<CharSequence> inputs = GapList.create(new GapString("abd"), new GapString("def"));
	}

	//

	@Trace(traceMethod = "matches")
	public void testEmpty() {
		StringFinder sf = StringFinder.builder().setFindString("").build();
		sf.matches("abc");
		sf = StringFinder.builder().setFindString("").setReverse(true).build();
		sf.matches("abc");
	}

	void testXStringFinder() {
		StringFinder sf = StringFinder.builder().setFindString("x").build();
		LOG.info("{}", sf);
		FindCharImpl sf2 = StringFinder.builder().setFindString("x").build(FindCharImpl.class);
		LOG.info("{}", sf2);
		FindStringImpl sf3 = StringFinder.builder().setFindString("x").build(FindStringImpl.class);
		LOG.info("{}", sf3);
	}

	static class TableMatcher implements CharPredicate {
		BitSet table = new BitSet();

		TableMatcher(char... cs) {
			for (char c : cs) {
				table.set(c);
			}
		}

		void add(char c) {
			table.set(c);
		}

		@Override
		public boolean test(char c) {
			return table.get(c);
		}
	}

	static class TableMatcher2 implements CharPredicate {
		boolean[] chars = new boolean[256];

		void add(char c) {
			chars[c] = true;
		}

		@Override
		public boolean test(char c) {
			return chars[c];
		}
	}

	static class BitSet256 {
		long bits0;
		long bits64;
		long bits128;
		long bits192;

		public void set(char c) {
			if (c < 128) {
				if (c < 64) {
					bits0 = BitSet64Tools.set(bits0, c);
				} else {
					bits64 = BitSet64Tools.set(bits64, c - 64);
				}
			} else {
				if (c < 192) {
					bits128 = BitSet64Tools.set(bits128, c - 128);
				} else {
					bits192 = BitSet64Tools.set(bits192, c - 192);
				}
			}
		}

		public boolean get(int c) {
			if (c < 128) {
				if (c < 64) {
					return BitSet64Tools.get(bits0, c);
				} else {
					return BitSet64Tools.get(bits64, c - 64);
				}
			} else {
				if (c < 192) {
					return BitSet64Tools.get(bits128, c - 128);
				} else {
					return BitSet64Tools.get(bits192, c - 192);
				}
			}
		}

	}

	static class TableMatcher3 implements CharPredicate {
		BitSet256 bitSet = new BitSet256();

		void add(char c) {
			bitSet.set(c);
		}

		@Override
		public boolean test(char c) {
			return bitSet.get(c);
		}
	}

	static class TableMatcher4 implements CharPredicate {
		long bits0;
		long bits64;
		long bits128;
		long bits192;

		void add(char c) {
			assert c < 256;

			if (c < 128) {
				if (c < 64) {
					bits0 = BitSet64Tools.set(bits0, c);
				} else {
					bits64 = BitSet64Tools.set(bits64, c - 64);
				}
			} else {
				if (c < 192) {
					bits128 = BitSet64Tools.set(bits128, c - 128);
				} else {
					bits192 = BitSet64Tools.set(bits192, c - 192);
				}
			}
		}

		@Override
		public boolean test(char c) {
			if (c < 128) {
				if (c < 64) {
					return BitSet64Tools.get(bits0, c);
				} else {
					return BitSet64Tools.get(bits64, c - 64);
				}
			} else {
				if (c < 192) {
					return BitSet64Tools.get(bits128, c - 128);
				} else {
					return BitSet64Tools.get(bits192, c - 192);
				}
			}
		}
	}

	static class AnyMatcher implements CharPredicate {

		final char[] chars;

		public AnyMatcher(CharSequence chars) {
			this.chars = chars.toString().toCharArray();
			Arrays.sort(this.chars);
		}

		@Override
		public boolean test(char c) {
			return Arrays.binarySearch(chars, c) >= 0;
		}
	}

	static class AnyMatcher2 implements CharPredicate {

		final String chars;

		public AnyMatcher2(String chars) {
			this.chars = chars;
		}

		@Override
		public boolean test(char c) {
			return chars.indexOf(c) != -1;
		}
	}

	static class RangeMatcher implements CharPredicate {

		private final char startInclusive;
		private final char endInclusive;

		RangeMatcher(char startInclusive, char endInclusive) {
			assert (endInclusive >= startInclusive);
			this.startInclusive = startInclusive;
			this.endInclusive = endInclusive;
		}

		@Override
		public boolean test(char c) {
			return startInclusive <= c && c <= endInclusive;
		}
	}

	public static class StringFindMultipleJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringFindMultipleJmhTest.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands11, BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);
			//opts.setRunTimeMillis(100);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String[] finds = new String[] { "bc", "abcd" };
			String PREFIX = StringTools.repeat("*", 1000);
			IList<String> strs = GapList.create("--bc--" + PREFIX, "--abcd" + PREFIX);
			//IList<String> strs = GapList.create(PREFIX + "--bc--" + PREFIX, PREFIX + "--abcd" + PREFIX);
			CyclicSource<String> strings = new CyclicSource<>(strs.get(0), strs.get(1));
			CyclicSource<StringBuilder> stringBuilders = new CyclicSource<>(new StringBuilder(strs.get(0)), new StringBuilder(strs.get(1)));
		}

		@Benchmark
		public int testIndexOfAny1(MyState state) {
			return indexOfAny1(state.strings.next(), state.finds);
		}

		//		@Benchmark
		//		public int testIndexOfAny2(MyState state) {
		//			return indexOfAny2(state.strings.next(), state.finds);
		//		}

		@Benchmark
		public int testIndexOfAny3(MyState state) {
			return indexOfAny3(state.strings.next(), state.finds);
		}

		@Benchmark
		public int testIndexOfAny4(MyState state) {
			return indexOfAny4(state.strings.next(), state.finds);
		}

		@Benchmark
		public int testIndexOfAny5(MyState state) {
			return indexOfAny5(state.strings.next(), state.finds);
		}

		//@Benchmark
		public int testIndexOfCharSequenceImpl1(MyState state) {
			StringBuilder sb = state.stringBuilders.next();
			return indexOf1(sb, "abcd", 0, -1);
		}

		//@Benchmark
		public int testIndexOfCharSequenceImpl2(MyState state) {
			StringBuilder sb = state.stringBuilders.next();
			return indexOf2(sb, "abcd", 0, -1);
		}

		//@Benchmark
		public int testIndexOfStringWithStringBuilder(MyState state) {
			StringBuilder sb = state.stringBuilders.next();
			String str = sb.toString();
			return str.indexOf("abcd", 0);
		}

		//@Benchmark
		public int testIndexOfStringWithStringToString(MyState state) {
			String str = state.strings.next().toString();
			return str.indexOf("abcd", 0);
		}

		//@Benchmark
		public int testIndexOfStringWithInstanceof(MyState state) {
			CharSequence cs = state.strings.next();
			if (cs instanceof String) {
				return ((String) cs).indexOf("abcd", 0);
			} else {
				return indexOf2(cs, "abcd", 0, -1);
			}
		}

		//@Benchmark
		public int testIndexOfStringWithString(MyState state) {
			String str = state.strings.next();
			return str.indexOf("abcd", 0);
		}

		//@Benchmark
		public int testLocal1(MyState state) {
			return indexOfAny1(state.strings.next(), state.finds);
		}

		//@Benchmark
		public int testStringUtils(MyState state) {
			return StringUtils.indexOfAny(state.strings.next(), state.finds);
		}

		public static int indexOf1(CharSequence str, CharSequence find, int start, int end) {
			if (end == -1) {
				end = str.length();
			}
			int findLen = find.length();

			while (start + findLen <= end) {
				int i;
				for (i = 0; i < findLen; i++) {
					if (find.charAt(i) != str.charAt(start + i)) {
						break;
					}
				}
				if (i == findLen) {
					return start;
				}
				start++;
			}
			return -1;
		}

		public static int indexOf2(CharSequence str, CharSequence find, int start, int end) {
			if (end == -1) {
				end = str.length();
			}
			int findLen = find.length();

			char first = find.charAt(0);
			while (start + findLen <= end) {
				if (str.charAt(start) == first) {
					int i;
					for (i = 1; i < findLen; i++) {
						if (find.charAt(i) != str.charAt(start + i)) {
							break;
						}
					}
					if (i == findLen) {
						return start;
					}
				}
				start++;
			}
			return -1;
		}

		public static int indexOfAny1(final String str, final String... searchStrs) {
			if (str == null || searchStrs == null) {
				return -1;
			}

			// String's can't have a MAX_VALUEth index.
			int ret = Integer.MAX_VALUE;

			int len = str.length();
			for (String search : searchStrs) {
				if (search == null) {
					continue;
				}
				int tmp = CharSequenceTools.indexOf(str, search, 0);
				if (tmp == -1) {
					continue;
				}

				if (tmp < ret) {
					ret = tmp;
				}
			}

			return ret == Integer.MAX_VALUE ? -1 : ret;
		}

		//		public static int indexOfAny2(final String str, final String... searchStrs) {
		//			if (str == null || searchStrs == null) {
		//				return -1;
		//			}
		//
		//			// String's can't have a MAX_VALUEth index.
		//			int ret = Integer.MAX_VALUE;
		//
		//			int len = str.length();
		//			for (String search : searchStrs) {
		//				if (search == null) {
		//					continue;
		//				}
		//				int end = (ret == Integer.MAX_VALUE) ? len : ret + search.length();
		//				int tmp = CharSequenceTools.indexOf(str, search, 0, end);
		//				if (tmp == -1) {
		//					continue;
		//				}
		//
		//				if (tmp < ret) {
		//					ret = tmp;
		//				}
		//			}
		//
		//			return ret == Integer.MAX_VALUE ? -1 : ret;
		//		}

		public static int indexOfAny3(final String str, final String... searchStrs) {
			if (str == null || searchStrs == null) {
				return -1;
			}

			TableMatcher tm = new TableMatcher();
			int maxSearchLen = 0;
			for (String search : searchStrs) {
				int sl = search.length();
				if (sl > 0) {
					tm.add(search.charAt(0));
					if (sl > maxSearchLen) {
						maxSearchLen = sl;
					}
				}
			}

			int end = str.length() - maxSearchLen;
			for (int i = 0; i < end; i++) {
				char c = str.charAt(i);
				if (!tm.test(c)) {
					continue;
				}

				// First char matches
				for (String search : searchStrs) {
					if (str.regionMatches(i, search, 0, search.length())) {
						return i;
					}
				}
			}
			return -1;
		}

		public static int indexOfAny4(final String str, final String... searchStrs) {
			if (str == null || searchStrs == null) {
				return -1;
			}

			TableMatcher3 tm = new TableMatcher3();
			int maxSearchLen = 0;
			for (String search : searchStrs) {
				int sl = search.length();
				if (sl > 0) {
					tm.add(search.charAt(0));
					if (sl > maxSearchLen) {
						maxSearchLen = sl;
					}
				}
			}

			int end = str.length() - maxSearchLen;
			for (int i = 0; i < end; i++) {
				char c = str.charAt(i);
				if (!tm.test(c)) {
					continue;
				}

				// First char matches
				for (String search : searchStrs) {
					if (str.regionMatches(i, search, 0, search.length())) {
						return i;
					}
				}
			}
			return -1;
		}

		public static int indexOfAny5(final String str, final String... searchStrs) {
			if (str == null || searchStrs == null) {
				return -1;
			}

			boolean[] chars = new boolean[256];

			int maxSearchLen = 0;
			for (String search : searchStrs) {
				int sl = search.length();
				if (sl > 0) {
					chars[search.charAt(0)] = true;
					if (sl > maxSearchLen) {
						maxSearchLen = sl;
					}
				}
			}

			int end = str.length() - maxSearchLen;
			for (int i = 0; i < end; i++) {
				char c = str.charAt(i);
				if (!chars[c]) {
					continue;
				}

				// First char matches
				for (String search : searchStrs) {
					if (str.regionMatches(i, search, 0, search.length())) {
						return i;
					}
				}
			}
			return -1;
		}
	}

	/**
	 * Even if the StringFinder in testStringFinderBuilder() is created on each call,
	 * JVM is able to optimize it away so the call will be as fast as the static call to String.indexOf().
	 * @see XStringFinder2JmhTest
	 */
	public static class XStringFinder1JmhTest extends StringJmhBenchmark {

		public XStringFinder1JmhTest() {
			// Needs Java 17 to achieve same speed as java.lang.String
			setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			StringFinder sf = new StringFinder.Builder().setFindString(FIND).build();
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		@Benchmark
		public int testString(MyState state) {
			String str = state.strings.next();
			return str.indexOf(state.FIND);
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			String str = state.strings.next();
			return state.sf.indexOf(str);
		}

		@Benchmark
		public int testStringFinderBuilder(MyState state) {
			String str = state.strings.next();
			StringFinder sf2 = new StringFinder.Builder().setFindString(state.FIND).build();
			return sf2.indexOf(str);
		}
	}

	/**
	 * If testStringFinderBuilder() is using different parameters, execution then 
	 * becomes slower String.indexOf() (but allocation is nevertheless optimized away).
	 * @see XStringFinder1JmhTest
	 */
	public static class XStringFinder2JmhTest extends StringJmhBenchmark {

		public XStringFinder2JmhTest() {
			// Needs Java 17 to achieve same speed as java.lang.String
			setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			StringFinder sf1 = new StringFinder.Builder().setFindString(FIND).build();
			CyclicSource<String> searchs = new CyclicSource<>(FIND, "x");
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		@Benchmark
		public int testString(MyState state) {
			String str = state.strings.next();
			String search = state.searchs.next();
			return str.indexOf(search);
		}

		@Benchmark
		public int testStringFinderBuilder(MyState state) {
			String str = state.strings.next();
			String search = state.searchs.next();
			StringFinder sf2 = new StringFinder.Builder().setFindString(search).build();
			return sf2.indexOf(str);
		}
	}

	public static class XStringFinderStringsJmhTest {

		static void test() {
			Options opts = new Options().includeClass(XStringFinderStringsJmhTest.class);
			// Needs Java 17 to achieve same speed as java.lang.String
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands17));
			//opts.setJavaVersions(GapList.create(BrowniesJavaEnv.JdkCommands11));

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			IList<String> FINDS = GapList.create(StringTools.repeat("0", 1000) + "-", StringTools.repeat("0", 1000) + "*", StringTools.repeat("0", 1000),
					StringTools.repeat("0", 1000) + "-", StringTools.repeat("0", 1000) + "*", StringTools.repeat("0", 1000),
					StringTools.repeat("0", 1000) + "-", StringTools.repeat("0", 1000) + "*", StringTools.repeat("0", 1000),
					StringTools.repeat("0", 1000) + "-", StringTools.repeat("0", 1000) + "*", StringTools.repeat("0", 1000),
					StringTools.repeat("0", 1000) + "-", StringTools.repeat("0", 1000) + "*", StringTools.repeat("0", 1000));
			String[] FINDS2 = FINDS.toArray(String.class);
			StringsMatcher matcher = new StringsMatcher.Builder().setSearchStrs(FINDS).build();
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + StringTools.repeat("a", 1000) + i + "-" + i + ")");
		}

		@Benchmark
		public int testStringUtils(MyState state) {
			return StringUtils.indexOfAny(state.strings.next(), state.FINDS2);
		}

		@Benchmark
		public int testStringsMatcher(MyState state) {
			return state.matcher.indexOf(state.strings.next());
		}

	}

	/**
	 * Show performance of returnEnd which is realized by adding a wrapper to the implementation.
	 */
	public static class XStringFinderReturnEndJmhTest {

		static void test() {
			Options opts = new Options().includeClass(XStringFinderReturnEndJmhTest.class);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			//runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			StringFinder sf1 = new StringFinder.Builder().setFindString(FIND).build();
			StringFinder sf2 = new StringFinder.Builder().setFindMatcher(StringMatcher.of(FIND)).build();
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		//@Benchmark
		public int testStringFinder1(MyState state) {
			return state.sf1.indexOf(state.strings.next());
		}

		//@Benchmark
		public int testStringFinder2(MyState state) {
			return state.sf2.indexOf(state.strings.next());
		}
	}

	/**
	 * Show that StringFinder.indexOf is as fast String.indexOf.
	 */
	public static class IndexOfCharSequenceJmhTest {

		static void test() {
			Options opts = new Options().includeClass(IndexOfCharSequenceJmhTest.class);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			StringFinder sf2 = new StringFinder.Builder().setFindString(FIND).build();
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			return state.sf2.indexOf(state.strings.next());
		}

		@Benchmark
		public int testString(MyState state) {
			return state.strings.next().indexOf(state.FIND);
		}

		@Benchmark
		public int testCharSequenceTools(MyState state) {
			return CharSequenceTools.indexOf(state.strings.next(), state.FIND);
		}

		@Benchmark
		public int testCast(MyState state) {
			return indexOf(state.strings.next(), state.FIND);
		}

		public int indexOf(CharSequence str, CharSequence search) {
			if (str instanceof String && search instanceof String) {
				return ((String) str).indexOf((String) search);
			} else {
				return CharSequenceTools.indexOf(str, search);
			}
		}
	}

	public static class StringFindJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringFindJmhTest.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands11, BrowniesJavaEnv.JdkCommands21));
			opts.setUseGcProfiler(true);
			//opts.setRunTimeMillis(100);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
			StringFinder stringFinder = StringFinder.builder().setFindString(FIND).build();
		}

		@Benchmark
		public int testStatic(MyState state) {
			return state.strings.next().indexOf(state.FIND);
		}

		@Benchmark
		public int testFindServiceStatic(MyState state) {
			return state.stringFinder.indexOf(state.strings.next());
		}
	}

	/**
	 * Compare usage of direct vs if vs function.
	 */
	public static class PerformanceBuilderJmhTest {

		interface Finder {
			int indexOf(String str);

			String before(String str);
		}

		static class Finder1 implements Finder {
			char c;

			Finder1(char c) {
				this.c = c;
			}

			@Override
			public int indexOf(String str) {
				return str.indexOf(c);
			}

			@Override
			public String before(String str) {
				return str.substring(0, indexOf(str));
			}
		}

		static class Finder2 implements Finder {
			String str;
			char c;

			Finder2(String str) {
				this.str = str;
			}

			Finder2(char c) {
				this.c = c;
			}

			@Override
			public int indexOf(String s) {
				if (str != null) {
					return s.indexOf(str);
				} else {
					return s.indexOf(c);
				}
			}

			@Override
			public String before(String str) {
				return str.substring(0, indexOf(str));
			}
		}

		static class Finder3 implements Finder {
			final Function<String, Integer> indexOfFnc;
			final Function<String, String> beforeFnc;

			Finder3(char c) {
				indexOfFnc = s -> s.indexOf(c);
				beforeFnc = s -> s.substring(0, s.indexOf(c));
			}

			@Override
			public int indexOf(String s) {
				return indexOfFnc.apply(s);
			}

			@Override
			public String before(String s) {
				return beforeFnc.apply(s);
			}
		}

		static void test() {
			Options opts = new Options().includeClass(PerformanceBuilderJmhTest.class);
			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class FinderState {
			Finder1 finder1 = new Finder1('.');
			Finder2 finder2 = new Finder2('.');
			Finder3 finder3 = new Finder3('.');

			String find = "abc.def";
		}

		//

		@Benchmark
		public Object testFinder1IndexOf(FinderState state) {
			return state.finder1.indexOf(state.find);
		}

		@Benchmark
		public Object testFinder2IndexOf(FinderState state) {
			return state.finder2.indexOf(state.find);
		}

		@Benchmark
		public Object testFinder3IndexOf(FinderState state) {
			return state.finder3.indexOf(state.find);
		}

		@Benchmark
		public Object testFinder1Before(FinderState state) {
			return state.finder1.before(state.find);
		}

		@Benchmark
		public Object testFinder2Before(FinderState state) {
			return state.finder2.before(state.find);
		}

		@Benchmark
		public Object testFinder3Before(FinderState state) {
			return state.finder3.before(state.find);
		}
	}

	/**
	 * Compare usage of static vs new vs thread local.
	 */
	public static class PerformanceCreateJmhTest {

		static void test() {
			Options opts = new Options().includeClass(PerformanceCreateJmhTest.class);
			opts.setUseGcProfiler(true);
			//opts.setWarmupIterations(1).setMeasurementIterations(1).setRunTimeSecs(2);
			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);
		}

		static final String FILE_SEPARATORS = "/\\";

		@State(Scope.Benchmark)
		public static class PathState {
			String path = "abc/def/ghi";
		}

		//

		@Benchmark
		public Object testNew(PathState state) {
			StringFinder finder = StringFinder.builder().setFindAnyChar(FILE_SEPARATORS).setReverse(true).build();
			int index = finder.indexOf(state.path);
			return index;
		}

		//

		static final StringFinder FINDER = StringFinder.builder().setFindAnyChar(FILE_SEPARATORS).setReverse(true).build();

		@Benchmark
		public Object testStatic(PathState state) {
			int index = FINDERS.get().indexOf(state.path);
			return index;
		}

		//

		static final ThreadLocal<StringFinder> FINDERS = new ThreadLocal<StringFinder>() {
			@Override
			protected StringFinder initialValue() {
				return StringFinder.builder().setFindAnyChar(FILE_SEPARATORS).setReverse(true).build();
			}
		};

		@Benchmark
		public Object testThread1(PathState state) {
			int index = FINDERS.get().indexOf(state.path);
			return index;
		}

		//

		static final ThreadCache<StringFinder> FINDER2 = new ThreadCache<StringFinder>(
				() -> StringFinder.builder().setFindAnyChar(FILE_SEPARATORS).setReverse(true).build());

		@Benchmark
		public Object testThread2(PathState state) {
			int index = FINDER2.get().indexOf(state.path);
			return index;
		}

	}

	public void runManual() {
		String str = "ab-ab-AB";

		CharPredicate cp = CharPredicates.of('a');
		StringFinder.Builder sfb = StringFinder.builder().setFindCharPredicate(cp);
		//StringFinder.Builder sfb = StringFinder.builder().setFindChar('a');
		int start = 8;
		int occurrence = 0;
		boolean reverse = true;
		boolean ignoreCase = true;

		sfb.setOccurrence(occurrence).setReverse(reverse).setIgnoreCase(ignoreCase);

		StringFinder sf = sfb.build();
		int index = sf.indexOf(str, start);
		LOG.info("{}", index);

		//testIndexOf(sfb, str, "MANUAL", start, occurrence, reverse, ignoreCase);
	}

	@Capture
	public void testIndexOfPattern() {
		String str = "ab-ab";
		StringFinder.Builder sfb;

		// Start
		sfb = StringFinder.builder().setFindMatcher(new RegexStringMatcher().setPattern("^a"));
		boolean reverse = false;
		testIndexOf(sfb, str, "regex(^a)", 0, 0, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 0, 1, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 3, 0, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 3, 1, reverse, false);

		reverse = true;
		testIndexOf(sfb, str, "regex(^a)", 6, 0, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 6, 1, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 3, 0, reverse, false);
		testIndexOf(sfb, str, "regex(^a)", 3, 1, reverse, false);

		// End
		sfb = StringFinder.builder().setFindMatcher(new RegexStringMatcher().setPattern("b$"));
		reverse = false;
		testIndexOf(sfb, str, "regex(b$)", 0, 0, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 0, 1, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 1, 0, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 1, 1, reverse, false);

		reverse = true;
		testIndexOf(sfb, str, "regex(b$)", 6, 0, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 6, 1, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 1, 0, reverse, false);
		testIndexOf(sfb, str, "regex(b$)", 1, 1, reverse, false);
	}

	@Trace(traceMethod = "/indexOf/")
	public void testIndexOfOccurrence() {
		StringFinder.Builder sfb = StringFinder.builder().setFindString("");
		StringFinder sf = sfb.build();
		int index0 = sf.indexOf("abc");
		CheckTools.check(index0 == 0);

		sf = sfb.setOccurrence(1).build();
		int index1 = sf.indexOf("abc");
		CheckTools.check(index1 == 0);

		CheckTools.check(StringUtils.countMatches("abc", "") == 0);
		CheckTools.check(StringUtils.indexOf("abc", "") == 0);
	}

	@Capture
	public void testIndexOfRepeat() {
		String str = "ab-aab-AB";
		StringFinder.Builder sfb = StringFinder.builder().setFindChar('a');
		StringFinder sf = sfb.build();
		doTestIndexOfRepeat(sf, str);

		sf = sfb.setIgnoreCase(true).build();
		doTestIndexOfRepeat(sf, str);
	}

	void doTestIndexOfRepeat(StringFinder sf, String str) {
		IIntList indexes = IntGapList.create();
		int start = 0;
		while (true) {
			int index = sf.indexOf(str, start);
			if (index == -1) {
				break;
			}
			indexes.add(index);
			start = index + 1;
		}
		LOG.info("{}", indexes);
	}

	@Trace
	public void testContains() {
		String str = "abc";
		CharPredicateMatcher cpm = new CharPredicateMatcher(Character::isLetterOrDigit);
		StringFinder sf = StringFinder.builder().setFindMatcher(cpm).build();
		sf.contains(str);
	}

	@Trace
	public void testCount() {
		String str = "ab-ab-AB";
		StringFinder sf = StringFinder.builder().setFindString("ab").build();
		sf.count(str);
	}

	@Trace(traceMethod = "/count|matches/")
	public void testOverlap() {
		String str = "-aaBBaaBBaa-";
		StringFinder sf0 = StringFinder.builder().setFindString("aaBBaa").build();
		StringFinder sf1 = StringFinder.builder().setFindString("aaBBaa").setOverlap(true).build();

		sf0.count(str);
		sf1.count(str);

		IList<IMatch> matches0 = sf0.matches(str);
		IList<IMatch> matches1 = sf1.matches(str);

		IList<IMatch> matchesIterator0 = CollectionTools2.collect(sf0.matchIterator(str));
		CheckTools.check(ObjectTools.equals(matchesIterator0, matches0));
		IList<IMatch> matchesIterator1 = CollectionTools2.collect(sf1.matchIterator(str));
		CheckTools.check(ObjectTools.equals(matchesIterator1, matches1));
	}

	@Capture
	public void testIndexOf() {
		String str = "ab-ab-AB";
		StringFinder.Builder sfb;

		// Check that the result for the different ways of searching are identical
		sfb = getBuilder().setFindChar('a');
		IList<Integer> result = testIndexOf(sfb, str, "char(a)");

		sfb = getBuilder().setFindCodePoint('a');
		IList<Integer> r2 = testIndexOf(sfb, str, "codePoint('a')");
		checkResult(result, r2);

		CharPredicate cp = CharPredicates.of('a');
		sfb = getBuilder().setFindCharPredicate(cp);
		IList<Integer> r3 = testIndexOf(sfb, str, "charPredicate(a)");
		checkResult(result, r3);

		sfb = getBuilder().setFindAnyChar("xa");
		IList<Integer> r4 = testIndexOf(sfb, str, "anyChar(xa)");
		checkResult(result, r4);

		sfb = getBuilder().setFindString("a");
		IList<Integer> r5 = testIndexOf(sfb, str, "string(a)");
		checkResult(result, r5);

		sfb = getBuilder().setFindRegex("[a]");
		IList<Integer> r6 = testIndexOf(sfb, str, "regex(a)");
		checkResult(result, r6);

		CheckTools.check("".indexOf("") == 0);
		CheckTools.check("x".indexOf("") == 0);
	}

	StringFinder.Builder getBuilder() {
		return new MyBuilder();
	}

	/** 
	 * Specialized Builder which prevents the optimization of <br> 
	 * - string to codePoint or char
	 * - codePoint to char
	 * - anyChar to codePoint or char
	 * - regex to string
	 */
	static class MyBuilder extends StringFinder.Builder {

		@Override
		public void setString(CharSequence str) {
			super.doSetString(str);
		}

		@Override
		public void setCodePoint(int codePoint) {
			super.doSetCodePoint(codePoint);
		}

		@Override
		public void setAnyChar(String anyChar) {
			super.doSetAnyChar(anyChar);
		}

		@Override
		public void setRegex(String regex) {
			super.doSetRegex(regex);
		}
	}

	void checkResult(IList<Integer> ref, IList<Integer> act) {
		if (!act.equals(ref)) {
			ListDiff<Integer> diff = ListDiff.create(ref, act);
			diff.checkEqual();
		}
	}

	IList<Integer> testIndexOf(StringFinder.Builder sfb, String str, String find) {
		IList<Integer> result = GapList.create();
		for (int start = 0; start <= 1; start++) {
			for (int occurrence = 0; occurrence <= 2; occurrence++) {
				for (int reverse = 0; reverse <= 1; reverse++) {
					for (int ignoreCase = 0; ignoreCase <= 1; ignoreCase++) {
						int pos;
						if (start == 0) {
							pos = (reverse == 0) ? 0 : str.length();
						} else {
							pos = (reverse == 0) ? 1 : str.length() - 3;
						}
						int index = testIndexOf(sfb, str, find, pos, occurrence, reverse != 0, ignoreCase != 0);
						result.add(index);
					}
				}
			}
		}
		return result;
	}

	int testIndexOf(StringFinder.Builder sfb, String str, String findDesc, int start, int occurrence, boolean reverse, boolean ignoreCase) {
		CharSequence cs = new GapString(str);
		boolean defaultPos = ((!reverse && start == 0) || (reverse && start == str.length()));

		// Call indexOf()
		StringFinder sf = null;
		try {
			sf = sfb
					.setOccurrence(occurrence)
					.setReverse(reverse)
					.setIgnoreCase(ignoreCase)
					.build();
		} catch (IllegalArgumentException e) {
			CheckTools.check(e.getMessage().equals("Case Insensitive handling not supported"));
			return Integer.MIN_VALUE;
		}

		int index = sf.indexOf(str, start);
		String startStr = (start == -1) ? "default" : "" + start;
		String indexStr = new StringPadder.Builder().setLength(2).setPadMode(PadMode.LEFT).build().pad(String.valueOf(index));
		LOG.info("Find {} in {} -> {} (start={}, occurrence={}, reverse={}, ignoreCase={}) using {}",
				findDesc, str, indexStr, startStr, occurrence, reverse, ignoreCase, sf.getClass().getSimpleName());

		{
			// Compare String vs CharSequence
			int index2 = sf.indexOf(cs, start);
			CheckTools.check(index2 == index);
		}
		if (defaultPos) {
			int index2 = sf.indexOf(str);
			CheckTools.check(index2 == index);
		}

		// Compare with find()
		IMatch match = sf.find(str, start);
		{
			// Compare String vs CharSequence			
			IMatch match2 = sf.find(cs, start);
			CheckTools.check(ObjectTools.equals(FuncTools.getIf(match2, IMatch::getStart), FuncTools.getIf(match, IMatch::getStart)));
			CheckTools.check(ObjectTools.equals(FuncTools.getIf(match2, IMatch::getEnd), FuncTools.getIf(match, IMatch::getEnd)));
		}

		if (index == -1) {
			CheckTools.check(match == null);
		} else {
			CheckTools.check(match.getStart() == index);
		}

		if (defaultPos) {
			IMatch match2 = sf.find(str);
			CheckTools.check(ObjectTools.equals(match2, match));
		}

		// Compare with endIndex()
		int endIndex = sf.indexOfEnd(str, start);
		{
			// Compare String vs CharSequence
			int endIndex2 = sf.indexOfEnd(cs, start);
			CheckTools.check(endIndex2 == endIndex);
		}
		if (endIndex == -1) {
			CheckTools.check(match == null);
		} else {
			CheckTools.check(endIndex == match.getEnd());
		}

		if (defaultPos) {
			int endIndex2 = sf.indexOfEnd(str);
			CheckTools.check(ObjectTools.equals(endIndex2, endIndex));
		}

		// Compare with contains()
		boolean contains = sf.contains(str, start);
		CheckTools.check(contains == (match != null));

		if (defaultPos) {
			boolean contains2 = sf.contains(str);
			CheckTools.check(ObjectTools.equals(contains2, contains));
		}

		// Compare with matches()
		IList<IMatch> matches = sf.matches(str, start);
		if (index == -1) {
			CheckTools.check(matches.size() <= occurrence);
		} else {
			CheckTools.check(match.equals(matches.getFirst()));
		}

		if (defaultPos) {
			IList<IMatch> matches2 = sf.matches(str);
			CheckTools.check(ObjectTools.equals(matches2, matches));
		}

		// Compare with matchIterator()
		IList<IMatch> matchesIterator = CollectionTools2.collect(sf.matchIterator(str, start));
		CheckTools.check(ObjectTools.equals(matchesIterator, matches));

		if (defaultPos) {
			IList<IMatch> matchesIterator2 = sf.matches(str);
			CheckTools.check(ObjectTools.equals(matchesIterator2, matchesIterator));
		}

		// Compare with count()
		int count = sf.count(str, start);
		CheckTools.check(count == matches.size());

		if (defaultPos) {
			int count2 = sf.count(str);
			CheckTools.check(count2 == count);
		}

		return index;
	}

	/** 
	 * Show that StringFinder is about 5-10% slower for finding string/char compared to java.lang.String
	 */
	public static class StringFinderTestJmh extends StringJmhBenchmark {

		public StringFinderTestJmh() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String findString = "aa";
			final StringFinder finderString = StringFinder.builder().setFindString(findString).build();

			final char findChar = 'a';
			final StringFinder finderChar = StringFinder.builder().setFindChar(findChar).build();

			final CyclicSource<String> source = new CyclicSource<>("a", "b", "aabaa", "baab");
		}

		@Benchmark
		public int testFindString_StringFinder(MyState state) {
			String str = state.source.next();
			return state.finderString.indexOf(str);
		}

		@Benchmark
		public int testFindString_JavaLangString(MyState state) {
			String str = state.source.next();
			return str.indexOf(state.findString);
		}

		@Benchmark
		public int testFindChar_StringFinder(MyState state) {
			String str = state.source.next();
			return state.finderChar.indexOf(str);
		}

		@Benchmark
		public int testFindChar_JavaLangString(MyState state) {
			String str = state.source.next();
			return str.indexOf(state.findChar);
		}
	}

	public static class StringFinderCharPredicateTestJmh extends StringJmhBenchmark {

		public StringFinderCharPredicateTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String anyChar = "abc";
			StringFinder finderAnyChar = StringFinder.builder().setFindAnyChar(anyChar).build();

			CharPredicate cp = c -> anyChar.indexOf(c) != -1;
			final StringFinder finderCharPredicate = StringFinder.builder().setFindCharPredicate(cp).build();

			CyclicSource<String> source = new CyclicSource<>("a", "01b23", "xyz");
		}

		@Benchmark
		public int testFindCharPredicate(MyState state) {
			String str = state.source.next();
			return state.finderCharPredicate.indexOf(str);
		}

		@Benchmark
		public int testFindAnyChar(MyState state) {
			String str = state.source.next();
			return state.finderAnyChar.indexOf(str);
		}
	}

	public static class FindStringTestJmh extends StringJmhBenchmark {

		public FindStringTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String findString = "aa";
			final StringFinder finderString = StringFinder.builder().setFindString(findString).build();
			final CyclicSource<String> source = new CyclicSource<>("a", "b", "aabaa", "baab");
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			String str = state.source.next();
			return state.finderString.indexOf(str);
		}

		@Benchmark
		public int testJavaLangString(MyState state) {
			String str = state.source.next();
			return str.indexOf(state.findString);
		}

		@Benchmark
		public int testStringUtils(MyState state) {
			String str = state.source.next();
			return StringUtils.indexOf(str, state.findString);
		}
	}

	public static class FindCharSequenceTestJmh extends StringJmhBenchmark {

		public FindCharSequenceTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String findString = "aa";
			final StringFinder finderString = StringFinder.builder().setFindString(findString).build();
			final IList<String> strings = GapList.create("a", "b", "aabaa", "baab");
			CyclicSource<IString> charseqs = new CyclicSource<>(strings.map(GapString::new));

		}

		@Benchmark
		public int testStringFinder(MyState state) {
			IString str = state.charseqs.next();
			return state.finderString.indexOf(str);
		}

		@Benchmark
		public int testStringUtils(MyState state) {
			IString str = state.charseqs.next();
			return StringUtils.indexOf(str, state.findString);
		}
	}

	public static class ContainsCharSequenceTestJmh extends StringJmhBenchmark {

		public ContainsCharSequenceTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final CharSequence findString = new GapString("aa");
			final StringFinder finderString = StringFinder.builder().setFindString(findString).build();
			final CyclicSource<String> strings = new CyclicSource<>("a", "b", "aabaa", "baab");
		}

		@Benchmark
		public boolean testStringFinder(MyState state) {
			String str = state.strings.next();
			return state.finderString.contains(str);
		}

		@Benchmark
		public boolean testJavaLangString(MyState state) {
			String str = state.strings.next();
			return str.contains(state.findString);
		}

		@Benchmark
		public boolean testStringUtils(MyState state) {
			String str = state.strings.next();
			return StringUtils.contains(str, state.findString);
		}
	}

	/** 
	 * Show that StringFinder is about 5-10% slower for finding string/char compared to java.lang.String
	 */
	public static class StringFinderTest2Jmh extends StringJmhBenchmark {

		public StringFinderTest2Jmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
			setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String findString = "aa";
			StringFinder finderString = StringFinder.builder().setFindString(findString).build();
			Find2 find2 = new Find2();
			Find3 find3 = new Find3();
			CyclicSource<String> source = new CyclicSource<>("a", "b", "aabaa", "baab");
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			String str = state.source.next();
			return state.finderString.indexOf(str);
		}

		@Benchmark
		public int testFindJavaLangString(MyState state) {
			String str = state.source.next();
			return str.indexOf(state.findString);
		}

		@Benchmark
		public int testFindString1(MyState state) {
			String str = state.source.next();
			return doIndexOf(str, state.findString);
		}

		@Benchmark
		public int testFindString2(MyState state) {
			String str = state.source.next();
			return state.find2.indexOf(str, state.findString);
		}

		@Benchmark
		public int testFindString3(MyState state) {
			String str = state.source.next();
			return state.find3.indexOf(str, state.findString);
		}

		static int doIndexOf(String str, String find) {
			return str.indexOf(find);
		}

		static class Find2 {
			int indexOf(String str, String find) {
				return doIndexOf(str, find);
			}
		}

		interface IFind3 {
			int indexOf(String str, String find);
		}

		static class Find3 implements IFind3 {
			@Override
			public int indexOf(String str, String find) {
				return doIndexOf(str, find);
			}
		}

	}

	/** Show performance of StringFinder */
	public static class StringFinderTest22Jmh {

		public static void test() {
			Options opts = new Options().includeClass(StringFinderTest2Jmh.class).setUseGcProfiler(true);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.createJdkTools(JavaVersion.JAVA_21)));
			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String SEARCH = "\r\n";
			final char[] CHARS = SEARCH.toCharArray();
			final StringFinder stringFinder = StringFinder.builder().setFindAnyChar(SEARCH).build();

			final String str = StringRepeater.build("-").repeat(10);
			CyclicSource<String> source = new CyclicSource<>("ab", "ab\rcd", "ab\ncd");
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			return state.stringFinder.indexOf(state.source.next());
		}

		@Benchmark
		public int testStringUtilsIndexOfAny(MyState state) {
			return StringUtils.indexOfAny(state.source.next(), state.SEARCH);
		}

		@Benchmark
		public int testIndexOfAny1(MyState state) {
			return indexOfAny1(state.source.next(), state.SEARCH);
		}

		@Benchmark
		public int testIndexOfAny2(MyState state) {
			return indexOfAny2(state.source.next(), state.CHARS);
		}

		static int indexOfAny1(String str, String chars) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (chars.indexOf(c) != -1) {
					return i;
				}
			}
			return -1;
		}

		static int indexOfAny2(String str, char[] chars) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				for (int j = 0; j < chars.length; j++) {
					if (chars[j] == c) {
						return i;
					}
				}
			}
			return -1;
		}

	}

}
