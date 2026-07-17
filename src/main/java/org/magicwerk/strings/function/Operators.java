/*
 * Copyright 2014 by Thomas Mauch
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
package org.magicwerk.strings.function;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Transformers.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Operators {

	/**
	 * Transformer which returns for any input the same constant value.
	 */
	public static class ValueTransformer<T> implements UnaryOperator<T> {
		T value;

		public ValueTransformer(T value) {
			this.value = value;
		}

		@Override
		public T apply(T elem) {
			return value;
		}
	};

	/**
	 * Transformer which calls several transformers in a row.
	 */
	public static class CombineTransformer<T> implements UnaryOperator<T> {
		UnaryOperator<T>[] transformers;

		public CombineTransformer(UnaryOperator<T>[] transformers) {
			this.transformers = transformers;
		}

		@Override
		public T apply(T elem) {
			for (UnaryOperator<T> transformer : transformers) {
				elem = transformer.apply(elem);
			}
			return elem;
		}
	};

	/**
	 * Returns transformer which returns the input value as output.
	 */
	public static <T> UnaryOperator<T> identity() {
		return o -> o;
	}

	/**
	 * Returns transformer which returns for any input the same constant value.
	 *
	 * @param value	value to return
	 */
	public static <T> UnaryOperator<T> value(T value) {
		return new ValueTransformer<T>(value);
	}

	/**
	 * Returns transformer which calls several transformers in a row.
	 *
	 * @param transformers transformers to call in a row
	 */
	@SafeVarargs
	public static <T> UnaryOperator<T> combine(UnaryOperator<T>... transformers) {
		return new CombineTransformer<T>(transformers);
	}

	public static <T> UnaryOperator<T> throwingUnary() {
		return o -> {
			CheckTools.error("Operator must not be called: {}", o);
			return null;
		};
	}

	public static <T> BinaryOperator<T> throwingBinary() {
		return (o1, o2) -> {
			CheckTools.error("Operator must not be called: {}, {}", o1, o2);
			return null;
		};
	}

}
