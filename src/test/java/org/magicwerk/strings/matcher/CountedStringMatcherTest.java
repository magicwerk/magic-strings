package org.magicwerk.strings.matcher;

import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.diff.ObjectDiff;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.CountedStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test of class {@link CountedStringMatcher}.
 */
public class CountedStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CountedStringMatcherTest().run();
	}

	void run() {
		testCountedStringMatcher();
	}

	@Trace(traceMethod = "/.*/")
	public void testCountedStringMatcher() {
		String find = "ab";
		String str = "(ab)(ab)(ab)";
		StringMatcher sm = StringMatcher.of(find);
		RegexStringMatcher rsm = new RegexStringMatcher().setPattern(find);
		doTestCountedStringMatcher(sm, rsm, str);
	}

	@InheritTrace
	void doTestCountedStringMatcher(StringMatcher sm, RegexStringMatcher rsm, String str) {
		// Forward
		doMatch(sm, rsm, str, 0);
		doMatch(sm, rsm, str, 1);
		doMatch(sm, rsm, str, 2);
		doMatch(sm, rsm, str, 3);

		// Backward
		doMatch(sm, rsm, str, -1);
		doMatch(sm, rsm, str, -2);
		doMatch(sm, rsm, str, -3);
		doMatch(sm, rsm, str, -4);
	}

	@InheritTrace
	void doMatch(StringMatcher sm, RegexStringMatcher rsm, String str, int count) {
		boolean overlap = false;
		CountedStringMatcher csm = CountedStringMatcher.of(sm, count, overlap);
		IMatch m = doMatch(csm, str);
		CountedStringMatcher crsm = CountedStringMatcher.of(rsm, count, overlap);
		IMatch rm = doMatch(crsm, str);
		ObjectDiff.checkEqual(m, rm);
	}

	@InheritTrace
	IMatch doMatch(IStringMatcher csm, String str) {
		IMatch match = csm.find(str);
		int start = csm.indexOf(str);
		int end = csm.indexOfEnd(str);
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
