package org.magicwerk.strings.helper;

import java.util.Objects;
import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor.ExceptionLogMode;
import org.magicwerk.brownies.core.function.StringPredicates;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.files.FileTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationFreeRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.BenchmarkFileResult;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.BenchmarkJsonParser;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.BenchmarkResult;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools.MemoryGcInfo;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaEnvRunner;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.brownies.tools.runner.TestRunner;
import org.magicwerk.brownies.tools.runner.TestRunner.TestRun;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.function.Predicates;
import org.magicwerk.strings.function.MultiPredicate.Mode;
import org.magicwerk.strings.function.Predicates.NamedPredicate;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.PrintTools;
import org.magicwerk.strings.helper.CheckTools.Check;
import org.magicwerk.strings.helper.CheckTools.CheckNull;
import org.magicwerk.strings.helper.CheckTools.TypeValidator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link CheckTools}.
 */
public class CheckToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CheckToolsTest().run();
	}

	void run() {
		//testCheckJmh();

		//testCheck();
		//testCheckForBoolean();
		testCheckTypeOf();
		//testExamples();
		//testTypeValidator();
		//testCheckRunner();
		//testCheckVarargTuples();		
	}

	@Trace
	public void testCheckVarargTuples() {
		CheckTools.checkVarargTuples("a", 1);
		CheckTools.checkVarargTuples("a", 1, "b", 2);

		// error
		CheckTools.checkVarargTuples("a", 1, "b");
		CheckTools.checkVarargTuples("a", 1, "b", "c");
	}

	@Capture
	public void testEqual() {
		String s0 = "a";
		String s1 = "b";

		Check.forEqual().check(s0, s0);
		assert (s0.equals(s0));
		assert (Objects.equals(s0, s0));

		fails(() -> Check.forEqual().check(s0, s1));
	}

	@Capture
	public void testExamples() {
		Object nil = null;
		Object one = "one";

		// Check that exactly one argument is not null
		Check.forNotNull(Mode.ONE).check(nil, one);
		Check.forNotNull(Mode.ONE).check(nil, one, nil);
		fails(() -> Check.forNotNull(Mode.ONE).check(one, one));

		// Check that all arguments are null
		Check.forNull(Mode.ALL).check(nil, nil);
		fails(() -> Check.forNull(Mode.ALL).check(one, one));

		// Check that one or no argument is true
		Check.forTrue(Mode.ONE_OR_NONE).check(true, false);
		Check.forTrue(Mode.ONE_OR_NONE).check(true, false, false);
		fails(() -> Check.forTrue(Mode.ONE_OR_NONE).check(true, true));

		// Check that at least one argument is false
		Check.forFalse(Mode.ANY).check(true, false);
		Check.forFalse(Mode.ANY).check(true, false, false);
		fails(() -> Check.forFalse(Mode.ANY).check(true, true));

		// Check predicate for single argument
		Check.of(Predicates.isOneOf("a", "b", "c")).check("a");
		fails(() -> Check.of(Predicates.isOneOf("a", "b", "c")).check("d"));

		// Check predicate with mode for multiple arguments
		Check.of(Mode.ALL, (String s) -> s.length() > 1).check("ab", "cd");
		fails(() -> Check.of(Mode.ALL, (String s) -> s.length() > 1).check("a"));
	}

	void fails(Runnable runnable) {
		new RunnableExecutor().setMustFail().run(runnable);
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.CheckTools$CheckNull", traceMethod = "check")
	public void testCheckForNull() {
		Object nil = null;
		Object one = "one";

		Check.forNotNull(Mode.ONE).check(nil, one);
		Check.forNotNull(Mode.ONE).check(one, nil);

		// Same call as above
		CheckNull.checkForNotNull(Mode.ONE).check(one, nil);

		Check.forNotNull(Mode.ONE).check();
		Check.forNotNull(Mode.ONE).check(nil, nil);
		Check.forNotNull(Mode.ONE).check(one, one);
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.CheckTools$CheckNull", traceMethod = "check")
	public void testCheckForBoolean() {

		class Args {
			int i = -1;
			double d = Double.NaN;
			String s = null;

			Args(int i) {
				this.i = i;
			}

			Args(double d) {
				this.d = d;
			}

			Args(String s) {
				this.s = s;
			}
		}

		Args args = new Args(1);
		Check.forTrue(Mode.ONE).check(args.i != -1, !Double.isNaN(args.d), args.s != null);

		args = new Args(1.2);
		Check.forTrue(Mode.ONE).check(args.i != -1, !Double.isNaN(args.d), args.s != null);

		args = new Args("abc");
		Check.forTrue(Mode.ONE).check(args.i != -1, !Double.isNaN(args.d), args.s != null);

		args = new Args(-1);
		Check.forTrue(Mode.ONE).check(args.i != -1, !Double.isNaN(args.d), args.s != null);

	}

	@Capture
	public void testCheckTypeOf() {
		RunnableExecutor re = new RunnableExecutor().setIgnoreException().setLogException(ExceptionLogMode.INFO);
		TypeValidator tv = new TypeValidator(Integer.class, String.class);
		tv.validate(1);
		re.run(() -> tv.validate(0L));

		Check.of(tv).check(1);
		re.run(() -> Check.of(tv).check(0L));

		re.run(() -> CheckTools.checkTypeOf(0L, Integer.class, String.class));
	}

	static final Predicate<String> longTest = NamedPredicate.of("longString", s -> s.length() >= 3);

	@Trace(traceClass = "CheckTools$CheckSingle", traceMethod = "/.*/")
	public void testCheckSingle() {
		Check.of(longTest).check("ab");
		Check.of(longTest).check("abc");

		String str = "abc";
		Check.of((String s) -> s.length() >= 3).check(str);

		// Compile error: method length() undefined for Object
		//Check.of(s -> s.length() >= 3).check("abc");

		// Traditional alternative
		CheckTools.check(str.length() >= 3, "string too short");
	}

	@Trace(traceClass = "CheckTools$CheckMulti", traceMethod = "/.*/")
	public void testCheckMulti() {
		Check.of(Mode.ANY, longTest).check("ab");
		Check.of(Mode.ANY, longTest).check("ab", "abc");
		Check.of(Mode.ANY, longTest).check("abc");
		Check.of(Mode.ANY, longTest).check("abc", "abcd");
	}

	@Test(groups = { "slow" })
	@Capture(source = Source.NONE)
	public void testCheckAllocationFree() {
		// Varargs are allocation free with Java 17
		Options opts = new Options().includeClass(CheckAllocationFree.class)
				.setJdkCommands(GapList.create(BrowniesJavaEnv.createJdkTools(JavaVersion.JAVA_17)));
		new JmhAllocationFreeRunner().checkAllocationFree(opts);
	}

	public static class CheckAllocationFree {
		@Benchmark
		public void test() {
			Check.of(longTest).check("abc");
			Check.of(Mode.ANY, longTest).check("abc", "abcd");
		}
	}

	@Capture
	public void testCheck() {
		Object nil = null;
		Object one = "one";
		Object two = "two";

		// ALL
		LOG.info("ALL");
		// - ok
		doTestCheck(Mode.ALL);
		doTestCheck(Mode.ALL, one);
		doTestCheck(Mode.ALL, one, two);
		// - error
		doTestCheck(Mode.ALL, nil);
		LOG.info("");

		// NONE
		LOG.info("NONE");
		// - ok
		doTestCheck(Mode.NONE);
		doTestCheck(Mode.NONE, nil);
		// - error
		doTestCheck(Mode.NONE, one);
		LOG.info("");

		// ANY
		LOG.info("ANY");
		// - ok
		doTestCheck(Mode.ANY, one);
		doTestCheck(Mode.ANY, one, nil);
		doTestCheck(Mode.ANY, one, two);
		// - error
		doTestCheck(Mode.ANY);
		doTestCheck(Mode.ANY, nil);
		LOG.info("");

		// ONE
		// - ok
		LOG.info("ONE");
		doTestCheck(Mode.ONE, one);
		doTestCheck(Mode.ONE, one, nil);
		// - error
		doTestCheck(Mode.ONE);
		doTestCheck(Mode.ONE, nil);
		doTestCheck(Mode.ONE, one, two);
		LOG.info("");

		// ONE_OR_NONE
		// - ok
		LOG.info("ONE_OR_NONE");
		doTestCheck(Mode.ONE_OR_NONE);
		doTestCheck(Mode.ONE_OR_NONE, one);
		doTestCheck(Mode.ONE_OR_NONE, nil);
		doTestCheck(Mode.ONE_OR_NONE, one, nil);
		// - error
		doTestCheck(Mode.ONE_OR_NONE, one, two);
		LOG.info("");

		// Check error messages
		LOG.info("Error messages");
		doTestCheck(() -> Check.forNotNull(Mode.NONE).check(one));
		doTestCheck(() -> Check.forNotNull(Mode.NONE).withMessage("fails").check(one));
		doTestCheck(() -> Check.forNotNull(Mode.NONE).withMessage("error: {}", "details").check(one));
	}

	void doTestCheck(Mode mode, Object... args) {
		String result = "ok";
		try {
			Check.forNotNull(mode).check(args);
		} catch (Exception e) {
			result = "ERROR: " + e.getMessage();
		}
		LOG.info("{} {} -> {}", mode, PrintTools.toString(args), result);
	}

	void doTestCheck(Runnable runnable) {
		new RunnableExecutor().setLogException(ExceptionLogMode.INFO).setIgnoreException().run(runnable);
	}

	static void testCheckRunner() {
		JavaTool jt = BrowniesJavaEnv.createJavaTool(JavaVersion.JAVA_11);
		jt.setPrintOutput(true);

		JavaRunner jr = new JavaRunner();
		jr.setMainMethod(CheckToolsTest.class, "doTestCheckRunner");
		jr.setJavaTool(jt);

		JavaEnvRunner runner = new JavaEnvRunner();
		runner.setJavaRunner(jr);
		runner.addRunOptions().setJvmArgs(CollectionTools2.concat(JavaTool.JvmUseGcEpsilon));
		runner.addRunOptions().setJvmArgs(CollectionTools2.concat(JavaTool.JvmUseGcEpsilon, JavaTool.JvmNoEscapeAnalysis));
		runner.setPrintOutput(true);
		runner.run();
	}

	/**
	 * Run method and print execution throughput and consumed memory.
	 * This method is executed in a forked JVM.
	 */
	static void doTestCheckRunner() {
		MemoryGcInfo mgi0 = MemoryTools.getMemoryGcInfo();

		TestRunner runner = new TestRunner("RUN");
		runner.add(createRun());
		runner.run();
		runner.printResults();

		// This test method is run without GC so we can easily determine the amount of memory consumed
		MemoryGcInfo mgi1 = MemoryTools.getMemoryGcInfo();
		CheckTools.check(mgi1.gcCount == 0);
		LOG.info("Consumed Memory: {}", mgi1.usedMemory - mgi0.usedMemory);
	}

	static TestRun createRun() {
		return new TestRun() {
			@Override
			public Object run() {
				CheckNull.checkForNotNull(Mode.ONE).check("one");
				return null;
			}
		};
	}

	public static class CheckJmhTest {

		static void test() {
			Options opts = new Options().includeClass(CheckJmhTest.class).setUseGcProfiler(true).setWarmupIterations(3).setMeasurementIterations(1);
			//opts.setJavaVersion(JavaVersion.JAVA_8);
			//opts.setJavaVersion(JavaVersion.JAVA_11);
			//opts.setJavaVersion(JavaVersion.JAVA_17);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.createJdkTools(JavaVersion.JAVA_21)));

			FilePath file = FilePath.of("output/CheckToolsTest.json");
			opts.setResultFile(file);

			JmhRunner runner = new JmhRunner();
			runner.runJmh(opts);

			String text = FileTools.readFile().setFile(file).readText();
			BenchmarkFileResult brf = new BenchmarkJsonParser().parse(text);
			IList<BenchmarkResult> brs = brf.getResults();
			for (BenchmarkResult br : brs) {
				if (br.getGcAllocRateNorm() < 1) {
					LOG.info("{}: {} {}", br.getBenchmark(), br.getGcCount(), br.getGcAllocRateNorm());
				}
			}
		}

		@State(Scope.Thread)
		public static class CheckState {
			String empty = "";
			String s1 = "a";
			String s2 = "b";
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck1(CheckState state) {
			Check.of(CheckJmhTest::isEmpty).check(state.empty);
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck2(CheckState state) {
			Check.of(CheckJmhTest::isEmpty).withMessage("fails").check(state.empty);
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck3(CheckState state) {
			Check.of(CheckJmhTest::isEmpty).withMessage("error: {}", "details").check(state.empty);
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck4(CheckState state) {
			Check.of(Predicates.not(CheckJmhTest::isEmpty)).check(state.s1);
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck5(CheckState state) {
			Check.of(CheckJmhTest::isNonEmpty).check(state.s1);
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck6(CheckState state) {
			Check.of(StringPredicates.isEmpty()).check(state.empty);
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck7(CheckState state) {
			Check.of(StringPredicates.isEmpty().negate()).check(state.s1);
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheckMulti(CheckState state) {
			Check.of(Mode.ALL, Predicates.not(StringTools::isEmpty)).withMessage("error: {}", "details").check(state.s1, state.s2);
		}

		// allocation-free for >= Java 21
		@Benchmark
		public void testCheckMulti2(CheckState state) {
			Check.of(Mode.ALL, Predicates.not(StringTools::isEmpty)).withMessage("error: {}", "details").check(state.s1, state.s2);
		}

		@Benchmark
		public void testCheckForNull(CheckState state) {
			Check.forNull(Mode.NONE).withMessage("error: {}", "details").check(state.s1, state.s2);
		}

		@Benchmark
		public void testCheckTypeOf(CheckState state) {
			CheckTools.checkTypeOf(state.s1, String.class);
		}

		static boolean isEmpty(String s) {
			return s == null || s.isEmpty();
		}

		static boolean isNonEmpty(String s) {
			return s != null && !s.isEmpty();
		}
	}

}
