package org.magicwerk.strings.matcher;

import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.diff.ObjectDiff;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.RepeatedStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test of class {@link RepeatedStringMatcher}.
 */
public class RepeatedStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new RepeatedStringMatcherTest().run();
	}

	void run() {
		testRepeatedStringMatcher();

		//new RepeatedStringMatcherJmhTest().test();
	}

	@Trace(traceMethod = "/.*/")
	public void testRepeatedStringMatcher() {
		String find = "01";
		String str = "a0101b0101c";
		StringMatcher sm = StringMatcher.of(find);
		RegexStringMatcher rsm = new RegexStringMatcher().setPattern(find);
		doTestRepeatedStringMatcher(sm, rsm, str);
	}

	@InheritTrace
	void doTestRepeatedStringMatcher(StringMatcher sm, RegexStringMatcher rsm, String str) {
		RepeatedStringMatcher mr0 = RepeatedStringMatcher.of(sm, false);
		IMatch m0 = doMatch(mr0, str);
		RepeatedStringMatcher mr1 = RepeatedStringMatcher.of(rsm, false);
		IMatch m1 = doMatch(mr1, str);
		ObjectDiff.checkEqual(m0, m1);

		// TODO add missing case
		RepeatedStringMatcher mr2 = RepeatedStringMatcher.of(sm, true);
		IMatch m2 = doMatch(mr2, str);
		RepeatedStringMatcher mr3 = RepeatedStringMatcher.of(rsm, true);
		IMatch m3 = doMatch(mr3, str);
		ObjectDiff.checkEqual(m2, m3);
	}

	@InheritTrace
	IMatch doMatch(RepeatedStringMatcher rsm, String str) {
		IMatch match = rsm.find(str);
		int start = rsm.indexOf(str);
		int end = rsm.indexOfEnd(str);
		if (match != null) {
			CheckTools.check(match.getStart() == start);
			CheckTools.check(match.getEnd() == end);
		} else {
			CheckTools.check(start == -1);
			CheckTools.check(end == -1);
		}
		return match;
	}

}
