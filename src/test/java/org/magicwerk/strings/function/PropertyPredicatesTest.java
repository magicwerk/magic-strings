package org.magicwerk.strings.function;

import java.util.Collection;
import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.function.PropertyPredicates;
import org.magicwerk.brownies.core.function.PropertyPredicates.NamedExplainingPredicate;
import org.magicwerk.brownies.core.function.PropertyPredicates.NamedListExplainingPredicate;
import org.magicwerk.brownies.core.function.PropertyPredicates.PropertyPredicate;
import org.magicwerk.brownies.core.function.PropertyPredicates.PropertyPredicateResult;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.function.MultiPredicate;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link PropertyPredicates}.
 */
public class PropertyPredicatesTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new PropertyPredicatesTest().run();
	}

	void run() {
		test();
	}

	@Capture
	public void test() {
		IList<MyClass> elems = GapList.create(
				// Ok
				new MyClass("static class,Holder,static final f0=CdiUtil.get()"),
				new MyClass("static class,Holder,static final f0=CdiUtil.get(),static final f1=CdiUtil.get()"),
				// Error
				// - wrong name
				new MyClass("static class,Holdr,static final f0=CdiUtil.get()"),
				// - wrong field
				new MyClass("static class,Holder,final f0=CdiUtil.get()"),
				new MyClass("static class,Holder,static final f0=CdiUtil.get(),final f1=CdiUtil.get()"),
				new MyClass("static class,Holder,final f0=CdiUtil.get(),final f1=CdiUtil.get()")
		//
		);

		Predicate<MyClass> p0 = new NamedExplainingPredicate.Builder<MyClass>("className", c -> c.getName().endsWith("Holder"))
				.setExplain(c -> "Name is: " + c.getName()).build();

		Predicate<MyClass> p1 = new NamedExplainingPredicate.Builder<MyClass>("classFieldsStatic", c -> !c.getFields().containsIf(f -> !f.contains("static")))
				.setExplainFalse(c -> "Field is not static: " + c.getFields().getIf(f -> !f.contains("static"))).build();

		Predicate<MyClass> p2 = new NamedListExplainingPredicate.Builder<MyClass, String>(
				"classFieldsStatic2", c -> c.getFields(), f -> f.contains("static"), MultiPredicate.Mode.ALL)
						.setExplainFalse(f -> "Field is not static: " + f).build();

		PropertyPredicate<MyClass> pp = new PropertyPredicate<>("EvalClass", GapList.create(p0, p1, p2));
		eval(pp, elems);
	}

	<T> void eval(PropertyPredicate<T> pp, Collection<T> elems) {
		for (T elem : elems) {
			PropertyPredicateResult<T> r = PropertyPredicates.eval(pp, elem);
			LOG.info("{}", r.formatDetails());
		}
	}

	/** Class {@link MyClass} is a dummy implementation of a class like structure for test which can be configured by a simple string */
	static class MyClass {
		static final StringSplitter splitter = StringSplitter.builder().setFindChar(',').build();

		IList<String> strs;

		MyClass(String desc) {
			strs = splitter.split(desc);
		}

		String getType() {
			return strs.get(0);
		}

		String getName() {
			return strs.get(1);
		}

		IList<String> getFields() {
			return strs.getAll(2, strs.size() - 2);
		}

		@Override
		public String toString() {
			return "Class " + getName();
		}
	}

}
