package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.NestedStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test of {@link NestedStringMatcher}.
 */
public class NestedStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new NestedStringMatcherTest().run();
	}

	void run() {
		//testNestedStringMatcher();
	}

	@Capture
	public void testNestedStringMatcher() {
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			StringMatcher m2 = StringMatcher.of("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			testNestedStringMatcher(nsm, "a<bxc>bxc<bxc>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			StringMatcher m2 = StringMatcher.of("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			testNestedStringMatcher(nsm, "a<byc>bxc<bxc>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			StringMatcher m2 = StringMatcher.of("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			testNestedStringMatcher(nsm, "a<bxcxd>bxc<bxc>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			testNestedStringMatcher(nsm, "a<bxc>bxc<bxc>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern("\\[.*?\\]");
			StringMatcher m3 = StringMatcher.of("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2, m3);
			testNestedStringMatcher(nsm, "a<b[dxe]c>bxc<b[dxe]c>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern("\\[.*?\\]");
			RegexStringMatcher m3 = new RegexStringMatcher().setPattern("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2, m3);
			testNestedStringMatcher(nsm, "a<b[dxe]c>bxc<b[dxe]c>b", 0);
		}
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			StringMatcher m2 = StringMatcher.of("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			testNestedStringMatcher(nsm, "a<bxc>b", 2);
		}

		// Replace
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			StringMatcher m2 = StringMatcher.of("ab");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);

			StringReplacer sr = StringReplacer.builder().setFindMatcher(nsm).setReplaceString("AB").build();
			String r = sr.replace("01 ab < 23 ab 45 > ab 67");
			LOG.info("{}", r);
		}
	}

	void testNestedStringMatcher(NestedStringMatcher nsm, String str, int start) {
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
