package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.CharMatcher;
import org.magicwerk.strings.matcher.CodePointMatcher;
import org.magicwerk.strings.matcher.IStringEndsAtMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringReverseMatcher;
import org.magicwerk.strings.matcher.IStringStartsAtMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.StringsMatcher;

/**
 * Test of class {@link StringMatcher}.
 */
public class IStringMatcherTest {

	public static void main(String[] args) {
		new IStringMatcherTest().run();
	}

	void run() {
		test();
	}

	@Capture
	public void test() {
		int startIndex = 2;
		int endIndex = 3;
		String input = "01x34";

		char c = 'x';
		int cp = c;
		String s = String.valueOf(c);

		CharMatcher cm = new CharMatcher(c);
		CodePointMatcher cpm = new CodePointMatcher(cp);
		StringMatcher sm = StringMatcher.of(s);
		StringsMatcher ssm = new StringsMatcher.Builder().setSearchStrs(s).build();
		StringsMatcher ssm2 = new StringsMatcher.Builder().setSearchStrs("yy", s).build();
		IList<IStringMatcher> ms = GapList.create(cm, cpm, sm, ssm, ssm2);

		for (int i = -1; i <= input.length(); i++) {
			int ic = input.indexOf(c, i);
			int is = input.indexOf(s, i);
			int lic = input.lastIndexOf(c, i);
			int lis = input.lastIndexOf(s, i);

			CheckTools.check(ic == is);
			CheckTools.check(lic == lis);
			CheckTools.check(ic == -1 || lic == -1 || ic == lic);

			boolean match = (i <= startIndex);
			boolean matchReverse = (i >= endIndex);
			boolean matchStartsAt = (i == startIndex);
			boolean matchEndsAt = (i == endIndex);
			IMatch m = match(ms, input, i, match, matchReverse, matchStartsAt, matchEndsAt);

			CheckTools.check(m != null);
			CheckTools.check(ic == -1 || ic == m.getStart());
			CheckTools.check(lic == -1 || lic == m.getEnd() - 1);
		}
	}

	IMatch match(IList<IStringMatcher> ms, String input, int index, boolean match, boolean matchReverse, boolean matchStartsAt, boolean matchEndsAt) {
		IList<IMatch> matches = ms.map(m -> matches(m, input, index, match, matchReverse, matchStartsAt, matchEndsAt));
		IList<IMatch> ns = CollectionTools2.getDistinct(matches);
		return ns.getSingle();
	}

	IMatch matches(IStringMatcher m, String input, int i,
			boolean match, boolean matchReverse, boolean matchStartsAt, boolean matchEndsAt) {
		IMatch m0 = match(m, input, i);
		IMatch m1 = matchReverse((IStringReverseMatcher) m, input, i);
		IMatch m2 = matchStartsAt((IStringStartsAtMatcher) m, input, i);
		IMatch m3 = matchEndsAt((IStringEndsAtMatcher) m, input, i);

		CheckTools.check(match == (m0 != null));
		CheckTools.check(matchReverse == (m1 != null));
		CheckTools.check(matchStartsAt == (m2 != null));
		CheckTools.check(matchEndsAt == (m3 != null));

		IList<IMatch> ms = GapList.create(m0, m1, m2, m3);
		ms.removeIf(n -> n == null);
		IList<IMatch> ns = CollectionTools2.getDistinct(ms);
		return ns.getSingle();
	}

	IMatch match(IStringMatcher sm, String str, int start) {
		IMatch match = sm.find(str, start);
		int startIndex = sm.indexOf(str, start);
		int endIndex = sm.indexOfEnd(str, start);

		CheckTools.check(match == null || match.getStart() == startIndex);
		CheckTools.check(match == null || match.getEnd() == endIndex);
		return match;
	}

	IMatch matchReverse(IStringReverseMatcher sm, String str, int end) {
		IMatch match = sm.findReverse(str, end);
		int startIndex = sm.indexOfReverse(str, end);
		int endIndex = sm.indexOfEndReverse(str, end);

		CheckTools.check(match == null || match.getStart() == startIndex);
		CheckTools.check(match == null || match.getEnd() == endIndex);
		return match;
	}

	IMatch matchStartsAt(IStringStartsAtMatcher sm, String str, int start) {
		IMatch startMatch = sm.matchStartingAt(str, start);
		boolean startsAt = sm.startsAt(str, start);
		int startMatchEndIndex = sm.indexOfEndStartingAt(str, start);

		CheckTools.check(startsAt == (startMatch != null));
		CheckTools.check(startMatch == null || startMatch.getEnd() == startMatchEndIndex);
		return startMatch;
	}

	IMatch matchEndsAt(IStringEndsAtMatcher sm, String str, int end) {
		IMatch endMatch = sm.matchEndingAt(str, end);
		boolean endsAt = sm.endsAt(str, end);
		int endMatchStartIndex = sm.indexOfEndingAt(str, end);

		CheckTools.check(endsAt == (endMatch != null));
		CheckTools.check(endMatch == null || endMatch.getStart() == endMatchStartIndex);
		return endMatch;
	}

}
