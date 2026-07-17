/*
 * Copyright 2010 by Thomas Mauch
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
package org.magicwerk.strings;

import java.util.Arrays;
import java.util.function.UnaryOperator;

import org.magicwerk.brownies.collections.helper.CapacityHelper;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link StringRepeater} implements efficiently repeating Strings.
 * It is designed to be used as static instance which will cache the created strings, e.g.
 * <pre>
 * static final StringRepeater DOTS = new StringRepeater(".");
 * </pre>
 * You can the use {@link #repeat(int)} to the string with the specified number of repetitions:
 * <pre>
 * String s = DOTS.repeat(10);
 * </pre> 
 * It is not designed to be used just once, use {@link String#repeat} for this. 
 * <p>
 * 
 */
public interface StringRepeater {

	/**
	 * Returns number of times the repeat string.
	 *
	 * @param count	number of times to repeat the string
	 * @return		repeated string
	 */
	public String repeat(int count);

	/** Release all stored cache strings */
	public void release();

	//

	public static StringRepeater build(char repeat) {
		return build(b -> b.setRepeat(repeat));
	}

	public static StringRepeater build(String repeat) {
		return build(b -> b.setRepeat(repeat));
	}

	/** Build {@link StringRepeater} with specified builder function */
	public static StringRepeater build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/** Get {@link Builder} to create a {@link StringRepeater} */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to create instances of {@link StringRepeater}.
	 */
	public static class Builder {

		String repeat;
		String separator;
		int preSize;
		boolean preInit;

		public Builder setRepeat(char repeat) {
			this.repeat = String.valueOf(repeat);
			return this;
		}

		public Builder setRepeat(String repeat) {
			this.repeat = repeat;
			return this;
		}

		public Builder setSeparator(char separator) {
			this.separator = String.valueOf(separator);
			return this;
		}

		public Builder setSeparator(String separator) {
			this.separator = separator;
			return this;
		}

		public Builder setPreSize(int preSize) {
			this.preSize = preSize;
			return this;
		}

		public Builder setPreInit(boolean preInit) {
			this.preInit = preInit;
			return this;
		}

		/** Build an instance of {@link StringAligner} with the specified configuration */
		public StringRepeater build() {
			CheckTools.check(repeat != null, "repeat must be set");

			StringRepeaterImpl impl;
			if (separator == null) {
				impl = new StringRepeaterImpl(repeat);
			} else {
				StringRepeaterSeparatorImpl separatorImpl = new StringRepeaterSeparatorImpl(repeat, separator);
				impl = separatorImpl;
			}
			impl.strs = impl.initRepeat(preSize, preInit);
			return impl;
		}

	}
	// == Multi-Threaded Design:

	// Construction:
	// - If a static StringRepeaterImplBase is constructed, it will be safely published, i.e. both
	// repeat and strs will only be visible to other threads initialized
	// - In a non-static instance, the final repeat field is guaranteed to be visible.
	// The initialization of strs however may not be visible to other threads,
	// it can either see the strs initialized or still being null.
	//
	// Ensuring the Capacity:
	// At the time when ensureCapacity() is called, the current, a stale (non null) or an uninitialized (null) value may be visible.
	// If the value is not current, a duplicate array allocation may happen for the current thread.
	//
	// Repeating the String:
	// If the cached value in the array strs is accessed, it can also return a non current null value.
	// In this case, a duplicate string construction may happen for the current thread.

	public abstract static class StringRepeaterImplBase implements StringRepeater {

		final static int DEFAULT_CAPACITY = 16;

		final String repeat;
		String[] strs;

		StringRepeaterImplBase(String repeat) {
			this.repeat = repeat;
		}

		//

		String[] initRepeat(int size, boolean init) {
			size++;
			String[] strs = newArray(size);
			if (init) {
				for (int i = 0; i < size; i++) {
					strs[i] = doRepeat(i);
				}
			}
			return strs;
		}

		String[] newArray(int size) {
			return new String[size];
		}

		abstract String doRepeat(int count);

		@Override
		public void release() {
			this.strs = new String[0];
		}

		@Override
		public String repeat(int count) {
			CheckTools.check(count >= 0, "count must be >= 0");

			String[] newStrs = ensureCapacity(count + 1);
			String str = newStrs[count];
			if (str == null) {
				str = doRepeat(count);
				newStrs[count] = str;
			}
			return str;
		}

		String[] ensureCapacity(int capacity) {
			// MT: Access strs just once
			String[] strs = this.strs;

			// MT: it can be that strs is seen as null by another thread, so use size 0 to allocate a new array 
			int size = (strs != null) ? strs.length : 0;

			// If capacity of array is sufficient, just return it
			if (capacity <= size) {
				return strs;
			}

			// Otherwise extend capacity
			capacity = CapacityHelper.calculateCapacity(capacity, size, DEFAULT_CAPACITY);
			String[] newStrs = Arrays.copyOf(strs, capacity);

			// MT: set new strs array, so it becomes visible to other threads at some point in time
			this.strs = newStrs;
			return newStrs;
		}
	}

	public static class StringRepeaterImpl extends StringRepeaterImplBase {

		StringRepeaterImpl(String repeat) {
			super(repeat);
		}

		@Override
		String doRepeat(int count) {
			return repeat.repeat(count);
		}
	}

	public static class StringRepeaterSeparatorImpl extends StringRepeaterImpl {

		final String separator;

		StringRepeaterSeparatorImpl(String repeat, String separator) {
			super(repeat);
			this.separator = separator;
		}

		@Override
		String doRepeat(int count) {
			if (count == 0) {
				return "";
			}
			int len = count * repeat.length() + (count - 1) * separator.length();
			StringBuilder buf = new StringBuilder(len);
			for (int i = 0; i < count - 1; i++) {
				buf.append(repeat);
				buf.append(separator);
			}
			buf.append(repeat);
			assert buf.length() == len;
			return buf.toString();
		}
	}

}
