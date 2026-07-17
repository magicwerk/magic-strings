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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.magicwerk.strings.helper.ObjectTools;

/**
 * Interface {@link IListable} allows uniform access to a list or array.
 */
public interface IListable<E> extends Iterable<E> {

	public int size();

	public E get(int index);

	public default boolean contains(E elem) {
		return indexOf(elem) != -1;
	}

	public default int indexOf(E elem) {
		for (int i = 0; i < size(); i++) {
			if (ObjectTools.equals(get(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public default Iterator<E> iterator() {
		return new ListableIterator<>(this);
	}

	public static class ListableIterator<T> implements Iterator<T> {

		IListable<T> listable;
		int index;

		public ListableIterator(IListable<T> listable) {
			this.listable = listable;
		}

		@Override
		public boolean hasNext() {
			return index < listable.size();
		}

		@Override
		public T next() {
			if (!(index < listable.size())) {
				throw new NoSuchElementException();
			}
			return listable.get(index++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
