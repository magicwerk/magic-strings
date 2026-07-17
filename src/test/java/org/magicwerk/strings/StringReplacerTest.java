package org.magicwerk.strings;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.CollectionTools2;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.CompareExecutor;
import org.magicwerk.brownies.core.conditional.ConditionalOperator;
import org.magicwerk.brownies.core.strings.escape.EnclosingEscaper.EnclosingMode;
import org.magicwerk.brownies.core.strings.escape.EscapeCharEscapers.EscapeCharEscaper;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.StringReplacer.IReplaceStringImpl;
import org.magicwerk.strings.StringReplacerAppender.ConstStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.IStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.RegexMatchReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.StringAppendReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.StringTransformReplaceAppender;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.RegexMatch;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.NestedStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringReplacer}.
 */
public class StringReplacerTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringReplacerTest().run();
	}

	void run() {
		//testGetMatches();
		//testReplace();
		//testStringTransformer();
		//testGeneric();
		//testNoChange();
		//testEmpty();
		//testNew();
		//testReplaceNew();

		//testBenchmarks();
		//runBenchmarks();

		new StringReplacerTestJmh().test();
		//new StringReplaceEscapeTestJmh().test();
		//new StringReplaceAppendTestJmh().test();
		//new StringReplaceOrRemoveTestJmh().test();
	}

	//@Capture(source = Source.NONE)
	public void testGeneric() {
		IStringTransformerBuilder builder = StringReplacer.builder().replaceChar('-', '=');
		IList<String> inputs = GapList.create("123", "-123-");
		testStringTranformerGeneric(builder, inputs);
	}

	/**
	 * Show performance of replace/remove with direct implementation, flag or Consumer.
	 */
	public static class StringReplaceOrRemoveTestJmh extends StringJmhBenchmark {
		{
			setJavaVersions(JavaVersion.JAVA_21);
			setEqualsWithToString(true);
			setShowBytecode(true);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String SEARCH = "a";
			final String REPLACE = "BB";
			Consumer<StringBuilder> replaceConsumer = buf -> buf.append(REPLACE);
			Consumer<StringBuilder> removeConsumer = buf -> {
			};
			CyclicSource<String> source = new CyclicSource<>("a", "(a)");
			StringBuilder buf = new StringBuilder(100);

			StringBuilder getStringBuilder() {
				buf.setLength(0);
				return buf;
			}

			String getInput() {
				return source.next();
			}
		}

		@Benchmark
		public Object testReplaceDirect(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			replace(buf, state.getInput(), state.SEARCH, state.REPLACE);
			return buf;
		}

		@Benchmark
		public Object testReplaceFlag(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			replaceOrRemoveFlag(buf, state.getInput(), state.SEARCH, true, state.REPLACE);
			return buf;
		}

		@Benchmark
		public Object testReplaceConsumer(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			replaceOrRemoveConsumer(buf, state.getInput(), state.SEARCH, state.replaceConsumer);
			return buf;
		}

		//@Benchmark
		public Object testRemoveDirect(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			remove(buf, state.getInput(), state.SEARCH);
			return buf;
		}

		//@Benchmark
		public Object testRemoveFlag(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			replaceOrRemoveFlag(buf, state.getInput(), state.SEARCH, false, null);
			return buf;
		}

		//@Benchmark
		public Object testRemoveConsumer(MyState state) {
			StringBuilder buf = state.getStringBuilder();
			replaceOrRemoveConsumer(buf, state.getInput(), state.SEARCH, state.removeConsumer);
			return buf;
		}

		static void replaceOrRemoveConsumer(StringBuilder buf, String str, String search, Consumer<StringBuilder> consumer) {
			int start = 0;
			int searchLen = search.length();
			while (true) {
				int index = str.indexOf(search, start);
				if (index == -1) {
					break;
				}
				buf.append(str, start, index);
				consumer.accept(buf);
				start = index + searchLen;
			}
			buf.append(str, start, str.length());
		}

		static void replaceOrRemoveFlag(StringBuilder buf, String str, String search, boolean doReplace, String replace) {
			int start = 0;
			int searchLen = search.length();
			while (true) {
				int index = str.indexOf(search, start);
				if (index == -1) {
					break;
				}
				buf.append(str, start, index);
				if (doReplace) {
					buf.append(replace);
				}
				start = index + searchLen;
			}
			buf.append(str, start, str.length());
		}

		static void replace(StringBuilder buf, String str, String search, String replace) {
			int start = 0;
			int searchLen = search.length();
			while (true) {
				int index = str.indexOf(search, start);
				if (index == -1) {
					break;
				}
				buf.append(str, start, index);
				buf.append(replace);
				start = index + searchLen;
			}
			buf.append(str, start, str.length());
		}

		static void remove(StringBuilder buf, String str, String search) {
			int start = 0;
			int searchLen = search.length();
			while (true) {
				int index = str.indexOf(search, start);
				if (index == -1) {
					break;
				}
				buf.append(str, start, index);
				start = index + searchLen;
			}
			buf.append(str, start, str.length());
		}
	}

	/** 
	 * Show that StringReplacer is about 5-10% slower for replacing string/char compared to java.lang.String
	 */
	public static class StringReplacerTestJmh extends StringJmhBenchmark {

		public StringReplacerTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
			//setJvmArgs(GapList.create(JavaTool.JvmPrintCompilation));
			//setJvmArgs(JavaTool.JvmPrintInlining);
			setJvmArgs(CollectionTools2.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining));
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final String findString = "a";
			final String replaceString = "BB";
			final StringReplacer replacerString = StringReplacer.builder().replaceString(findString, replaceString).build();

			final char findChar = 'a';
			final char replaceChar = 'B';
			final StringReplacer replacerChar = StringReplacer.builder().replaceChar(findChar, replaceChar).build();

			final CyclicSource<String> source = new CyclicSource<>("a", "b", "aba", "bab");
		}

		@Benchmark
		public Object testReplaceString_StringReplacer(MyState state) {
			String str = state.source.next();
			return state.replacerString.replace(str);
		}

		//@Benchmark
		public String testReplaceString_JavaLangString(MyState state) {
			String str = state.source.next();
			return str.replace(state.findString, state.replaceString);
		}

		//@Benchmark
		public Object testReplaceChar_StringReplacer(MyState state) {
			String str = state.source.next();
			return state.replacerChar.replace(str);
		}

		//@Benchmark
		public String testReplaceChar_JavaLangString(MyState state) {
			String str = state.source.next();
			return str.replace(state.findChar, state.replaceChar);
		}

		//@Benchmark
		public String testStringUtilsReplace(MyState state) {
			return StringUtils.replace(state.source.next(), state.findString, state.replaceString);
		}

		//@Benchmark
		public String testStringToolsReplace(MyState state) {
			return StringTools.replace(state.source.next(), state.findString, state.replaceString);
		}

	}

	public void testNew() {
		testIgnoreCase();
		testReplaceNew();
		testReplaceNewCheck();
		test2();
		test3();
		test4();
	}

	void testIgnoreCase() {
		{
			StringReplacer replacer = StringReplacer.builder().setFindChar('a').setReplaceChar('X').setIgnoreCase(true).build();
			String s = replacer.replace("(aA)");
			CheckTools.check(s.equals("(XX)"));
		}
		{
			StringReplacer replacer = StringReplacer.builder().setFindChar('a').setReplaceString("X").setIgnoreCase(true).build();
			String s = replacer.replace("(aA)");
			CheckTools.check(s.equals("(XX)"));
		}
	}

	void test4() {
		StringReplacer replacer = StringReplacer.builder().setFindChar('a').setReplaceString("AA").build();
		String s = replacer.replace("(a)");
		CheckTools.check(s.equals("(AA)"));
	}

	void test3() {
		IStringMatcher sm = StringMatcher.of("x");
		IStringReplaceAppender sr = new StringTransformReplaceAppender(s -> "(" + s + ")");
		StringReplacer replacer = StringReplacer.builder().replace(sm, sr).build();
		String s = replacer.replace("axbxc");
		CheckTools.check(s.equals("a(x)b(x)c"));
	}

	void test2() {
		String s = "abc";
		String sr = "ABc";
		{
			IReplaceStringImpl.Builder builder = StringReplacer.builder();

			StringReplacer replacer = builder.replaceString("a", "A").build();
			String s1 = replacer.replace(s);

			replacer = builder.replaceString("b", "B").build();
			String s2 = replacer.replace(s1);

			CheckTools.check(s2.equals(sr));
		}
		{
			// Note you cannot pass twice the same string as otherwise as NoChangeReplacer will be created 
			IReplaceStringImpl replacer = StringReplacer.builder().replaceString("", "1").build(IReplaceStringImpl.class);

			replacer.replaceString("a", "A");
			String s1 = replacer.replace(s);

			replacer.replaceString("b", "B");
			String s2 = replacer.replace(s1);

			CheckTools.check(s2.equals(sr));
		}
	}

	@Capture
	public void testStringTransformer() {
		IStringMatcher sm = StringMatcher.of("a");
		IStringReplaceAppender sr = new ConstStringReplaceAppender("b");
		String input = "0a1";
		String r0 = StringReplacer.builder().replace(sm, sr).build().replace(input);
		String r1 = StringReplacer.builder().replace(sm, sr).build().apply(input);
		CheckTools.check(r0.equals(r1));
	}

	@Capture
	public void testNoChange() {
		String input = "0a1";
		String r0 = StringReplacer.builder().replaceString("a", "a").setReturnMode(ReturnMode.RETURN_NULL).build().replace(input);
		CheckTools.check(r0 == null);
	}

	@Capture
	public void testEmpty() {
		String input = "abc";
		String r0 = StringReplacer.builder().replaceString("", "x").build().replace(input);
		CheckTools.check("xaxbxcx".equals(r0));
	}

	@Trace
	public void testReplaceNew() {
		// Replace 'x' with 'y'
		char c = 'x';
		int cp = c;
		String s = "" + c;

		char c2 = 'y';
		int cp2 = c2;
		String s2 = "" + c2;

		IStringMatcher sm = StringMatcher.of(s);
		IStringReplaceAppender sr = new StringTransformReplaceAppender(x -> s2);

		String input = "axb";
		new CompareExecutor().checkEqual(
				() -> StringReplacer.builder().replaceChar(c, c2).build().replace(input),
				() -> StringReplacer.builder().replaceCodePoint(cp, cp2).build().replace(input),
				() -> StringReplacer.builder().replaceString(s, s2).build().replace(input),
				() -> StringReplacer.builder().replaceAnyChar(s, s2).build().replace(input),
				() -> StringReplacer.builder().replace(sm, sr).build().replace(input));
	}

	void testReplaceNewCheck() {
		testReplaceNewCheck("abcXdef", 'X', 'Y');
		testReplaceNewCheck("abcdef", 'X', 'Y');
		testReplaceNewCheck("abcXXdef", 'X', 'Y');
	}

	void testReplaceNewCheck(String str, char findChar, char replaceChar) {
		int findCodePoint = findChar;
		int replaceCodePoint = replaceChar;
		String findString = "" + findChar;
		String replaceString = "" + replaceChar;
		String findAnyChar = findString + "0";
		String replaceAnyChar = replaceString + "1";

		IStringMatcher sm = StringMatcher.of(findString);
		IStringReplaceAppender sr = new StringTransformReplaceAppender(s -> replaceString);

		// Use single instance of StringIndexOutOfBoundsException so equals() will return true
		IllegalArgumentException e = new IllegalArgumentException();
		Function<Throwable, Throwable> changeError = t -> (t instanceof IllegalArgumentException) ? e : t;

		new CompareExecutor().checkEqualChangeError(changeError,
				() -> StringReplacer.builder().replaceChar(findChar, replaceChar).setNumReplace(1).setCheckReplace(true).build().replace(str),
				() -> StringReplacer.builder().replaceCodePoint(findCodePoint, replaceCodePoint).setNumReplace(1).setCheckReplace(true).build().replace(str),
				() -> StringReplacer.builder().replaceString(findString, replaceString).setNumReplace(1).setCheckReplace(true).build().replace(str),
				() -> StringReplacer.builder().replaceAnyChar(findAnyChar, replaceAnyChar).setNumReplace(1).setCheckReplace(true).build().replace(str),
				() -> StringReplacer.builder().replace(sm, sr).setNumReplace(1).setCheckReplace(true).build().replace(str));
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringReplacer", traceMethod = "/replace/") //TODO
	public void testReplaceWithCount() {
		StringReplacer.Builder rb = StringReplacer.builder();
		rb.setFindRegex("(\\.lambda\\$)(\\w+\\$)?(\\d+)");
		rb.setReplaceAppender(new RegexMatchReplaceAppender() {
			@Override
			public void replace(int matchIndex, IMatch match, StringBuilder buf) {
				RegexMatch rm = (RegexMatch) match;
				String str = rm.getMatchResult().group(1) + matchIndex;
				buf.append(str);
			}
		});

		StringReplacer r = rb.build();
		r.replace("c.lambda$1-c.lambda$SimpleLambda$3-c.lambda$10-c.lambda$ComplexLambda$11");
	}

	@Trace(traceClass = "org.magicwerk.brownies.core.strings.StringReplacer") // TODO
	public void testReplace() {
		// Plain string
		StringReplacer.Builder rb = StringReplacer.builder().replaceRegex("012", "345");
		StringReplacer r = rb.build();
		r.replace("abc-012-def");
		r.replace("abc-012-def-012-ghi");

		rb.setNumReplace(1);
		r = rb.build();
		String s2 = r.replace("abc-012-def-012-ghi");

		rb.setCheckReplace(true);
		r = rb.build();
		r.replace("abc-012-def-012-ghi");

		rb.setNumReplace(2);
		r = rb.build();
		r.replace("abc-012-def-012-ghi");

		// Regex
		rb = StringReplacer.builder().replaceRegex("(\\d)(\\d)(\\d)", "9{1}9");
		r = rb.build();
		r.replace("abc-012-def-012-ghi");

		// Match whole pattern with 3 groups, use formatting string
		rb.replaceRegex("(before-)(.+)(-after)", "{1}NEW{3}");
		r = rb.build();
		r.replace("-before-old-after-");

		// Match whole pattern by explicitly specifying that whole regex should be for matching even if there is only a single group,
		// also specify group to use for formatting
		rb.replaceRegex(new RegexStringMatcher().setPattern("before-(.+)-after"), new RegexMatchReplaceAppender().setFormatGroup(1, "NEW"));
		r = rb.build();
		r.replace("-before-old-after-");

		// Match only part of pattern, specify single group which will be used for matching instead of group 0
		rb.replaceRegex(new RegexStringMatcher().setPattern("before-(.+)-after").setGroup(1), new RegexMatchReplaceAppender().setFormat("NEW"));
		r = rb.build();
		r.replace("-before-old-after-");

		// Match only part of pattern, using positive lookbehind and lookahead without group, i.e. group 0 is used
		rb.replaceRegex(new RegexStringMatcher().setPattern("(?<=before-).+(?=-after)"), new RegexMatchReplaceAppender().setFormat("NEW"));
		r = rb.build();
		r.replace("-before-old-after-");

		// Match regex per line, do unlimited replacements
		rb.replaceRegex(new RegexStringMatcher().setPattern("(?m)^.*(MARK).*$").setGroup(1), new RegexMatchReplaceAppender().setFormat("SIGN"));
		r = rb.build();
		r.replace("first-line\nbefore-MARK-after\nbefore2-MARK-after2\nlast-line");

		// NestedStringMatcher
		{
			RegexStringMatcher m1 = new RegexStringMatcher().setPattern("<.*?>");
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern("\\[.*?\\]");
			RegexStringMatcher m3 = new RegexStringMatcher().setPattern("x");
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2, m3);
			rb.setFindMatcher(nsm).setReplaceString("X");
			//r = new RegexReplacer();
			//r.setMatcher(nsm);
			//r.setFormat("X");
			r.replace("a<b[dxe]c>bxc<b[dxe]c>b");
		}

		{
			StringReplacer sr = StringReplacer.build(b -> b.replaceChar(c -> true, c -> (c >= '0' && c < '9') ? (char) (c + 1) : c));
			sr.replace("abc");
			sr.replace("a0b1c");
		}
	}

	@Capture
	public void testMultiReplace() {
		// Use ConditionalOperator to apply only the first of several replacers
		ConditionalOperator<String> co = new ConditionalOperator<>();
		co.add(StringReplacer.builder().replaceRegex("\\d\\d", "2-{0}").build());
		co.add(StringReplacer.builder().replaceRegex("\\d", "1-{0}").build());
		apply(co, "0");
		apply(co, "12");
	}

	void apply(ConditionalOperator<String> co, String input) {
		String output = co.apply(input);
		LOG.info("{} -> {}", input, output);
	}

	/**
	 * Show that appending without creating a temporary string first is about 5% faster.
	 */
	public static class StringReplaceAppendTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			IStringMatcher sm = StringMatcher.of("x");

			IStringReplaceAppender sra1 = new StringTransformReplaceAppender(s -> "(" + s + ")");
			StringReplacer sr1 = StringReplacer.builder().replace(sm, sra1).build();

			IStringReplaceAppender sra2 = new StringAppendReplaceAppender((buf, s) -> buf.append("(").append(s).append(")"));
			StringReplacer sr2 = StringReplacer.builder().replace(sm, sra2).build();

			CyclicSource<String> source = new CyclicSource<>("axb", "axbxc");
		}

		@Benchmark
		public Object testReplace1(MyState state) {
			String s = state.source.next();
			return state.sr1.replace(s);
		}

		@Benchmark
		public Object testReplace2(MyState state) {
			String s = state.source.next();
			return state.sr2.replace(s);
		}
	}

	/**
	 * Show that testEscapeCharEscaperStatic() is fastest (125%) followed by testEscapeCharEscaper (100%) and testEscapeStringReplacer (75%)
	 */
	public static class StringReplaceEscapeTestJmh extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String ac = "\\[]-^ ";
			CharPredicate cp = CharPredicates.oneOf(ac);

			IStringReplaceAppender sra = new StringAppendReplaceAppender((buf, s) -> buf.append('\\').append(s));
			StringReplacer sr = StringReplacer.builder().setFindAnyChar(ac).setReplaceAppender(sra).build();

			EscapeCharEscaper ece = EscapeCharEscaper.builder().setCharsToEscape(cp)
					.setEnclosingMode(EnclosingMode.OFF).setEscapeChar('\\').build();

			CyclicSource<String> source = new CyclicSource<>("a[b", "a[b]c");
		}

		@Benchmark
		public Object testEscapeStringReplacer(MyState state) {
			String s = state.source.next();
			return state.sr.replace(s);
		}

		@Benchmark
		public Object testEscapeCharEscaperStatic(MyState state) {
			String s = state.source.next();
			return EscapeCharEscaper.escapeChars(s, '\\', state.cp);
		}

		@Benchmark
		public Object testEscapeCharEscaper(MyState state) {
			String s = state.source.next();
			return state.ece.encode(s);
		}
	}

	// Benchmarks

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringReplacerBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringReplacer}.
	 */
	public static abstract class StringReplacerBenchmarkTestBase {

		StringReplacer stringReplacer;

		@TestMethod
		public String testStringReplacer(String str) {
			return stringReplacer.replace(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static class StringReplacerBenchmarkTest extends StringReplacerBenchmarkTestBase {

		String searchChars = "abc";
		String replaceChars = "AB";

		{
			stringReplacer = StringReplacer.builder().replaceAnyChar(searchChars, replaceChars).build();
		}

		@TestData
		IList<String> inputs = GapList.create("0a1b2c3");

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.replaceChars(str, searchChars, replaceChars);
		}
	}

}
