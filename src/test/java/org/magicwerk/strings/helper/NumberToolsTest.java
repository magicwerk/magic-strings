package org.magicwerk.strings.helper;

import java.math.BigInteger;

import org.magictest.client.Format;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.NumberTools;

/**
 * Test of class {@link NumberTools}.
 */
public class NumberToolsTest {

	public static void main(String[] args) {
		new NumberToolsTest().run();
	}

	void run() {
		testGetCommonNumberClass();
	}

	@Trace
	public void testGetCommonNumberClass() {
		NumberTools.getCommonNumberClass(int.class, int.class);
		NumberTools.getCommonNumberClass(int.class, Integer.class);
		NumberTools.getCommonNumberClass(int.class, long.class);
		NumberTools.getCommonNumberClass(int.class, float.class);
		NumberTools.getCommonNumberClass(int.class, double.class);
		NumberTools.getCommonNumberClass(BigInteger.class, double.class);
	}

	@Trace
	public void testCreateAnyNumber() {
		NumberTools.createAnyNumber(int.class);
		NumberTools.createAnyNumber(Integer.class);
	}

	@Trace(formats = { @Format(apply = Trace.RESULT, formatter = "formatNumber") })
	public void testConvertNumberIf() {
		NumberTools.convertNumberIf(0, int.class);
		NumberTools.convertNumberIf(1000, byte.class);
		NumberTools.convertNumberIf(null, int.class);
		NumberTools.convertNumberIf(null, Integer.class);

		GapList<Class<? extends Number>> cs = GapList.create(byte.class, int.class);
		NumberTools.convertNumberIf(123, cs);
		NumberTools.convertNumberIf(1234, cs);
	}

	@Trace(formats = { @Format(apply = Trace.RESULT, formatter = "formatNumber") })
	public void testConvertNumber() {
		NumberTools.convertNumber(0, int.class);
		NumberTools.convertNumber(1000, byte.class);
		NumberTools.convertNumber(null, int.class);
		NumberTools.convertNumber(null, Integer.class);

		GapList<Class<? extends Number>> cs = GapList.create(byte.class, int.class);
		NumberTools.convertNumber(123, cs);
		NumberTools.convertNumber(1234, cs);
	}

	static String formatNumber(Number n) {
		return n + " " + ((n != null) ? n.getClass() : "");
	}

	@Trace(traceMethod = "/is.*/")
	public void testIsNumber() {
		NumberTools.isByte(123);
		NumberTools.isByte(1234);
		NumberTools.isShort(1234);
		NumberTools.isShort(123456);
		NumberTools.isInt(123456);
	}

}
