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
package org.magicwerk.strings.format;

import org.magicwerk.strings.format.StringFormatParsers.StringFormatParser;
import org.magicwerk.strings.mapper.IStringExistsMapper;
import org.magicwerk.strings.mapper.IStringMapper;

/**
 * Class {@link StringFormatterImpl} build a formatted string by parsing a format string and a applying the arguments at the same time.
 * It is designed to be used once.
 */
public class StringFormatterImpl extends StringFormatBase {

	StringBuilder buf;
	IStringMapper mapper;
	StringFormatParser parser;

	//

	public StringFormatterImpl(String format) {
		this(format, StringFormatParsers.StringFormatParser);
	}

	public StringFormatterImpl(String format, StringFormatParser parser) {
		this(format, parser, null);
	}

	public StringFormatterImpl(String format, IStringExistsMapper existsMapper) {
		this(format, StringFormatParsers.StringFormatParser, existsMapper);
	}

	public StringFormatterImpl(String format, StringFormatParser parser, IStringExistsMapper existsMapper) {
		this.format = format;
		this.parser = parser;
		this.existsMapper = existsMapper;
	}

	//

	@Override
	protected String doFormat(IStringMapper mapper) {
		this.mapper = mapper;
		buf = new StringBuilder();
		parser.parse(format, this);
		return buf.toString();
	}

	@Override
	public void addConst(String str) {
		buf.append(str);
	}

	@Override
	public void addParam(Object key) {
		if (existsMapper != null) {
			if (!existsMapper.exists(key)) {
				throw new IllegalArgumentException("Invalid parameter " + key);
			}
		}

		String str = mapper.getString(key);
		buf.append(str);
	}
}
