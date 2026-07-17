package org.magicwerk.strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.IterateExecutor;
import org.magicwerk.brownies.core.reflect.ReflectReflection;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.matcher.AnchoredStringMatcher;
import org.magicwerk.strings.matcher.ConditionalStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.MultiStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.RepeatedStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

public class JavaUtilRegexTest extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final ReflectReflection reflectReflection = new ReflectReflection();

	public static void main(String[] args) {
		new JavaUtilRegexTest().run();
	}

	void run() {
		runManual();
		//runTests();
		//runBenchmarks();
		//test();
	}

	void runManual() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(true);
			jbc.setRunBenchmarkFast(false);
			jbc.setRunBenchmarkShowHtml(true);
			//jbc.setRunVerify(false);
			//jbc.setRunBenchmarkShowBytecode(true);
			//jbc.setRunBenchmarkShowSource(true);

			// Analyze JIT
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation("hotspot.log")));

			// Analyze Allocation: JmhAllocationJfrObserverState needs at least Java 16
			//jbc.setBenchmarkStateClass(JmhAllocationJfrObserverState.class);

			//jbc.setRunTime(1000);
			//jbc.setJavaVersions(JavaVersion.JAVA_17);
			//jbc.setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		});

		//test(AnchoredStringSearch_Test.class);
		test(StringsMatcherNot_Test.class);
	}

	public void runBenchmarks() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(true);
			jbc.setRunBenchmarkFast(false);
			jbc.setRunBenchmarkShowHtml(false);

			jbc.setJavaVersions(JavaVersion.JAVA_11);
		});

		analyzeTestClasses(getClass());
		//testClasses = testClasses.filter(c -> c.getSimpleName().startsWith("Unwrap"));

		IList<TestExecution> tes = GapList.create();
		IterateExecutor.forEach(testClasses, cd -> {
			Class<?> c = ReflectTools.getClass(cd.getName());
			TestExecution te = test(c);
			tes.add(te);
		});

		new BenchmarkTestHelper().setBaseImplementation("testJavaUtilRegex").showReport(tes);
	}

	/**
	 * Show that matching a literal regex is about 7 times slower than matching a literal string
	 * and consumes memory on each call.
	 */
	public static class RegexPerformanceJmhTest extends StringJmhBenchmark {

		public RegexPerformanceJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "abc";
			Pattern FIND_RE = Pattern.compile(FIND);
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		@Benchmark
		public int testFindString(MyState state) {
			String str = state.strings.next();
			int index = str.indexOf(state.FIND);
			return index;
		}

		@Benchmark
		public int testFindRegex(MyState state) {
			String str = state.strings.next();
			Matcher matcher = state.FIND_RE.matcher(str);
			matcher.find();
			return matcher.start();
		}
	}

	/**
	 * Show that
	 * - StringsMatcher outperforms regex by factor 1.5 // TODO should be faster
	 * - StringsMatcher does not need any memory
	 * - StringMatcher outperforms StringsMatcher by factor 3
	 */
	public static class StringsMatcherNot_Test {

		public static final String JAVADOC_COMMENT_START = "/**";

		// Note: /**/ is not a Javadoc, but block comment 
		// Negative lookahead (?!): you want to match something not followed by something else
		static final String JAVADOC_COMMENT_START_PATTERN = "/\\*\\*(?!/)";

		static final IStringMatcher regexMatcher = new RegexStringMatcher().setPattern(JAVADOC_COMMENT_START_PATTERN);

		static final IStringMatcher stringsMatcher = StringsMatcher.builder().setSearchStrs("/**").setSearchNotStrs("/**/").build();

		static final IStringMatcher stringMatcher = StringMatcher.of(JAVADOC_COMMENT_START);

		static final IStringMatcher conditionalStringMatcher = new ConditionalStringMatcher(StringMatcher.of(JAVADOC_COMMENT_START), m -> {
			return StringTools.charAt(m.getInput(), m.getEnd()) != '/';
		});

		@TestData
		IList<String> inputs = GapList.create("abc /** comment */ def", "abc /**/ def", "abc def");

		@TestMethod
		public int testMagicStrings(CharSequence str) {
			return stringsMatcher.indexOf(str);
		}

		@TestMethod
		public int testConditionalStringMatcher(CharSequence str) {
			return conditionalStringMatcher.indexOf(str);
		}

		@TestMethod
		public int testRegexStringMatcher(CharSequence str) {
			return regexMatcher.indexOf(str);
		}

		//@TestMethod
		public int testStringMatcher(CharSequence str) {
			return stringMatcher.indexOf(str);
		}

	}

	/** 
	 * Show that
	 * - AnchoredStringMatcher outperforms a regex by factor 8 (indexOf())
	 * - RegexStringMatcher is about as fast than dealing with Pattern directly
	 */
	public static class AnchoredStringSearch_Test {
		String str = "01";

		StringMatcher sm = StringMatcher.of(str);
		AnchoredStringMatcher sm0 = AnchoredStringMatcher.of(sm, -1);
		//AnchoredStringMatcher sm0 = AnchoredStringMatcher.of(sm, 0);

		String regex = "(01)$";
		RegexStringMatcher sm1 = new RegexStringMatcher().setPattern(regex);

		Pattern pattern = Pattern.compile(regex);

		@TestData
		IList<String> inputs = GapList.create("0101abc", "abc0101");

		@TestMethod
		public int testMagicStrings(CharSequence str) {
			return sm0.indexOf(str);
		}

		@TestMethod
		public int testRegexStringMatcher(CharSequence str) {
			return sm1.indexOf(str);
		}

		@TestMethod
		public int testJavaUtilRegex(CharSequence str) {
			Matcher matcher = pattern.matcher(str);
			return (matcher.find()) ? matcher.start() : -1;
		}
	}

	/** 
	 * Show that
	 * - RepeatedStringMatcher outperforms a regex by factor 2 (find()) to 4 (indexOf())
	 * - RegexStringMatcher is about 10% slower than dealing with Pattern directly
	 */
	public static class RepeatedStringSearch_Test {

		String find = "01";
		StringMatcher sm = StringMatcher.of(find);
		RepeatedStringMatcher sm0 = RepeatedStringMatcher.of(sm, false);

		String regex = "(01)+";
		RegexStringMatcher rsm = new RegexStringMatcher().setPattern(regex);

		Pattern pattern = Pattern.compile(regex);

		@TestData
		IList<String> inputs = GapList.create("a01b", "a0101b", "a010101c");

		@TestMethod
		public int testMagicStrings(CharSequence str) {
			return sm0.indexOf(str);
		}

		@TestMethod
		public int testRegexStringMatcher(CharSequence str) {
			return rsm.indexOf(str);
		}

		@TestMethod
		public int testJavaUtilRegex(CharSequence str) {
			Matcher matcher = pattern.matcher(str);
			return (matcher.find()) ? matcher.start() : -1;
		}
	}

	/** Show that StringMatcher outperforms regex by factor 2 */
	public static class MultiStringSearch_Test {

		String s0 = "ab";
		String s1 = "cd";

		StringMatcher sm0 = StringMatcher.of(s0);
		StringMatcher sm1 = StringMatcher.of(s1);
		MultiStringMatcher msm = new MultiStringMatcher(sm0, sm1);

		Pattern p = Pattern.compile("ab|cd");
		RegexStringMatcher rsm = new RegexStringMatcher().setPattern(p);

		StringsMatcher sm = StringsMatcher.builder().setSearchStrs(s0, s1).build();

		@TestData
		IList<String> inputs = GapList.create("-ab-xx-", "-xx-cd-");

		@TestMethod
		public int testMultiStringMatcher(CharSequence str) {
			return msm.indexOf(str);
		}

		@TestMethod
		public int testMagicStrings(CharSequence str) {
			return sm.indexOf(str);
		}

		@TestMethod
		public int testRegexStringMatcher(CharSequence str) {
			return rsm.indexOf(str);
		}

		@TestMethod
		public int testJavaUtilRegex(CharSequence str) {
			Matcher m = p.matcher(str);
			return (m.find()) ? m.start() : -1;
		}
	}

}
