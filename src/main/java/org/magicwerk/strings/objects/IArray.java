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
 * Interface {@link IArray} represents an array storing several items of the same type.
 */
public interface IArray<E> extends IListable<E>, Comparable<AbstractArray<E>> {

	@Override
	public int size();

	@Override
	public E get(int index);

	/** Set element at specified position */
	public void set(int index, E elem);

	/** Copy content to array */
	public Object[] toArray();

	/**
	 * @return first value in array, throws an exception if array is empty
	 */
	public E getFirst();

	/**
	 * @return last value in array, throws an exception if array is empty
	 */
	public E getLast();

	/**
	 * @return first non null value stored in array, null if none found or array is empty
	 */
	public E getAny();

}
