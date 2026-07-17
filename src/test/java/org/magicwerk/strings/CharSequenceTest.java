package org.magicwerk.strings;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.concurrent.RunnableExecutor;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;

/**
 * Test classes implementing {@link CharSequence}.
 */
public class CharSequenceTest {

	@Capture
	public static void testString() {
		System.out.println("Testing java.lang.String as CharSequence");
		testCharSequence("abc");
	}

	//

	/**
	 * Test that the passed {@link CharSequence} fulfills the contract.
	 * 
	 * @param str	CharSequence to check
	 */
	public static void testCharSequence(CharSequence str) {
		testCharAt(str);
		testSubSequence(str);
		testToString(str);
	}

	static void testCharAt(CharSequence str) {
		int len = str.length();

		RunnableExecutor re = new RunnableExecutor().setMustFail();
		re.run(() -> str.charAt(-1));
		re.run(() -> str.charAt(len));
	}

	static void testSubSequence(CharSequence str) {
		int len = str.length();
		String str1 = toString(str);
		String str2 = toString(str.subSequence(0, len));
		CheckTools.check(ObjectTools.equals(str1, str2));

		RunnableExecutor re = new RunnableExecutor().setMustFail();
		re.run(() -> str.subSequence(-1, 0));
		re.run(() -> str.subSequence(0, len + 1));
	}

	static void testToString(CharSequence str) {
		String str1 = toString(str);
		String str2 = str.toString();
		CheckTools.check(ObjectTools.equals(str1, str2));
	}

	static String toString(CharSequence str) {
		int len = str.length();
		StringBuilder buf = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			buf.append(str.charAt(i));
		}
		return buf.toString();
	}
}
