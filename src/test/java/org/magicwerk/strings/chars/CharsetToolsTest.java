package org.magicwerk.strings.chars;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.chars.CharsetTools;
import org.magicwerk.strings.helper.CheckTools;
import org.slf4j.Logger;

/**
 * Test of class {@link CharsetTools}.
 */
public class CharsetToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CharsetToolsTest().test();
	}

	void test() {
		//testIso8859();
		testDecoder();
	}

	@Trace
	public static void testHasReplacementChar() {
		Charset csIso88591 = StandardCharsets.ISO_8859_1;
		Charset csUtf8 = StandardCharsets.UTF_8;

		byte[] chars1 = new byte[] { 'a', 'b', 'c' };
		byte[] chars2 = new byte[] { 'a', 'b', 'c', (byte) 0xE4 /* ae */, 'A' };

		String str1 = new String(chars1, csIso88591);
		String str2 = new String(chars2, csIso88591);
		String str3 = new String(chars2, csUtf8);

		CharsetTools.hasReplacementChar(str1);
		CharsetTools.hasReplacementChar(str2);
		CharsetTools.hasReplacementChar(str3);
	}

	@Capture
	public void testIso8859() {
		int num = 256;
		byte[] bytes = new byte[num];
		for (int i = 0; i < num; i++) {
			bytes[i] = (byte) i;
		}

		String str = CharsetTools.decode(bytes, CharsetTools.ISO88591);
		CheckTools.check(str.length() == bytes.length);
		byte[] bytes2 = CharsetTools.encode(str, CharsetTools.ISO88591);
		CheckTools.check(Arrays.equals(bytes, bytes2));
	}

	@Capture
	public void testDecoder() {
		final char CHAR_LEN_3 = '\u20AC'; // Euro

		String strLen6 = "";
		strLen6 += CHAR_LEN_3;
		strLen6 += CHAR_LEN_3;
		byte[] data = CharsetTools.encode(strLen6, CharsetTools.UTF8);
		LOG.info("{}", data);

		// The default decoder reports malformed input with an exception
		try {
			CharsetTools.decode(data, 0, 4, CharsetTools.UTF8);
		} catch (Exception e) {
			LOG.info("Error: {}", e.getMessage());
		}

		// Create a decoder to ignore malformed input
		CharsetDecoder csd = CharsetTools.getDecoder(CharsetTools.UTF8).onMalformedInput(CodingErrorAction.IGNORE);
		String str = CharsetTools.decode(data, 0, 4, csd);
		LOG.info("Result: {}", str);
	}
}
