package org.magicwerk.strings;

import java.util.Comparator;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.Consumers;
import org.magicwerk.brownies.core.LogTools;
import org.magicwerk.brownies.core.concurrent.IterateExecutor;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.javassist.analyzer.ClassDef;
import org.magicwerk.brownies.javassist.analyzer.JavaAnalyzer;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.collections.IList;
import org.slf4j.Logger;

/**
 * Class {@link BenchmarkTestBase} is the base class for string benchmark tests.
 */
public class BenchmarkTestBase {

	static final Logger LOG = LogTools.getLogger();

	Consumer<JmhBenchmarkCreator> configureJmhBenchmarkCreator = Consumers.nop();
	IList<ClassDef> testClasses;

	/** Analyze test classes (all classes ending with "_Test" are added to field testClasses) */
	protected void analyzeTestClasses(Class<?> cl) {
		JavaAnalyzer ja = new JavaAnalyzer().setUseCurrentClassPath();
		ClassDef cd2 = ja.analyzeClass(cl);
		IList<ClassDef> cds = cd2.getEnclosedClasses();
		testClasses = cds.filter(c -> c.getSimpleName().endsWith("_Test"));
		testClasses.sort(Comparator.comparing(ClassDef::getName));
	}

	protected void configureJmhBenchmarkCreator(Consumer<JmhBenchmarkCreator> configureJmhBenchmarkCreator) {
		this.configureJmhBenchmarkCreator = configureJmhBenchmarkCreator;
	}

	protected void runTests() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(false);
		});

		analyzeTestClasses(getClass());
		IterateExecutor.forEach(testClasses, cd -> {
			Class<?> c = ReflectTools.getClass(cd.getName());
			test(c);
		});
	}

	protected TestExecution test(Class<?> c) {
		JmhBenchmarkCreator jbc = new JmhBenchmarkCreator();
		jbc.setTestClass(c);
		configureJmhBenchmarkCreator.accept(jbc);
		TestExecution te = jbc.run();
		return te;
	}

}
