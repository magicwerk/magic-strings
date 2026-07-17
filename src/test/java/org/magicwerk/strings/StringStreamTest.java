package org.magicwerk.strings;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.strings.stream.StringStream;
import org.magicwerk.brownies.core.strings.stream.StringStream.IStringStreamMatch;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadUntilChar;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.helper.CheckTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringStream}.
 */
public class StringStreamTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringStreamTest().run();
	}

	void run() {
		//StringStreamCountJmhTest.test();
		//StringStreamFindJmhTest.test();

		//testStringStream();
		//testStringStream2();
		//testStringStream3();
		testStringStream4();
		//testReadUntilMatch();
	}

	public static class StringStreamCountJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringStreamCountJmhTest.class);
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {

			static final char C = '9';

			String str;
			{
				final int len = 1000;
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < len; i++) {
					buf.append((char) ('0' + (i % 10)));
				}
				str = buf.toString();
			}

			CyclicSource<String> src = new CyclicSource<>(str, str + "X");

			String next() {
				return src.next();
			}
		}

		@Benchmark
		public int testCount(MyState state) {
			return count(state.next(), MyState.C);
		}

		@Benchmark
		public int testCount2(MyState state) {
			return count2(state.next(), MyState.C);
		}

		static int count(String str, char c) {
			int n = 0;
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == c) {
					n++;
				}
			}
			return n;
		}

		static int count2(String str, char c) {
			StringStream ss = new StringStream(str);
			int n = 0;
			while (true) {
				int cc = ss.readChar();
				if (cc == -1) {
					return n;
				}
				if (cc == c) {
					n++;
				}
			}
		}
	}

	public static class StringStreamFindJmhTest {

		static void test() {
			Options opts = new Options().includeClass(StringStreamFindJmhTest.class);
			opts.setUseGcProfiler(true);

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		@State(Scope.Benchmark)
		public static class MyState {

			static final char C = '9';

			String str;
			{
				final int len = 1000;
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < len; i++) {
					buf.append((char) ('0' + (i % 10)));
				}
				str = buf.toString();
			}

			CyclicSource<String> src = new CyclicSource<>(str, str + "X");

			String next() {
				return src.next();
			}
		}

		@Benchmark
		public int testFind(MyState state) {
			return find(state.next(), MyState.C);
		}

		@Benchmark
		public int testFind2(MyState state) {
			return find2(state.next(), MyState.C);
		}

		@Benchmark
		public int testFind3(MyState state) {
			return find3(state.next(), MyState.C);
		}

		@Benchmark
		public int testFind4(MyState state) {
			return find2(state.next(), MyState.C);
		}

		static int find(String str, char c) {
			return str.indexOf(c);
		}

		static int find2(String str, char c) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == c) {
					return i;
				}
			}
			return -1;
		}

		static int find3(String str, char c) {
			StringStream ss = new StringStream(str);
			int i = 0;
			while (true) {
				int cc = ss.readChar();
				if (cc == -1) {
					return -1;
				}
				if (cc == c) {
					return i;
				}
				i++;
			}
		}

		static int find4(String str, char c) {
			StringStream ss = new StringStream(str);
			return ss.readUntil(cc -> cc == c);
		}
	}

	@Capture
	public void test() {
		String in = "01234";
		StringStream ss = new StringStream(in);
		char c0 = (char) ss.readChar();
		String s0 = ss.readString(2);
		String s1 = ss.readText();
		int c1 = ss.readChar();
		LOG.info("{}:{}:{}:{}", c0, s0, s1, c1);
	}

	@Capture
	public void testStringStream() {
		String in = "(abc)";
		StringStream ss = new StringStream(in);

		ss.readChar('(');
		ss.mark();
		ss.readConsume(new ReadUntilChar(')').setConsumeMatch(false));
		String s = ss.marked();
		ss.readChar(')');

		LOG.info("{} -> {}", in, s);
	}

	@Capture
	public void testStringStream2() {
		String in = "(abc)";
		{
			// Read until found
			StringStream ss = new StringStream(in);
			ss.readChar('(');
			int len = ss.readUntil(CharPredicates.equals(')'));
			String s = (len != -1) ? ss.consumed(len, 1) : null;
			LOG.info("{} -> {}", in, s);
			CheckTools.check(ss.isEof());
		}
		{
			// Read until end as not found
			StringStream ss = new StringStream(in);
			ss.readChar('(');
			ss.mark();
			int len = ss.readUntil(CharPredicates.equals(']'));
			String s = (len != -1) ? ss.consumed(len, 1) : null;
			String s2 = ss.marked();
			LOG.info("{} -> {} {}", in, s, s2);
			CheckTools.check(ss.isEof());
		}
	}

	@Capture
	public void testStringStream3() {
		String in = "(abc)";
		{
			StringStream ss = new StringStream(in);
			ss.readChar('(');
			int len = ss.readWhile(CharPredicates.notEquals(')'));
			String s = ss.consumed(len);
			LOG.info("{} -> {}", in, s);
		}
		{
			StringStream ss = new StringStream(in);
			ss.readChar('(');
			String s = ss.readStringWhile(CharPredicates.notEquals(')'));
			LOG.info("{} -> {}", in, s);
		}
		{
			StringStream ss = new StringStream(in);
			String s = ss.readStringWhile(CharPredicates.equals('x'));
			LOG.info("{} -> {}", in, s);
		}
	}

	@Capture
	public void testStringStream4() {
		String in = "--ab--cd=";
		StringStream ss = new StringStream(in);

		ss.readString("--");
		ss.mark();
		String s0 = ss.readStringUntilMatch(s -> s.readStringIf("--") ? 2 : -1);
		ss.mark();
		String s1 = ss.readStringUntilMatch(IStringStreamMatch.of(CharPredicates.equals('=')));
		ss.mark();
		String s2 = ss.readStringUntilMatch(s -> s.readStringIf("--") ? 2 : -1);

		LOG.info("{} -> {} -> {} -> {}", in, s0, s1, s2);
	}

	@Capture
	public void testReadUntilMatch() {
		String in = "abc--cde";
		{
			StringStream ss = new StringStream(in);
			ss.mark();
			int len = ss.readUntilMatch(s -> s.readStringIf("--") ? 2 : -1);
			String match = (len != -1) ? ss.consumed(len) : null;
			String before = (len != -1) ? ss.marked(-len) : ss.marked();
			LOG.info("{} -> {}/{} ", in, before, match);
		}
		{
			StringStream ss = new StringStream(in);
			ss.mark();
			int len = ss.readUntilMatch(s -> s.readStringIf("==") ? 2 : -1);
			String match = (len != -1) ? ss.consumed(len) : null;
			String before = (len != -1) ? ss.marked(-len) : ss.marked();
			LOG.info("{} -> {}/{} ", in, before, match);
		}
	}

}
