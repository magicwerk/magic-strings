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
 * Class {@link Tuple} stores 2 objects of different types.
 * <p>
 * Use {@link Pair} for same type.
 */
public class Tuple<E0, E1> extends IArray2<Object, E0, E1> {

	public static <U0, U1> Tuple<U0, U1> of(U0 item0, U1 item1) {
		return new Tuple<>(item0, item1);
	}

	/**
	 * Default constructor which initializes elements to null.
	 */
	public Tuple() {
	}

	/**
	 * Constructor which initializes elements to given values.
	 *
	 * @param item0		value for first element
	 * @param item1		value for second element
	 */
	public Tuple(E0 item0, E1 item1) {
		super(item0, item1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E0 getFirst() {
		return (E0) super.getFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E1 getLast() {
		return (E1) super.getLast();
	}
}
