package org.magicwerk.strings.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.magictest.client.Capture;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.helper.PrintTools;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link PrintTools}.
 */
public class PrintToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new PrintToolsTest().run();
	}

	void run() {
	}

	// toString:
	// Issues with toString():
	// - obj.toString() will throw a NPE if obj is null, where String.valueOf(obj) returns "null"
	// - Content of array is not printed but, but a reference to the object like [Ljava.lang.Object;@6e3c1e69
	// - Printing collections does not properly handled cycles: siple cycles are handled by printing "(this collection)", complex result in StackOverflowError
	// - generally toString() may throw exceptions which are not handled, e.g.
	//   - Collection.toString() can throw StackOverflowError with cycles
	//   - other implementations may throw NPEs etc.
	// - Logging frameworks like SLF4J therefore protect generation of string output with try/catch and report the generated error

	@Capture(formatter = "format")
	public void testToStringJdk() {
		testToString(PrintToolsTest::printToStringJdk);
	}

	static void printToStringJdk(Object obj) {
		try {
			System.out.println(obj);
		} catch (Throwable t) {
			System.out.println("Exception on toString() of type " + obj.getClass() + ": " + t.getClass().getSimpleName());
		}
	}

	@Capture
	public void testToString() {
		testToString(PrintToolsTest::printToString);
	}

	static void printToString(Object obj) {
		try {
			String str = PrintTools.toString(obj);
			System.out.println(str);
		} catch (Throwable t) {
			System.out.println("Exception on toString() of type " + obj.getClass() + ": " + t.getClass().getSimpleName());
		}
	}

	static final StringReplacer FORMATTER = StringReplacer.builder().replaceRegex("(Ljava.lang.Object;@)\\w+", "{1}").build();

	static String format(String str) {
		return FORMATTER.replace(str);
	}

	static void testToString(Consumer<Object> printer) {
		{
			// JDK: Prints "this collection"
			List<Object> list = new ArrayList<>();
			list.add(list);
			printer.accept(list);
		}
		{
			// JDK: Fails with java.lang.StackOverflowError
			List<Object> list = new ArrayList<>();
			List<Object> list2 = new ArrayList<>();
			list.add(list2);
			list2.add(list);
			printer.accept(list);
		}

		{
			// Empty list
			List<Object> list = new ArrayList<>();
			printer.accept(list);
		}
		{
			// List with 1 element
			List<Object> list = new ArrayList<>();
			list.add("obj");
			printer.accept(list);
		}
		{
			// List with 2 elements
			List<Object> list = new ArrayList<>();
			list.add("obj0");
			list.add("obj1");
			printer.accept(list);
		}
		{
			// Nested list 
			List<Object> list = new ArrayList<>();
			List<Object> list2 = new ArrayList<>();
			list2.add("obj");
			list.add(list2);
			printer.accept(list);
		}

		{
			// Empty array
			Object[] arr = new Object[0];
			printer.accept(arr);
		}
		{
			// Array with 1 element
			Object[] arr = new Object[1];
			arr[0] = "obj";
			printer.accept(arr);
		}
		{
			// Array with 2 elements
			Object[] arr = new Object[2];
			arr[0] = "obj0";
			arr[1] = "obj1";
			printer.accept(arr);
		}
		{
			// Nested array 
			Object[][] arr = new Object[1][];
			Object[] arr2 = new Object[1];
			arr2[0] = "obj";
			arr[0] = arr2;
			printer.accept(arr);
		}

	}

	static void testToStringSlf4JManual() {
		// Logging reports error
		// SLF4J: Failed toString() invocation on an object of type [java.util.ArrayList]
		// Reported exception: java.lang.StackOverflowError
		List<Object> list = new ArrayList<>();
		List<Object> list2 = new ArrayList<>();
		list.add(list2);
		list2.add(list);
		LOG.info("{}", list);
	}

}