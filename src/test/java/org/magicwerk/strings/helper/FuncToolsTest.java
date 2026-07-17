package org.magicwerk.strings.helper;

import static org.magicwerk.strings.helper.FuncTools.max;
import static org.magicwerk.strings.helper.FuncTools.min;
import static org.magicwerk.strings.helper.FuncTools.nvl;

import java.util.Optional;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.helper.FuncTools;
import org.magicwerk.strings.helper.FuncTools.MapMode;
import org.slf4j.Logger;

/**
 * Test of class {@link FuncTools}.
 */
public class FuncToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new FuncToolsTest().run();
	}

	void run() {
		testMap();
	}

	@Trace
	@SuppressWarnings("unused")
	public void testCastIf() {
		String s = "abc";
		Integer i = 123;
		Object o = s;

		String s2 = FuncTools.castIf(o, String.class);
		Integer i2 = FuncTools.castIf(o, Integer.class);
	}

	@Trace
	@SuppressWarnings("unused")
	public void testGetCastIf() {
		String s = "abc";
		Integer i = 123;
		Object o = s;

		Optional<Object> op1 = Optional.of(s);
		Optional<Object> op2 = Optional.of(i);
		Optional<Object> op3 = Optional.empty();

		String s2 = FuncTools.getCastIf(op1, String.class);
		FuncTools.getCastIf(op2, String.class);
		FuncTools.getCastIf(op3, String.class);
	}

	@Trace
	public void testMapIndex() {
		FuncTools.mapIndex(1, "a", "b", "c");
		FuncTools.mapIndex(9, "a", "b", "c");
	}

	@Trace
	public void testMap() {
		FuncTools.map(false, "a", true, "b");
		FuncTools.map(false, "a", false, "b", "c");
		FuncTools.map(false, "a", false, "b");
	}

	@Capture
	public void testMapWithMode() {
		// does not compile
		//String s = map(null, MapMode.NULL, "a", 1);

		// MapMode.NULL
		// Mapping found:
		doMap(String.class, "a", MapMode.NULL, "a", "a1");
		doMap(String.class, null, MapMode.NULL, null, "x");
		// No mapping found, use default mapping: return null
		doMap(String.class, "b", MapMode.NULL, "a", "a1");
		// Error: wrong number of arguments
		doMap(String.class, "a", MapMode.NULL, "a", "a1", "default");
		// Error: class cast exception
		doMap(String.class, "b", MapMode.NULL, "a", "a1", "b", 1);
		System.out.println();

		// MapMode.ERROR
		// Mapping found:
		doMap(String.class, "a", MapMode.ERROR, "a", "a1");
		doMap(String.class, null, MapMode.ERROR, null, "x");
		// No mapping found, use default mapping: error
		doMap(String.class, "b", MapMode.ERROR, "a", "a1");
		// Error: wrong number of arguments
		doMap(String.class, "a", MapMode.ERROR, "a", "a1", "default");
		// Error: class cast exception
		doMap(String.class, "b", MapMode.ERROR, "a", "a1", "b", 1);
		System.out.println();

		// MapMode.INPUT
		// Mapping found:
		doMap(String.class, "a", MapMode.INPUT, "a", "a1");
		doMap(String.class, null, MapMode.INPUT, null, "x");
		// No mapping found, use default mapping: input value
		doMap(String.class, "b", MapMode.INPUT, "a", "a1");
		// Error: wrong number of arguments
		doMap(String.class, "a", MapMode.INPUT, "a", "a1", "default");
		// Error: class cast exception
		doMap(String.class, "b", MapMode.INPUT, "a", "a1", "b", 1);
		System.out.println();

		// MapMode.DEFAULT
		// Mapping found:
		doMap(String.class, "a", MapMode.DEFAULT, "a", "a1", "default");
		doMap(String.class, null, MapMode.DEFAULT, null, "x", "default");
		// No mapping found, use default mapping: input value
		doMap(String.class, "b", MapMode.DEFAULT, "a", "a1", "default");
		// Error: wrong number of arguments
		doMap(String.class, "a", MapMode.DEFAULT, "a", "a1", "default");
		// Error: class cast exception
		doMap(String.class, "b", MapMode.DEFAULT, "a", "a1", "b", 1, "default");
		doMap(String.class, "c", MapMode.DEFAULT, "a", "a1", "b", "b2", 2);
	}

	static <K, V, R> void doMap(Class<R> result, K input, MapMode mode, K key1, V val1, Object... keyVals) {
		StringPrinter buf = new StringPrinter().setElemMarker(" ");
		buf.addAll("input:", input, "mode:", mode, "mapping:", key1, val1);
		buf.add(keyVals);
		buf.add("->");
		try {
			Object r = FuncTools.map(input, mode, key1, val1, keyVals);
			result.cast(r);
			buf.add(r);
		} catch (Exception e) {
			buf.addPartsIf("Error: ", e.getMessage());
		}
		System.out.println(buf);
	}

	@Trace
	public void testNvl() {
		nvl("abc", "nvl");
		nvl(null, "nvl");
		nvl("abc", null);
		// Without cast, the wrong method accepting a Supplier is called
		nvl(null, (Object) null);

		nvl("abc", "def", "nvl");
		nvl(null, "def", "nvl");
		nvl("abc", "def", null);
		nvl(null, "def", null);

		nvl();
		nvl((Object) null);
		nvl("abc");
	}

	@Trace
	public void testMax() {
		max("abc", "def");
		max("abc", "ghi", "def");
		max((String) null, "nvl");
		max("abc", null);
		max((String) null, null);
	}

	@Trace
	public void testMin() {
		min("abc", "def");
		min("abc", "ghi", "def");
		min((String) null, "nvl");
		min("abc", null);
		min((String) null, null);
	}

}
