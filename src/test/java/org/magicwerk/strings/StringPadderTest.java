package org.magicwerk.strings;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Format;
import org.magictest.client.Format.OutputType;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.StringPadder;
import org.magicwerk.strings.StringUnwrapper;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.StringPadder.PadMode;
import org.magicwerk.strings.StringPadder.StringPadderInline;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringPadder}.
 */
public class StringPadderTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringPadderTest().run();
	}

	void run() {
		//testCompare();
		//testPad();
		//testPadAll();
		testGeneric();

		//testBenchmarks();
		//runBenchmarks();

		//StringPadderTestJmh.test();
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringPadder.builder().setPadChar('-').setLength(5);
		IList<String> inputs = GapList.create("1234", "12345", "123456");
		testStringTranformerGeneric(builder, inputs);
	}

	@Capture
	public void testCompare() {
		String s = "0123456789";

		for (int i = 1; i <= 3; i++) {
			int len = 5 * i;
			LOG.info("len = {}", len);
			for (PadMode pm : PadMode.values()) {
				LOG.info("padMode = {}", pm);
				StringPadder sp = StringPadder.builder().setPadChar('.').setPadMode(pm).setLength(len).build();
				String r = sp.pad(s);
				LOG.info("{}", r);

				IString ms = new GapString(s);
				sp.padInline(ms);
				CheckTools.check(ms.toString().equals(r));
			}
		}
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringPadder$StringPadderInline", //
			formats = { @Format(apply = Trace.RESULT, outputType = OutputType.PRE) })
	public void testPad() {
		String s = "abc";
		String s2 = "abcd";

		StringPadderInline padder = StringPadder.inline();
		padder.setLength(6);
		padder.setPadChar('.');

		padder.setPadMode(PadMode.RIGHT).pad(s);
		padder.setPadMode(PadMode.CENTER).pad(s);
		padder.setCenterRight(true).pad(s);
		padder.setCenterRight(false);
		padder.setPadMode(PadMode.LEFT).pad(s);

		padder.setPadString("<>");

		padder.setPadMode(PadMode.RIGHT).pad(s);
		padder.setPadMode(PadMode.RIGHT).pad(s2);
		padder.setPadMode(PadMode.CENTER).pad(s);
		padder.setPadMode(PadMode.CENTER).pad(s2);
		padder.setCenterRight(true);
		padder.pad(s);
		padder.pad(s2);
		padder.setCenterRight(false);
		padder.setPadMode(PadMode.LEFT).pad(s);
		padder.setPadMode(PadMode.LEFT).pad(s2);

		padder = StringPadder.inline().setLength(6);
		padder.pad(s);
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringPadder$StringPadderInline", //
			formats = { @Format(apply = Trace.ALL_PARAMS, outputType = OutputType.PRE) })
	public void testPadAll() {
		String s = "abc";
		String s2 = "abcd";
		String s3 = "abcdef";
		IList<String> strs = GapList.create(s, s2, s3);

		StringPadderInline padder = StringPadder.inline();
		padder.setPadChar('.');

		padder.setPadMode(PadMode.RIGHT).padAll(strs.copy());
		padder.setPadMode(PadMode.CENTER).padAll(strs.copy());
		//		padder.setPadMode(PadMode.CENTER_LEFT).padAll(strs.copy());
		padder.setPadMode(PadMode.LEFT).padAll(strs.copy());

		padder.setPadString("<>");

		padder.setPadMode(PadMode.RIGHT).padAll(strs.copy());
		padder.setPadMode(PadMode.CENTER).padAll(strs.copy());
		//		padder.setPadMode(PadMode.CENTER_LEFT).padAll(strs.copy());
		padder.setPadMode(PadMode.LEFT).padAll(strs.copy());
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringPadderLeftLen3BenchmarkTest.class, StringPadderLeftLen9BenchmarkTest.class,
				StringPadderRightLen3BenchmarkTest.class, StringPadderRightLen9BenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringUnwrapper}.
	 */
	public static abstract class StringPadderBenchmarkTestBase {

		char pad = 'x';
		int len;
		StringPadder stringPadder;

		@TestData
		IList<String> inputs = GapList.create("abc", "abcde");

		@TestMethod
		public String testStringPadder(String str) {
			return stringPadder.pad(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringPadderLeftLen3BenchmarkTest extends StringPadderBenchmarkTestBase {
		{
			len = 3;
			stringPadder = new StringPadder.Builder().setPadMode(PadMode.LEFT).setLength(len).setPadChar(pad).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.leftPad(str, len);
		}
	}

	public static class StringPadderLeftLen9BenchmarkTest extends StringPadderBenchmarkTestBase {
		{
			len = 9;
			stringPadder = new StringPadder.Builder().setPadMode(PadMode.LEFT).setLength(len).setPadChar(pad).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.leftPad(str, len, pad);
		}
	}

	public static class StringPadderRightLen3BenchmarkTest extends StringPadderBenchmarkTestBase {
		{
			len = 3;
			stringPadder = new StringPadder.Builder().setPadMode(PadMode.RIGHT).setLength(len).setPadChar(pad).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.rightPad(str, len);
		}
	}

	public static class StringPadderRightLen9BenchmarkTest extends StringPadderBenchmarkTestBase {
		{
			len = 9;
			stringPadder = new StringPadder.Builder().setPadMode(PadMode.RIGHT).setLength(len).setPadChar(pad).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.rightPad(str, len, pad);
		}
	}

	//

	/** Show that StringPadder is as fast as StringUtils */
	public static class StringPadderTestJmh {

		public static void test() {
			// Check memory usage:
			// - add "JmhAllocationObserverState" as parameter to benchmark
			// - reduce runtime to avoid GC: opts.setRunTimeMillis(50)
			// - comment out: // runner.verifyJmhMethods(opts, 10);

			Options opts = new Options().includeClass(StringPadderTestJmh.class).setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			//runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final int LEN = 3;
			final char FILL = '0';
			final StringPadder padder = new StringPadder.Builder().setLength(LEN).setPadChar(FILL).setPadMode(PadMode.RIGHT).build();

			CyclicSource<String> source = new CyclicSource<>("a", "ab", "abc");
		}

		@Benchmark
		public String testStringPadder(MyState state) {
			return state.padder.pad(state.source.next());
		}

		@Benchmark
		public String testStringPadderBuilder(MyState state) {
			StringPadder padder = new StringPadder.Builder().setLength(state.LEN).setPadChar(state.FILL).setPadMode(PadMode.RIGHT).build();
			return padder.pad(state.source.next());
		}

		@Benchmark
		public String testStringPadderInline(MyState state) {
			StringPadder padder = new StringPadderInline().setLength(state.LEN).setPadChar(state.FILL).setPadMode(PadMode.RIGHT);
			return padder.pad(state.source.next());
		}

		@Benchmark
		public String testStringUtilsPad(MyState state) {
			return StringUtils.rightPad(state.source.next(), state.LEN, state.FILL);
		}
	}

}
