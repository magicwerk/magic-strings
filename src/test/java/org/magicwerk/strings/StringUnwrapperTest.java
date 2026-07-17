package org.magicwerk.strings;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.InheritTrace;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.StringUnwrapper;
import org.magicwerk.strings.StringWrapper;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringWrapper.WrapMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.matcher.StringMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test class of {@link StringWrapper}.
 */
public class StringUnwrapperTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringUnwrapperTest().run();
	}

	void run() {
		//test();
		//testGeneric();
		//testRemove();

		testBenchmarks();
		//runBenchmarks();
		//new StringUnwrapperJmhTest().test();
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringUnwrapper.builder().setUnwrapString("-");
		IList<String> inputs = GapList.create("123", "-123-");
		testStringTranformerGeneric(builder, inputs);
	}

	@Trace(traceMethod = "/isWrapped|unwrap/")
	public void test() {
		String str = "=";
		{
			StringUnwrapper.Builder swb = StringUnwrapper.builder().setUnwrapString(str);
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
		{
			StringMatcher sm = StringMatcher.of(str);
			StringUnwrapper.Builder swb = StringUnwrapper.builder().setUnwrapMatcher(sm);
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
	}

	@InheritTrace
	void test(StringUnwrapper.Builder swb, WrapMode loc) {
		swb.setWrapMode(loc);
		StringUnwrapper sw = swb.build();
		Report.printStep(sw.getClass().getSimpleName() + " " + loc.toString());
		test(sw, "ab");
		test(sw, "=ab=");
		test(sw, "=ab");
		test(sw, "ab=");
	}

	@InheritTrace
	void test(StringUnwrapper sw, String str) {
		boolean wrapped = sw.isWrapped(str);
		String str2 = sw.unwrap(str);
		CheckTools.check(str2 == str || !str2.equals(str));
		CheckTools.check(wrapped != (str2 == str));
	}

	@Trace
	public void testUnwrap() {
		StringUnwrapper.Builder swb = StringUnwrapper.builder().setUnwrapHeadString("(").setUnwrapTailString(")").setReturnMode(ReturnMode.RETURN_UNCHANGED);

		swb.setWrapMode(WrapMode.HEAD_OR_TAIL).build().unwrap("(ab");
		swb.setWrapMode(WrapMode.HEAD_AND_TAIL).build().unwrap("(ab");

		swb.setWrapMode(WrapMode.HEAD_OR_TAIL).build().unwrap("(ab)");
		swb.setWrapMode(WrapMode.HEAD_AND_TAIL).build().unwrap("(ab)");
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringUnwrapperUnwrapBenchmarkTest.class, StringUnwrapperRemoveStartBenchmarkTest.class,
				StringUnwrapperRemoveEndBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringUnwrapper}.
	 */
	public static abstract class StringUnwrapperBenchmarkTestBase {

		String wrap = "'";
		StringUnwrapper stringUnwrapper;

		@TestData
		IList<String> inputs = GapList.create("ab", "'ab'", " ab ");

		@TestMethod
		public String testStringUnwrapper(String str) {
			return stringUnwrapper.unwrap(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringUnwrapperUnwrapBenchmarkTest extends StringUnwrapperBenchmarkTestBase {
		{
			stringUnwrapper = StringUnwrapper.builder().setUnwrapString(wrap).setWrapMode(WrapMode.HEAD_AND_TAIL).setReturnMode(ReturnMode.RETURN_UNCHANGED)
					.build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.unwrap(str, wrap);
		}
	}

	public static class StringUnwrapperRemoveStartBenchmarkTest extends StringUnwrapperBenchmarkTestBase {
		{
			stringUnwrapper = StringUnwrapper.builder().setUnwrapString(wrap).setWrapMode(WrapMode.HEAD).setReturnMode(ReturnMode.RETURN_UNCHANGED)
					.build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.removeStart(str, wrap);
		}
	}

	public static class StringUnwrapperRemoveEndBenchmarkTest extends StringUnwrapperBenchmarkTestBase {
		{
			stringUnwrapper = StringUnwrapper.builder().setUnwrapString(wrap).setWrapMode(WrapMode.TAIL).setReturnMode(ReturnMode.RETURN_UNCHANGED)
					.build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.removeEnd(str, wrap);
		}
	}

	//

	public static class StringUnwrapperJmhTest extends StringJmhBenchmark {

		public StringUnwrapperJmhTest() {
			setJavaVersions(JavaVersion.JAVA_24);
			//setRunTime(1000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String HEAD = "-";
			StringMatcher sm = StringMatcher.of(HEAD);
			CyclicSource<String> strings = new CyclicSource<>("-abc", "abc");
			StringUnwrapper stringUnwrapperUnchanged = StringUnwrapper.builder().setUnwrapHeadString(HEAD).setWrapMode(WrapMode.HEAD)
					.setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
			StringUnwrapper stringUnwrapperMatcherUnchanged = StringUnwrapper.builder().setUnwrapHeadMatcher(sm).setWrapMode(WrapMode.HEAD)
					.setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
			StringUnwrapper stringUnwrapperNull = StringUnwrapper.builder().setUnwrapString(HEAD).setWrapMode(WrapMode.HEAD)
					.setReturnMode(ReturnMode.RETURN_NULL).build();
		}

		@Benchmark
		public String testStringToolsUnchanged(MyState state) {
			return StringTools.removeHeadIf(state.strings.next(), state.HEAD);
		}

		@Benchmark
		public String testStringUnwrapperUnchanged(MyState state) {
			return state.stringUnwrapperUnchanged.unwrap(state.strings.next());
		}

		@Benchmark
		public String testStringUnwrrapperMatcherUnchanged(MyState state) {
			return state.stringUnwrapperMatcherUnchanged.unwrap(state.strings.next());
		}

		//@Benchmark
		public String testStringToolsNull(MyState state) {
			return StringTools.removeHead(state.strings.next(), state.HEAD);
		}

		//@Benchmark
		public String testStringUnwrapperNull(MyState state) {
			return state.stringUnwrapperNull.unwrap(state.strings.next());
		}
	}

}
