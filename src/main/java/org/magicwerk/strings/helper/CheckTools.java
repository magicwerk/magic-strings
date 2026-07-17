/*
 * Copyright 2012 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.strings.helper;

import java.util.Collection;
import java.util.function.Predicate;

import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.function.MultiPredicate;
import org.magicwerk.strings.function.Predicates;
import org.magicwerk.strings.function.MultiPredicate.Mode;
import org.magicwerk.strings.helper.PrintTools.ToStringSupplier;

/**
 * Class {@link CheckTools} contains functionality to check for errors.
 */
public class CheckTools {

	// --- Throwing methods ---

	// --- General methods ---

	/**
	 * Checks that condition is true. If it false, an exception is thrown.
	 *
	 * @param cond	condition which must be true
	 * @throws 		IllegalArgumentException if condition is false
	 */
	public static void check(boolean cond) {
		if (!cond) {
			error();
		}
	}

	/**
	 * Checks that condition is true. If it false, an exception is thrown.
	 *
	 * @param cond	condition which must be true
	 * @param msg	message string for exception
	 * @throws 		IllegalArgumentException if condition is false
	 */
	public static void check(boolean cond, String msg) {
		if (!cond) {
			error(msg);
		}
	}

	/**
	 * Checks that condition is true. If it false, an exception is thrown.
	 *
	 * @param cond		condition which must be true
	 * @param format	format string for exception
	 * @param args		format arguments string for exception
	 * @throws 			IllegalArgumentException if condition is false
	 */
	public static void check(boolean cond, String format, Object... args) {
		if (!cond) {
			error(format, args);
		}
	}

	/**
	 * Throws an IllegalArgumentException without error message.
	 */
	public static RuntimeException error() {
		return throwError(null);
	}

	/**
	 * Throws an IllegalArgumentException with the specified error message.
	 *
	 * @param msg	error message
	 */
	public static RuntimeException error(String msg) {
		return throwError(msg);
	}

	/**
	 * Throws an IllegalArgumentException with the specified error message.
	 */
	static RuntimeException throwError(String msg) {
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Throws an IllegalArgumentException with the specified error and message.
	 */
	static RuntimeException throwError(Throwable error, String msg) {
		throw new IllegalArgumentException(msg, error);
	}

	/**
	 * Throws an IllegalArgumentException with the specified error message.
	 * Note that this method never returns, but always throws an exception.
	 * However the declared return value allows to write code like <code>throw error("failure")</code> to stop the compiler from complaining.
	 *
	 * @param format 	format string
	 * @param args		format arguments
	 * @return			this method never returns, but always throws an exception
	 */
	public static RuntimeException error(String format, Object... args) {
		String msg = StringFormatter.format(format, args);
		return throwError(msg);
	}

	public static RuntimeException error(Throwable t, String msg) {
		throw throwError(t, msg);
	}

	public static RuntimeException error(Throwable t, String format, Object... args) {
		String msg = StringFormatter.format(format, args);
		throw throwError(t, msg);
	}

	// --- Specific methods ---

	/**
	 * Checks that specified object is not null.
	 *
	 * @param obj	object to check
	 * @return		object if not null
	 * @throws 		IllegalArgumentException if object is null
	 */
	public static <T> T checkNonNull(T obj) {
		return checkNonNull(obj, "Object may not be null");
	}

	/**
	 * Checks that specified object is not null.
	 * If just the name of the checked object is passed (i.e. the string does not contain spaces),
	 * the text "may not be null" is added.
	 *
	 * @param obj		object to check
	 * @param nameOrMsg name of checked object or full error message
	 * @return			object if not null
	 * @throws 			NullPointerException if object is null
	 */
	public static <T> T checkNonNull(T obj, String nameOrMsg) {
		if (obj == null) {
			if (nameOrMsg.indexOf(' ') == -1) {
				nameOrMsg = nameOrMsg + " may not be null";
			}
			throw new NullPointerException(nameOrMsg);
		}
		return obj;
	}

	/**
	 * Checks that specified object is not null.
	 *
	 * @param obj		object to check
	 * @param msg		format string
	 * @param args		format arguments
	 * @return			object if not null
	 * @throws 			NullPointerException if object is null
	 */
	public static <T> T checkNonNull(T obj, String msg, Object... args) {
		if (obj == null) {
			String str = StringFormatter.format(msg, args);
			throw new NullPointerException(str);
		}
		return obj;
	}

	public static <T> T checkValid(T obj, boolean cond, String msg, Object... args) {
		CheckTools.check(cond, msg, args);
		return obj;
	}

	public static <T> T checkValid(T obj, boolean cond, String msg) {
		CheckTools.check(cond, msg);
		return obj;
	}

	/**
	 * Check that condition is valid and return object unchanged.
	 * This method allows to represent arguments checks without if statements similar to checkNonNull.
	 */
	public static <T> T checkValid(T obj, boolean cond) {
		CheckTools.check(cond);
		return obj;
	}

	public interface Validate<E> extends Predicate<E> {
		default void validate(E elem) {
			if (!test(elem)) {
				error(elem);
			}
		}

		default void error(E elem) {
			String error = getError(elem);
			CheckTools.error(error);
		}

		default String getError(E elem) {
			return this + ": " + PrintTools.toString(elem);
		}
	}

	public static class TypeValidator implements Validate<Object> {

		Class<?>[] types;

		TypeValidator(Class<?>... types) {
			this.types = types;
		}

		@Override
		public boolean test(Object obj) {
			for (Class<?> c : types) {
				if (obj == null) {
					if (c == null) {
						return true;
					}
				} else {
					if (c != null) {
						if (c.isAssignableFrom(obj.getClass())) {
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		public String getError(Object elem) {
			Class<?> c = (elem != null) ? elem.getClass() : null;
			return "Object has invalid type: " + c + ", expected: " + PrintTools.toString(types);
		}

		@Override
		public String toString() {
			return "Expected types: " + PrintTools.toString(types);
		}
	}

	public static void checkIndex(int index, int loIncl, int hiExcl) {
		if (index < loIncl || index >= hiExcl) {
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T checkTypeOf(Object obj, Class<?>... types) {
		return (T) Check.of(new TypeValidator(types)).check(obj);
	}

	public static <K, V> void checkVarargTuples(K key, V value, Object... keyValues) {
		VarargTools.checkVarargTuples(key, value, keyValues);
	}

	//

	/**
	 * Class {@link Check} provides access to several check methods. <br>
	 * Example to check that exactly one of the arguments is true:
	 * <pre>
	 * Check.forTrue(Mode.ONE).check(a, b, c);
	 * </pre>
	 */
	public static class Check<T> {

		public static <T> CheckSingle<T> of(Predicate<T> predicate) {
			return new CheckSingle<>(predicate);
		}

		public static <T> CheckMulti<T> of(Mode mode, Predicate<T> predicate) {
			return new CheckMulti<>(predicate, mode);
		}

		public static <T> CheckNull<T> forNull(Mode mode) {
			return CheckNull.checkForNull(mode);
		}

		public static <T> CheckNull<T> forNotNull(Mode mode) {
			return CheckNull.checkForNotNull(mode);
		}

		/** Check whether the specified arguments are true as defined. */
		public static <T> CheckNull<T> forTrue(Mode mode) {
			return CheckNull.checkForTrue(mode);
		}

		public static <T> CheckNull<T> forFalse(Mode mode) {
			return CheckNull.checkForFalse(mode);
		}

		public static <T> CheckEquals<T> forEqual() {
			return new CheckEquals<>(true);
		}

		public static <T> CheckEquals<T> forNotEqual() {
			return new CheckEquals<>(false);
		}
	}

	public static class CheckBase<E, T extends CheckBase<E, ?>> {
		String msg;
		Object[] args;

		@SuppressWarnings("unchecked")
		public T withMessage(String msg) {
			this.msg = msg;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		public T withMessage(String format, Object... args) {
			this.msg = format;
			this.args = args;
			return (T) this;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		void error(Object predicate, Object vals) {
			String str = (args != null) ? StringFormatter.format(msg, args) : msg;
			if (str == null) {
				if (predicate instanceof Validate) {
					str = ((Validate) predicate).getError(vals);
				} else {
					// CheckToolsTest$$Lambda$28/120478350@2a265ea9 
					str = predicate.toString();
				}
			}
			str = str + ": " + PrintTools.toString(vals);
			CheckTools.error(str);
		}
	}

	/**
	 * Class {@link CheckEquals} checks for equality.
	 */
	public static class CheckEquals<E> extends CheckBase<E, CheckEquals<E>> {

		boolean equals;

		CheckEquals(boolean equals) {
			this.equals = equals;
		}

		public void check(E val0, E val1) {
			if (ObjectTools.equals(val0, val1) == equals) {
				return;
			}
			String msg = (equals) ? "Must be equal" : "Must not be equal";
			error(msg, ToStringSupplier.of(() -> val0 + " / " + val1));
		}
	}

	/**
	 * Class {@link CheckSingle} checks a single {@link Predicate}.
	 */
	public static class CheckSingle<E> extends CheckBase<E, CheckSingle<E>> {

		Predicate<E> predicate;

		CheckSingle(Predicate<E> predicate) {
			this.predicate = predicate;
		}

		public E check(E val) {
			if (predicate.test(val)) {
				return val;
			}
			error(predicate, val);
			return null;
		}
	}

	/**
	 * Class {@link CheckMulti} checks a {@link MultiPredicate} which tests multiple values against a single predicate.
	 */
	public static class CheckMulti<E> extends CheckBase<E, CheckMulti<E>> {

		MultiPredicate<E> multiPredicate;

		CheckMulti(Predicate<E> predicate, Mode mode) {
			multiPredicate = new MultiPredicate<E>(mode, predicate);
		}

		@SuppressWarnings("unchecked")
		public void check(E... vals) {
			if (multiPredicate.testArray(vals)) {
				return;
			}
			error(multiPredicate, vals);
		}

		public void check(Collection<E> vals) {
			if (multiPredicate.testCollection(vals)) {
				return;
			}
			error(multiPredicate, vals);
		}
	}

	public static class CheckNull<E> extends CheckBase<E, CheckNull<E>> {

		// Arrays must have same order as enum Mode (ALL, NONE, ANY, ONE, ONE_OR_NONE)
		@SuppressWarnings({ "rawtypes", "unchecked" })
		static final MultiPredicate[] checkPredicatesNull = new MultiPredicate[] {
				new MultiPredicate(Mode.ALL, Predicates.isNull()),
				new MultiPredicate(Mode.NONE, Predicates.isNull()),
				new MultiPredicate(Mode.ANY, Predicates.isNull()),
				new MultiPredicate(Mode.ONE, Predicates.isNull()),
				new MultiPredicate(Mode.ONE_OR_NONE, Predicates.isNull())
		};
		@SuppressWarnings({ "rawtypes", "unchecked" })
		static final MultiPredicate[] checkPredicatesNotNull = new MultiPredicate[] {
				new MultiPredicate(Mode.ALL, Predicates.isNotNull()),
				new MultiPredicate(Mode.NONE, Predicates.isNotNull()),
				new MultiPredicate(Mode.ANY, Predicates.isNotNull()),
				new MultiPredicate(Mode.ONE, Predicates.isNotNull()),
				new MultiPredicate(Mode.ONE_OR_NONE, Predicates.isNotNull())
		};
		@SuppressWarnings({ "rawtypes", "unchecked" })
		static final MultiPredicate[] checkPredicatesTrue = new MultiPredicate[] {
				new MultiPredicate(Mode.ALL, isBoolean(true)),
				new MultiPredicate(Mode.NONE, isBoolean(true)),
				new MultiPredicate(Mode.ANY, isBoolean(true)),
				new MultiPredicate(Mode.ONE, isBoolean(true)),
				new MultiPredicate(Mode.ONE_OR_NONE, isBoolean(true))
		};
		@SuppressWarnings({ "rawtypes", "unchecked" })
		static final MultiPredicate[] checkPredicatesFalse = new MultiPredicate[] {
				new MultiPredicate(Mode.ALL, isBoolean(false)),
				new MultiPredicate(Mode.NONE, isBoolean(false)),
				new MultiPredicate(Mode.ANY, isBoolean(false)),
				new MultiPredicate(Mode.ONE, isBoolean(false)),
				new MultiPredicate(Mode.ONE_OR_NONE, isBoolean(false))
		};

		static Predicate<Object> isBoolean(boolean expected) {
			return new Predicate<Object>() {

				@Override
				public boolean test(Object obj) {
					if (obj instanceof Boolean) {
						return expected == (Boolean) obj;
					}
					throw CheckTools.error("{} is not of type boolean");
				}

				@Override
				public String toString() {
					return "" + expected;
				}
			};
		}

		MultiPredicate<E> checkPredicates;

		@SuppressWarnings("unchecked")
		public static <T> CheckNull<T> checkForNull(Mode mode) {
			return new CheckNull<>(checkPredicatesNull[mode.ordinal()]);
		}

		@SuppressWarnings("unchecked")
		public static <T> CheckNull<T> checkForNotNull(Mode mode) {
			return new CheckNull<>(checkPredicatesNotNull[mode.ordinal()]);
		}

		@SuppressWarnings("unchecked")
		public static <T> CheckNull<T> checkForTrue(Mode mode) {
			return new CheckNull<>(checkPredicatesTrue[mode.ordinal()]);
		}

		@SuppressWarnings("unchecked")
		public static <T> CheckNull<T> checkForFalse(Mode mode) {
			return new CheckNull<>(checkPredicatesFalse[mode.ordinal()]);
		}

		CheckNull(MultiPredicate<E> checkPredicates) {
			this.checkPredicates = checkPredicates;
		}

		@SuppressWarnings("unchecked")
		public void check(E... vals) {
			if (checkPredicates.testArray(vals)) {
				return;
			}
			error(checkPredicates, vals);
		}
	}

}
