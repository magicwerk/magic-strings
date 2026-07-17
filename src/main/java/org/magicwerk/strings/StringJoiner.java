package org.magicwerk.strings;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.magicwerk.strings.function.TriConsumer;
import org.magicwerk.strings.helper.FuncTools;

/**
 * Class {@link StringJoiner} implements joining of strings.
 */
public interface StringJoiner {

	/** Join all objects as configured after being converted to a string */
	public <E> String joinObjects(@SuppressWarnings("unchecked") E... args);

	/** Join all objects as configured after being converted to a string */
	public <E> String joinObjects(Iterable<E> args);

	/** Join all strings as configured */
	public <E extends CharSequence> String joinStrings(@SuppressWarnings("unchecked") E... args);

	/** Join all strings as configured */
	public <E extends CharSequence> String joinStrings(Iterable<E> list);

	//

	/** Build {@link StringJoiner} with specified builder function */
	public static StringJoiner build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringJoiner}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	//

	/**
	 * Class {@link Builder} allows to create instances of {@link StringJoiner}.
	 */
	public static class Builder {

		boolean ignoreNull;
		String nullString;
		String elemMarker;
		Function<Object, String> printFnc;

		// Begin/end marker
		boolean beginEndMarkerAlways;
		String endMarker;
		String beginMarker;

		int bufSize = 1024;

		//

		public Builder setJoin(char elemMarker) {
			this.elemMarker = Character.toString(elemMarker);
			return this;
		}

		public Builder setJoin(int elemMarker) {
			this.elemMarker = Character.toString(elemMarker);
			return this;
		}

		public Builder setJoin(String elemMarker) {
			this.elemMarker = elemMarker;
			return this;
		}

		public Builder setIgnoreNull(boolean ignoreNull) {
			this.ignoreNull = ignoreNull;
			return this;
		}

		public Builder setNullString(String nullString) {
			this.nullString = nullString;
			return this;
		}

		public Builder setPrintFunction(Function<Object, String> printFunction) {
			this.printFnc = printFunction;
			return this;
		}

		public Builder setBeginMarker(String beginMarker) {
			this.beginMarker = beginMarker;
			return this;
		}

		public Builder setEndMarker(String endMarker) {
			this.endMarker = endMarker;
			return this;
		}

		public Builder setBeginEndMarkersAlways(boolean beginEndMarkerAlways) {
			this.beginEndMarkerAlways = beginEndMarkerAlways;
			return this;
		}

		/**
		 * Build an instance of {@link StringJoiner} with the specified configuration.
		 */
		public StringJoiner build() {
			StringJoinerImplBase sjb;
			if (!ignoreNull) {
				StringJoinerNoIgnoreImpl sj = new StringJoinerNoIgnoreImpl();
				sjb = sj;
			} else {
				StringJoinerIgnoreImpl sj = new StringJoinerIgnoreImpl();
				sjb = sj;
			}

			sjb.bufSize = bufSize;
			sjb.separatorStr = elemMarker;
			sjb.separatorLen = StringTools.length(elemMarker);
			sjb.nullStr = nullString;
			sjb.nullLen = StringTools.length(nullString);
			sjb.formatObj = (printFnc != null) ? printFnc : Object::toString;

			if (beginMarker != null && endMarker != null) {
				sjb.beginMarker = FuncTools.nvl(beginMarker, "");
				sjb.endMarker = FuncTools.nvl(endMarker, "");
				sjb.beginEndMarker = sjb.beginMarker + sjb.endMarker;
				sjb.beginEndMarkerAlways = beginEndMarkerAlways;
			}

			return sjb;
		}
	}

	//

	abstract static class StringJoinerImplBase implements StringJoiner {

		static final int NULL_LEN_IGNORE = -1;
		static final int NULL_LEN_ERROR = -2;

		String nullStr;
		int nullLen;
		Function<Object, String> formatObj;
		String separatorStr;
		int separatorLen;

		boolean beginEndMarkerAlways;
		String beginEndMarker;
		String beginMarker;
		String endMarker;

		/** Initial buffer size to use if the length cannot be precalculated */
		int bufSize;

		//

		abstract <E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, Iterable<E> args);

		abstract <E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, @SuppressWarnings("unchecked") E... args);

		void appendObject(StringBuilder buf, Object arg, String separator) {
			String str = formatObject(arg);
			appendString(buf, str, separator);
		}

		String formatObject(Object arg) {
			if (arg == null) {
				return null;
			} else {
				return formatObj.apply(arg);
			}
		}

		void appendString(StringBuilder buf, CharSequence str, String separator) {
			if (str == null) {
				str = nullStr;
			}
			if (str != null) {
				if (separator != null) {
					buf.append(separator);
				}
				buf.append(str);
			}
		}
		//

		@Override
		public <E extends CharSequence> String joinStrings(Iterable<E> args) {
			int len = getStringsLength(args);
			return doJoin(this::appendString, len, args).toString();
		}

		@Override
		public String joinStrings(CharSequence... args) {
			int len = getStringsLength(args);
			return doJoin(this::appendString, len, args).toString();
		}

		@Override
		public <E> String joinObjects(Iterable<E> args) {
			return doJoin(this::appendObject, bufSize, args).toString();
		}

		@Override
		public <E> String joinObjects(@SuppressWarnings("unchecked") E... args) {
			return doJoin(this::appendObject, bufSize, args).toString();
		}

		//

		StringBuilder createStringBuilder(int size) {
			if (beginEndMarker == null) {
				return new StringBuilder(size);
			} else {
				size += beginEndMarker.length();
				return new StringBuilder(size).append(beginMarker);
			}
		}

		CharSequence returnJoinResult(StringBuilder buf) {
			if (beginEndMarker != null) {
				buf.append(endMarker);
			}
			return buf.toString();
		}

		String returnEmptyResult() {
			return (beginEndMarkerAlways) ? beginEndMarker : "";
		}

		//

		/** Returns resulting string length if all arguments are joined */
		<E extends CharSequence> int getStringsLength(Iterable<E> args) {
			int len = 0;
			for (CharSequence arg : args) {
				if (arg == null) {
					// case if null values is not allowed is handled later
					len += nullLen;
				} else {
					len += arg.length();
				}
				len += separatorLen;
			}
			return len;
		}

		/** Returns resulting string length if all arguments are joined */
		int getStringsLength(CharSequence... args) {
			int num = args.length;
			if (num == 0) {
				return 0;
			}
			int len = (num - 1) * separatorLen;
			for (CharSequence arg : args) {
				if (arg == null) {
					// case if null values is not allowed is handled later
					len += nullLen;
				} else {
					len += arg.length();
				}
			}
			return len;
		}
	}

	static class StringJoinerNoIgnoreImpl extends StringJoinerImplBase {

		@Override
		<E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, Iterable<E> args) {
			Iterator<E> iter = args.iterator();
			if (!iter.hasNext()) {
				return returnEmptyResult();
			}

			// Handle first element
			StringBuilder buf = createStringBuilder(bufSize);
			append.accept(buf, iter.next(), null);

			// Handle remaining elements
			while (iter.hasNext()) {
				append.accept(buf, iter.next(), separatorStr);
			}
			return returnJoinResult(buf);
		}

		@Override
		<E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, @SuppressWarnings("unchecked") E... args) {
			if (args.length == 0) {
				return returnEmptyResult();
			}

			// Handle first element
			StringBuilder buf = createStringBuilder(bufSize);
			append.accept(buf, args[0], null);

			// Handle remaining elements
			for (int i = 1; i < args.length; i++) {
				append.accept(buf, args[i], separatorStr);
			}
			return returnJoinResult(buf);
		}
	}

	/**
	 * 
	 */
	static class StringJoinerIgnoreImpl extends StringJoinerNoIgnoreImpl {

		<E extends CharSequence> int getStringsLength_2(Iterable<E> args) {
			int len = 0;
			boolean first = true;
			for (CharSequence arg : args) {
				if (arg == null) {
					if (nullLen == NULL_LEN_IGNORE) {
						continue;
					} else if (nullLen == NULL_LEN_ERROR) {
						throw new NullPointerException();
					}
					len += nullLen;
				} else {
					len += arg.length();
				}
				if (first) {
					first = true;
				} else {
					len += separatorLen;
				}
			}
			return len;
		}

		/** Returns resulting string length if all arguments are joined */
		int getStringsLength_2(CharSequence... args) {
			int len = 0;
			boolean first = true;
			for (CharSequence arg : args) {
				if (arg == null) {
					if (nullLen == NULL_LEN_IGNORE) {
						continue;
					} else if (nullLen == NULL_LEN_ERROR) {
						throw new NullPointerException();
					}
					len += nullLen;
				} else {
					len += arg.length();
				}
				if (first) {
					first = true;
				} else {
					len += separatorLen;
				}
			}
			return len;
		}

		//

		@Override
		<E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, Iterable<E> args) {
			StringBuilder buf = null;
			Iterator<E> iter = args.iterator();

			// Handle first element
			while (iter.hasNext()) {
				E arg = iter.next();
				if (arg == null) {
					continue;
				}

				buf = createStringBuilder(bufSize);
				append.accept(buf, arg, null);
				break;
			}
			if (buf == null) {
				return returnEmptyResult();
			}

			// Handle remaining elements
			while (iter.hasNext()) {
				E arg = iter.next();
				if (arg == null) {
					continue;
				}

				append.accept(buf, arg, separatorStr);
			}
			return returnJoinResult(buf);
		}

		@Override
		<E> CharSequence doJoin(TriConsumer<StringBuilder, E, String> append, int bufSize, @SuppressWarnings("unchecked") E... args) {
			if (args.length == 0) {
				return returnEmptyResult();
			}

			// Handle first element
			StringBuilder buf = null;
			int i;
			for (i = 0; i < args.length; i++) {
				E arg = args[i];
				if (arg == null) {
					continue;
				}

				buf = createStringBuilder(bufSize);
				append.accept(buf, arg, null);
				break;
			}
			if (buf == null) {
				return returnEmptyResult();
			}

			// Handle remaining elements
			for (i++; i < args.length; i++) {
				E arg = args[i];
				if (arg == null) {
					continue;
				}

				append.accept(buf, arg, separatorStr);
			}
			return returnJoinResult(buf);
		}
	}
}