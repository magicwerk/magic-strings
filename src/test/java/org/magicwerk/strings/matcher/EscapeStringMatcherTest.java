package org.magicwerk.strings.matcher;

import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.matcher.EscapeStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.slf4j.Logger;

/**
 * Test of class {@link EscapeStringMatcher}.
 */
public class EscapeStringMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		testFind();
	}

	@Trace
	public static void testFind() {
		IStringMatcher matcher = new EscapeStringMatcher('\'', '\'');
		matcher.find("123");
		matcher.find("123 'abc' 456");
		matcher.find("123''456");

		matcher = new EscapeStringMatcher('\'', '|');
		matcher.find("123");
		matcher.find("123 'abc' 456");
		matcher.find("123 |'abc|' 456");
		matcher.find("123 ||'abc||' 456");

		IStringMatcher doubleQuoteStringEnd = new EscapeStringMatcher('"', '\\');

		String s0 = "\"";
		LOG.info("{}", s0);
		CheckTools.check(s0.length() == 1);
		String s1 = "\\\"";
		LOG.info("{}", s1);
		CheckTools.check(s1.length() == 2);
		String s2 = "\\\\\"";
		LOG.info("{}", s2);
		String s3 = "\\\\\\\"";
		LOG.info("{}", s3);

		doubleQuoteStringEnd.find(s0);
		doubleQuoteStringEnd.find(s1);
		doubleQuoteStringEnd.find(s2);
		doubleQuoteStringEnd.find(s3);
	}
}
