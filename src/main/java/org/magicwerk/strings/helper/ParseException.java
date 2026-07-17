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
package org.magicwerk.strings.helper;

/**
 * A ParseException is thrown if parsing fails.
 * It extends IllegalArgumentException.
 * Compared to java.text.ParseException, this exception is not checked.
 */
public class ParseException extends TypeException {

	public ParseException(String msg) {
		super(msg);
	}

	public ParseException(String msg, Throwable e) {
		super(msg, e);
	}

	public ParseException(Throwable e) {
		super(e);
	}
}
