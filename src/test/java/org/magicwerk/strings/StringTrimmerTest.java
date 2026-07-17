package org.magicwerk.strings;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.AllocationHeapDumpObserver;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.primitive.ICharList;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.StringTrimmer;
import org.magicwerk.strings.StringUnwrapper;
import org.magicwerk.strings.StringWrapper;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringTrimmer.CollapseMode;
import org.magicwerk.strings.StringTrimmer.TrimMode;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CharToolsTest;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.matcher.CharPredicateMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.objects.Tuple;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.CharMatcher;

/**
 * Test of class {@link StringTrimmer}.
 */
public class StringTrimmerTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringTrimmerTest().run();
	}

	void run() {
		//testManual();
		//testBenchmarks();
		//runBenchmarks();

		testCollapse();
		//testGeneric();
		//testLocation();
		//testTrim();
		//testTrim2();

		//new StringTrimmerImplTestJmh().test();
		//new MutableStringExample1TestJmh().test();
		//new MutableStringExample2TestJmh().test();
		//new StringTrimmerTestJmh().test();
		//new StringTrimmerInlineTestJmh().test();
	}

	void testManual() {
		CharPredicate cp = c -> c == '.' || c == ',';
		StringTrimmer r = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(TrimMode.TAIL).build();
		String s = r.trim(".,.");
		LOG.info("{}", s);
	}

	@Capture
	public void testCollapse() {
		CharPredicate cp = CharPredicates.of('.');
		doTestCollapse("abc", cp, TrimMode.HEAD_TAIL, null, null);
		doTestCollapse("abc", cp, TrimMode.HEAD_TAIL, CollapseMode.BODY, "-");
		doTestCollapse("..abc..", cp, TrimMode.HEAD_TAIL, CollapseMode.BODY, "-");
		doTestCollapse("..a..b..", cp, TrimMode.HEAD_TAIL, CollapseMode.BODY, "-");
		doTestCollapse("..a..b..c..", cp, TrimMode.HEAD_TAIL, CollapseMode.BODY, "-");
	}

	void doTestCollapse(String str, CharPredicate trimPredicate, TrimMode trimMode, CollapseMode collapseMode, String collapseStr) {
		StringTrimmer st = StringTrimmer
				.build(b -> b.setFindCharPredicate(trimPredicate).setTrimMode(trimMode).setCollapseMode(collapseMode).setCollapseString(collapseStr));
		String s = st.trim(str);
		LOG.info("{} -> {} (trimMode: {}, collapseMode: {}", str, s, trimMode, collapseMode);

		CharPredicateMatcher cpm = new CharPredicateMatcher(trimPredicate);
		StringTrimmer st2 = StringTrimmer
				.build(b -> b.setFindMatcher(cpm).setTrimMode(trimMode).setCollapseMode(collapseMode).setCollapseString(collapseStr));
		String s2 = st2.trim(str);
		CheckTools.check(s2.equals(s));
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringTrimmer.builder().setFindChar('-').setTrimMode(TrimMode.HEAD_TAIL);
		IList<String> inputs = GapList.create("123", "-123-");
		testStringTranformerGeneric(builder, inputs);
	}

	@Capture
	public void testTrim2() {
		IList<String> strs = GapList.create("-=abc=-", "def");
		CharPredicate cp = c -> c == '-' || c == '=';
		for (String str : strs) {
			for (TrimMode loc : TrimMode.values()) {
				StringTrimmer st = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(loc).build();
				String s = st.trim(str);
				LOG.info("{} ({}): {}", str, loc, s);
			}
		}
	}

	@Trace
	public void testTrim() {
		CharPredicate cp = c -> c == '.' || c == ',';
		StringTrimmer r = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(TrimMode.HEAD_TAIL).build();
		doTestRemove(r, "abc");
		doTestRemove(r, ".abc");
		doTestRemove(r, ".,abc");
		doTestRemove(r, "abc,");
		doTestRemove(r, "abc,.");
		doTestRemove(r, ",abc,");
		doTestRemove(r, ".,abc,.");
		doTestRemove(r, ".,,.");

		r = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(TrimMode.HEAD).build();
		doTestRemove(r, ".,,.");

		r = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(TrimMode.TAIL).build();
		doTestRemove(r, ".,,.");
	}

	@InheritTrace
	void doTestRemove(StringTrimmer st, String str) {
		// Call remove() for @Trace
		st.trim(str);

		// Calls in lambdas are not traced
		CharSequence s0 = getIf(() -> st.trim(str));

		CharSequence s1 = getIf(() -> {
			IString s = new GapString(str);
			st.trimInline(s);
			return s;
		});

		CheckTools.check((s0 == null && s1 == null) || CharSequenceTools.equals(s0, s1));
	}

	CharSequence getIf(Callable<CharSequence> c) {
		return new RunnableExecutor().callIf(c);
	}

	@Capture
	public void testLocation() {
		for (TrimMode loc : TrimMode.values()) {
			for (String str : GapList.create("--aa--", "--aa==", "--")) {
				StringTrimmer st = StringTrimmer.builder().setFindChar('-').setTrimMode(loc).build();
				String s = st.trim(str);
				LOG.info("{}: {} -> {}", loc, str, s);
			}
		}
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringTrimmerBenchmarkTest.class, StringTrimmerWhitespaceBenchmarkTest.class,
				StringTrimmerStartBenchmarkTest.class, StringTrimmerEndBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringUnwrapper}.
	 */
	public static abstract class StringTrimmerBenchmarkTestBase {

		char wrapChar = '\'';
		String wrap = "'";
		StringTrimmer stringTrimmer;

		@TestData
		IList<String> inputs = GapList.create("ab", "'ab'", " ab ");

		@TestMethod
		public String testStringTrimmer(String str) {
			return stringTrimmer.trim(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringTrimmerBenchmarkTest extends StringTrimmerBenchmarkTestBase {
		{
			stringTrimmer = StringTrimmer.builder().setFindCharPredicate(CharPredicates.equals(wrapChar)).setTrimMode(TrimMode.HEAD_TAIL).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.strip(str, wrap);
		}
	}

	public static class StringTrimmerWhitespaceBenchmarkTest extends StringTrimmerBenchmarkTestBase {
		{
			stringTrimmer = StringTrimmer.builder().setFindCharPredicate(Character::isWhitespace).setTrimMode(TrimMode.HEAD_TAIL).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.strip(str);
		}
	}

	public static class StringTrimmerStartBenchmarkTest extends StringTrimmerBenchmarkTestBase {
		{
			stringTrimmer = StringTrimmer.builder().setFindCharPredicate(CharPredicates.equals(wrapChar)).setTrimMode(TrimMode.HEAD).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.stripStart(str, wrap);
		}
	}

	public static class StringTrimmerEndBenchmarkTest extends StringTrimmerBenchmarkTestBase {
		{
			stringTrimmer = StringTrimmer.builder().setFindCharPredicate(CharPredicates.equals(wrapChar)).setTrimMode(TrimMode.TAIL).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.stripEnd(str, wrap);
		}
	}

	//

	/**
	 * Show that the specialized implementation using a CharPredicate is 4 times faster than the general one for IStringMatcher.
	 */
	public static class StringTrimmerImplTestJmh extends StringJmhBenchmark {

		public StringTrimmerImplTestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char CHAR = 'a';
			char CHAR_IC = 'A';
			String input = StringTools.repeat(CHAR, 10) + StringTools.repeat('x', 100) + StringTools.repeat(CHAR, 10);

			StringTrimmer trimmer1 = StringTrimmer.builder().setTrimMode(TrimMode.HEAD_TAIL).setFindChar(CHAR).build();
			StringTrimmer trimmer2 = StringTrimmer.builder().setTrimMode(TrimMode.HEAD_TAIL).setFindChar(CHAR_IC).setIgnoreCase(true).build();
			IStringMatcher sm = new org.magicwerk.strings.matcher.CharMatcher(CHAR);
			StringTrimmer trimmer3 = StringTrimmer.builder().setTrimMode(TrimMode.HEAD_TAIL).setFindMatcher(sm).build();
			CyclicSource<String> strings = new CyclicSource<>(input);
		}

		@Benchmark
		public CharSequence testTrimChar(MyState state) {
			String s = state.strings.next();
			return state.trimmer1.apply(s);
		}

		@Benchmark
		public CharSequence testTrimCharIgnoreCase(MyState state) {
			String s = state.strings.next();
			return state.trimmer2.apply(s);
		}

		@Benchmark
		public CharSequence testTrimMatcher(MyState state) {
			String s = state.strings.next();
			return state.trimmer3.apply(s);
		}
	}

	/** StringRemover is as fast as StringUtils, but slower than as CharMatcher  */
	public static class MutableStringExample1TestJmh extends StringJmhBenchmark {

		public MutableStringExample1TestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
			setEqualsWithToString(true);
		}

		// Task: trim leading/trailing spaces (' ', '\t'), remove wrapping '-', add new wrapping '='
		@State(Scope.Benchmark)
		public static class MyState {
			int len = 200;
			String str = StringTools.repeat("" + CharToolsTest.CHAR_NO_LATIN1, len);
			char oldWrap = '-';
			char newWrap = '=';
			String trim = " \t";
			StringTrimmer trimmer = StringTrimmer.builder().setFindCharPredicate(CharPredicates.oneOf(trim)).build();
			StringUnwrapper removeWrapper = StringUnwrapper.builder().setUnwrapString("" + oldWrap).build();
			StringWrapper addWrapper = StringWrapper.builder().setWrapString("" + newWrap).build();
			CyclicSource<String> strings = new CyclicSource<>(" -" + str + "-\t");
		}

		@Benchmark
		public CharSequence testMutableString(MyState state) {
			String s = state.strings.next();
			IString cl = new GapString(s);
			state.trimmer.trimInline(cl);
			state.removeWrapper.unwrapInline(cl);
			state.addWrapper.wrapInline(cl);
			return cl;
		}

		@Benchmark
		public CharSequence testImmutableString(MyState state) {
			String s = state.strings.next();
			s = StringUtils.strip(s, state.trim);
			s = StringUtils.unwrap(s, state.oldWrap);
			s = StringUtils.wrap(s, state.newWrap);
			return s;
		}
	}

	public static class MutableStringExample2TestJmh extends StringJmhBenchmark {

		public MutableStringExample2TestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
			setEqualsWithToString(true);
		}

		// Task 1: trim leading/trailing spaces (' ', '\t'), remove wrapping '('/')', add new wrapping '['/']'
		// Task 2: add that old wrapping exists
		@State(Scope.Benchmark)
		public static class MyState {
			//int len = 100;
			int len = 1000;

			//char c = CharToolsTest.CHAR_LATIN1;
			char c = CharToolsTest.CHAR_NO_LATIN1;

			String str = StringTools.repeat("" + c, len);
			String oldWrapHead = "(";
			String oldWrapTail = ")";
			String newWrapHead = "[";
			String newWrapTail = "]";
			String trim = " \t";
			StringTrimmer trimmer = StringTrimmer.builder().setTrimMode(TrimMode.HEAD_TAIL).setFindCharPredicate(CharPredicates.oneOf(trim)).build();
			StringUnwrapper removeWrapper = StringUnwrapper.builder().setUnwrapHeadString(oldWrapHead).setUnwrapTailString(oldWrapTail)
					.setReturnMode(ReturnMode.THROW_EXCEPTION).build();
			StringWrapper addWrapper = StringWrapper.builder().setWrapHeadString(newWrapHead).setWrapTailString(newWrapTail).build();
			CyclicSource<String> strings = new CyclicSource<>(" (" + str + ")\t");
			RunnableExecutor executor = new RunnableExecutor();
		}

		@Benchmark
		public CharSequence testMutableString(MyState state) {
			String s = state.strings.next();
			return state.executor.callIf(() -> doTestMutableString(state, s));
		}

		@Benchmark
		public CharSequence testImmutableString(MyState state) {
			String s = state.strings.next();
			return state.executor.callIf(() -> doTestImmutableString(state, s));
		}

		CharSequence doTestMutableString(MyState state, String s) {
			IString cl = new GapString(s);
			state.trimmer.trimInline(cl);
			state.removeWrapper.unwrapInline(cl);
			state.addWrapper.wrapInline(cl);
			return cl;
		}

		CharSequence doTestImmutableString(MyState state, String s) {
			s = StringUtils.strip(s, state.trim);

			// StringUtils.unwrap does not support different marker for head/tail or checking for existence
			//s = StringUtils.unwrap(s, state.oldWrap);
			if (!(s.startsWith(state.oldWrapHead) && s.endsWith(state.oldWrapTail))) {
				throw new IllegalArgumentException("Missing wrapping");
			}
			s = s.substring(state.oldWrapHead.length(), s.length() - state.oldWrapTail.length());

			// StringUtils.wrap does not support different marker for head/tail
			s = state.newWrapHead + s + state.newWrapTail;
			return s;
		}

	}

	/** StringRemover is as fast as StringUtils, but slower than as CharMatcher  */
	public static class StringTrimmerTestJmh extends StringJmhBenchmark {

		public StringTrimmerTestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String removeChars = "-";
			CharPredicate cp = CharPredicates.oneOf(removeChars);
			StringTrimmer st = StringTrimmer.builder().setTrimMode(TrimMode.HEAD_TAIL).setFindCharPredicate(cp).build();
			CharMatcher cm = CharMatcher.anyOf(removeChars);
			CyclicSource<String> strings = new CyclicSource<>("a", "-a", "a-", "-a-");
		}

		@Benchmark
		public Object testStringTrimmer(MyState state) {
			String s = state.strings.next();
			return state.st.trim(s);
		}

		@Benchmark
		public Object testStringUtils(MyState state) {
			String s = state.strings.next();
			return StringUtils.strip(s, state.removeChars);
		}

		@Benchmark
		public Object testCharMatcher(MyState state) {
			String s = state.strings.next();
			return state.cm.trimFrom(s);
		}
	}

	/** StringRemover is as fast as StringUtils, but twice as slow as CharMatcher  */
	public static class StringTrimmerInlineTestJmh extends StringJmhBenchmark {

		public StringTrimmerInlineTestJmh() {
			setCompareParams(false);
			setJavaVersions(JavaVersion.JAVA_21);
			//setRunVerify(false);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			@Param({ "" + CharToolsTest.CHAR_LATIN1, "" + CharToolsTest.CHAR_NO_LATIN1 })
			String fill;

			@Param({ "10", "100", "1000" })
			int len;

			String headChar = "<";
			String tailChar = ">";
			CharPredicate headCp = CharPredicates.oneOf(headChar);
			CharPredicate tailCp = CharPredicates.oneOf(tailChar);
			StringTrimmer headSt = StringTrimmer.builder().setTrimMode(TrimMode.HEAD).setFindCharPredicate(headCp).build();
			StringTrimmer tailSt = StringTrimmer.builder().setTrimMode(TrimMode.TAIL).setFindCharPredicate(tailCp).build();
			CharMatcher headCm = CharMatcher.anyOf("<");
			CharMatcher tailCm = CharMatcher.anyOf(">");
			String str;
			CyclicSource<String> strings;
			CyclicSource<IString> charlists;

			@Setup
			public void init() {
				// Note that as len is provided by a @Param, it cannot be used in the constructor, but only in the @Setup method
				str = StringTools.repeat(fill, len);
				// StringTrimmer is faster, as 2 copy operations are needed
				strings = new CyclicSource<>("<" + str + ">");
				// StringTrimmer is slower, as just one or no copy operation is needed
				//strings = new CyclicSource<>("<>", "<" + str, str + ">", "<" + str + ">", str);
				charlists = new CyclicSource<>(strings.getAll().map(GapString::new));
			}
		}

		@Benchmark
		public int testStringTrimmer(MyState state) {
			//ICharList cl = state.charlists.next();
			String s = state.strings.next();
			IString cl = new GapString(s);
			state.headSt.trimInline(cl);
			state.tailSt.trimInline(cl);
			return cl.length();
		}

		@Benchmark
		public int testGuava(MyState state) {
			String s = state.strings.next();
			s = state.headCm.trimLeadingFrom(s);
			s = state.tailCm.trimTrailingFrom(s);
			return s.length();
		}
	}

	/** Show that a mutable string outperforms java.lang.String  */
	// FIXME strange behavior
	public static class StringTrimmerMutableTestJmh extends StringJmhBenchmark {

		public StringTrimmerMutableTestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
		}

		void analyzeHeapDumps() {
			AllocationHeapDumpObserver o = new AllocationHeapDumpObserver();
			o.setHprofFile(FilePath.of("C:\\Users\\thoma\\AppData\\Local\\Temp\\heapdump-1745443930546.hprof"));
			IList<Tuple<Integer, String>> allocatedInstances = o.getAllocatedInstances();
			LOG.info("allocatedInstances: {}", allocatedInstances);
		}

		@State(Scope.Benchmark)
		public static class MyState {

			String getString(int i) {
				if (i % 2 == 0) {
					return StringTools.repeat("x", i);
				} else {
					return "(" + i + StringTools.repeat("x", i) + i + ")";
				}
			}

			CyclicSource<String> strings = new CyclicSource<>(10, i -> getString(i));
			CyclicSource<ICharList> charlists = new CyclicSource<>(10, i -> new GapString(getString(i)));
			StringTrimmer sr = StringTrimmer.builder().setFindCharPredicate(c -> c == '(' || c == ')').setTrimMode(TrimMode.HEAD_TAIL).build();
			StringTrimmer sr0 = StringTrimmer.builder().setFindCharPredicate(c -> c == '(').setTrimMode(TrimMode.HEAD_TAIL).build();
			StringTrimmer sr1 = StringTrimmer.builder().setFindCharPredicate(c -> c == ')').setTrimMode(TrimMode.HEAD_TAIL).build();
		}

		@Benchmark
		public Object testStringRemover1(MyState state) {
			String s = state.strings.next();
			return state.sr.trim(s);
		}

		@Benchmark
		public Object testStringRemover1Inline(MyState state) {
			String s = state.strings.next();
			IString cl = new GapString(s);
			state.sr.trimInline(cl);
			s = cl.toString();
			return s;
		}

		@Benchmark
		public Object testStringRemover2(MyState state) {
			String s = state.strings.next();
			s = state.sr0.trim(s);
			s = state.sr1.trim(s);
			return s;
		}

		@Benchmark
		public Object testStringRemover2Inline(MyState state) {
			String s = state.strings.next();
			IString cl = new GapString(s);
			state.sr0.trimInline(cl);
			state.sr1.trimInline(cl);
			s = cl.toString();
			return s;
		}

		//			public Object testStringRemoverMutableStringCreated0(MyState state, JmhAllocationHeapDumpObserverState s) {
		//@Benchmark
		public Object testStringRemoverMutableStringCreated0(MyState state) {
			ICharList cl = new GapString(state.strings.next());
			return cl.toString();
		}

		//@Fork(jvmArgs = { "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+PrintInlining", "-verbose:gc" })
		//@Fork(jvmArgs = { "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+PrintInlining", "-verbose:gc", "-XX:+UseEpsilonGC" })
		//@Fork(jvmArgs = { "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC" })
		//@Benchmark
		//public Object testStringRemoverMutableStringCreated1(MyState state, JmhAllocationJfrObserverState s) {
		//public Object testStringRemoverMutableStringCreated1(MyState state, JmhAllocationHeapDumpObserverState s) {
		public Object testStringRemoverMutableStringCreated1(MyState state) {
			IString cl = new GapString(state.strings.next());
			state.sr.trimInline(cl);
			return cl.toString();
		}

		//@Benchmark
		public Object testStringRemoverMutableStringCreated2(MyState state) {
			//public Object testStringRemoverMutableStringCreated2(MyState state, JmhAllocationHeapDumpObserverState s) {
			//public Object testStringRemoverMutableStringCreated2(MyState state, JmhAllocationJfrObserverState s) {
			IString cl = new GapString(state.strings.next());
			//char[] vs = (char[]) ReflectTools.getAnyFieldValue(cl, "values");
			//state.sr0.removeInline(cl);
			//state.sr0.removeInline(cl);
			state.sr1.trimInline(cl);
			state.sr1.trimInline(cl);
			//char[] vs2 = (char[]) ReflectTools.getAnyFieldValue(cl, "values");
			//CheckTools.check(vs2 == vs);
			return cl.toString();
		}

	}
}
