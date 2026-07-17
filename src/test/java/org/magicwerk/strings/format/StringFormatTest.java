package org.magicwerk.strings.format;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.format.StringFormat;
import org.magicwerk.strings.format.StringFormatParsers.LiteralFormatParser;
import org.magicwerk.strings.format.StringFormatParsers.SingleCharFormatParser;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.PrintTools;
import org.magicwerk.strings.mapper.IStringMapper;
import org.slf4j.Logger;

/**
 * Test of class {@link StringFormat}.
 */
public class StringFormatTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringFormatTest().run();
	}

	void run() {
		//testLiteralFormatParser();
		//testEscapelessNamedFormatParser();
		//testFormatSql();
		testLiteral();
	}

	@Trace(traceMethod = "/.*Literal.*/")
	public void testLiteral() {
		{
			String f = "abc";
			StringFormat sf = new StringFormat(f);
			sf.isLiteralString();
			sf.getLiteralString();
			String r = sf.format();
			CheckTools.check(r.equals(f));
		}
		{
			String f = "a{}c";
			StringFormat sf = new StringFormat(f);
			sf.isLiteralString();
			sf.getLiteralString();
		}
		{
			String f = "a{}c";
			String f2 = "a{{}c";
			StringFormat sf = new StringFormat(f2);
			sf.isLiteralString();
			sf.getLiteralString();
			String r = sf.format();
			CheckTools.check(r.equals(f));
		}
	}

	@Capture
	public void testLiteralFormatParser() {
		StringFormat sf = LiteralFormatParser.create("abc");
		String s = sf.format("1");
		LOG.info("{}", s);
	}

	@Capture
	public void testFormatSql() {
		String sql = "update tab set x=?, y=?";
		String log = formatSql(sql, "1", "2");
		LOG.info("{} -> {}", sql, log);
	}

	String formatSql(String sql, Object... args) {
		StringBuilder buf = new StringBuilder();
		final int[] index = new int[] { 0 };
		StringFormat sf = new StringFormat(sql, new SingleCharFormatParser('?'));
		String str = sf.formatMapper(new IStringMapper() {
			@Override
			public String getString(Object key) {
				String val = PrintTools.toString(args[index[0]]);
				index[0]++;
				return val;
			}
		});
		buf.append(str);
		return buf.toString();
	}

}
