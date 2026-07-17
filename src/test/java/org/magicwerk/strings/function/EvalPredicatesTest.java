package org.magicwerk.strings.function;

import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.function.EvalPredicates;
import org.magicwerk.brownies.core.function.StringPredicates;
import org.magicwerk.brownies.core.function.EvalPredicates.EvalAndPredicate;
import org.magicwerk.brownies.core.function.EvalPredicates.EvalMultiPredicate.MultiPredicateResult;
import org.magicwerk.brownies.core.function.EvalPredicates.EvalOrPredicate;
import org.magicwerk.brownies.core.function.EvalPredicates.EvalPredicate;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.function.Predicates;
import org.magicwerk.strings.function.Predicates.AndPredicate;
import org.magicwerk.strings.function.Predicates.OrPredicate;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link EvalPredicates}.
 */
public class EvalPredicatesTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new EvalPredicatesTest().run();
	}

	void run() {
		//testEvalPredicate();
		testEvalAndPredicate();
		//testEvalOrPredicate();
	}

	@Capture
	public void testEvalPredicate() {
		Predicate<String> p = StringPredicates.contains("a");
		EvalPredicate<String> ep = new EvalPredicate<>(p);

		ep.setElemPredicate(Predicates.allow());

		IList<String> strs = GapList.create("a", "b", "c");
		strs.forEach(s -> ep.test(s));

		LOG.info("{}", ep.getCountStats());
		LOG.info("{}", ep.getResults());
	}

	@Capture
	public void testEvalOrPredicate() {
		Predicate<String> p1 = StringPredicates.contains("a");
		Predicate<String> p2 = StringPredicates.contains("b");
		OrPredicate<String> op = new OrPredicate<>(p1, p2);

		EvalOrPredicate<String> ep = new EvalOrPredicate<>(op);
		ep.setElemPredicate(Predicates.allow());

		IList<String> strs = GapList.create("a", "b", "c");
		strs.forEach(s -> ep.test(s));
		IList<MultiPredicateResult<String>> mrs = ep.getMultiResults();

		LOG.info("{}", ep.getCountStats());
		LOG.info("{}", mrs);
		for (int i = 0; i < mrs.size(); i++) {
			LOG.info("{}", ep.getResultStats(mrs.get(i)));
		}
		int n = ep.getNumPredicates();
		for (int i = 0; i < n; i++) {
			LOG.info("{}: {}", i, ep.getCountStats(i));
			LOG.info("{}: {}", i, ep.getResults(i));
		}
	}

	@Capture
	public void testEvalAndPredicate() {
		Predicate<String> p1 = StringPredicates.contains("a");
		Predicate<String> p2 = StringPredicates.contains("b");
		AndPredicate<String> op = new AndPredicate<>(p1, p2);

		EvalAndPredicate<String> ep = new EvalAndPredicate<>(op);
		ep.setElemPredicate(Predicates.allow());

		IList<String> strs = GapList.create("ab", "a", "b");
		strs.forEach(s -> ep.test(s));
		IList<MultiPredicateResult<String>> mrs = ep.getMultiResults();

		LOG.info("{}", ep.getCountStats());
		LOG.info("{}", mrs);
		for (int i = 0; i < mrs.size(); i++) {
			LOG.info("{}", ep.getResultStats(mrs.get(i)));
		}
		int n = ep.getNumPredicates();
		for (int i = 0; i < n; i++) {
			LOG.info("{}: {}", i, ep.getCountStats(i));
			LOG.info("{}: {}", i, ep.getResults(i));
		}
	}

}
