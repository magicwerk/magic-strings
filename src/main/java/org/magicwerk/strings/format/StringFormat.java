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

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.format.StringFormatParsers.StringFormatParser;
import org.magicwerk.strings.mapper.IStringExistsMapper;
import org.magicwerk.strings.mapper.IStringMapper;

/**
 * Class {@link StringFormat} parses a format string into an internal representation which is used to apply the arguments later.
 * It is designed to be used several times once it has been constructed.
 */
public class StringFormat extends StringFormatBase {

	/**
	 * When the format string is compiled, it is split up in an array of {@link IStringProvider} objects which will each
	 * append a substring to the complete result string when called for printing.
	 */
	IList<IStringProvider> providers;
	/** Length of constant strings (used for pre-sizing the buffer) */
	int constLen;
	/** Number of parameter strings (used for pre-sizing the buffer) */
	int numParams;

	//

	/**
	 * Constructor.
	 *
	 * @param format format string
	 */
	public StringFormat(String format) {
		this(format, StringFormatParsers.StringFormatParser);
	}

	public StringFormat(String format, StringFormatParser parser) {
		this(format, parser, null);
	}

	public StringFormat(String format, IStringExistsMapper existsMapper) {
		this(format, StringFormatParsers.StringFormatParser, existsMapper);
	}

	/**
	 * Constructor.
	 *
	 * @param format		format string
	 * @param existsMapper	mapper which checks for existence of parameters or null
	 * @throws IllegalArgumentException if the format string is invalid
	 */
	public StringFormat(String format, StringFormatParser parser, IStringExistsMapper existsMapper) {
		this.format = format;
		this.existsMapper = existsMapper;

		providers = GapList.create();
		parser.parse(format, this);
	}

	@Override
	public void addConst(String str) {
		// Merge input with last entry if it is also constant
		IStringProvider last = providers.getLastOrNull();
		if (last instanceof ConstString) {
			ConstString cs = (ConstString) last;
			IStringProvider strProv = new ConstString(cs.getConst() + str);
			providers.set(providers.size() - 1, strProv);
		} else {
			IStringProvider strProv = new ConstString(str);
			providers.add(strProv);
		}
		constLen += str.length();
	}

	@Override
	public void addParam(Object key) {
		if (existsMapper != null) {
			if (!existsMapper.exists(key)) {
				throw new IllegalArgumentException("Invalid parameter " + key);
			}
		}
		IStringProvider strProv = new ParamString(key);
		providers.add(strProv);
		numParams++;
	}

	@Override
	protected String doFormat(IStringMapper mapper) {
		StringBuilder buf = new StringBuilder();
		int size = providers.size();
		for (int i = 0; i < size; i++) {
			IStringProvider strProv = providers.get(i);
			buf.append(strProv.getString(mapper));
		}
		return buf.toString();
	}

	protected int getInitialBufferSize(int constLen, int numParams) {
		return constLen + numParams * getParamBufferSize();
	}

	protected int getParamBufferSize() {
		return 16;
	}

	/** 
	 * This method delegates handling of the parsed providers to an external interface.
	 * This can be handy if the output should not be a simple string, but must be written to another target.
	 */
	public void print(IStringFormat formatter) {
		for (IStringProvider strProv : providers) {
			if (strProv instanceof ConstString) {
				formatter.printConst(((ConstString) strProv).getConst());
			} else if (strProv instanceof ParamString) {
				formatter.printParam(((ParamString) strProv).getParam());
			} else {
				throw new AssertionError();
			}
		}
	}

	/** Return number of parameters expected by the message format */
	public int getNumParams() {
		return numParams;
	}

	/**
	 * Returns the parameters in the order of occurrence in the format string.
	 * If a parameter occurs several times, it is also returned several times.
	 * 
	 * @return list with parameters
	 */
	public IList<Object> getArgs() {
		IList<Object> args = GapList.create();
		for (IStringProvider str : providers) {
			if (str instanceof ParamString) {
				ParamString pstr = (ParamString) str;
				args.add(pstr.getParam());
			}
		}
		return args;
	}

	/**
	 * Returns the literal string represented by the format, or null if not a literal.
	 * <p>
	 * Use {@link #isLiteralString} to determine whether the format represents a literal string. 
	 */
	public String getLiteralString() {
		if (providers.size() == 1) {
			IStringProvider provider = providers.getSingle();
			if (provider instanceof ConstString) {
				return ((ConstString) provider).getConst();
			}
		}
		return null;
	}

	/**
	 * Determines whether the format represents a literal string.
	 * <p>
	 * Use {@link #getLiteralString} to retrieve the literal string represented by the format. 
	 */
	public boolean isLiteralString() {
		if (providers.size() == 1) {
			return providers.getSingle() instanceof ConstString;
		}
		return false;
	}

	/**
	 * A StringProvider provides a String if called.
	 */
	interface IStringProvider {
		/**
		 * Return string to provide.
		 */
		String getString(IStringMapper mapper);
	}

	/**
	 * Class {@link ConstString} provides a constant string.
	 */
	static class ConstString implements IStringProvider {
		final String str;

		/**
		 * Constructor.
		 *
		 * @param str	string to provide later
		 */
		public ConstString(String str) {
			this.str = str;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that the provided {@link IStringMapper} is not used.
		 */
		@Override
		public String getString(IStringMapper mapper) {
			return str;
		}

		String getConst() {
			return str;
		}

		@Override
		public String toString() {
			return "ConstString: " + str;
		}
	}

	/**
	 * Class {@link ParamString} provides a string by retrieving it from a StringMapper.
	 */
	static class ParamString implements IStringProvider {
		final Object param;

		/**
		 * @param param	parameter (type integer or string)
		 */
		public ParamString(Object param) {
			this.param = param;
		}

		/**
		 * @return the param
		 */
		public Object getParam() {
			return param;
		}

		@Override
		public String getString(IStringMapper mapper) {
			return mapper.getString(param);
		}

		@Override
		public String toString() {
			return "ParamString: " + param;
		}
	}
}
