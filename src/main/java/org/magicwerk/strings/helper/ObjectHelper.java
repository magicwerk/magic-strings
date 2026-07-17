package org.magicwerk.strings.helper;

import java.util.Comparator;
import java.util.function.Function;

import org.magicwerk.strings.StringPrinter;

/**
 * Class {@link ObjectHelper} contains helper for implementing java.lang.Object methods like hashCode, equals, etc.
 */
public class ObjectHelper {

	/**
	 * <pre>
	 * public boolean equals(Object obj) {
	 *	 return ObjectHelper.implEquals(this, obj, MyObj::getField1, MyObj::getField1);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> boolean implEquals(T obj1, Object obj2, Function<T, ?>... getters) {
		return implEquals(obj1, obj2, null, getters);
	}

	/**
	 * <pre>
	 * public boolean equals(Object obj) {
	 *	 return ObjectHelper.implEquals(this, obj, BaseObj.class, MyObj::getField1, MyObj::getField1);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> boolean implEquals(T obj1, Object obj2, Class<?> clazz, Function<T, ?>... getters) {
		if (obj1 == null || obj2 == null) {
			return obj1 == obj2;
		}
		if (obj1 == obj2) {
			return true;
		}

		if (clazz != null) {
			if (!clazz.isInstance(obj1) || !clazz.isInstance(obj2)) {
				return false;
			}
		} else {
			if (!ObjectTools.equals(obj1.getClass(), obj2.getClass())) {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		T o2 = (T) obj2;
		for (Function<T, ?> getter : getters) {
			if (!ObjectTools.equals(getter.apply(obj1), getter.apply(o2))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <pre>
	 * public int hashCode() {
	 *	 return ObjectHelper.implHashCode(this,  MyObj::getField1, MyObj::getField1);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> int implHashCode(T obj, Function<T, ?>... getters) {
		int hashCode = 1;
		if (obj != null) {
			for (Function<T, ?> getter : getters) {
				Object val = getter.apply(obj);
				hashCode = 31 * hashCode + ObjectTools.hashCode(val);
			}
		}
		return hashCode;
	}

	/**
	 * Compare two objects.
	 * The objects are compared by applying the getters to get their properties which must be of type Comparable.
	 * 
	 * <pre>
	 * public int compareTo(MyObj obj) {
	 *	 return ObjectHelper.implCompare(this, obj, MyObj::getField1, MyObj::getField2);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> int implCompare(T obj1, T obj2, Function<T, ?>... getters) {
		if (obj1 == null) {
			if (obj2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (obj2 == null) {
				return -1;
			}
		}
		if (obj1 == obj2) {
			return 0;
		}

		for (Function<T, ?> getter : getters) {
			Object val1 = getter.apply(obj1);
			Object val2 = getter.apply(obj2);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			int cmp = ObjectTools.compare((Comparable) val1, (Comparable) val2);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	/**
	 * Compare two objects.
	 * The objects are compared by applying the getters to get their properties which must be of type Comparable.
	 * 
	 * <pre>
	 * public int compareTo(MyObj obj) {
	 *	 return ObjectHelper.implCompare(this, obj, MyObj::getField1, MyObj::getField2);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T, R> int implCompare(T obj1, T obj2, GetterWithComparator<T, R>... getters) {
		if (obj1 == null) {
			if (obj2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (obj2 == null) {
				return -1;
			}
		}
		if (obj1 == obj2) {
			return 0;
		}

		for (int i = 0; i < getters.length; i++) {
			Function<T, R> getter = getters[i].getter;
			Comparator<R> comparator = getters[i].comparator;
			R val1 = getter.apply(obj1);
			R val2 = getter.apply(obj2);
			int cmp = comparator.compare(val1, val2);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	/**
	 * <pre>
	 * public String toString() {
	 *	 return ObjectHelper.implToString(this, MyObj::getField1, MyObj::getField2);
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> String implToString(T obj, Function<T, ?>... getters) {
		StringPrinter buf = new StringPrinter().setElemMarker(", ");
		for (Function<T, ?> getter : getters) {
			Object val = getter.apply(obj);
			buf.add(val);
		}
		return buf.toString();
	}

	/**
	 * <pre>
	 * public String toString() {
	 *	 return ObjectHelper.implToString(this, getter("field1", MyObj::getField1), getter("field2", MyObj::getField2));
	 * }
	 * </pre>
	 */
	@SafeVarargs
	public static <T> String implToString(T obj, GetterWithName<T>... getters) {
		StringPrinter buf = new StringPrinter().setElemMarker(", ").setPartMarker(": ");
		for (GetterWithName<T> getter : getters) {
			Object val = getter.getter.apply(obj);
			buf.addParts(getter.name, val);
		}
		return buf.toString();
	}

	public static <T> GetterWithName<T> getter(String name, Function<T, ?> getter) {
		return new GetterWithName<T>(name, getter);
	}

	public static <T, R> GetterWithComparator<T, R> getter(Function<T, R> getter, Comparator<R> comparator) {
		return new GetterWithComparator<T, R>(getter, comparator);
	}

	public static class GetterWithName<T> {
		String name;
		Function<T, ?> getter;

		GetterWithName(String name, Function<T, ?> getter) {
			this.name = name;
			this.getter = getter;
		}
	}

	public static class GetterWithComparator<T, R> {
		Function<T, R> getter;
		Comparator<R> comparator;

		GetterWithComparator(Function<T, R> getter, Comparator<R> comparator) {
			this.getter = getter;
			this.comparator = comparator;
		}
	}

}
