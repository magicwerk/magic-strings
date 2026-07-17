package org.magicwerk.strings.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.ObjectTools;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link ObjectTools}.
 */
public class ObjectToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ObjectToolsTest().run();
	}

	void run() {
		//testCast();
		testEquals();
	}

	@Trace(traceMethod = "/cast|castOr/")
	public static void testCast() {
		Object n = null;
		Object o = new Object();
		String s = "abc";
		Integer i = 123;

		ObjectTools.cast(n, String.class);
		ObjectTools.cast(o, String.class);
		ObjectTools.cast(s, String.class);
		ObjectTools.cast(i, String.class);

		ObjectTools.castOr(n, String.class, "default");
		ObjectTools.castOr(o, String.class, "default");
		ObjectTools.castOr(s, String.class, "default");
		ObjectTools.castOr(i, String.class, "default");
	}

	@SuppressWarnings("unused")
	@Trace(traceMethod = "/get|getOr/")
	public static void testGet() {
		Object n = null;
		Object o = new Object();
		String s = "abc";
		Object s2 = s;
		Integer i = 123;

		int l2 = ObjectTools.getOr(s2, String.class, String::length, -1);
		int l1 = ObjectTools.getOr(i, String.class, String::length, -1);
	}

	@Trace
	public static void testIsOneOf() {
		String[] args = new String[] { "alpha", "beta", "gamma" };
		String[] argsNull = new String[] { "alpha", "beta", "gamma", null };

		String arg = "alpha";
		ObjectTools.isOneOf(arg, args);
		ObjectTools.isOneOf("beta", args);
		ObjectTools.isOneOf("gamma", args);
		ObjectTools.isOneOf("xyz", args);
		ObjectTools.isOneOf((String) null, args);
		ObjectTools.isOneOf((String) null, argsNull);
		ObjectTools.isOneOf("alpha");
		ObjectTools.isOneOf((String) null);
	}

	@Trace
	public static void testIndexOneOf() {
		String[] args = new String[] { "alpha", "beta", "gamma" };
		String[] argsNull = new String[] { "alpha", "beta", "gamma", null };

		ObjectTools.indexOneOf("alpha", args);
		ObjectTools.indexOneOf("beta", args);
		ObjectTools.indexOneOf("gamma", args);
		ObjectTools.indexOneOf("xyz", args);
		ObjectTools.indexOneOf((String) null, args);
		ObjectTools.indexOneOf((String) null, argsNull);
		ObjectTools.indexOneOf("alpha");
		ObjectTools.indexOneOf((String) null);
	}

	@Trace
	public static void testEquals() {
		byte[] b1 = new byte[] { 1, 2 };
		byte[] b2 = new byte[] { 1, 2 };
		byte[] b3 = new byte[] { 0, 0 };

		Object[] a1 = new Object[] { "a", "b" };
		Object[] a2 = new Object[] { "a", "b" };
		Object[] a3 = new Object[] { "a", "X" };

		ArrayList<String> l1 = new ArrayList<String>();
		l1.add("a");
		l1.add("b");
		ArrayList<String> l2 = new ArrayList<String>();
		l2.add("a");
		l2.add("b");
		ArrayList<String> l3 = new ArrayList<String>();
		l3.add("a");
		l3.add("X");

		// equals
		ObjectTools.equals(b1, b1);
		ObjectTools.equals(b1, b2);
		ObjectTools.equals(b1, b3);

		// hashCode
		int hb1 = ObjectTools.hashCode(b1);
		int hb2 = ObjectTools.hashCode(b2);
		int hb3 = ObjectTools.hashCode(b3);
		assert (hb1 == hb2);
		assert (hb1 != hb3);

		// equals
		ObjectTools.equals(a1, a1);
		ObjectTools.equals(a1, a2);
		ObjectTools.equals(a1, a3);

		// hashCode
		int ha1 = ObjectTools.hashCode(a1);
		int ha2 = ObjectTools.hashCode(a2);
		int ha3 = ObjectTools.hashCode(a3);
		assert (ha1 == ha2);
		assert (ha1 != ha3);

		// equals
		ObjectTools.equals(l1, l1);
		ObjectTools.equals(l1, l2);
		ObjectTools.equals(l1, l3);

		// hashCode
		int hl1 = ObjectTools.hashCode(l1);
		int hl2 = ObjectTools.hashCode(l2);
		int hl3 = ObjectTools.hashCode(l3);
		assert (hl1 == hl2);
		assert (hl1 != hl3);

		//
		ObjectTools.equals(null, null);
		ObjectTools.equals(null, a1);

		String s1 = "abc";
		String s2 = "def";
		ObjectTools.equals(s1, s1);
		ObjectTools.equals(s1, s2);

		ObjectTools.equals(s1, l1);

		// Note that java.util.Objects does not handle arrays properly
		assert (Objects.equals(a1, a1));
		assert (!Objects.equals(a1, a2));

		// Map (HashMap / TreeMap)
		HashMap<Integer, String> hashMap = new HashMap<>();
		TreeMap<Integer, String> treeMap = new TreeMap<>();
		hashMap.put(1, "a");
		hashMap.put(2, "b");
		treeMap.put(1, "a");
		treeMap.put(2, "b");
		assert Objects.equals(hashMap, treeMap);
		assert Objects.equals(treeMap, hashMap);
		ObjectTools.equals(hashMap, treeMap);
		ObjectTools.equals(treeMap, hashMap);
	}

	@Trace
	public static void testEqualsArray() {
		int[] i1 = new int[] { 1 };
		int[] i1b = new int[] { 1 };
		int[] i2 = new int[] { 2 };

		String[] s1 = new String[] { "a" };
		String[] s1b = new String[] { "a" };
		String[] s2 = new String[] { "b" };

		int[][] ii1 = new int[][] { { 1 }, { 2 } };
		int[][] ii1b = new int[][] { { 1 }, { 2 } };
		int[][] ii2 = new int[][] { { 1 } };

		Object[] oi1 = new Object[] { new int[] { 1 }, new int[] { 2 } };
		Object[] oi2 = new Object[] { new int[] { 1 }, new int[] { 2 } };

		ObjectTools.equalsArray(i1, i1);
		ObjectTools.equalsArray(i1, i1b);
		ObjectTools.equalsArray(i1, i2);

		ObjectTools.equalsArray(s1, s1);
		ObjectTools.equalsArray(s1, s1b);
		ObjectTools.equalsArray(s1, s2);

		ObjectTools.equalsArray(ii1, ii1);
		ObjectTools.equalsArray(ii1, ii1b);
		ObjectTools.equalsArray(ii1, ii2);

		ObjectTools.equalsArray(oi1, oi2);
	}

	@Trace
	public static void testCompare() {
		ObjectTools.compare("a", "b");
		ObjectTools.compare("a", "a");
		ObjectTools.compare("b", "a");

		ObjectTools.compare("a", null);
		ObjectTools.compare(null, "a");

		ObjectTools.compare(null, null);

		// does not compile
		//ObjectTools.compare("a", 1);
	}

	@Trace
	public static void testCompareFields() {
		ObjectTools.compareFields("a1", "a2", "b1", "b2");
		ObjectTools.compareFields("a2", "a1", "b1", "b2");
		ObjectTools.compareFields("a1", "a1", "b1", "b2");
		ObjectTools.compareFields("a1", "a1", "b1", "b1");

		ObjectTools.compareFields("a1", 1);
		ObjectTools.compareFields("a1", new Object());

		ObjectTools.compareFields("a1", "a2", "X");
	}

}
