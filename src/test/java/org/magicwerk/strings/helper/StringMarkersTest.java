package org.magicwerk.strings.helper;

import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.StringTools;
import org.magicwerk.strings.helper.StringMarkers;
import org.slf4j.Logger;

/**
 * Test of class {@link StringTools}.
 */
public class StringMarkersTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringMarkersTest().run();
	}

	void run() {
		//testNormalizeEOL();
		//testLineIterator();
		//testUnesacpeJava();
		//testGetDisplayName();
		//testJoinFunctionality();
		//testSplitFunctionality();
		//testPerformancePad();
		//testSubstringBefore();
		//testSubstringAfter();

		//new StringToolsConcatJmhTest().test();
		//new CommonPrefixJmhTest().test();
		//ReturnIndexOrMatchJmhTest.test();		
		//ReturnLengthOrStringJmhTest.test();
		//StringSubstitutorTestPerformanceJmh.test();
		//StringEscapeUtilsTestPerformanceJmh.test();
		//StringBuilderTestPerformanceJmh.test();
		//ContainsAnyTestJmh.test();
		//ReplaceCharsTestJmh.test();
		//StringTestPerformanceJmh.test();
		//SplitTestPerformanceJmh.test();
	}

	@Trace
	public static void testGetMarkerString() {
		StringMarkers.getMarkerString("abcde", "x", "-");
		StringMarkers.getMarkerString("abcde", "b", "-");
	}

	@Trace
	public static void testIsMarkerString() {
		StringMarkers.isMarkerString("abcde", "x");
		StringMarkers.isMarkerString("abcde", "b");
	}

}
