package org.magicwerk.strings;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Format.OutputType;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringAligner;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.StringAligner.Builder;
import org.slf4j.Logger;

/**
 * Test of class {@link StringAligner}.
 */
public class StringAlignerTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringAlignerTest().run();
	}

	void run() {
		testAlign();
		//testGeneric();

		//testBenchmarks();
		//runBenchmarks();
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		Builder b = StringAligner.builder();
		b.getPadderConfig().setPadChar('-');
		IStringTransformerBuilder builder = b.setLength(5);
		IList<String> inputs = GapList.create("1234", "12345", "123456");
		testStringTranformerGeneric(builder, inputs);
	}

	@Capture(outputType = OutputType.PRE)
	public void testAlign() {
		{
			StringAligner sa = StringAligner.builder().setLength(5).build();
			LOG.info("(" + sa.align("012") + ")");
			LOG.info("(" + sa.align("01234") + ")");
			LOG.info("(" + sa.align("0123456") + ")");
		}
		{
			StringAligner.Builder sab = StringAligner.builder().setLength(5);
			sab.getPadderConfig().setPadChar('x');
			sab.getTruncaterConfig().setTruncateMarker(".");
			StringAligner sa = sab.build();
			LOG.info("(" + sa.align("012") + ")");
			LOG.info("(" + sa.align("01245") + ")");
			LOG.info("(" + sa.align("0123456") + ")");
		}
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringAlignerTruncateBenchmarkTest.class, StringAlignerPadderBenchmarkTest.class);
	}

	public static abstract class StringAlignerBenchmarkTestBase {

		// TODO check len=0, len=1

		int len = 5;
		char padChar = 'x';
		String truncateMarker = ".";

		StringAligner stringAligner;
		{
			StringAligner.Builder sab = StringAligner.builder().setLength(len);
			sab.getPadderConfig().setPadChar('x');
			sab.getTruncaterConfig().setTruncateMarker(".");
			stringAligner = sab.build();
		}

		@TestMethod
		public String testStringPadder(String str) {
			return stringAligner.align(str);
		}
	}

	public static class StringAlignerTruncateBenchmarkTest extends StringAlignerBenchmarkTestBase {

		@TestData
		IList<String> inputs = GapList.create("abcde", "abcdefg");

		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.abbreviate(str, truncateMarker, len);
		}
	}

	public static class StringAlignerPadderBenchmarkTest extends StringAlignerBenchmarkTestBase {

		@TestData
		IList<String> inputs = GapList.create("abcde", "abc");

		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.rightPad(str, len, padChar);
		}
	}

}
