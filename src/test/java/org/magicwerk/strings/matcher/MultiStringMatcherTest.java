package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.MultiStringMatcher;
import org.magicwerk.strings.matcher.NestedStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.slf4j.Logger;

/**
 * Test of {@link NestedStringMatcher}.
 */
public class MultiStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new MultiStringMatcherTest().run();
	}

	void run() {
		testMultiStringMatcher();
	}

	@Capture
	public void testMultiStringMatcher() {
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("aS");
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern("(?<=S)b");
			MultiStringMatcher msm = new MultiStringMatcher(m1, m2);
			testStringMatcher(msm, "aSb", 0);
		}
		{
			RegexStringMatcher rsm = new RegexStringMatcher().setPattern("aS|(?<=S)b");
			testStringMatcher(rsm, "aSb", 0);
		}
	}

	void testStringMatcher(IStringMatcher nsm, String str, int start) {
		while (true) {
			IMatch m = nsm.find(str, start);
			if (m == null) {
				break;
			}
			LOG.info("{}", m);
			start = m.getEnd();
		}
		LOG.info("");
	}
}
