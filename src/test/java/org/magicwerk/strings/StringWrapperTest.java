package org.magicwerk.strings;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.InheritTrace;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.StringWrapper;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.StringWrapper.WrapMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test class of {@link StringWrapper}.
 */
public class StringWrapperTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringWrapperTest().run();
	}

	void run() {
		//testManual();

		//test();
		testGeneric();
		//testRemove();

		//testBenchmarks();
		//runBenchmarks();
		//new StringWrapperRemoveJmhTest().test();
	}

	void testManual() {
		StringWrapper sw = StringWrapper.build(b -> b.setReturnMode(ReturnMode.RETURN_NULL));
	}

	@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringWrapper.builder().setWrapString("-");
		IList<String> inputs = GapList.create("123", "-123-");
		testStringTranformerGeneric(builder, inputs);
	}

	@Trace(traceMethod = "/wrap|isWrapped/")
	public void test() {
		String str = "=";
		{
			Report.printStep("=== wrapString('=') ===");
			StringWrapper.Builder swb = StringWrapper.builder().setWrapString(str);
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
		{
			Report.printStep("=== wrapMatcher('=') ===");
			StringMatcher sm = StringMatcher.of(str);
			StringWrapper.Builder swb = StringWrapper.builder().setMatchMatcher(sm).setWrapString(str);
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
		{
			Report.printStep("=== wrapMatcher('=').wrapString('#') ===");
			StringMatcher sm = StringMatcher.of(str);
			StringWrapper.Builder swb = StringWrapper.builder().setMatchMatcher(sm).setWrapString("#");
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
		{
			Report.printStep("=== wrapMatcher(Regex('=')).wrapString('#') ===");
			RegexStringMatcher rsm = new RegexStringMatcher().setPattern(str);
			StringWrapper.Builder swb = StringWrapper.builder().setMatchMatcher(rsm).setWrapString("#");
			test(swb, WrapMode.HEAD_AND_TAIL);
			test(swb, WrapMode.HEAD_OR_TAIL);
			test(swb, WrapMode.HEAD);
			test(swb, WrapMode.TAIL);
		}
	}

	@InheritTrace
	void test(StringWrapper.Builder swb, WrapMode loc) {
		swb.setWrapMode(loc);
		test(swb, loc, "ab");
		test(swb, loc, "=ab=");
		test(swb, loc, "=ab");
		test(swb, loc, "ab=");
	}

	@InheritTrace
	void test(StringWrapper.Builder swb, WrapMode loc, String str) {
		Report.printStep(loc.toString());
		swb.setWrapAlways(true);
		StringWrapper sw = swb.build();
		boolean wrapped = sw.isWrapped(str);

		Report.printStep("wrapAlways= true");
		String s = sw.wrap(str);
		assert s.length() > str.length();

		Report.printStep("wrapAlways= false");
		swb.setWrapAlways(false);
		sw = swb.build();
		String str2 = sw.wrap(str);
		CheckTools.check(str2 == str || !str2.equals(str));
		CheckTools.check(wrapped == (str2 == str));
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringWrapperWrapBenchmarkTest.class, StringWrapperWrapIfMissingBenchmarkTest.class,
				StringWrapperPrependIfMissingBenchmarkTest.class,
				StringWrapperAppendIfMissingBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringWrapper}.
	 */
	public static abstract class StringWrapperBenchmarkTestBase {

		String wrap = "'";
		StringWrapper stringWrapper;

		@TestData
		IList<String> inputs = GapList.create("ab", "'ab'", " ab ");

		@TestMethod
		public String testStringWrapper(String str) {
			return stringWrapper.wrap(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringWrapperWrapBenchmarkTest extends StringWrapperBenchmarkTestBase {
		{
			stringWrapper = StringWrapper.builder().setWrapString(wrap).setWrapMode(WrapMode.HEAD_AND_TAIL).setWrapAlways(true).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.wrap(str, wrap);
		}
	}

	public static class StringWrapperWrapIfMissingBenchmarkTest extends StringWrapperBenchmarkTestBase {
		{
			stringWrapper = StringWrapper.builder().setWrapString(wrap).setWrapMode(WrapMode.HEAD_AND_TAIL).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.wrapIfMissing(str, wrap);
		}
	}

	public static class StringWrapperPrependIfMissingBenchmarkTest extends StringWrapperBenchmarkTestBase {
		{
			stringWrapper = StringWrapper.builder().setWrapString(wrap).setWrapMode(WrapMode.HEAD).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.prependIfMissing(str, wrap);
		}
	}

	public static class StringWrapperAppendIfMissingBenchmarkTest extends StringWrapperBenchmarkTestBase {
		{
			stringWrapper = StringWrapper.builder().setWrapString(wrap).setWrapMode(WrapMode.TAIL).build();
		}

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.appendIfMissing(str, wrap);
		}
	}

}
