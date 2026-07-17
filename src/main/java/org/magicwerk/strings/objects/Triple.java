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
 * Class {@link Triple} stores 3 objects of different types.
 * <p>
 * Use {@link Triad} for same type.
 */
public class Triple<E0, E1, E2> extends IArray3<Object, E0, E1, E2> {

	public static <U0, U1, U2> Triple<U0, U1, U2> of(U0 item0, U1 item1, U2 item2) {
		return new Triple<>(item0, item1, item2);
	}

	/**
	 * Default constructor which initializes elements to null.
	 */
	public Triple() {
	}

	/**
	 * Constructor which initializes elements to given values.
	 *
	 * @param item0	value for first element
	 * @param item1	value for second element
	 * @param item2	value for third element
	 */
	public Triple(E0 item0, E1 item1, E2 item2) {
		super(item0, item1, item2);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E0 getFirst() {
		return (E0) super.getFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E2 getLast() {
		return (E2) super.getLast();
	}

}
