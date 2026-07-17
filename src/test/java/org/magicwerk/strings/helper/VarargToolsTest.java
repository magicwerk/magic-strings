package org.magicwerk.strings.helper;

import org.magictest.client.Capture;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.VarargTools;
import org.slf4j.Logger;

/**
 * Test of class {@link VarargTools}.
 */
public class VarargToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new VarargToolsTest().run();
	}

	void run() {
		testProcessVarargTuples();
	}

	@Capture
	public void testProcessVarargTuples() {
		VarargTools.processVarargTuples((k, v) -> LOG.info("{}: {}", k, v), 1, "1a");
		VarargTools.processVarargTuples((k, v) -> LOG.info("{}: {}", k, v), 1, "1a", 2, "2b");
	}

}
