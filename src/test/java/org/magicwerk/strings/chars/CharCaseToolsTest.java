package org.magicwerk.strings.chars;

import java.util.regex.Pattern;

import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.slf4j.Logger;

/**
 * Test of class {@link CharCaseTools}.
 */
public class CharCaseToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CharCaseToolsTest().run();
	}

	void run() {
		testRegexCaseJdk();
	}

	static final char CHAR_UC_ASCII = 'A'; // U+0041
	static final char CHAR_LC_ASCII = 'a'; // U+0061
	static final char CHAR_UC_ISO_8859_1 = 'Ä'; // U+00c4
	static final char CHAR_LC_ISO_8859_1 = 'ä'; // U+00e4
	static final char CHAR_UC_CHAR = 'Ā'; // U+0100
	static final char CHAR_LC_CHAR = 'ā'; // U+0101
	static final int CHAR_UC_CODEPOINT = CodePointTools.firstCodePoint("𐐀"); // U+10400
	static final int CHAR_LC_CODEPOINT = CodePointTools.firstCodePoint("𐐨"); // U+10428

	public void testRegexCaseJdk() {
		// Needs CASE_INSENSITIVE
		testRegex(Character.toString(CHAR_UC_ASCII), Character.toString(CHAR_LC_ASCII));
		// Needs CASE_INSENSITIVE and UNICODE_CASE
		testRegex(Character.toString(CHAR_UC_ISO_8859_1), Character.toString(CHAR_LC_ISO_8859_1));
		testRegex(Character.toString(CHAR_UC_CHAR), Character.toString(CHAR_LC_CHAR));
		testRegex(Character.toString(CHAR_UC_CODEPOINT), Character.toString(CHAR_LC_CODEPOINT));
	}

	public void testRegex(String regex, String input) {
		Pattern p0 = Pattern.compile(regex, 0);
		boolean m0 = p0.matcher(input).matches();
		Pattern p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		boolean m1 = p1.matcher(input).matches();
		Pattern p2 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		boolean m2 = p2.matcher(input).matches();
		LOG.info("{} {} {}", m0, m1, m2);
	}

	//

	public interface IGetChars_OLD {

		interface GetChars {
			int size();
		}

		static class GetChars1 implements GetChars {
			char c;

			@Override
			public int size() {
				return 1;
			}
		}

		static class GetChars2 implements GetChars {
			char c1;
			char c2;

			@Override
			public int size() {
				return 2;
			}
		}

		static class GetChars3 implements GetChars {
			char c1;
			char c2;
			char c3;

			@Override
			public int size() {
				return 3;
			}
		}

		int getChars(char c, char[] chars);

		public static IGetChars_OLD c() {
			return (c, cs) -> {
				cs[0] = c;
				return 1;
			};
		}

		public static IGetChars_OLD Lc() {
			return (c, cs) -> {
				cs[0] = Character.toLowerCase(c);
				return 1;
			};
		}

		public static IGetChars_OLD Uc() {
			return (c, cs) -> {
				cs[0] = Character.toUpperCase(c);
				return 1;
			};
		}

		public static IGetChars_OLD cUc() {
			return (c, cs) -> {
				cs[0] = c;
				cs[1] = Character.toUpperCase(c);
				return 2;
			};
		}

		public static IGetChars_OLD cUcLUc() {
			return (c, cs) -> {
				cs[0] = c;
				cs[1] = Character.toUpperCase(c);
				cs[2] = Character.toLowerCase(cs[1]);
				return 3;
			};
		}

	}

}
