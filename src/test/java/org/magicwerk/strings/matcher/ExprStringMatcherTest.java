package org.magicwerk.strings.matcher;

import org.magictest.client.Trace;
import org.magicwerk.brownies.core.strings.matcher.ExprStringMatcher;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of {@link ExprStringMatcher}.
 */
public class ExprStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ExprStringMatcherTest().run();
	}

	void run() {
		testFind();
	}

	@Trace
	public void testFind() {
		ExprStringMatcher esm = new ExprStringMatcher();
		esm.setRegex("size (\\w+{size}) bytes");
		esm.setQuery("size > 1000");

		esm.find("start size 900 bytes end");
		esm.find("start size 1100 bytes end");
	}
}
