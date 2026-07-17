package org.magicwerk.strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.TestValues;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringPrinter;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of class {@link StringPrinter}.
 */
public class StringPrinterTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new StringPrinterTest().run();
	}

	void run() {
		//StringPrinterJmhTest.test();
		//StringPrinterFormatJmhTest.test();
		//new StringPrinterCreateJmhTest().test();
		new LogJmhTest().test();

		//testExamples();
		//testAdd();
		//testAddConditional();
		//testConcatWithoutSeparator();
		//testConfig();
		//testIndent();
		//testPerformance();
		//testPrint();
		//testSeparator();
		//testSeparatorConcat();
		//testStringPrinter();
	}

	@Capture
	public void testExamples() {
		// Use StringPrinter to construct a URL with arguments
		{
			StringPrinter buf = new StringPrinter();
			buf.setBeginMarker("?").setElemMarker("&");
			buf.print("localhost/rest");
			LOG.info("{}", buf);

			buf.add("verbose=true");
			LOG.info("{}", buf);

			buf.add("sorted");
			LOG.info("{}", buf);
		}
		{
			StringPrinter buf = new StringPrinter();
			buf.add("localhost/rest");
			buf.setBeginMarker("?").setElemMarker("&").setPartMarker("=");
			LOG.info("{}", buf);

			buf.add("verbose=true");
			LOG.info("{}", buf);

			buf.addPartsIf("sorted", null);
			buf.addPartsIf("fast", "true");
			LOG.info("{}", buf);
		}
	}

	/**
	 * Show that directly formatting a list/map into a StinrBuilder is faster and consumes less memory that working with toString().
	 * There is not much improvement in Java 11/17, but in Java 21 the customized approach is about 120-150% times faster.
	 */
	public static class LogJmhTest extends StringJmhBenchmark {

		public LogJmhTest() {
			setJavaVersions(JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<IList<String>> lists = new CyclicSource<>(10, i -> TestValues.getStringList(i));
			CyclicSource<Map<Integer, String>> maps = new CyclicSource<>(10, i -> TestValues.getIntegerStringMap(i));
		}

		@Benchmark
		public int testListLogToString(MyState state) {
			IList<String> list = state.lists.next();
			StringBuilder buf = new StringBuilder();
			buf.append(list);
			return buf.length();
		}

		@Benchmark
		public int testListLogFormatter(MyState state) {
			IList<String> list = state.lists.next();
			StringBuilder buf = new StringBuilder();
			format(buf, list);
			return buf.length();
		}

		@Benchmark
		public int testMapLogToString(MyState state) {
			Map<Integer, String> list = state.maps.next();
			StringBuilder buf = new StringBuilder();
			buf.append(list);
			return buf.length();
		}

		@Benchmark
		public int testMapLogFormatter(MyState state) {
			Map<Integer, String> list = state.maps.next();
			StringBuilder buf = new StringBuilder();
			format(buf, list);
			return buf.length();
		}

		// Like AbstractCollection.toString()
		static <E> void format(StringBuilder buf, Collection<E> coll) {
			Iterator<E> it = coll.iterator();
			if (!it.hasNext()) {
				buf.append("[]");
				return;
			}

			buf.append('[');
			for (;;) {
				E e = it.next();
				buf.append(e == coll ? "(this Collection)" : e);
				if (!it.hasNext()) {
					buf.append(']');
					return;
				}
				buf.append(',').append(' ');
			}
		}

		// Like AbstractMap.toString()
		public <K, V> void format(StringBuilder buf, Map<K, V> map) {
			Iterator<Entry<K, V>> i = map.entrySet().iterator();
			if (!i.hasNext()) {
				buf.append("{}");
				return;
			}

			buf.append('{');
			for (;;) {
				Entry<K, V> e = i.next();
				K key = e.getKey();
				V value = e.getValue();
				buf.append(key == this ? "(this Map)" : key);
				buf.append('=');
				buf.append(value == this ? "(this Map)" : value);
				if (!i.hasNext()) {
					buf.append('}');
					return;
				}
				buf.append(',').append(' ');
			}
		}
	}

	/**
	 * Show that reuse uses somewhat less memory, but is not really faster.
	 */
	public static class StringPrinterCreateJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + ")");

			StringPrinter stringPrinter = new StringPrinter().setBeginMarker("(").setEndMarker(")").setElemMarker(":");

			IList<String> getAll() {
				return strings.getAll();
			}
		}

		@Benchmark
		public String testNew(MyState state) {
			IList<String> all = state.strings.getAll();
			StringPrinter buf = new StringPrinter().setBeginMarker("(").setEndMarker(")").setElemMarker(":");
			return buf.addAll(all).toString();
		}

		@Benchmark
		public String testReuse(MyState state) {
			IList<String> all = state.strings.getAll();
			StringPrinter buf = state.stringPrinter;
			buf.clear();
			return buf.addAll(all).toString();
		}

		@Benchmark
		public String testTemplate(MyState state) {
			IList<String> all = state.strings.getAll();
			StringPrinter buf = StringPrinter.fromConfig(state.stringPrinter);
			buf.clear();
			return buf.addAll(all).toString();
		}
	}

	/**
	 * Show that using a formatter is faster than using stream().map().
	 */
	public static class StringPrinterFormatJmhTest extends StringJmhBenchmark {

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + ")");

			IList<String> getAll() {
				return strings.getAll();
			}
		}

		static final Function<String, String> formatter = s -> "[" + s + "]";

		@Benchmark
		public String testFormatter(MyState state) {
			IList<String> all = state.strings.getAll();
			StringPrinter buf = new StringPrinter();
			buf.setFormatter(formatter);
			return buf.addAll(all).toString();
		}

		@Benchmark
		public String testStreamMap(MyState state) {
			IList<String> all = state.strings.getAll();
			StringPrinter buf = new StringPrinter();
			return buf.addAll(all.stream().map(formatter)).toString();
		}
	}

	public static class StringPrinterJmhTest extends StringJmhBenchmark {

		static int SIZE = 1000;

		@State(Scope.Thread)
		public static class MyState {
		}

		@Benchmark
		public String testStringPrinter(MyState state) {
			StringPrinter buf = new StringPrinter();
			for (int i = 0; i < SIZE; i++) {
				buf.print(i);
			}
			return buf.toString();
		}

		@Benchmark
		public String testStringBuilder(MyState state) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < SIZE; i++) {
				buf.append(i);
			}
			return buf.toString();
		}

		@Benchmark
		public String testStringAdd(MyState state) {
			String str = "";
			for (int i = 0; i < SIZE; i++) {
				str += i;
			}
			return str;
		}

	}

	@Capture
	public void testConfig() {
		StringPrinter sp = new StringPrinter();
		sp.setMarkers("<", ",", ">");
		sp.addAll("a1", "a2");
		sp.nestConfig(true);
		sp.setMarkers("[", "-", "]");
		sp.addAll("b1", "b2");
		sp.nestConfig(true);
		sp.setMarkers("{", "|", "}");
		sp.addAll("c1", "c2");
		sp.unnestConfig(true);
		sp.addAll("d1", "d2");
		sp.unnestConfig(true);
		sp.addAll("e1", "e2");
		System.out.println(sp);
	}

	@Capture
	public void testIndent() {
		StringPrinter sp;

		sp = new StringPrinter();
		sp.setIndent("(=)");
		sp.indent();
		sp.println("line1");
		sp.println("line2");
		System.out.println(sp);

		// String is not parsed and therefore second line is not indented
		sp = new StringPrinter();
		sp.setIndent("(=)");
		sp.indent();
		sp.print("line1\nline2\n");
		System.out.println(sp);

		// String is parsed and therefore second line is indented
		sp = new StringPrinter();
		sp.setDetectNewLines(true);
		sp.setIndent("(=)");
		sp.indent();
		sp.print("line1\nline2\n");
		System.out.println(sp);

		// Different indent strings
		sp = new StringPrinter();
		sp.setIndent("- ");
		sp.print("line-1");
		sp.indent();
		sp.print("line-1-1");
		sp.unindent();
		sp.println("line-2");
		sp.println("line-3");
		sp.indent();
		sp.print("line-3-1");
		sp.unindent();
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setIndent("(=)");
		sp.println("line1");
		sp.indent();
		sp.println("line2");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker("=\n");
		sp.setIndent("-");
		sp.add("line1");
		sp.add("line2");
		sp.indent();
		sp.add("line3");
		System.out.println(sp);
	}

	@Capture
	public void testPrint() {
		{
			StringPrinter sp = new StringPrinter().setMarkers("[", ",", "]");
			sp.print("before");
			sp.addAll("e0", "e1");
			System.out.println(sp);
		}
		{
			StringPrinter sp = new StringPrinter().setMarkers("[", ",", "]");
			sp.addAll("e0", "e1");
			sp.endMarker();
			sp.print("after");
			System.out.println(sp);
		}
		{
			StringPrinter sp = new StringPrinter().setMarkers("[", ",", "]");
			sp.addAll("e0", "e1").endMarker();
			sp.print("between");
			sp.addAll("e2", "e3");
			System.out.println(sp);
		}
		{
			StringPrinter sp = new StringPrinter().setMarkers("[", ",", "]");
			sp.addAll("e0", "e").skipMarker();
			sp.addAll("1", "e2", "e3");
			System.out.println(sp);
		}
	}

	@Capture
	public void testSeparator() {
		StringPrinter sp;

		// Begin/end separator are printed as there is an element
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		sp.add("a");
		System.out.println(sp);

		// Begin/end separator are not printed as there is no element
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		System.out.println(sp);

		// Begin/end separator are not printed as there is no element
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		sp.setPrintMarkersAlways(true);
		System.out.println(sp);

		// Use skipMarker to join text without marker
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		sp.add("a");
		sp.skipMarker();
		sp.add("a");
		sp.add("b");
		System.out.println(sp);

		// Use skipMarker to join text without marker, begin/end marker will be printed anyhow
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		sp.add("a");
		sp.skipMarker();
		sp.add("a");
		System.out.println(sp);

		// Use skipMarker to join text without marker, begin/end marker will be printed anyhow
		sp = new StringPrinter();
		sp.setMarkers("[", ", ", "]");
		sp.addIf(null);
		sp.skipMarker();
		sp.addIf("a");
		sp.addIf("b");
		System.out.println(sp);
	}

	@Capture
	public void testSeparatorConcat() {
		StringPrinter sp;

		// Concat the two arguments
		sp = new StringPrinter();
		sp.setElemMarker("-");
		sp.addAllIf("a", "b");
		System.out.println(sp);

		// One argument is null, so it is ignored and no concatenation is done
		sp = new StringPrinter();
		sp.setElemMarker("-");
		sp.addAllIf(null, "b");
		System.out.println(sp);

		// One argument is an empty string, so concatenation is done
		sp = new StringPrinter();
		sp.setElemMarker("-");
		sp.addAllIf("", "b");
		System.out.println(sp);
	}

	@Capture
	public void testConcatWithoutSeparator() {
		StringPrinter sp;

		sp = new StringPrinter();
		sp.setElemMarker(",");
		sp.addPartsIf("severity", ":", "info");
		sp.addPartsIf("message", ":", "text");
		sp.addPartsIf("error", ":", "stacktrace");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker(",");
		sp.addPartsIf("severity", ":", "info");
		sp.addPartsIf("message", ":", null);
		sp.addPartsIf("error", ":", "stacktrace");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker(",");
		sp.setPartMarker(":");
		sp.addPartsIf("severity", "info");
		sp.addPartsIf("message", "text");
		sp.addPartsIf("error", "stacktrace");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker(",");
		sp.setPartMarker(":");
		sp.addPartsIf("severity", "info");
		sp.addPartsIf("message", null);
		sp.addPartsIf("error", "stacktrace");
		System.out.println(sp);
	}

	@Capture
	@SuppressWarnings({ "all" })
	public void testAddConditional() {
		String[] sa0 = new String[] { "a", "b", "c" };
		IList<String> sl0 = GapList.create(sa0);
		Stream<String> ss0 = Arrays.stream(sa0);

		String[] sa1 = new String[] { "a", null, "c" };
		IList<String> sl1 = GapList.create(sa1);
		Stream<String> ss1 = Arrays.stream(sa1);

		StringPrinter sp = new StringPrinter().setElemMarker("-").setPartMarker(":");
		sp.add("X");
		sp.addPartsIf(sa0); // Suppressed warning
		sp.addPartsIf((Object[]) sa0);
		sp.addPartsIf(sl0);
		sp.addPartsIf(ss0);
		sp.add("X");
		sp.addPartsIf(sa1); // Suppressed warning
		sp.addPartsIf((Object[]) sa1);
		sp.addPartsIf(sl1);
		sp.addPartsIf(ss1);
		sp.add("X");
		System.out.println(sp);
	}

	@Capture
	public void testAdd() {
		StringPrinter sp;

		sp = new StringPrinter();
		sp.setElemMarker("|");
		sp.add("a");
		sp.add(null);
		sp.add("b");
		sp.add("");
		sp.add("c");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker("|");
		sp.setNullString("NULL");
		sp.add("a");
		sp.add(null);
		sp.add("b");
		sp.add("");
		sp.add("c");
		System.out.println(sp);

		sp = new StringPrinter();
		sp.setElemMarker("|");
		sp.setNullString(null);
		sp.add("a");
		sp.add(null);
		sp.add("b");
		sp.add("");
		sp.add("c");
		System.out.println(sp);
	}

	@Trace(traceMethod = "/.*/")
	public void testStatic() {
		StringPrinter.formatArray(GapList.create());
		StringPrinter.formatArray(GapList.create(1));
		StringPrinter.formatArray(GapList.create(1, 2));
	}

	@Capture
	public static void testStringPrinter() {
		StringPrinter sp1 = new StringPrinter();
		print(sp1);
		sp1.clear();

		sp1.setEol("|");
		sp1.setIndent("-");
		print(sp1);

		StringPrinter sp2 = new StringPrinter().setMarkers("(", ",", ")");
		sp2.addIf("a");
		System.out.println(sp2.toString());

		StringPrinter sp3 = new StringPrinter().setMarkers("(", ",", ")");
		sp3.addIf("a");
		sp3.addIf("b");
		System.out.println(sp3.toString());

		StringPrinter sp4 = new StringPrinter().setMarkers("(", ",", ")");
		sp4.addPartsIf("key1", "=", "val1");
		sp4.addPartsIf("key2", "=", null);
		System.out.println(sp4.toString());
	}

	static void print(StringPrinter sp) {
		sp.println("H1");
		sp.indent();
		sp.println("H1.1");
		sp.indent();
		sp.println("Text 1.1");
		sp.unindent();
		sp.println("H1.2");
		sp.indent();
		sp.println("Text 1.2");
		sp.unindent();
		sp.unindent();

		String str = sp.toString();
		System.out.println(str);
	}

}
