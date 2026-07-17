package org.magicwerk.strings.helper;

import java.util.Arrays;
import java.util.List;

import org.magictest.client.Trace;
import org.magicwerk.strings.helper.ArrayTools;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Test of class {@link ArrayTools}.
 */
public class ArrayToolsTest {

	public static void main(String[] args) {
		new ArrayToolsTest().run();
	}

	void run() {
		//testArrayAsList();
		testToList();
	}

	static String s0 = "a";
	static String[] s1 = new String[] { "a" };
	static String[][] s2 = new String[][] { { "a" } };

	@Trace(traceMethod = "/.*/")
	public static void testConvert() {
		Object[] objArr = new Object[] { 1, 2, 3 };
		Integer[] intArr = ArrayTools.convert(objArr, Integer.class);

		// java.lang.ArrayStoreException
		@SuppressWarnings("unused")
		String[] strArr = ArrayTools.convert(objArr, String.class);

		// null
		@SuppressWarnings("unused")
		String[] strArr2 = ArrayTools.convertIf(objArr, String.class);

		// Convert with converter
		@SuppressWarnings("unused")
		String[] strArr3 = ArrayTools.convert(intArr, String.class, i -> Integer.toString(i));
	}

	@Trace
	public static void testGetNumDimensions() {
		ArrayTools.getNumDimensions(s0);
		ArrayTools.getNumDimensions(s1);
		ArrayTools.getNumDimensions(s2);
	}

	@Trace
	public static void testGetComponentType() {
		ArrayTools.getComponentType(s0);
		ArrayTools.getComponentType(s1);
		ArrayTools.getComponentType(s2);
	}

	@Trace
	public static void testGetBaseComponentType() {
		ArrayTools.getBaseComponentType(s0);
		ArrayTools.getBaseComponentType(s1);
		ArrayTools.getBaseComponentType(s2);
	}

	@Trace(traceClass = "java.util.Arrays", traceMethod = "asList")
	public void testArraysAsListJdk() {
		// ok
		List<?> li = Arrays.asList(1, 2, 3);
		CheckTools.check(li.size() == 3);

		// wrong
		int[] ia = new int[] { 1, 2, 3 };
		List<?> lia = Arrays.asList(ia);
		CheckTools.check(lia.size() == 1);

		// wrong
		Object oa = ia;
		List<?> loa = Arrays.asList(oa);
		CheckTools.check(loa.size() == 1);
	}

	@Trace
	public static void testToList() {
		int[] int1Dim = new int[] { 1, 2 };
		int[][] int2Dim = new int[][] { { 1, 2 }, { 3, 4 } };
		int[][][] int3Dim = new int[][][] { { { 1, 2 }, { 3, 4 } }, { { 5, 6 }, { 7, 8 } } };

		ArrayTools.toList(int1Dim, true);
		ArrayTools.toList(int2Dim, true);
		ArrayTools.toList(int3Dim, true);

		Integer[] integer1Dim = new Integer[] { 1, 2 };
		Integer[][] integer2Dim = new Integer[][] { { 1, 2 }, { 3, 4 } };
		Integer[][][] integer3Dim = new Integer[][][] { { { 1, 2 }, { 3, 4 } }, { { 5, 6 }, { 7, 8 } } };

		ArrayTools.toList(integer1Dim, true);
		ArrayTools.toList(integer2Dim, true);
		ArrayTools.toList(integer3Dim, true);

		String[] string1Dim = new String[] { "a1", "b2" };
		String[][] string2Dim = new String[][] { { "a1", "b2" }, { "c3", "d4" } };
		String[][][] string3Dim = new String[][][] { { { "a1", "b2" }, { "c3", "d4" } }, { { "e5", "f6" }, { "g7", "h8" } } };

		ArrayTools.toList(string1Dim, true);
		ArrayTools.toList(string2Dim, true);
		ArrayTools.toList(string3Dim, true);

		List<?> li = ArrayTools.toList(1, 2, 3);
		CheckTools.check(li.size() == 3);

		int[] ia = new int[] { 1, 2, 3 };
		List<?> lia = ArrayTools.toList(ia);
		CheckTools.check(lia.size() == 3);

		Object oa = ia;
		List<?> loa = ArrayTools.toList(oa);
		CheckTools.check(loa.size() == 3);
	}

	@Trace
	public static void testGet() {
		int[] ia = new int[] { 1, 2, 3 };
		String[] sa = new String[] { "a", "b", "c" };

		ArrayTools.get(ia, 0);
		ArrayTools.get(sa, 1);
	}
}
