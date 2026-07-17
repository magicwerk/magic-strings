package org.magicwerk.strings.format;

import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.format.FormatTools;
import org.magicwerk.strings.helper.CheckTools;
import org.slf4j.Logger;
import org.testng.annotations.Test;

/**
 * Test of class {@link FormatTools}.
 */
public class FormatToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new FormatToolsTest().run();
	}

	void run() {
		testFormatIntAsChars();
	}

	// Format int

	@Test
	public void testFormatIntAsChars() {
		doTestFormatIntAsChars(Integer.MIN_VALUE);
		doTestFormatIntAsChars(-1);
		doTestFormatIntAsChars(0);
		doTestFormatIntAsChars(1);
		doTestFormatIntAsChars(Integer.MAX_VALUE);
	}

	//@Test(groups = { "slow" })
	public void testFormatIntAsCharsFull() {
		for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
			if (i % 100_000_000 == 0) {
				LOG.info("{}", i);
			}
			doTestFormatIntAsChars(i);
		}
		doTestFormatIntAsChars(Integer.MAX_VALUE);
	}

	void doTestFormatIntAsChars(int val) {
		char[] chars = FormatTools.formatIntAsChars(val);
		String str = Integer.toString(val);
		CheckTools.check(str.equals(new String(chars)));
	}

	// Format long

	@Test
	public void testFormatLongAsChars() {
		doTestFormatLongAsChars(Long.MIN_VALUE);
		doTestFormatLongAsChars(-1);
		doTestFormatLongAsChars(0);
		doTestFormatLongAsChars(1);
		doTestFormatLongAsChars(Long.MAX_VALUE);
	}

	//@Test(groups = { "slow" })
	public void testFormatLongAsCharsFull() {
		for (long i = Long.MIN_VALUE; i < Long.MAX_VALUE; i++) {
			if (i % 100_000_000 == 0) {
				LOG.info("{}", i);
			}
			doTestFormatLongAsChars(i);
		}
		doTestFormatLongAsChars(Long.MAX_VALUE);
	}

	void doTestFormatLongAsChars(long val) {
		char[] chars = FormatTools.formatLongAsChars(val);
		String str = Long.toString(val);
		CheckTools.check(str.equals(new String(chars)));
	}
}
