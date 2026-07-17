package org.magicwerk.strings;

import java.util.function.Consumer;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.function.StringPredicates;
import org.magicwerk.brownies.core.strings.stream.StringStream;
import org.magicwerk.brownies.core.strings.stream.StringStreamer;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ConsumeWhileEnclosed;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadUntilChar;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadUntilLine;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadUntilOneOf;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadUntilString;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadWhileChar;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadWhileCharDoubleEscaped;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadWhileCharEscapedEscaped;
import org.magicwerk.brownies.core.strings.stream.StringStreamerTools.ReadWhileOneOf;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.slf4j.Logger;

/**
 * Test of class {@link StringStreamerTools}.
 */
public class StringStreamerToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringStreamerToolsTest().run();
	}

	void run() {
		//testReadUntilLine();
		testReadUntilOneOf();
		//testReadWhileEscapedCharEscaped();
		//testReadWhileOneOf();
	}

	//

	@Capture
	public void testReadUntilLine() {
		String text = "a\nb\nc\nd\ne";
		{
			StringStreamer ss = new StringStreamer(text);
			ReadUntilLine rul = new ReadUntilLine().setLineMatch(StringPredicates.startsWith("c"));
			ss.readConsume(rul);
			LOG.info("{}\n", rul.getMatch());
		}
		{
			StringStreamer ss = new StringStreamer(text);
			ReadUntilLine rul = new ReadUntilLine().setLineMatchs(GapList.create(StringPredicates.startsWith("c")));
			ss.readConsume(rul);
			LOG.info("{}\n", rul.getMatch());
		}
		{
			StringStreamer ss = new StringStreamer(text);
			ReadUntilLine rul = new ReadUntilLine().setLineMatchs(
					GapList.create(StringPredicates.startsWith("c"), StringPredicates.startsWith("d")));
			ss.readConsume(rul);
			LOG.info("{}\n", rul.getMatch());
		}
	}

	//

	@Capture
	public void testReadWhileChar() {
		ReadWhileChar r1 = new ReadWhileChar('a');
		ReadWhileChar r2 = new ReadWhileChar(c -> c == 'a' || c == 'b');

		LOG.info("testReadWhileChar('a')");
		doTestReadWhileChar(r1, "abc");
		doTestReadWhileChar(r1, "xyz");

		LOG.info("\ntestReadWhileChar(c -> c == 'a' || c == 'b')");
		doTestReadWhileChar(r2, "abc");
		doTestReadWhileChar(r2, "xyz");
	}

	void doTestReadWhileChar(ReadWhileChar reader, String input) {
		StringStreamer streamer = new StringStreamer(input);
		streamer.readConsume(reader);

		String buffer = streamer.buffer();
		String remainder = streamer.readText();
		LOG.info("input: {}, buffer: {}, remainder: {}", input, buffer, remainder);
	}

	//

	@Capture
	public void testReadUntilChar() {
		ReadUntilChar r1 = new ReadUntilChar('x');
		ReadUntilChar r2 = new ReadUntilChar(c -> c == 'x' || c == 'y');

		LOG.info("ReadUntilChar('x')");
		doTestReadUntilChar(r1, "abc");
		doTestReadUntilChar(r1, "axc");

		LOG.info("\nsetConsumeMatch(false)");
		r1.setConsumeMatch(false);
		doTestReadUntilChar(r1, "abc");
		doTestReadUntilChar(r1, "axc");

		LOG.info("\nReadUntilChar(c -> c == 'x' || c == 'y')");
		doTestReadUntilChar(r2, "abc");
		doTestReadUntilChar(r2, "axc");

		LOG.info("\nsetConsumeMatch(false)");
		r2.setConsumeMatch(false);
		doTestReadUntilChar(r2, "abc");
		doTestReadUntilChar(r2, "axc");
	}

	void doTestReadUntilChar(ReadUntilChar reader, String input) {
		StringStreamer streamer = new StringStreamer(input);
		int consumed = streamer.readConsume(reader);

		boolean matched = reader.hasMatched();
		Character match = reader.getMatchChar();
		String buffer = streamer.buffer();
		String remainder = streamer.readText();
		LOG.info("input: {}, match: {}/{}, consumed: {}, buffer: {}, remainder: {}", input, matched, match, consumed, buffer, remainder);
	}

	//

	@Capture
	public void testReadUntilString() {
		ReadUntilString r = new ReadUntilString("012");

		LOG.info("ReadUntilString('012')");
		doTestReadUntilString(r, "abc");
		doTestReadUntilString(r, "a012b");

		LOG.info("\nsetConsumeMatch(false)");
		r.setConsumeMatch(false);
		doTestReadUntilString(r, "abc");
		doTestReadUntilString(r, "a012b");
	}

	void doTestReadUntilString(ReadUntilString reader, String input) {
		StringStreamer streamer = new StringStreamer(input);
		streamer.readConsume(reader);

		boolean matched = reader.hasMatched();
		String match = reader.getMatch();
		String buffer = streamer.buffer();
		String remainder = streamer.readText();
		LOG.info("input: {}, match: {}/{}, buffer: {}, remainder: {}", input, matched, match, buffer, remainder);
	}

	//

	@Capture
	public void testReadWhileOneOf() {
		ReadWhileOneOf reader = new ReadWhileOneOf(GapList.create("ab-", "cd-"));
		StringStream stream = new StringStream("ab-cd-ef");
		//StringStream stream = new StringStream("ab-cd-ef");
		stream.readConsume(reader);
		LOG.info("buffer: {}, available: {}", stream.getBufferView(), stream.getAvailableView());
	}

	@Capture
	public void testReadUntilOneOf() {
		doTestReadUntilOneOf(true);
		System.out.println("\nconsumeMatch = false");
		doTestReadUntilOneOf(false);

		System.out.println("\nrepeated");
		StringStreamer streamer = new StringStreamer("ab-cd-ef");
		ReadUntilOneOf reader = new ReadUntilOneOf(GapList.create("ab-", "cd-"));
		while (true) {
			streamer.readConsume(reader);
			if (!reader.hasMatched()) {
				break;
			}
		}
		System.out.println(streamer);
	}

	void doTestReadUntilOneOf(boolean consumeMatch) {
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch), "");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch), "a");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch), "x");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch), "bcd");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch), "bcd", "bcde");

		System.out.println("preferLong = true");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch).setPreferLong(true), "bcd", "bcdx");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch).setPreferLong(true), "bcd", "bcde");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch).setPreferLong(true), "def", "defgh");

		System.out.println("preventAdvance = true");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch).setPreventAdvance(true), "abc");
		doTestReadUntilOneOf("abcdefg", (r) -> r.setConsumeMatch(consumeMatch).setPreventAdvance(true), "bcd");
	}

	void doTestReadUntilOneOf(String input, Consumer<ReadUntilOneOf> setup, String... matches) {
		StringStreamer streamer = new StringStreamer(input);
		IList<String> strs = GapList.create(matches);
		ReadUntilOneOf reader = new ReadUntilOneOf(strs);
		setup.accept(reader);
		streamer.readConsume(reader);
		String matched = reader.getMatch();
		String buffer = streamer.buffer();
		String remainder = streamer.readText();
		LOG.info("input: {}, matches: {} -> matched: {}, buffer: {}, remainder: {}",
				new Object[] { input, matches, matched, buffer, remainder });
	}

	@Capture
	public void testReadWhileCharEscapedEscaped() {
		char escapeChar = '-';
		char endChar = ']';
		ReadWhileCharEscapedEscaped r = new ReadWhileCharEscapedEscaped(escapeChar, endChar);
		consumeWhileEscaped("abc", r);
		consumeWhileEscaped("ab]x", r);
		consumeWhileEscaped("ab-]cd]x", r);
	}

	@Capture
	public void testReadWhileCharDoubleEscaped() {
		char escapeChar = '-';
		ReadWhileCharDoubleEscaped r = new ReadWhileCharDoubleEscaped(escapeChar);
		consumeWhileEscaped("abc", r);
		consumeWhileEscaped("ab-x", r);
		consumeWhileEscaped("ab--cd-x", r);
	}

	void consumeWhileEscaped(String input, ConsumeWhileEnclosed reader) {
		StringStreamer streamer = new StringStreamer(input);
		streamer.readConsume(reader);
		String consumed = streamer.buffer();
		LOG.info("input: {}, consumed: {}, escaped: {}", input, consumed, reader.needsEscape());
	}

}
