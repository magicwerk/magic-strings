package org.magicwerk.strings;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhTool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Evaluation how method {@link StringUtils#replaceChars} in commons-lang could be optimized.
 */
public class CommonsLangReplaceCharsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CommonsLangReplaceCharsTest().run();
	}

	void run() {
		ReplaceCharsTestJmh.test();
	}

	void test() {
		testReplaceChars_StringStringString_317();
		testReplaceChars_StringStringString_NEW();
	}

	public static class ReplaceCharsTestJmh {

		static void test() {
			Options opts = new Options().includeClass(ReplaceCharsTestJmh.class);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands11));
			opts.setUseGcProfiler(true);
			opts.setRunTimeMillis(1000);

			JmhRunner runner = new JmhRunner();
			runner.verifyJmhMethods(opts, 10);
			//runner.runJmh(opts);

			JmhTool jr = new JmhTool();
			jr.runJmh(opts);
			jr.showPivotTable();
		}

		static class Replace {
			String str;
			String search;
			String replace;

			Replace(String str, String search, String replace) {
				this.str = str;
				this.search = search;
				this.replace = replace;
			}
		}

		static Replace ReplaceNormal = new Replace("012345678901234567890123456789012345678,", ",", ";");
		static Replace ReplaceNormal2 = new Replace("01234567abdasdfasdfs123456789012345678,", ",", ";");
		static Replace ReplaceNormal3 = new Replace("01234567890123sdfasdfasdfas89012345678,", ",", ";");
		//static Replace ReplaceNormal = new Replace("0123,5678,0123,5678,", ",", ";");
		static Replace ReplaceLate = new Replace("012345678,012345678,", ",", ";");
		static Replace ReplaceNone = new Replace("01234567890123456789", ";", ";");
		static Replace ReplaceAll = new Replace(",,,,,,,,,,,,,,,,,,,,", ",", ";");

		@State(Scope.Benchmark)
		public static class TestState {
			IList<Replace> replaces = GapList.create(ReplaceNormal, ReplaceLate, ReplaceNone, ReplaceAll);
			//CyclicSource<Replace> replaces2 = new CyclicSource<>(ReplaceNormal, ReplaceNormal2, ReplaceNormal3);

			@Param({ "0", "1", "2", "3" })
			//@Param({ "0" })
			public int param;

			Replace get() {
				return replaces.get(param);
				//return replaces2.next();
			}
		}

		@Benchmark
		public String testReplaceChars_317(TestState state) {
			Replace replace = state.get();
			return StringUtils_317.replaceChars(replace.str, replace.search, replace.replace);
		}

		@Benchmark
		public String testReplaceChars_NEW(TestState state) {
			Replace replace = state.get();
			return StringUtils_NEW.replaceChars(replace.str, replace.search, replace.replace);
		}
	}

	static class StringUtils_NEW {

		public static String replaceChars2(String str, String searchChars, String replaceChars) {
			if (StringTools.isEmpty(str) || StringTools.isEmpty(searchChars)) {
				return str;
			}
			replaceChars = ObjectUtils.toString(replaceChars);
			final int replaceCharsLen = replaceChars.length();
			final int strLength = str.length();
			//StringBuilder buf = null;
			boolean modified = false;
			StringBuilder buf = new StringBuilder(strLength);
			int start = 0;
			for (int i = 0; i < strLength; i++) {
				final char ch = str.charAt(i);
				final int index = searchChars.indexOf(ch);
				if (index >= 0) {
					//if (buf == null) {
					//buf = new StringBuilder(strLength);
					//}
					if (start < i) {
						buf.append(str, start, i);
					}
					if (index < replaceCharsLen) {
						buf.append(replaceChars.charAt(index));
					}
					start = i + 1;
					modified = true;
				}
			}

			//if (buf != null) {
			if (modified) {
				if (start < strLength) {
					buf.append(str, start, strLength);
				}
				return buf.toString();
			}
			return str;
		}

		public static String replaceChars(final String str, final String searchChars, String replaceChars) {
			if (isEmpty(str) || isEmpty(searchChars)) {
				return str;
			}
			replaceChars = ObjectUtils.toString(replaceChars);
			boolean modified = false;
			final int replaceCharsLength = replaceChars.length();
			final int strLength = str.length();
			StringBuilder buf = null;
			for (int i = 0; i < strLength; i++) {
				final char ch = str.charAt(i);
				final int index = searchChars.indexOf(ch);
				if (index >= 0) {
					if (buf == null) {
						buf = new StringBuilder(strLength);
						buf.append(str, 0, i);
					}
					modified = true;
					if (index < replaceCharsLength) {
						buf.append(replaceChars.charAt(index));
					}
				} else {
					if (buf != null) {
						buf.append(ch);
					}
				}
			}
			if (buf != null) {
				return buf.toString();
			}
			return str;
		}
	}

	static class StringUtils_317 {

		public static String replaceChars(final String str, final String searchChars, String replaceChars) {
			if (isEmpty(str) || isEmpty(searchChars)) {
				return str;
			}
			replaceChars = ObjectUtils.toString(replaceChars);
			boolean modified = false;
			final int replaceCharsLength = replaceChars.length();
			final int strLength = str.length();
			final StringBuilder buf = new StringBuilder(strLength);
			for (int i = 0; i < strLength; i++) {
				final char ch = str.charAt(i);
				final int index = searchChars.indexOf(ch);
				if (index >= 0) {
					modified = true;
					if (index < replaceCharsLength) {
						buf.append(replaceChars.charAt(index));
					}
				} else {
					buf.append(ch);
				}
			}
			if (modified) {
				return buf.toString();
			}
			return str;
		}

	}

	static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	//

	public void testReplaceChars_StringStringString_NEW() {
		assertNull(StringUtils_NEW.replaceChars(null, null, null));
		assertNull(StringUtils_NEW.replaceChars(null, "", null));
		assertNull(StringUtils_NEW.replaceChars(null, "a", null));
		assertNull(StringUtils_NEW.replaceChars(null, null, ""));
		assertNull(StringUtils_NEW.replaceChars(null, null, "x"));

		assertEquals("", StringUtils_NEW.replaceChars("", null, null));
		assertEquals("", StringUtils_NEW.replaceChars("", "", null));
		assertEquals("", StringUtils_NEW.replaceChars("", "a", null));
		assertEquals("", StringUtils_NEW.replaceChars("", null, ""));
		assertEquals("", StringUtils_NEW.replaceChars("", null, "x"));

		assertEquals("abc", StringUtils_NEW.replaceChars("abc", null, null));
		assertEquals("abc", StringUtils_NEW.replaceChars("abc", null, ""));
		assertEquals("abc", StringUtils_NEW.replaceChars("abc", null, "x"));

		assertEquals("abc", StringUtils_NEW.replaceChars("abc", "", null));
		assertEquals("abc", StringUtils_NEW.replaceChars("abc", "", ""));
		assertEquals("abc", StringUtils_NEW.replaceChars("abc", "", "x"));

		assertEquals("ac", StringUtils_NEW.replaceChars("abc", "b", null));
		assertEquals("ac", StringUtils_NEW.replaceChars("abc", "b", ""));
		assertEquals("axc", StringUtils_NEW.replaceChars("abc", "b", "x"));

		assertEquals("ayzya", StringUtils_NEW.replaceChars("abcba", "bc", "yz"));
		assertEquals("ayya", StringUtils_NEW.replaceChars("abcba", "bc", "y"));
		assertEquals("ayzya", StringUtils_NEW.replaceChars("abcba", "bc", "yzx"));

		assertEquals("abcba", StringUtils_NEW.replaceChars("abcba", "z", "w"));
		assertSame("abcba", StringUtils_NEW.replaceChars("abcba", "z", "w"));

		// Javadoc examples:
		assertEquals("jelly", StringUtils_NEW.replaceChars("hello", "ho", "jy"));
		assertEquals("ayzya", StringUtils_NEW.replaceChars("abcba", "bc", "yz"));
		assertEquals("ayya", StringUtils_NEW.replaceChars("abcba", "bc", "y"));
		assertEquals("ayzya", StringUtils_NEW.replaceChars("abcba", "bc", "yzx"));

		// From https://issues.apache.org/bugzilla/show_bug.cgi?id=25454
		assertEquals("bcc", StringUtils_NEW.replaceChars("abc", "ab", "bc"));
		assertEquals("q651.506bera", StringUtils_NEW.replaceChars("d216.102oren",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789",
				"nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM567891234"));
	}

	public void testReplaceChars_StringStringString_317() {
		assertNull(StringUtils_317.replaceChars(null, null, null));
		assertNull(StringUtils_317.replaceChars(null, "", null));
		assertNull(StringUtils_317.replaceChars(null, "a", null));
		assertNull(StringUtils_317.replaceChars(null, null, ""));
		assertNull(StringUtils_317.replaceChars(null, null, "x"));

		assertEquals("", StringUtils_317.replaceChars("", null, null));
		assertEquals("", StringUtils_317.replaceChars("", "", null));
		assertEquals("", StringUtils_317.replaceChars("", "a", null));
		assertEquals("", StringUtils_317.replaceChars("", null, ""));
		assertEquals("", StringUtils_317.replaceChars("", null, "x"));

		assertEquals("abc", StringUtils_317.replaceChars("abc", null, null));
		assertEquals("abc", StringUtils_317.replaceChars("abc", null, ""));
		assertEquals("abc", StringUtils_317.replaceChars("abc", null, "x"));

		assertEquals("abc", StringUtils_317.replaceChars("abc", "", null));
		assertEquals("abc", StringUtils_317.replaceChars("abc", "", ""));
		assertEquals("abc", StringUtils_317.replaceChars("abc", "", "x"));

		assertEquals("ac", StringUtils_317.replaceChars("abc", "b", null));
		assertEquals("ac", StringUtils_317.replaceChars("abc", "b", ""));
		assertEquals("axc", StringUtils_317.replaceChars("abc", "b", "x"));

		assertEquals("ayzya", StringUtils_317.replaceChars("abcba", "bc", "yz"));
		assertEquals("ayya", StringUtils_317.replaceChars("abcba", "bc", "y"));
		assertEquals("ayzya", StringUtils_317.replaceChars("abcba", "bc", "yzx"));

		assertEquals("abcba", StringUtils_317.replaceChars("abcba", "z", "w"));
		assertSame("abcba", StringUtils_317.replaceChars("abcba", "z", "w"));

		// Javadoc examples:
		assertEquals("jelly", StringUtils_317.replaceChars("hello", "ho", "jy"));
		assertEquals("ayzya", StringUtils_317.replaceChars("abcba", "bc", "yz"));
		assertEquals("ayya", StringUtils_317.replaceChars("abcba", "bc", "y"));
		assertEquals("ayzya", StringUtils_317.replaceChars("abcba", "bc", "yzx"));

		// From https://issues.apache.org/bugzilla/show_bug.cgi?id=25454
		assertEquals("bcc", StringUtils_317.replaceChars("abc", "ab", "bc"));
		assertEquals("q651.506bera", StringUtils_317.replaceChars("d216.102oren",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789",
				"nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM567891234"));
	}

	void assertEquals(Object o1, Object o2) {
		CheckTools.check(ObjectTools.equals(o1, o2));
	}

	void assertSame(Object o1, Object o2) {
		CheckTools.check(o1 == o2);
	}

	void assertNull(Object o1) {
		CheckTools.check(o1 == null);
	}

}
