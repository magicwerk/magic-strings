package org.magicwerk.strings.matcher;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;
import org.magicwerk.strings.matcher.EscapeStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Test of class {@link StringMatcher}.
 */
public class StringMatcherTest {

	static class StringMatcher2 implements IStringMatcher {
		@SuppressWarnings("unused")
		private String match;

		/**
		 * Constructor.
		 *
		 * @param match	string to look for
		 */
		public StringMatcher2(String match) {
			this.match = match;
		}

		@Override
		public Match find(CharSequence str, int start) {
			return null;
			//		int matchLen = match.length();
			//		StringStreamer streamer = new StringStreamer(str);
			//		int matched = 0;
			//		int c;
			//		while (true) {
			//			if (matched == matchLen) {
			//				return new Match((int)streamer.readPosition()-matched, matched);
			//			}
			//			c = streamer.readChar();
			//			if (c == -1) {
			//				return null;
			//			}
			//			if (c == match.charAt(matched)) {
			//				matched++;
			//			} else {
			//				matched = 0;
			//				if (c == match.charAt(matched)) {
			//					matched++;
			//				}
			//			}
			//		}
		}

	}

	static class StringMatcher3 implements IStringMatcher {
		@SuppressWarnings("unused")
		private String match;

		/**
		 * Constructor.
		 *
		 * @param match	string to look for
		 */
		public StringMatcher3(String match) {
			this.match = match;
		}

		@Override
		public Match find(CharSequence str, int start) {
			return null;
			//		int matchLen = match.length();
			//		StringReader streamer = new StringReader(str);
			//		int matched = 0;
			//		int c = 0;
			//		int read = 0;
			//		while (true) {
			//			if (matched == matchLen) {
			//				return new Match(read-matched, matched);
			//			}
			//			try {
			//				c = streamer.read();
			//			} catch (IOException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			//			if (c == -1) {
			//				return null;
			//			}
			//			read++;
			//			if (c == match.charAt(matched)) {
			//				matched++;
			//			} else {
			//				matched = 0;
			//				if (c == match.charAt(matched)) {
			//					matched++;
			//				}
			//			}
			//		}
		}

	}

	public static void main(String[] args) {
		new StringMatcherTest().run();
	}

	void run() {
		test();
		//testEquals();

		//Performance1JmhTest.test();
		//Performance2JmhTest.test();
	}

	@Capture
	public void testEquals() {
		String FIND = "abc";

		IStringMatcher sm0 = StringMatcher.of(FIND);
		IStringMatcher sm1 = new RegexStringMatcher().setPattern(FIND);

		IMatch m0 = sm0.find(FIND);
		IMatch m1 = sm1.find(FIND);

		CheckTools.check(m0.hashCode() == m1.hashCode());

		CheckTools.check(m0.equals(m1));
		CheckTools.check(m1.equals(m0));
	}

	@Trace(traceMethod = "find")
	public void test() {
		String STR = "01x34";
		String FIND = "x";
		StringMatcher sm = StringMatcher.of(FIND);

		IMatch match = sm.find(STR);
		CheckTools.check(sm.indexOf(STR) == match.getStart());
		CheckTools.check(sm.indexOfEnd(STR) == match.getEnd());

		IMatch revMatch = sm.findReverse(STR);
		CheckTools.check(sm.indexOfReverse(STR) == revMatch.getStart());
		CheckTools.check(sm.indexOfEndReverse(STR) == revMatch.getEnd());

		CheckTools.check(CharSequenceTools.indexOf(STR, FIND) == match.getStart());
		CheckTools.check(CharSequenceTools.lastIndexOf(STR, FIND) == match.getStart());
		CheckTools.check(CharSequenceTools.reverseIndexOf(STR, FIND) == match.getEnd());

		for (int i = 0; i < STR.length(); i++) {
			boolean start = (i == match.getStart());
			boolean startsAt = sm.startsAt(STR, i);
			IMatch startMatch = sm.matchStartingAt(STR, i);
			int startMatchEndIndex = sm.indexOfEndStartingAt(STR, i);

			CheckTools.check(startsAt == start);
			if (start) {
				CheckTools.check(startMatch != null);
				CheckTools.check(startMatch.equals(match));
				CheckTools.check(startMatchEndIndex == startMatch.getEnd());
			} else {
				CheckTools.check(startMatchEndIndex == -1);
			}

			boolean end = (i == match.getEnd());
			boolean endsAt = sm.endsAt(STR, i);
			IMatch endMatch = sm.matchEndingAt(STR, i);
			int endMatchStartIndex = sm.indexOfEndingAt(STR, i);
			CheckTools.check(endsAt == end);
			if (end) {
				CheckTools.check(endMatch != null);
				CheckTools.check(endMatch.equals(match));
				CheckTools.check(endMatchStartIndex == endMatch.getStart());
			} else {
				CheckTools.check(endMatchStartIndex == -1);
			}
		}
	}

	static IStringMatcher regexStringStart = new RegexStringMatcher().setPattern("'");
	static IStringMatcher regexStringEnd = new RegexStringMatcher().setPattern(
			"(?x)" + // allow comments
					"(?>" + // start non-backtracking group
					"  [^']+" + // anything but a quote one or more times
					"|" + // or
					"  ''" + // double quote
					")*" + // end of group
					"'"); // closing quote
	// see http://www.regular-expressions.info/atomic.html
	// Range quoted string: http://blog.stevenlevithan.com/archives/match-quoted-string

	static IStringMatcher escapeStringStart = StringMatcher.of("'");
	static IStringMatcher escapeStringEnd = new EscapeStringMatcher('\'', '\'');

	public static class Performance1JmhTest {

		static void test() {
			Options opts = new Options().includeClass(Performance1JmhTest.class);
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final int length = 10;
			CyclicSource<String> source;

			public MyState() {
				String str = StringTools.repeat("x", length);

				String s0 = str + "before 'str' after";
				String s1 = str + "before 'ante''post' after";
				String s2 = str + "before '' after";
				source = new CyclicSource<>(s0, s1, s2);
			}
		}

		@Benchmark
		public IMatch testEscape(MyState state) {
			return match(state.source.next(), regexStringStart, regexStringEnd);
		}

		@Benchmark
		public IMatch testRegex(MyState state) {
			return match(state.source.next(), regexStringStart, regexStringEnd);
		}

		static IMatch match(String str, IStringMatcher start, IStringMatcher end) {
			IMatch matchStart = start.find(str);
			IMatch matchEnd = null;
			if (matchStart != null) {
				matchEnd = end.find(str.substring(matchStart.getEnd()));
			}
			return matchEnd;
		}
	}

	public static class Performance2JmhTest {

		static void test() {
			Options opts = new Options().includeClass(Performance2JmhTest.class);
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {

			static final String FIND = "abc";

			public enum Impl {
				StringMatcherImpl(StringMatcher.of(FIND)),
				RegexStringMatcherImpl(new RegexStringMatcher().setPattern(FIND));

				IStringMatcher matcher;

				Impl(IStringMatcher matcher) {
					this.matcher = matcher;
				}
			}

			@Param({ "StringMatcherImpl", "RegexStringMatcherImpl" })
			Impl impl;

			String str;

			public MyState() {
				str = StringTools.repeat("-", 1000) + FIND;
			}
		}

		@Benchmark
		public IMatch testImpl(MyState state) {
			return state.impl.matcher.find(state.str);
		}

		//@Benchmark
		//		public IMatch testStringMatcher(MyState state) {
		//			return state.sm.find(state.str);
		//		}
		//
		//		//@Benchmark
		//		public IMatch testStringsMatcher(MyState state) {
		//			return state.ssm.find(state.str);
		//		}
	}

	@Capture
	public static void testRegexStringMatcher() {
		match(regexStringStart, regexStringEnd, 10);
		match(escapeStringStart, escapeStringEnd, 10);
	}

	static void match(IStringMatcher start, IStringMatcher end, int length) {
		String str = StringUtils.repeat("x", length);

		match(str + "before 'str' after", start, end);
		match(str + "before 'ante''post' after", start, end);
		match(str + "before '' after", start, end);
	}

	static void match(String str, IStringMatcher start, IStringMatcher end) {
		IMatch matchStart = start.find(str);
		IMatch matchEnd;
		if (matchStart == null) {
			matchEnd = null;
		} else {
			matchEnd = end.find(str.substring(matchStart.getStart() + matchStart.getLength()));
		}
		System.out.println("Input: " + str);
		if (matchStart == null) {
			System.out.println("No start match");
		} else {
			System.out.println("Start match: " + str.substring(matchStart.getStart()));
			if (matchEnd == null) {
				System.out.println("No end match");
			} else {
				System.out.println("Match: "
						+ str.substring(matchStart.getStart(), matchStart.getStart() + matchStart.getLength() + matchEnd.getStart() + matchEnd.getLength()));
			}
		}
	}

	@Trace(traceMethod = "/.*/")
	public void testStringMatcher() {
		StringMatcher sm = StringMatcher.of("abc");
		sm.find("(abc)");
		sm.indexOf("(abc)");
	}

}
