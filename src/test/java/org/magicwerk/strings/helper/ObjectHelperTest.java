package org.magicwerk.strings.helper;

import static org.magicwerk.strings.helper.ObjectHelper.getter;
import static org.magicwerk.strings.helper.ObjectHelper.implToString;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectHelper;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link ObjectHelper}.
 */
public class ObjectHelperTest {

	static class MyClass {
		int number;
		String string;

		public MyClass(int number, String string) {
			this.number = number;
			this.string = string;
		}

		public int getNumber() {
			return number;
		}

		public String getString() {
			return string;
		}
	}

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ObjectHelperTest().test();
	}

	void test() {
		//testImplToString();
		testObjectHelper();
		//testEquals();
	}

	@Trace
	public void testImplToString() {
		MyClass obj0 = new MyClass(0, null);
		implToString(obj0, getter("number", MyClass::getNumber), getter("string", MyClass::getString));

		MyClass obj1 = new MyClass(1, "a");
		implToString(obj1, getter("number", MyClass::getNumber), getter("string", MyClass::getString));
	}

	@Capture
	public void testObjectHelper() {
		Val va = new ValA(0);
		Val vb = new ValB(0);

		Object o = new Object();
		CheckTools.check(!va.equals(o));

		CheckTools.check(va.equals(vb));
		CheckTools.check(vb.equals(va));
	}

	static class Val {
		int val;

		Val(int val) {
			this.val = val;
		}

		public int getVal() {
			return val;
		}

		@Override
		public int hashCode() {
			return ObjectHelper.implHashCode(this, Val::getVal);
		}

		@Override
		public boolean equals(Object obj) {
			// implEquals() must define base class Val.class to handle different subobject types correctly
			return ObjectHelper.implEquals(this, obj, Val.class, Val::getVal);
		}
	}

	static class ValA extends Val {
		ValA(int val) {
			super(val);
		}
	}

	static class ValB extends Val {
		ValB(int val) {
			super(val);
		}
	}
	//

	void testEquals() {
		// Correctly implementing equals() is not trivial and not possible in a generic way where classes can be extended and state added		
		// http://www.angelikalanger.com/Articles/EffectiveJava/01.Equals-Part1/01.Equals1.html
		// http://www.angelikalanger.com/Articles/EffectiveJava/02.Equals-Part2/02.Equals2.html
		// https://www.artima.com/articles/how-to-write-an-equality-method-in-java

		LOG.info("-- getClass");
		MyPoint1 a1 = new MyPoint1(1, 2);
		MyPoint1 a2 = new MyPoint1(1, 2);
		MyColorPoint1 a3 = new MyColorPoint1(1, 2, "red");
		MyPoint1 a4 = new MyPoint1(1, 2) {
		};
		testEquals(a1, a2, a3, a4);

		LOG.info("-- instanceof");
		MyPoint2 b1 = new MyPoint2(1, 2);
		MyPoint2 b2 = new MyPoint2(1, 2);
		MyColorPoint2 b3 = new MyColorPoint2(1, 2, "red");
		MyPoint2 b4 = new MyPoint2(1, 2) {
		};
		testEquals(b1, b2, b3, b4);

		LOG.info("-- instanceof/canEqual");
		MyPoint3 c1 = new MyPoint3(1, 2);
		MyPoint3 c2 = new MyPoint3(1, 2);
		MyColorPoint3 c3 = new MyColorPoint3(1, 2, "red");
		MyPoint3 c4 = new MyPoint3(1, 2) {
		};
		testEquals(c1, c2, c3, c4);
	}

	void testEquals(Object x, Object y, Object z, Object anonymous) {
		testLiskov(x, y);
		testLiskov(y, z);
		testLiskovAnonymous(y, anonymous);

		testReflexive(x);
		testReflexive(y);
		testReflexive(z);

		testSymmetric(x, y);
		testSymmetric(x, z);
		testSymmetric(y, z);

		testTransitive(x, y, z);
		testTransitive(y, z, x);
		testTransitive(z, x, y);
	}

	void testReflexive(Object x) {
		if (!x.equals(x)) {
			LOG.warn("Not reflexive: {}", x);
		}
	}

	void testSymmetric(Object x, Object y) {
		boolean eq1 = x.equals(y);
		boolean eq2 = y.equals(x);
		if (eq1 != eq2) {
			LOG.warn("Not symmetric: {} / {}", x, y);
		}
	}

	void testTransitive(Object x, Object y, Object z) {
		if (x.equals(y) && y.equals(z)) {
			if (!x.equals(z)) {
				LOG.warn("Not transitive: {} / {} / {}", new Object[] { x, y, z });
			}
		}
	}

	void testLiskov(Object x, Object y) {
		if (!x.equals(y) || !y.equals(x)) {
			LOG.warn("Liskov violated: {} / {}", x, y);
		}
	}

	void testLiskovAnonymous(Object x, Object y) {
		if (!x.equals(y) || !y.equals(x)) {
			LOG.warn("Liskov violated for anonymous: {} / {}", x, y);
		}
	}

	private static class MyPoint1 {
		int x;
		int y;

		public MyPoint1(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyPoint1 other = (MyPoint1) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}

	private static class MyColorPoint1 extends MyPoint1 {
		String color;

		public MyColorPoint1(int x, int y, String color) {
			super(x, y);
			this.color = color;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyColorPoint1 other = (MyColorPoint1) obj;
			if (color == null) {
				if (other.color != null)
					return false;
			} else if (!color.equals(other.color))
				return false;
			return true;
		}
	}

	private static class MyPoint2 {
		int x;
		int y;

		public MyPoint2(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof MyPoint2))
				return false;
			MyPoint2 other = (MyPoint2) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}

	private static class MyColorPoint2 extends MyPoint2 {
		String color;

		public MyColorPoint2(int x, int y, String color) {
			super(x, y);
			this.color = color;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof MyColorPoint2))
				return false;
			MyColorPoint2 other = (MyColorPoint2) obj;
			if (color == null) {
				if (other.color != null)
					return false;
			} else if (!color.equals(other.color))
				return false;
			return true;
		}
	}

	private static class MyPoint3 {
		int x;
		int y;

		public MyPoint3(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof MyPoint3) {
				MyPoint3 that = (MyPoint3) other;
				result = (that.canEqual(this) && this.x == that.x && this.y == that.y);
			}
			return result;
		}

		public boolean canEqual(Object other) {
			return (other instanceof MyPoint3);
		}
	}

	private static class MyColorPoint3 extends MyPoint3 { // No longer violates symmetry requirement
		String color;

		public MyColorPoint3(int x, int y, String color) {
			super(x, y);
			this.color = color;
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof MyColorPoint3) {
				MyColorPoint3 that = (MyColorPoint3) other;
				result = (that.canEqual(this) && this.color.equals(that.color) && super.equals(that));
			}
			return result;
		}

		@Override
		public boolean canEqual(Object other) {
			return (other instanceof MyColorPoint3);
		}
	}

}
