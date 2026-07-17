package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IgnoreStringMatcher;
import org.slf4j.Logger;

/**
 * Test of {@link IgnoreStringMatcher}.
 */
public class IgnoreStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new IgnoreStringMatcherTest().run();
	}

	void run() {
		testIgnoreStringMatcher();
	}

	@Capture
	public void testIgnoreStringMatcher() {
		testFind("abc", "[abc");
		testFind("abc", "abc]");
		testFind("abc", "[abc]");
		{
			IgnoreStringMatcher m = new IgnoreStringMatcher("java", "javascript");
			testFind(m, "a java b");
			testFind(m, "a javascript b");
			testFind(m, "a powerjava b");
		}
		{
			IgnoreStringMatcher m = new IgnoreStringMatcher("java", "javascript", "powerjava");
			testFind(m, "a java b");
			testFind(m, "a javascript b");
			testFind(m, "a powerjava b");
		}
		{
			IgnoreStringMatcher m = new IgnoreStringMatcher("xx", "00xx11xx22");
			testFind(m, "a xx b");
			testFind(m, "a 00xx11 b");
			testFind(m, "a 11xx22 b");
			testFind(m, "a 00xx11xx22 b");
		}
	}

	void testFind(String match, String ignore) {
		IgnoreStringMatcher m = new IgnoreStringMatcher(match, ignore);

		IList<String> strs = GapList.create("abc", "[abc", "abc]", "[abc]", "a[abc]c");
		for (String str : strs) {
			testFind(m, str);
		}
		LOG.info("");
	}

	void testFind(IStringMatcher matcher, String input) {
		IMatch match = matcher.find(input);
		LOG.info("{}: on '{}' -> {}", matcher, input, match);
	}
}
