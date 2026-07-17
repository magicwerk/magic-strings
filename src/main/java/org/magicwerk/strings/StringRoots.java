package org.magicwerk.strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;

import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharCaseMode;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.ICharMode;
import org.magicwerk.strings.helper.CollectionTools;

/**
 * Class {@link StringRoots} determines common length and start/end of multiple strings. 
 */
public interface StringRoots {

	// getCommonRoot: String

	default String getCommonRoot(String str0, String str1) {
		return (String) getCommonRoot((CharSequence) str0, (CharSequence) str1);
	}

	default String getCommonRoot(String... strs) {
		return (String) getCommonRoot((CharSequence[]) strs);
	}

	default String getCommonRootString(Collection<String> strs) {
		return (String) getCommonRootCharSequence(strs);
	}

	// getCommonRoot: CharSequence

	CharSequence getCommonRoot(CharSequence str0, CharSequence str1);

	CharSequence getCommonRoot(CharSequence... strs);

	CharSequence getCommonRootCharSequence(Collection<? extends CharSequence> strs);

	// getCommonLength

	/** Return common length of the passed strings at the start or end (if reserve has been configured) */
	int getCommonLength(CharSequence str0, CharSequence str1);

	/** Return common length of the passed strings at the start or end (if reserve has been configured) */
	int getCommonLength(CharSequence... strs);

	/** Return common length of the passed strings at the start or end (if reserve has been configured) */
	<T extends CharSequence> int getCommonLength(Collection<T> strs);

	// getDiffIndex

	/** Returns first index from the start where the passed strings differ (or from the end if reserve has been configured) */
	int getDiffIndex(CharSequence str0, CharSequence str1);

	int getDiffIndex(CharSequence... strs);

	<T extends CharSequence> int getDiffIndex(Collection<T> strs);

	//

	/** Build {@link StringRoots} with specified builder function */
	public static StringRoots build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringRoots}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringRoots}.
	 */
	public static class Builder {
		ICharMode ignoreCase;
		boolean reverse;

		/** Setter for {@link #ignoreCase} */
		public Builder setIgnoreCase(ICharMode ignoreCase) {
			this.ignoreCase = ignoreCase;
			return this;
		}

		public Builder setIgnoreCase(boolean ignoreCase) {
			return setIgnoreCase(CharMode.getCharMode(ignoreCase));
		}

		/** Setter for {@link #ignoreCase} */
		public Builder setReverse(boolean reverse) {
			this.reverse = reverse;
			return this;
		}

		/**
		 * Build an instance of {@link StringRoots} with the specified configuration.
		 */
		public StringRoots build() {

			StringRootsImpl sr = null;
			ICharMode ic = (ignoreCase != null) ? ignoreCase : CharMode.CI_CODEPOINT;
			if (ic.getCharCaseMode() != CharCaseMode.CASE_SENSITIVE) {
				if (!ic.supportCodePoints()) {
					sr = (reverse) ? new StringRootsReverseCharImpl() : new StringRootsForwardCharImpl();
				} else {
					sr = (reverse) ? new StringRootsReverseImpl() : new StringRootsForwardImpl();
				}
			}
			if (sr == null) {
				sr = new StringRootsImpl();
			}
			sr.ignoreCase = ic;
			sr.reverse = reverse;
			return sr;
		}
	}

	//

	public static class StringRootsForwardCharImpl extends StringRootsForwardImpl {

		// Performance:
		// Overriding getCommonLength() and repeating the calculation of minLen
		// is 10% faster than only to override doGetCommonLength()

		@Override
		public int getCommonLength(CharSequence str0, CharSequence str1) {
			return CharSequenceTools.commonLength(str0, str1);
		}
	}

	public static class StringRootsForwardImpl extends StringRootsImpl {

		@Override
		public int getCommonLength(CharSequence str0, CharSequence str1) {
			int maxLen = Math.min(str0.length(), str1.length());
			if (maxLen == 0) {
				return 0;
			}

			int len = 0;
			while (len < maxLen && str0.charAt(len) == str1.charAt(len)) {
				len++;
			}
			if (isValidSurrogatePairAt(str0, len - 1) || isValidSurrogatePairAt(str1, len - 1)) {
				len--;
			}
			return len;
		}

		@Override
		CharSequence doGetCommon(CharSequence str, int len) {
			return CharSequenceTools.subSequence(str, 0, len);
		}
	}

	public static class StringRootsReverseCharImpl extends StringRootsReverseImpl {
		@Override
		public int getCommonLength(CharSequence str0, CharSequence str1) {
			int maxLen = Math.min(str0.length(), str1.length());
			if (maxLen == 0) {
				return 0;
			}

			int len = 0;
			while (len < maxLen && str0.charAt(str0.length() - len - 1) == str1.charAt(str1.length() - len - 1)) {
				len++;
			}
			return len;
		}
	}

	public static class StringRootsReverseImpl extends StringRootsImpl {

		@Override
		public int getCommonLength(CharSequence str0, CharSequence str1) {
			int maxLen = Math.min(str0.length(), str1.length());
			if (maxLen == 0) {
				return 0;
			}

			int len = 0;
			while (len < maxLen && str0.charAt(str0.length() - len - 1) == str1.charAt(str1.length() - len - 1)) {
				len++;
			}
			if (isValidSurrogatePairAt(str0, str0.length() - len - 1)
					|| isValidSurrogatePairAt(str1, str1.length() - len - 1)) {
				len--;
			}
			return len;
		}

		@Override
		CharSequence doGetCommon(CharSequence str, int len) {
			return CharSequenceTools.subSequence(str, str.length() - len, str.length());
		}
	}

	public static class StringRootsImpl implements StringRoots {

		ICharMode ignoreCase;
		boolean reverse;

		@Override
		public int getDiffIndex(CharSequence str0, CharSequence str1) {
			int len0 = str0.length();
			int len1 = str1.length();
			int minLen = Math.min(len0, len1);
			int len = doGetCommonLength(str0, str1, minLen);
			if (len == len0 && len == len1) {
				return -1;
			}
			return len;
		}

		@Override
		public int getDiffIndex(CharSequence... strs) {
			return getCommonLength(Arrays.asList(strs));
		}

		@Override
		public <T extends CharSequence> int getDiffIndex(Collection<T> strs) {
			int minLen = getMinLengthDiff(strs);
			if (minLen == 0 || minLen == -1) {
				return minLen;
			}
			int len = doGetCommonLength(strs, minLen);
			if (minLen < 0) {
				int allLen = -minLen + 1;
				if (allLen == len) {
					return -1;
				}
			}
			return len;
		}

		// getCommonRoot: CharSequence

		@Override
		public CharSequence getCommonRoot(CharSequence str0, CharSequence str1) {
			int len = getCommonLength(str0, str1);
			if (len == 0) {
				return "";
			}
			return doGetCommon(str0, len);
		}

		@Override
		public CharSequence getCommonRoot(CharSequence... strs) {
			int len = getCommonLength(strs);
			if (len == 0) {
				return "";
			}
			CharSequence str = strs[0];
			return doGetCommon(str, len);
		}

		@Override
		public CharSequence getCommonRootCharSequence(Collection<? extends CharSequence> strs) {
			int len = getCommonLength(strs);
			if (len == 0) {
				return "";
			}
			CharSequence str = CollectionTools.getFirst(strs);
			return doGetCommon(str, len);
		}

		CharSequence doGetCommon(CharSequence str, int len) {
			if (reverse) {
				return CharSequenceTools.subSequence(str, str.length() - len, str.length());
			} else {
				return CharSequenceTools.subSequence(str, 0, len);
			}
		}

		// getCommonLength

		@Override
		public int getCommonLength(CharSequence str0, CharSequence str1) {
			int minLen = Math.min(str0.length(), str1.length());
			if (minLen == 0) {
				return 0;
			}
			return doGetCommonLength(str0, str1, minLen);
		}

		@Override
		public int getCommonLength(CharSequence... strs) {
			return getCommonLength(Arrays.asList(strs));
		}

		@Override
		public <T extends CharSequence> int getCommonLength(Collection<T> strs) {
			int minLen = getMinLength(strs);
			if (minLen == 0) {
				return 0;
			}
			return doGetCommonLength(strs, minLen);
		}

		/** Determine common length of passed strings from the start (or from the end if {@link #reverse} */
		int doGetCommonLength(CharSequence str0, CharSequence str1, int minLen) {
			for (int i = 0; i < minLen; i++) {
				int i0 = (reverse) ? (str0.length() - i - 1) : i;
				int i1 = (reverse) ? (str1.length() - i - 1) : i;
				char c0 = str0.charAt(i0);
				char c1 = str1.charAt(i1);
				if (c0 != c1) {
					int d = 0;
					if (reverse) {
						if (!CodePointTools.isValidEndIndexForCodePoint(str0, i0) || !CodePointTools.isValidEndIndexForCodePoint(str1, i1)) {
							d = 1;
						}
					} else {
						if (!CodePointTools.isValidStartIndexForCodePoint(str0, i0) || !CodePointTools.isValidStartIndexForCodePoint(str1, i1)) {
							d = 1;
						}
					}
					i0 = CodePointTools.getValidStartIndexForCodePoint(str0, i0, false);
					i1 = CodePointTools.getValidStartIndexForCodePoint(str1, i1, false);
					int cp0 = CodePointTools.codePointAt(str0, i0);
					int cp1 = CodePointTools.codePointAt(str1, i1);
					if (!ignoreCase.getCodePointEqual().isEqualCodePoint(cp0, cp1)) {
						return i - d;
					}
				}
			}
			return minLen;
		}

		public <T extends CharSequence> int doGetCommonLength(Collection<T> strs, int minLen) {
			for (int i = 0; i < minLen; i++) {
				boolean first = true;
				CharSequence str0 = null;
				char c0 = 0;
				int i0 = 0;
				for (CharSequence str1 : strs) {
					int i1 = (reverse) ? (str1.length() - i - 1) : i;
					char c1 = str1.charAt(i1);
					if (first) {
						str0 = str1;
						c0 = c1;
						i0 = i1;
						first = false;

					} else if (c0 != c1) {
						int d = 0;
						if (reverse) {
							if (!CodePointTools.isValidEndIndexForCodePoint(str0, i0) || !CodePointTools.isValidEndIndexForCodePoint(str1, i1)) {
								d = 1;
							}
						} else {
							if (!CodePointTools.isValidStartIndexForCodePoint(str0, i0) || !CodePointTools.isValidStartIndexForCodePoint(str1, i1)) {
								d = 1;
							}
						}
						i0 = CodePointTools.getValidStartIndexForCodePoint(str0, i0, false);
						i1 = CodePointTools.getValidStartIndexForCodePoint(str1, i1, false);
						int cp0 = CodePointTools.codePointAt(str0, i0);
						int cp1 = CodePointTools.codePointAt(str1, i1);
						if (!ignoreCase.getCodePointEqual().isEqualCodePoint(cp0, cp1)) {
							return i - d;
						}
					}
				}
			}
			return minLen;
		}

		/** Return minimum length of all strings in the collection */
		<T extends CharSequence> int getMinLength(Collection<T> strs) {
			if (strs.isEmpty()) {
				return 0;
			}

			int minLen = Integer.MAX_VALUE;
			for (CharSequence str : strs) {
				int len = str.length();
				minLen = Math.min(minLen, len);
			}
			return minLen;
		}

		/** Return minimum length of all strings in the collection */
		<T extends CharSequence> int getMinLengthDiff(Collection<T> strs) {
			if (strs.isEmpty()) {
				return 0;
			}

			boolean sameLen = true;
			int minLen = -1;
			for (CharSequence str : strs) {
				int len = str.length();
				if (minLen == -1) {
					minLen = len;
				} else if (len < minLen) {
					minLen = len;
					sameLen = false;
				} else if (len > minLen) {
					sameLen = false;
				}
			}
			return (sameLen) ? (-minLen - 1) : minLen;
		}

		boolean isValidSurrogatePairAt(CharSequence str, int index) {
			return index >= 0 && CodePointTools.isValidSurrogatePairAt(str, index);
		}
	}
}