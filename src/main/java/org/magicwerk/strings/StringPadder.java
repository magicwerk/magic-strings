package org.magicwerk.strings;

import java.util.List;
import java.util.function.UnaryOperator;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.ICollectionTools;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.BuilderHelper.BuilderStringBase;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilderBase;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CollectionTools;

/**
 * Class {@link StringPadder} implements padding of strings.
 */
public interface StringPadder extends IStringTransformer {

	// IStringTransformer

	/**
	 * This method calls {@link #pad}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return pad(input);
	}

	/**
	 * This method calls {@link #padInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		padInline(input);
	}

	//

	/**
	 * Pad string as configured.
	 */
	default String pad(String str) {
		return (String) pad((CharSequence) str);
	}

	/**
	 * Pad a string as configured.
	 */
	CharSequence pad(CharSequence str);

	/**
	 * Pad mutable string as configured.
	 */
	void padInline(IString str);

	//

	/**
	 * Pad a list of strings as configured.
	 * If the length is not explicitly specified, the maximum length of all strings will be used. 
	 * 
	 * @param strs	list of strings to pad (content of list will be modified)
	 */
	IList<String> padAll(List<String> strs);

	//

	/** Build {@link StringPadder} with specified builder function */
	public static StringPadder build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringPadder}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Get {@link StringPadderInline} to execute padding calls where parameters can be changed dynamically.
	 */
	public static StringPadderInline inline() {
		return new StringPadderInline();
	}

	//

	/** Enum {@link PadMode} defines how text is padded, one of {@link #LEFT}, {@link #RIGHT}, {@link #CENTER} */
	public enum PadMode {
		/** Fill characters are added at right, text is left aligned */
		RIGHT,
		/** Fill characters are added left and right, text is centered */
		CENTER,
		/** Fill characters are added at left, text is right aligned */
		LEFT
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringPadder}.
	 */
	public static class Builder extends BuilderImpl<Builder> implements IStringTransformerBuilder {

		@Override
		public StringPadder build() {
			return doBuild();
		}
	}

	/**
	 * Class {@link Config} allows to configure instances of {@link StringPadder} 
	 * without the possibility to build them explicitly (this is done implicitly as part of an coordinating action).
	 * This is needed as the same config information is used for both {@link StringPadder} and {@link StringAligner}.
	 */
	public static class Config extends BuilderImpl<Config> {
	}

	/**
	 * Class {@link BuilderImpl} is the private implementation of {@link Builder} / {@link Config}.
	 */
	static class BuilderImpl<T extends BuilderImpl<T>> implements IStringTransformerBuilderBase {
		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;
		Integer length;
		PadMode padMode = PadMode.RIGHT;
		BuilderStringBase padString = new BuilderStringBase();
		boolean alignFillStr;
		boolean centerRight;

		//

		BuilderImpl() {
			padString.setChar(BuilderHelper.padderCharSpace);
		}

		@SuppressWarnings("unchecked")
		T castThis() {
			return (T) this;
		}

		@Override
		public T setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return castThis();
		}

		public T setLength(int length) {
			this.length = length;
			return castThis();
		}

		public T setPadMode(PadMode padMode) {
			this.padMode = padMode;
			return castThis();
		}

		public T setPadChar(char padChar) {
			padString.setChar(padChar);
			return castThis();
		}

		public T setPadString(int padCodePoint) {
			padString.setCodePoint(padCodePoint);
			return castThis();
		}

		public T setPadString(String padStr) {
			padString.setString(padStr);
			return castThis();
		}

		public T setAlignFillStr(boolean alignFillStr) {
			this.alignFillStr = alignFillStr;
			return castThis();
		}

		public T setCenterRight(boolean centerRight) {
			this.centerRight = centerRight;
			return castThis();
		}

		/** Build an instance of {@link StringPadder} with the specified configuration */
		StringPadderImpl doBuild() {
			// Method is made public in extending class Builder

			check();

			StringPadderImpl st = new StringPadderImpl();
			st.returnMode = returnMode;
			st.length = length;
			st.padMode = padMode;
			st.alignFillStr = alignFillStr;
			st.centerRight = centerRight;

			Character padChar = padString.doGetChar();
			if (padChar != null) {
				st.fillChar = padChar;
			} else {
				// represent code point as string
				st.fillStr = padString.getAsString();
			}
			return st;
		}

		void check() {
			CheckTools.check(length != null && length >= 0, "length must be set and >= 0");
		}
	}

	//

	public static class StringPadderImpl extends StringTransformerImpl implements StringPadder {

		int length = -1;
		PadMode padMode;
		char fillChar;
		String fillStr;
		boolean alignFillStr;
		boolean centerRight = true;

		//

		@Override
		public CharSequence pad(CharSequence str) {
			return transform(str);
		}

		@Override
		public void padInline(IString str) {
			transformInline(str);
		}

		@Override
		public CharSequence doTransform(CharSequence str) {
			if (str.length() < length) {
				return doPad(str, length);
			} else {
				return null;
			}
		}

		@Override
		public IString doTransformInline(IString str) {
			if (str.length() < length) {
				char[] chars = doPadArray(str, length);
				str.initArray(chars);
				return str;
			} else {
				return null;
			}
		}

		protected String doPad(CharSequence str, int length) {
			char[] chars = doPadArray(str, length);
			return new String(chars);
		}

		char[] doPadArray(CharSequence str, int length) {
			// Calculate start/end position of str in chars[length]
			int strLen = str.length();
			int start;
			int end;
			if (padMode == PadMode.RIGHT) {
				// input string is at head
				start = 0;
				end = strLen;
			} else if (padMode == PadMode.LEFT) {
				// input string is at tail
				start = length - strLen;
				end = length;
			} else if (padMode == PadMode.CENTER) {
				start = length - strLen;
				if (start % 2 == 1) {
					if (centerRight) {
						start++;
					}
				}
				start = start / 2;
				end = start + strLen;
			} else {
				throw new AssertionError();
			}

			char[] chars = new char[length];
			CharSequenceTools.getChars(str, 0, str.length(), chars, start);

			if (fillStr == null) {
				// pad left with char
				for (int i = 0; i < start; i++) {
					chars[i] = fillChar;
				}
				// pad right with char
				for (int i = end; i < length; i++) {
					chars[i] = fillChar;
				}
			} else {
				// pad left with string
				int fillLen = fillStr.length();
				for (int i = 0; i < start; i++) {
					char c = fillStr.charAt(i % fillLen);
					chars[i] = c;
				}
				// pad right with string
				for (int i = end; i < length; i++) {
					int j = (alignFillStr) ? (i - end) : i;
					char c = fillStr.charAt(j % fillLen);
					chars[i] = c;
				}
			}
			return chars;
		}

		@Override
		public IList<String> padAll(List<String> strs) {
			int len = (length == -1) ? CollectionTools.maxInt(strs, String::length) : length;
			return ICollectionTools.map(strs, s -> doPad(s, len), () -> new GapList<>(len));
		}
	}

	//

	/**
	 * Class {@link StringPadderInline} allows to dynamically change configuration (without the need to use a builder).
	 */
	public static class StringPadderInline extends StringPadderImpl {

		// TODO needed? add public setters to StringPadderImpl?

		public StringPadderInline() {
			returnMode = ReturnMode.RETURN_UNCHANGED;
			length = -1;
			padMode = PadMode.RIGHT;
			fillChar = ' ';
			fillStr = null;
			alignFillStr = false;
			centerRight = false;
		}

		public static StringPadderInline create() {
			return new StringPadderInline();
		}

		public StringPadderInline setLength(int length) {
			this.length = length;
			return this;
		}

		public StringPadderInline setPadMode(PadMode padMode) {
			this.padMode = padMode;
			return this;
		}

		public StringPadderInline setPadChar(char fillChar) {
			this.fillChar = fillChar;
			return this;
		}

		public StringPadderInline setPadString(String fillStr) {
			this.fillStr = fillStr;
			return this;
		}

		public StringPadderInline setCenterRight(boolean centerRight) {
			this.centerRight = centerRight;
			return this;
		}

		@Override
		public CharSequence pad(CharSequence text) {
			CheckTools.check(length >= 0, "length must be >= 0");

			return super.pad(text);
		}
	}

}