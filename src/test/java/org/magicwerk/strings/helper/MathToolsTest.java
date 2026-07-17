package org.magicwerk.strings.helper;

import static org.magictest.client.Report.setDescription;

import org.magictest.client.Trace;
import org.magicwerk.brownies.core.MathTools2;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.MathTools;
import org.slf4j.Logger;

/**
 * Test of class {@link MathTools2}.
 */
public class MathToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new MathToolsTest().run();
	}

	void run() {
		testSignum();
	}

	@Trace
	public static void testSignum() {
		setDescription("signum(int)");
		MathTools.signum(2);
		MathTools.signum(1);
		MathTools.signum(0);
		MathTools.signum(-1);
		MathTools.signum(-2);

		setDescription("signum(long)");
		MathTools.signum(2L);
		MathTools.signum(1L);
		MathTools.signum(0L);
		MathTools.signum(-1L);
		MathTools.signum(-2L);
	}

}
