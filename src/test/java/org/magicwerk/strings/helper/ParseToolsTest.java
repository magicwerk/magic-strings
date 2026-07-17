package org.magicwerk.strings.helper;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestData;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethod;
import org.magicwerk.strings.BenchmarkTestBase;
import org.magicwerk.strings.helper.ParseTools;
import org.slf4j.Logger;

public class ParseToolsTest extends BenchmarkTestBase {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ParseToolsTest().run();
	}

	void run() {
		runManual();

		//test();
	}

	void runManual() {
		configureJmhBenchmarkCreator(jbc -> {
			jbc.setRunBenchmark(true);
			jbc.setRunBenchmarkFast(false);
			jbc.setRunBenchmarkShowHtml(true);
			//jbc.setRunBenchmarkShowBytecode(true);
			//jbc.setRunBenchmarkShowSource(true);

			// Analyze JIT
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation));
			//jbc.setJvmArgs(CollectionTools.concat(JavaTool.JvmPrintCompilation, JavaTool.JvmPrintInlining, JavaTool.JvmLogCompilation("hotspot.log")));

			// Analyze Allocation: JmhAllocationJfrObserverState needs at least Java 16
			//jbc.setBenchmarkStateClass(JmhAllocationJfrObserverState.class);

			//jbc.setRunTime(1000);
			jbc.setJavaVersions(JavaVersion.JAVA_17);
			//jbc.setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21, JavaVersion.JAVA_25);
		});

		test(ParseInt_Test.class);
	}

	public static class ParseInt_Test {

		// ParseTools.parseInt() is allocation free with Java 17

		@TestData
		IList<String> inputs = GapList.create("1", "10", "100", "1000", "10000", "-1");

		@TestMethod
		public int testJavaLang(String str) {
			return Integer.parseInt(str);
		}

		@TestMethod
		public int testParseTools(String str) {
			return ParseTools.parseInt(str);
		}
	}

}
