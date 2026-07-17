package org.magicwerk.strings;

import org.magictest.client.Capture;
import org.magictest.client.Report;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.strings.GapString;
import org.magicwerk.strings.IString;
import org.magicwerk.strings.IStringTransformer;
import org.magicwerk.strings.ReturnMode;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkTestBase;

/**
 * Test class for general string tests.
 */
public abstract class StringBenchmarkTestBase extends JmhBenchmarkTestBase {

	@Override
	@Capture
	public void testBenchmarks() {
		// Add @Capture
		super.testBenchmarks();
	}

	boolean runBenchmark = false;
	boolean runBenchmarkFast = false;
	boolean runBenchmarkShowHtml = false;

	TestExecution test(Class<?> c) {
		JmhBenchmarkCreator jbc = new JmhBenchmarkCreator();
		jbc.setTestClass(c);
		jbc.setRunBenchmark(runBenchmark);
		jbc.setRunBenchmarkFast(runBenchmarkFast);
		jbc.setRunBenchmarkShowHtml(runBenchmarkShowHtml);
		return jbc.run();
	}

	/** 
	 * Test string transformation using the specified builder with all inputs, all ReturnModes and verify that
	 * apply() and applyInline() behave correctly.
	 */
	void testStringTranformerGeneric(IStringTransformerBuilder builder, IList<String> inputs) {
		for (String str : inputs) {
			// Variable unchanged will be set on first return mode RETURN_UNCHANGED
			// and used for checking of modes RETURN_NULL and THROW_EXCEPTION
			Boolean unchanged = null;
			for (ReturnMode rm : ReturnMode.values()) {
				// Create transformer
				builder.setReturnMode(rm);
				IStringTransformer sp = builder.build();

				// Call apply()
				String s1 = null;
				boolean e1 = false;
				try {
					s1 = sp.apply(str);
				} catch (Exception e) {
					e1 = true;
				}

				// Call applyInline()
				String s2 = null;
				boolean e2 = false;
				try {
					IString mutable = new GapString(str);
					sp.applyInline(mutable);
					s2 = mutable.toString();

				} catch (Exception e) {
					e2 = true;
				}

				CheckTools.check(ObjectTools.equals(e1, e2));
				Report.printCapture("{} -> {}", str, (e1) ? "Exception" : s1);

				if (rm == ReturnMode.RETURN_UNCHANGED) {
					assert unchanged == null;
					unchanged = s1.equals(str);
					if (unchanged) {
						// Check that no new string has been created
						CheckTools.check(s1 == str);
						CheckTools.check(s2.toString().equals(str));
					}
				} else if (rm == ReturnMode.RETURN_NULL) {
					if (unchanged) {
						CheckTools.check(s1 == null);
						CheckTools.check(s2.isEmpty());
					}

				} else if (rm == ReturnMode.THROW_EXCEPTION) {
					if (unchanged) {
						CheckTools.check(e1);
						CheckTools.check(e2);
					}
				}
				if (!unchanged) {
					CheckTools.check(ObjectTools.equals(s1, s2));
				}
			}
		}
	}

}
