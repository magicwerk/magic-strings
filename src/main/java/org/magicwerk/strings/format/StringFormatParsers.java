package org.magicwerk.strings.format;

import java.text.MessageFormat;

import org.magicwerk.strings.helper.ParseTools;

/**
 * Class {@link StringFormatParsers} provides implementations of interface {@link StringFormatParser}.
 */
public class StringFormatParsers {

	public static final StringFormatParser StringFormatParser = new NamedFormatParser('{', '}');
	public static final StringFormatParser MessageFormatParser = new MessageFormatParser();
	public static final LiteralFormatParser LiteralFormatParser = new LiteralFormatParser();

	/**
	 * Interface {@link StringFormatParser} defines the interface for parsing a format string into a string format.
	 */
	public interface StringFormatParser {
		void parse(String format, StringFormatBase stringFormat);
	}

	/**
	 * Class {@link LiteralFormatParser} will not do any formatting, but return the input string unchanged.
	 */
	public static class LiteralFormatParser implements StringFormatParser {

		public static StringFormat create(String str) {
			return new StringFormat(str, StringFormatParsers.LiteralFormatParser);
		}

		@Override
		public void parse(String format, StringFormatBase sf) {
			sf.addConst(format);
		}
	}

	/**
	 * Class {@link SingleCharFormatParser} will parse a format string with a single char argument marker.
	 * Example: a parameterized SQL string like "update pt set x=?, y=?"
	 */
	public static class SingleCharFormatParser implements StringFormatParser {

		char arg;

		public SingleCharFormatParser(char arg) {
			this.arg = arg;
		}

		@Override
		public void parse(String format, StringFormatBase sf) {
			int len = format.length();
			int i = 0;
			int start = 0;
			while (i < len) {
				char c = format.charAt(i);
				if (c == arg) {
					if (i < len - 1 && format.charAt(i + 1) == arg) {
						// double character: escape character
						sf.addConst(format.substring(start, i));
						start = i + 1;
						i += 2;
					} else {
						// single character: start argument
						sf.addConst(format.substring(start, i));
						sf.addParam(null);
						i++;
						start = i;
					}
				} else {
					i++;
				}
			}
			if (start < i) {
				sf.addConst(format.substring(start));
			}
		}
	}

	/**
	 * Class {@link NamedFormatParser} will parse a format string with the specified argument marker for start and end.
	 * Example: a string with format "update <>" can be handled with "new NamedFormatParser('<', '>')".
	 */
	public static class NamedFormatParser implements StringFormatParser {

		char startArg;
		char endArg;

		/** Create {@link NamedFormatParser} */
		public NamedFormatParser(char startArg, char endArg) {
			this.startArg = startArg;
			this.endArg = endArg;
		}

		@Override
		public void parse(String format, StringFormatBase sf) {
			StringBuilder keyBuf = new StringBuilder();
			int len = format.length();
			boolean text = true;
			int i = 0;
			int start = 0;
			while (i < len) {
				char c = format.charAt(i);
				if (text) {
					// we are in text mode, so look for start of argument
					if (c == startArg) {
						if (i < len - 1 && format.charAt(i + 1) == startArg) {
							// double character: escape character
							sf.addConst(format.substring(start, i));
							start = i + 1;
							i += 2;
						} else {
							// single character: start argument
							sf.addConst(format.substring(start, i));
							i++;
							start = i;
							text = false;
							keyBuf.delete(0, keyBuf.length());
						}
					} else {
						i++;
					}
				} else {
					// we are in argument mode, so look for end of argument
					if (c == endArg) {
						// The end character cannot be escaped as otherwise "{ARG}}}" would be wrong interpreted
						keyBuf.append(format.substring(start, i));
						i++;
						start = i;
						text = true;
						String key = keyBuf.toString();
						Object param;
						if (key.length() == 0) {
							param = null;
						} else {
							Integer index = ParseTools.parseIntIf(key);
							param = (index != null) ? index : key;
						}
						sf.addParam(param);
					} else {
						i++;
					}
				}
			}
			if (!text) {
				throw new IllegalArgumentException("Argument not closed");
			}
			if (start < i) {
				sf.addConst(format.substring(start));
			}
		}
	}

	/** 
	 * Class {@link MessageFormatParser} accepts a format string as used by {@link MessageFormat}
	 */
	public static class MessageFormatParser implements StringFormatParser {

		enum Mode {
			NORMAL,
			TEXT,
			ARG
		}

		char startArg = '{';
		char endArg = '}';
		char quoteChar = '\'';

		@Override
		public void parse(String format, StringFormatBase sf) {
			StringBuilder keyBuf = new StringBuilder();
			int len = format.length();
			Mode mode = Mode.NORMAL;
			int i = 0;
			int start = 0;
			while (i < len) {
				char c = format.charAt(i);
				if (mode == Mode.NORMAL) {
					// we are in text mode, so look for start of argument
					if (c == startArg) {
						sf.addConst(format.substring(start, i));
						i++;
						start = i;
						mode = Mode.ARG;
						keyBuf.delete(0, keyBuf.length());
					} else if (c == quoteChar) {
						sf.addConst(format.substring(start, i));
						i++;
						start = i;
						mode = Mode.TEXT;
					} else {
						i++;
					}
				} else if (mode == Mode.ARG) {
					// we are in argument mode, so look for end of argument
					if (c == endArg) {
						keyBuf.append(format.substring(start, i));
						i++;
						start = i;
						mode = Mode.NORMAL;
						Integer index = ParseTools.parseInt(keyBuf.toString());
						sf.addParam(index);
					} else {
						i++;
					}
				} else if (mode == Mode.TEXT) {
					if (c == quoteChar) {
						if (i < len - 1 && format.charAt(i + 1) == quoteChar) {
							// double character: escape character
							sf.addConst(format.substring(start, i));
							start = i + 1;
							i += 2;
						} else {
							// single character: end text
							sf.addConst(format.substring(start, i));
							i++;
							start = i;
							mode = Mode.NORMAL;
						}
					} else {
						i++;
					}
				} else {
					assert (false);
				}
			}
			if (mode == Mode.ARG) {
				throw new IllegalArgumentException("Argument not closed");
			}
			if (start < i) {
				sf.addConst(format.substring(start));
			}
		}
	}

}