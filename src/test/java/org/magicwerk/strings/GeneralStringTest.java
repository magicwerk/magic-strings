package org.magicwerk.strings;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.collections.Tree;
import org.magicwerk.brownies.core.reflect.JavaConst;
import org.magicwerk.brownies.files.FilePath;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.javassist.analyzer.ApplicationDef;
import org.magicwerk.brownies.javassist.analyzer.ClassDef;
import org.magicwerk.brownies.javassist.analyzer.JavaAnalyzer;
import org.magicwerk.brownies.javassist.analyzer.tools.JavaAnalyzerTools;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmark;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.ICollectionTools;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.Key1Set;
import org.magicwerk.brownies.tools.dev.jvm.JmhState;
import org.magicwerk.strings.BuilderHelper.BuilderStringBase;
import org.magicwerk.strings.StringTrimmer.TrimMode;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CharToolsTest;
import org.magicwerk.strings.helper.FuncTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import com.google.common.base.CharMatcher;

/**
 * Test class for general string tests.
 */
public class GeneralStringTest extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	/**
	 * Base test class with common functionality.
	 */
	public static class StringJmhBenchmark extends JmhBenchmark {
		// TODO
		//			// Check memory usage:
		//			// - add "JmhAllocationObserverState s" as parameter to benchmark
		//			// - reduce runtime to avoid GC: opts.setRunTimeMillis(50)
		//			// - comment out: // runner.verifyJmhMethods(opts, 10);
	}

	//

	public static void main(String[] args) {
		new GeneralStringTest().run();
	}

	void run() {
		analyzeStringBuilderClasses();
		//analyzeIStringMatcherClasses();

		//runManual();
		//new CharPredicateJmhTest().test();
		//new FieldAccessJmhTest().test();
		//new DynamicLambdaJmhTest().test();
		//new StringSubstringJmhTest().test();
		//new InlineApplierJmhTest().test();
		//new AllocWithoutGcTestJmh().test();
		//		new InlineDelegateJmhTest().test();
		//new EscapeAnalysisPointJmhTest().test();
		//new FormatIntoBufferJmhTest().test();
		//new StringBuilderAppendSubstringJmhTest().test();
		//new StringIndexOfJmhTest().test();
	}

	void analyzeStringBuilderClasses() {
		JavaAnalyzer ja = new JavaAnalyzer();
		ja.setUseCurrentClassPath(); // to access IStringMatcher
		ja.setSupportModules(true); // to have file paths ready
		ClassDef cd = ja.analyzeClass(BuilderStringBase.class);
		FilePath dir = cd.getFullPath().getParent();
		ApplicationDef app = ja.analyzeClassDirectory(dir);
		//IList<ClassDef> cds = JavaAnalyzerTools.getSubTypes(cd, true);
		Key1Set<ClassDef, ?> cs1 = app.getClassDefs().filter(c -> c.getSimpleName().equals("Builder"));
		IList<ClassDef> cs2 = cs1.flatMap(c -> c.getAllTypes());
		Set<ClassDef> cds = new HashSet<>(cs2);
		cds.removeIf(c -> JavaConst.CLASS_Object.equals(c.getName()));

		StringPrinter buf = new StringPrinter();
		buf.println("@startuml");
		for (ClassDef c : cds) {
			buf.println(formatUml(c));
		}
		buf.println("@enduml");
		String uml = buf.toString();
		LOG.info("{}", uml);
	}

	String formatUml(ClassDef cd) {
		String cn = getName(cd);
		String extend = getExtends(cd);
		String implement = getImplements(cd);

		String type = cd.getClassType().toString();
		String name;
		if ("Builder".equals(cd.getSimpleName())) {
			name = "**" + getName(cd) + "**";
		} else {
			name = cn;
		}

		StringPrinter buf = new StringPrinter().setElemMarker(" ");
		buf.add(type);
		buf.add(name);
		buf.addPartsIf("extends ", extend);
		buf.addPartsIf("implements ", implement);
		return buf.toString();
	}

	String getName(ClassDef cd) {
		String cn = cd.getSimpleName();
		if (ObjectTools.isOneOf(cn, "Builder", "BuilderImpl")) {
			return cd.getEnclosingClass().getSimpleName() + "$" + cn;
		} else {
			return cn;
		}
	}

	String getExtends(ClassDef cd) {
		ClassDef c = cd.getSuperclass();
		if (c == null || JavaConst.CLASS_Object.equals(c.getName())) {
			return null;
		}
		return getName(c);
	}

	String getImplements(ClassDef cd) {
		IList<ClassDef> cs = cd.getInterfaces();
		if (cs == null || cs.isEmpty()) {
			return null;
		}
		return StringPrinter.formatComma(cd.getInterfaces().stream().map(c -> getName(c)));
	}

	void analyzeIStringMatcherClasses() {
		JavaAnalyzer ja = new JavaAnalyzer();
		ja.setUseCurrentClassPath(); // to access IStringMatcher
		ja.setSupportModules(true); // to have file paths ready
		ClassDef cd = ja.analyzeClass(IStringMatcher.class);
		FilePath dir = cd.getFullPath().getParent();
		ja.analyzeClassDirectory(dir);
		Tree<ClassDef> tree = JavaAnalyzerTools.getSubTypeTree(cd);

		Set<ClassDef> cds = new HashSet<>(tree.getDescendantValues());
		Set<ClassDef> cds1 = ICollectionTools.filter(cds, c -> c.isAbstract(), HashSet::new);
		Set<ClassDef> cds2 = ICollectionTools.filter(cds, c -> !c.isAbstract(), HashSet::new);
		LOG.info("{}", tree);
		LOG.info("Abstract: {}", cds1);
		LOG.info("Concrete: {}", cds2);
		Set<ClassDef> cds3 = ICollectionTools.filter(cds, c -> c.getSimpleName().startsWith("IString"), HashSet::new);
		LOG.info("Interface: {}", cds3);
	}

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

			//jbc.setJavaVersion(JavaVersion.JAVA_11);
		});

		test(StripEndJmhTest.class);
	}

	/** Analyze performance problem with StringTrimmer */
	public static class StripEndJmhTest {

		// StringTrimmer has a significant performance problem (10%) due to the CharPredicate used.
		// If the predicate is available inline or near the call, it seems that it can be optimized in a way
		// which is not possible if the predicate is created up-front, then stored in a field
		// and later retrieved for being applied. This is somehow surprising as you would think 
		// creating the new string would need most of the time.

		String inputStrip = " <01234> ";

		CharPredicate cp = c -> Character.isWhitespace(c);
		CharPredicate cp2 = cp.negate();
		CharMatcher charMatcher = CharMatcher.whitespace();
		StringTrimmer trimmer = StringTrimmer.builder().setFindCharPredicate(cp).setTrimMode(TrimMode.TAIL).build();

		@TestData
		IList<String> inputs = GapList.create(inputStrip);

		@TestMethod
		public CharSequence testStringUtils(String input) {
			return StringUtils.stripEnd(input, null);
		}

		@TestMethod
		public CharSequence testGuava(String input) {
			return charMatcher.trimTrailingFrom(input);

			// Source com.google.common.base.CharMatcher.Whitespace:
			//
			// 		TABLE is a precomputed hashset of whitespace characters. MULTIPLIER serves as a hash function
			// 		whose key property is that it maps 25 characters into the 32-slot table without collision.
			// 		Basically this is an opportunistic fast implementation as opposed to "good code". For most
			// 		other use-cases, the reduction in readability isn't worth it.
			//
			//		    static final String TABLE =
			//		        "\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000"
			//		            + "\u2029\u000B\u3000\u2008\u2003\u205F\u3000\u1680"
			//		            + "\u0009\u0020\u2006\u2001\u202F\u00A0\u000C\u2009"
			//		            + "\u3000\u2004\u3000\u3000\u2028\n\u2007\u3000";
			//		    static final int MULTIPLIER = 1682554634;
			//		    static final int SHIFT = Integer.numberOfLeadingZeros(TABLE.length() - 1);
			//
			//		    @Override
			//		    public boolean matches(char c) {
			//		      return TABLE.charAt((MULTIPLIER * c) >>> SHIFT) == c;
			//		    }
		}

		@TestMethod
		public CharSequence testJavaLangString(String input) {
			return input.stripTrailing();

			// Source java.lang.StringLatin1:
			//
			//		    public static int lastIndexOfNonWhitespace(byte[] value) {
			//		        int length = value.length;
			//		        int right = length;
			//		        while (0 < right) {
			//		            char ch = (char)(value[right - 1] & 0xff);
			//		            if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
			//		                break;
			//		            }
			//		            right--;
			//		        }
			//		        return right;
			//		    }
		}

		@TestMethod
		public CharSequence testStringTrimmer(String input) {
			return trimmer.trim(input);
		}

		@TestMethod
		public CharSequence testTrimString1(String input) {
			return doTrimString1(input);
		}

		@TestMethod
		public CharSequence testTrimString2(String input) {
			return doTrimString2(input);
		}

		@TestMethod
		public CharSequence testTrimString3(String input) {
			return doTrimString3(input);
		}

		String doTrimString1(String str) {
			int len = str.length();
			int end = CharSequenceTools.doLastIndexOf(str, cp2, len - 1) + 1;
			if (end != len) {
				return str.substring(0, end);
			} else {
				return null;
			}
		}

		String doTrimString2(String str) {
			int len = str.length();
			int end = CharSequenceTools.doLastIndexOf(str, (CharPredicate) c -> !cp.test(c), len - 1) + 1;
			if (end != len) {
				return str.substring(0, end);
			} else {
				return null;
			}
		}

		String doTrimString3(String str) {
			int len = str.length();
			int end = CharSequenceTools.doLastIndexOf(str, (CharPredicate) c -> !Character.isWhitespace(c), len - 1) + 1;
			if (end != len) {
				return str.substring(0, end);
			} else {
				return null;
			}
		}

	}

	/**
	 * Test shows how compact strings (storing only Latin1 characters in bytes) can report unexpected results.
	 * Expectation would be that CharGapList is much faster as fewer objects must be created and copied.
	 * However the the compressed storage makes this effect disappear.  
	 */
	public static class StringRetainTestJmh extends StringJmhBenchmark {

		public StringRetainTestJmh() {
			setJavaVersions(JavaVersion.JAVA_21);
			setCompareParams(false);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			@Param({ "" + CharToolsTest.CHAR_LATIN1, "" + CharToolsTest.CHAR_NO_LATIN1 })
			char c;

			String s;

			@Setup
			public void setup() {
				s = StringTools.repeat("" + c, 1000);
			}
		}

		@Benchmark
		public int testRetainCharList(MyState state) {
			IString cl = new GapString(state.s);
			int len = state.s.length();
			cl.retain(1, len - 1);
			cl.retain(0, len - 2);
			return cl.length();
		}

		@Benchmark
		public int testRetainString(MyState state) {
			int len = state.s.length();
			String s = state.s.substring(1, len);
			s = s.substring(0, len - 2);
			return s.length();
		}
	}

	/**
	 * Show that using static final class instances is as fast as static methods.
	 */
	public static class ModeTestJmh extends StringJmhBenchmark {
		{
			setJavaVersions(JavaVersion.JAVA_21);
			setRunVerify(false);
			setRunTime(1000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> vals = new CyclicSource<>(0, 1, 2, 3, 4);
			CyclicSource<Integer> adds = new CyclicSource<>(5, 6, 7, 8, 9);
			CyclicSource<Boolean> mode0 = new CyclicSource<>(false, false);
			CyclicSource<Boolean> mode1 = new CyclicSource<>(false, true);
			CyclicSource<Boolean> mode2 = new CyclicSource<>(true, true);
		}

		static final Adder adderMode0 = new Adder(false);
		static final Adder adderMode1 = new Adder(true);

		static class Adder {
			boolean mode;

			Adder(boolean mode) {
				this.mode = mode;
			}

			int doAdd(int val, int add) {
				return add(val, add, mode);
			}
		}

		@Benchmark
		public Object testAdderMode0(MyState state) {
			return adderMode0.doAdd(state.vals.next(), state.adds.next());
		}

		@Benchmark
		public Object testAdderMode1(MyState state) {
			return adderMode1.doAdd(state.vals.next(), state.adds.next());
		}

		@Benchmark
		public Object testMode0(MyState state) {
			return add(state.vals.next(), state.adds.next(), state.mode0.next());
		}

		@Benchmark
		public Object testMode1(MyState state) {
			return add(state.vals.next(), state.adds.next(), state.mode1.next());
		}

		@Benchmark
		public Object testMode2(MyState state) {
			return add(state.vals.next(), state.adds.next(), state.mode2.next());
		}

		static int add(int val, int add, boolean mode) {
			if (mode) {
				return val + 2 * add;
			} else {
				return val + 3 * add;
			}
		}
	}

	/**
	 * Show effect of String.indexOf(String) vs String.contains(CharSequence).
	 * If a CharSequence is passed, it is converted to a String using CharSequence.toString().
	 * This needs memory allocation and slows down execution.
	 * 
	 * testCharSeqContainsCharSeq                     69960390.291 �  3416897.901   ops/s
	 * testCharSeqContainsCharSeq:gc.alloc.rate.norm        ? 10??                   B/op
	 * testCharSeqContainsString                      68209668.689 �  5276155.860   ops/s
	 * testCharSeqContainsString:gc.alloc.rate.norm         ? 10??                   B/op
	 * testStringContainsCharSeq                      64216005.068 �  4508967.013   ops/s
	 * testStringContainsCharSeq:gc.alloc.rate.norm         24.000 �        0.001    B/op
	 * testStringContainsString                       92973826.010 � 11166180.362   ops/s
	 * testStringContainsString:gc.alloc.rate.norm          ? 10??                   B/op
	 */
	public static class StringContainsJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			String FIND = "-";
			CharSequence FIND2 = new GapString(FIND);
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + FIND + i + ")");
		}

		@Benchmark
		public boolean testStringContainsString(MyState state) {
			return state.strings.next().contains(state.FIND);
		}

		@Benchmark
		public boolean testStringContainsCharSeq(MyState state) {
			return state.strings.next().contains(state.FIND2);
		}

		@Benchmark
		public boolean testCharSeqContainsString(MyState state) {
			return CharSequenceTools.contains(state.strings.next(), state.FIND);
		}

		@Benchmark
		public boolean testCharSeqContainsCharSeq(MyState state) {
			return CharSequenceTools.contains(state.strings.next(), state.FIND2);
		}
	}

	/**
	 * Show that directly looking checking a character for equality is faster than using a CharPredicate.
	 */
	public static class StringIndexOfJmhTest extends StringJmhBenchmark {

		public StringIndexOfJmhTest() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_21);
			//setRunTime(100);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			final char find = 'x';

			CharPredicate cp = c -> c == find;
			CharPredicate cp2 = CharPredicates.equals(find);
			CharPredicate cp3 = new CharPredicateChar(find);

			CyclicSource<String> strings = new CyclicSource<>(10, i -> StringTools.repeat('\u2192', 1000));
		}

		static class CharPredicateChar implements CharPredicate {

			final char find;

			CharPredicateChar(char find) {
				this.find = find;
			}

			@Override
			public boolean test(char c) {
				return c == find;
			}
		}

		@Benchmark
		public int testIndexOfChar(MyState state) {
			String str = state.strings.next();
			return indexOf(str, state.find);
		}

		@Benchmark
		public int testIndexOfCharPredicate(MyState state) {
			String str = state.strings.next();
			return indexOf(str, state.cp);
		}

		//@Benchmark
		public int testIndexOfCharPredicate2(MyState state) {
			String str = state.strings.next();
			return indexOf(str, state.cp2);
		}

		//@Benchmark
		public int testIndexOfCharPredicate3(MyState state) {
			String str = state.strings.next();
			return indexOf(str, state.cp3);
		}

		static int indexOf(CharSequence str, char c) {
			int end = str.length();
			for (int i = 0; i < end; i++) {
				if (str.charAt(i) == c) {
					return i;
				}
			}
			return -1;
		}

		static int indexOf(CharSequence str, CharPredicate predicate) {
			int end = str.length();
			for (int i = 0; i < end; i++) {
				if (predicate.test(str.charAt(i))) {
					return i;
				}
			}
			return -1;
		}

	}

	/**
	 * Show that JIT can optimize an empty substring operation since Java 17.
	 */
	//java.lang.StringLatin1.newString(byte[], int, int)
	//if (len == 0) { return ""; }
	public static class StringSubstringJmhTest extends StringJmhBenchmark {

		public StringSubstringJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + ")");
		}

		@Benchmark
		public String testSubstring(MyState state) {
			String str = state.strings.next();
			return str.substring(1, 1);
		}

		@Benchmark
		public String testSubstring2(MyState state) {
			String str = state.strings.next();
			return substring2(str, 1, 1);
		}

		static String substring2(String str, int start, int end) {
			if (start == end) {
				return "";
			} else {
				return str.substring(start, end);
			}
		}
	}

	/**
	 * Show that JIT cannot optimize the temporary string created by substring() for writing into a StringBuilder.
	 */
	public static class StringBuilderAppendSubstringJmhTest extends StringJmhBenchmark {

		public StringBuilderAppendSubstringJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			StringBuilder buf = new StringBuilder(10);
			String add = "01234";
		}

		@Benchmark
		public int testAppend(MyState state) {
			StringBuilder buf = state.buf;
			buf.setLength(0);

			buf.append(state.add, 1, 4);

			return buf.length();
		}

		@Benchmark
		public int testAppendSubstring(MyState state) {
			StringBuilder buf = state.buf;
			buf.setLength(0);

			String str = state.add.substring(1, 4);
			buf.append(str);

			return buf.length();
		}
	}

	/**
	 * Show that a direct implementation is faster than a CharPredicate before Java 25, then performance seems to be equal.
	 */
	public static class CharPredicateJmhTest extends StringJmhBenchmark {

		public CharPredicateJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			static final CharPredicate cp = Character::isWhitespace;
			static final CharPredicate cp0 = cp.negate();
			static final CharPredicate cp1 = c -> !cp.test(c);
			static final CharPredicate cp2 = c -> !Character.isWhitespace(c);
			CyclicSource<String> strs = new CyclicSource<>("abc", "  abc");
		}

		@Benchmark
		public int testIndexOf(MyState state) {
			String str = state.strs.next();
			return indexOf(str);
		}

		@Benchmark
		public int testIndexOfPredicate0(MyState state) {
			String str = state.strs.next();
			return indexOfPredicate(str, MyState.cp0);
		}

		@Benchmark
		public int testIndexOfPredicate1(MyState state) {
			String str = state.strs.next();
			return indexOfPredicate(str, MyState.cp1);
		}

		@Benchmark
		public int testIndexOfPredicate2(MyState state) {
			String str = state.strs.next();
			return indexOfPredicate(str, MyState.cp2);
		}

		@Benchmark
		public int testIndexOfPredicate3(MyState state) {
			String str = state.strs.next();
			return indexOfPredicate3(str);
		}

		static int indexOfPredicate(CharSequence str, CharPredicate find) {
			for (int i = 0; i < str.length(); i++) {
				if (find.test(str.charAt(i))) {
					return i;
				}
			}
			return -1;
		}

		static int indexOfPredicate3(CharSequence str) {
			for (int i = 0; i < str.length(); i++) {
				if (MyState.cp0.test(str.charAt(i))) {
					return i;
				}
			}
			return -1;
		}

		static int indexOf(CharSequence str) {
			for (int i = 0; i < str.length(); i++) {
				if (!Character.isWhitespace(str.charAt(i))) {
					return i;
				}
			}
			return -1;
		}
	}

	/**
	 * Show JIT can optimize also nested allocations
	 * - since Java 21, testPoint2a() runs fast without allocation
	 * - before Java 21, testPoint2a() caused allocations and was therefore slower than testPoint2b()
	 */
	public static class EscapeAnalysisPointJmhTest extends StringJmhBenchmark {

		public EscapeAnalysisPointJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		static class Point1 {
			int x;
			int y;

			int getSum() {
				return x + y;
			}
		}

		static class Point2a {
			Point1 pt0 = new Point1();
			Point1 pt1 = new Point1();

			int getSum() {
				return pt0.getSum() + pt1.getSum();
			}
		}

		static class Point2b {
			int x0;
			int y0;
			int x1;
			int y1;

			int getSum() {
				return x0 + y0 + x1 + y1;
			}
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> indexes = new CyclicSource<>(5, i -> i);
		}

		@Benchmark
		public int testPoint2a(MyState state) {
			Point2a pt = createPoint2a(state.indexes.next());
			return pt.getSum();
		}

		@Benchmark
		public int testPoint2b(MyState state) {
			Point2b pt = createPoint2b(state.indexes.next());
			return pt.getSum();
		}

		Point2a createPoint2a(int i) {
			Point2a pt = new Point2a();
			pt.pt0.x = i + 0;
			pt.pt0.y = i + 1;
			pt.pt1.x = i + 2;
			pt.pt1.y = i + 3;
			return pt;
		}

		Point2b createPoint2b(int i) {
			Point2b pt = new Point2b();
			pt.x0 = i + 0;
			pt.y0 = i + 1;
			pt.x1 = i + 2;
			pt.y1 = i + 3;
			return pt;
		}
	}

	/**
	 * Show JIT can optimize lambda workers away, i.e. it is as fast a direct call e.g. to String.substring.
	 */
	public static class InlineApplierJmhTest extends StringJmhBenchmark {

		public InlineApplierJmhTest() {
			// 2 x 5 x 500 ms: second is 5-10% faster
			// 2 x 5 x 1000 ms: second is 5-10% faster
			// 2 x 5 x 5000: equal performance
			setRunTime(1000);

			// Print generated assembly code of all methods
			//setJvmArgs(JavaTool.JvmPrintAssembly);

			// Print generated assembly code of specified method
			getOptions().setJvmArgs(GapList.create("-XX:CompileCommand=print,*Test.sub1", "-XX:CompileCommand=print,*Test.sub2"));

			//setJvmArgs(GapList.create(JavaTool.JvmPrintCompilation));
			//setJvmArgs(JavaTool.JvmPrintInlining);
			//getOptions().setRunIterations(20);
			setJavaVersions(JavaVersion.JAVA_21);
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> strs = new CyclicSource<>(5, i -> "abc" + StringTools.repeat("x", i));
			Substring ss = new Substring();
		}

		interface ISubstring {
			String substring(String str, int start, int end);
		}

		static class Substring implements ISubstring {

			@Override
			public String substring(String str, int start, int end) {
				return str.substring(start, end);
			}
		}

		@Benchmark
		public String test1(MyState state) {
			String str = state.strs.next();
			return sub1(str, 1, 2);
		}

		@Benchmark
		public String test2(MyState state) {
			String str = state.strs.next();
			return sub2(state.ss, str, 1, 2);
		}

		static String sub1(String str, int start, int end) {
			return str.substring(start, end);
		}

		static String sub2(ISubstring ss, String str, int start, int end) {
			return ss.substring(str, start, end);
		}
	}

	/**
	 * Performance with Java 17:
	 * - Field: 100%
	 * - Param: 102%
	 * - ParamFinal: 110%
	 * - ParamStaticFinal: 109%
	 */
	public static class FieldAccessJmhTest extends StringJmhBenchmark {

		public FieldAccessJmhTest() {
			setJavaVersions(JavaVersion.JAVA_17);
			setRunTime(1000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			static final String SEARCH = "xx";
			final String searchFinal = "xx";
			String search = "xx";

			final StartsWithWorker sww = new StartsWithWorker(SEARCH);
			final CyclicSource<String> strs = new CyclicSource<>("abc", "xxabc");
		}

		@Benchmark
		public boolean useField(MyState state) {
			String s = state.strs.next();
			return state.sww.startsWith(s);
		}

		@Benchmark
		public boolean useParamStaticFinal(MyState state) {
			String s = state.strs.next();
			return s.startsWith(MyState.SEARCH);
		}

		@Benchmark
		public boolean useParamFinal(MyState state) {
			String s = state.strs.next();
			return s.startsWith(state.searchFinal);
		}

		@Benchmark
		public boolean useParam(MyState state) {
			String s = state.strs.next();
			return s.startsWith(state.search);
		}

		static class StartsWithWorker {
			final String startsWith;

			StartsWithWorker(String startsWith) {
				this.startsWith = startsWith;
			}

			public boolean startsWith(String str) {
				return str.startsWith(startsWith);
			}
		}

	}

	/**
	 * Show that a dynamic lambda is about 5% slower than a static one which is also 5% slower than a normal method call.
	 */
	public static class DynamicLambdaJmhTest extends StringJmhBenchmark {

		public DynamicLambdaJmhTest() {
			setJavaVersions(JavaVersion.JAVA_21);
			setRunTime(1000);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> ints = new CyclicSource<>(5, i -> i);
			IAdder adder1 = new Adder();
			IAdder adder2 = new DynamicAdder(true);
		}

		interface IAdder {
			int add(int n);
		}

		static class Adder implements IAdder {

			@Override
			public int add(int n) {
				return n + 1;
			}
		}

		static class DynamicAdder implements IAdder {

			Function<Integer, Integer> fnc;

			DynamicAdder(boolean add) {
				fnc = adder(add);
			}

			@Override
			public int add(int n) {
				return fnc.apply(n);
			}

			Function<Integer, Integer> adder(boolean add) {
				if (add) {
					return i -> i + 1;
				} else {
					return i -> i - 1;
				}
			}
		}

		@Benchmark
		public int adder0(MyState state) {
			int n = state.ints.next();
			return add(n);
		}

		int add(int n) {
			return n + 1;
		}

		@Benchmark
		public int adder1(MyState state) {
			int n = state.ints.next();
			return state.adder1.add(n);
		}

		@Benchmark
		public int adder2(MyState state) {
			int n = state.ints.next();
			return state.adder2.add(n);
		}
	}

	/**
	 * Show JIT can optimize delegation away if they are effectively final. 
	 */
	public static class InlineDelegateJmhTest extends StringJmhBenchmark {

		public InlineDelegateJmhTest() {
			// Print generated assembly code of all methods
			//setJvmArgs(JavaTool.JvmPrintAssembly);

			// Print generated assembly code of specified method
			getOptions().setJvmArgs(GapList.create("-XX:CompileCommand=print,*IX.add"));

			//setJvmArgs(GapList.create(JavaTool.JvmPrintCompilation));
			//setJvmArgs(JavaTool.JvmPrintInlining);
			//getOptions().setRunIterations(20);
			setJavaVersions(JavaVersion.JAVA_21);
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		interface I {
			int add(int x0, int x1);
		}

		static class I1 implements I {
			@Override
			public int add(int x0, int x1) {
				return x0 + x1;
			}
		}

		static class I2 implements I {
			@Override
			public int add(int x0, int x1) {
				return x0 + x1;
			}
		}

		static class I3 implements I {
			@Override
			public int add(int x0, int x1) {
				return x0 + x1;
			}
		}

		static class IX implements I {
			I i;

			IX(I i) {
				this.i = i;
			}

			@Override
			public int add(int x0, int x1) {
				return i.add(x0, x1);
			}
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<Integer> indexes = new CyclicSource<>(5, i -> i);
			I i = new I1();
			I1 i1 = new I1();
			I2 i2 = new I2();
			IX ix = new IX(i);
			IX ix1 = new IX(i1);
		}

		public static class MyJmhState2 extends JmhState {
			I1 i1 = new I1();
			I2 i2 = new I2();
			IX ixx2 = new IX(null);

			@Override
			public void onStartIteration(int iter) {
				I i = (iter % 2 == 0) ? i1 : i2;
				ixx2.i = i;
			}
		}

		public static class MyJmhState3 extends JmhState {
			I1 i1 = new I1();
			I2 i2 = new I2();
			I3 i3 = new I3();
			IX ixx3 = new IX(null);

			@Override
			public void onStartIteration(int iter) {
				I i = FuncTools.mapIndex(iter % 3, i1, i2, i3);
				ixx3.i = i;
			}
		}

		//@Benchmark
		public int testI(MyState state) {
			int n = state.indexes.next();
			return state.i.add(n, n + 1);
		}

		//@Benchmark
		public int testI1(MyState state) {
			int n = state.indexes.next();
			return state.i1.add(n, n + 1);
		}

		//@Benchmark
		public int testI2(MyState state) {
			int n = state.indexes.next();
			return state.i2.add(n, n + 1);
		}

		@Benchmark
		public int testIX(MyState state) {
			int n = state.indexes.next();
			return state.ix.add(n, n + 1);
		}

		//@Benchmark
		// Monomorphic call, call to add() is compiled once, in measurement iteration 1
		public int testIX1(MyState state) {
			int n = state.indexes.next();
			return state.ix1.add(n, n + 1);
		}

		@Benchmark
		// Bimorphic call, call to add() is compiled twice, in measurement iteration 1 and 2
		public int testIXX2(MyState state, MyJmhState2 as) {
			int n = state.indexes.next();
			return as.ixx2.add(n, n + 1);
		}

		@Benchmark
		// Megamorphic call, call to add() is compiled three times, in measurement iteration 1, 2, and 3
		public int testIXX3(MyState state, MyJmhState3 as) {
			int n = state.indexes.next();
			return as.ixx3.add(n, n + 1);
		}
	}

	/**
	 * Show JIT can typically not optimize creation of a temporary String which is immediately written into a StringBuilder:
	 * - before Java 17, also constants string creation was not optimized
	 * - since Java 17, creation of constant strings can be optimized away
	 * - creation of dynamic strings cannot be optimized
	 */
	public static class EscapeAnalysisStringJmhTest extends StringJmhBenchmark {

		public EscapeAnalysisStringJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			int[] arr = new int[] { 0, 1, 2, 3, 4 };
			StringBuilder buf = new StringBuilder(10);
			CyclicSource<Integer> indexes = new CyclicSource<>(5, i -> i);

			String get(int id) {
				return "(" + id + ")";
			}
		}

		@Benchmark
		public int testAppendStringFix(MyState state) {
			String s = state.get(0);

			StringBuilder buf = state.buf;
			buf.setLength(0);
			buf.append(s);
			return buf.length();
		}

		@Benchmark
		public int testAppendStringVar(MyState state) {
			String s = state.get(state.indexes.next());

			StringBuilder buf = state.buf;
			buf.setLength(0);
			buf.append(s);
			return buf.length();
		}
	}

	/** Show how formatting into a buffer instead of creating temporary strings can be much faster */
	public static class FormatIntoBufferJmhTest extends StringJmhBenchmark {

		public FormatIntoBufferJmhTest() {
			//setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			int[] arr = new int[] { 0, 1, 2, 3, 4 };
			StringBuilder buf = new StringBuilder(100);
		}

		@Benchmark
		public int testAppendAsString(MyState state) {
			StringBuilder buf = state.buf;
			buf.setLength(0);

			String s = formatArray(state.arr);
			buf.append(s);

			return buf.length();
		}

		@Benchmark
		public int testAppendToBuffer(MyState state) {
			StringBuilder buf = state.buf;
			buf.setLength(0);

			formatArray(buf, state.arr);

			return buf.length();
		}

		//

		static void formatArray(StringBuilder buf, int[] arr) {
			buf.append("[ ");
			for (int i = 0; i < arr.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				formatElem(buf, arr[i]);
			}
			buf.append("]");
		}

		static void formatElem(StringBuilder buf, int i) {
			buf.append("(");
			formatInt(buf, i);
			buf.append(")");
		}

		static void formatInt(StringBuilder buf, int i) {
			if (i > 0) {
				buf.append("+");
			}
			buf.append(i);
		}
		//

		static String formatArray(int[] arr) {
			StringBuilder buf = new StringBuilder();
			buf.append("[ ");
			for (int i = 0; i < arr.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(formatElem(arr[i]));
			}
			buf.append("]");
			return buf.toString();
		}

		static String formatElem(int i) {
			return "(" + formatInt(i) + ")";
		}

		static String formatInt(int i) {
			if (i > 0) {
				return "+" + Integer.toString(i);
			} else {
				return Integer.toString(i);
			}
		}
	}

}
