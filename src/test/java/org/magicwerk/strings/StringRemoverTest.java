package org.magicwerk.strings;

import java.util.function.Function;
import java.util.function.IntPredicate;

import org.apache.commons.lang3.StringUtils;
import org.magictest.client.Capture;
import org.magictest.client.InheritTrace;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.concurrent.CompareExecutor;
import org.magicwerk.brownies.core.regex.RegexBuilder;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.IStringTransformer;
import org.magicwerk.strings.MultiStringReplacer;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.StringFinder;
import org.magicwerk.strings.StringRemover;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CodePointPredicates;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.MultiStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test of class {@link StringRemover}.
 */
public class StringRemoverTest extends StringBenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringRemoverTest().run();
	}

	void run() {
		//testGeneric();
		//testRetain();
		testRemove();
		//testRemoveRetain();
		//testRemoveReplace();
		//testNoChange();
		//testStringTransformer();

		//testBenchmarks();
		//runBenchmarks();
	}

	//@Capture
	public void testGeneric() {
		// FIXME applyInline not yet implemented
		IStringTransformerBuilder builder = StringRemover.builder().setFindChar('-');
		IList<String> inputs = GapList.create("123", "-123-");
		testStringTranformerGeneric(builder, inputs);
	}

	@Capture
	public void testRemoveRetain() {
		testRemoveRetain("a0b1c2d", "012");
	}

	/** Test that remove / retain / find work consistently */
	void testRemoveRetain(String input, String findAnyChar) {
		String remove = StringRemover.builder().setFindAnyChar(findAnyChar).build().remove(input);
		String retain = StringRemover.builder().setFindAnyChar(findAnyChar).setRetain(true).build().remove(input);
		IList<IMatch> ms = StringFinder.builder().setFindAnyChar(findAnyChar).build().matches(input);

		StringBuilder remove2 = new StringBuilder();
		StringBuilder retain2 = new StringBuilder();
		int pos = 0;
		for (IMatch m : ms) {
			remove2.append(input, pos, m.getStart());
			retain2.append(input, m.getStart(), m.getEnd());
			pos = m.getEnd();
		}
		remove2.append(input, pos, input.length());
		CheckTools.check(remove.equals(remove2.toString()));
		CheckTools.check(retain.equals(retain2.toString()));
	}

	@Capture
	public void testRemoveReplace() {
		testRemoveReplace("a0b1c2d", "012");
	}

	/** Test that remove / replace with empty string work consistently */
	void testRemoveReplace(String input, String findAnyChar) {
		String remove = StringRemover.builder().setFindAnyChar(findAnyChar).build().remove(input);
		String replace = StringReplacer.builder().setFindAnyChar(findAnyChar).setReplaceString("").build().replace(input);

		CheckTools.check(remove.equals(replace.toString()));
	}

	@Capture
	public void testRetain() {
		String input = "a0b0c";
		String r0 = StringRemover.builder().setFindChar('0').build().remove(input);
		String r1 = StringRemover.builder().setFindChar('0').setRetain(true).build().remove(input);
		LOG.info("{}: {} - {}", input, r0, r1);
	}

	@Capture
	public void testStringTransformer() {
		IStringMatcher sm = StringMatcher.of("a");
		String input = "0a1";
		String r0 = StringRemover.builder().setFindMatcher(sm).build().remove(input);
		String r1 = StringRemover.builder().setFindMatcher(sm).build().apply(input);
		CheckTools.check(r0.equals(r1));
	}

	@Capture
	public void testNoChange() {
		String input = "0a1";
		String r = StringRemover.builder().setFindString("x").setReturnMode(ReturnMode.RETURN_NULL).build().remove(input);
		CheckTools.check(r == null);
		r = StringRemover.builder().setFindString("xx").setReturnMode(ReturnMode.RETURN_NULL).build().remove(input);
		CheckTools.check(r == null);
		r = StringRemover.builder().setFindString("").setReturnMode(ReturnMode.RETURN_NULL).build().remove(input);
		CheckTools.check(r == null);
	}

	@Trace(traceMethod = "apply")
	public void testRemoveMultiple() {
		StringMatcher m1 = StringMatcher.of("aa");
		StringMatcher m2 = StringMatcher.of("aaa");
		MultiStringMatcher msm = new MultiStringMatcher(m1, m2).setPreferLong(true);
		StringRemover sr = StringRemover.builder().setFindMatcher(msm).build();

		StringReplacer r1 = StringReplacer.builder().replaceString("aa", "").build();
		StringReplacer r2 = StringReplacer.builder().replaceString("aaa", "").build();
		MultiStringReplacer smr = MultiStringReplacer.builder().setReplacers(r1, r2).build();

		for (IStringTransformer st : GapList.immutable(sr, smr)) {
			st.apply("[aa]");
			st.apply("[aaa]");
			st.apply("[aaaa]");
		}
	}

	@Trace
	public void testRemove() {
		testRemoveChar("abcXdef", 'X');
		testRemoveChar("abcdef", 'X');
		testRemoveChar("abcXXdef", 'X');

		int cp = CodePointToolsTest.CODE_POINT_SURROGATE_0;
		String str = CodePointTools.codePointToString(cp);
		testRemoveCodePoint("abc" + str + "def", cp);
		testRemoveCodePoint("abc" + "def", cp);
		testRemoveCodePoint("abc" + str + str + "def", cp);
	}

	@InheritTrace
	void testRemoveChar(String str, char c) {
		doTestRemoveChar(str, c);
		doTestRemoveCharChecked(str, c);
	}

	@InheritTrace
	void testRemoveCodePoint(String str, int cp) {
		doTestRemoveCodePoint(str, cp);
		doTestRemoveCodePointChecked(str, cp);
	}

	static class FindStringChar {
		char c;
		int cp;
		String str;
		String anyChar;
		CharPredicate charPredicate;
		IntPredicate codePointPredicate;
		String regex;
		IStringMatcher stringMatcher;

		FindStringChar(char c) {
			this.c = c;

			cp = c;
			str = "" + c;
			anyChar = str + "0";
			charPredicate = CharPredicates.equals(c);
			codePointPredicate = CodePointPredicates.of(c);
			regex = RegexBuilder.regexForLiteral(str);
			stringMatcher = StringMatcher.of(str);
		}
	}

	static class FindStringCodePoint {
		int cp;
		String str;
		String anyChar;
		IntPredicate codePointPredicate;
		String regex;
		IStringMatcher stringMatcher;

		FindStringCodePoint(int cp) {
			this.cp = cp;

			str = CodePointTools.codePointToString(cp);
			anyChar = str + "0";
			codePointPredicate = CodePointPredicates.of(cp);
			regex = RegexBuilder.regexForLiteral(str);
			stringMatcher = StringMatcher.of(str);
		}
	}

	@InheritTrace
	void doTestRemoveChar(String input, char findChar) {
		// Call remove() for @Trace
		getBuilder().setFindChar(findChar).build().remove(input);

		// Calls in lambdas are not traced
		FindStringChar find = new FindStringChar(findChar);
		new CompareExecutor().checkEqual(
				() -> getBuilder().setFindChar(find.c).build().remove(input),
				// code point cannot be tested as the optimized variant only works if charCount is 2
				//() -> getBuilder().setFindCodePoint(find.cp).build().remove(input)
				() -> getBuilder().setFindString(find.str).build().remove(input),
				() -> getBuilder().setFindAnyChar(find.anyChar).build().remove(input),
				() -> getBuilder().setFindCharPredicate(find.charPredicate).build().remove(input),
				() -> getBuilder().setFindCodePointPredicate(find.codePointPredicate).build().remove(input),
				() -> getBuilder().setFindRegex(find.regex).build().remove(input),
				() -> getBuilder().setFindMatcher(find.stringMatcher).build().remove(input));
	}

	@InheritTrace
	void doTestRemoveCodePoint(String input, int cp) {
		// Call remove() for @Trace
		getBuilder().setFindCodePoint(cp).build().remove(input);

		// Calls in lambdas are not traced
		FindStringCodePoint find = new FindStringCodePoint(cp);
		new CompareExecutor().checkEqual(
				() -> getBuilder().setFindCodePoint(find.cp).build().remove(input),
				() -> getBuilder().setFindString(find.str).build().remove(input),
				() -> getBuilder().setFindAnyChar(find.anyChar).build().remove(input),
				() -> getBuilder().setFindCodePointPredicate(find.codePointPredicate).build().remove(input),
				() -> getBuilder().setFindRegex(find.regex).build().remove(input),
				() -> getBuilder().setFindMatcher(find.stringMatcher).build().remove(input));
	}

	@InheritTrace
	void doTestRemoveCharChecked(String str, char findChar) {
		// Call remove() for @Trace
		getBuilder().setFindChar(findChar).setNumReplace(1).setCheckReplace(true).build().remove(str);

		// Calls in lambdas are not traced
		FindStringChar find = new FindStringChar(findChar);

		// Use single instance of StringIndexOutOfBoundsException so equals() will return true
		IllegalArgumentException e = new IllegalArgumentException();
		Function<Throwable, Throwable> changeError = t -> (t instanceof IllegalArgumentException) ? e : t;

		new CompareExecutor().checkEqualChangeError(changeError,
				() -> getBuilder().setFindChar(find.c).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindCodePoint(find.cp).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindString(find.str).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindAnyChar(find.anyChar).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindCharPredicate(find.charPredicate).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindCodePointPredicate(find.codePointPredicate).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindRegex(find.regex).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindMatcher(find.stringMatcher).setNumReplace(1).setCheckReplace(true).build().remove(str));
	}

	@InheritTrace
	void doTestRemoveCodePointChecked(String str, int cp) {
		// Call remove() for @Trace
		getBuilder().setFindCodePoint(cp).setNumReplace(1).setCheckReplace(true).build().remove(str);

		// Calls in lambdas are not traced
		FindStringCodePoint find = new FindStringCodePoint(cp);

		// Use single instance of StringIndexOutOfBoundsException so equals() will return true
		IllegalArgumentException e = new IllegalArgumentException();
		Function<Throwable, Throwable> changeError = t -> (t instanceof IllegalArgumentException) ? e : t;

		new CompareExecutor().checkEqualChangeError(changeError,
				() -> getBuilder().setFindCodePoint(find.cp).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindString(find.str).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindAnyChar(find.anyChar).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindCodePointPredicate(find.codePointPredicate).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindRegex(find.regex).setNumReplace(1).setCheckReplace(true).build().remove(str),
				() -> getBuilder().setFindMatcher(find.stringMatcher).setNumReplace(1).setCheckReplace(true).build().remove(str));
	}

	StringRemover.Builder getBuilder() {
		return new MyBuilder();
	}

	/** 
	 * Specialized Builder which prevents the optimization of <br> 
	 * - string to codePoint or char
	 * - codePoint to char
	 * - anyChar to codePoint or char
	 * - regex to string
	 */
	static class MyBuilder extends StringRemover.Builder {

		@Override
		public void setString(CharSequence str) {
			super.doSetString(str);
		}

		@Override
		public void setCodePoint(int codePoint) {
			super.doSetCodePoint(codePoint);
		}

		@Override
		public void setAnyChar(String anyChar) {
			super.doSetAnyChar(anyChar);
		}

		@Override
		public void setRegex(String regex) {
			super.doSetRegex(regex);
		}
	}

	//

	@Override
	public IList<Class<?>> getTestClasses() {
		return GapList.create(StringRemoverRemoveCharFoundBenchmarkTest.class, StringRemoverRemoveCharNotFoundBenchmarkTest.class,
				StringRemoverRemoveStringFoundBenchmarkTest.class, StringRemoverRemoveStringNotFoundBenchmarkTest.class);
	}

	/**
	 * Base class for tests/benchmarks of {@link StringRemover}.
	 */
	public static abstract class StringRemoverBenchmarkTestBase {

		StringRemover stringRemover;

		@TestMethod
		public String testStringRemover(String str) {
			return stringRemover.remove(str);
		}

		@TestMethod
		public abstract String testCommonsStringUtils(String str);
	}

	public static abstract class StringRemoverRemoveCharBenchmarkTestBase extends StringRemoverBenchmarkTestBase {

		char removeChar;

		@TestData
		IList<String> inputs = GapList.create("a0b0c");

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.remove(str, removeChar);
		}
	}

	public static class StringRemoverRemoveCharFoundBenchmarkTest extends StringRemoverRemoveCharBenchmarkTestBase {
		{
			removeChar = '0';
			stringRemover = StringRemover.builder().setFindChar(removeChar).setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
		}
	}

	public static class StringRemoverRemoveCharNotFoundBenchmarkTest extends StringRemoverRemoveCharBenchmarkTestBase {
		{
			removeChar = 'x';
			stringRemover = StringRemover.builder().setFindChar(removeChar).setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
		}
	}

	public static abstract class StringRemoverRemoveStringBenchmarkTestBase extends StringRemoverBenchmarkTestBase {

		String removeString;

		@TestData
		IList<String> inputs = GapList.create("a00b00c");

		@Override
		@TestMethod
		public String testCommonsStringUtils(String str) {
			return StringUtils.remove(str, removeString);
		}
	}

	public static class StringRemoverRemoveStringFoundBenchmarkTest extends StringRemoverRemoveStringBenchmarkTestBase {
		{
			removeString = "00";
			stringRemover = StringRemover.builder().setFindString(removeString).setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
		}
	}

	public static class StringRemoverRemoveStringNotFoundBenchmarkTest extends StringRemoverRemoveStringBenchmarkTestBase {
		{
			removeString = "xx";
			stringRemover = StringRemover.builder().setFindString(removeString).setReturnMode(ReturnMode.RETURN_UNCHANGED).build();
		}
	}

}
