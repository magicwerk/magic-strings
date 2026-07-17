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
package org.magicwerk.strings.objects;

/**
 * Class {@link Array} stores several items of the same type. It has a fixed size.
 */
public class Array<E> extends AbstractArray<E> {

	static final IArray<?> EMPTY_ARRAY = new Array<>(0);

	E[] elems;

	/**
	 * Create {@link IArray} with specified size.
	 */
	@SuppressWarnings("unchecked")
	public static <T> IArray<T> createArray(int size) {
		if (size == 0) {
			return (IArray<T>) EMPTY_ARRAY;
		} else if (size == 1) {
			return new Single<>();
		} else if (size == 2) {
			return new Pair<T>();
		} else if (size == 3) {
			return new Triad<T>();
		} else {
			return new Array<>(size);
		}
	}

	/**
	 * Create array with specified size.
	 * 
	 * @param size	size of array
	 */
	@SuppressWarnings("unchecked")
	public Array(int size) {
		elems = (E[]) new Object[size];
	}

	/**
	 * Create array containing the specified elements.
	 * 
	 * @param elems	elements to be contained in array
	 */
	@SafeVarargs
	public Array(E... elems) {
		this.elems = elems.clone();
	}

	@Override
	public int size() {
		return elems.length;
	}

	@Override
	public E get(int index) {
		return elems[index];
	}

	@Override
	public void set(int index, E elem) {
		elems[index] = elem;
	}

}
