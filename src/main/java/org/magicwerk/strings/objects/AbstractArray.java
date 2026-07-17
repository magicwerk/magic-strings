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

import java.util.Collection;
import java.util.Iterator;

import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;

/**
 * Class {@link AbstractArray} implements an array which stores several items of the same type.
 */
public abstract class AbstractArray<E> implements IArray<E> {

	// This abstract class is only needed because a default method in an interface cannot override a method from Object.
	// Otherwise all these methods could be moved to IArray.

	protected void init(Collection<E> coll) {
		int size = coll.size();
		CheckTools.check(size == size());
		Iterator<E> iter = coll.iterator();
		for (int i = 0; i < size; i++) {
			set(i, iter.next());
		}
	}

	@Override
	public E getFirst() {
		return get(0);
	}

	@Override
	public E getLast() {
		return get(size() - 1);
	}

	@Override
	public E getAny() {
		for (int i = 0; i < size(); i++) {
			E val = get(i);
			if (val != null) {
				return val;
			}
		}
		return null;
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[size()];
		for (int i = 0; i < size(); i++) {
			array[i] = get(i);
		}
		return array;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof IArray<?>)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		IArray<E> list = (IArray<E>) obj;
		int size = size();
		if (size != list.size()) {
			return false;
		}
		for (int i = 0; i < size; i++) {
			if (!isElementEqual(get(i), list.get(i))) {
				return false;
			}
		}
		return true;
	}

	protected boolean isElementEqual(E obj0, E obj1) {
		return ObjectTools.equals(obj0, obj1);
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		int size = size();
		for (int i = 0; i < size; i++) {
			E elem = get(i);
			hashCode = 31 * hashCode + ObjectTools.hashCode(elem);
		}
		return hashCode;
	}

	@Override
	public int compareTo(AbstractArray<E> that) {
		int size0 = this.size();
		int size1 = that.size();
		int num = Math.min(size0, size1);
		for (int i = 0; i < num; i++) {
			E obj0 = this.get(i);
			E obj1 = that.get(i);
			CheckTools.check(ObjectTools.isInstanceOrNull(obj0, Comparable.class) && ObjectTools.isInstanceOrNull(obj1, Comparable.class),
					"objects in IArray do not implement Comparable");
			@SuppressWarnings({ "rawtypes", "unchecked" })
			int cmp = ObjectTools.compare((Comparable) obj0, (Comparable) obj1);
			if (cmp != 0) {
				return cmp;
			}
		}
		return size1 - size0;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		int size = size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(get(i));
		}
		buf.append("]");
		return buf.toString();
	}

}
