package org.magicwerk.strings.chars;

import java.util.function.Function;

import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.core.concurrent.CompareExecutor;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JavaEnvironment;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.helper.CheckTools;
import org.slf4j.Logger;

/**
 * Test of class {@link CharSequenceTools}.
 */
public class CodePointToolsTest {

	// High Surrogate (First char in the pair) – A value from U+D800 to U+DBFF
	// Low Surrogate (Second char in the pair) – A value from U+DC00 to U+DFFF

	// Constants defined in Character class
	//public static final char MIN_HIGH_SURROGATE = '\uD800';
	//public static final char MAX_HIGH_SURROGATE = '\uDBFF';
	//public static final char MIN_LOW_SURROGATE  = '\uDC00';
	//public static final char MAX_LOW_SURROGATE  = '\uDFFF';

	public static final int CODE_POINT_SURROGATE_0 = 0x10000;
	public static final int CODE_POINT_SURROGATE_1 = 0x10001;

	public static final int CODE_POINT_SURROGATE_00;
	public static final int CODE_POINT_SURROGATE_01;
	public static final int CODE_POINT_SURROGATE_10;
	public static final int CODE_POINT_SURROGATE_11;

	/** String containing one single code points consisting of a hi/lo surrogate pair (length 2) */
	public static final String STRING_SURROGATE_00;
	public static final String STRING_SURROGATE_01;
	public static final String STRING_SURROGATE_10;
	public static final String STRING_SURROGATE_11;

	public static final char CHAR_HI_SURROGATE_0 = Character.MIN_HIGH_SURROGATE + 0;
	public static final char CHAR_HI_SURROGATE_1 = Character.MIN_HIGH_SURROGATE + 1;
	public static final char CHAR_LO_SURROGATE_0 = Character.MIN_LOW_SURROGATE + 0;
	public static final char CHAR_LO_SURROGATE_1 = Character.MIN_LOW_SURROGATE + 1;

	public static final String STRING_SURROGATE_HI = String.valueOf(CHAR_HI_SURROGATE_0);
	public static final String STRING_SURROGATE_LO = String.valueOf(CHAR_LO_SURROGATE_0);

	static {
		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_0));
		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_1));

		char[] cs0 = Character.toChars(CODE_POINT_SURROGATE_0);
		char[] cs1 = Character.toChars(CODE_POINT_SURROGATE_1);
		CheckTools.check(cs0[0] == cs1[0]);
		CheckTools.check(cs0[1] != cs1[1]);

		CODE_POINT_SURROGATE_00 = Character.toCodePoint(CHAR_HI_SURROGATE_0, CHAR_LO_SURROGATE_0);
		CODE_POINT_SURROGATE_01 = Character.toCodePoint(CHAR_HI_SURROGATE_0, CHAR_LO_SURROGATE_1);
		CODE_POINT_SURROGATE_10 = Character.toCodePoint(CHAR_HI_SURROGATE_1, CHAR_LO_SURROGATE_0);
		CODE_POINT_SURROGATE_11 = Character.toCodePoint(CHAR_HI_SURROGATE_1, CHAR_LO_SURROGATE_1);

		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_00));
		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_01));
		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_10));
		CheckTools.check(!CodePointTools.isCharCodePoint(CODE_POINT_SURROGATE_11));

		STRING_SURROGATE_00 = Character.toString(CODE_POINT_SURROGATE_00);
		STRING_SURROGATE_01 = Character.toString(CODE_POINT_SURROGATE_01);
		STRING_SURROGATE_10 = Character.toString(CODE_POINT_SURROGATE_10);
		STRING_SURROGATE_11 = Character.toString(CODE_POINT_SURROGATE_11);
	}

	// Note that Java does not allow you to include a surrogate character in an easy way like '\u10000'
	public static final String STRING_SURROGATE_0 = CodePointTools.codePointToString(CODE_POINT_SURROGATE_0);
	public static final String STRING_SURROGATE_1 = CodePointTools.codePointToString(CODE_POINT_SURROGATE_1);

	public static final String STRING_WITH_SURROGATE = "(" + CodePointTools.codePointToString(CODE_POINT_SURROGATE_0) + ")";

	public static final int INVALID_CODEPOINT = Character.MAX_CODE_POINT + 1;

	//

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CodePointToolsTest().run();
	}

	void run() {
		testCodePoints();
		testCodePointsJava21();
		//testCodePointCount();
	}

	void testCodePointsJava21() {
		JavaTool jt = BrowniesJavaEnv.createJavaTool(JavaVersion.JAVA_21);
		jt.setPrintOutput(true);
		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(jt);
		jr.setMainMethod(CodePointToolsTest.class, "testCodePoints");
		jr.run();
	}

	void testCodePoints() {
		LOG.info("testCodePoints (Java {})", JavaEnvironment.getSystemJavaVersion());

		CheckTools.check(STRING_SURROGATE_HI.length() == 1);
		CheckTools.check(STRING_SURROGATE_LO.length() == 1);
		{
			// A character followed by a surrogate-pair
			String s = new String(new char[] { CHAR_HI_SURROGATE_0, CHAR_HI_SURROGATE_0, CHAR_LO_SURROGATE_0 });
			CheckTools.check(s.length() == 3);
			CheckTools.check(s.codePointCount(0, s.length()) == 2);
			CheckTools.check(s.indexOf(CHAR_HI_SURROGATE_0) == 0);
			CheckTools.check(s.indexOf(CODE_POINT_SURROGATE_00) == 1);
			// Even if the char sequence exists in the string, -1 is returned as the code point is not valid
			CheckTools.check(s.indexOf(Character.toCodePoint(CHAR_HI_SURROGATE_0, CHAR_HI_SURROGATE_0)) == -1);

			String ss = String.valueOf(CHAR_HI_SURROGATE_0);
			CheckTools.check(s.indexOf(ss) == 0);
			CheckTools.check(s.indexOf(ss, 1) == 1);
			ss = String.valueOf(CHAR_LO_SURROGATE_0);
			CheckTools.check(s.indexOf(ss, 1) == 2);

			// Note: there is no code point CHAR_LO_SURROGATE_0 in the string, but indexOf() finds it nevertheless
			IIntList cps = CodePointTools.getCodePoints(s);
			CheckTools.check(cps.contains(CHAR_LO_SURROGATE_0) == false);
			CheckTools.check(s.indexOf(CHAR_LO_SURROGATE_0) != -1);
			CheckTools.check(s.indexOf(String.valueOf(CHAR_LO_SURROGATE_0)) != -1);
		}
		{
			// A surrogate-pair followed by a character 
			String s = new String(new char[] { CHAR_HI_SURROGATE_0, CHAR_LO_SURROGATE_0, CHAR_HI_SURROGATE_0 });
			CheckTools.check(s.length() == 3);
			CheckTools.check(s.codePointCount(0, s.length()) == 2);
			CheckTools.check(s.indexOf(CHAR_HI_SURROGATE_0) == 0);
			CheckTools.check(s.indexOf(CODE_POINT_SURROGATE_00) == 0);
			// Even if the char sequence exists in the string, -1 is returned as the code point is not valid
			CheckTools.check(s.indexOf(Character.toCodePoint(CHAR_LO_SURROGATE_0, CHAR_HI_SURROGATE_0)) == -1);
			// Even if low surrogate at position 1 belongs to a surrogate pair, it is returned if searched as char
			CheckTools.check(s.indexOf(CHAR_LO_SURROGATE_0, 1) == 1);

			String ss = String.valueOf(CHAR_HI_SURROGATE_0);
			CheckTools.check(s.indexOf(ss) == 0);
			ss = String.valueOf(CHAR_LO_SURROGATE_0);
			CheckTools.check(s.indexOf(ss, 1) == 1);
			ss = String.valueOf(CHAR_HI_SURROGATE_0);
			CheckTools.check(s.indexOf(ss, 1) == 2);

			// Note: there is no code point CHAR_LO_SURROGATE_0 in the string, but indexOf() finds it nevertheless
			IIntList cps = CodePointTools.getCodePoints(s);
			CheckTools.check(cps.contains(CHAR_LO_SURROGATE_0) == false);
			CheckTools.check(s.indexOf(CHAR_LO_SURROGATE_0) != -1);
			CheckTools.check(s.indexOf(String.valueOf(CHAR_LO_SURROGATE_0)) != -1);
		}
	}

	// codePointAt

	@Capture
	public void testCodePointAt() {
		testCodePointAt("abc");
		testCodePointAt(STRING_WITH_SURROGATE);
	}

	void testCodePointAt(String str) {
		for (int i = 0; i <= str.length(); i++) {
			testCodePointAt(str, i);
		}
	}

	void testCodePointAt(String str, int index) {
		// Use single instance of StringIndexOutOfBoundsException so equals() will return true
		StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException();
		Function<Throwable, Throwable> changeError = t -> (t instanceof StringIndexOutOfBoundsException) ? e : t;

		new CompareExecutor().checkEqualChangeError(changeError,
				() -> str.codePointAt(index),
				() -> CodePointTools.codePointAt(str, index));
	}

	// codePointBefore

	@Capture
	public void testCodePointBefore() {
		testCodePointAt("abc");
		testCodePointAt(STRING_WITH_SURROGATE);
	}

	void testCodePointBefore(String str) {
		for (int i = 0; i <= str.length(); i++) {
			testCodePointBefore(str, i);
		}
	}

	void testCodePointBefore(String str, int index) {
		new CompareExecutor().checkEqual(
				() -> str.codePointBefore(index),
				() -> CodePointTools.codePointBefore(str, index));
	}

	// codePointCount

	@Capture
	public void testCodePointCount() {
		testCodePointCount("abc");
		testCodePointCount(STRING_WITH_SURROGATE);

		String s = getString(CHAR_HI_SURROGATE_0, CHAR_LO_SURROGATE_0, CHAR_HI_SURROGATE_1, 'x');
		CheckTools.check(s.length() == 4);
		CheckTools.check(s.codePointCount(0, s.length()) == 3);

		IIntList cps0 = IntGapList.create(s.codePoints().toArray());
		IIntList cps1 = CodePointTools.getCodePoints(s);
		CheckTools.check(cps0.equals(cps1));
		CheckTools.check(cps0.size() == 3);
		CheckTools.check(cps0.get(0) == CODE_POINT_SURROGATE_00 && cps0.get(1) == CHAR_HI_SURROGATE_1 && cps0.get(2) == 'x');
	}

	void testCodePointCount(String str) {
		new CompareExecutor().checkEqual(
				() -> str.codePointCount(0, str.length()),
				() -> CodePointTools.codePointCount(str, 0, str.length()));
	}

	String getString(char... cs) {
		return new String(cs);
	}

}
