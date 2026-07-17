/*
 * Copyright 2011 by Thomas Mauch
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
package org.magicwerk.strings;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.primitive.ICharList;

/**
 * Interface {@link IString} represents a mutable string, i.e. it offers methods to change the the string instance inline.
 * It also adds additional read-only convenient methods to the one provided by {@link CharSequence}.
 * Its default implementations is {@link GapList}.
 */
public interface IString extends ICharList, CharSequence, Comparable<IString> {

	// Char

	/** Get the specified number of chars as string */
	public String getAsString(int index, int len);

	/** Get the specified number of chars as char array */
	public char[] getAsChars(int index, int len);

	public int indexOfCodePoint(int codepoint);

	// Code Point

	/** Get code point at specified index */
	public int getCodePoint(int index);

	/** Add code point at specified index */
	public void addCodePoint(int index, int codepoint);

	/** Remove code point at specified index */
	public void removeCodePoint(int index);

	// Code Points 

	/** Get the specified number of codepoints as string */
	public String getCodePointsAsString(int index, int len);

	/** Get the specified number of codepoints as char array */
	public char[] getCodePointsAsChars(int index, int len);

	/** Remove specified number of code points at specified index */
	public void removeCodePoints(int index, int len);

	// String

	public boolean addString(CharSequence str);

	public boolean addString(int index, CharSequence str);

	public boolean addString(CharSequence str, int off, int len);

	public boolean addString(int index, CharSequence str, int off, int len);

	public void putString(int index, CharSequence str);

	public void putString(int index, CharSequence str, int off, int len);

}
