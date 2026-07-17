package org.magicwerk.strings.matcher;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.diff.SetDiff;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestInit;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringJoiner;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.ConditionalStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.MultiStringMatcher;
import org.magicwerk.strings.matcher.NestedStringMatcher;
import org.magicwerk.strings.matcher.StartEndStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.magicwerk.strings.matcher.StringsMatcher.Builder;
import org.magicwerk.strings.matcher.StringsMatcher.StringsSimpleMatcher;
import org.magicwerk.strings.matcher.StringsMatcher.StringsTreeCharMatcher;
import org.magicwerk.strings.matcher.StringsMatcher.StringsTreeMatcher;
import org.slf4j.Logger;

/**
 * Test of class {@link StringsMatcher}.
 */
public class StringsMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringsMatcherTest().run();
	}

	void run() {
		//testFind();
		//testFind2();
		//testFind3();
		//testFind4();
		//testIndexOfStrings();
		//testMatchNot();
		//testAhoCorasick();
		//testMatchNodeTree();
		testStringsTreeMatcher();

		//test(TreeStringMatcherJmhTest.class);
	}

	/** Example showing how combining matcher can be used to solve complex problem quite easily */
	@Capture
	public void testExample() {
		IStringMatcher commentStart = StringMatcher.of("/*");
		IStringMatcher commentEnd = StringMatcher.of("*/");
		IStringMatcher stringStart = StringMatcher.of("\"");
		IStringMatcher stringEnd = StringMatcher.of("\"");
		IStringMatcher commentMatcher = new StartEndStringMatcher(commentStart, commentEnd);
		IStringMatcher stringMatcher = new StartEndStringMatcher(stringStart, stringEnd);
		MultiStringMatcher multiMatcher = new MultiStringMatcher(commentMatcher, stringMatcher);
		ConditionalStringMatcher matcher = new ConditionalStringMatcher(multiMatcher, m -> m.getString().startsWith("/*"));

		String input = "/* abc */ \"abc\" /* abc */";
		StringFinder finder = StringFinder.build(b -> b.setFindMatcher(matcher));
		IList<IMatch> comments = finder.matches(input);
		LOG.info("{}", comments);

		IStringMatcher textMatcher = StringMatcher.of("b");
		NestedStringMatcher matcher2 = new NestedStringMatcher(matcher, textMatcher);
		StringFinder finder2 = StringFinder.build(b -> b.setFindMatcher(matcher2));
		IList<IMatch> matches = finder2.matches(input);
		LOG.info("{}", matches);

		StringReplacer replacer = StringReplacer.build(b -> b.setFindMatcher(matcher2).setReplaceString("XYZ"));
		String output = replacer.replace(input);
		LOG.info("{}", output);
	}

	@Capture
	public void testStringsTreeMatcher() {
		//doTestStringsTreeMatcher(GapList.create(""));
		//doTestStringsTreeMatcher(GapList.create("a"));
		doTestStringsTreeMatcher(GapList.create("a", "b"));
		doTestStringsTreeMatcher(GapList.create("a", "ab"));
		doTestStringsTreeMatcher(GapList.create("a", "ab", "abc"));
		doTestStringsTreeMatcher(GapList.create("abcd", "ab"));
		doTestStringsTreeMatcher(GapList.create("abcdef", "ab", "abcd"));
		doTestStringsTreeMatcher(GapList.create("abcdef", "abcd", "ab"));
		doTestStringsTreeMatcher(GapList.create("abcd", "ab", "cd"));

		doTestStringsTreeMatcher(GapList.create("abc"));
		doTestStringsTreeMatcher(GapList.create("abc", "xyz"));
		doTestStringsTreeMatcher(GapList.create("abc", "abcx"));
		doTestStringsTreeMatcher(GapList.create("abc", "abx"));
		doTestStringsTreeMatcher(GapList.create("abc", "abxy"));
	}

	void doTestStringsTreeMatcher(IList<String> searchStrs) {
		doTestStringsTreeMatcher(searchStrs, GapList.create());
	}

	void doTestStringsTreeMatcher(IList<String> searchStrs, IList<String> searchNotStrs) {
		StringsMatcher sm = StringsMatcher.builder().setSearchStrs(searchStrs).setSearchNotStrs(searchNotStrs).setUseTree(true).build();
		StringsMatcher.StringsTreeCharMatcher sm2 = (StringsTreeCharMatcher) sm;
		LOG.info("{}\n{}", sm2.printSearchStrings(), sm2.printTree());
		Set<String> ss1 = new TreeSet<>(searchStrs);
		Set<String> ss2 = sm2.getSearchString();
		new SetDiff<>(ss1, ss2).checkEqual();

		for (String str : searchStrs) {
			// Find bare search string
			IMatch m = sm.find(str);
			LOG.info("{} -> {}", str, m);
			CheckTools.check(m != null && m.getStart() == 0 && m.getLength() == str.length());

			// Find search string surrounded with brackets
			String str2 = "[" + str + "]";
			IMatch m2 = sm.find(str2);
			CheckTools.check(m2 != null);
			if (m.getStart() == m.getEnd()) {
				CheckTools.check(m2.getStart() == 0 && m2.getLength() == 0);
			} else {
				CheckTools.check(m2.getStart() == m.getStart() + 1 && m2.getLength() == m.getLength());
			}
		}

		// Find all search strings
		{
			String join = StringJoiner.build(b -> b.setJoin('-')).joinStrings(searchStrs);
			int pos = 0;
			IMatch m = null;
			for (String str : searchStrs) {
				IMatch m2 = sm.find(join, pos);
				if (m != null) {
					CheckTools.check(m2.getStart() == m.getEnd() + 1 && m2.getLength() == str.length());
				}
				m = m2;
				pos = m2.getEnd();
			}
		}

		// Ignore case
		StringsMatcher smm = StringsMatcher.builder().setSearchStrs(searchStrs).setSearchNotStrs(searchNotStrs).setUseTree(true).setIgnoreCase(true).build();
		StringsMatcher.StringsTreeCharMatcher smm2 = (StringsTreeCharMatcher) smm;
		LOG.info("{}\n{}", smm2.printSearchStrings(), sm2.printTree());

		String join = StringJoiner.build(b -> b.setJoin('-')).joinStrings(searchStrs).toUpperCase();
		int pos = 0;
		IMatch m = null;
		for (String str : searchStrs) {
			IMatch m2 = smm.find(join, pos);
			if (m != null) {
				CheckTools.check(m2.getStart() == m.getEnd() + 1 && m2.getLength() == str.length());
			}
			m = m2;
			pos = m2.getEnd();
		}

		LOG.info("");
	}

	void doTestStringsTreeMatcher(IList<String> searchStrs, String input) {
		IList<String> strs = GapList.create("abc");
		//IList<String> strs = GapList.create("abc", "abcd");
		StringsMatcher sm = StringsMatcher.builder().setSearchStrs(strs).setUseTree(true).build();
		StringsMatcher.StringsTreeCharMatcher sm2 = (StringsTreeCharMatcher) sm;
		LOG.info("{}\n{}", sm2.printSearchStrings(), sm2.printTree());
		IMatch m = sm.find("ab");
		LOG.info("{}", m);
	}

	void test(Class<?> clazz) {
		JmhBenchmarkCreator jbc = new JmhBenchmarkCreator();
		jbc.setTestClass(clazz);
		jbc.setRunTest(true);
		jbc.setRunBenchmark(true);
		jbc.setRunBenchmarkFast(true);
		jbc.setJavaVersions(JavaVersion.JAVA_17);

		// Method MyTest.init(int base) annotated with @TestInit will be called with argument 10.
		// Therefore the reported results wil be 10/11 instead of 0/1.
		Object[] p1 = new Object[] { 10 };
		Object[] p2 = new Object[] { 11 };
		IList<Object[]> ps = GapList.create(p1, p2);
		jbc.setTestInit(ps);
		//jbc.setTestInit(10);

		jbc.run();
	}

	void testAhoCorasick() {
		Trie trie = Trie.builder()
				.addKeyword("hers")
				.addKeyword("his")
				.addKeyword("she")
				.addKeyword("he")
				.build();
		Collection<Emit> emits = trie.parseText("ushers");
		LOG.info("{}", emits);
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.matcher.StringsMatcher$StringsSimpleMatcher", traceMethod = "indexOf")
	public void testIndexOfStrings() {
		new StringsSimpleMatcher(GapList.create("cd", "bc")).indexOf("abcd");
		new StringsSimpleMatcher(GapList.create("x")).indexOf("abcd");
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.matcher.StringsMatcher$StringsTreeMatcher", parameters = Trace.ALL_PARAMS | Trace.THIS)
	public void testFind() {
		String input = "abcde";
		StringsMatcher sm = StringsMatcher.builder().setSearchStrs("ab", "abc").setUseTree(true).build();
		@SuppressWarnings("unused")
		IMatch m = sm.find(input);

		sm = StringsMatcher.builder().setSearchStrs("ab", "abc").setSearchNotStrs("abcd").setUseTree(true).build();
		m = sm.find(input);

		sm = StringsMatcher.builder().setSearchStrs("bc", "bcd").setSearchNotStrs("abc").setUseTree(true).build();
		m = sm.find(input);

		sm = StringsMatcher.builder().setSearchStrs("bc", "bcd", "").setSearchNotStrs("abc").setUseTree(true).build();
		m = sm.find(input, 0);
		m = sm.find(input, 1);
	}

	@Trace(traceMethod = "find")
	public void testFind4() {
		String input = "abcde";
		StringsMatcher sm = StringsMatcher.builder().setSearchStrs("ab", "abc").build();
		@SuppressWarnings("unused")
		IMatch m = sm.find(input);

		sm = StringsMatcher.builder().setSearchStrs("bc", "bcd").build();
		m = sm.find(input);

		sm = StringsMatcher.builder().setSearchStrs("bc", "bcd", "").build();
		m = sm.find(input, 0);
		m = sm.find(input, 1);
	}

	@Capture
	public void testFind2() {
		doTestFind2("", "x");

		doTestFind2("abcdefg", "");
		doTestFind2("abcdefg", "a");
		doTestFind2("abcdefg", "x");
		doTestFind2("abcdefg", "bcd");

		doTestFind2("abcdefg", "bcd", "bcdx");
		doTestFind2("abcdefg", "bcd", "bcde");
		doTestFind2("abcdefg", "def", "defgh");
	}

	void doTestFind2(String input, String... matches) {
		StringsMatcher sm = StringsMatcher.builder().setSearchStrs(matches).setUseTree(true).build();
		IMatch m = sm.find(input);
		LOG.info("input: {}, matches: {} -> find: {}", new Object[] { input, matches, m });
	}

	@Capture
	public void testFind3() {
		CheckTools.check("abc".contains("") == true);
		CheckTools.check("abc".indexOf("") == 0);
		CheckTools.check("".indexOf("x") == -1);
		CheckTools.check("".indexOf("") == 0);

		doTestFind3("", "x");

		doTestFind3("abcdefg", "");
		doTestFind3("abcdefg", "a");
		doTestFind3("abcdefg", "x");
		doTestFind3("abcdefg", "bcd");

		doTestFind3("abcdefg", "bcd", "bcdx");
		doTestFind3("abcdefg", "bcd", "bcde");
		doTestFind3("abcdefg", "def", "defgh");
	}

	void doTestFind3(String input, String... matches) {
		StringsMatcher sm = new StringsMatcher.Builder().setSearchStrs(matches).build();
		IMatch m = sm.find(input);
		LOG.info("input: {}, matches: {} -> find: {}", new Object[] { input, matches, m });
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.matcher.StringsMatcher$StringsTreeMatcher")
	public void testMatchNot() {
		StringsTreeMatcher tsm = StringsTreeMatcher.of(GapList.create(), GapList.create("0abc", "abc1", "0abc1"), null);
		tsm.matchNot("abc", 0, 3);
		tsm.matchNot("0abc", 1, 3);
		tsm.matchNot("abc1", 0, 3);
		tsm.matchNot("0abc1", 1, 3);
	}

	/**
	 * Compare performance of multiple implementations for finding several strings.
	 * It shows that {@link StringsTreeMatcher} outperforms {@link org.ahocorasick.trie.Trie}.
	 */
	public static class TreeStringMatcherJmhTest {

		// Variations:
		// - input string: can be short or long
		// - find strings: there may be few or many find strings
		// - find strings: the find strings can be short or long
		// - case sensitiviy: trie converts keywords to lowercase as they are added
		// 
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

		static final Logger LOG = LogbackTools.getConsoleLogger();

		String nameSuffix;

		@TestInit
		public void init(int param) {
			LOG.info("=============================== init {}", param);
			nameSuffix = "-PARAM=" + param;
			boolean ignoreCase = false;
			initSmall(ignoreCase);
		}

		void initSmall(boolean ignoreCase) {
			IList<String> searchStrs = GapList.create("456", "abc", "def");

			CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");
			init(searchStrs, smallSource.getAll(), ignoreCase);
		}

		void initMany(boolean ignoreCase) {
			IList<String> searchStrs = GapList.create("456");
			for (int i = 0; i < 1000; i++) {
				char c = (char) i;
				searchStrs.add("x" + c + "x");
			}

			CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");
			init(searchStrs, smallSource.getAll(), ignoreCase);
		}

		void initLong(boolean ignoreCase) {
			IList<String> searchStrs = GapList.create("456");
			for (int i = 0; i < 1000; i++) {
				searchStrs.add("45" + StringTools.repeat("x", 100) + i);
			}
			CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "456");

			init(searchStrs, smallSource.getAll(), ignoreCase);
		}

		void initSingle(boolean ignoreCase) {
			String part = StringTools.repeat("x", 1000);
			String find = part + "x";
			IList<String> searchStrs = GapList.create(find);
			IList<String> inputStrs = GapList.create();
			for (int i = 0; i < 1000; i++) {
				searchStrs.add(part + "y" + i);
				inputStrs.add(i + part + "x" + i);
			}

			init(searchStrs, inputStrs, ignoreCase);
		}

		void init(IList<String> searchStrs, IList<String> inputStrs, boolean ignoreCase) {
			this.stringsMatcher = createTreeStringsMatcher(searchStrs, ignoreCase);
			this.trie = createTrie(searchStrs, ignoreCase);
			this.inputs = inputStrs;
		}

		Trie createTrie(IList<String> strs, boolean ignoreCase) {
			TrieBuilder b = Trie.builder().ignoreOverlaps();
			if (ignoreCase) {
				b.ignoreCase();
			}
			strs.forEach(s -> b.addKeyword(s));
			return b.build();
		}

		StringsMatcher createTreeStringsMatcher(IList<String> strs, boolean ignoreCase) {
			Builder smb = StringsMatcher.builder().setSearchStrs(strs).setUseTree(true);
			if (ignoreCase) {
				smb.setIgnoreCase(true);
			}
			return smb.build();
		}

		StringsMatcher stringsMatcher;
		Trie trie;

		@TestData
		IList<String> inputs;

		@TestMethod
		public int testStringsMatcher(String input) {
			return stringsMatcher.indexOf(input);
		}

		@TestMethod
		public int testTrie(String input) {
			Emit firstMatch = trie.firstMatch(input);
			return firstMatch.getStart();
		}

		//@Benchmark
		//		public int testRegexStringMatcher(MyState state) {
		//			String s = state.next();
		//			return state.getRegexStringMatcher().indexOf(s);
		//		}
		//
		//		@Benchmark
		//		public int testTreeStringsMatcher(MyState state) {
		//			String s = state.next();
		//			return state.getTreeStringsMatcher().indexOf(s);
		//		}
		//
		//		//@Benchmark
		//		public int testStringUtils(MyState state) {
		//			String s = state.next();
		//			return state.getStringUtils().indexOf(s);
		//		}

		//

		//			public enum SourceMode {
		//				SMALL,
		//				MEDIUM,
		//				LARGE
		//			}
		//
		//			//@Param({ "SMALL", "MEDIUM", "LARGE" })
		//			@Param({ "MEDIUM" })
		//			SourceMode sourceMode;
		//
		//			CyclicSource<String> smallSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 10 + i) + "abc");
		//			CyclicSource<String> mediumSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 1_000 + i) + "abc");
		//			CyclicSource<String> largeSource = new CyclicSource<>(10, i -> StringTools.repeat("-", 1_000_000 + i) + "abc");
		//			IList<CyclicSource<String>> sources = GapList.create(smallSource, mediumSource, largeSource);
		//
		//			String next() {
		//				int index = sourceMode.ordinal();
		//				CyclicSource<String> source = sources.get(index);
		//				return source.next();
		//			}
		//
		//			IList<String> finds1 = GapList.create("abc", "ab");
		//			IList<String> finds2 = GapList.create();
		//			{
		//				for (int i = 0; i < 1000; i++) {
		//					finds2.add("def" + i);
		//				}
		//				finds2.addArray("abc", "ab");
		//			}
		//
		//			public enum FindMode {
		//				FEW,
		//				MANY
		//			}
		//
		//			//@Param({ "FEW", "MANY" })
		//			@Param({ "MANY" })
		//			FindMode findMode;
		//
		//			IList<StringsMatcher> sm = GapList.create(createStringsMatcher(finds1), createStringsMatcher(finds2));
		//			IList<StringsMatcher> tsm = GapList.create(createTreeStringsMatcher(finds1), createTreeStringsMatcher(finds2));
		//			IList<RegexStringMatcher> rsm = GapList.create(createRegexStringMatcher(finds1), createRegexStringMatcher(finds2));
		//			IList<Trie> trie = GapList.create(createTrie(finds1), createTrie(finds2));
		//
		//			interface StringUtilsIndexOfAny {
		//				int indexOf(final CharSequence cs);
		//			}
		//
		//			IList<StringUtilsIndexOfAny> sus = GapList.create(createStringUtils(finds1), createStringUtils(finds2));
		//
		//			StringUtilsIndexOfAny getStringUtils() {
		//				return sus.get(findMode.ordinal());
		//			}
		//
		//			StringsMatcher getStringsMatcher() {
		//				return sm.get(findMode.ordinal());
		//			}
		//
		//			StringsMatcher getTreeStringsMatcher() {
		//				return tsm.get(findMode.ordinal());
		//			}
		//
		//			RegexStringMatcher getRegexStringMatcher() {
		//				return rsm.get(findMode.ordinal());
		//			}
		//
		//			Trie getTrie() {
		//				return trie.get(findMode.ordinal());
		//			}
		//
		//			StringUtilsIndexOfAny createStringUtils(IList<String> strs) {
		//				return new StringUtilsIndexOfAny() {
		//					@Override
		//					public int indexOf(CharSequence cs) {
		//						return StringUtils.indexOfAny(cs, strs.toArray(String.class));
		//					};
		//				};
		//			}
		//
		//			StringsMatcher createStringsMatcher(IList<String> strs) {
		//				//return new StringsMatcher.Builder().setSearchStrs(strs).build();
		//				return new StringsMatcher.Builder().setSearchStrs(strs).setIgnoreCase(true).build();
		//			}
		//
		//			RegexStringMatcher createRegexStringMatcher(IList<String> strs) {
		//				String regex = StringTools.join(strs, "|");
		//				return new RegexStringMatcher().setPattern(regex);
		//			}

	}

}
