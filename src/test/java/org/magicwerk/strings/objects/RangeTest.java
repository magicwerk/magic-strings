package org.magicwerk.strings.objects;

import org.magictest.client.Trace;
import org.magicwerk.brownies.core.objects.NumberRange;
import org.magicwerk.brownies.core.objects.Range;

/**
 * Test of result classes {@link Range} and {@link NumberRange}.
 */
public class RangeTest {

	public static void main(String[] args) {
		new RangeTest().run();
	}

	void run() {
		testNumberRange();
	}

	@Trace(traceClass = "NumberRange", traceMethod = "/.*/")
	public void testNumberRange() {
		NumberRange<Integer> nr = new NumberRange<>(1, 9);
		nr.range();
	}
}
