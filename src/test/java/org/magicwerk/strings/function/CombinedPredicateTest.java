package org.magicwerk.strings.function;

import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.function.CombinedPredicate;
import org.magicwerk.brownies.core.function.StringPredicates;
import org.magicwerk.brownies.core.function.CombinedPredicate.Result;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.function.Predicates;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link CombinedPredicate}.
 */
public class CombinedPredicateTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CombinedPredicateTest().run();
	}

	void run() {
		test();
	}

	@Capture
	public void test() {
		// System test class:
		// - class named *ST, in src/test/java, inherits from AbstractSystemTest, has @Test methods, 
		// Timer class:
		// - class named *Timer, directly implements Wm6Timer, inherits from AbstractWm6Timer, has annotations @Singleton and @LocalBean

		@SuppressWarnings("unused")
		String c1 = "name SystemTestST, extends AbstractSystemTest, has @Test methods, in src/test/java";

		Predicate<String> p1 = StringPredicates.endsWith("ST");
		Predicate<String> p2 = Predicates.namedPredicate("endsWith", s -> s.endsWith("ST"));
		//Predicate<String> p3 = s -> s.endsWith("ST");

		CombinedPredicate<String> cp = new CombinedPredicate<>(p1, p2);
		Result<String> r = cp.evaluate("TestST");
		LOG.info("{}", r.getTruePredicates());
	}
}
