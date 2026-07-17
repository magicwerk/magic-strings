package org.magicwerk.strings.format;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.magictest.client.Capture;
import org.magictest.client.Formatter;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.strings.escape.StringBuilders.GrowingStringBuilder;
import org.magicwerk.brownies.core.strings.escape.StringEscapeTools.ThreadLocalCharBuffer;
import org.magicwerk.brownies.core.time.Timer;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.AllocationHeapDumpObserver;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.brownies.tools.dev.jvm.JmhTool;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.format.StringFormat;
import org.magicwerk.strings.format.StringFormatParsers;
import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.format.StringFormatParsers.NamedFormatParser;
import org.magicwerk.strings.format.StringFormatParsers.SingleCharFormatParser;
import org.magicwerk.strings.format.StringFormatParsers.StringFormatParser;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CollectionTools;
import org.magicwerk.strings.mapper.IStringMapper;
import org.magicwerk.strings.objects.Tuple;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringFormatter}.
 */
public class StringFormatterTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringFormatterTest().run();
	}

	void run() {
		//testFormat2();
		//		testFormat();
		//testFormat1();
		//testMessageFormatJdk();
		//testPerformance();
		//    	System.out.println();
		//    	testPerformance();

		StringFormatterJmhTest.test();
		//PerformanceJmhTest.test();
	}

	@Capture
	public void testFormat2() {
		LOG.info("Logger: {}", "a", "b");
		LOG.info("String.format: {}", String.format("%s", "a", "b"));
		LOG.info("MessageFormat.format: {}", MessageFormat.format("{0}", "a", "b"));
		LOG.info("StringFormatter.format: {}", StringFormatter.format("{}", "a", "b"));
		LOG.info("");

		Object[] objs = new Object[] { "a", "b" };
		LOG.info("Logger: {}", objs);
		LOG.info("String.format: {}", String.format("%s", objs));
		LOG.info("MessageFormat.format: {}", MessageFormat.format("{0}", objs));
		LOG.info("StringFormatter.format: {}", StringFormatter.format("{}", objs));
		LOG.info("");

		// Without cast to Object[], a warning is raised
		// warning: non-varargs call of varargs method with inexact argument type for last parameter;
		// It is not suppressed by @SuppressWarnings("varargs"), but only by @SuppressWarnings("all")
		// Do not cast to Object, it will pass the array as single argument to the varargs call
		String[] strs = new String[] { "a", "b" };
		LOG.info("Logger: {}", (Object) strs);
		LOG.info("String.format: {}", String.format("%s", (Object[]) strs));
		LOG.info("MessageFormat.format: {}", MessageFormat.format("{0}", (Object[]) strs));
		LOG.info("StringFormatter.format: {}", StringFormatter.format("{}", (Object[]) strs));
		LOG.info("");

		List<String> list = GapList.create("a", "b");
		LOG.info("Logger: {}", list);
		LOG.info("String.format: {}", String.format("%s", list));
		LOG.info("MessageFormat.format: {}", MessageFormat.format("{0}", list));
		LOG.info("StringFormatter.format: {}", StringFormatter.format("{}", list));
		LOG.info("");

		// Treat the List like Object... to avoid converting
		LOG.info("StringFormatter.formatList: {}", StringFormatter.formatList("{}", list));
	}

	//

	static void testMessageFormatJdk() {
		// https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html

		// Arguments must be numbered or an error is thrown: "java.lang.IllegalArgumentException: can't parse argument number"
		//LOG.info("{}", MessageFormat.format("{}-{}", "Hello", "World"));
		testMessageFormat("{1}-{0}", "World", "Hello");

		// Use "'" for escaping text, "''" to escape the quote itself 
		testMessageFormat("{0}'{''}'{1}", "Hello", "World");

		// Unmatched quotes are accepted
		testMessageFormat("'abc", "");

		// Unmatched braces are not accepted: "IllegalArgumentException: Unmatched braces in the pattern."
		//LOG.info("{}", MessageFormat.format("ab{0", ""));

		//

		LOG.info("{}", StringFormatter.format("{}-{}", "Hello", "World"));
		LOG.info("{}", StringFormatter.format("{1}-{0}", "World", "Hello"));

		LOG.info("{}", StringFormatter.format("{0}{{'}{1}", "Hello", "World"));
	}

	static void testMessageFormat(String format, Object... args) {
		LOG.info("{}", MessageFormat.format(format, args));

		MessageFormat fmt = new MessageFormat(format);
		LOG.info("{}", fmt.format(args));

		StringFormat sfmt = new StringFormat(format, StringFormatParsers.MessageFormatParser);
		LOG.info("{}", sfmt.format(args));
	}

	//

	public static class StringFormatterJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringFormatterJmhTest.class);
			opts.setUseGcProfiler(true);

			JmhTool jr = new JmhTool();
			JmhRunner runner = jr.getRunner();
			//runner.setFastMode(true);
			//runner.verifyJmhMethods(opts, 10);

			jr.runJmh(opts);
			jr.showPivotTable();
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(===" + i + "===)");
		}

		@Benchmark
		public String testStringFormatter(MyState state) {
			return StringFormatter.format("begin-{}-{}-end", state.strings.next(), state.strings.next());
		}

		@Benchmark
		public String testStringFormatter2(MyState state) {
			return format2("begin-{}-{}-end", state.strings.next(), state.strings.next());
		}

		@Benchmark
		public String testStringFormatter3(MyState state) {
			return format3("begin-{}-{}-end", state.strings.next(), state.strings.next());
		}

		public static String format2(String format, Object... objs) {
			StringFormat formatter = new StringFormat2(format);
			return formatter.format(objs);
		}

		static class StringFormat2 extends StringFormat {
			public StringFormat2(String format) {
				super(format);
			}

			@Override
			protected String doFormat(IStringMapper mapper) {
				int bufSize = getInitialBufferSize(constLen, numParams);
				GrowingStringBuilder buf = new GrowingStringBuilder(bufSize);
				int size = providers.size();
				for (int i = 0; i < size; i++) {
					IStringProvider strProv = providers.get(i);
					buf.append(strProv.getString(mapper));
				}
				return buf.toString();
			}
		}

		static final ThreadLocalCharBuffer threadLocalCharBuffer1024 = new ThreadLocalCharBuffer(1024);

		public static String format3(String format, Object... objs) {
			StringFormat formatter = new StringFormat3(format);
			return formatter.format(objs);
		}

		static class StringFormat3 extends StringFormat {
			public StringFormat3(String format) {
				super(format);
			}

			@Override
			protected String doFormat(IStringMapper mapper) {
				char[] data = threadLocalCharBuffer1024.getBuffer(1024);
				GrowingStringBuilder buf = new GrowingStringBuilder(data);
				int size = providers.size();
				for (int i = 0; i < size; i++) {
					IStringProvider strProv = providers.get(i);
					buf.append(strProv.getString(mapper));
				}
				return buf.toString();
			}
		}

	}

	public static class PerformanceJmhTest {

		static AllocationHeapDumpObserver heapObserver = new AllocationHeapDumpObserver().setHprofFile(FilePath.of("output/StringFormatterTest.hprof"))
				.setLive(false);

		static void test() {
			Options opts = new Options().includeClass(PerformanceJmhTest.class);
			opts.setRunTimeMillis(100);
			opts.setWarmupIterations(3).setMeasurementIterations(1);
			opts.setJvmArgs(JavaTool.JvmUseGcEpsilon);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.createJdkTools(JavaVersion.JAVA_11)));
			opts.setUseGcProfiler(true);
			JmhRunner runner = new JmhRunner();
			//runner.setBuildBenchmarks(false);
			runner.runJmh(opts);

			IList<Tuple<Integer, String>> list = heapObserver.getAllocatedInstances();
			list = list.filter(t -> t.getFirst() > 1000);
			LOG.info("{}", list);

		}

		@State(Scope.Benchmark)
		public static class StringFormatterCompiledState {
			StringFormat stringFormat = new StringFormat("begin-{}-{}-end");
		}

		@Benchmark
		//		public void testStringFormatterCompiled(HeapObserverState s, StringFormatterCompiledState state) {
		public String testStringFormatterCompiled(StringFormatterCompiledState state) {
			return state.stringFormat.format("a", "b");
		}

		@Benchmark
		public String testStringFormatter() {
			return StringFormatter.format("begin-{}-{}-end", "a", "b");
		}

		@Benchmark
		public String testStringFormatter2() {
			StringFormat formatter = new StringFormat("begin-{}-{}-end");
			return formatter.format("a", "b");
		}

		@State(Scope.Benchmark)
		public static class MessageFormatCompiledState {
			MessageFormat messageFormat = new MessageFormat("begin-{0}-{1}-end");
		}

		@Benchmark
		public String testMessageFormatCompiled(MessageFormatCompiledState state) {
			return state.messageFormat.format(new String[] { "a", "b" });
		}

		@Benchmark
		public String testMessageFormat() {
			return MessageFormat.format("begin-{0}-{1}-end", "a", "b");
		}

		@Benchmark
		public String testStringFormat() {
			return String.format("begin-%s-%s-end", "a", "b");
		}

	}

	static void testPerformance() {
		int num = 5_000_000;
		for (int n = 0; n < 3; n++) {
			Timer t;

			// StringFormatter
			System.out.println("StringFormatter: direct");
			t = new Timer();
			for (int i = 0; i < num; i++) {
				String s = StringFormatter.format("begin-{}-{}-end", "a", "b");
				if (i == 0) {
					System.out.println(s);
				}
			}
			t.printElapsed();

			System.out.println("StringFormatter: compiled");
			t = new Timer();
			StringFormat sf = new StringFormat("begin-{}-{}-end");
			for (int i = 0; i < num; i++) {
				String s = sf.format("a", "b");
				if (i == 0) {
					System.out.println(s);
				}
			}
			t.printElapsed();

			// String.format
			System.out.println("String.format");
			t = new Timer();
			for (int i = 0; i < num; i++) {
				String s = String.format("begin-%s-%s-end", "a", "b");
				if (i == 0) {
					System.out.println(s);
				}
			}
			t.printElapsed();

			// MessageFormat
			System.out.println("MessageFormat: direct");
			t = new Timer();
			for (int i = 0; i < num; i++) {
				String s = MessageFormat.format("begin-{0}-{1}-end", "a", "b");
				if (i == 0) {
					System.out.println(s);
				}
			}
			t.printElapsed();

			System.out.println("MessageFormat: compiled");
			t = new Timer();
			MessageFormat mf = new MessageFormat("begin-{0}-{1}-end");
			for (int i = 0; i < num; i++) {
				String s = mf.format(new String[] { "a", "b" });
				if (i == 0) {
					System.out.println(s);
				}
			}
			t.printElapsed();
			System.out.println();
		}
	}

	static void testFunc() {
		String str = StringFormatter.format("begin-{}-{}-end", "a", "b");
		System.out.println(str);
		str = StringFormatter.format("begin-{1}-{0}-end", "a", "b");
		System.out.println(str);
		str = StringFormatter.format("begin-{0}-{}-{}-end", "a", "b", "c");
		System.out.println(str);
	}

	@Trace(traceMethod = "/format.*/", parameters = Trace.PARAM0)
	public static void testFormat() {
		StringFormatter.format("{} {0}", "a");
		StringFormatter.format("{0} {0}", "a");
		StringFormatter.format("{0} {0} {}", "a", "b");
		StringFormatter.format("{1} {0} {}", "a", "b");

		StringFormatter.format("({})({})", "a", "b");
		StringFormatter.format("({})({})", 0, 1);

		IStringMapper mapper = new IStringMapper() {
			@Override
			public String getString(Object key) {
				if (key.equals("a")) {
					return "(A)";
				} else if (key.equals("b")) {
					return "(B)";
				} else {
					throw new IllegalArgumentException("Invalid key " + key);
				}
			}
		};
		StringFormatter.formatMapper("no_param", mapper);
		StringFormatter.formatMapper("invalid_param{", mapper);
		StringFormatter.formatMapper("param_a[{a}]", mapper);

		List<String> strings = Arrays.asList(new String[] { "(A)", "(B)", "(C)" });
		StringFormatter.formatList("begin-[{a}]-end", strings);
		StringFormatter.formatList("begin-[{-1}]-end", strings);
		StringFormatter.formatList("begin-[{0}]-end", strings);
		StringFormatter.formatList("begin-[{1}]-end", strings);
		StringFormatter.formatList("begin-[{3}]-end", strings);
		StringFormatter.formatList("begin-{0}-{1}-end", strings);
		StringFormatter.formatList("begin-{1}-{0}-end", strings);
		StringFormatter.formatList("begin-{}-{}-end", strings);
		StringFormatter.formatList("begin-{1}-{}-end", strings);
		StringFormatter.formatList("begin-{1}-{}-{}-end", strings);

		StringFormatter.formatMapper("abc", mapper);
		StringFormatter.formatMapper("a{b}c", mapper);
		StringFormatter.formatMapper("a{{c", mapper);
		StringFormatter.formatMapper("a{{b}c", mapper);
		StringFormatter.formatMapper("a{b}}c}d", mapper);
		StringFormatter.formatMapper("a{b{c}d", mapper);

		Map<String, String> map = CollectionTools.createHashMap("b", "B");
		StringFormatter.formatMap("a{b}c", map);
		StringFormatter.formatMap("a{b}c", "b", "B");
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringFormat", traceMethod = "/format.*/", parameters = Trace.THIS | Trace.PARAM0)
	public static void testFormat1() {
		StringFormatParser parser1 = new NamedFormatParser('{', '}');
		StringFormatParser parser2 = new NamedFormatParser('[', ']');
		StringFormatParser parser3 = new SingleCharFormatParser('?');

		{
			// Format using a List
			List<String> strings = Arrays.asList(new String[] { "(A)", "(B)" });

			StringFormat sf = new StringFormat("left-{1}-mid-{0}-right", parser1);
			sf.formatList(strings);

			sf = new StringFormat("left-[1]-mid-[0]-right", parser2);
			sf.formatList(strings);

			sf = new StringFormat("left-?-mid-?-right", parser3);
			sf.formatList(strings);
		}
		{
			// Format using a Map
			Map<String, String> map = CollectionTools.createHashMap("a", "a1");

			String f = "before-{a}-after";
			StringFormat sf = new StringFormat(f, parser1);
			String s = sf.formatMap(map);
			String s2 = StringFormatter.formatMap(f, map);
			CheckTools.check(s2.equals(s));
		}
	}

	@Formatter
	public static String format(StringFormat sf) {
		return sf.getFormat();
	}
}
