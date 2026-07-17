package org.magicwerk.strings.function;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.helper.PrintTools;

/**
 * Class {@link Predicates} contains several implementations of {@link Predicate} like allow, deny, and, or, not.
 * <p>
 * All predicates produce a explanatory toString() representation (compared to lambdas).
 */
public class Predicates {

	public static class AllowPredicate<T> implements Predicate<T> {
		@Override
		public boolean test(T elem) {
			return true;
		}

		@Override
		public String toString() {
			return "AllowPredicate";
		}
	}

	public static class DenyPredicate<T> implements Predicate<T> {
		@Override
		public boolean test(T elem) {
			return false;
		}

		@Override
		public String toString() {
			return "DenyPredicate";
		}
	}

	public static class DenyBiPredicate<T, U> implements BiPredicate<T, U> {
		@Override
		public boolean test(T e1, U e2) {
			return false;
		}

		@Override
		public String toString() {
			return "DenyBiPredicate";
		}
	}

	/** Class {@link ValuePredicate} always returns the specified {@link #value}, ignoring the value passed to {@link #test} */
	public static class ValuePredicate<T> implements Predicate<T> {
		boolean value;

		public ValuePredicate(boolean value) {
			this.value = value;
		}

		public boolean getValue() {
			return value;
		}

		@Override
		public boolean test(T elem) {
			return value;
		}

		@Override
		public String toString() {
			return "ValuePredicate " + value;
		}
	}

	/** Class {@link ValuePredicate} always returns the specified {@link #value}, ignoring the value passed to {@link #test} */
	public static class InstancePredicate<T> implements Predicate<T> {
		Class<?> expectedClass;

		public InstancePredicate(Class<?> expectedClass) {
			this.expectedClass = expectedClass;
		}

		public Class<?> getExpectedClass() {
			return expectedClass;
		}

		@Override
		public boolean test(T elem) {
			return expectedClass.isInstance(elem);
		}

		@Override
		public String toString() {
			return "InstancePredicate " + expectedClass;
		}
	}

	/** Interface {@link ISinglePredicate} declares that evaluation of a predicate is implemented by this predicate alone */
	public interface ISinglePredicate<T> extends Predicate<T> {
		public Predicate<T> getPredicate();
	}

	/** Interface {@link IMultiPredicate} declares that evaluation of a predicate is delegated to several predicates where the results are combined */
	public interface IMultiPredicate<T> extends Predicate<T> {
		public IList<Predicate<T>> getPredicates();

		public default int getNumPredicates() {
			return getPredicates().size();
		}
	}

	/** Class {@link MultiPredicate} is the abstract base class for implementations of {@link IMultiPredicate} */
	public abstract static class MultiPredicate<T> implements IMultiPredicate<T> {
		IList<Predicate<T>> predicates;

		@SafeVarargs
		protected MultiPredicate(Predicate<T>... predicates) {
			this.predicates = GapList.create(predicates);
		}

		@Override
		public IList<Predicate<T>> getPredicates() {
			return predicates;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + PrintTools.toString(getPredicates());
		}
	}

	/** Class {@link AndPredicate} evaluates to true if all one of the predicates evaluate to true. Evaluation is done with short-circuiting. */
	public static class AndPredicate<T> extends MultiPredicate<T> {

		@SafeVarargs
		public AndPredicate(Predicate<T>... predicates) {
			this.predicates = GapList.create(predicates);
		}

		@Override
		public boolean test(T elem) {
			for (Predicate<T> predicate : predicates) {
				if (!predicate.test(elem)) {
					return false;
				}
			}
			return true;
		}
	}

	/** Class {@link OrPredicate} evaluates to true if at least one of the predicates evaluate to true. Evaluation is done with short-circuiting. */
	public static class OrPredicate<T> extends MultiPredicate<T> {

		@SafeVarargs
		public OrPredicate(Predicate<T>... predicates) {
			this.predicates = GapList.create(predicates);
		}

		@Override
		public boolean test(T elem) {
			for (Predicate<T> predicate : predicates) {
				if (predicate.test(elem)) {
					return true;
				}
			}
			return false;
		}
	}

	/** Class {@link OnePredicate} evaluates to true, if exactly one of the predicates evaluate to true, the others to false */
	public static class OnePredicate<T> extends MultiPredicate<T> {

		@SafeVarargs
		public OnePredicate(Predicate<T>... predicates) {
			this.predicates = GapList.create(predicates);
		}

		@Override
		public boolean test(T elem) {
			int count = 0;
			for (Predicate<T> predicate : predicates) {
				if (predicate.test(elem)) {
					count++;
				}
			}
			return count == 1;
		}
	}

	/**
	 * Abstract class {@link SinglePredicate} stores the single predicate implementing {@link ISinglePredicate}
	 */
	public abstract static class SinglePredicate<T> implements ISinglePredicate<T> {
		Predicate<T> predicate;

		public SinglePredicate(Predicate<T> predicate) {
			this.predicate = predicate;
		}

		@Override
		public Predicate<T> getPredicate() {
			return predicate;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + getPredicate();
		}
	}

	public static class NotPredicate<T> extends SinglePredicate<T> {

		public NotPredicate(Predicate<T> predicate) {
			super(predicate);
		}

		@Override
		public boolean test(T elem) {
			return !predicate.test(elem);
		}

		@Override
		public String toString() {
			return "NotPredicate " + predicate;
		}
	}

	public static class OneOfPredicate<T> implements Predicate<T> {
		List<T> elems;

		@SafeVarargs
		public OneOfPredicate(T... elems) {
			this.elems = GapList.create(elems);
		}

		public OneOfPredicate(List<T> elems) {
			this.elems = GapList.create(elems);
		}

		public List<T> getElems() {
			return elems;
		}

		@Override
		public boolean test(T elem) {
			return elems.contains(elem);
		}

		@Override
		public String toString() {
			return "OneOfPredicate " + elems;
		}
	}

	public static class IsNullPredicate<T> implements Predicate<T> {
		@Override
		public boolean test(T elem) {
			return elem == null;
		}

		@Override
		public String toString() {
			return "IsNull";
		}
	}

	public static class IsNotNullPredicate<T> implements Predicate<T> {
		@Override
		public boolean test(T elem) {
			return elem != null;
		}

		@Override
		public String toString() {
			return "IsNotNull";
		}
	}

	public static class EqualsPredicate<T> implements Predicate<T> {
		T elem;

		public EqualsPredicate(T elem) {
			this.elem = elem;
		}

		public T getElem() {
			return elem;
		}

		@Override
		public boolean test(T elem) {
			return Objects.equals(elem, this.elem);
		}

		@Override
		public String toString() {
			return "EqualsPredicate " + elem;
		}
	}

	/**
	 * Class {@link NamedPredicate} adds a name to a {@link Predicate} making its textual representation human readable compared to internal lambda expressions.
	 */
	public static class NamedPredicate<T> extends SinglePredicate<T> {
		String name;

		public static <T> NamedPredicate<T> of(String name, Predicate<T> predicate) {
			return new NamedPredicate<>(name, predicate);
		}

		public NamedPredicate(String name, Predicate<T> predicate) {
			super(predicate);
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean test(T elem) {
			return predicate.test(elem);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Class {@link NamedBiPredicate} adds a name to a {@link BiPredicate} making its textual representation human readable compared to internal lambda expressions.
	 */
	public static class NamedBiPredicate<T, U> implements BiPredicate<T, U> {
		String name;
		BiPredicate<T, U> predicate;

		public static <T, U> NamedBiPredicate<T, U> of(String name, BiPredicate<T, U> predicate) {
			return new NamedBiPredicate<>(name, predicate);
		}

		public NamedBiPredicate(String name, BiPredicate<T, U> predicate) {
			this.name = name;
			this.predicate = predicate;
		}

		public String getName() {
			return name;
		}

		public BiPredicate<T, U> getBiPredicate() {
			return predicate;
		}

		@Override
		public boolean test(T t, U u) {
			return predicate.test(t, u);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	//

	/**
	 * Helper function which can be used to convert a method reference into a predicate. <br>
	 * Example: {@code Predicates.predicate(String::length)}
	 */
	public static <T> Predicate<T> predicate(Predicate<T> predicate) {
		return predicate;
	}

	public static <T> Predicate<T> namedPredicate(String name, Predicate<T> predicate) {
		return new NamedPredicate<>(name, predicate);
	}

	public static <T> Predicate<T> isNull() {
		return new IsNullPredicate<T>();
	}

	public static <T> Predicate<T> isNotNull() {
		return new IsNotNullPredicate<T>();
	}

	/**
	 * @return	predicate which allows the input argument
	 */
	public static <T> Predicate<T> is(T elem) {
		return new EqualsPredicate<T>(elem);
	}

	/**
	 * @return	predicate which allows all input arguments
	 */
	@SafeVarargs
	public static <T> Predicate<T> isOneOf(T... elems) {
		return new OneOfPredicate<T>(elems);
	}

	public static <T> Predicate<T> isInstance(Class<?> expectedClass) {
		return new InstancePredicate<T>(expectedClass);
	}

	/**
	 * @return	predicate which allows all input arguments
	 */
	public static <T> Predicate<T> allow() {
		return new AllowPredicate<T>();
	}

	/**
	 * @param clazz	clazz (if the compiler cannot detect the correct type T of the predicate itself)
	 * @return		predicate which allows all input arguments
	 */
	public static <T> Predicate<T> allow(Class<T> clazz) {
		return new AllowPredicate<T>();
	}

	/**
	 * @return	predicate which denies all input arguments
	 */
	public static <T> Predicate<T> deny() {
		return new DenyPredicate<T>();
	}

	public static <T, U> BiPredicate<T, U> denyBi() {
		return new DenyBiPredicate<T, U>();
	}

	/**
	 * @param clazz	clazz (if the compiler cannot detect the correct type T of the predicate itself)
	 * @return		predicate which denies all input arguments
	 */
	public static <T> Predicate<T> deny(Class<T> clazz) {
		return new DenyPredicate<T>();
	}

	public static <T> Predicate<T> value(boolean allow) {
		return new ValuePredicate<T>(allow);
	}

	@SafeVarargs
	public static <T> Predicate<T> and(Predicate<T>... predicates) {
		return new AndPredicate<T>(predicates);
	}

	@SafeVarargs
	public static <T> Predicate<T> or(Predicate<T>... predicates) {
		return new OrPredicate<T>(predicates);
	}

	@SafeVarargs
	public static <T> Predicate<T> one(Predicate<T>... predicates) {
		return new OnePredicate<T>(predicates);
	}

	public static <T> NotPredicate<T> not(Predicate<T> predicate) {
		return new NotPredicate<T>(predicate);
	}

	public static <T> List<T> filter(Collection<T> elems, Predicate<T> predicate) {
		GapList<T> list = GapList.create();
		for (T elem : elems) {
			if (predicate.test(elem)) {
				list.add(elem);
			}
		}
		return list;
	}

	//

	/**
	 * Returns true if any of the predicates match, false otherwise.
	 */
	public static <T> boolean testAny(Collection<Predicate<T>> predicates, T t) {
		for (Predicate<T> p : predicates) {
			if (p.test(t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if all of the predicates match, false otherwise.
	 */
	public static <T> boolean testAll(Collection<Predicate<T>> predicates, T t) {
		for (Predicate<T> p : predicates) {
			if (!p.test(t)) {
				return false;
			}
		}
		return true;
	}

	public static <T> boolean testNone(Collection<Predicate<T>> predicates, T t) {
		for (Predicate<T> p : predicates) {
			if (p.test(t)) {
				return false;
			}
		}
		return true;
	}

	//

	public static <T> boolean testAny(Predicate<T> predicate, Collection<T> coll) {
		for (T elem : coll) {
			if (predicate.test(elem)) {
				return true;
			}
		}
		return false;
	}

	public static <T> boolean testAll(Predicate<T> predicate, Collection<T> coll) {
		for (T elem : coll) {
			if (predicate.test(elem)) {
				return false;
			}
		}
		return true;
	}

	public static <T> boolean testNone(Predicate<T> predicate, Collection<T> coll) {
		for (T elem : coll) {
			if (predicate.test(elem)) {
				return false;
			}
		}
		return true;
	}

	// 

	public static <T> Predicate<Collection<T>> testAny(Predicate<T> predicate) {
		return coll -> testAny(predicate, coll);
	}

	public static <T> Predicate<Collection<T>> testAll(Predicate<T> predicate) {
		return coll -> testAll(predicate, coll);
	}

	public static <T> Predicate<Collection<T>> testNone(Predicate<T> predicate) {
		return coll -> testNone(predicate, coll);
	}

}