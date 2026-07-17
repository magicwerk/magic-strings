package org.magicwerk.strings;

import java.util.List;

import org.magictest.MagicTest;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.test.magictest.MagicTestRunner;
import org.magicwerk.brownies.tools.dev.tools.JavaOptions;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;

/**
 * Run MagicTest.
 *
 * @author Thomas Mauch
 */
public class Test {

	public static void main(String[] args) {
		new Test().run();
	}

	void run() {
		runMagicTest();

		//String className = "org.magicwerk.brownies.core.FileListerTest";
		//String outDir = "C:\\dev\\Java\\Sources\\magicwerk.origo\\Java\\Brownies\\Brownies-Core-Test\\magictest";
		//TestReporter tr = new TestReporter(className, outDir);

		//runRename();
	}

	void runMagicTest() {
		MagicTestRunner mtr = new MagicTestRunner();

		//mtr.runMagicTestEclipsePackage();

		mtr.setShowActualReport(true);
		mtr.setShowReferenceReport(false);

		String clazz = "org.magicwerk.brownies.core.strings.matcher.StringsMatcherTest";

		// MagicTest

		IList<String> magicTestArgs = GapList.create(
				"-loglevel", "trace",
				"-run",
				"-class", clazz
		//"-method", "org.org.magicwerk.brownies.core.strings.StringTruncaterTest"
		//				"-save",
		//"-delete",
		//"-missing", clazz
		//"-class", "org.magicwerk.brownies.core.classloader.ClassPathAnalyzerTest"
		//"-package", "org.magicwerk.brownies.core.values.io"
		//"org.magicwerk.brownies.core.strings.*Test"
		// Unknown class
		//"org.magicwerk.brownies.core.**.*Test"
		);

		//mtr.runMagicTestEclipse(magicTestArgs);
		//mtr.runMagicTestForkEclipse(magicTestArgs);
		//mtr.runMagicTestForkJar(magicTestArgs);

		// MagicTestNG

		// Run MagicTestNG with single class
		IList<String> magicTestNgArgsClass = GapList.create(
				"-testclass", clazz,
				"-verbose", "10", "-excludegroups", "manual,linux,magicwerk");

		// Run MagicTestNG with test suite
		IList<String> magicTestNgArgsSuite = GapList.create(
				"-verbose", "10", "-excludegroups", "manual,linux,magicwerk",
				"testng.xml"); //"C:\\Windows\\TEMP\\testng-eclipse-839192392\\testng-customsuite.xml");

		// If MagicTestNG is run in MagicTest only mode, no arguments of TestNG must be used
		IList<String> magicTestNgArgsNoTestNg = GapList.create(
				"-run",
				"-loglevel", "debug",
				"org.magicwerk.brownies.core.reflect.ReflectToolsTest");

		IList<String> magicTestNgArgs = magicTestNgArgsClass;
		//mtr.runMagicTestNgEclipse(magicTestNgArgs);
		//mtr.runMagicTestNgForkEclipse(magicTestNgArgs);
		mtr.runMagicTestNgForkJar(magicTestNgArgs);
	}

	//

	/**
	 * Run all tests in this package.
	 */
	static void runMagicTestAll() {
		//      MagicTest.runPackage(getPackageName(getCurrentClass()));
		//      MagicTest.runPackage("org.magicwerk.brownies.core");

		new MagicTest().run(new String[] { "-run", "org.magicwerk.brownies.core.**.*Test" });
	}

	static void forkMagicTestNg() {
		List<String> jvmArgs = null;
		String mainClass = "org.magictest.ng.MagicTestNG";
		List<String> args = GapList.create("-testclass", "org.magicwerk.brownies.core.FileListerTest");
		//args = GapList.create("testng.xml");

		JavaOptions opts = new JavaOptions().setJvmArgs(jvmArgs).setMainClass(mainClass).setArgs(args);
		new JavaTool().run(opts);
	}

	static void runTestNg() {
		String[] args = new String[] { "C:\\Users\\thoma\\AppData\\Local\\Temp\\testng-eclipse--875141114\\testng-customsuite.xml"
				//                "-testclass", "org.magicwerk.brownies.collections.SetListTest"
		};
		org.testng.TestNG.main(args);
	}

	static void runRename() {
		new MagicTest().run(new String[] { "-rename", "-package", "org.magicwerk.brownies.files.filemodel", "org.magicwerk.brownies.core.files.filemodel" });
		new MagicTest().run(new String[] { "-delete", "-package", "org.magicwerk.brownies.files.filemodel" });
	}

}
