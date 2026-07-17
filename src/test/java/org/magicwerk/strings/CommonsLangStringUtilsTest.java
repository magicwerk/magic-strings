package org.magicwerk.strings;

import java.util.Collection;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.IterateExecutor;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.brownies.core.diff.ObjectDiff;
import org.magicwerk.brownies.core.diff.StringDiff;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.text2.TextCaseTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.javassist.analyzer.ClassDef;
import org.magicwerk.brownies.javassist.analyzer.JavaAnalyzer;
import org.magicwerk.brownies.javassist.analyzer.MethodDef;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestCompare;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestVerify;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringJoiner;
import org.magicwerk.strings.StringPadder;
import org.magicwerk.strings.StringRemover;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.StringRoots;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.StringTrimmer;
import org.magicwerk.strings.StringTruncater;
import org.magicwerk.strings.StringUnwrapper;
import org.magicwerk.strings.StringWrapper;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringPadder.PadMode;
import org.magicwerk.strings.StringTrimmer.CollapseMode;
import org.magicwerk.strings.StringTrimmer.TrimMode;
import org.magicwerk.strings.StringTruncater.TruncateMode;
import org.magicwerk.strings.StringWrapper.WrapMode;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;
import org.magicwerk.strings.matcher.IStringFixedLenMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;
import org.magicwerk.brownies.tools.dev.jvm.JmhTool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.Strings;

/**
 * Class {@link CommonsLangStringUtilsTest} compares magic-strings with methods in commons-lang StringUtils.
 * <p>
 * https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html
 */
public class CommonsLangStringUtilsTest extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	IList<MethodDef> methodsStringUtils;

	public static void main(String[] args) {
		new CommonsLangStringUtilsTest().run();
	}

	void run() {
		runManual();
		//runTests();
		//runBenchmarks();
		//test();
	}

	void runManual() {
		LOG.info("{}", "abc".replace("", "x"));
		LOG.info("{}", StringUtils.replace("abc", "", "x"));

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

		test(Count_Char_Test.class);
		//test(Count_String_Test.class);
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

		new BenchmarkTestHelper().setBaseImplementation("testStringUtils").showReport(tes);
	}

	void analyze() {
		JavaAnalyzer ja = new JavaAnalyzer().setUseCurrentClassPath();
		ClassDef cd = ja.analyzeClass(StringUtils.class);
		methodsStringUtils = cd.getMethodDefs().filter(m -> m.isPublic());
	}

	void test() {
		analyzeStringUtils();

		//testAbbreviate();
		//testStrip();
		//testPad();
		//testWrap();
		//testUnwrap();
		//testTruncate();
		//testRemove();
		//testReplace();

		//		testCommon();
		//		testContains();
		//		testCountMatches();
		//		testIndexOf();
		//		testOrdinalIndexes();
		//		testSplit();
		//		testSubstring();
		//		testWrap();

		//new IndexOfAnyStringJmhTest().test();
		//new StringSplitJmhTest().test();
		//new StringJoinJmhTest().test();
		//WrapUnwrapJmhTest.test();
		//new StringRemoverJmhTest().test();
		//IndexOfDifferenceJmhTest.test();
		//StringUtilsWrapJmhTest.test();
		//StringWrapperRemoveJmhTest.test();
		//ReplaceCharsJmhTest.test();
	}

	void analyzeStringUtils() {
		analyze();

		IList<String> methods = methodsStringUtils.map(MethodDef::getName);
		methods.sort(null);
		methods = CollectionTools2.getDistinct(methods);

		for (String method : methods) {
			LOG.info("Method: {}", method);

			IList<String> m2 = methodsStringUtils.filterMap(m -> m.getName().equals(method), MethodDef::getSimpleTypedName);
			LOG.info("Variants: {}", m2);

			IList<String> ts = testClasses.mapFilter(ClassDef::getSimpleName, n -> getMethodName(n).equals(method));
			LOG.info("Tests: {}", ts);

			LOG.info("===\n");
		}
	}

	static final StringSplitter methodNameSplitter = StringSplitter.build(b -> b.setFindChar('_'));

	String getMethodName(String testClassName) {
		String n = methodNameSplitter.getFirst(testClassName);
		return TextCaseTools.toFirstLower(n);
	}

	void failTest(Runnable runnable) {
		new RunnableExecutor().setMustFail().setLogException().run(runnable);
	}

	// Tests

	@Override
	@Capture
	public void runTests() {
		super.runTests();
	}

	// abbreviate

	// String abbreviate(String str, int maxWidth) == abbreviate(str, "..." maxWidth)
	// String abbreviate(String str, String abbrevMarker, int maxWidth)
	// String abbreviateMiddle(String str, String middle, int length)

	// TODO   String abbreviate(final String str, final int offset, final int maxWidth)
	// TODO   String abbreviate(final String str, final String abbrevMarker, int offset, final int maxWidth)

	void testAbbreviate() {
		test(Abbreviate_Change_Test.class);
		test(Abbreviate_NoChange_Test.class);
		test(AbbreviateMiddle_Change_Test.class);
		test(AbbreviateMiddle_NoChange_Test.class);
		//test(AbbreviateChangeTooShortTest.class); // TODO
	}

	static class TransformBase_TestBase {

		/** Check result of test execution for change / no-change (determined whether name has marker "_NoChange") */
		@TestVerify
		void checkResult(TestExecution te) {
			boolean expectEqual = te.testClass.getName().contains("_NoChange");
			String input = (String) te.testMethodsParamValues.get(0)[0];
			String result = (String) te.testMethodsResults.get(0).getValue();
			boolean equal = ObjectTools.equals(input, result);
			CheckTools.check(equal == expectEqual, "Unexpected change result: {}", te);
		}
	}

	static class AbbreviateBase_TestBase extends TransformBase_TestBase {
		int length = 5;
		String marker = "===";

		String inputLong = "0123456789";
		String inputShort = "01234";

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			this.inputs = GapList.create(input);
		}
	}

	static class Abbreviate_TestBase extends AbbreviateBase_TestBase {
		StringTruncater truncater = StringTruncater.builder().setLength(length).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_RIGHT).build();

		@TestMethod
		public CharSequence testStringTruncater(String input) {
			return truncater.truncate(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.abbreviate(input, marker, length);
		}
	}

	public static class AbbreviateMiddle_TestBase extends AbbreviateBase_TestBase {
		StringTruncater truncater = StringTruncater.builder().setLength(length).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_CENTER).build();

		@TestMethod
		public CharSequence testStringTruncater(String input) {
			return truncater.truncate(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.abbreviateMiddle(input, marker, length);
		}
	}

	public static class Abbreviate_Change_Test extends Abbreviate_TestBase {
		{
			setInput(inputLong);
		}
	}

	public static class Abbreviate_NoChange_Test extends Abbreviate_TestBase {
		{
			setInput(inputShort);
		}
	}

	public static class AbbreviateMiddle_Change_Test extends AbbreviateMiddle_TestBase {
		{
			setInput(inputLong);
		}
	}

	public static class AbbreviateMiddle_NoChange_Test extends AbbreviateMiddle_TestBase {
		{
			setInput(inputShort);
		}
	}

	// TODO
	public static class Abbreviate_ChangeTooShort_Test_TODO extends Abbreviate_TestBase {
		{
			length = 2;
			marker = "===";
		}

		@TestData
		IList<String> inputs = GapList.create("01234");
	}

	// normalize
	//
	// String normalizeSpace(String str)

	static class NormalizeSpace_TestBase extends WrapAppendPrepend_TestBase {
		StringTrimmer trimmer = StringTrimmer.build(b -> b.setFindCharPredicate(CharPredicates.whitespace).setTrimMode(TrimMode.HEAD_TAIL)
				.setCollapseMode(CollapseMode.BODY).setCollapseString(" "));

		@TestMethod
		public CharSequence testStringTrimmer(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.normalizeSpace(input);
		}

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			this.inputs = GapList.create(input);
		}
	}

	public static class NormalizeSpace_NoChange_Test extends NormalizeSpace_TestBase {
		{
			setInput("abcde");
		}
	}

	public static class NormalizeSpace_Change_Test extends NormalizeSpace_TestBase {
		{
			setInput(" ab  de ");
		}
	}

	// count
	//
	// int countMatches(final CharSequence str, final CharSequence sub)
	// int countMatches(final CharSequence str, final char ch)

	void testCount() {
		test(Count_Char_Test.class);
		test(Count_String_Test.class);
	}

	static class StringUtils_TestBase {
	}

	static class StringUtilsRead_TestBase extends StringUtils_TestBase {
	}

	static class Count_TestBase extends StringUtilsRead_TestBase {
		StringFinder stringFinder;

		@TestData
		IList<String> inputs = GapList.create("a", "abc", "x", "xyz");

		void setInput(String input) {
			this.inputs = GapList.create(input);
		}

		@TestMethod
		public int testStringFinder(String input) {
			return stringFinder.count(input);
		}
	}

	public static class Count_Char_Test extends Count_TestBase {
		char findChar = 'x';
		{
			stringFinder = StringFinder.builder().setFindChar(findChar).build();
		}

		@TestMethod
		public int testStringUtils(String input) {
			return StringUtils.countMatches(input, findChar);
		}

	}

	public static class Count_String_Test extends Count_TestBase {
		String findString = "xyz";
		{
			stringFinder = StringFinder.builder().setFindString(findString).build();
		}

		@TestMethod
		public int testStringUtils(String input) {
			return StringUtils.countMatches(input, findString);
		}
	}

	// wrap / appendIfMissing / prependIfMissing

	public static class WrapAppendPrepend_TestBase extends TransformBase_TestBase {
		char wrapChar = 'x';
		String wrapStr = "xx";
		String[] suffixesStr = new String[] { "xx", "uu" };

		String inputWrapped = "xx012345xx";
		String inputWrappedCI = "XX012345XX";
		String inputWrappedSuffix = "uu012345uu";
		String inputNotWrapped = "012345";
		String inputNotWrappedCI = "012345";
	}

	// unwrap
	//
	// String unwrap(String str, String wrapToken)
	// String unwrap(String str, char wrapChar)
	//
	// String removeStart(String str, char remove)
	// String removeStart(String str, String remove)
	//
	// String removeEnd(String str, String remove)
	// TODO there is no removeEnd(String, char)

	// TODO String removeStartIgnoreCase(final String str, final String remove)
	// TODO String removeEndIgnoreCase(final String str, final String remove)

	void testUnwrap() {
		test(Unwrap_String_Change_Test.class);
		test(Unwrap_String_NoChange_Test.class);
		test(Unwrap_Char_Change_Test.class);
		test(Unwrap_Char_NoChange_Test.class);

		test(RemoveStart_String_Change_Test.class);
		test(RemoveStart_String_NoChange_Test.class);
		test(RemoveStart_Char_Change_Test.class);
		test(RemoveStart_Char_NoChange_Test.class);

		test(RemoveEnd_String_Change_Test.class);
		test(RemoveEnd_String_NoChange_Test.class);

		test(RemoveStartIgnoreCase_String_Change_Test.class);
		test(RemoveStartIgnoreCase_String_ChangeCI_Test.class);
		test(RemoveStartIgnoreCase_String_NoChange_Test.class);

		test(RemoveEndIgnoreCase_String_Change_Test.class);
		test(RemoveEndIgnoreCase_String_ChangeCI_Test.class);
		test(RemoveEndIgnoreCase_String_NoChange_Test.class);
	}

	static class Unwrap_TestBase extends WrapAppendPrepend_TestBase {
		@TestData
		IList<String> inputs;

		void setInput(String input) {
			this.inputs = GapList.create(input);
		}
	}

	static class Unwrap_String_TestBase extends Unwrap_TestBase {
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapString(wrapStr).setWrapMode(WrapMode.HEAD_AND_TAIL).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.unwrap(input, wrapStr);
		}
	}

	static class Unwrap_Char_TestBase extends Unwrap_TestBase {
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapChar(wrapChar).setWrapMode(WrapMode.HEAD_AND_TAIL).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.unwrap(input, wrapChar);
		}
	}

	static class RemoveStart_String_TestBase extends Unwrap_TestBase {
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapString(wrapStr).setWrapMode(WrapMode.HEAD).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeStart(input, wrapStr);
		}
	}

	static class RemoveStart_Char_TestBase extends Unwrap_TestBase {
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapChar(wrapChar).setWrapMode(WrapMode.HEAD).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeStart(input, wrapChar);
		}
	}

	static class RemoveEnd_String_TestBase extends Unwrap_TestBase {
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapString(wrapStr).setWrapMode(WrapMode.TAIL).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeEnd(input, wrapStr);
		}
	}

	static class RemoveStartIgnoreCase_String_TestBase extends Unwrap_TestBase {
		IStringMatcher matcher = StringMatcher.of(wrapStr, CharEqual.isEqualCharIgnoreCase());
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapMatcher(matcher).setWrapMode(WrapMode.HEAD).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeStartIgnoreCase(input, wrapStr);
		}
	}

	static class RemoveEndIgnoreCase_String_TestBase extends Unwrap_TestBase {
		IStringMatcher matcher = StringMatcher.of(wrapStr, CharEqual.isEqualCharIgnoreCase());
		StringUnwrapper unwrapper = StringUnwrapper.builder().setUnwrapMatcher(matcher).setWrapMode(WrapMode.TAIL).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return unwrapper.unwrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeEndIgnoreCase(input, wrapStr);
		}
	}

	public static class Unwrap_String_Change_Test extends Unwrap_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class Unwrap_String_NoChange_Test extends Unwrap_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class Unwrap_Char_Change_Test extends Unwrap_Char_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class Unwrap_Char_NoChange_Test extends Unwrap_Char_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class RemoveStart_String_Change_Test extends RemoveStart_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class RemoveStart_String_NoChange_Test extends RemoveStart_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class RemoveStart_Char_Change_Test extends RemoveStart_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class RemoveStart_Char_NoChange_Test extends RemoveStart_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class RemoveEnd_String_Change_Test extends RemoveEnd_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class RemoveEnd_String_NoChange_Test extends RemoveEnd_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class RemoveStartIgnoreCase_String_Change_Test extends RemoveStartIgnoreCase_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class RemoveStartIgnoreCase_String_ChangeCI_Test extends RemoveStartIgnoreCase_String_TestBase {
		{
			setInput(inputWrappedCI);
		}
	}

	public static class RemoveStartIgnoreCase_String_NoChange_Test extends RemoveStartIgnoreCase_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class RemoveEndIgnoreCase_String_Change_Test extends RemoveEndIgnoreCase_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class RemoveEndIgnoreCase_String_ChangeCI_Test extends RemoveEndIgnoreCase_String_TestBase {
		{
			setInput(inputWrappedCI);
		}
	}

	public static class RemoveEndIgnoreCase_String_NoChange_Test extends RemoveEndIgnoreCase_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	// wrap
	//
	//	String wrap(String str, char wrapWith)
	//	String wrap(String str, String wrapWith)
	//	String wrapIfMissing(String str, char wrapWith)
	//	String wrapIfMissing(String str, String wrapWith)

	void testWrap() {
		test(Wrap_String_Change_Test.class);
		test(Wrap_Char_Change_Test.class);

		test(WrapIfMissing_String_Change_Test.class);
		test(WrapIfMissing_String_NoChange_Test.class);
		test(WrapIfMissing_Char_Change_Test.class);
		test(WrapIfMissing_Char_NoChange_Test.class);
	}

	static class Wrap_TestBase extends WrapAppendPrepend_TestBase {
		WrapMode wrapMode = WrapMode.HEAD_OR_TAIL;

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	static class Wrap_String_TestBase extends Wrap_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setWrapMode(wrapMode).setWrapAlways(true).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.wrap(input, wrapStr);
		}
	}

	public static class Wrap_String_Change_Test extends Wrap_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	static class Wrap_Char_TestBase extends Wrap_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapChar(wrapChar).setWrapMode(wrapMode).setWrapAlways(true).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.wrap(input, wrapChar);
		}
	}

	public static class Wrap_Char_Change_Test extends Wrap_Char_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	static class WrapIfMissing_String_TestBase extends Wrap_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setWrapMode(wrapMode).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.wrapIfMissing(input, wrapStr);
		}
	}

	public static class WrapIfMissing_String_Change_Test extends WrapIfMissing_String_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class WrapIfMissing_String_NoChange_Test extends WrapIfMissing_String_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	static class WrapIfMissing_Char_TestBase extends Wrap_TestBase {
		final StringWrapper wrapper = StringWrapper.builder().setWrapChar(wrapChar).setWrapMode(wrapMode).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.wrapIfMissing(input, wrapChar);
		}
	}

	public static class WrapIfMissing_Char_Change_Test extends WrapIfMissing_Char_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class WrapIfMissing_Char_NoChange_Test extends WrapIfMissing_Char_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	static class Append_TestBase extends WrapAppendPrepend_TestBase {
		WrapMode location = WrapMode.TAIL;

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	static class AppendIfMissing_TestBase extends Append_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.appendIfMissing(input, wrapStr);
		}
	}

	public static class AppendIfMissing_Change_Test extends AppendIfMissing_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class AppendIfMissing_NoChange_Test extends AppendIfMissing_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class AppendIfMissing_Suffix_TestBase extends Append_TestBase {
		IStringMatcher wrapMatcher = StringsMatcher.of(suffixesStr);
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setMatchMatcher(wrapMatcher).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.appendIfMissing(input, wrapStr, suffixesStr);
		}
	}

	public static class AppendIfMissing_Suffix_Change_Test extends AppendIfMissing_Suffix_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class AppendIfMissing_Suffix_NoChange_Test extends AppendIfMissing_Suffix_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	static class AppendIfMissingIgnoreCase_TestBase extends Append_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setCharMode(CharMode.CI_CHAR).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.appendIfMissingIgnoreCase(input, wrapStr);
		}
	}

	public static class AppendIfMissingIgnoreCase_Change_Test extends AppendIfMissingIgnoreCase_TestBase {
		{
			setInput(inputNotWrapped);
		}
	}

	public static class AppendIfMissingIgnoreCase_NoChange_Test extends AppendIfMissingIgnoreCase_TestBase {
		{
			setInput(inputWrapped);
		}
	}

	public static class AppendIfMissingIgnoreCase_ChangeCI_Test extends AppendIfMissingIgnoreCase_TestBase {
		{
			setInput(inputNotWrappedCI);
		}
	}

	public static class AppendIfMissingIgnoreCase_NoChangeCI_Test extends AppendIfMissingIgnoreCase_TestBase {
		{
			setInput(inputWrappedCI);
		}
	}

	// prependIfMissing
	//
	// String prependIfMissing(String str, CharSequence prefix, CharSequence... prefixes) 
	// String prependIfMissingIgnoreCase(String str, CharSequence prefix, CharSequence... prefixes) 

	void testPrependIfMissing() {
		test(PrependIfMissing_Change_Test.class);
		test(PrependIfMissing_NoChange_Test.class);
		test(PrependIfMissing_Suffix_Change_Test.class);
		test(PrependIfMissing_Suffix_NoChange_Test.class);

		test(PrependIfMissingIgnoreCase_Change_Test.class);
		test(PrependIfMissingIgnoreCase_NoChange_Test.class);
		test(PrependIfMissingIgnoreCase_ChangeCI_Test.class);
		test(PrependIfMissingIgnoreCase_NoChangeCI_Test.class);
	}

	static class Prepend_TestBase extends WrapAppendPrepend_TestBase {
		WrapMode location = WrapMode.HEAD;
	}

	static class PrependIfMissing_TestBase extends Prepend_TestBase {
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.prependIfMissing(input, wrapStr);
		}
	}

	public static class PrependIfMissing_Change_Test extends PrependIfMissing_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputNotWrapped);
	}

	public static class PrependIfMissing_NoChange_Test extends PrependIfMissing_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputWrapped);
	}

	static class PrependIfMissing_Suffix_TestBase extends Prepend_TestBase {
		IStringMatcher wrapMatcher = StringsMatcher.of(suffixesStr);
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setMatchMatcher(wrapMatcher).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.prependIfMissing(input, wrapStr, suffixesStr);
		}
	}

	public static class PrependIfMissing_Suffix_Change_Test extends PrependIfMissing_Suffix_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputNotWrapped);
	}

	public static class PrependIfMissing_Suffix_NoChange_Test extends PrependIfMissing_Suffix_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputWrapped);
	}

	static class PrependIfMissingIgnoreCase_TestBase extends Prepend_TestBase {
		IStringMatcher wrapMatcher = StringMatcher.of(wrapStr, CharEqual.isEqualCharIgnoreCase());
		StringWrapper wrapper = StringWrapper.builder().setWrapString(wrapStr).setMatchMatcher(wrapMatcher).setWrapMode(location).build();

		@TestMethod
		public CharSequence testStringWrapper(String input) {
			return wrapper.wrap(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.prependIfMissingIgnoreCase(input, wrapStr);
		}
	}

	public static class PrependIfMissingIgnoreCase_Change_Test extends PrependIfMissingIgnoreCase_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputNotWrapped);
	}

	public static class PrependIfMissingIgnoreCase_NoChange_Test extends PrependIfMissingIgnoreCase_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputWrapped);
	}

	public static class PrependIfMissingIgnoreCase_ChangeCI_Test extends PrependIfMissingIgnoreCase_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputNotWrappedCI);
	}

	public static class PrependIfMissingIgnoreCase_NoChangeCI_Test extends PrependIfMissingIgnoreCase_TestBase {
		@TestData
		IList<String> inputs = GapList.create(inputWrappedCI);
	}

	//---

	// Substring

	void testSubstring() {
		testSubstringBefore();
		testSubstringAfter();
	}

	void testSubstringBefore() {
		testSubstringBefore("axc", "x");
	}

	void testSubstringAfter() {
		testSubstringBefore("axc", "x");
	}

	void testSubstringBefore(String str, String sep) {
		StringSplitter ss = StringSplitter.builder().setFindString(sep).build();
		String s0 = ss.getFirst(str);
		String s1 = StringUtils.substringBefore(str, sep);
		CheckTools.check(ObjectTools.equals(s0, s1));
	}

	void testSubstringAfter(String str, String sep) {
		StringSplitter ss = StringSplitter.builder().setFindString(sep).setPartNullIfNoMatch(false).build();
		String s0 = ss.getAfterFirst(str);
		String s1 = StringUtils.substringAfter(str, sep);
		CheckTools.check(ObjectTools.equals(s0, s1));
	}

	// IndexOf

	void testIndexOf() {
		testIndexOfAnyString();
	}

	void testIndexOfAnyString() {
		testIndexOfAnyString("abcdef", "cd", "de");
	}

	void testIndexOfAnyString(String str, String... finds) {
		// StringUtils.indexOfAny: 
		// - no start position
		// - result does not show which string matched, additional analysis needed
		// - all string must be analyzed to prefer long strings (e.g. find "abc" if "ab" is also valid)
		StringsMatcher sm = new StringsMatcher.Builder().setSearchStrs(finds).build();
		StringFinder sf = StringFinder.builder().setFindMatcher(sm).build();
		int i0 = sf.indexOf(str);
		int i1 = StringUtils.indexOfAny(str, finds);
		CheckTools.check(i0 == i1);
	}

	// Reverse

	// public static String reverse(final String str)

	public static class Reverse_Test {

		@TestData
		IList<String> inputs = GapList.create("", "a", "ab", "abc",
				CodePointToolsTest.STRING_SURROGATE_00, "a" + CodePointToolsTest.STRING_SURROGATE_00,
				CodePointToolsTest.STRING_SURROGATE_0, "a" + CodePointToolsTest.STRING_SURROGATE_0);

		@TestMethod
		public String testMagicStrings(String input) {
			return StringTools.reverse(input);
		}

		@TestMethod
		public String testStringUtils(String input) {
			return StringUtils.reverse(input);
		}
	}

	// commonPrefix / commonSuffix

	//public static String commonPrefix(CharSequence a, CharSequence b)
	//public static String commonSuffix(CharSequence a, CharSequence b)

	public static class CommonPrefixSuffix_TestBase {

		// StringUtils.getCommonPrefix() tears surrogates apart

		@TestData
		IList<String> inputs0 = GapList.create("aa", "ab", "b", "ac");
		// CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_0 + "a");
		@TestData
		IList<String> inputs1 = GapList.create("aa", "ac", "c", "bc");
		// CodePointToolsTest.STRING_SURROGATE_01, CodePointToolsTest.STRING_SURROGATE_0 + "b");

	}

	public static class CommonPrefix_Test extends CommonPrefixSuffix_TestBase {

		StringRoots roots = StringRoots.build(b -> b);

		@TestMethod
		public String testMagicStrings(String str0, String str1) {
			return roots.getCommonRoot(str0, str1);
		}

		@TestMethod
		public String testStringUtils(String str0, String str1) {
			return StringUtils.getCommonPrefix(str0, str1);
		}
	}

	// Join

	// public static <T> String join(final T... elements)
	// public static String joinWith(final String delimiter, final Object... array)

	static class Join_TestBase {
	}

	public static class Join_Test extends Join_TestBase {

		StringJoiner joiner = StringJoiner.builder().build();

		@TestData
		IList<String[]> inputs = GapList.<String[]>create(new String[] { "a", "b", "c" });

		@TestMethod
		public String testStringTruncater(String[] inputs) {
			return joiner.joinStrings(inputs);
		}

		@TestMethod
		public String testStringUtils(String[] inputs) {
			return StringUtils.join(inputs);
		}
	}

	public static class JoinWith_Test extends Join_TestBase {

		String sep = ",";
		StringJoiner joiner = StringJoiner.builder().setJoin(sep).build();

		@TestData
		IList<String[]> inputs = GapList.<String[]>create(new String[] { "a", "b", "c" });

		@TestMethod
		public String testStringTruncater(String[] inputs) {
			return joiner.joinStrings(inputs);
		}

		@TestMethod
		public String testStringUtils(String[] inputs) {
			return StringUtils.joinWith(sep, (Object[]) inputs);
		}
	}

	// Split

	// String[] split(final String str) 
	// - String[] splitPreserveAllTokens(final String str)
	// String[] split(final String str, final char separatorChar)
	// - String[] splitPreserveAllTokens(final String str, final char separatorChar)
	// String[] split(final String str, final String separatorChars)
	// - String[] splitPreserveAllTokens(final String str, final String separatorChars)
	// String[] splitByWholeSeparator(final String str, final String separator)
	// - String[] splitByWholeSeparatorPreserveAllTokens(final String str, final String separator)
	// String[] splitByCharacterType(final String str, final boolean camelCase)
	// - String[] splitByCharacterType(final String str)
	// - String[] splitByCharacterTypeCamelCase(final String str)

	// TODO
	// String[] split(final String str, final String separatorChars, final int max)
	// - String[] splitPreserveAllTokens(final String str, final String separatorChars, final int max)

	// String[] splitByWholeSeparator( final String str, final String separator, final int max)
	// String[] splitByWholeSeparatorPreserveAllTokens(final String str, final String separator, final int max)

	void testSplitString() {
		// TODO
		// StringUtils does not split on "", but "str".indexOf("") matches 
		int i = "ab".indexOf("");
		CheckTools.check(i == 0);
		String[] s = StringUtils.splitByWholeSeparator("ab", "");
		CheckTools.check(s.length == 1);
		//testSplitString("ab", "", false);
		//testSplitString("ab", "", true);
	}

	static class Split_TestBase {
		@TestCompare
		Object compareResult(Object result) {
			if (result instanceof String[]) {
				return GapList.create((String[]) result);
			} else {
				return result;
			}
		}
	}

	public static class Split_Space_TestBase extends Split_TestBase {
		@TestData
		IList<String> inputs = GapList.create("a", "a \tb");
	}

	public static class Split_Space_Test extends Split_Space_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindCharPredicate(CharPredicates.whitespace).setCombineSeparators(true).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.split(input);
		}
	}

	public static class Split_Space_Preserve_Test extends Split_Space_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindCharPredicate(CharPredicates.whitespace).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitPreserveAllTokens(input);
		}
	}

	public static class Split_AnyChar_TestBase extends Split_TestBase {
		String seps = ",;";
		CharPredicate cp = CharPredicates.oneOf(seps);

		@TestData
		IList<String> inputs = GapList.create("a,,b;;c");
	}

	public static class Split_AnyChar_Test extends Split_AnyChar_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindCharPredicate(cp).setCombineSeparators(true).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.split(input, seps);
		}
	}

	public static class Split_AnyChar_Preserve_Test extends Split_AnyChar_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindCharPredicate(cp).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitPreserveAllTokens(input, seps);
		}
	}

	static class Split_String_TestBase extends Split_TestBase {
		String sep = "()";

		@TestData
		IList<String> inputs = GapList.create("a()()b", "()");
	}

	public static class Split_String_Test extends Split_String_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindString(sep).setCombineSeparators(true).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitByWholeSeparator(input, sep);
		}
	}

	public static class Split_String_Preserve_Test extends Split_String_TestBase {

		StringSplitter splitter = StringSplitter.builder().setFindString(sep).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitByWholeSeparatorPreserveAllTokens(input, sep);
		}
	}

	public static class SplitByCharacterType_Test extends Split_TestBase {

		@TestData
		IList<String> inputs = GapList.create("ab cd  ef", "ASFRules");

		BiPredicate<Character, Character> c = (c0, c1) -> Character.getType(c0) == Character.getType(c1);
		StringSplitter splitter = StringSplitter.builder().setSplitDetector(c).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitByCharacterType(input);
		}
	}

	public static class SplitByCharacterTypeCamelCase_Test extends Split_TestBase {

		@TestData
		IList<String> inputs = GapList.create("ASFRules");

		// Implement IStringFixedLenMatcher to prevent allocation of IMatch objects
		IStringMatcher sm = new IStringFixedLenMatcher() {
			// Matcher must be stateful 
			int add;

			@Override
			public int getMatchLength() {
				return 0;
			}

			@Override
			public int indexOf(CharSequence str, int start) {
				start += add;
				add = 0;

				int len = str.length();
				if (start >= len) {
					return -1;
				}
				int index = start;
				int type = Character.getType(str.charAt(index));
				while (index < len) {
					int t = Character.getType(str.charAt(index));
					if (t != type) {
						if (t == Character.LOWERCASE_LETTER && type == Character.UPPERCASE_LETTER) {
							// store information about the past match returned to advance on next call
							add = 1;
							return index - 1;
						} else {
							return index;
						}
					}
					index++;
				}
				return -1;
			}

			@Override
			public IMatch find(CharSequence str, int start) {
				// Force use of indexOf() method
				throw new UnsupportedOperationException();
			}
		};

		StringSplitter splitter = StringSplitter.builder().setFindMatcher(sm).build();

		@TestMethod
		public IList<String> testMagicStrings(String input) {
			return splitter.split(input);
		}

		@TestMethod
		public String[] testStringUtils(String input) {
			return StringUtils.splitByCharacterTypeCamelCase(input);
		}
	}

	// Common roots

	// int indexOfDifference(final CharSequence... css) {
	// String getCommonPrefix(final String... strs) {

	void testCommon() {
		testIndexOfDifference();
		testGetCommonPrefix();
	}

	void testIndexOfDifference() {
		testIndexOfDifference("ab", "ac");

		// StringUtils.indexOfDifference does not handle surrogates properly
		failTest(() -> testIndexOfDifference(CodePointToolsTest.STRING_SURROGATE_0, CodePointToolsTest.STRING_SURROGATE_1));
	}

	void testGetCommonPrefix() {
		testGetCommonPrefix("ab", "ac");

		// StringUtils.indexOfDifference does not handle surrogates properly
		failTest(() -> testGetCommonPrefix(CodePointToolsTest.STRING_SURROGATE_0, CodePointToolsTest.STRING_SURROGATE_1));

		testGetCommonPrefixGuava("ab", "ac");
		testGetCommonPrefixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_01);
		testGetCommonPrefixGuava(CodePointToolsTest.STRING_SURROGATE_10, CodePointToolsTest.STRING_SURROGATE_11);
		testGetCommonPrefixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_HI);
		testGetCommonPrefixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_LO);

		testGetCommonSuffixGuava("ba", "ca");
		testGetCommonSuffixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_01);
		testGetCommonSuffixGuava(CodePointToolsTest.STRING_SURROGATE_10, CodePointToolsTest.STRING_SURROGATE_11);
		testGetCommonSuffixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_HI);
		testGetCommonSuffixGuava(CodePointToolsTest.STRING_SURROGATE_00, CodePointToolsTest.STRING_SURROGATE_LO);
	}

	void testIndexOfDifference(String str0, String str1) {
		StringRoots sr = new StringRoots.Builder().build();
		int i0 = sr.getCommonLength(str0, str1);
		int i1 = StringUtils.indexOfDifference(str0, str1);
		new ObjectDiff<>(i0, i1).checkEqual();
	}

	void testGetCommonPrefix(String str0, String str1) {
		StringRoots sr = new StringRoots.Builder().build();
		CharSequence s0 = sr.getCommonRoot(str0, str1);
		CharSequence s1 = StringUtils.getCommonPrefix(str0, str1);
		new StringDiff().setStrings(s0, s1).checkEqual();
	}

	void testGetCommonPrefixGuava(String str0, String str1) {
		StringRoots sr = new StringRoots.Builder().build();
		CharSequence s0 = sr.getCommonRoot(str0, str1);
		CharSequence s1 = Strings.commonPrefix(str0, str1);
		new StringDiff().setStrings(s0, s1).checkEqual();
	}

	void testGetCommonSuffixGuava(String str0, String str1) {
		StringRoots sr = new StringRoots.Builder().setReverse(true).build();
		CharSequence s0 = sr.getCommonRoot(str0, str1);
		CharSequence s1 = Strings.commonSuffix(str0, str1);
		new StringDiff().setStrings(s0, s1).checkEqual();
	}

	// Ordinal indexes (occurrence)

	// int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal)
	// int lastOrdinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal)

	void testOrdinalIndexes() {
		testOrdinalIndex();
		testLastOrdinalIndex();
	}

	void testOrdinalIndex() {
		testOrdinalIndex("a-b-c", "-", 0);
		testOrdinalIndex("a-b-c", "-", 1);
		testOrdinalIndex("a-b-c", "-", 2);

		// Overlap
		testOrdinalIndex("ababab", "aba", 0);
		testOrdinalIndex("ababab", "aba", 1);
		testOrdinalIndex("ababab", "aba", 2);
	}

	void testLastOrdinalIndex() {
		testLastOrdinalIndex("a-b-c", "-", 0);
		testLastOrdinalIndex("a-b-c", "-", 1);
		testLastOrdinalIndex("a-b-c", "-", 2);
	}

	void testOrdinalIndex(String str, String searchStr, int occurrence) {
		StringFinder sf = StringFinder.builder().setFindString(searchStr).setOccurrence(occurrence).setOverlap(true).build();
		int i0 = sf.indexOf(str);
		int i1 = StringUtils.ordinalIndexOf(str, searchStr, occurrence + 1);
		ObjectDiff.checkEqual(i0, i1);
	}

	void testLastOrdinalIndex(String str, String searchStr, int occurrence) {
		StringFinder sf = StringFinder.builder().setFindString(searchStr).setOccurrence(occurrence).setReverse(true).build();
		int i0 = sf.indexOf(str);
		int i1 = StringUtils.lastOrdinalIndexOf(str, searchStr, occurrence + 1);
		CheckTools.check(i0 == i1);
	}

	//

	// String remove(String str, char remove)
	// String remove(String str, String remove)
	// String removeIgnoreCase(String str, String remove)
	// String deleteWhitespace(String str)

	// Regex
	// public static String removeAll(String text, String regex)
	// public static String removeFirst(String text, String regex)
	// public static String removePattern(String source, String regex)

	void testRemove() {
		test(Remove_Char_Change_Test.class);
		test(Remove_Char_NoChange_Test.class);
		test(Remove_String_Change_Test.class);
		test(Remove_String_NoChange_Test.class);
		test(RemoveIgnoreCase_String_NoChange_Test.class);
		test(RemoveIgnoreCase_String_Change_Test.class);
		test(RemoveIgnoreCase_String_ChangeCI_Test.class);
		test(DeleteWhitespace_Change_Test.class);
		test(DeleteWhitespace_NoChange_Test.class);
	}

	static class Remove_TestBase extends TransformBase_TestBase {
		char removeChar = '-';
		String removeStr = "xx";

		String removeNoChange = "01234";
		String removeChange = "01xx4-6  9=BxxEF";
		String removeChangeCI = "01XX4-6  9=BXXEF";

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	static class Remove_Char_TestBase extends Remove_TestBase {
		StringRemover remover = StringRemover.builder().setFindChar(removeChar).build();

		@TestMethod
		public CharSequence testStringRemover(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.remove(input, removeChar);
		}
	}

	static class Remove_String_TestBase extends Remove_TestBase {
		StringRemover remover = StringRemover.builder().setFindString(removeStr).build();

		@TestMethod
		public CharSequence testStringRemover(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.remove(input, removeStr);
		}
	}

	static class RemoveIgnoreCase_String_TestBase extends Remove_TestBase {
		IStringMatcher matcher = StringMatcher.of(removeStr, CharEqual.isEqualCharIgnoreCase());
		StringRemover remover = StringRemover.builder().setFindMatcher(matcher).build();

		@TestMethod
		public CharSequence testStringRemover(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.removeIgnoreCase(input, removeStr);
		}
	}

	static class DeleteWhitespace_TestBase extends Remove_TestBase {
		StringRemover remover = StringRemover.builder().setFindCharPredicate(Character::isWhitespace).build();

		@TestMethod
		public CharSequence testStringRemover(String input) {
			return remover.remove(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.deleteWhitespace(input);
		}
	}

	public static class DeleteWhitespace_Change_Test extends DeleteWhitespace_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class DeleteWhitespace_NoChange_Test extends DeleteWhitespace_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class Remove_Char_Change_Test extends Remove_Char_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class Remove_Char_NoChange_Test extends Remove_Char_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class Remove_String_Change_Test extends Remove_String_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class Remove_String_NoChange_Test extends Remove_String_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class RemoveIgnoreCase_String_Change_Test extends RemoveIgnoreCase_String_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class RemoveIgnoreCase_String_NoChange_Test extends RemoveIgnoreCase_String_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class RemoveIgnoreCase_String_ChangeCI_Test extends RemoveIgnoreCase_String_TestBase {
		{
			setInput(removeChangeCI);
		}
	}

	// Replace

	//    String replace(String text, String searchString, String replacement) == -1
	//    String replace(String text, String searchString, String replacement, int max)
	//
	//    String replaceIgnoreCase(String text, String searchString, String replacement) == -1
	//    String replaceIgnoreCase(String text, String searchString, String replacement, int max)
	//
	//    String replaceOnce(String text, String searchString, String replacement) == 1
	//    String replaceOnceIgnoreCase(String text, String searchString, String replacement)
	//	
	//    String replaceChars(String str, char searchChar, char replaceChar)
	//    String replaceChars(String str, String searchChars, String replaceChars)
	//
	//    String replaceEach(String text, String[] searchList, String[] replacementList)
	//    String replaceEachRepeatedly(String text, String[] searchList, String[] replacementList)
	//
	//Regex
	//    String replaceAll(String text, String regex, String replacement)
	//    String replaceFirst(String text, String regex, String replacement)
	//    String replacePattern(String source, String regex, String replacement)

	void testReplace() {
		test(ReplaceChars_Char_NoChange_Test.class);
		test(ReplaceChars_Char_Change_Test.class);
		test(ReplaceChars_AnyChar_NoChange_Test.class);
		test(ReplaceChars_AnyChar_Change_Test.class);
		test(Replace_NoChange_Test.class);
		test(Replace_Change_Test.class);
		test(ReplaceIgnoreCase_NoChange_Test.class);
		test(ReplaceIgnoreCase_Change_Test.class);
		test(Replace_Occurrence_NoChange_Test.class);
		test(Replace_Occurrence_Change_Test.class);
		test(ReplaceOnce_NoChange_Test.class);
		test(ReplaceOnce_Change_Test.class);
		test(ReplaceOnceIgnoreCase_NoChange_Test.class);
		test(ReplaceOnceIgnoreCase_Change_Test.class);
	}

	static class ReplaceBase_TestBase extends TransformBase_TestBase {
		char removeChar = '-';
		char replaceChar = '=';
		String removeAnyChar = "567";
		String replaceAnyChar = "89";
		String removeStr = "xx";
		String replaceStr = "oo";

		String removeNoChange = "01234";
		String removeChange = "01xx4-6xx9=BxxEF";
		String removeChangeCI = "01XX4-6XX9=BXXEF";

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	static class Replace_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replace(input, removeStr, replaceStr);
		}
	}

	static class Replace_Occurrence_TestBase extends ReplaceBase_TestBase {
		int numReplace = 2;
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).setNumReplace(numReplace).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replace(input, removeStr, replaceStr, numReplace);
		}
	}

	static class ReplaceIgnoreCase_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).setIgnoreCase(true).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceIgnoreCase(input, removeStr, replaceStr);
		}
	}

	static class ReplaceIgnoreCase_Occurrence_TestBase extends ReplaceBase_TestBase {
		int numReplace = 2;
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).setIgnoreCase(true).setNumReplace(numReplace).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceIgnoreCase(input, removeStr, replaceStr, numReplace);
		}
	}

	static class ReplaceOnce_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).setNumReplace(1).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceOnce(input, removeStr, replaceStr);
		}
	}

	static class ReplaceOnceIgnoreCase_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceString(removeStr, replaceStr).setNumReplace(1).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceOnceIgnoreCase(input, removeStr, replaceStr);
		}
	}

	static class ReplaceChars_Char_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceChar(removeChar, replaceChar).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceChars(input, removeChar, replaceChar);
		}
	}

	static class ReplaceChars_AnyChar_TestBase extends ReplaceBase_TestBase {
		StringReplacer replacer = StringReplacer.builder().replaceAnyChar(removeAnyChar, replaceAnyChar).build();

		@TestMethod
		public CharSequence testStringReplacer(String input) {
			return replacer.replace(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.replaceChars(input, removeAnyChar, replaceAnyChar);
		}
	}

	public static class Replace_NoChange_Test extends Replace_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class Replace_Change_Test extends Replace_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceIgnoreCase_NoChange_Test extends ReplaceIgnoreCase_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class ReplaceIgnoreCase_Change_Test extends ReplaceIgnoreCase_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class Replace_Occurrence_NoChange_Test extends Replace_Occurrence_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class Replace_Occurrence_Change_Test extends Replace_Occurrence_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceOnce_NoChange_Test extends ReplaceOnce_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class ReplaceOnce_Change_Test extends ReplaceOnce_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceOnceIgnoreCase_NoChange_Test extends ReplaceOnceIgnoreCase_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class ReplaceOnceIgnoreCase_Change_Test extends ReplaceOnceIgnoreCase_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceChars_Char_NoChange_Test extends ReplaceChars_Char_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class ReplaceChars_Char_Change_Test extends ReplaceChars_Char_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceChars_AnyChar_NoChange_Test extends ReplaceChars_AnyChar_TestBase {
		{
			setInput(removeNoChange);
		}
	}

	public static class ReplaceChars_AnyChar_Change_Test extends ReplaceChars_AnyChar_TestBase {
		{
			setInput(removeChange);
		}
	}

	public static class ReplaceCharsJmhTest {

		static void test() {
			Options opts = new Options().includeClass(ReplaceCharsJmhTest.class);
			//opts.setJavaVersions(GapList.create(TestTools.JdkCommands11, TestTools.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhTool jr = new JmhTool();
			JmhRunner ju = jr.getRunner();
			ju.verifyJmhMethods(opts, 10);
			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String searchChars = "abc";
			String replaceChars = "AB";

			StringReplacer sf = StringReplacer.builder().replaceAnyChar(searchChars, replaceChars).build();

			CyclicSource<String> cs = new CyclicSource<>("0a1b2c3");
		}

		@Benchmark
		public String test(MyState state) {
			return StringUtils.replaceChars(state.cs.next(), state.searchChars, state.replaceChars);
		}

		@Benchmark
		public String testBrownies(MyState state) {
			return state.sf.replace(state.cs.next());
		}
	}

	//

	void testContains() {
		testContainsAnyChar();
		testContainsAnyString();
		testContainsNone();
		testContainsOnly();
		testContainsString();
		testContainsStringIgnoreCase();
	}

	// contains any char

	// boolean containsAny(final CharSequence cs, final char... searchChars)
	// boolean containsAny(final CharSequence cs, final CharSequence searchChars)

	void testContainsAnyChar() {
		testContainsAnyChar("abc", "a");
		testContainsAnyChar("abc", "xa");
		testContainsAnyChar("abc", "x");
	}

	void testContainsAnyChar(String str, String searchChars) {
		CharPredicate cp = CharPredicates.oneOf(searchChars);
		StringFinder sf = StringFinder.builder().setFindCharPredicate(cp).build();
		boolean r0 = sf.contains(str);
		boolean r1 = StringUtils.containsAny(str, searchChars);
		CheckTools.check(r0 == r1);
	}

	// contains any string

	// TODO boolean containsAnyIgnoreCase(final CharSequence cs, final CharSequence... searchCharSequences)
	// boolean contains(final CharSequence seq, final CharSequence searchSeq)

	void testContainsString() {
		testContainsString("abc", "bc");
		testContainsString("abc", "xa");
		testContainsString("abc", "x");
	}

	void testContainsString(String str, String searchStr) {
		StringFinder sf = StringFinder.builder().setFindString(searchStr).build();
		boolean r0 = sf.contains(str);
		boolean r1 = StringUtils.contains(str, searchStr);
		CheckTools.check(r0 == r1);
	}

	// boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr)

	void testContainsStringIgnoreCase() {
		testContainsStringIgnoreCase("abc", "bc");
		testContainsStringIgnoreCase("abc", "xa");
		testContainsStringIgnoreCase("abc", "x");
		testContainsStringIgnoreCase("abc", "BC");
	}

	void testContainsStringIgnoreCase(String str, String searchStr) {
		StringFinder sf = StringFinder.builder().setFindString(searchStr).setIgnoreCase(true).build();
		boolean r0 = sf.contains(str);
		boolean r1 = StringUtils.containsIgnoreCase(str, searchStr);
		CheckTools.check(r0 == r1);
	}

	// boolean containsAny(final CharSequence cs, final CharSequence... searchCharSequences)

	void testContainsAnyString() {
		testContainsAnyString("abc", "bc");
		testContainsAnyString("abc", "xa");
		testContainsAnyString("abc", "x");
		testContainsAnyString("abc", "x", "b");
	}

	void testContainsAnyString(String str, String... searchStrs) {
		StringsMatcher sm = new StringsMatcher.Builder().setSearchStrs(searchStrs).build();
		StringFinder sf = StringFinder.builder().setFindMatcher(sm).build();
		boolean r0 = sf.contains(str);
		boolean r1 = StringUtils.containsAny(str, searchStrs);
		CheckTools.check(r0 == r1);
	}

	// boolean containsOnly(final CharSequence cs, final String validChars)
	// boolean containsOnly(final CharSequence cs, final char... valid)

	void testContainsOnly() {
		testContainsOnly("abcba", "abc");
		testContainsOnly("abcba", "ab");
	}

	void testContainsOnly(String str, String validChars) {
		CharPredicate cp = CharPredicates.oneOf(validChars).negate();
		StringFinder sf = StringFinder.builder().setFindCharPredicate(cp).build();
		boolean r0 = !sf.contains(str);
		boolean r1 = StringUtils.containsOnly(str, validChars);
		CheckTools.check(r0 == r1);
	}

	// boolean containsNone(final CharSequence cs, final String invalidChars)
	// boolean containsNone(final CharSequence cs, final char... invalid)

	void testContainsNone() {
		testContainsNone("abc", "x");
		testContainsNone("axc", "x");
	}

	void testContainsNone(String str, String invalidChars) {
		CharPredicate cp = CharPredicates.oneOf(invalidChars);
		StringFinder sf = StringFinder.builder().setFindCharPredicate(cp).build();
		boolean r0 = !sf.contains(str);
		boolean r1 = StringUtils.containsNone(str, invalidChars);
		CheckTools.check(r0 == r1);
	}

	//

	//    public static String wrap(final String str, final char wrapWith)
	//    public static String wrap(final String str, final String wrapWith)
	//    public static String wrapIfMissing(final String str, final char wrapWith)
	//    public static String wrapIfMissing(final String str, final String wrapWith)

	//    public static String unwrap(final String str, final char wrapChar)
	//    public static String unwrap(final String str, final String wrapToken)
	//
	//    public static String prependIfMissing(final String str, final CharSequence prefix, final CharSequence... prefixes)
	//    public static String prependIfMissingIgnoreCase(final String str, final CharSequence prefix, final CharSequence... prefixes)

	// String strip(final String str) == strip(String str, null)
	// String strip(String str, String stripChars)
	// String stripEnd(String str, String stripChars)
	// String stripStart(String str, String stripChars)

	void testStrip() {
		test(StripStart_Whitespace_Change_Test.class);
		test(StripStart_Whitespace_NoChange_Test.class);
		test(StripStart_AnyChar_Change_Test.class);
		test(StripStart_AnyChar_NoChange_Test.class);

		test(StripEnd_Whitespace_Change_Test.class);
		test(StripEnd_Whitespace_NoChange_Test.class);
		test(StripEnd_AnyChar_Change_Test.class);
		test(StripEnd_AnyChar_NoChange_Test.class);

		test(Strip_Whitespace_Change_Test.class);
		test(Strip_Whitespace_NoChange_Test.class);
		test(Strip_AnyChar_Change_Test.class);
		test(Strip_AnyChar_NoChange_Test.class);
	}

	interface GetTestData {
		// The fields inputStrip/inputNoStrip are referenced through the class so they are not final and can be changed at runtime
		IList<String> getTestData(StripStartEnd_TestBase base);
	}

	interface GetTestDataStrip extends GetTestData {
		@Override
		default IList<String> getTestData(StripStartEnd_TestBase base) {
			return GapList.create(base.inputStrip);
		}
	}

	interface GetTestDataNoStrip extends GetTestData {
		@Override
		default IList<String> getTestData(StripStartEnd_TestBase base) {
			return GapList.create(base.inputNoStrip);
		}
	}

	interface GetStripChars {
		// The fields inputStrip/inputNoStrip are referenced through the class so they are not final and can be changed at runtime
		String getStripChars(StripStartEnd_TestBase base);
	}

	interface GetStripCharsWhitespace extends GetStripChars {
		@Override
		default String getStripChars(StripStartEnd_TestBase base) {
			return base.stripWhitespace;
		}
	}

	interface GetStripCharsAnyChar extends GetStripChars {
		@Override
		default String getStripChars(StripStartEnd_TestBase base) {
			return base.stripAnyChar;
		}
	}

	abstract static class StripStartEnd_TestBase implements GetStripChars, GetTestData {

		String stripAnyChar = "< >";
		String stripWhitespace = null;

		String inputStrip = " <01234> ";
		String inputNoStrip = "01234";

		StringTrimmer trimmer;
		String stripChars;

		StripStartEnd_TestBase(TrimMode location) {
			stripChars = getStripChars(this);
			if (stripChars == null) {
				trimmer = StringTrimmer.builder().setFindCharPredicate(CharPredicates.whitespace).setTrimMode(location).build();
			} else {
				trimmer = StringTrimmer.builder().setFindAnyChar(stripChars).setTrimMode(location).build();
			}
		}

		@TestMethod
		public CharSequence testStringTrimmer(String input) {
			return trimmer.trim(input);
		}

		@TestData
		IList<String> inputs = getTestData(this);
	}

	abstract static class StripStart_TestBase extends StripStartEnd_TestBase {
		StripStart_TestBase() {
			super(TrimMode.HEAD);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.stripStart(input, stripChars);
		}
	}

	abstract static class StripEnd_TestBase extends StripStartEnd_TestBase {
		StripEnd_TestBase() {
			super(TrimMode.TAIL);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.stripEnd(input, stripChars);
		}
	}

	abstract static class Strip_TestBase extends StripStartEnd_TestBase {
		Strip_TestBase() {
			super(TrimMode.HEAD_TAIL);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.strip(input, stripChars);
		}
	}

	public static class StripStart_Whitespace_Change_Test extends StripStart_TestBase
			implements GetStripCharsWhitespace, GetTestDataStrip {
	}

	public static class StripStart_Whitespace_NoChange_Test extends StripStart_TestBase
			implements GetStripCharsWhitespace, GetTestDataNoStrip {
	}

	public static class StripStart_AnyChar_Change_Test extends StripStart_TestBase
			implements GetStripCharsAnyChar, GetTestDataStrip {
	}

	public static class StripStart_AnyChar_NoChange_Test extends StripStart_TestBase
			implements GetStripCharsAnyChar, GetTestDataNoStrip {
	}

	public static class StripEnd_Whitespace_Change_Test extends StripEnd_TestBase
			implements GetStripCharsWhitespace, GetTestDataStrip {
	}

	public static class StripEnd_Whitespace_NoChange_Test extends StripEnd_TestBase
			implements GetStripCharsWhitespace, GetTestDataNoStrip {
	}

	public static class StripEnd_AnyChar_Change_Test extends StripEnd_TestBase
			implements GetStripCharsAnyChar, GetTestDataStrip {
	}

	public static class StripEnd_AnyChar_NoChange_Test extends StripEnd_TestBase
			implements GetStripCharsAnyChar, GetTestDataNoStrip {
	}

	public static class Strip_Whitespace_Change_Test extends Strip_TestBase
			implements GetStripCharsWhitespace, GetTestDataStrip {
	}

	public static class Strip_Whitespace_NoChange_Test extends Strip_TestBase
			implements GetStripCharsWhitespace, GetTestDataNoStrip {
	}

	public static class Strip_AnyChar_Change_Test extends Strip_TestBase
			implements GetStripCharsAnyChar, GetTestDataStrip {
	}

	public static class Strip_AnyChar_NoChange_Test extends Strip_TestBase
			implements GetStripCharsAnyChar, GetTestDataNoStrip {
	}

	// truncate

	// String truncate(String str, int maxWidth)
	// TODO String truncate(String str, int offset, int maxWidth)

	void testTruncate() {
		test(Truncate_Change_Test.class);
		test(Truncate_NoChange_Test.class);
	}

	static class Truncate_TestBase extends TransformBase_TestBase {
		String inputShort = "01234";
		String inputLong = "0123456789ABCDEF";

		int len = 7;
		StringTruncater truncater = new StringTruncater.Builder().setTruncate(TruncateMode.TRUNCATE_RIGHT).setLength(len).build();

		@TestMethod
		public CharSequence testStringTruncater(String input) {
			return truncater.truncate(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.truncate(input, len);
		}

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	public static class Truncate_Change_Test extends Truncate_TestBase {
		{
			setInput(inputLong);
		}
	}

	public static class Truncate_NoChange_Test extends Truncate_TestBase {
		{
			setInput(inputShort);
		}
	}

	//  pad

	static class Pad_TestBase extends TransformBase_TestBase {
		char fillChar = '-';
		String fillStr = "<>";
		int len = 10;

		String inputPad = "01234";
		String inputNoPad = "0123456789ABCDEF";

		@TestData
		IList<String> inputs;

		void setInput(String input) {
			inputs = GapList.create(input);
		}
	}

	static class LeftPad_TestBase extends Pad_TestBase {
		PadMode padMode = PadMode.LEFT;
	}

	static class RightPad_TestBase extends Pad_TestBase {
		PadMode padMode = PadMode.RIGHT;
	}

	static class Center_TestBase extends Pad_TestBase {
		PadMode padMode = PadMode.CENTER;
	}

	static class LeftPad_Char_TestBase extends LeftPad_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadChar(fillChar).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.leftPad(input, len, fillChar);
		}
	}

	static class LeftPad_String_TestBase extends LeftPad_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadString(fillStr).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.leftPad(input, len, fillStr);
		}
	}

	static class RightPad_TestBase_String extends RightPad_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadString(fillStr).setAlignFillStr(true).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.rightPad(input, len, fillStr);
		}
	}

	static class RightPad_TestBase_Char extends RightPad_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadChar(fillChar).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.rightPad(input, len, fillChar);
		}
	}

	static class Center_TestBase_String extends Center_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadString(fillStr).setAlignFillStr(true).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.center(input, len, fillStr);
		}
	}

	static class Center_TestBase_Char extends Center_TestBase {
		StringPadder padder = new StringPadder.Builder().setPadMode(padMode).setLength(len).setPadChar(fillChar).build();

		@TestMethod
		public CharSequence testStringPadder(String input) {
			return padder.pad(input);
		}

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.center(input, len, fillChar);
		}
	}

	// LeftPad

	public static class LeftPad_Char_Change_Test extends LeftPad_Char_TestBase {
		{
			setInput(inputPad);
		}
	}

	public static class LeftPad_Char_NoChange_Test extends LeftPad_Char_TestBase {
		{
			setInput(inputNoPad);
		}
	}

	public static class LeftPad_String_Change_Test extends LeftPad_String_TestBase {
		{
			setInput(inputPad);
		}
	}

	public static class LeftPad_String_NoChange_Test extends LeftPad_String_TestBase {
		{
			setInput(inputNoPad);
		}
	}

	// RightPad

	public static class RightPad_Char_Change_Test extends RightPad_TestBase_Char {
		{
			setInput(inputPad);
		}
	}

	public static class RightPad_Char_NoChange_Test extends RightPad_TestBase_Char {
		{
			setInput(inputNoPad);
		}
	}

	public static class RightPad_String_Change_Test extends RightPad_TestBase_String {
		{
			setInput(inputPad);
		}
	}

	public static class RightPad_String_NoChange_Test extends RightPad_TestBase_String {
		{
			setInput(inputNoPad);
		}
	}

	// Center

	public static class Center_Char_Change_Test extends Center_TestBase_Char {
		{
			setInput(inputPad);
		}
	}

	public static class Center_Char_NoChange_Test extends Center_TestBase_Char {
		{
			setInput(inputNoPad);
		}
	}

	public static class Center_String_Change_Test extends Center_TestBase_String {
		{
			setInput(inputPad);
		}
	}

	public static class Center_String_NoChange_Test extends Center_TestBase_String {
		{
			setInput(inputNoPad);
		}
	}

	// String leftPad(String str, int size) == leftPad(str, size, ' ')
	// String leftPad(String str, int size, char padChar)
	// String leftPad(String str, int size, String padStr)

	// String rightPad(String str, int size) == rightPad(str, size, ' ')
	// String rightPad(String str, int size, char padChar)
	// String rightPad(String str, int size, String padStr)

	// String center(String str, int size) == center(str, size, ' ')
	// String center(String str, int size, char padChar)
	// String center(String str, int size, String padStr)

	void testPad() {
		test(LeftPad_Char_Change_Test.class);
		test(LeftPad_Char_NoChange_Test.class);
		test(LeftPad_String_Change_Test.class);
		test(LeftPad_String_NoChange_Test.class);

		test(RightPad_Char_Change_Test.class);
		test(RightPad_Char_NoChange_Test.class);
		test(RightPad_String_Change_Test.class);
		test(RightPad_String_NoChange_Test.class);

		test(Center_Char_Change_Test.class);
		test(Center_Char_NoChange_Test.class);
		test(Center_String_Change_Test.class);
		test(Center_String_NoChange_Test.class);
	}

	//

	public static class StringUtilsWrapJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringUtilsWrapJmhTest.class);
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String WRAP = "'";
			StringWrapper sw = StringWrapper.builder().setWrapString(WRAP).setWrapMode(WrapMode.HEAD_AND_TAIL).setWrapAlways(true).build();
			CyclicSource<String> strings = new CyclicSource<>("ab", "abcd");
		}

		@Benchmark
		public String testStringWrapper(MyState state) {
			return state.sw.wrap(state.strings.next());
		}

		@Benchmark
		public String test(MyState state) {
			return StringUtils.wrap(state.strings.next(), state.WRAP);
		}
	}

	public static class StringSplitJmhTest extends StringJmhBenchmark {

		public StringSplitJmhTest() {
			setRunVerify(false);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> cs = new CyclicSource<>("ASFRules");
			IStringMatcher sm = new StringCharacterTypeCamelCaseMatcher();
			StringSplitter sr = StringSplitter.builder().setFindMatcher(sm).build();
		}

		@Benchmark
		public Object testStringUtil(MyState state) {
			return StringUtils.splitByCharacterTypeCamelCase(state.cs.next());
		}

		@Benchmark
		public Object testStringSplitter(MyState state) {
			return state.sr.split(state.cs.next());
		}

		// Matcher which creates an empty match if the character type changes
		static class StringCharacterTypeCamelCaseMatcher implements IStringMatcher {
			// Matcher must be stateful 
			int add;

			@Override
			public IMatch find(CharSequence str, int start) {
				start += add;
				add = 0;

				int len = str.length();
				if (start >= len) {
					return null;
				}
				int index = start;
				int type = Character.getType(str.charAt(index));
				while (index < len) {
					int t = Character.getType(str.charAt(index));
					if (t != type) {
						if (t == Character.LOWERCASE_LETTER && type == Character.UPPERCASE_LETTER) {
							// store information about the past match returned to advance on next call
							add = 1;
							return new Match(str, index - 1, index - 1);
						} else {
							return new Match(str, index, index);
						}
					}
					index++;
				}
				return null;
			}
		}
	}

	public static class StringJoinJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String[] arrs = new String[] { "0123456789", "0123456789", "0123456789", "0123456789", "0123456789" };
			IList<String> strs = GapList.create("0123456789", "0123456789", "0123456789", "0123456789", "0123456789");
		}

		@Benchmark
		public String testStringUtilJoin(MyState state) {
			return StringUtils.join(state.arrs);
		}

		@Benchmark
		public String testJoin1(MyState state) {
			return join1(state.strs);
		}

		@Benchmark
		public String testJoin2(MyState state) {
			return join2(state.strs);
		}

		public static String join1(Collection<String> strs) {
			int len = 0;
			for (String str : strs) {
				len += str.length();
			}
			StringBuilder buf = new StringBuilder(len);
			for (String str : strs) {
				buf.append(str);
			}
			return buf.toString();
		}

		public static String join2(Collection<String> strs) {
			StringBuilder buf = new StringBuilder();
			for (String str : strs) {
				buf.append(str);
			}
			return buf.toString();
		}
	}

	public static class WrapUnwrapJmhTest {

		static void test() {
			Options opts = new Options().includeClass(WrapUnwrapJmhTest.class);
			//opts.setJavaVersions(GapList.create(TestTools.JdkCommands11, TestTools.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhTool jr = new JmhTool();
			JmhRunner ju = jr.getRunner();
			ju.verifyJmhMethods(opts, 10);
			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char unwrap = '-';
			char wrap = '=';
			String mid = StringTools.repeat("x", 1000);
			String str = "-" + mid + "-";
			CyclicSource<String> cs = new CyclicSource<>(str);
		}

		@Benchmark
		public String test(MyState state) {
			String s = StringUtils.unwrap(state.cs.next(), state.unwrap);
			return StringUtils.wrap(s, state.wrap);
		}

		@Benchmark
		public String testManual(MyState state) {
			IString cs = new GapString(state.cs.next());
			if (StringTools.startsWith(cs, state.unwrap) && StringTools.endsWith(cs, state.unwrap)) {
				cs.remove(0);
				cs.remove(cs.length() - 1);
			}
			cs.add(0, state.wrap);
			cs.add(state.wrap);
			return cs.toString();
		}
	}

	public static class IndexOfDifferenceJmhTest {

		static void test() {
			Options opts = new Options().includeClass(IndexOfDifferenceJmhTest.class);
			opts.setRunTimeMillis(500);
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
			StringRoots sr = new StringRoots.Builder().build();
			CyclicSource<String> cs = new CyclicSource<>("ab", "ac", "abc", "abd");
		}

		@Benchmark
		public int test(MyState state) {
			String s0 = state.cs.next();
			String s1 = state.cs.next();
			return StringUtils.indexOfDifference(s0, s1);
		}

		@Benchmark
		public int testStringRoots(MyState state) {
			String s0 = state.cs.next();
			String s1 = state.cs.next();
			return state.sr.getCommonLength(s0, s1);
		}

		@Benchmark
		public int testStringRootsBuilder(MyState state) {
			String s0 = state.cs.next();
			String s1 = state.cs.next();
			StringRoots sr = new StringRoots.Builder().build();
			return sr.getCommonLength(s0, s1);
		}
	}

	public static class IndexOfAnyStringJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String[] finds = new String[] { "ab", "cd" };
			StringsMatcher sm = new StringsMatcher.Builder().setSearchStrs(finds).build();
			StringFinder sf = StringFinder.builder().setFindMatcher(sm).build();
			CyclicSource<String> cs = new CyclicSource<>("ab", "ac", "abc", "abd");
		}

		@Benchmark
		public int test(MyState state) {
			String s = state.cs.next();
			return StringUtils.indexOfAny(s, state.finds);
		}

		@Benchmark
		public int testStringFinder(MyState state) {
			String s = state.cs.next();
			return state.sf.indexOf(s);
		}
	}
}
