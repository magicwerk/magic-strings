package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magictest.client.InheritTrace;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.diff.ObjectDiff;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.AnchoredStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.RepeatedStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link AnchoredStringMatcher}.
 */
public class AnchoredStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new AnchoredStringMatcherTest().run();
	}

	void run() {
		//		testAnchoredStringMatcher();
		testAnchoredRepeatedStringMatcher();

		//new AnchoredStringMatcherJmhTest().test();
	}

	@Capture
	public void testAnchoredRepeatedStringMatcher() {
		String str = "01";
		IList<String> inputs = GapList.create("0101abc", "abc0101");

		StringMatcher sm = StringMatcher.of(str);
		RepeatedStringMatcher rsm = RepeatedStringMatcher.of(sm, false);
		AnchoredStringMatcher asm = AnchoredStringMatcher.of(sm, 0);
		AnchoredStringMatcher arsm = AnchoredStringMatcher.of(rsm, 0);
		AnchoredStringMatcher asm2 = AnchoredStringMatcher.of(sm, -1);
		AnchoredStringMatcher arsm2 = AnchoredStringMatcher.of(rsm, -1);
		IList<IStringMatcher> sms = GapList.create(sm, rsm, asm, arsm, asm2, arsm2);

		for (String input : inputs) {
			LOG.info("\nInput {}:", input);
			for (IStringMatcher m : sms) {
				doFind(m, input);
			}
		}
	}

	void doFind(IStringMatcher sm, String input) {
		StringFinder sf = StringFinder.builder().setFindMatcher(sm).build();
		IMatch m = sf.find(input);
		LOG.info("{} ({})", m, sm);
	}

	@Trace(traceMethod = "/.*/")
	public void testAnchoredStringMatcher() {
		String find = "abc";
		StringMatcher sm = StringMatcher.of(find);
		RegexStringMatcher rsm = new RegexStringMatcher().setPattern(find);

		doMatch(sm, rsm, "abc)", 0);
		doMatch(sm, rsm, "(abc)", 0);

		doMatch(sm, rsm, "abc)", 1);
		doMatch(sm, rsm, "(abc)", 1);

		doMatch(sm, rsm, "(abc", -1);
		doMatch(sm, rsm, "(abc)", -1);

		doMatch(sm, rsm, "(abc", -2);
		doMatch(sm, rsm, "(abc)", -2);
	}

	@InheritTrace
	void doMatch(StringMatcher sm, RegexStringMatcher rsm, String str, int pos) {
		AnchoredStringMatcher csm = AnchoredStringMatcher.of(sm, pos);
		IMatch m = doMatch(csm, str);

		Report.setAutoTrace(false);
		AnchoredStringMatcher crsm = AnchoredStringMatcher.of(rsm, pos);
		IMatch rm = doMatch(crsm, str);
		Report.setAutoTrace(true);

		ObjectDiff.checkEqual(m, rm);
	}

	@InheritTrace
	IMatch doMatch(AnchoredStringMatcher csm, String str) {
		IMatch match = csm.find(str);
		int start = csm.indexOf(str);
		int end = csm.indexOfEnd(str);
		checkMatch(match, start, end);

		boolean autoTrace = Report.isAutoTrace();
		Report.setAutoTrace(false);

		IMatch match2;
		int start2;
		int end2;

		boolean startsAt = (csm.pos >= 0);
		if (startsAt) {
			int pos = csm.pos;
			if (match != null) {
				CheckTools.check(pos == start);
			}

			match2 = csm.matchStartingAt(str, pos);
			start2 = (csm.startsAt(str, pos)) ? pos : -1;
			end2 = csm.indexOfEndStartingAt(str, pos);

		} else {
			int pos = str.length() + csm.pos + 1;
			if (match != null) {
				CheckTools.check(str.length() + csm.pos + 1 == end);
			}

			match2 = csm.matchEndingAt(str, pos);
			start2 = csm.indexOfEndingAt(str, pos);
			end2 = csm.endsAt(str, pos) ? end : -1;
		}
		checkMatch(match2, start2, end2);
		CheckTools.check(ObjectTools.equals(match2, match) && start2 == start && end2 == end);

		Report.setAutoTrace(autoTrace);
		return match;
	}

	void checkMatch(IMatch match, int start, int end) {
		if (match != null) {
			CheckTools.check(match.getStart() == start);
			CheckTools.check(match.getEnd() == end);
		} else {
			CheckTools.check(start == -1);
			CheckTools.check(end == -1);
		}
	}

	/**
	 * Show that RepeatedStringMatcher outperforms a regex by factor 4.
	 */
	public static class AnchoredStringMatcherJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String find = "01";
			StringMatcher sm = StringMatcher.of(find);
			AnchoredStringMatcher asm = AnchoredStringMatcher.of(sm, 0);
			StringFinder sf = StringFinder.builder().setFindString(find).setAnchored(true).build();

			CyclicSource<String> strings = new CyclicSource<>(10, i -> find + "-" + i);
		}

		@Benchmark
		public boolean testStringStartsWith(MyState state) {
			String s = state.strings.next();
			return s.startsWith(state.find);
		}

		@Benchmark
		public boolean testAnchoredStringMatcher(MyState state) {
			String s = state.strings.next();
			return state.asm.indexOf(s) != -1;
		}

		@Benchmark
		public boolean testAnchoredStringFinder(MyState state) {
			String s = state.strings.next();
			return state.sf.contains(s);
		}
	}
}
