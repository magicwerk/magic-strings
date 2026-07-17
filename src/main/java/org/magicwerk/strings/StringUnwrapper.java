package org.magicwerk.strings;

import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.BuilderWrapperBase;
import org.magicwerk.strings.StringWrapper.WrapMode;
import org.magicwerk.strings.StringWrapper.WrapperContainsCharImpl;
import org.magicwerk.strings.StringWrapper.WrapperContainsImpl;
import org.magicwerk.strings.StringWrapper.WrapperContainsStringImpl;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.matcher.IStringMatcher;

/**
 * Class {@link StringUnwrapper} removes wrapping markers from strings.
 */
public interface StringUnwrapper extends IStringTransformer {

	// IStringTransformer

	/**
	 * This method calls {@link #unwrap}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return unwrap(input);
	}

	/**
	 * This method calls {@link #unwrapInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		unwrapInline(input);
	}

	// String

	/** Determines whether string contains wrapping markers as configured */
	default boolean isWrapped(String str) {
		return isWrapped((CharSequence) str);
	}

	/** Remove wrapping markers as configured from string */
	default String unwrap(String str) {
		return (String) unwrap((CharSequence) str);
	}

	// CharSequence

	/** Determines whether string contains wrapping markers as configured */
	public boolean isWrapped(CharSequence str);

	/** Remove wrapping markers as configured from string */
	public CharSequence unwrap(CharSequence str);

	//

	/** Remove wrapping markers as configured to mutable string */
	public void unwrapInline(IString str);

	//

	/** Build {@link StringUnwrapper} with specified builder function */
	public static StringUnwrapper build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringUnwrapper}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringUnwrapper}.
	 */
	public static class Builder extends BuilderWrapperBase<Builder> {

		// Char

		public Builder setUnwrapChar(char c) {
			this.containsHead.setChar(c);
			this.containsTail.setChar(c);
			return this;
		}

		public Builder setUnwrapHeadChar(char c) {
			this.containsHead.setChar(c);
			return this;
		}

		public Builder setUnwrapTailChar(char c) {
			this.containsTail.setChar(c);
			return this;
		}

		//

		public Builder setUnwrapCodePoint(int cp) {
			this.containsHead.setCodePoint(cp);
			this.containsTail.setCodePoint(cp);
			return this;
		}

		public Builder setUnwrapHeadCodePoint(int cp) {
			this.containsHead.setCodePoint(cp);
			return this;
		}

		public Builder setUnwrapTailCodePoint(int cp) {
			this.containsTail.setCodePoint(cp);
			return this;
		}

		//

		/** Setter for {@link #containsHead} and {@link #containsTail} */
		public Builder setUnwrapString(String str) {
			this.containsHead.setString(str);
			this.containsTail.setString(str);
			return this;
		}

		public Builder setUnwrapHeadString(String str) {
			this.containsHead.setString(str);
			return this;
		}

		public Builder setUnwrapTailString(String str) {
			this.containsTail.setString(str);
			return this;
		}

		//

		public Builder setUnwrapCharPredicate(CharPredicate cp) {
			containsHead.setCharPredicate(cp);
			containsTail.setCharPredicate(cp);
			return this;
		}

		public Builder setUnwrapHeadCharPredicate(CharPredicate cp) {
			containsHead.setCharPredicate(cp);
			return this;
		}

		public Builder setUnwrapTailCharPredicate(CharPredicate cp) {
			containsTail.setCharPredicate(cp);
			return this;
		}

		//

		public Builder setUnwrapAnyChar(String anyChar) {
			containsHead.setAnyChar(anyChar);
			containsTail.setAnyChar(anyChar);
			return this;
		}

		public Builder setUnwrapHeadAnyChar(String anyChar) {
			containsHead.setAnyChar(anyChar);
			return this;
		}

		public Builder setUnwrapTailAnyChar(String anyChar) {
			containsTail.setAnyChar(anyChar);
			return this;
		}

		//

		public Builder setUnwrapRegex(String regex) {
			containsHead.setRegex(regex);
			containsTail.setRegex(regex);
			return this;
		}

		public Builder setUnwrapHeadRegex(String regex) {
			containsHead.setRegex(regex);
			return this;
		}

		public Builder setUnwrapTailRegex(String regex) {
			containsTail.setRegex(regex);
			return this;
		}

		//

		public Builder setUnwrapMatcher(IStringMatcher matcher) {
			containsHead.setMatcher(matcher);
			containsTail.setMatcher(matcher);
			return this;
		}

		public Builder setUnwrapHeadMatcher(IStringMatcher matcher) {
			containsHead.setMatcher(matcher);
			return this;
		}

		public Builder setUnwrapTailMatcher(IStringMatcher matcher) {
			containsTail.setMatcher(matcher);
			return this;
		}

		/**
		 * Build an instance of {@link StringUnwrapper} with the specified configuration.
		 */
		@Override
		public StringUnwrapper build() {
			WrapperContainsBuilder contains = analyzeContains();

			WrapperContainsImpl ci;
			if (contains.useMatcher) {
				ci = createStringLocationContainsMatcherImpl(contains.headMatcher, contains.tailMatcher);
			} else if (contains.useString) {
				ci = new WrapperContainsStringImpl(contains.headString, contains.tailString);
			} else if (contains.useChar) {
				ci = new WrapperContainsCharImpl(contains.headChar, contains.tailChar);
			} else {
				throw new AssertionError();
			}

			StringUnwrapperImpl ri = createStringUnwrapperImpl();
			ri.containsImpl = ci;
			ri.returnMode = returnMode;
			return ri;
		}

		StringUnwrapperImpl createStringUnwrapperImpl() {
			if (wrapMode == WrapMode.HEAD) {
				return new StringUnwrapperHeadImpl();
			} else if (wrapMode == WrapMode.TAIL) {
				return new StringUnwrapperTailImpl();
			} else if (wrapMode == WrapMode.HEAD_OR_TAIL) {
				return new StringUnwrapperHeadOrTailImpl();
			} else if (wrapMode == WrapMode.HEAD_AND_TAIL) {
				return new StringUnwrapperHeadAndTailImpl();
			} else {
				throw new AssertionError();
			}
		}
	}

	//

	abstract static class StringUnwrapperImpl extends StringTransformerImpl implements StringUnwrapper {

		WrapperContainsImpl containsImpl;

		// StringUnwrapper

		@Override
		public boolean isWrapped(CharSequence str) {
			if (str == null) {
				return false;
			}
			return doIsWrapped(str);
		}

		abstract boolean doIsWrapped(CharSequence str);

		@Override
		public CharSequence unwrap(CharSequence str) {
			return transform(str);
		}

		@Override
		public void unwrapInline(IString str) {
			transformInline(str);
		}

		//

		boolean hasHead(CharSequence str) {
			return containsImpl.hasHead(str);
		}

		boolean hasTail(CharSequence str) {
			return containsImpl.hasTail(str);
		}

		boolean hasHeadString(String str) {
			return containsImpl.hasHeadString(str);
		}

		boolean hasTailString(String str) {
			return containsImpl.hasTailString(str);
		}

		public CharSequence removeHead(CharSequence str, int headLen) {
			return StringTools.removeLeft(str, headLen);
		}

		public CharSequence removeTail(CharSequence str, int tailLen) {
			return StringTools.removeRight(str, tailLen);
		}

		public CharSequence removeHeadTail(CharSequence str, int headLen, int tailLen) {
			return str.subSequence(headLen, str.length() - tailLen);
		}

		public IString removeHeadInline(IString str, int headLen) {
			str.remove(0, headLen);
			return str;
		}

		public IString removeTailInline(IString str, int tailLen) {
			str.remove(str.size() - tailLen, tailLen);
			return str;
		}

		public IString removeHeadTailInline(IString str, int headLen, int tailLen) {
			str.retain(headLen, str.length() - tailLen - headLen);
			return str;
		}

	}

	// Head

	static class StringUnwrapperHeadImpl extends StringUnwrapperImpl {

		String headStr;
		char headChar;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasHead(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				return removeHead(str, headLen);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				return removeHeadInline(str, headLen);
			} else {
				return null;
			}
		}
	}

	// Tail

	static class StringUnwrapperTailImpl extends StringUnwrapperImpl {

		String tailStr;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasTail(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			int tailLen = containsImpl.lengthEndsWith(str);
			if (tailLen != -1) {
				return removeTail(str, tailLen);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			int tailLen = containsImpl.lengthEndsWith(str);
			if (tailLen != -1) {
				return removeTailInline(str, tailLen);
			} else {
				return null;
			}
		}
	}

	// HeadOrTail

	static class StringUnwrapperHeadOrTailImpl extends StringUnwrapperImpl {
		String headStr;
		String tailStr;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasHead(str) || hasTail(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			boolean found = false;
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				found = true;
			} else {
				headLen = 0;
			}

			int tailLen = containsImpl.lengthEndsWith(str);
			if (tailLen != -1) {
				found = true;
			} else {
				tailLen = 0;
			}

			if (found) {
				return removeHeadTail(str, headLen, tailLen);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			boolean found = false;
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				found = true;
			} else {
				headLen = 0;
			}

			int tailLen = containsImpl.lengthEndsWith(str);
			if (tailLen != -1) {
				found = true;
			} else {
				tailLen = 0;
			}

			if (found) {
				return removeHeadTailInline(str, headLen, tailLen);
			} else {
				return null;
			}
		}
	}

	// HeadAndTail

	static class StringUnwrapperHeadAndTailImpl extends StringUnwrapperImpl {
		String headStr;
		String tailStr;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasHead(str) && hasTail(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				int tailLen = containsImpl.lengthEndsWith(str);
				if (tailLen != -1) {
					if (str.length() >= headLen + tailLen) {
						return removeHeadTail(str, headLen, tailLen);
					}
				}
			}
			return null;
		}

		@Override
		IString doTransformInline(IString str) {
			int headLen = containsImpl.lengthStartsWith(str);
			if (headLen != -1) {
				int tailLen = containsImpl.lengthEndsWith(str);
				if (tailLen != -1) {
					if (str.length() >= headLen + tailLen) {
						return removeHeadTailInline(str, headLen, tailLen);
					}
				}
			}
			return null;
		}
	}

}
