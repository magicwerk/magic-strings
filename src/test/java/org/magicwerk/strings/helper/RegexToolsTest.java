package org.magicwerk.strings.helper;

import java.util.regex.Pattern;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.RegexTools;
import org.slf4j.Logger;

/**
 * Test of class {@link RegexTools}.
 */
public class RegexToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new RegexToolsTest().run();
	}

	void run() {
	}

	@Trace
	public static void testSplit() {
		Pattern p = Pattern.compile(",");
		RegexTools.split("12,34,56", p);
		RegexTools.split("12,34,,56", p);
		RegexTools.split(",12,34,56,", p);
		RegexTools.split(",", p);

		RegexTools.split("12", p);
		RegexTools.split("", p);
		RegexTools.split(null, p);
	}

	@Trace
	public static void testGetFirstSplit() {
		Pattern p = Pattern.compile(",");
		RegexTools.getFirstSplit("12,34,,56", p);
		RegexTools.getFirstSplit(",12,34,56,", p);
		RegexTools.getFirstSplit(",", p);
		RegexTools.getFirstSplit("12", p);
		RegexTools.getFirstSplit("", p);
		RegexTools.getFirstSplit(null, p);
	}

	@Trace
	public static void testCount() {
		Pattern p = Pattern.compile(",");
		RegexTools.count("12,34,56", p);
		RegexTools.count("12,34,,56", p);
		RegexTools.count(",12,34,56,", p);
		RegexTools.count(",", p);

		RegexTools.count("12", p);
		RegexTools.count("", p);
		RegexTools.count(null, p);
	}

	@Trace(traceMethod = "/.*/")
	public void testLiteral() {
		RegexTools.isSimpleStringLiteral("abc");
		RegexTools.isSimpleStringLiteral("^abc");
		RegexTools.isSimpleStringLiteral("\\^abc");

		RegexTools.getStringLiteral("abc");
		RegexTools.getStringLiteral("^abc");
		RegexTools.getStringLiteral("\\^abc");
	}

	@Trace
	public static void testGet() {
		RegexTools.get(Pattern.compile("-(abc)-"), "0-abc-1");
		RegexTools.get(Pattern.compile("-(ab(c))-"), "0-abc-1", 1);

		RegexTools.get(Pattern.compile("abc"), "0-def-1");

		RegexTools.get(Pattern.compile("\\d"), "a 01 b 23 c");
	}

	@Trace
	public static void testGetAll() {
		Pattern p = Pattern.compile("^\\s*(\\d*)\\s*(\\w*)\\s*$");
		RegexTools.getAll(p, "1024 b");
		RegexTools.getAll(p, " 1024  b ");
		RegexTools.getAll(p, "1024");
		RegexTools.getAll(p, "b");
		RegexTools.getAll(p, "b b");
	}

	@Trace
	public static void testMatches() {
		RegexTools.matches("abc", "abc");
		RegexTools.matches("a", "abc");
		RegexTools.matches("\\w+", "abc");
		RegexTools.matches("\\s*[0-9]{4}\\s*", "1000");
		RegexTools.matches("-\\d{3}-", "abc-123-xyz");
		RegexTools.matches("^-\\d{3}-$", "abc-123-xyz");
		RegexTools.matches("\\w{3}-\\d{3}-\\w{3}", "abc-123-xyz");
		RegexTools.matches("^\\w{3}-\\d{3}-\\w{3}$", "abc-123-xyz");
	}

	@Trace
	public static void testRemove() {
		RegexTools.remove("\\d", "a1c");
		RegexTools.remove("(\\d)", "a1c");

		RegexTools.remove("9", "a1c");
	}

	@Trace
	public static void testSet() {
		RegexTools.set("\\d", "a1c", "X");
		RegexTools.set("(\\d)", "a1c", "X");
	}

	@Trace
	public static void testSetAll() {
		RegexTools.setAll("\\d", "a1c2e", "X");
		RegexTools.setAll("(\\d)", "a1c2e", "X");
	}

	@Trace
	public static void testRemoveAll() {
		RegexTools.removeAll("\\d", "a1c2e");
		RegexTools.removeAll("(\\d)", "a1c2e");
	}

	@Capture
	public static void testRegexTools() {
		testRegexFind("-\\d{3}-", "abc-123-xyz"); // matches
		testRegexFind("^-\\d{3}-$", "abc-123-xyz"); // no match

		testRegexFind("\\w{3}-\\d{3}-\\w{3}", "abc-123-xyz");
		testRegexFind("^\\w{3}-\\d{3}-\\w{3}$", "abc-123-xyz");
	}

	static void testRegexMatches(String pattern, String input) {
		boolean matches = RegexTools.matches(pattern, input);
		if (matches)
			System.out.println("'" + pattern + "' matches '" + input + "'");
		else
			System.out.println("'" + pattern + "' does not match '" + input + "'");
	}

	static void testRegexFind(String pattern, String input) {
		boolean matches = RegexTools.find(pattern, input);
		if (matches)
			System.out.println("'" + pattern + "' finds '" + input + "'");
		else
			System.out.println("'" + pattern + "' does not find '" + input + "'");
	}

}
