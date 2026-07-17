/*
 * Copyright 2014 by Thomas Mauch
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
package org.magicwerk.strings.chars;

import java.util.Iterator;
import java.util.function.IntPredicate;

import org.magicwerk.collections.primitive.IIntList;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;

/**
 * Class {@link CodePointPredicates} contains standard predicates implementing {@link IntPredicate}.
 */
public class CodePointPredicates {

	public static IntPredicate equals(int cp) {
		return new OneCodePoint(cp);
	}

	public static IntPredicate equals(int cp, CodePointEqual equal) {
		return new OneCodePointIgnoreCase(cp, equal);
	}

	//

	public static IntPredicate of(int c) {
		return new OneCodePoint(c);
	}

	public static IntPredicate of(int c1, int c2) {
		return new TwoCodePoints(c1, c2);
	}

	public static IntPredicate of(int c1, int c2, int c3) {
		return new ThreeCodePoints(c1, c2, c3);
	}

	/**
	 * Create {@link IntPredicate} which matches any of the code points.
	 * This method does support surrogates.
	 */
	public static IntPredicate oneOf(CharSequence chars) {
		if (chars.length() == 0) {
			return NoCodePoint.INSTANCE;
		}

		int[] cps0 = getFewCodePoints(chars);
		if (cps0 != null) {
			switch (cps0.length) {
			case 1:
				return new OneCodePoint(cps0[0]);
			case 2:
				return new TwoCodePoints(cps0[0], cps0[1]);
			case 3:
				return new ThreeCodePoints(cps0[0], cps0[1], cps0[2]);
			}
		}

		IIntList cps1 = CodePointTools.getCodePoints(chars);
		return new ManyCodePoints(cps1);
	}

	/**
	 * Create {@link IntPredicate} which matches any of the code points respecting case as defined.
	 * This method does support surrogates.
	 */
	public static IntPredicate oneOf(CharSequence chars, CodePointEqual equal) {
		if (equal == null) {
			return oneOf(chars);
		}

		if (chars.length() == 0) {
			return NoCodePoint.INSTANCE;
		}

		int[] cps0 = getFewCodePoints(chars);
		if (cps0 != null) {
			switch (cps0.length) {
			case 1:
				return new OneCodePointIgnoreCase(cps0[0], equal);
			case 2:
				return new TwoCodePointsIgnoreCase(cps0[0], cps0[1], equal);
			case 3:
				return new ThreeCodePointsIgnoreCase(cps0[0], cps0[1], cps0[2], equal);
			}
		}

		IIntList cps1 = CodePointTools.getCodePoints(chars);
		return new ManyCodePointsIgnoreCase(cps1, equal);
	}

	/** Returns the code points contained in the string as int array if the number of code points is less than or equal 3, otherwise null. */
	static int[] getFewCodePoints(CharSequence chars) {
		int len = chars.length();
		if (len == 1) {
			// 1 char
			return new int[] { chars.charAt(0) };
		} else if (len > 6) {
			// must be more than 3 code points
			return null;
		}

		// Combination of chars and codepoints which could fit into 3 codepoints
		Iterator<Integer> cpi = CodePointTools.codePointIterator(chars);
		int size = 1;
		int cp0 = cpi.next();

		int cp1 = -1;
		if (cpi.hasNext()) {
			cp1 = cpi.next();
			size = 2;
		}

		int cp2 = -1;
		if (cpi.hasNext()) {
			cp2 = cpi.next();
			size = 3;
		}

		if (cpi.hasNext()) {
			// More than 3 code points
			return null;
		}

		switch (size) {
		case 1:
			return new int[] { cp0 };
		case 2:
			return new int[] { cp0, cp1 };
		case 3:
			return new int[] { cp0, cp1, cp2 };
		default:
			throw new AssertionError();
		}
	}

	//

	public static class NegatedCodePointPredicate implements IntPredicate {

		final IntPredicate predicate;

		public NegatedCodePointPredicate(IntPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean test(int c) {
			return !predicate.test(c);
		}
	}

	public static class NoCodePoint implements IntPredicate {

		public static final NoCodePoint INSTANCE = new NoCodePoint();

		protected NoCodePoint() {
		}

		@Override
		public boolean test(int c) {
			return false;
		}
	}

	public static class OneCodePoint implements IntPredicate {

		final int ch;

		public OneCodePoint(int ch) {
			this.ch = ch;
		}

		@Override
		public boolean test(int c) {
			return c == ch;
		}
	}

	public static class OneCodePointIgnoreCase implements IntPredicate {

		final int ch;
		final CodePointEqual equal;

		public OneCodePointIgnoreCase(int ch, CodePointEqual equal) {
			this.ch = ch;
			this.equal = equal;
		}

		@Override
		public boolean test(int c) {
			return equal.isEqualCodePoint(c, ch);
		}
	}

	public static class TwoCodePoints implements IntPredicate {

		final int ch0;
		final int ch1;

		public TwoCodePoints(int ch0, int ch1) {
			this.ch0 = ch0;
			this.ch1 = ch1;
		}

		@Override
		public boolean test(int c) {
			return c == ch0 || c == ch1;
		}
	}

	public static class TwoCodePointsIgnoreCase implements IntPredicate {

		final int ch0;
		final int ch1;
		final CodePointEqual equal;

		public TwoCodePointsIgnoreCase(int ch0, int ch1, CodePointEqual equal) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.equal = equal;
		}

		@Override
		public boolean test(int c) {
			return equal.isEqualCodePoint(c, ch0) || equal.isEqualCodePoint(c, ch1);
		}
	}

	public static class ThreeCodePoints implements IntPredicate {

		final int ch0;
		final int ch1;
		final int ch2;

		public ThreeCodePoints(int ch0, int ch1, int ch2) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.ch2 = ch2;
		}

		@Override
		public boolean test(int c) {
			return c == ch0 || c == ch1 || c == ch2;
		}
	}

	public static class ThreeCodePointsIgnoreCase implements IntPredicate {

		final int ch0;
		final int ch1;
		final int ch2;
		final CodePointEqual equal;

		public ThreeCodePointsIgnoreCase(int ch0, int ch1, int ch2, CodePointEqual equal) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.ch2 = ch2;
			this.equal = equal;
		}

		@Override
		public boolean test(int c) {
			return equal.isEqualCodePoint(c, ch0) || equal.isEqualCodePoint(c, ch1) || equal.isEqualCodePoint(c, ch2);
		}
	}

	public static class ManyCodePoints implements IntPredicate {

		final IIntList codePoints;

		public ManyCodePoints(IIntList codePoints) {
			this.codePoints = codePoints;
		}

		@Override
		public boolean test(int cp) {
			return search(cp) >= 0;
		}

		public int indexOf(int cp) {
			int index = search(cp);
			return (index >= 0) ? index : -1;
		}

		int search(int cp) {
			return codePoints.binarySearch(cp);
		}
	}

	public static class ManyCodePointsIgnoreCase implements IntPredicate {

		final IIntList codePoints;
		final CodePointEqual equal;

		public ManyCodePointsIgnoreCase(IIntList codePoints, CodePointEqual equal) {
			this.codePoints = codePoints;
			this.equal = equal;
		}

		@Override
		public boolean test(int c) {
			for (int i = 0; i < codePoints.size(); i++) {
				if (equal.isEqualCodePoint(c, codePoints.get(i))) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Class {@link RangeCodePoints} tests whether a code point is in the defined range of code points. 
	 */
	public static class RangeCodePoints implements IntPredicate {

		final int firstCodePoint;
		final int lastCodePoint;

		public RangeCodePoints(int firstCodePoint, int lastCodePoint) {
			this.firstCodePoint = firstCodePoint;
			this.lastCodePoint = lastCodePoint;
		}

		@Override
		public boolean test(int cp) {
			return firstCodePoint <= cp && cp <= lastCodePoint;
		}

		/** Getter for {@link #firstCodePoint} */
		public int getFirstCodePoint() {
			return firstCodePoint;
		}

		/** Getter for {@link #lastCodePoint} */
		public int getLastCodePoint() {
			return lastCodePoint;
		}
	}

}
