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
 * Class {@link IArray3} stores three objects of different type.
 */
public class IArray3<T, E0, E1, E2> extends AbstractArray<T> {

	E0 item0;
	E1 item1;
	E2 item2;

	/**
	 * Default constructor which initializes elements to null.
	 */
	public IArray3() {
	}

	/**
	 * Constructor which initializes elements to given values.
	 *
	 * @param item0	value for first element
	 * @param item1	value for second element
	 * @param item2	value for third element
	 */
	public IArray3(E0 item0, E1 item1, E2 item2) {
		this.item0 = item0;
		this.item1 = item1;
		this.item2 = item2;
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
	 * @return third object
	 */
	public E2 getItem2() {
		return item2;
	}

	/**
	 * Set value of first element.
	 *
	 * @param item0 value for first element
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

	/**
	 * Set value of third element.
	 *
	 * @param item2	value for third element
	 */
	public void setItem2(E2 item2) {
		this.item2 = item2;
	}

	// IArray

	@Override
	public int size() {
		return 3;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		CheckTools.checkIndex(index, 0, 3);
		if (index == 0) {
			return (T) item0;
		} else if (index == 1) {
			return (T) item1;
		} else {
			return (T) item2;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(int index, T elem) {
		CheckTools.checkIndex(index, 0, 3);
		if (index == 0) {
			item0 = (E0) elem;
		} else if (index == 1) {
			item1 = (E1) elem;
		} else {
			item2 = (E2) elem;
		}
	}

	@Override
	public Object[] toArray() {
		return new Object[] { item0, item1, item2 };
	}

}
