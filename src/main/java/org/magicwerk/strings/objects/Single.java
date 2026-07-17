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
 * Class {@link Single} stores a single value.
 */
public class Single<E> extends AbstractArray<E> {

	E item0;

	public static <U> Single<U> of(U item0) {
		return new Single<>(item0);
	}

	/**
	 * Default constructor which initializes elements to null.
	 */
	public Single() {
	}

	/**
	 * Constructor which initializes elements to given values.
	 *
	 * @param item0		value
	 */
	public Single(E item0) {
		this.item0 = item0;
	}

	/**
	 * @return true if a value is stored, false for null
	 */
	public boolean has() {
		return item0 != null;
	}

	/**
	 * Clear by setting stored value to null.
	 */
	public void clear() {
		item0 = null;
	}

	/**
	 * @return object
	 */
	public E get() {
		return item0;
	}

	/**
	 * Set value of element.
	 *
	 * @param item value to set
	 */
	public void set(E item) {
		this.item0 = item;
	}

	/**
	 * @return object
	 */
	public E getItem0() {
		return item0;
	}

	/**
	 * Set value of element.
	 *
	 * @param item0 value to set
	 */
	public void setItem0(E item0) {
		this.item0 = item0;
	}

	// IArray

	@Override
	public int size() {
		return 1;
	}

	@Override
	public E get(int index) {
		CheckTools.checkIndex(index, 0, 1);
		return item0;
	}

	@Override
	public void set(int index, E elem) {
		CheckTools.checkIndex(index, 0, 1);
		item0 = elem;
	}

	@Override
	public Object[] toArray() {
		return new Object[] { item0 };
	}

}
