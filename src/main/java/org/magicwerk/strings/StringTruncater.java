package org.magicwerk.strings;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilderBase;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link StringTruncater} truncates strings.
 */
public interface StringTruncater extends IStringTransformer {

	/**
	 * This method calls {@link #truncate}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return truncate(input);
	}

	/**
	 * This method calls {@link #truncateInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		truncateInline(input);
	}

	//

	default String truncate(String str) {
		return (String) truncate((CharSequence) str);
	}

	public CharSequence truncate(CharSequence text);

	void truncateInline(IString text);

	//

	/** Build {@link StringTruncater} with specified builder function */
	public static StringTruncater build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringTruncater}.
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringTruncater}.
	 */
	public static class Builder extends BuilderImpl<Builder> implements IStringTransformerBuilder {

		@Override
		public StringTruncater build() {
			return doBuild();
		}
	}

	/**
	 * Class {@link Config} allows to configure instances of {@link StringTruncater} 
	 * without the possibility to build them explicitly (this is done implicitly as part of an coordinating action).
	 * This is needed as the same config information is used for both {@link StringTruncater} and {@link StringAligner}.
	 */
	public static class Config extends BuilderImpl<Config> {
	}

	/**
	 * Class {@link BuilderImpl} is the private implementation of {@link Builder} / {@link Config}.
	 */
	static class BuilderImpl<T extends BuilderImpl<T>> implements IStringTransformerBuilderBase {
		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;
		Integer length;
		Integer keepLeft;
		Integer keepRight;
		String truncateMarker = "";
		boolean centerRight;

		@SuppressWarnings("unchecked")
		T castThis() {
			return (T) this;
		}

		/** Setter for {@link #length} */
		public T setLength(int length) {
			this.length = length;
			return castThis();
		}

		/** Setter for {@link #truncateMarker} */
		public T setTruncateMarker(String truncateMarker) {
			this.truncateMarker = truncateMarker;
			return castThis();
		}

		/** Setter for {@link #centerRight} */
		public T setCenterRight(boolean centerRight) {
			this.centerRight = centerRight;
			return castThis();
		}

		/** Set {@link TruncateMode} (default is TRUNCATE_RIGHT) */
		public T setTruncate(TruncateMode mode) {
			if (mode == TruncateMode.TRUNCATE_RIGHT) {
				keepLeft = -1;
				keepRight = 0;
			} else if (mode == TruncateMode.TRUNCATE_LEFT) {
				keepLeft = 0;
				keepRight = -1;
			} else if (mode == TruncateMode.TRUNCATE_CENTER) {
				keepLeft = -1;
				keepRight = -1;
			} else if (mode == TruncateMode.TRUNCATE_LEFT_RIGHT) {
				keepLeft = 0;
				keepRight = 0;
			} else {
				throw new AssertionError();
			}
			return castThis();
		}

		@Override
		public T setReturnMode(ReturnMode mode) {
			this.returnMode = mode;
			return castThis();
		}

		/** Setter for {@link #keepLeft} */
		public T setKeepLeft(int keepLeft) {
			this.keepLeft = keepLeft;
			return castThis();
		}

		/** Setter for {@link #keepRight} */
		public T setKeepRight(int keepRight) {
			this.keepRight = keepRight;
			return castThis();
		}

		// FIXME
		boolean useLambda;

		public T useLambda() {
			this.useLambda = true;
			return castThis();
		}

		/**
		 * Build an instance of {@link StringTruncater} with the specified configuration.
		 */
		StringTruncaterImplBase doBuild() {
			// Method is made public in extending class Builder

			check();

			int stKeepLeft;
			int stKeepRight;
			if (keepLeft == null && keepRight == null) {
				// Use TRUNCATE_RIGHT as default
				stKeepLeft = -1;
				stKeepRight = 0;
			} else {
				stKeepLeft = (keepLeft != null) ? keepLeft : -1;
				stKeepRight = (keepRight != null) ? keepRight : -1;

				if (stKeepLeft == -1 && stKeepRight == -1) {
					// Truncate center, keep left and right as many characters as possible
				} else if (stKeepLeft == -1) {
					// Truncate right, keep left as many characters as possible
					assert stKeepRight >= 0;
					CheckTools.check(stKeepRight <= length);
				} else if (stKeepRight == -1) {
					// Truncate left, keep right as many characters as possible
					assert stKeepLeft >= 0;
					CheckTools.check(stKeepLeft <= length);
				} else if (stKeepLeft == 0 && stKeepRight == 0) {
					// Truncate left and right, keep in center as many characters as possible
				} else {
					assert stKeepLeft > 0 && stKeepRight > 0;
					CheckTools.check(stKeepLeft + StringTools.length(truncateMarker) + stKeepRight == length, "keepLeft and keepRight do not add to length");
				}
			}

			StringTruncaterWithoutMarkerImpl st;
			if (StringTools.length(truncateMarker) > 0) {
				StringTruncaterWithMarkerImpl st2 = new StringTruncaterWithMarkerImpl();
				st2.truncateMarker = truncateMarker;
				st2.keepLeft = stKeepLeft;
				st2.keepRight = stKeepRight;
				st2.centerRight = centerRight;
				st = st2;
			} else {
				st = getStringTruncaterWithoutMarkerImpl(length, stKeepLeft, stKeepRight, centerRight);
			}
			st.returnMode = returnMode;
			st.length = length;
			return st;
		}

		void check() {
			CheckTools.check(length != null && length >= 0, "length must be set and >= 0");
		}

		StringTruncaterWithoutMarkerImpl getStringTruncaterWithoutMarkerImpl(int length, int keepLeft, int keepRight, boolean centerRight) {
			if (keepLeft == -1 && keepRight == -1) {
				// Truncate center, keep left and right as many characters as possible
				int copyLen = length;
				int copyLeft = copyLen / 2;
				if (centerRight)
					if (copyLen % 2 == 1) {
						copyLeft++;
					}
				int copyRight = copyLen - copyLeft;
				return new StringTruncaterWithoutMarkerLeftRightImpl(copyLeft, copyRight);

			} else if (keepLeft == -1) {
				// Truncate right, keep left as many characters as possible
				assert keepRight >= 0;
				if (keepRight > 0) {
					return new StringTruncaterWithoutMarkerLeftRightImpl(length - keepRight, keepRight);
				} else {
					return new StringTruncaterWithoutMarkerLeftImpl(length);
				}

			} else if (keepRight == -1) {
				// Truncate left, keep right as many characters as possible
				assert keepLeft >= 0;
				if (keepLeft > 0) {
					return new StringTruncaterWithoutMarkerLeftRightImpl(keepLeft, length - keepLeft);
				} else {
					return new StringTruncaterWithoutMarkerRightImpl(length);
				}

			} else if (keepLeft == 0 && keepRight == 0) {
				// Truncate left and right, keep in center as many characters as possible
				return new StringTruncaterWithoutMarkerCenterImpl(length, centerRight);

			} else {
				assert keepLeft > 0 && keepRight > 0;
				return new StringTruncaterWithoutMarkerLeftRightImpl(keepLeft, keepRight);
			}
		}

	}

	//

	public enum TruncateMode {
		/** Truncate string right, i.e. start remains unchanged */
		TRUNCATE_RIGHT,
		/** Truncate string left, i.e. end remains unchanged */
		TRUNCATE_LEFT,
		/** Truncate string left and right, i.e. middle remains unchanged */
		TRUNCATE_LEFT_RIGHT,
		/** Truncate string at center, i.e. start and end remains unchanged */
		TRUNCATE_CENTER
	}

	public abstract static class StringTruncaterImplBase extends StringTransformerImpl implements StringTruncater {

		public static final char CHAR_THREE_DOTS = '\u2026';
		public static final String STRING_THREE_DOTS = String.valueOf(CHAR_THREE_DOTS);

		static final TruncateApplier truncateApplier = new TruncateApplier();
		static final TruncateInlineApplier truncateInlineApplier = new TruncateInlineApplier();

		int length;

		//

		@Override
		public CharSequence truncate(CharSequence str) {
			return transform(str);
		}

		@Override
		public void truncateInline(IString str) {
			transformInline(str);
		}

		@Override
		public CharSequence doTransform(CharSequence str) {
			if (str.length() > length) {
				return doTruncate(truncateApplier, str);
			} else {
				return null;
			}
		}

		@Override
		public IString doTransformInline(IString str) {
			if (str.length() > length) {
				return (IString) doTruncate(truncateInlineApplier, str);
			} else {
				return null;
			}
		}

		/** Implement truncate operation on input string with the methods offered by {@link ITruncateApplier} */
		abstract <T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str);
	}

	public static class StringTruncaterWithoutMarkerLeftRightImpl extends StringTruncaterWithoutMarkerImpl {
		int left;
		int right;

		StringTruncaterWithoutMarkerLeftRightImpl(int left, int right) {
			this.left = left;
			this.right = right;
		}

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			return applier.getLeftRight(str, left, right);
		}
	}

	public static class StringTruncaterWithoutMarkerLeftImpl extends StringTruncaterWithoutMarkerImpl {
		int left;

		StringTruncaterWithoutMarkerLeftImpl(int left) {
			this.left = left;
		}

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			return applier.getLeft(str, left);
		}
	}

	public static class StringTruncaterWithoutMarkerRightImpl extends StringTruncaterWithoutMarkerImpl {
		int right;

		StringTruncaterWithoutMarkerRightImpl(int right) {
			this.right = right;
		}

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			return applier.getRight(str, right);
		}
	}

	public static class StringTruncaterWithoutMarkerCenterImpl extends StringTruncaterWithoutMarkerImpl {
		int center;
		boolean centerRight;

		StringTruncaterWithoutMarkerCenterImpl(int center, boolean centerRight) {
			this.center = center;
			this.centerRight = centerRight;
		}

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			return applier.getCenter(str, center, centerRight);
		}
	}

	abstract static class StringTruncaterWithoutMarkerImpl extends StringTruncaterImplBase {

		//		@Override
		//		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
		//			CharSequence result;
		//			if (keepLeft == -1 && keepRight == -1) {
		//				// Truncate center, keep left and right as many characters as possible
		//				int copyLen = length;
		//				int copyLeft = copyLen / 2;
		//				if (centerRight)
		//					if (copyLen % 2 == 1) {
		//						copyLeft++;
		//					}
		//				int copyRight = copyLen - copyLeft;
		//				result = applier.getLeftRight(str, copyLeft, copyRight);
		//
		//			} else if (keepLeft == -1) {
		//				// Truncate right, keep left as many characters as possible
		//				assert keepRight >= 0;
		//				if (keepRight > 0) {
		//					result = applier.getLeftRight(str, length - keepRight, keepRight);
		//				} else {
		//					result = applier.getLeft(str, length);
		//				}
		//
		//			} else if (keepRight == -1) {
		//				// Truncate left, keep right as many characters as possible
		//				assert keepLeft >= 0;
		//				if (keepLeft > 0) {
		//					result = applier.getLeftRight(str, keepLeft, length - keepLeft);
		//				} else {
		//					result = applier.getRight(str, length);
		//				}
		//
		//			} else if (keepLeft == 0 && keepRight == 0) {
		//				// Truncate left and right, keep in center as many characters as possible
		//				result = applier.getCenter(str, length, centerRight);
		//
		//			} else {
		//				assert keepLeft > 0 && keepRight > 0;
		//				result = applier.getLeftRight(str, keepLeft, keepRight);
		//			}
		//
		//			assert result.length() == length;
		//			return result;
		//		}
	}

	public static class StringTruncaterWithoutMarkerLambdaImpl_OLD extends StringTruncaterImplBase {

		final int keepLeft;
		final int keepRight;
		final boolean centerRight;
		final Function<CharSequence, CharSequence> truncateApplierFnc;
		final Function<IString, CharSequence> truncateInlineApplierFnc;

		StringTruncaterWithoutMarkerLambdaImpl_OLD(final int keepLeft, final int keepRight, final boolean centerRight) {
			this.keepLeft = keepLeft;
			this.keepRight = keepRight;
			this.centerRight = centerRight;

			truncateApplierFnc = createTruncate(truncateApplier);
			truncateInlineApplierFnc = createTruncate(truncateInlineApplier);
		}

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			return truncateApplierFnc.apply(str);
		}

		<T extends CharSequence> Function<T, CharSequence> createTruncate(ITruncateApplier<T> applier) {
			Function<T, CharSequence> result;
			if (keepLeft == -1 && keepRight == -1) {
				// Truncate center, keep left and right as many characters as possible
				int left = length / 2;
				if (centerRight)
					if (length % 2 == 1) {
						left++;
					}
				int copyLeft = left;
				int copyRight = length - copyLeft;
				result = str -> applier.getLeftRight(str, copyLeft, copyRight);

			} else if (keepLeft == -1) {
				// Truncate right, keep left as many characters as possible
				assert keepRight >= 0;
				int copyLen = length - keepRight;
				if (copyLen > 0) {
					result = str -> applier.getLeftRight(str, copyLen, keepRight);
				} else {
					result = str -> applier.getLeftRight(str, length, 0);
				}

			} else if (keepRight == -1) {
				// Truncate left, keep right as many characters as possible
				assert keepLeft >= 0;
				int copyLen = length - keepLeft;
				if (copyLen > 0) {
					result = str -> applier.getLeftRight(str, keepLeft, copyLen);
				} else {
					result = str -> applier.getLeftRight(str, 0, length);
				}

			} else if (keepLeft == 0 && keepRight == 0) {
				// Truncate left and right, keep in center as many characters as possible
				result = str -> applier.getCenter(str, length, centerRight);

			} else {
				assert keepLeft > 0 && keepRight > 0;
				result = str -> applier.getLeftRight(str, keepLeft, keepRight);
			}

			return result;
		}
	}

	public static class StringTruncaterWithMarkerImpl extends StringTruncaterWithoutMarkerImpl {

		int keepLeft;
		int keepRight;
		String truncateMarker;
		boolean centerRight;

		@Override
		<T extends CharSequence> CharSequence doTruncate(ITruncateApplier<T> applier, T str) {
			int markerLen = truncateMarker.length();

			CharSequence result;
			if (keepLeft == -1 && keepRight == -1) {
				// Truncate center, keep left and right as many characters as possible
				int copyLen = length - markerLen;
				if (copyLen > 0) {
					int copyLeft = copyLen / 2;
					if (centerRight)
						if (copyLen % 2 == 1) {
							copyLeft++;
						}
					int copyRight = copyLen - copyLeft;
					result = applier.getLeftMarkerRight(str, copyLeft, copyRight, truncateMarker);
				} else {
					int copyStart = (markerLen - length) / 2;
					if (centerRight && (markerLen - length) % 2 == 1) {
						copyStart++;
					}
					result = applier.getMarkerMid(str, truncateMarker, copyStart, length);
				}

			} else if (keepLeft == -1) {
				// Truncate right, keep left as many characters as possible
				assert keepRight >= 0;
				int copyLen = length - markerLen - keepRight;
				if (copyLen > 0) {
					result = applier.getLeftMarkerRight(str, copyLen, keepRight, truncateMarker);
				} else {
					result = applier.getMarkerMid(str, truncateMarker, 0, length);
				}

			} else if (keepRight == -1) {
				// Truncate left, keep right as many characters as possible
				assert keepLeft >= 0;
				int copyLen = length - markerLen - keepLeft;
				if (copyLen > 0) {
					result = applier.getLeftMarkerRight(str, keepLeft, copyLen, truncateMarker);
				} else {
					result = applier.getMarkerMid(str, truncateMarker, truncateMarker.length() - length, length);
				}

			} else if (keepLeft == 0 && keepRight == 0) {
				// Truncate left and right, keep in center as many characters as possible
				int copyLen = length - 2 * markerLen;
				if (copyLen > 0) {
					result = applier.getMarkerCenterMarker(str, truncateMarker, copyLen, centerRight);
				} else {
					int copyLeft = length / 2;
					if (centerRight)
						if (length % 2 == 1) {
							copyLeft++;
						}
					int copyRight = length - copyLeft;
					result = applier.getMarkerLeftMarkerRight(str, truncateMarker, copyLeft, copyRight);
				}

			} else {
				assert keepLeft > 0 && keepRight > 0;
				result = applier.getLeftMarkerRight(str, keepLeft, keepRight, truncateMarker);
			}

			assert result.length() == length;
			return result;
		}

	}

	//

	/**
	 * Interface {@link ITruncateApplier} offers the interface needed by {@link StringTruncater} to implement then methods
	 * for truncating string both for immutable and mutable strings.
	 */
	interface ITruncateApplier<T extends CharSequence> {

		CharSequence getLeftMarkerRight(T input, int inputLeft, int inputRight, String marker);

		CharSequence getLeft(T input, int inputLeft);

		CharSequence getRight(T input, int inputRight);

		CharSequence getLeftRight(T input, int inputLeft, int inputRight);

		CharSequence getMarkerMid(T input, String marker, int markerStart, int markerLen);

		CharSequence getMarkerLeftMarkerRight(T input, String marker, int markerLeft, int markerRight);

		CharSequence getMarkerCenterMarker(T input, String marker, int inputCenter, boolean centerRight);

		CharSequence getCenter(T input, int inputCenter, boolean centerRight);
	}

	/**
	 * Class {@link TruncateApplier} implements {@link ITruncateApplier} for immutable strings.
	 */
	static class TruncateApplier implements ITruncateApplier<CharSequence> {

		@Override
		public CharSequence getLeft(CharSequence input, int inputLeft) {
			return left(input, inputLeft);
		}

		@Override
		public CharSequence getRight(CharSequence input, int inputRight) {
			return right(input, inputRight);
		}

		@Override
		public CharSequence getLeftRight(CharSequence input, int inputLeft, int inputRight) {
			if (inputLeft == 0 && inputRight == 0) {
				return "";
			} else if (inputLeft == 0) {
				return right(input, inputRight);
			} else if (inputRight == 0) {
				return left(input, inputLeft);
			} else {
				return StringTools.concat(left(input, inputLeft), right(input, inputRight));
			}
		}

		@Override
		public CharSequence getLeftMarkerRight(CharSequence input, int inputLeft, int inputRight, String marker) {
			StringBuilder buf = new StringBuilder();
			if (inputLeft > 0) {
				buf.append(input, 0, inputLeft);
			}
			buf.append(marker);
			if (inputRight > 0) {
				buf.append(input, input.length() - inputRight, input.length());
			}
			return buf.toString();
		}

		@Override
		public CharSequence getMarkerMid(CharSequence input, String marker, int markerStart, int markerLen) {
			return mid(marker, markerStart, markerLen);
		}

		@Override
		public CharSequence getMarkerLeftMarkerRight(CharSequence input, String marker, int markerLeft, int markerRight) {
			return StringTools.concat(left(marker, markerLeft), right(marker, markerRight));
		}

		@Override
		public CharSequence getMarkerCenterMarker(CharSequence input, String marker, int inputCenter, boolean centerRight) {
			int inputStart = (input.length() - inputCenter) / 2;
			if (centerRight) {
				if ((input.length() - inputCenter) % 2 == 1) {
					inputStart++;
				}
			}
			return marker + mid(input, inputStart, inputCenter) + marker;
		}

		@Override
		public CharSequence getCenter(CharSequence input, int inputCenter, boolean centerRight) {
			int inputStart = (input.length() - inputCenter) / 2;
			if (centerRight) {
				if ((input.length() - inputCenter) % 2 == 1) {
					inputStart++;
				}
			}
			return mid(input, inputStart, inputCenter);
		}

		static CharSequence left(CharSequence str, int len) {
			return str.subSequence(0, len);
		}

		static CharSequence right(CharSequence str, int len) {
			int strLen = str.length();
			return str.subSequence(strLen - len, strLen);
		}

		static CharSequence mid(CharSequence str, int pos, int len) {
			return str.subSequence(pos, pos + len);
		}
	}

	/**
	 * Class {@link TruncateInlineApplier} implements {@link ITruncateApplier} for mutable strings.
	 */
	static class TruncateInlineApplier implements ITruncateApplier<IString> {

		@Override
		public CharSequence getLeft(IString input, int inputLeft) {
			input.resize(inputLeft, (char) 0);
			return input;
		}

		@Override
		public CharSequence getRight(IString input, int inputRight) {
			input.remove(0, input.length() - inputRight);
			return input;
		}

		@Override
		public CharSequence getLeftRight(IString input, int inputLeft, int inputRight) {
			input.move(input.size() - inputRight, inputLeft, inputRight);
			input.resize(inputLeft + inputRight, (char) 0);
			return input;
		}

		@Override
		public CharSequence getLeftMarkerRight(IString input, int inputLeft, int inputRight, String marker) {
			input.putString(inputLeft, marker);
			input.move(input.size() - inputRight, inputLeft + marker.length(), inputRight);
			input.resize(inputLeft + marker.length() + inputRight, (char) 0);
			return input;
		}

		@Override
		public CharSequence getMarkerMid(IString input, String marker, int markerStart, int markerLen) {
			input.clear();
			input.addString(marker, markerStart, markerLen);
			return input;
		}

		@Override
		public CharSequence getMarkerLeftMarkerRight(IString input, String marker, int markerLeft, int markerRight) {
			input.clear();
			input.addString(marker, 0, markerLeft);
			input.addString(marker, marker.length() - markerRight, markerRight);
			return input;
		}

		@Override
		public CharSequence getMarkerCenterMarker(IString input, String marker, int inputCenter, boolean centerRight) {
			int inputLen = input.length();
			int inputStart = (inputLen - inputCenter) / 2;
			if (centerRight) {
				if ((inputLen - inputCenter) % 2 == 1) {
					inputStart++;
				}
			}
			int markerLen = marker.length();
			input.putString(0, marker);
			input.move(inputStart, markerLen, inputCenter);
			input.putString(markerLen + inputCenter, marker);
			input.resize(markerLen + inputCenter + markerLen, (char) 0);
			return input;
		}

		@Override
		public CharSequence getCenter(IString input, int inputCenter, boolean centerRight) {
			int inputLen = input.length();
			int inputStart = (inputLen - inputCenter) / 2;
			if (centerRight) {
				if ((inputLen - inputCenter) % 2 == 1) {
					inputStart++;
				}
			}
			input.retain(inputStart, inputCenter);
			return input;
		}
	}

}