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

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link IArray2} stores two objects of different type.
 * Use {@link Pair} for two objects of same type.
 */
public class IArray2<T, E0, E1> extends AbstractArray<T> {

	E0 item0;
	E1 item1;

	/**
	 * Default constructor which initializes elements to null.
	 */
	public IArray2() {
	}

	/**
	 * Constructor which initializes elements to given values.
	 *
	 * @param item0		value for first element
	 * @param item1		value for second element
	 */
	public IArray2(E0 item0, E1 item1) {
		this.item0 = item0;
		this.item1 = item1;
	}

	/**
	 * @return first object
	 */
	public E0 getItem0() {
		return item0;
	}

	/**
	 * @return second object
	 */
	public E1 getItem1() {
		return item1;
	}

	/**
	 * Set value of first element.
	 *
	 * @param item0	value for first element
	 */
	public void setItem0(E0 item0) {
		this.item0 = item0;
	}

	/**
	 * Set value of second element.
	 *
	 * @param item1	value for second element
	 */
	public void setItem1(E1 item1) {
		this.item1 = item1;
	}

	// IArray

	@Override
	public int size() {
		return 2;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		CheckTools.checkIndex(index, 0, 2);
		return (index == 0) ? (T) item0 : (T) item1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(int index, T elem) {
		CheckTools.checkIndex(index, 0, 2);
		if (index == 0) {
			item0 = (E0) elem;
		} else {
			item1 = (E1) elem;
		}
	}

	@Override
	public Object[] toArray() {
		return new Object[] { item0, item1 };
	}

}
