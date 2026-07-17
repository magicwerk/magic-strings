package org.magicwerk.strings.function;

import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.function.Predicates;
import org.magicwerk.strings.helper.CheckTools;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link Predicates}.
 */
public class PredicatesTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new PredicatesTest().run();
	}

	void run() {
		testPredicates();
	}

	@Capture
	public void testPredicates() {
		{
			Predicate<String> p = Predicates.allow();
			CheckTools.check(p.test(null));
			CheckTools.check(p.test("abc"));
		}
		{
			Predicate<String> p = Predicates.deny();
			CheckTools.check(!p.test(null));
			CheckTools.check(!p.test("abc"));
		}
	}
}
