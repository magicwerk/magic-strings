package org.magicwerk.strings;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.brownies.core.diff.ListDiff;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.javassist.analyzer.ClassDef;
import org.magicwerk.brownies.javassist.analyzer.JavaAnalyzer;
import org.magicwerk.brownies.javassist.analyzer.MethodDef;
import org.magicwerk.brownies.javassist.reflect.AnalyzerReflection;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JavaEnvironment;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestCompare;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.Key2List;
import org.magicwerk.collections.primitive.CharGapList;
import org.magicwerk.collections.primitive.ICharList;
import org.magicwerk.collections.primitive.IIntList;
import org.magicwerk.collections.primitive.IntGapList;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CodePointToolsTest;
import org.magicwerk.strings.function.MultiPredicate.Mode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.helper.CheckTools.Check;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.LinebreakMatcher;
import org.magicwerk.strings.matcher.StringsMatcher.StringsSimpleMatcher;
import org.magicwerk.strings.text.TextTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

public class JavaLangStringTest extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new JavaLangStringTest().run();
	}

	void run() {
		showStringMethods();

		//runManual();
		//test();

		//new StringSizeJmhTest().test();
	}

	//

	/**
	 * Show Integer.stringSize() is the fastest implementation possible.
	 */
	public static class StringSizeJmhTest extends StringJmhBenchmark {

		public StringSizeJmhTest() {
			setJavaVersions(JavaVersion.JAVA_25);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> ints = new CyclicSource<>(1, 10, 100, 1000);
		}

		@Benchmark
		public int testIntegerStringSize(MyState state) {
			int i = state.ints.next();
			return integerStringSize(i);
		}

		@Benchmark
		public int testStringSize(MyState state) {
			int i = state.ints.next();
			return stringSize(i);
		}

		// Code from Integer.stringSize
		static int integerStringSize(int x) {
			int d = 1;
			if (x >= 0) {
				d = 0;
				x = -x;
			}
			int p = -10;
			for (int i = 1; i < 10; i++) {
				if (x > p)
					return i + d;
				p = 10 * p;
			}
			return 10 + d;
		}

		// Simple implementation not handling negatives, but nevertheless slower (probably as no loop unrolling is done)
		static int stringSize(int x) {
			int l = 0;
			while (x > 0) {
				x = x / 10;
				l++;
			}
			return l;
		}
	}

	void showStringMethods() {
		IList<String> mnsJ8 = getStringMethods(JavaVersion.JAVA_8);
		IList<String> mnsJ11 = getStringMethods(JavaVersion.JAVA_11);
		IList<String> mnsJ17 = getStringMethods(JavaVersion.JAVA_17);
		IList<String> mnsJ21 = getStringMethods(JavaVersion.JAVA_21);
		IList<String> mnsJ25 = getStringMethods(JavaVersion.JAVA_25);

		LOG.info("java.lang.String JAVA_25:\n{}\n", StringPrinter.formatLines(mnsJ25));

		{
			ListDiff<String> ld = ListDiff.create(mnsJ25, mnsJ21);
			LOG.info("Diff Java 25-21:\n{}\n", ld.explainDiff());
		}
		{
			ListDiff<String> ld = ListDiff.create(mnsJ21, mnsJ17);
			LOG.info("Diff Java 21-17:\n{}\n", ld.explainDiff());
		}
		{
			ListDiff<String> ld = ListDiff.create(mnsJ17, mnsJ11);
			LOG.info("Diff Java 17-11:\n{}\n", ld.explainDiff());
		}
		{
			ListDiff<String> ld = ListDiff.create(mnsJ11, mnsJ8);
			LOG.info("Diff Java 11-8:\n{}\n", ld.explainDiff());
		}
	}

	IList<String> getStringMethods(JavaVersion jv) {
		JavaAnalyzer ja = new JavaAnalyzer();
		FilePath javaHome = JavaEnvironment.getJavaHome(jv);
		ja.setUseSystemClassLoader(javaHome);

		ClassDef cd = ja.analyzeClass(String.class);
		Key2List<MethodDef, String, String> mds = cd.getDeclaredMethods();
		IList<String> mns = mds.filterMap(this::isStringMethod, this::getStringMethodName);
		mns.sort(null);
		return mns;
	}

	static final AnalyzerReflection analyzerReflection = new AnalyzerReflection();

	boolean isStringMethod(MethodDef md) {
		if (!md.isPublic()) {
			return false;
		}

		ClassDef cd = analyzerReflection.getDefiningMethodClass(md);
		String cn = cd.getName();
		if (ObjectTools.isOneOf(cn, "java.lang.constant.Constable", "java.lang.constant.ConstantDesc")) {
			return false;
		}

		if (cn.equals("java.lang.CharSequence")) {
			MethodDef md2 = analyzerReflection.getDefiningMethod(md);
			if (!md2.isAbstract()) {
				// CharSequence.chars(), CharSequence().codePoints()
				return false;
			}
		}

		return true;
	}

	String getStringMethodName(MethodDef md) {
		String s = md.getReturnType().getSimpleName() + " " + md.getSimpleTypedName();
		if (md.isStatic()) {
			s = "#static " + s;
		}
		return s;
	}

	//

	void runManual() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(true);
			jbc.setRunBenchmarkFast(false);
			jbc.setRunBenchmarkShowHtml(true);
			//jbc.setRunBenchmarkShowBytecode(true);
			//jbc.setRunBenchmarkShowSource(true);

			// Analyze JIT
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation("hotspot.log")));

			// Analyze Allocation: JmhAllocationJfrObserverState needs at least Java 16
			//jbc.setBenchmarkStateClass(JmhAllocationJfrObserverState.class);

			//jbc.setRunTime(1000);
			//jbc.setJavaVersions(JavaVersion.JAVA_17);
			//jbc.setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		});

		test(SplitLines_All_Test.class);
		test(SplitLines_First_Test.class);
	}

	void test() {
		//testIndexEmptyJdk();
		//		testSurrogateJdk();
		//		testSurrogateIgnoreCaseJdk();
		//		testValidCharsJdk();
		//testUpperLowerCaseChar1Jdk();
		testUpperLowerCaseJdk();
	}

	void testUpperLowerCaseJdk() {
		//	testUpperLowerCaseCodePointJdk();
		//		testUpperLowerCaseCodePointJdk21();
		testUpperLowerCaseCompareJdk_Slow();
		//testTitleCaseJdk();
		//testUpperLowerCaseChar1Jdk();
		//		testUpperLowerCaseChar2Jdk();
		//testUpperLowerCaseChar3Jdk();
		//testUpperLowerCaseChar4Jdk();
		//		//testUpperLowerCaseCompareJdk_Slow();
		//		testUpperLowerCaseCharPredicateJdk_Slow();
	}

	/** Execute testUpperLowerCaseCodePointJdk with Java 17 */
	void testUpperLowerCaseCodePointJdk21() {
		JavaTool jt = BrowniesJavaEnv.createJavaTool(JavaVersion.JAVA_17);
		jt.setPrintOutput(true);
		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(jt);
		jr.setMainMethod(JavaLangStringTest.class, "testUpperLowerCaseCodePointJdk");
		jr.run();
	}

	/** 
	 * Test upper/lower case of code points. Correct since Java 16:
	 * Support supplementary characters in String case insensitive operations
	 * https://bugs.openjdk.org/browse/JDK-8248664
	 */
	void testUpperLowerCaseCodePointJdk() {
		LOG.info("testUpperLowerCaseCodePointJdk (Java {})", JavaEnvironment.getSystemJavaVersion());
		int numHi = Character.MAX_HIGH_SURROGATE - Character.MIN_HIGH_SURROGATE + 1;
		int numLo = Character.MAX_LOW_SURROGATE - Character.MIN_LOW_SURROGATE + 1;
		CheckTools.check(numHi == 1024 && numLo == 1024);
		for (int i = Character.MIN_HIGH_SURROGATE; i <= Character.MAX_LOW_SURROGATE; i++) {
			char c = (char) i;
			CheckTools.check(Character.getType(c) == Character.SURROGATE);
			CheckTools.check(Character.isUpperCase(c) == false);
			CheckTools.check(Character.isLowerCase(c) == false);
			CheckTools.check(Character.toUpperCase(c) == c);
			CheckTools.check(Character.toLowerCase(c) == c);
		}

		int numSurrogates = Character.MAX_CODE_POINT - Character.MIN_SUPPLEMENTARY_CODE_POINT + 1;
		CheckTools.check(numSurrogates == 1024 * 1024);

		for (int cp = Character.MIN_SUPPLEMENTARY_CODE_POINT; cp <= Character.MAX_CODE_POINT; cp++) {
			if (Character.isUpperCase(cp)) {
				handleCodePoint(cp, Character.toLowerCase(cp));
			} else if (Character.isLowerCase(cp)) {
				handleCodePoint(Character.toUpperCase(cp), cp);
			}
		}
	}

	void handleCodePoint(int uc, int lc) {
		String suc = Character.toString(uc);
		String slc = Character.toString(lc);
		CheckTools.check(suc.equalsIgnoreCase(slc));
	}

	public static class SplitLines_TestBase {
		String regex = "\\R";
		Pattern pattern = Pattern.compile(regex);

		StringSplitter splitter = StringSplitter.build(b -> b.setFindMatcher(LinebreakMatcher.LINEBREAK_JAVA_MATCHER));
		IStringMatcher matcher = new StringsSimpleMatcher(LinebreakMatcher.LINEBREAKS_JAVA);
		StringSplitter splitter2 = StringSplitter.build(b -> b.setFindMatcher(matcher));

		@TestData
		IList<String> inputs = GapList.create("abc", "abc\ndef", "abc\ndef\nghi");

		@TestCompare
		Object compareResult(Object result) {
			if (result instanceof String[]) {
				return GapList.create((String[]) result);
			} else {
				return result;
			}
		}
	}

	public static class SplitLines_All_Test extends SplitLines_TestBase {

		@TestMethod
		public List<String> testMagicStrings(String str) {
			return splitter.split(str);
		}

		@TestMethod
		public List<String> testMagicStrings2(String str) {
			return splitter2.split(str);
		}

		@TestMethod
		public List<String> testTextTools(String str) {
			return TextTools.splitLines(str);
		}

		@TestMethod
		public List<String> testStringLines(String str) {
			return str.lines().collect(Collectors.toList());
		}

		@TestMethod
		public String[] testStringSplit(String str) {
			return str.split(regex);
		}
	}

	public static class SplitLines_First_Test extends SplitLines_TestBase {

		@TestMethod
		public String testMagicStrings(String str) {
			return splitter.getFirst(str);
		}

		@TestMethod
		public String testMagicStrings2(String str) {
			return splitter2.getFirst(str);
		}

		@TestMethod
		public String testTextTools(String str) {
			return TextTools.getLine(str, 0, false);
		}

		@TestMethod
		public String testStringLines(String str) {
			return str.lines().findFirst().get();
		}

		@TestMethod
		public String testStringSplit(String str) {
			return str.split(regex)[0];
		}
	}

	/** Show how JDK handles searching for empty strings */
	void testIndexEmptyJdk() {
		String str = "ab";
		CheckTools.check(str.indexOf("") == 0);
		CheckTools.check(str.indexOf("", 1) == 1);
		CheckTools.check(str.indexOf("", 2) == 2);

		CheckTools.check(str.indexOf("", -1) == 0);
		CheckTools.check(str.indexOf("", 3) == 2);

		String s0 = str.replace("", "-");
		CheckTools.check("-a-b-".equals(s0));
	}

	/** Check how JDK handles string indexes */
	void testIndexJdk() {
		String str = "abc";

		// Handling on substring() is strict
		str.substring(0);
		str.substring(3);
		new RunnableExecutor().setLogException().setMustFail().run(() -> str.substring(-1));
		new RunnableExecutor().setLogException().setMustFail().run(() -> str.substring(4));

		// Handling on indexOf()/lastIndexOf() is lenient
		str.indexOf('b', 0);
		str.indexOf('b', 3);
		CheckTools.check(str.indexOf('b', -1) == 1);
		str.indexOf('b', 4);

		str.lastIndexOf('b', 0);
		str.lastIndexOf('b', 3);
		str.lastIndexOf('b', -1);
		str.lastIndexOf('b', 4);
	}

	/** 
	 * Show how reverse search with String.lastIndex works:
	 * If the searched string starts at the specified position, this position is returned.
	 * The passed start position should therefore be source.length() - find.length()
	 * After a match, the next start position should be index-find.length() to avoid overlapping matches. 
	 */
	void testFindReverseJdk() {
		{
			String source = "xxx";
			String find = "x";
			for (int i = source.length(); i >= 0; i--) {
				int pos = source.lastIndexOf(find, i);
				LOG.info("{}: {}", i, pos);
			}
		}
		LOG.info("");
		{
			String source = "abab";
			String find = "ab";
			for (int i = source.length(); i >= 0; i--) {
				int pos = source.lastIndexOf(find, i);
				LOG.info("{}: {}", i, pos);
			}
		}
	}

	/**
	 * Show that for all character only one of lowerCase, upperCase, titleCase is true. 
	 * (but there are of course a lot of characters which are neither, e.g. digits etc)
	 */
	void testUpperLowerCaseChar1Jdk() {
		char c = StringTools.NOT_A_CHAR;
		CheckTools.check(Character.getType(c) == Character.UNASSIGNED);

		int numC1 = 0;
		for (int i = 0; i <= Character.MAX_VALUE; i++) {
			if (Character.getType(i) != Character.UNASSIGNED) {
				numC1++;
			}
		}
		int numC2 = 0;
		for (int i = Character.MIN_SUPPLEMENTARY_CODE_POINT; i <= Character.MAX_CODE_POINT; i++) {
			if (Character.getType(i) != Character.UNASSIGNED) {
				numC2++;
			}
		}
		LOG.info("numC1: {} / {}, numC2: {} / {}",
				numC1, Character.MAX_VALUE, numC2, Character.MAX_CODE_POINT - Character.MIN_SUPPLEMENTARY_CODE_POINT);

		int numLc = 0;
		int numUc = 0;
		int numTc = 0;
		for (int i = 0; i <= Character.MAX_CODE_POINT; i++) {
			boolean lc = Character.isLowerCase(i);
			boolean uc = Character.isUpperCase(i);
			boolean tc = Character.isTitleCase(i);
			Check.forTrue(Mode.ONE_OR_NONE).check(lc, uc, tc);
			numLc += (lc) ? 1 : 0;
			numUc += (uc) ? 1 : 0;
			numTc += (tc) ? 1 : 0;
		}
		LOG.info("numLc: {}, numUc: {}, numTc: {}", numLc, numUc, numTc);
	}

	/** 
	 * Test that lower and upper case characters are always either both 1 char or 2 chars.
	 * This means that if we test for equality we can rely on the length also if case insensitive.
	 */
	void testUpperLowerCaseChar2Jdk() {
		for (int i = 0; i <= Character.MAX_CODE_POINT; i++) {
			int lc = Character.toLowerCase(i);
			int uc = Character.toUpperCase(i);
			boolean bi = CodePointTools.isCharCodePoint(i);
			boolean blc = CodePointTools.isCharCodePoint(lc);
			boolean buc = CodePointTools.isCharCodePoint(uc);
			CheckTools.check(blc == bi && buc == bi);
		}
	}

	/** 
	 * Show that there are Unicode characters where both lower and upper case variants are distinct, e.g. 
	 * the Unicode character U+01C5, "ǅ", is a Latin capital letter D with small letter Z with caron, which is a title case character.
	 * - 4 violations 
	 */
	void testUpperLowerCaseChar3Jdk() {
		ICharList violations = CharGapList.create();
		for (int i = 0; i <= Character.MAX_VALUE; i++) {
			char c = (char) i;
			char lc = Character.toLowerCase(c);
			char uc = Character.toUpperCase(c);
			boolean tc = Character.isTitleCase(c);
			boolean valid = (c == lc || c == uc);
			if (!valid) {
				violations.add(c);
				LOG.info("tc: {}", tc);
			} else {
				if (tc) {
					// Show that there are title case characters where toUpperCase() return the same character
					// but isUpperCase() will return false
					LOG.info("TC: {} {} {} {} {}", print(c), print(lc), print(uc), Character.isLowerCase(lc), Character.isUpperCase(uc));
				}
			}
		}
		LOG.info("{} violations:", violations.size());
		for (int i = 0; i < violations.size(); i++) {
			char c = violations.get(i);
			LOG.info("- {} ({})", c, Integer.toHexString(c));
		}
	}

	String print(char c) {
		return c + " (" + CharTools.toUnicodeNumber(c) + ")";
	}

	void testUpperLowerCaseChar4Jdk() {
		Set<Integer> types = new TreeSet<>();
		for (int i = 0; i <= Character.MAX_VALUE; i++) {
			char c = (char) i;
			char lc = Character.toLowerCase(c);
			char uc = Character.toUpperCase(c);
			if (lc != c || uc != c) {
				types.add(Character.getType(c));
			}
		}

		// Show types which can have upper/lowercase
		LOG.info("{}", types);
		// [1, 2, 3, 6, 10, 28]
		// - Constants in java.lang.Character:
		// public static final byte UPPERCASE_LETTER = 1;
		// public static final byte LOWERCASE_LETTER = 2;
		// public static final byte TITLECASE_LETTER = 3;
		// public static final byte NON_SPACING_MARK = 6;
		// public static final byte LETTER_NUMBER = 10;
		// public static final byte OTHER_SYMBOL = 28;
	}

	void testTitleCaseJdk() {
		char c = 453;
		CheckTools.check(Character.isTitleCase(c));
		char lc = Character.toLowerCase(c);
		char uc = Character.toUpperCase(c);
		String sc = String.valueOf(c);
		String slc = String.valueOf(lc);
		String suc = String.valueOf(uc);
		LOG.info("{} - {} - {}", sc, slc, suc);
		LOG.info("{} - {}", sc.equalsIgnoreCase(slc), sc.equalsIgnoreCase(suc));
	}

	void showChars(String title, ICharList chars) {
		LOG.info("{} {}:", chars.size(), title);
		for (int i = 0; i < chars.size(); i++) {
			char c = chars.get(i);
			LOG.info("- {} ({})", c, CharTools.toUnicodeChar(c));
		}
	}

	/**
	 * Show that using either only toLowerCase() or toUpperCase() cannot fully cover the combination of toUpperCase()/toLowerCase().
	 * - 4 violations
	 */
	void testUpperLowerCaseCompareJdk_Slow() {
		IIntList violations = IntGapList.create();
		for (int i = 0; i <= Character.MAX_CODE_POINT; i++) {
			if (i % 1000 == 0) {
				LOG.info("... {}", i);
			}
			int c = i;
			int uc = Character.toUpperCase(c);
			int lc = Character.toLowerCase(c);
			if (uc == c && lc == c) {
				continue;
			}

			IIntList cs1 = IntGapList.create();
			for (int i1 = 0; i1 <= Character.MAX_CODE_POINT; i1++) {
				int c1 = i1;
				boolean eq = CharCaseTools.isEqualCodePointCI(c1, c);
				if (eq) {
					cs1.add(c1);
				}
			}

			IIntList cs2 = IntGapList.create();
			for (int i2 = 0; i2 <= Character.MAX_CODE_POINT; i2++) {
				int c2 = i2;
				int uc2 = Character.toUpperCase(c2);
				//int lc2 = Character.toLowerCase(c2);
				boolean eq2 = c2 == c || uc2 == uc;
				if (eq2) {
					cs2.add(c2);
				}
			}

			boolean valid = cs2.equals(cs1);
			if (!valid) {
				violations.add(c);
			}
		}
		LOG.info("{} violations:", violations.size());
		for (int i = 0; i < violations.size(); i++) {
			int c = violations.get(i);
			LOG.info("- {} ({})", Character.toString(c), Integer.toHexString(c));
		}
	}

	// There are 18 characters which needs toLowerCase(toUpperCase(c)) instead of simpler toUpperCase(c).
	// Using upper case is better than lower case (with 67 characters).
	static final String CHARS_NEED_UC_LC_CASE = "IKikÅåİıΘΩθωϑϴẞΩKÅ";

	/**
	 * Show that CharPredicate can only partly handle ignore case option.
	 * - 59 violations without title case, 55 with title case
	 */
	void testUpperLowerCaseCharPredicateJdk_Slow() {
		ICharList violations = CharGapList.create();
		for (int i = 0; i <= Character.MAX_VALUE; i++) {
			if (i % 1000 == 0) {
				LOG.info("... {}", i);
			}
			char c = (char) i;
			String s = String.valueOf(c);
			CharPredicate cp = x -> x == c;

			ICharList cs1 = CharGapList.create();
			for (int i1 = 0; i1 <= Character.MAX_VALUE; i1++) {
				char c1 = (char) i1;
				String s1 = String.valueOf(c1);
				boolean eq = CharCaseTools.isEqualCodePointCI(c1, c);
				boolean eq2 = s1.equalsIgnoreCase(s);
				CheckTools.check(eq2 == eq);
				if (eq) {
					cs1.add(c1);
				}
			}

			ICharList cs2 = CharGapList.create();
			for (int i2 = 0; i2 <= Character.MAX_VALUE; i2++) {
				char c2 = (char) i2;
				char lc2 = Character.toLowerCase(c2);
				char uc2 = Character.toUpperCase(c2);
				char tc2 = Character.toTitleCase(c2);
				if (cp.test(c2) || cp.test(lc2) || cp.test(uc2) || cp.test(tc2)) {
					cs2.add(c2);
				}
			}

			boolean valid = cs2.equals(cs1);
			if (!valid) {
				violations.add(c);
			}
		}
		LOG.info("{} violations:", violations.size());
		for (int i = 0; i < violations.size(); i++) {
			char c = violations.get(i);
			LOG.info("- {} ({})", c, Integer.toHexString(c));
		}
	}

	public void testSurrogateIgnoreCaseJdk() {
		int lc = 0;
		int uc = 0;
		for (int i = Character.MIN_SUPPLEMENTARY_CODE_POINT; i <= Character.MAX_CODE_POINT + 1; i++) {
			lc = 0;
			uc = 0;
			if (Character.isUpperCase(i)) {
				uc = i;
				lc = Character.toLowerCase(i);
			} else if (Character.isLowerCase(i)) {
				lc = i;
				uc = Character.toUpperCase(i);
			}
			if (lc != 0 && uc != 0) {
				break;
			}
		}
		LOG.info("lower {} ({}), upper {} ({})", Character.toString(lc), Integer.toHexString(lc), Character.toString(uc), Integer.toHexString(uc));

		// Java versions before 16 did handle surrogates correctly in case insensitive comparing, this has been fixed with Java 16
		// Support supplementary characters in String case insensitive operations
		// https://bugs.openjdk.org/browse/JDK-8248664

		String slc = Character.toString(lc);
		String suc = Character.toString(uc);
		LOG.info("{} - {}", slc.equals(suc), slc.equalsIgnoreCase(suc));

	}

	public void testValidCharsJdk() {
		RunnableExecutor re = new RunnableExecutor();
		for (int i = -1; i <= Character.MAX_CODE_POINT + 1; i++) {
			final int cp = i;
			boolean validChar = re.callBool(() -> Character.toChars(cp));
			boolean validType = (Character.getType(i) != 0);
			if (validChar) {
				//if (validChar && validType) {
				continue;
			}

			LOG.info("{} {}: validChar= {}, validType= {}", i, Integer.toHexString(i), validChar, validType);
		}

		// Characters.toChars() fails for an invalid code point, however Character.toCodePoint() works
		int i = CodePointToolsTest.INVALID_CODEPOINT;
		re.setMustFail().run(() -> Character.toChars(i));
		char hi = Character.highSurrogate(i);
		char lo = Character.lowSurrogate(i);
		boolean isHi = Character.isHighSurrogate(hi);
		boolean isLo = Character.isLowSurrogate(lo);
		LOG.info("isHi {}, isLo {}", isHi, isLo);
		int cp = Character.toCodePoint(hi, lo);
		CheckTools.check(cp == i);
	}

	public void testSurrogateJdk() {
		int i0 = Character.MIN_SUPPLEMENTARY_CODE_POINT;
		int i1 = Character.MAX_CODE_POINT;
		int max = i1 - i0;
		LOG.info("max surrogates: {}", max);
		int num = 0;
		for (int i = i0; i < i1; i++) {
			char c0 = Character.highSurrogate(i);
			char c1 = Character.lowSurrogate(i);
			if (Character.isHighSurrogate(c0) && Character.isLowSurrogate(c1)) {
				int t = Character.getType(i);
				if (t != 0) {
					LOG.info("{} {}", Integer.toHexString(i), i);
					num++;
				}
			}
		}
		LOG.info("num surrogates: {}", num);
	}

}
