package org.magicwerk.strings;

import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link StringAligner} aligns string by either padding or truncating them.
 * It integrates {@link StringPadder} and {@link StringTruncater} into a common API.
 * Use {@link Builder#getPadderConfig} and {@link Builder#getTruncaterConfig} to access the configuration of the children.
 */
public interface StringAligner extends IStringTransformer {

	// IStringTransformer

	/**
	 * This method calls {@link #align}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String str) {
		return align(str);
	}

	/**
	 * This method calls {@link #alignInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString str) {
		alignInline(str);
	}

	//

	default String align(String str) {
		return (String) align((CharSequence) str);
	}

	/** 
	 * Align input string to configured length, either by truncating or padding.
	 */
	CharSequence align(CharSequence str);

	/** 
	 * Align input string to configured length, either by truncating or padding.
	 */
	void alignInline(IString str);

	//

	/** Build {@link StringAligner} with specified builder function */
	public static StringAligner build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringAligner}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to create instances of {@link StringAligner}.
	 */
	public static class Builder implements IStringTransformerBuilder {
		Integer length;
		ReturnMode returnMode;
		StringPadder.Config padderConfig = new StringPadder.Config().setReturnMode(null);
		StringTruncater.Config truncateConfig = new StringTruncater.Config().setReturnMode(null);

		@Override
		public Builder setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return null;
		}

		/** Setter for {@link #length} */
		public Builder setLength(int length) {
			this.length = length;
			return this;
		}

		/** Getter for {@link #padderConfig} */
		public StringPadder.Config getPadderConfig() {
			return padderConfig;
		}

		/** Getter for {@link #truncateConfig} */
		public StringTruncater.Config getTruncaterConfig() {
			return truncateConfig;
		}

		/** Build an instance of {@link StringAligner} with the specified configuration */
		@Override
		public StringAligner build() {
			int len;
			if (length == null) {
				Integer len1 = padderConfig.length;
				Integer len2 = truncateConfig.length;
				if (len1 == null && len2 == null) {
					CheckTools.error("length must be specified");
				} else if (len1 != null && len2 != null && len1 != len2) {
					CheckTools.error("different length specified for padder and truncater");
				}
				len = len1;
			} else {
				len = length;
				Integer len1 = padderConfig.length;
				Integer len2 = truncateConfig.length;
				if (len1 != null && len1 != len) {
					CheckTools.error("different length specified for padder");
				}
				if (len2 != null && len2 != len) {
					CheckTools.error("different length specified for truncater");
				}
			}
			padderConfig.setLength(len);
			truncateConfig.setLength(len);

			StringAlignerImpl st = new StringAlignerImpl();
			st.returnMode = determineReturnMode();
			st.length = length;
			st.padder = padderConfig.doBuild();
			st.truncater = truncateConfig.doBuild();
			return st;
		}

		ReturnMode determineReturnMode() {
			ReturnMode mode = null;
			if (returnMode != null) {
				CheckTools.check(mode == null || mode == returnMode);
				mode = returnMode;
			}
			if (padderConfig.returnMode != null) {
				CheckTools.check(mode == null || mode == padderConfig.returnMode);
				mode = padderConfig.returnMode;
			}
			if (truncateConfig.returnMode != null) {
				CheckTools.check(mode == null || mode == truncateConfig.returnMode);
				mode = truncateConfig.returnMode;
			}
			return (mode != null) ? mode : ReturnMode.RETURN_UNCHANGED;
		}
	}

	public static class StringAlignerImpl extends StringTransformerImpl implements StringAligner {

		StringTransformerImpl padder;
		StringTransformerImpl truncater;
		int length;

		//

		@Override
		public CharSequence align(CharSequence str) {
			return transform(str);
		}

		@Override
		public void alignInline(IString str) {
			transformInline(str);
		}

		@Override
		public CharSequence doTransform(CharSequence str) {
			int strLen = str.length();
			if (strLen > length) {
				return truncater.doTransform(str);
			} else if (strLen < length) {
				return padder.doTransform(str);
			} else {
				return null;
			}
		}

		@Override
		public IString doTransformInline(IString str) {
			int strLen = str.length();
			if (strLen > length) {
				return truncater.doTransformInline(str);
			} else if (strLen < length) {
				return padder.doTransformInline(str);
			} else {
				return null;
			}
		}
	}
}