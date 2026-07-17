package org.magicwerk.strings;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Format;
import org.magictest.client.Format.OutputType;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.exceptions.ExceptionTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestBenchmark;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestCompare;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.StringJoiner;
import org.magicwerk.strings.StringRepeater;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.google.common.base.Joiner;

/**
 * Test of class {@link StringJoiner}.
 */
public class StringJoinerTest extends StringBenchmarkTestBase {

	public static void main(String[] args) {
		new StringJoinerTest().run();
	}

	void run() {
		//testJoin();

		testBenchmarks();
		//runBenchmarks();

		//new StringJoinerTestJmh().test();
		//JoinerImplTestJmh.test();
	}

	@Trace(traceMethod = "/join.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.RESULT, formats = {
			@Format(apply = Trace.RESULT, outputType = OutputType.PRE) })
	public void testJoin() {
		StringJoiner joiner = StringJoiner.builder().setBeginMarker("(").setEndMarker(")").setJoin('-').build();
		joiner.joinStrings(GapList.create());
		joiner.joinStrings(GapList.create("a"));
		joiner.joinObjects(GapList.create("a", "b"));
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(
				StringJoinerFailOnNullBenchmarkTest.class,
				StringJoinerIgnoreNullBenchmarkTest.class,
				StringJoinerNullEmptyBenchmarkTest.class,
				StringJoinerReplaceNullBenchmarkTest.class);
	}

	/**
	 * Test/benchmark of {@link StringJoiner}.
	 */
	public static class StringJoinerBenchmarkTestBase {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@TestData
		IList<IList<String>> inputs = (IList) GapList.create(
				(Object) GapList.create("0123456789", "0123456789", "0123456789"),
				(Object) GapList.create("0123456789", null, "0123456789"));

		char separator;
		StringJoiner stringJoiner;
		Joiner joiner;

		@TestCompare
		Object compareResult(Object result) {
			if (result instanceof Throwable) {
				Throwable error = ((Throwable) result);
				return ExceptionTools.getMessage(error);
			} else {
				return result;
			}
		}

		@TestMethod
		public String testStringJoiner(List<String> strs) {
			return stringJoiner.joinStrings(strs);
		}

		@TestMethod
		public String testGuavaStrings(List<String> strs) {
			return joiner.join(strs);
		}

		@TestMethod
		public String testCommonsStringUtils(List<String> strs) {
			return StringUtils.join(strs, separator);
		}
	}

	/** Fail due to null value */
	@TestBenchmark(testOnly = true)
	public static class StringJoinerFailOnNullBenchmarkTest extends StringJoinerBenchmarkTestBase {
		{
			separator = '-';
			stringJoiner = StringJoiner.builder().setJoin(separator).build();
			joiner = Joiner.on(separator);
		}

		@Override
		@TestMethod
		public String testStringJoiner(List<String> strs) {
			if (strs.contains(null)) {
				throw new NullPointerException();
			} else {
				return super.testStringJoiner(strs);
			}
		}

		@Override // Ignored as @Override without @TestMethod
		public String testCommonsStringUtils(List<String> strs) {
			throw CheckTools.error();
		}
	}

	/** Ignore null value */
	public static class StringJoinerIgnoreNullBenchmarkTest extends StringJoinerBenchmarkTestBase {
		{
			separator = '-';
			stringJoiner = StringJoiner.builder().setJoin(separator).setIgnoreNull(true).build();
			joiner = Joiner.on(separator).skipNulls();
		}

		@Override // Ignored as @Override without @TestMethod
		public String testCommonsStringUtils(List<String> strs) {
			throw CheckTools.error();
		}
	}

	/** Replace null value with empty string */
	public static class StringJoinerNullEmptyBenchmarkTest extends StringJoinerBenchmarkTestBase {
		{
			separator = '-';
			stringJoiner = StringJoiner.builder().setJoin(separator).setNullString("").build();
			joiner = Joiner.on(separator).useForNull("");
		}
	}

	/** Replace null value with specified string */
	public static class StringJoinerReplaceNullBenchmarkTest extends StringJoinerBenchmarkTestBase {
		{
			separator = '-';
			stringJoiner = StringJoiner.builder().setJoin(separator).setNullString("abc").build();
			joiner = Joiner.on(separator).useForNull("abc");
		}

		@Override // ignore
		public String testCommonsStringUtils(List<String> strs) {
			throw CheckTools.error();
		}
	}

	//

	/** Show that StringJoiner is as fast as StringUtils */
	public static class StringJoinerTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			final String JOIN = "";
			//final String JOIN = "-";
			final StringJoiner stringJoiner1 = StringJoiner.builder().build();
			final StringJoiner stringJoiner2 = StringJoiner.builder().setJoin(JOIN).build();
			final StringJoiner stringJoiner3 = StringJoiner.builder().setJoin(JOIN).setBeginMarker("").build();
			final Joiner guavaJoiner = Joiner.on(JOIN);

			//final String str = "ab";
			final String str = StringRepeater.build("-").repeat(10);
			CyclicSource<List<String>> source = new CyclicSource<>(GapList.create(str, str), GapList.create(str, str));
		}

		@Benchmark
		public Object testStringJoiner1Strings(MyState state) {
			return state.stringJoiner1.joinStrings(state.source.next());
		}

		@Benchmark
		public Object testStringJoiner1Objects(MyState state) {
			return state.stringJoiner1.joinObjects(state.source.next());
		}

		@Benchmark
		public Object testStringJoiner2Strings(MyState state) {
			return state.stringJoiner2.joinStrings(state.source.next());
		}

		@Benchmark
		public Object testStringJoiner2Objects(MyState state) {
			return state.stringJoiner2.joinObjects(state.source.next());
		}

		@Benchmark
		public Object testStringJoiner3Strings(MyState state) {
			return state.stringJoiner3.joinStrings(state.source.next());
		}

		@Benchmark
		public Object testStringJoiner3Objects(MyState state) {
			return state.stringJoiner3.joinObjects(state.source.next());
		}

		@Benchmark
		public String testStringUtilsJoin(MyState state) {
			return StringUtils.join(state.source.next(), state.JOIN);
		}

		@Benchmark
		public String testGuavaJoiner(MyState state) {
			return state.guavaJoiner.join(state.source.next());
		}
	}

	//

	public static class JoinerImplTestJmh {

		public static void test() {
			// Check memory usage:
			// - add "JmhAllocationObserverState s" as parameter to benchmark
			// - reduce runtime to avoid GC: opts.setRunTimeMillis(50)
			// - comment out: // runner.verifyJmhMethods(opts, 10);

			Options opts = new Options().includeClass(JoinerImplTestJmh.class);
			opts.setUseGcProfiler(true);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.createJdkTools(JavaVersion.JAVA_8)));
			JmhRunner runner = new JmhRunner();
			//runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String JOIN = "-";
			final Joiner1 joiner1 = new Joiner1().setJoin(JOIN);
			final Joiner2 joiner2 = new Joiner2().setJoin(JOIN);
			final Joiner3 joiner3 = Joiner3.Builder.of().setJoin(JOIN).build();
			final Joiner4 joiner4 = Joiner4.Builder.of().setJoin(JOIN).build();

			final Joiner1 joiner1b = new Joiner1();
			final Joiner2 joiner2b = new Joiner2();
			final Joiner3 joiner3b = Joiner3.Builder.of().build();
			final Joiner4 joiner4b = Joiner4.Builder.of().build();

			CyclicSource<IList<String>> source = new CyclicSource<>(GapList.create("a", "b"), GapList.create("c", "d"));
		}

		@Benchmark
		public String testJoiner1R(MyState state) {
			return new Joiner1().setJoin(state.JOIN).join(state.source.next());
		}

		@Benchmark
		public String testJoiner2R(MyState state) {
			return new Joiner2().setJoin(state.JOIN).join(state.source.next());
		}

		@Benchmark
		public String testJoiner3R(MyState state) {
			return Joiner3.Builder.of().setJoin(state.JOIN).build().join(state.source.next());
		}

		@Benchmark
		public String testJoiner4R(MyState state) {
			return Joiner3.Builder.of().setJoin(state.JOIN).build().join(state.source.next());
		}

		@Benchmark
		public Object testStaticJoin(MyState state) {
			return staticJoin(state.source.next(), state.JOIN);
		}

		@Benchmark
		public String testJoiner1(MyState state) {
			return state.joiner1.join(state.source.next());
		}

		@Benchmark
		public String testJoiner2(MyState state) {
			return state.joiner2.join(state.source.next());
		}

		@Benchmark
		public String testJoiner3(MyState state) {
			return state.joiner3.join(state.source.next());
		}

		@Benchmark
		public String testJoiner4(MyState state) {
			return state.joiner4.join(state.source.next());
		}

		//

		@Benchmark
		public Object testStaticJoinB(MyState state) {
			return staticJoin(state.source.next());
		}

		@Benchmark
		public String testJoiner1B(MyState state) {
			return state.joiner1b.join(state.source.next());
		}

		@Benchmark
		public String testJoiner2B(MyState state) {
			return state.joiner2b.join(state.source.next());
		}

		@Benchmark
		public String testJoiner3B(MyState state) {
			return state.joiner3b.join(state.source.next());
		}

		@Benchmark
		public String testJoiner4B(MyState state) {
			return state.joiner4b.join(state.source.next());
		}

	}

	static String staticJoin(IList<String> strs, String join) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.size(); i++) {
			if (i > 0) {
				buf.append(join);
			}
			buf.append(strs.get(i));
		}
		return buf.toString();
	}

	static String staticJoin(IList<String> strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.size(); i++) {
			buf.append(strs.get(i));
		}
		return buf.toString();
	}

	static class Joiner1 {
		String join = "";

		Joiner1 setJoin(String join) {
			this.join = join;
			return this;
		}

		String join(IList<String> strs) {
			return staticJoin(strs, join);
		}
	}

	static class Joiner2 {
		String join;

		Joiner2() {
		}

		private Joiner2(String join) {
			this.join = join;
		}

		Joiner2 setJoin(String join) {
			return new Joiner2(join);
		}

		String join(IList<String> strs) {
			return staticJoin(strs, join);
		}
	}

	static abstract class Joiner3 {

		static class Builder {

			String join;

			static Builder of() {
				return new Builder();
			}

			Builder setJoin(String join) {
				this.join = join;
				return this;
			}

			Joiner3 build() {
				if (join == null) {
					return new Joiner3a();
				} else {
					return new Joiner3b(join);
				}
			}
		}

		abstract String join(IList<String> strs);

		static class Joiner3a extends Joiner3 {
			@Override
			String join(IList<String> strs) {
				return staticJoin(strs);
			}
		}

		static class Joiner3b extends Joiner3 {
			String join;

			Joiner3b(String join) {
				this.join = join;
			}

			@Override
			String join(IList<String> strs) {
				return staticJoin(strs, join);
			}
		}
	}

	static abstract class Joiner4 {

		static class Builder {

			String join;

			static Builder of() {
				return new Builder();
			}

			Builder setJoin(String join) {
				this.join = join;
				return this;
			}

			Joiner4 build() {
				if (join == null) {
					return new Joiner4a(this);
				} else {
					return new Joiner4b(this);
				}
			}
		}

		Builder builder;

		Joiner4(Builder builder) {
			this.builder = builder;
		}

		abstract String join(IList<String> strs);

		static class Joiner4a extends Joiner4 {
			Joiner4a(Builder builder) {
				super(builder);
			}

			@Override
			String join(IList<String> strs) {
				return staticJoin(strs);
			}
		}

		static class Joiner4b extends Joiner4 {
			Joiner4b(Builder builder) {
				super(builder);
			}

			@Override
			String join(IList<String> strs) {
				return staticJoin(strs, builder.join);
			}
		}
	}

}
