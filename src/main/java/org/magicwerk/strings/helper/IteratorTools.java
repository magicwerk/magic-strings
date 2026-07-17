/*
 * Copyright 2012 by Thomas Mauch
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
package org.magicwerk.strings.helper;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class {@link IteratorTools} offers functionality around {@link Iterable}, {@link Iterator}, and {@link Enumeration}.
 */
public class IteratorTools {

	/**
	 * Create {@link Stream} out of {@link Iterator}. The stream is sequential.
	 */
	public static <T> Stream<T> asStream(Iterator<T> iterator) {
		return asStream(iterator, false);
	}

	/**
	 * Create {@link Stream} out of {@link Iterator}. The stream can be sequential or parallel.
	 */
	public static <T> Stream<T> asStream(Iterator<T> iterator, boolean parallel) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel);
	}

	/**
	 * Create {@link Stream} out of {@link Iterable}. The stream is sequential.
	 */
	public static <T> Stream<T> asStream(Iterable<T> iterable) {
		return asStream(iterable, false);
	}

	/**
	 * Create {@link Stream} out of {@link Iterable}. The stream can be sequential or parallel.
	 */
	public static <T> Stream<T> asStream(Iterable<T> iterable, boolean parallel) {
		return StreamSupport.stream(iterable.spliterator(), parallel);
	}

}
