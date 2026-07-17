package org.magicwerk.strings.text;

import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.text.TextPos;
import org.magicwerk.strings.text.TextRange;
import org.slf4j.Logger;

/**
 * Test of class {@link TextRange}.
 */
public class TextRangeTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new TextRangeTest().run();
	}

	void run() {
		testTextRange();
	}

	@Trace(traceMethod = "/.*/", result = Trace.RESULT | Trace.THIS)
	public void testTextRange() {
		new TextRange(new TextPos(0, 4), new TextPos(0, 7));

		new TextRange(new TextPos(1, 0), new TextPos(0, 0));
	}

	@Trace
	public void testClip() {
		TextRange clip = new TextRange(new TextPos(3, 3), new TextPos(7, 7));

		// Clip TextPos
		clip.clip(new TextPos(2, 2));
		clip.clip(new TextPos(5, 5));
		clip.clip(new TextPos(8, 8));

		clip.clip(new TextRange(new TextPos(4, 4), new TextPos(6, 6)));
		clip.clip(new TextRange(new TextPos(2, 2), new TextPos(8, 8)));
		clip.clip(new TextRange(new TextPos(2, 2), new TextPos(5, 5)));
		clip.clip(new TextRange(new TextPos(5, 5), new TextPos(8, 8)));
		clip.clip(new TextRange(new TextPos(1, 1), new TextPos(2, 2)));
		clip.clip(new TextRange(new TextPos(8, 8), new TextPos(9, 9)));
	}

}
