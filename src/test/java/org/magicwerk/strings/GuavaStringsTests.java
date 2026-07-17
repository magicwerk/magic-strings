package org.magicwerk.strings;

import java.util.List;

import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.IterateExecutor;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmark;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringJoiner;
import org.magicwerk.strings.StringRemover;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.StringRoots;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.StringTrimmer;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringTrimmer.CollapseMode;
import org.magicwerk.strings.StringTrimmer.TrimMode;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * Class {@link GuavaStringsTests} compares magic-strings with methods in Guava strings.
 * <p>
 * https://github.com/google/guava/wiki/stringsexplained
 */
public class GuavaStringsTests extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new GuavaStringsTests().run();
	}

	void run() {
		//runManual();
		//runTests();
		//runBenchmarks();
		test();
	}

	void test() {
		new RemoveFromJmhTest().test();
	}

	void runManual() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(true);
			jbc.setRunBenchmarkFast(false);
			jbc.setRunBenchmarkShowHtml(true);
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

		test(CommonSuffix_Test.class);
		//test(IndexIn_Char_Test.class);
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

		new BenchmarkTestHelper().setBaseImplementation("testGuavaStrings").showReport(tes);
	}

	@Override
	@Capture
	public void runTests() {
		super.runTests();
	}

	// commonPrefix / commonSuffix

	//public static String commonPrefix(CharSequence a, CharSequence b)
	//public static String commonSuffix(CharSequence a, CharSequence b)

	public static class CommonPrefixSuffix_TestBase {
		@TestData
		IList<String> inputs0 = GapList.create("aa", "ab", "b", "ac", CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_0 + "a");
		@TestData
		IList<String> inputs1 = GapList.create("aa", "ac", "c", "bc", CodePointToolsTest.STRING_SURROGATE_01, CodePointToolsTest.STRING_SURROGATE_0 + "b");

	}

	public static class CommonPrefix_Test extends CommonPrefixSuffix_TestBase {

		StringRoots roots = StringRoots.build(b -> b);

		@TestMethod
		public String testMagicStrings(String str0, String str1) {
			return roots.getCommonRoot(str0, str1);
		}

		@TestMethod
		public String testGuavaStrings(String str0, String str1) {
			return Strings.commonPrefix(str0, str1);
		}
	}

	public static class CommonSuffix_Test extends CommonPrefixSuffix_TestBase {

		StringRoots roots = StringRoots.build(b -> b.setReverse(true));

		@TestMethod
		public String testMagicStrings(String str0, String str1) {
			return roots.getCommonRoot(str0, str1);
		}

		@TestMethod
		public String testGuavaStrings(String str0, String str1) {
			return Strings.commonSuffix(str0, str1);
		}
	}

	// boolean matchesAllOf(CharSequence sequence)

	public static class MatchesAllOf_Test extends Find_TestBase {
		{
			setInputs(inputChange, findAnyChar + findAnyChar);
		}

		CharPredicate cp = CharPredicates.oneOf(findAnyChar).negate();
		StringFinder finder = StringFinder.builder().setFindCharPredicate(cp).build();

		@TestMethod
		public boolean testMagicStrings2(String input) {
			return !finder.contains(input);
		}

		@TestMethod
		public boolean testGuavaStrings(String input) {
			return cm.matchesAllOf(input);
		}
	}

	// boolean matchesAnyOf(CharSequence sequence)

	public static class MatchesAnyOf_Test extends Find_TestBase {

		@TestMethod
		public boolean testMagicStrings(String input) {
			return finder.contains(input);
		}

		@TestMethod
		public boolean testGuavaStrings(String input) {
			return cm.matchesAnyOf(input);
		}
	}

	// boolean matchesNoneOf(CharSequence sequence)

	public static class MatchesNoneOf_Test extends Find_TestBase {

		@TestMethod
		public boolean testMagicStrings(String input) {
			return !finder.contains(input);
		}

		@TestMethod
		public boolean testGuavaStrings(String input) {
			return cm.matchesNoneOf(input);
		}
	}

	static class Find_TestBase extends CharMatcher_TestBase {

		StringFinder finder = StringFinder.builder().setFindAnyChar(findAnyChar).build();

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		{
			setInputs(inputChange, inputNoChange);
		}
	}

	// int indexIn(CharSequence sequence)

	public static class IndexIn_Test extends Find_TestBase {

		@TestMethod
		public int testMagicStrings(String input) {
			return finder.indexOf(input);
		}

		@TestMethod
		public int testGuavaStrings(String input) {
			return cm.indexIn(input);
		}
	}

	public static class IndexIn_Char_Test extends Find_TestBase {

		StringFinder finder = StringFinder.builder().setFindChar(findChar).build();

		CharMatcher cm = CharMatcher.is(findChar);

		@TestMethod
		public int testMagicStrings(String input) {
			return finder.indexOf(input);
		}

		@TestMethod
		public int testGuavaStrings(String input) {
			return cm.indexIn(input);
		}
	}

	// int indexIn(CharSequence sequence, int start)
	public static class IndexIn_Start_Test extends Find_TestBase {

		int start = 5;

		@TestMethod
		public int testMagicStrings(String input) {
			return finder.indexOf(input, start);
		}

		@TestMethod
		public int testGuavaStrings(String input) {
			return cm.indexIn(input, start);
		}
	}

	// int lastIndexIn(CharSequence sequence)
	public static class LastIndexIn_Test extends Find_TestBase {

		StringFinder finder = StringFinder.builder().setFindAnyChar(findAnyChar).setReverse(true).build();

		@TestMethod
		public int testMagicStrings(String input) {
			return finder.indexOf(input);
		}

		@TestMethod
		public int testGuavaStrings(String input) {
			return cm.lastIndexIn(input);
		}
	}

	// int countIn(CharSequence sequence)

	public static class CountIn_Test extends Find_TestBase {

		@TestMethod
		public int testMagicStrings(String input) {
			return finder.count(input);
		}

		@TestMethod
		public int testGuavaStrings(String input) {
			return cm.countIn(input);
		}
	}

	//

	static class CharMatcher_TestBase {
		char findChar = 'x';
		String findAnyChar = "xyz";
		char replaceChar = '-';
		String replaceStr = "<>";

		String inputChange = "xyz012xyz345xyz";
		String inputNoChange = "012345";
		String inputNoChange2 = "xyx";

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			this.inputs = GapList.create(input);
		}

		void setInputs(String... inputs) {
			this.inputs = GapList.create(inputs);
		}
	}

	// String retainFrom(CharSequence sequence)

	static class RetainFrom_TestBase extends CharMatcher_TestBase {

		StringRemover remover = StringRemover.build(b -> b.setFindAnyChar(findAnyChar).setRetain(true));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.retainFrom(input);
		}
	}

	public static class RetainFrom_Change_Test extends RetainFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class RetainFrom_NoChange_Test extends RetainFrom_TestBase {
		{
			setInput(inputNoChange2);
		}
	}

	// String removeFrom(CharSequence sequence)

	static class RemoveFrom_TestBase extends CharMatcher_TestBase {

		StringRemover remover = StringRemover.build(b -> b.setFindAnyChar(findAnyChar));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.removeFrom(input);
		}
	}

	public static class RemoveFrom_Change_Test extends RemoveFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class RemoveFrom_NoChange_Test extends RemoveFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String collapseFrom(CharSequence sequence, char replacement)

	static class CollapseFrom_TestBase extends CharMatcher_TestBase {

		StringTrimmer trimmer = StringTrimmer.build(b -> b.setFindAnyChar(findAnyChar).setCollapseMode(CollapseMode.BODY).setCollapseChar(replaceChar));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.collapseFrom(input, replaceChar);
		}
	}

	public static class CollapseFrom_Change_Test extends CollapseFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class CollapseFrom_NoChange_Test extends CollapseFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String trimFrom(CharSequence sequence)

	static class TrimFrom_TestBase extends CharMatcher_TestBase {
		StringTrimmer trimmer = StringTrimmer.build(b -> b.setFindAnyChar(findAnyChar).setTrimMode(TrimMode.HEAD_TAIL));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.trimFrom(input);
		}
	}

	public static class TrimFrom_Change_Test extends TrimFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class TrimFrom_NoChange_Test extends TrimFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String trimLeadingFrom(CharSequence sequence)

	static class TrimLeadingFrom_TestBase extends CharMatcher_TestBase {
		StringTrimmer trimmer = StringTrimmer.build(b -> b.setFindAnyChar(findAnyChar).setTrimMode(TrimMode.HEAD));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.trimLeadingFrom(input);
		}
	}

	public static class TrimLeadingFrom_Change_Test extends TrimLeadingFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class TrimLeadingFrom_NoChange_Test extends TrimLeadingFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String trimTrailingFrom(CharSequence sequence)

	static class TrimTrailingFrom_TestBase extends CharMatcher_TestBase {
		StringTrimmer trimmer = StringTrimmer.build(b -> b.setFindAnyChar(findAnyChar).setTrimMode(TrimMode.TAIL));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.trimTrailingFrom(input);
		}
	}

	public static class TrimTrailingFrom_Change_Test extends TrimTrailingFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class TrimTrailingFrom_NoChange_Test extends TrimTrailingFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String trimAndCollapseFrom(CharSequence sequence, char replacement)

	static class TrimAndCollapseFrom_TestBase extends CharMatcher_TestBase {
		StringTrimmer trimmer = StringTrimmer
				.build(b -> b.setFindAnyChar(findAnyChar).setTrimMode(TrimMode.HEAD_TAIL).setCollapseMode(CollapseMode.BODY).setCollapseChar(replaceChar));

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.trimAndCollapseFrom(input, replaceChar);
		}
	}

	public static class TrimAndCollapseFrom_Change_Test extends TrimAndCollapseFrom_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class TrimAndCollapseFrom_NoChange_Test extends TrimAndCollapseFrom_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String replaceFrom(CharSequence sequence, CharSequence replacement)

	static class ReplaceFrom_String_TestBase extends CharMatcher_TestBase {
		StringReplacer replacer = StringReplacer.builder().setFindAnyChar(findAnyChar).setReplaceString(replaceStr).build();

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.replaceFrom(input, replaceStr);
		}
	}

	public static class ReplaceFrom_String_Change_Test extends ReplaceFrom_String_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class ReplaceFrom_String_NoChange_Test extends ReplaceFrom_String_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	// String replaceFrom(CharSequence sequence, char replacement)

	static class ReplaceFrom_Char_TestBase extends CharMatcher_TestBase {
		StringReplacer replacer = StringReplacer.builder().setFindAnyChar(findAnyChar).setReplaceChar(replaceChar).build();

		CharMatcher cm = CharMatcher.anyOf(findAnyChar);

		@TestMethod
		public CharSequence testMagicStrings(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testGuavaStrings(String input) {
			return cm.replaceFrom(input, replaceChar);
		}
	}

	public static class ReplaceFrom_Char_Change_Test extends ReplaceFrom_Char_TestBase {
		{
			setInput(inputChange);
		}
	}

	public static class ReplaceFrom_Char_NoChange_Test extends ReplaceFrom_Char_TestBase {
		{
			setInput(inputNoChange);
		}
	}

	//

	public static class StringRemoveFromJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			final String removeChars = "x";
			//final String removeChars = "xy";
			//final String removeChars = "xyz";
			//final CharPredicate cp = CharPredicates.oneOf(removeChars);
			final CharPredicate cp = CharPredicates.equals('x');
			final StringRemover stringRemover = StringRemover.builder().setFindCharPredicate(cp).build();

			final CharMatcher cm = CharMatcher.anyOf(removeChars);
			CyclicSource<String> source = new CyclicSource<>("xyabcyz", "xyaxcyz");
		}

		@Benchmark
		public Object testStringRemover(MyState state) {
			return state.stringRemover.remove(state.source.next());
		}

		@Benchmark
		public Object testGuavaRemoveFrom(MyState state) {
			return state.cm.removeFrom(state.source.next());
		}
	}

	static class Joiner_TestBase {
		char sep = '-';
		String nullString = "NIL";
	}

	public static class Joiner_Test extends Joiner_TestBase {

		StringJoiner joiner = StringJoiner.builder().setJoin(sep).build();;

		// Guava Joiner fails on null without the useForNull() option
		Joiner joiner2 = Joiner.on(sep);

		@TestData
		IList<IList<String>> inputs = GapList.create();
		{
			inputs.add(GapList.create("a", "b", "c"));
		}

		@TestMethod
		public String testMagicStrings(List<String> input) {
			return joiner.joinStrings(input);
		}

		@TestMethod
		public String testGuavaStrings(List<String> input) {
			return joiner2.join(input);
		}
	}

	public static class Joiner_UseForNull_Test extends Joiner_TestBase {

		StringJoiner joiner = StringJoiner.builder().setNullString(nullString).setJoin(sep).build();;

		Joiner joiner2 = Joiner.on(sep).useForNull(nullString);

		@TestData
		IList<IList<String>> inputs = GapList.create();
		{
			inputs.add(GapList.create("a", "b", "c"));
			inputs.add(GapList.create("a", null, "c"));
		}

		@TestMethod
		public String testMagicStrings(List<String> input) {
			return joiner.joinStrings(input);
		}

		@TestMethod
		public String testGuavaStrings(List<String> input) {
			return joiner2.join(input);
		}
	}

	public static class Joiner_SkipNulls_Test extends Joiner_TestBase {

		StringJoiner joiner = StringJoiner.builder().setJoin(sep).build();;

		Joiner joiner2 = Joiner.on(sep).skipNulls();

		@TestData
		IList<IList<String>> inputs = GapList.create();
		{
			inputs.add(GapList.create("a", "b", "c"));
			inputs.add(GapList.create("a", null, "c"));
		}

		@TestMethod
		public String testMagicStrings(List<String> input) {
			return joiner.joinStrings(input);
		}

		@TestMethod
		public String testGuavaStrings(List<String> input) {
			return joiner2.join(input);
		}
	}

	static class Splitter_TestBase {
		char sep = ',';

		@TestData
		IList<String> inputs = GapList.create("ab", "a,b", "a,,b");
	}

	public static class Splitter_Test extends Splitter_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindChar(sep).build();

		Splitter splitter2 = Splitter.on(sep);

		@TestMethod
		public List<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public List<String> testGuavaStrings(String input) {
			return splitter2.splitToList(input);
		}
	}

	public static class Splitter_OmitEmpty_Test extends Splitter_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindChar(sep).setCombineSeparators(true).build();

		Splitter splitter2 = Splitter.on(sep).omitEmptyStrings();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public List<String> testGuavaStrings(String input) {
			return splitter2.splitToList(input);
		}
	}

	void testSplitterIterable() {
		// Splitter returns an Iterable
		Iterable<String> ss = Splitter.on(',').split("a,b");
		IList<String> ss0 = GapList.create();

		ss.forEach(ss0::add);
		IList<String> ss1 = StringTools.split("a,b", ',');
		CheckTools.check(ss0.equals(ss1));
	}

	/**
	 * Show that Guava's removeFrom is faster than StringRemover except case NoRemoveFromCharSeq
	 * (in this case removeFrom() has to create a result String from the input CharSequence which is not necessary for the StringRemover). 
	 */
	public static class RemoveFromJmhTest extends JmhBenchmark {
		{
			setEqualsWithToString(true);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			public enum MyData {
				RemoveFromString(new CyclicSource<>("a0b1c", "d0e1f")),
				RemoveFromCharSeq(new CyclicSource<>(new GapString("a0b1c"), new GapString("d0e1f"))),
				NoRemoveFromString(new CyclicSource<>("abc", "def")),
				NoRemoveFromCharSeq(new CyclicSource<>(new GapString("abc"), new GapString("def")));

				CyclicSource<CharSequence> strs;

				MyData(CyclicSource<CharSequence> strs) {
					this.strs = strs;
				}
			}

			@Param
			MyData data;

			String chars = "012";
			CharPredicate cp = CharPredicates.oneOf(chars);
			CharMatcher cm = CharMatcher.anyOf(chars);
			StringRemover sr = StringRemover.builder().setFindAnyChar(chars).build();
			//StringTrimmer sr = StringTrimmer.builder().setCharPredicate(cp).build();
		}

		//		@Benchmark
		//		public String testStringTools(MyState state) {
		//			return StringTools.removeChar(state.data.strs.next(), state.cp);
		//		}

		@Benchmark
		public CharSequence testStringRemover(MyState state) {
			CharSequence str = state.data.strs.next();
			return state.sr.remove(str);
		}

		@Benchmark
		public CharSequence testGuavaRemoveFrom(MyState state) {
			CharSequence str = state.data.strs.next();
			return state.cm.removeFrom(str);
		}

		//@Benchmark
		public CharSequence testGuavaRemoveFrom2(MyState state) {
			CharSequence str = state.data.strs.next();
			CharMatcher cm = CharMatcher.anyOf(state.chars);
			return cm.removeFrom(str);
		}
	}

}
