package org.magicwerk.strings.chars;

import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationFreeRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.strings.chars.CodePointPredicates;
import org.openjdk.jmh.annotations.Benchmark;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link CodePointPredicates}.
 */
public class CodePointPredicatesTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CodePointPredicatesTest().run();
	}

	void run() {
		testCodePointPredicatesAllocationFree();
	}

	@Test(groups = { "slow" })
	@Capture(source = Source.NONE)
	public void testCodePointPredicatesAllocationFree() {
		Options opts = new Options().includeClass(CodePointPredicatesAllocationFree.class);
		new JmhAllocationFreeRunner().checkAllocationFree(opts);
	}

	public static class CodePointPredicatesAllocationFree {
		@Benchmark
		public void test() {
			CodePointPredicates.oneOf("abc");
		}
	}

}
