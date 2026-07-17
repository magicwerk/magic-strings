package org.magicwerk.strings;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.concurrent.ExecutorServiceTools;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.helper.CapacityHelper;
import org.magicwerk.strings.StringRepeater;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.slf4j.Logger;

/**
 * Test class of {@link StringRepeater}.
 */
public class StringRepeaterTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringRepeaterTest().run();
	}

	void run() {
		//testConcurrency();
		//testRepeat();

		//testBenchmarks();
		runBenchmarks();

		//StringRepeaterJmhTest.test();
		//StringRepeaterParallelJmhTest.test();
	}

	void testConcurrency() {
		int size = 10;
		StringRepeater repeater = StringRepeater.build("-");
		//		ExecutorServiceTools.runParallelThreads(10, () -> {
		ExecutorServiceTools.executeRunnables(10, () -> {
			for (int i = 0; i < size; i++) {
				String str = repeater.repeat(i);
				CheckTools.check(str.length() == i);
			}
		});
	}

	@Trace
	public void testRepeat() {
		StringRepeater sr = StringRepeater.build("<->");
		sr.repeat(0);
		sr.repeat(1);
		sr.repeat(2);
		sr.repeat(4);
		sr.repeat(-1);
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringRepeatCharBenchmarkTest.class, StringRepeatStringBenchmarkTest.class, StringRepeatStringSeparatorBenchmarkTest.class);
	}

	public static class StringRepeatBenchmarkTestBase {
	}

	public static class StringRepeatCharBenchmarkTest extends StringRepeatBenchmarkTestBase {

		char repeat = 'x';
		StringRepeater stringRepeater = StringRepeater.build(repeat);

		@TestData
		IList<Integer> inputs = GapList.create(0, 1, 2, 10);

		@TestMethod
		public String testStringRepeater(int num) {
			return stringRepeater.repeat(num);
		}

		@TestMethod
		public String testCommonsStringUtils(int num) {
			return StringUtils.repeat(repeat, num);
		}
	}

	public static class StringRepeatStringBenchmarkTest extends StringRepeatBenchmarkTestBase {

		String repeat = "ab";
		StringRepeater stringRepeater = StringRepeater.build(repeat);

		@TestData
		IList<Integer> inputs = GapList.create(0, 1, 2, 10);

		@TestMethod
		public String testStringRepeater(int num) {
			return stringRepeater.repeat(num);
		}

		@TestMethod
		public String testCommonsStringUtils(int num) {
			return StringUtils.repeat(repeat, num);
		}
	}

	public static class StringRepeatStringSeparatorBenchmarkTest extends StringRepeatBenchmarkTestBase {

		String repeat = "ab";
		String separator = ",";
		StringRepeater stringRepeater = StringRepeater.builder().setRepeat(repeat).setSeparator(separator).build();

		@TestData
		IList<Integer> inputs = GapList.create(0, 1, 2, 10);

		@TestMethod
		public String testStringRepeater(int num) {
			return stringRepeater.repeat(num);
		}

		@TestMethod
		public String testCommonsStringUtils(int num) {
			return StringUtils.repeat(repeat, separator, num);
		}
	}

	//

	public static class StringRepeaterJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringRepeaterJmhTest.class);
			//opts.setJavaVersions(GapList.create(TestTools.JdkCommands21));
			//opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			String str = "-";
			StringRepeater stringRepeater = StringRepeater.build(str);
		}

		@Benchmark
		public int testStringRepeat(MyState state) {
			int n = 0;
			for (int i = 0; i < 100; i++) {
				String s = state.str.repeat(i);
				n += s.length();
			}
			return n;
		}

		@Benchmark
		public int testStringRepeater(MyState state) {
			int n = 0;
			for (int i = 0; i < 100; i++) {
				String s = state.stringRepeater.repeat(i);
				n += s.length();
			}
			return n;
		}
	}

	@Threads(10)
	public static class StringRepeaterParallelJmhTest {

		// StringRepeater is 10% faster than StringRepeaterVolatile
		// StringRepeaterTest.StringRepeaterParallelJmhTest.testStringRepeat            thrpt    5  34882121.399   ops/s
		// StringRepeaterTest.StringRepeaterParallelJmhTest.testStringRepeaterVolatile  thrpt    5  31002446.654   ops/s

		static void test() {
			Options opts = new Options().includeClass(StringRepeaterParallelJmhTest.class);
			//opts.setJavaVersions(GapList.create(TestTools.JdkCommands21));
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		static final int NUM = 100;

		@State(Scope.Benchmark)
		public static class MyState {
			char c = '-';
			StringRepeater stringRepeater = StringRepeater.builder().setRepeat(c).setPreSize(0).setPreInit(false).build();
			StringRepeaterVolatile stringRepeaterVolatile = new StringRepeaterVolatile(c, 0, false);
			StringRepeaterUncached stringRepeaterUncached = new StringRepeaterUncached(c);
		}

		@Benchmark
		public int testStringRepeat(MyState state) {
			int n = 0;
			for (int i = 0; i < NUM; i++) {
				String s = state.stringRepeater.repeat(i);
				n += s.length();
			}
			return n;
		}

		@Benchmark
		public int testStringRepeaterVolatile(MyState state) {
			int n = 0;
			for (int i = 0; i < NUM; i++) {
				String s = state.stringRepeaterVolatile.repeat(i);
				n += s.length();
			}
			return n;
		}

		@Benchmark
		public int testStringRepeaterUncached(MyState state) {
			int n = 0;
			for (int i = 0; i < NUM; i++) {
				String s = state.stringRepeaterUncached.repeat(i);
				n += s.length();
			}
			return n;
		}

	}

	static class StringRepeaterVolatile {

		final static int DEFAULT_CAPACITY = 16;

		final String repeat;
		volatile String[] strs;

		//

		public StringRepeaterVolatile(char repeat, int size, boolean init) {
			this.repeat = String.valueOf(repeat);
			this.strs = initRepeat(size, init);
		}
		//

		String[] initRepeat(int size, boolean init) {
			size++;
			String[] strs = newArray(size);
			if (init) {
				for (int i = 0; i < size; i++) {
					strs[i] = doRepeat(i);
				}
			}
			return strs;
		}

		String[] newArray(int size) {
			return new String[size];
		}

		String doRepeat(int count) {
			return repeat.repeat(count);
		}

		public String repeat(int count) {
			String[] newStrs = ensureCapacity(count + 1);
			String str = newStrs[count];
			if (str == null) {
				str = doRepeat(count);
				newStrs[count] = str;
			}
			return str;
		}

		String[] ensureCapacity(int capacity) {
			String[] strs = this.strs;
			int size = strs.length;

			// If capacity of array is sufficient, just return it
			if (capacity <= size) {
				return strs;
			}

			// Otherwise extend capacity
			capacity = CapacityHelper.calculateCapacity(capacity, size, DEFAULT_CAPACITY);
			String[] newStrs = Arrays.copyOf(strs, capacity);

			// MT: set new strs array, so it becomes visible to other threads at some point in time
			this.strs = newStrs;
			return newStrs;
		}
	}

	static class StringRepeaterUncached {

		final String repeat;

		public StringRepeaterUncached(char repeat) {
			this.repeat = String.valueOf(repeat);
		}

		public String repeat(int count) {
			return repeat.repeat(count);
		}

	}
}
