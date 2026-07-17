package org.magicwerk.strings;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.apache.commons.lang3.StringUtils;
import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.StringRoots;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.magicwerk.strings.matcher.StringsMatcher.Builder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.Strings;

/**
 * Test of class {@link StringRoots}.
 */
public class StringRootsTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringRootsTest().run();
	}

	void run() {
		test();

		//testBenchmarks();
		//runBenchmarks();
	}

	@Trace(traceMethod = "/.*/")
	public void test() {
		{
			StringRoots sr = StringRoots.builder().build();
			doTest(sr, "abc", "ab");
		}
		{
			StringRoots sr = StringRoots.builder().setReverse(true).build();
			doTest(sr, "abc", "bc");
		}
		{
			StringRoots sr = StringRoots.builder().setIgnoreCase(true).build();
			doTest(sr, "abc", "Ab");
		}
		{
			StringRoots sr = StringRoots.builder().setIgnoreCase(true).setReverse(true).build();
			doTest(sr, "abc", "Bc");
		}
		// Surrogates
		{
			StringRoots sr = StringRoots.builder().build();
			doTest(sr, "x" + CodePointToolsTest.STRING_SURROGATE_0, "x" + CodePointToolsTest.STRING_SURROGATE_1);
		}
		{
			StringRoots sr = StringRoots.builder().setReverse(true).build();
			doTest(sr, CodePointToolsTest.STRING_SURROGATE_0 + "x", CodePointToolsTest.STRING_SURROGATE_1 + "x");
		}
	}

	@InheritTrace
	void doTest(StringRoots sr, String s0, String s1) {
		int len0 = sr.getCommonLength(s0, s1);
		String str0 = sr.getCommonRoot(s0, s1);

		IList<String> strs = GapList.create(s0, s1, s1);
		int len1 = sr.getCommonLength(strs);
		String str1 = sr.getCommonRootString(strs);

		CheckTools.check(len0 == len1);
		CheckTools.check(ObjectTools.equals(str0, str1));
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringRootsPrefixBenchmarkTest.class, StringRootsSuffixBenchmarkTest.class, StringRootsIgnoreCaseBenchmarkTest.class,
				StringRootsIndexBenchmarkTest.class);
	}

	public static class StringRootsBenchmarkTestBase {

		@TestData
		IList<String> inputs0 = GapList.create("abc", "abc", "abc", "abc", "abc", "abc", "");
		@TestData
		IList<String> inputs1 = GapList.create("abc", "ab", "abX", "bc", "Xbc", "", "");
	}

	public static class StringRootsPrefixBenchmarkTest extends StringRootsBenchmarkTestBase {

		StringRoots stringRootsPrefix = StringRoots.builder().build();

		@TestMethod
		public String testStringRoots(String s0, String s1) {
			return stringRootsPrefix.getCommonRoot(s0, s1);
		}

		@TestMethod
		public String testGuavaStrings(String s0, String s1) {
			return Strings.commonPrefix(s0, s1);
		}

		@TestMethod
		public String testCommonsStringUtils(String s0, String s1) {
			return StringUtils.getCommonPrefix(s0, s1);
		}
	}

	public static class StringRootsSuffixBenchmarkTest extends StringRootsBenchmarkTestBase {

		StringRoots stringRootsSuffix = StringRoots.builder().setReverse(true).build();

		// not supported by CommonsLang

		@TestMethod
		public String testStringRoots(String s0, String s1) {
			return stringRootsSuffix.getCommonRoot(s0, s1);
		}

		@TestMethod
		public String testGuavaStrings(String s0, String s1) {
			return Strings.commonSuffix(s0, s1);
		}
	}

	public static class StringRootsIndexBenchmarkTest extends StringRootsBenchmarkTestBase {

		// not supported by Guava

		StringRoots stringRoots = StringRoots.builder().build();

		@TestMethod
		public int testStringRoots(String s0, String s1) {
			return stringRoots.getDiffIndex(s0, s1);
		}

		@TestMethod
		public int testCommonsStringUtils(String s0, String s1) {
			return StringUtils.indexOfDifference(s0, s1);
		}

	}

	/** Compare StringRoots with/without ignoring case */
	public static class StringRootsIgnoreCaseBenchmarkTest extends StringRootsBenchmarkTestBase {

		StringRoots stringRoots = StringRoots.builder().build();
		StringRoots stringRootsIgnoreCase = StringRoots.builder().setIgnoreCase(true).build();

		@TestMethod
		public String testStringRootsIgnoreCase(String s0, String s1) {
			return stringRootsIgnoreCase.getCommonRoot(s0, s1);
		}

		@TestMethod
		public String testStringRoots(String s0, String s1) {
			return stringRoots.getCommonRoot(s0, s1);
		}
	}

	//

	public static class TreeStringMatcherJmhTest extends StringJmhBenchmark {
		// Test cases:
		// 1. Search few short strings in a short input string, e.g.
		//    Input: "0123456789", search for "456", "abc"
		// 2. Search many short strings in a short input string, e.g.
		//    Input: "0123456789", search for "456", "aaa", "bbb", "ccc", etc.
		//    -> tree must be efficient
		// 3. Search long strings in a short input string, e.g.
		//    Input: "012", search for "abcde"
		//    -> they cannot match 
		// 4. Search few long strings in a long input string, e.g.
		//    Input: 1000 x "-" + "0123456789", search for 1000 x "-" +"012", "abc"
		//    -> match should be fast if only one leaf can match

		{
			//setRunVerify(false);
			//setJavaVersions(JavaVersion.JAVA_21);
		}

		static void testManual() {
			//			MyState state = new MyState();
			//			state.sourceMode = SourceMode.LARGE;
			//			state.findMode = FindMode.MANY;
			//			state.findMode = FindMode.FEW;
			//			TreeStringMatcherJmhTest test = new TreeStringMatcherJmhTest();
			//			test.testTrie(state);
		}

		@State(Scope.Benchmark)
		public static class MyState {

			public enum TestCase {
				SMALL,
				MANY,
				LONG,
				SINGLE;
			}

			//@Param
			@Param({ "SINGLE" })
			TestCase testCase;

			//@Param
			@Param({ "false" })
			boolean ignoreCase;

			public MyState() {
				initSmall();
				initMany();
				initLong();
				initSingle();
			}

			void initSmall() {
				IList<String> searchStrs = GapList.create("456", "abc", "def");
				addTrie(TestCase.SMALL, createTrie(searchStrs));
				addStringsMatcher(TestCase.SMALL, createTreeStringsMatcher(searchStrs));

				CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");
				addSource(TestCase.SMALL, smallSource);
			}

			void initMany() {
				IList<String> searchStrs = GapList.create("456");
				for (int i = 0; i < 1000; i++) {
					char c = (char) i;
					searchStrs.add("x" + c + "x");
				}
				addTrie(TestCase.MANY, createTrie(searchStrs));
				addStringsMatcher(TestCase.MANY, createTreeStringsMatcher(searchStrs));

				CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");
				addSource(TestCase.MANY, smallSource);
			}

			void initLong() {
				IList<String> searchStrs = GapList.create("456");
				for (int i = 0; i < 1000; i++) {
					searchStrs.add("45" + StringTools.repeat("x", 100) + i);
				}
				addTrie(TestCase.LONG, createTrie(searchStrs));
				addStringsMatcher(TestCase.LONG, createTreeStringsMatcher(searchStrs));

				CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");
				addSource(TestCase.LONG, smallSource);
			}

			void initSingle() {
				String part = StringTools.repeat("x", 1000);
				String find = part + "x";
				IList<String> searchStrs = GapList.create(find);
				IList<String> inputStrs = GapList.create();
				for (int i = 0; i < 1000; i++) {
					searchStrs.add(part + "y" + i);
					inputStrs.add(i + part + "x" + i);
				}

				addTrie(TestCase.SINGLE, createTrie(searchStrs));
				addStringsMatcher(TestCase.SINGLE, createTreeStringsMatcher(searchStrs));

				CyclicSource<String> smallSource = new CyclicSource<>(inputStrs);
				addSource(TestCase.SINGLE, smallSource);
			}

			void addTrie(TestCase testCase, Trie trie) {
				trieList.put(testCase.ordinal(), trie);
			}

			void addStringsMatcher(TestCase testCase, StringsMatcher sm) {
				stringsMatcherList.put(testCase.ordinal(), sm);
			}

			void addSource(TestCase testCase, CyclicSource<String> cs) {
				sources.put(testCase.ordinal(), cs);
			}

			Trie createTrie(IList<String> strs) {
				TrieBuilder b = Trie.builder().ignoreOverlaps();
				if (ignoreCase) {
					b.ignoreCase();
				}
				strs.forEach(s -> b.addKeyword(s));
				return b.build();
			}

			StringsMatcher createTreeStringsMatcher(IList<String> strs) {
				Builder smb = StringsMatcher.builder().setSearchStrs(strs).setUseTree(true);
				if (ignoreCase) {
					smb.setIgnoreCase(true);
				}
				return smb.build();
			}

			IList<StringsMatcher> stringsMatcherList = GapList.create();
			IList<Trie> trieList = GapList.create();

			StringsMatcher getStringsMatcher() {
				return stringsMatcherList.get(testCase.ordinal());
			}

			Trie getTrie() {
				return trieList.get(testCase.ordinal());
			}

			IList<CyclicSource<String>> sources = GapList.create();

			String next() {
				CyclicSource<String> source = sources.get(testCase.ordinal());
				return source.next();
			}

		}

		@Benchmark
		public int testC(MyState state) {
			String s = state.next();
			return state.getStringsMatcher().indexOf(s);
		}

		@Benchmark
		public int testTrie(MyState state) {
			String s = state.next();
			Emit firstMatch = state.getTrie().firstMatch(s);
			return firstMatch.getStart();
		}
	}
}
