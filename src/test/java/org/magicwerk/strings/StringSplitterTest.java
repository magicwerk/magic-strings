package org.magicwerk.strings;

import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magicwerk.brownies.core.print.PrintTools2;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestCompare;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.StringSplitter.SeparatorMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.helper.PrintTools;
import org.magicwerk.strings.helper.CheckTools.Check;
import org.magicwerk.strings.objects.Pair;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.Splitter;

/**
 * Test of class {@link StringSplitter}.
 */
public class StringSplitterTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringSplitterTest().run();
	}

	void run() {
		//testSubstringBeforeAfter();
		//testBeforeAfter();
		//testSplit();
		testSplitDetector();
		//testSplit2();

		//runBenchmarks();
		//SubstringBeforeAfterTestJmh.test();
	}

	static final char separator = '-';

	public void testSplitDetector() {
		BiPredicate<Character, Character> p = (c0, c1) -> c0 == c1;
		StringSplitter ss = StringSplitter.build(b -> b.setSplitDetector(p));
		IList<String> strs = ss.split("abbccc");
		LOG.info("{}", strs);
	}

	@Capture
	public void testSplitSimple() {
		StringSplitter ss = StringSplitter.builder().setFindChar(separator).build();
		IList<String> strs = ss.split("a-b");
		LOG.info("{}", strs);
	}

	@Capture
	public void testSubstringBeforeAfter() {
		// TODO check with string which is not found
		String str = "abc-def-ghi";
		String sep = "-";

		LOG.info("{}", StringUtils.substringBefore(str, sep));
		LOG.info("{}", StringSplitter.builder().setFindString(sep).build().getFirst(str));

		LOG.info("{}", StringUtils.substringAfter(str, sep));
		LOG.info("{}", StringSplitter.builder().setFindString(sep).build().splitFirst(str).getLast());

		LOG.info("{}", StringUtils.substringBeforeLast(str, sep));
		LOG.info("{}", StringSplitter.builder().setFindString(sep).build().splitLast(str).getFirst());

		LOG.info("{}", StringUtils.substringAfterLast(str, sep));
		LOG.info("{}", StringSplitter.builder().setFindString(sep).build().getLast(str));

		// substringBeforeWithSeparator
		LOG.info("{}", StringSplitter.builder().setFindString(sep).setSeparatorMode(SeparatorMode.ADD_LEFT).build().getFirst(str));
		// substringAfterWithSeparator
		LOG.info("{}", StringSplitter.builder().setFindString(sep).setSeparatorMode(SeparatorMode.ADD_RIGHT).build().splitFirst(str).getLast());
		// substringBeforeLastWithSeparator
		LOG.info("{}", StringSplitter.builder().setFindString(sep).setSeparatorMode(SeparatorMode.ADD_LEFT).build().splitLast(str).getFirst());
		// substringAfterLastWithSeparator
		LOG.info("{}", StringSplitter.builder().setFindString(sep).setSeparatorMode(SeparatorMode.ADD_RIGHT).build().getLast(str));
	}

	public static class SubstringBeforeAfterTestJmh {

		static void test() {
			Options opts = new Options().includeClass(SubstringBeforeAfterTestJmh.class);
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
			String str = "abc-def-ghi";
			String sep = "-";

			StringSplitter splitter = StringSplitter.builder().setFindString(sep).build();
		}

		@Benchmark
		public String testCommonsUtil(TestState state) {
			return StringUtils.substringBefore(state.str, state.sep);
		}

		@Benchmark
		public String testStringSplitter(TestState state) {
			return state.splitter.getFirst(state.str);
		}
	}

	// TODO align with testSplit()
	@Capture
	public void testSplit2() {
		String sep = "-";
		StringSplitter ss = StringSplitter.builder().setFindString(sep).build();

		IList<String> strs = GapList.create("a", "a-b", "a--b", "a-b-c", "a-", "-a", "-a-b", "a-b-", "-", "--", "", null);
		for (String str : strs) {
			LOG.info("{}", str);

			IList<String> split = ss.split(str);
			Pair<String> splitFirst = ss.splitFirst(str);
			Pair<String> splitLast = ss.splitLast(str);
			String first = ss.getFirst(str);
			String last = ss.getLast(str);
			String afterFirst = ss.getAfterFirst(str);
			String beforeLast = ss.getBeforeLast(str);
			LOG.info("- split: {}", split);
			LOG.info("- splitFirst/splitLast: {} / {}", splitFirst, splitLast);
			LOG.info("- first/afterFirst: {} / {}", first, afterFirst);
			LOG.info("- beforeLast/last: {} / {}", beforeLast, last);

			String splitFirst0 = (splitFirst != null) ? splitFirst.getFirst() : null;
			String splitFirst1 = (splitFirst != null) ? splitFirst.getLast() : null;
			String splitLast0 = (splitLast != null) ? splitLast.getFirst() : null;
			String splitLast1 = (splitLast != null) ? splitLast.getLast() : null;
			Check.forEqual().check(first, splitFirst0);
			Check.forEqual().check(afterFirst, splitFirst1);
			Check.forEqual().check(beforeLast, splitLast0);
			Check.forEqual().check(last, splitLast1);

			if (str == null) {
				// input is null
				CheckTools.check(
						split == null && splitFirst == null && splitLast == null && first == null && last == null && afterFirst == null && beforeLast == null);
			} else {
				// input is not null
				Check.forEqual().check(str, StringTools.join(split, sep));
				Check.forEqual().check(str, join(splitFirst.getFirst(), sep, splitFirst.getLast()));
				Check.forEqual().check(str, join(splitLast.getFirst(), sep, splitLast.getLast()));
				Check.forEqual().check(str, join(splitFirst.getFirst(), sep, splitFirst.getLast()));
				Check.forEqual().check(str, join(splitLast.getFirst(), sep, splitLast.getLast()));

				Check.forEqual().check(str, join(first, sep, afterFirst));
				Check.forEqual().check(str, join(beforeLast, sep, last));

				int numParts = split.size();
				if (numParts == 1) {
					// no split
					Check.forEqual().check(first, last);
				}
			}
		}
	}

	String join(String s0, String sep, String s1) {
		return new StringPrinter().setElemMarker(sep).addAllIf(s0, s1).toString();
	}

	@Capture
	public void testSplit() {
		StringSplitter.Builder ss = StringSplitter.builder().setIgnoreUnmatchedText(true).setFindChar(separator);

		// TODO does it make sense that first and last return the same string? 
		testSplit(ss, "a");

		testSplit(ss, "a-b");
		testSplit(ss, "a--b");
		testSplit(ss, "a-b-c");
		testSplit(ss, "a-");
		testSplit(ss, "-a");
		testSplit(ss, "-a-b");
		testSplit(ss, "a-b-");
		testSplit(ss, "-");
		testSplit(ss, "--");
		testSplit(ss, "");
	}

	void testSplit(StringSplitter.Builder ss, String str) {
		StringPrinter buf = new StringPrinter();
		buf.println("String: {}", str);

		doTestSplit(ss, SeparatorMode.REMOVE, str, buf);
		doTestSplit(ss, SeparatorMode.OWN, str, buf);
		doTestSplit(ss, SeparatorMode.ADD_LEFT, str, buf);
		doTestSplit(ss, SeparatorMode.ADD_RIGHT, str, buf);
		doTestSplit(ss, SeparatorMode.STARTS, str, buf);
		doTestSplit(ss, SeparatorMode.ENDS, str, buf);

		LOG.info("{}", buf);
	}

	void doTestSplit(StringSplitter.Builder ss, SeparatorMode sm, String str, StringPrinter buf) {
		buf.indent();
		buf.println("separatorMode: {}", sm);
		ss.setSeparatorMode(sm);
		doTestSplit(ss, str, buf);
		buf.unindent();
	}

	void doTestSplit(StringSplitter.Builder ssb, String str, StringPrinter buf) {
		StringSplitter ss = ssb.build();
		IList<String> split = ss.split(str);
		Pair<String> splitFirst = ss.splitFirst(str);
		Pair<String> splitLast = ss.splitLast(str);
		String getFirst = ss.getFirst(str);
		String getLast = ss.getLast(str);
		String afterFirst = ss.getAfterFirst(str);
		String beforeLast = ss.getBeforeLast(str);

		CheckTools.check(ObjectTools.equals(afterFirst, splitFirst.getLast()));
		CheckTools.check(ObjectTools.equals(beforeLast, splitLast.getFirst()));

		SeparatorMode sm = ssb.separatorMode;
		if (sm != SeparatorMode.STARTS && sm != SeparatorMode.ENDS) {
			CheckTools.check(getFirst.equals(split.getFirst()));
			CheckTools.check(getLast.equals(split.getLast()));
			CheckTools.check(getFirst.equals(splitFirst.getFirst()));
			CheckTools.check(getLast.equals(splitLast.getLast()));

			ssb.setSeparatorMode(SeparatorMode.OWN);
			ss = ssb.build();
			IList<String> splitOwn = ss.split(str);
			ssb.setSeparatorMode(sm);

			if (sm != SeparatorMode.OWN) {
				// splitOwn contains text-separator-text-separator-text...
				int size = splitOwn.size() / 2 + 1;
				CheckTools.check(split.size() == size);
			}

			if (splitFirst != null && splitFirst.getLast() != null) {
				int d1 = (sm == SeparatorMode.REMOVE || sm == SeparatorMode.ADD_LEFT) ? 1 : 0;
				CheckTools.check(splitFirst.getLast().equals(StringTools.join(splitOwn.getAll(1 + d1, splitOwn.size() - 1 - d1))));
			}

			if (splitLast != null && splitLast.getFirst() != null) {
				int d2 = (sm == SeparatorMode.REMOVE || sm == SeparatorMode.ADD_RIGHT) ? 1 : 0;
				CheckTools.check(splitLast.getFirst().equals(StringTools.join(splitOwn.getAll(0, splitOwn.size() - 1 - d2))));
			}
		}
		ssb.setCombineSeparators(true);
		ss = ssb.build();
		IList<String> split2 = ss.split(str);
		Pair<String> splitFirst2 = ss.splitFirst(str);
		Pair<String> splitLast2 = ss.splitLast(str);
		String getFirst2 = ss.getFirst(str);
		String getLast2 = ss.getLast(str);
		ssb.setCombineSeparators(false);

		String splitFirstGetFirst = PrintTools.toString(splitFirst.getFirst(), "");
		String splitFirstGetLast = PrintTools.toString(splitFirst.getLast(), "");
		String splitLastGetFirst = PrintTools.toString(splitLast.getFirst(), "");
		String splitLastGetLast = PrintTools.toString(splitLast.getLast(), "");
		if (sm != SeparatorMode.REMOVE) {
			CheckTools.check(str.equals(splitFirstGetFirst + splitFirstGetLast));
			CheckTools.check(str.equals(splitLastGetFirst + splitLastGetLast));
		} else {
			String s = (split.size() > 1) ? String.valueOf(separator) : "";
			CheckTools.check(str.equals(splitFirstGetFirst + s + splitFirstGetLast));
			CheckTools.check(str.equals(splitLastGetFirst + s + splitLastGetLast));
		}

		buf.indent();
		print(buf, "split", split, split2);
		print(buf, "splitFirst", splitFirst, splitFirst2);
		print(buf, "splitLast", splitLast, splitLast2);
		print(buf, "getFirst", getFirst, getFirst2);
		print(buf, "getLast", getLast, getLast2);
		buf.unindent();
	}

	void print(StringPrinter buf, String key, Object obj, Object obj2) {
		if (ObjectTools.equals(obj, obj2)) {
			buf.println("{}= {}", key, PrintTools2.print(obj));
		} else {
			buf.println("{}= {} ({})", key, PrintTools2.print(obj), PrintTools2.print(obj2));
		}
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringSplitterTestSet1a.class, StringSplitterTestSet1b.class, StringSplitterTestSet2a.class);
	}

	/**
	 * Test/benchmark of {@link StringSplitterTestSet}.
	 */
	public static class StringSplitterTestSetBase {

		char sep;
		StringSplitter stringSplitter;
		Splitter splitter;

		@TestMethod
		public IList<String> testStringSplitter(String str) {
			return stringSplitter.split(str);
		}

		@TestMethod
		public List<String> testGuavaStrings(String str) {
			return splitter.splitToList(str);
		}

		@TestMethod
		public String[] testCommonsStringUtils(String str) {
			return StringUtils.split(str, sep);
		}

		@TestCompare
		Object compareResult(Object result) {
			if (result instanceof String[]) {
				return GapList.create((String[]) result);
			} else {
				return result;
			}
		}
	}

	public static class StringSplitterTestSet1 extends StringSplitterTestSetBase {
		{
			sep = '-';
			stringSplitter = StringSplitter.builder().setFindChar(sep).build();
			splitter = Splitter.on(sep);
		}
	}

	public static class StringSplitterTestSet2 extends StringSplitterTestSetBase {
		{
			sep = '-';
			stringSplitter = StringSplitter.builder().setFindChar(sep).setCombineSeparators(true).build();
			splitter = Splitter.on(sep).omitEmptyStrings();
		}
	}

	public static class StringSplitterTestSet1a extends StringSplitterTestSet1 {
		@TestData
		IList<String> inputs0 = GapList.create("a-c");
	}

	public static class StringSplitterTestSet1b extends StringSplitterTestSet1 {

		@TestData
		IList<String> inputs0 = GapList.create("a--d");

		@Override // disable
		public String[] testCommonsStringUtils(String str) {
			throw CheckTools.error();
		}
	}

	public static class StringSplitterTestSet2a extends StringSplitterTestSet2 {

		@TestData
		IList<String> inputs0 = GapList.create("a--d");
	}

	void testSplitterChar(String str, char sep) {
		StringSplitter sr = StringSplitter.builder().setFindChar(sep).setCombineSeparators(true).build();
		IList<String> s0 = sr.split(str);
		IList<String> s1 = GapList.create(StringUtils.split(str, sep));
		CheckTools.check(ObjectTools.equals(s0, s1));

		List<String> s2 = Splitter.on(sep).omitEmptyStrings().splitToList(str);
		CheckTools.check(ObjectTools.equals(s0, s2));
	}

}
