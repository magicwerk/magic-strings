package org.magicwerk.strings;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringTruncater;
import org.magicwerk.strings.StringUnwrapper;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringTruncater.TruncateMode;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringTruncater}.
 */
public class StringTruncaterTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringTruncaterTest().run();
	}

	void run() {
		testTruncate();
		//testGeneric();

		//testBenchmarks();
		//runBenchmarks();

		//new StringTruncaterTestJmh().test();
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringTruncater.builder().setLength(5);
		IList<String> inputs = GapList.create("1234", "12345", "123456");
		testStringTranformerGeneric(builder, inputs);
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringTruncater$Builder", traceMethod = "build")
	public void testTruncateBuilder() {
		String marker = "-";
		{
			new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setKeepLeft(3).setKeepRight(3).build();
		}
		{
			new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setKeepLeft(2).setKeepRight(2).build();
		}
	}

	@Trace
	public void testTruncate() {
		String marker = "-";
		String str = "0123456789";
		{
			// default is TruncateMode.RIGHT
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_LEFT);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_CENTER);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(6).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_CENTER);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(6).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_CENTER)
					.setCenterRight(true);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_LEFT_RIGHT);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_LEFT_RIGHT)
					.setCenterRight(true);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setKeepLeft(2);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = new StringTruncater.Builder().setLength(7).setTruncateMarker(marker).setKeepRight(2);
			truncate(tt, str);
		}

		// Truncate marker is longer than length
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncated)");
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncated)").setTruncate(TruncateMode.TRUNCATE_LEFT);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncated)").setTruncate(TruncateMode.TRUNCATE_LEFT_RIGHT);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncated)").setTruncate(TruncateMode.TRUNCATE_LEFT_RIGHT)
					.setCenterRight(true);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncd)").setTruncate(TruncateMode.TRUNCATE_CENTER);
			truncate(tt, str);
		}
		{
			StringTruncater.Builder tt = StringTruncater.builder().setLength(5).setTruncateMarker("(truncd)").setTruncate(TruncateMode.TRUNCATE_CENTER)
					.setCenterRight(true);
			truncate(tt, str);
		}
	}

	@InheritTrace
	void truncate(StringTruncater.Builder stb, String str) {
		{
			StringTruncater st = stb.build();
			String s0 = st.truncate(str);

			CharSequence s1 = getIf(() -> {
				IString s = new GapString(str);
				st.truncateInline(s);
				return s;
			});

			CheckTools.check(CharSequenceTools.equals(s0, s1));
		}
		{
			stb.setTruncateMarker("");
			StringTruncater st = stb.build();
			String s0 = st.truncate(str);

			CharSequence s1 = getIf(() -> {
				IString s = new GapString(str);
				st.truncateInline(s);
				return s;
			});

			CheckTools.check(CharSequenceTools.equals(s0, s1));
		}
	}

	CharSequence getIf(Callable<CharSequence> c) {
		return new RunnableExecutor().callIf(c);
	}

	//

	// Missing
	// String abbreviate(final String str, final int offset, final int maxWidth)
	// String abbreviate(final String str, final String abbrevMarker, int offset, final int maxWidth)

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringTruncaterAbbreviateBenchmarkTest.class, StringTruncaterAbbreviateMiddleBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringUnwrapper}.
	 */
	public static abstract class StringTruncaterBenchmarkTestBase {

		String marker = "...";
		int length = 5;
		StringTruncater stringTruncater;

		@TestData
		IList<String> inputs = GapList.create("0123456789");

		@TestMethod
		public String testStringUnwrapper(String str) {
			return stringTruncater.truncate(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringTruncaterAbbreviateBenchmarkTest extends StringTruncaterBenchmarkTestBase {
		{
			stringTruncater = StringTruncater.builder().setLength(length).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_RIGHT).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			// StringUtils.abbreviate(String, int) uses "..." as marker
			return StringUtils.abbreviate(str, marker, length);
		}
	}

	public static class StringTruncaterAbbreviateMiddleBenchmarkTest extends StringTruncaterBenchmarkTestBase {
		{
			stringTruncater = StringTruncater.builder().setLength(length).setTruncateMarker(marker).setTruncate(TruncateMode.TRUNCATE_CENTER).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.abbreviateMiddle(str, marker, length);
		}
	}

	//

	public static class StringTruncaterTestJmh extends StringJmhBenchmark {

		public StringTruncaterTestJmh() {
			setJavaVersions(JavaVersion.JAVA_21, JavaVersion.JAVA_24);
			//setRunTime(3000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			int len = 5;
			StringTruncater st = new StringTruncater.Builder().setTruncate(TruncateMode.TRUNCATE_RIGHT).setLength(len).setTruncateMarker("").build();
			StringTruncater stLambda = new StringTruncater.Builder().setTruncate(TruncateMode.TRUNCATE_RIGHT).setLength(len).setTruncateMarker("").useLambda()
					.build();
			CyclicSource<String> strings = new CyclicSource<>(5, i -> StringTools.repeat("x", 10 + i));
		}

		@Benchmark
		public Object testStringTruncater(MyState state) {
			String s = state.strings.next();
			return state.st.truncate(s);
		}

		@Benchmark
		public Object testStringTruncaterLambda(MyState state) {
			String s = state.strings.next();
			return state.stLambda.truncate(s);
		}

		@Benchmark
		public Object testStringUtils(MyState state) {
			String s = state.strings.next();
			return StringUtils.truncate(s, state.len);
		}

	}

}
