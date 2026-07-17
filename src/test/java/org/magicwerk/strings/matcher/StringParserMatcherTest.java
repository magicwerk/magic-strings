package org.magicwerk.strings.matcher;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.StringParserMatcher;
import org.magicwerk.brownies.core.strings.parser.StringParser;
import org.magicwerk.brownies.core.strings.parser.StringParser.ParserMatcher;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.slf4j.Logger;

/**
 * Test of {@link StringParserMatcher}.
 */
public class StringParserMatcherTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringParserMatcherTest().run();
	}

	void run() {
		testStringParserMatcher();
	}

	@Capture
	public void testStringParserMatcher() {
		IStringMatcher procStart = StringMatcher.of("begin");
		IStringMatcher procEnd = StringMatcher.of("end;");
		ParserMatcher pm = new ParserMatcher("proc", procStart, procEnd).setRecursive(true);

		StringParser sp = new StringParser();
		sp.addParser(pm);
		StringParserMatcher spm = new StringParserMatcher(sp);

		testStringParserMatcher(spm, "before begin null; end; after", 0);
		testStringParserMatcher(spm, "before begin null1; begin null2; end; end; after", 0);
	}

	void testStringParserMatcher(StringParserMatcher nsm, String str, int start) {
		while (true) {
			IMatch m = nsm.find(str, start);
			if (m == null) {
				break;
			}
			LOG.info("{}", m);
			start = m.getEnd();
		}
		LOG.info("");
	}
}
