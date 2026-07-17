package org.magicwerk.strings;

import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.BuilderMatcherBase;
import org.magicwerk.strings.BuilderHelper.BuilderStringBase;
import org.magicwerk.strings.BuilderHelper.BuilderWrapperBase;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.chars.CharCaseTools.ICharMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.FuncTools;
import org.magicwerk.strings.matcher.IStringEndsAtMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringStartsAtMatcher;

/**
 * Class {@link StringWrapper} adds wrapping markers to strings.
 */
public interface StringWrapper extends IStringTransformer {

	// IStringTransformer

	/**
	 * This method calls {@link #wrap}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return wrap(input);
	}

	/**
	 * This method calls {@link #wrapInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		wrapInline(input);
	}

	// String

	/** 
	 * Determines whether string contains wrapping markers as configured.
	 */
	default boolean isWrapped(String str) {
		return isWrapped((CharSequence) str);
	}

	/** Add wrapping markers as configured to string */
	default String wrap(String str) {
		return (String) wrap((CharSequence) str);
	}

	// CharSequence

	/** 
	 * Determines whether string contains wrapping markers as configured.
	 */
	boolean isWrapped(CharSequence str);

	/** Add wrapping markers as configured to string */
	CharSequence wrap(CharSequence str);

	/** Add wrapping markers as configured to mutable string */
	void wrapInline(IString str);

	//

	/** Build {@link StringWrapper} with specified builder function */
	public static StringWrapper build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringWrapper}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringWrapper}.
	 */
	public static class Builder extends BuilderWrapperBase<Builder> {

		/** If true, wrapping is always without checking whether the string is already wrapped */
		boolean wrapAlways = false;

		BuilderStringBase addHead = new BuilderMatcherBase();
		BuilderStringBase addTail = new BuilderMatcherBase();

		/** Setter for {@link #wrapAlways} */
		public Builder setWrapAlways(boolean wrapAlways) {
			this.wrapAlways = wrapAlways;
			return this;
		}

		// Char

		public Builder setWrapChar(char c) {
			this.addHead.setChar(c);
			this.addTail.setChar(c);
			return this;
		}

		public Builder setWrapHeadChar(char c) {
			this.addHead.setChar(c);
			return this;
		}

		public Builder setWrapTailChar(char c) {
			this.addTail.setChar(c);
			return this;
		}

		//

		public Builder setWrapCodePoint(int cp) {
			this.addHead.setCodePoint(cp);
			this.addTail.setCodePoint(cp);
			return this;
		}

		public Builder setWrapHeadCodePoint(int cp) {
			this.addHead.setCodePoint(cp);
			return this;
		}

		public Builder setWrapTailCodePoint(int cp) {
			this.addTail.setCodePoint(cp);
			return this;
		}

		//

		/** Setter for {@link #addHead} and {@link #addTail} */
		public Builder setWrapString(String str) {
			this.addHead.setString(str);
			this.addTail.setString(str);
			return this;
		}

		public Builder setWrapHeadString(String str) {
			this.addHead.setString(str);
			return this;
		}

		public Builder setWrapTailString(String str) {
			this.addTail.setString(str);
			return this;
		}

		//

		public Builder setMatchChar(char c) {
			this.containsHead.setChar(c);
			this.containsTail.setChar(c);
			return this;
		}

		public Builder setMatchHeadChar(char c) {
			this.containsHead.setChar(c);
			return this;
		}

		public Builder setMatchTailChar(char c) {
			this.containsTail.setChar(c);
			return this;
		}

		//

		public Builder setMatchCodePoint(int cp) {
			this.containsHead.setCodePoint(cp);
			this.containsTail.setCodePoint(cp);
			return this;
		}

		public Builder setMatchHeadCodePoint(int cp) {
			this.containsHead.setCodePoint(cp);
			return this;
		}

		public Builder setMatchTailCodePoint(int cp) {
			this.containsTail.setCodePoint(cp);
			return this;
		}

		//

		/** Setter for {@link #containsHead} and {@link #containsTail} */
		public Builder setMatchString(String str) {
			this.containsHead.setString(str);
			this.containsTail.setString(str);
			return this;
		}

		public Builder setMatchHeadString(String str) {
			this.containsHead.setString(str);
			return this;
		}

		public Builder setMatchTailString(String str) {
			this.containsTail.setString(str);
			return this;
		}

		//

		public Builder setMatchCharPredicate(CharPredicate cp) {
			containsHead.setCharPredicate(cp);
			containsTail.setCharPredicate(cp);
			return this;
		}

		public Builder setMatchHeadCharPredicate(CharPredicate cp) {
			containsHead.setCharPredicate(cp);
			return this;
		}

		public Builder setMatchTailCharPredicate(CharPredicate cp) {
			containsTail.setCharPredicate(cp);
			return this;
		}

		//

		public Builder setMatchAnyChar(String anyChar) {
			containsHead.setAnyChar(anyChar);
			containsTail.setAnyChar(anyChar);
			return this;
		}

		public Builder setMatchHeadAnyChar(String anyChar) {
			containsHead.setAnyChar(anyChar);
			return this;
		}

		public Builder setMatchTailAnyChar(String anyChar) {
			containsTail.setAnyChar(anyChar);
			return this;
		}

		//

		public Builder setMatchRegex(String regex) {
			containsHead.setRegex(regex);
			containsTail.setRegex(regex);
			return this;
		}

		public Builder setMatchHeadRegex(String regex) {
			containsHead.setRegex(regex);
			return this;
		}

		public Builder setMatchTailRegex(String regex) {
			containsTail.setRegex(regex);
			return this;
		}

		//

		public Builder setMatchMatcher(IStringMatcher matcher) {
			containsHead.setMatcher(matcher);
			containsTail.setMatcher(matcher);
			return this;
		}

		public Builder setMatchHeadMatcher(IStringMatcher matcher) {
			containsHead.setMatcher(matcher);
			return this;
		}

		public Builder setMatchTailMatcher(IStringMatcher matcher) {
			containsTail.setMatcher(matcher);
			return this;
		}

		/**
		 * Build an instance of {@link StringWrapper} with the specified configuration.
		 */
		@Override
		public StringWrapper build() {
			String addHeadString = null;
			String addTailString = null;

			WrapperContainsBuilder contains = analyzeContains();
			if (contains.needsHead) {
				addHeadString = addHead.getAsString();
				CheckTools.check(addHeadString != null, "Missing head definition");
				if (!contains.hasHead()) {
					contains.headString = addHeadString;
					contains.useString = true;
				}
			}
			if (contains.needsTail) {
				addTailString = addTail.getAsString();
				CheckTools.check(addTailString != null, "Missing tail definition");
				if (!contains.hasTail()) {
					contains.tailString = addTailString;
					contains.useString = true;
				}
			}

			StringWrapperImpl c;
			if (wrapAlways) {
				c = createStringWrapperAlwaysImpl(addHeadString, addTailString);
			} else {
				c = createStringWrapperIfMissingImpl(addHeadString, addTailString);
			}

			// If wrapAlways is true, calling wrap() does not need an instance of WrapperContainsImpl.
			// It is however needed for calling isWrapped().
			WrapperContainsImpl ci;
			if (contains.useMatcher) {
				ci = createStringLocationContainsMatcherImpl(contains.headMatcher, contains.tailMatcher);
			} else if (contains.useString) {
				if (containsHead.charMode == null && containsTail.charMode == null) {
					ci = new WrapperContainsStringImpl(contains.headString, contains.tailString);
				} else {
					ICharMode headIgnoreCase = FuncTools.nvl(containsHead.charMode, CharMode.getCharMode(false));
					ICharMode tailIgnoreCase = FuncTools.nvl(containsTail.charMode, CharMode.getCharMode(false));
					boolean headHasCodePoint = contains.headString != null && CodePointTools.containsCodePoint(contains.headString);
					boolean tailHasCodePoint = contains.tailString != null && CodePointTools.containsCodePoint(contains.tailString);
					if (headHasCodePoint || tailHasCodePoint) {
						ci = new WrapperContainsStringCodePointEqualImpl(contains.headString, contains.tailString,
								headIgnoreCase.getCodePointEqual(), tailIgnoreCase.getCodePointEqual());
					} else {
						ci = new WrapperContainsStringCharEqualImpl(contains.headString, contains.tailString,
								headIgnoreCase.getCharEqual(), tailIgnoreCase.getCharEqual());
					}
				}
			} else if (contains.useChar) {
				ci = new WrapperContainsCharImpl(contains.headChar, contains.tailChar);
			} else {
				throw new AssertionError();
			}

			c.containsImpl = ci;
			c.returnMode = returnMode;
			return c;
		}

		StringWrapperImpl createStringWrapperAlwaysImpl(String headStr, String tailStr) {
			if (wrapMode == WrapMode.HEAD) {
				StringWrapperHeadImpl c;
				if (headStr.length() > 1) {
					c = new StringWrapperHeadImpl();
				} else {
					StringWrapperHeadCharImpl cc = new StringWrapperHeadCharImpl();
					cc.headChar = StringTools.firstChar(headStr);
					c = cc;
				}
				c.headStr = headStr;
				return c;

			} else if (wrapMode == WrapMode.TAIL) {
				StringWrapperTailImpl c;
				if (tailStr.length() > 1) {
					c = new StringWrapperTailImpl();
				} else {
					StringWrapperTailCharImpl cc = new StringWrapperTailCharImpl();
					cc.tailChar = StringTools.firstChar(tailStr);
					c = cc;
				}
				c.tailStr = tailStr;
				return c;

			} else if (wrapMode == WrapMode.HEAD_OR_TAIL) {
				StringWrapperHeadTailImpl c = null;
				if (headStr.length() > 1 || tailStr.length() > 1) {
					c = new StringWrapperHeadOrTailImpl();
				} else {
					StringWrapperHeadOrTailCharImpl cc = new StringWrapperHeadOrTailCharImpl();
					cc.headChar = StringTools.firstChar(tailStr);
					cc.tailChar = StringTools.lastChar(tailStr);
					c = cc;
				}
				c.headStr = headStr;
				c.tailStr = tailStr;
				return c;

			} else if (wrapMode == WrapMode.HEAD_AND_TAIL) {
				StringWrapperHeadTailImpl c;
				if (headStr.length() > 1 || tailStr.length() > 1) {
					c = new StringWrapperHeadAndTailImpl();
				} else {
					StringWrapperHeadAndTailCharImpl cc = new StringWrapperHeadAndTailCharImpl();
					cc.headChar = StringTools.firstChar(tailStr);
					cc.tailChar = StringTools.lastChar(tailStr);
					c = cc;
				}
				c.headStr = headStr;
				c.tailStr = tailStr;
				return c;
			} else {
				throw new AssertionError();
			}
		}

		StringWrapperImpl createStringWrapperIfMissingImpl(String headStr, String tailStr) {
			if (wrapMode == WrapMode.HEAD) {
				StringWrapperHeadImpl c;
				if (headStr.length() > 1) {
					c = new StringWrapperHeadIfMissingImpl();
				} else {
					StringWrapperHeadCharIfMissingImpl cc = new StringWrapperHeadCharIfMissingImpl();
					cc.headChar = StringTools.firstChar(headStr);
					c = cc;
				}
				c.headStr = headStr;
				return c;

			} else if (wrapMode == WrapMode.TAIL) {
				StringWrapperTailImpl c;
				if (tailStr.length() > 1) {
					c = new StringWrapperTailIfMissingImpl();
				} else {
					StringWrapperTailCharIfMissingImpl cc = new StringWrapperTailCharIfMissingImpl();
					cc.tailChar = StringTools.firstChar(tailStr);
					c = cc;
				}
				c.tailStr = tailStr;
				return c;

			} else if (wrapMode == WrapMode.HEAD_OR_TAIL) {
				StringWrapperHeadTailImpl c = null;
				if (headStr.length() > 1 || tailStr.length() > 1) {
					c = new StringWrapperHeadOrTailIfMissingImpl();
				} else {
					StringWrapperHeadOrTailCharIfMissingImpl cc = new StringWrapperHeadOrTailCharIfMissingImpl();
					cc.headChar = StringTools.firstChar(tailStr);
					cc.tailChar = StringTools.lastChar(tailStr);
					c = cc;
				}
				c.headStr = headStr;
				c.tailStr = tailStr;
				return c;

			} else if (wrapMode == WrapMode.HEAD_AND_TAIL) {
				StringWrapperHeadTailImpl c;
				if (headStr.length() > 1 || tailStr.length() > 1) {
					c = new StringWrapperHeadAndTailIfMissingImpl();
				} else {
					StringWrapperHeadAndTailCharIfMissingImpl cc = new StringWrapperHeadAndTailCharIfMissingImpl();
					cc.headChar = StringTools.firstChar(tailStr);
					cc.tailChar = StringTools.lastChar(tailStr);
					c = cc;
				}
				c.headStr = headStr;
				c.tailStr = tailStr;
				return c;
			} else {
				throw new AssertionError();
			}
		}

	}

	/** 
	 * Enumeration {@link WrapMode} is used to control wrapping and unwrapping in ({@link StringWrapper} and {@link StringUnwrapper}).
	 */
	public enum WrapMode {
		/** Only head of string is handled */
		HEAD,
		/** Only tail of string is handled */
		TAIL,
		/**
		 * Head and tail of string are handled separately. 
		 * Wrap: If head is not wrapped, it is wrapped, if tail is not wrapped, it is wrapped.
		 * Unwrap: If head is wrapped, it is unwrapped, if tail is wrapped, it is unwrapped.
		 */
		HEAD_OR_TAIL,
		/**
		 * Head and tail of string are handled together
		 * Wrap: If head or tail is not wrapped, they are both wrapped again.
		 * Unwrap: Only if both head and tail are wrapped, they are unwrapped. If only one matches, no change is done.
		 */
		HEAD_AND_TAIL;
	}

	//

	abstract static class StringWrapperImpl extends StringTransformerImpl implements StringWrapper {

		WrapperContainsImpl containsImpl;

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

		// StringWrapper

		@Override
		public boolean isWrapped(CharSequence str) {
			if (str == null) {
				return false;
			}
			return doIsWrapped(str);
		}

		abstract boolean doIsWrapped(CharSequence str);

		@Override
		public CharSequence wrap(CharSequence str) {
			return transform(str);
		}

		@Override
		public String wrap(String str) {
			return transform(str);
		}

		@Override
		public void wrapInline(IString str) {
			transformInline(str);
		}

		//

		String addHead(String str, char headChar) {
			return headChar + str;
		}

		String addHead(CharSequence str, String headStr) {
			return headStr + str;
		}

		String addTail(String str, char tailChar) {
			return str + tailChar;
		}

		String addTail(CharSequence str, String tailStr) {
			return str + tailStr;
		}

		String addHeadTail(String str, char headChar, char tailChar) {
			return headChar + str + tailChar;
		}

		String addHeadTail(CharSequence str, String headStr, String tailStr) {
			return headStr + str + tailStr;
		}

		//

		IString addHeadInline(IString str, String headStr) {
			str.addString(0, headStr);
			return str;
		}

		IString addTailInline(IString str, String tailStr) {
			str.addString(tailStr);
			return str;
		}

		IString addHeadTailInline(IString str, String headStr, String tailStr) {
			str.addString(tailStr);
			str.addString(0, headStr);
			return str;
		}
	}

	// Head

	static class StringWrapperHeadImpl extends StringWrapperImpl {

		String headStr;
		char headChar;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasHead(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			return addHead(str, headStr);
		}

		@Override
		IString doTransformInline(IString str) {
			return addHeadInline(str, headStr);
		}
	}

	static class StringWrapperHeadCharImpl extends StringWrapperHeadImpl {

		@Override
		String doTransformString(String str) {
			return addHead(str, headChar);
		}
	}

	static class StringWrapperHeadIfMissingImpl extends StringWrapperHeadImpl {

		@Override
		String doTransformString(String str) {
			if (!hasHead(str)) {
				return addHead(str, headStr);
			} else {
				return null;
			}
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			if (!hasHead(str)) {
				return addHead(str, headStr);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			if (!hasHead(str)) {
				return addHeadInline(str, headStr);
			} else {
				return null;
			}
		}
	}

	static class StringWrapperHeadCharIfMissingImpl extends StringWrapperHeadIfMissingImpl {

		@Override
		String doTransformString(String str) {
			if (!hasHead(str)) {
				return addHead(str, headChar);
			} else {
				return null;
			}
		}
	}

	// Tail

	static class StringWrapperTailImpl extends StringWrapperImpl {

		String tailStr;
		char tailChar;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasTail(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			return addTail(str, tailStr);
		}

		@Override
		IString doTransformInline(IString str) {
			return addTailInline(str, tailStr);
		}
	}

	static class StringWrapperTailCharImpl extends StringWrapperTailImpl {

		@Override
		String doTransformString(String str) {
			return addTail(str, tailChar);
		}
	}

	static class StringWrapperTailIfMissingImpl extends StringWrapperTailImpl {

		@Override
		String doTransformString(String str) {
			if (!hasTail(str)) {
				return addTail(str, tailStr);
			} else {
				return null;
			}
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			if (!hasTail(str)) {
				return addTail(str, tailStr);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			if (!hasTail(str)) {
				return addTailInline(str, tailStr);
			} else {
				return null;
			}
		}
	}

	static class StringWrapperTailCharIfMissingImpl extends StringWrapperTailIfMissingImpl {

		@Override
		String doTransformString(String str) {
			if (!hasTail(str)) {
				return addTail(str, tailChar);
			} else {
				return null;
			}
		}
	}

	// HeadTail

	abstract static class StringWrapperHeadTailImpl extends StringWrapperImpl {

		String headStr;
		String tailStr;
		char headChar;
		char tailChar;

		@Override
		boolean doIsWrapped(CharSequence str) {
			return hasHead(str) && hasTail(str);
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			return addHeadTail(str, headStr, tailStr);
		}

		@Override
		IString doTransformInline(IString str) {
			return addHeadTailInline(str, headStr, tailStr);
		}
	}

	abstract static class StringWrapperHeadTailCharImpl extends StringWrapperHeadTailImpl {

		@Override
		String doTransformString(String str) {
			return addHeadTail(str, headChar, tailChar);
		}
	}

	// HeadOrTail

	static class StringWrapperHeadOrTailImpl extends StringWrapperHeadTailImpl {
	}

	static class StringWrapperHeadOrTailCharImpl extends StringWrapperHeadTailCharImpl {
	}

	static class StringWrapperHeadOrTailIfMissingImpl extends StringWrapperHeadOrTailImpl {

		@Override
		String doTransformString(String str) {
			boolean startsWith = hasHeadString(str);
			boolean endsWith = hasTailString(str);
			if (!startsWith) {
				if (!endsWith) {
					return addHeadTail(str, headStr, tailStr);
				} else {
					return addHead(str, headStr);
				}
			} else {
				if (!endsWith) {
					return addTail(str, tailStr);
				} else {
					return null;
				}
			}
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith) {
				if (!endsWith) {
					return addHeadTail(str, headStr, tailStr);
				} else {
					return addHead(str, headStr);
				}
			} else {
				if (!endsWith) {
					return addTail(str, tailStr);
				} else {
					return null;
				}
			}
		}

		@Override
		IString doTransformInline(IString str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith) {
				if (!endsWith) {
					return addHeadTailInline(str, headStr, tailStr);
				} else {
					return addHeadInline(str, headStr);
				}
			} else {
				if (!endsWith) {
					return addTailInline(str, tailStr);
				} else {
					return null;
				}
			}
		}
	}

	static class StringWrapperHeadOrTailCharIfMissingImpl extends StringWrapperHeadOrTailIfMissingImpl {

		@Override
		String doTransformString(String str) {
			boolean startsWith = hasHeadString(str);
			boolean endsWith = hasTailString(str);
			if (!startsWith) {
				if (!endsWith) {
					return addHeadTail(str, headChar, tailChar);
				} else {
					return addHead(str, headChar);
				}
			} else {
				if (!endsWith) {
					return addTail(str, tailChar);
				} else {
					return null;
				}
			}
		}
	}

	// HeadAndTail

	static class StringWrapperHeadAndTailImpl extends StringWrapperHeadTailImpl {
	}

	static class StringWrapperHeadAndTailCharImpl extends StringWrapperHeadTailCharImpl {
	}

	static class StringWrapperHeadAndTailIfMissingImpl extends StringWrapperHeadAndTailImpl {

		@Override
		String doTransformString(String str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith || !endsWith) {
				return addHeadTail(str, headStr, tailStr);
			} else {
				return null;
			}
		}

		@Override
		CharSequence doTransform(CharSequence str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith || !endsWith) {
				return addHeadTail(str, headStr, tailStr);
			} else {
				return null;
			}
		}

		@Override
		IString doTransformInline(IString str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith || !endsWith) {
				return addHeadTailInline(str, headStr, tailStr);
			} else {
				return null;
			}
		}
	}

	static class StringWrapperHeadAndTailCharIfMissingImpl extends StringWrapperHeadAndTailIfMissingImpl {

		@Override
		String doTransformString(String str) {
			boolean startsWith = hasHead(str);
			boolean endsWith = hasTail(str);
			if (!startsWith || !endsWith) {
				return super.doTransformString(str);
			} else {
				return null;
			}
		}
	}

	// Contains

	/**
	 * Class {@link WrapperContainsImpl} is the abstract base class used by {@link StringWrapper} and {@link StringUnwrapper} to
	 * determine whether a string is wrapped or not.
	 */
	abstract static class WrapperContainsImpl {

		boolean hasHeadString(String str) {
			return hasHead(str);
		}

		boolean hasTailString(String str) {
			return hasTail(str);
		}

		abstract boolean hasHead(CharSequence str);

		abstract boolean hasTail(CharSequence str);

		/** Returns length of match if str starts with prefix, otherwise -1 (used for unwrapping) */
		abstract int lengthStartsWith(CharSequence str);

		/** Returns length of match if str ends with suffix, otherwise -1 (used for unwrapping) */
		abstract int lengthEndsWith(CharSequence str);
	}

	// Contains string

	static class WrapperContainsCharImpl extends WrapperContainsImpl {

		final Character headChar;
		final Character tailChar;

		WrapperContainsCharImpl(Character headChar, Character tailChar) {
			this.headChar = headChar;
			this.tailChar = tailChar;
		}

		@Override
		boolean hasHead(CharSequence str) {
			return CharSequenceTools.startsWith(str, headChar);
		}

		@Override
		boolean hasTail(CharSequence str) {
			return CharSequenceTools.endsWith(str, tailChar);
		}

		@Override
		int lengthStartsWith(CharSequence str) {
			return (hasHead(str)) ? 1 : -1;
		}

		@Override
		int lengthEndsWith(CharSequence str) {
			return (hasTail(str)) ? 1 : -1;
		}
	}

	abstract static class WrapperContainsStringBaseImpl extends WrapperContainsImpl {
		final String headStr;
		final String tailStr;

		WrapperContainsStringBaseImpl(String headStr, String tailStr) {
			this.headStr = headStr;
			this.tailStr = tailStr;
		}

		@Override
		int lengthStartsWith(CharSequence str) {
			return (hasHead(str)) ? headStr.length() : -1;
		}

		@Override
		int lengthEndsWith(CharSequence str) {
			return (hasTail(str)) ? tailStr.length() : -1;
		}
	}

	static class WrapperContainsStringImpl extends WrapperContainsStringBaseImpl {

		WrapperContainsStringImpl(String headStr, String tailStr) {
			super(headStr, tailStr);
		}

		@Override
		boolean hasHead(CharSequence str) {
			return CharSequenceTools.startsWith(str, headStr);
		}

		@Override
		boolean hasTail(CharSequence str) {
			return CharSequenceTools.endsWith(str, tailStr);
		}

		@Override
		boolean hasHeadString(String str) {
			return str.startsWith(headStr);
		}

		@Override
		boolean hasTailString(String str) {
			return str.endsWith(headStr);
		}
	}

	static class WrapperContainsStringCharEqualImpl extends WrapperContainsStringBaseImpl {
		final CharEqual headEqual;
		final CharEqual tailEqual;

		WrapperContainsStringCharEqualImpl(String headStr, String tailStr, CharEqual headEqual, CharEqual tailEqual) {
			super(headStr, tailStr);
			this.headEqual = headEqual;
			this.tailEqual = tailEqual;
		}

		@Override
		boolean hasHead(CharSequence str) {
			return CharSequenceTools.startsWith(str, headStr, headEqual);
		}

		@Override
		boolean hasTail(CharSequence str) {
			return CharSequenceTools.endsWith(str, tailStr, tailEqual);
		}
	}

	static class WrapperContainsStringCodePointEqualImpl extends WrapperContainsStringBaseImpl {
		final CodePointEqual headEqual;
		final CodePointEqual tailEqual;

		WrapperContainsStringCodePointEqualImpl(String headStr, String tailStr, CodePointEqual headEqual, CodePointEqual tailEqual) {
			super(headStr, tailStr);
			this.headEqual = headEqual;
			this.tailEqual = tailEqual;
		}

		@Override
		boolean hasHead(CharSequence str) {
			return CharSequenceTools.startsWith(str, headStr, headEqual);
		}

		@Override
		boolean hasTail(CharSequence str) {
			return CharSequenceTools.endsWith(str, tailStr, tailEqual);
		}
	}

	// Contains matcher

	static class WrapperContainsMatcherImpl extends WrapperContainsImpl {

		final IStringStartsAtMatcher headMatcher;
		final IStringEndsAtMatcher tailMatcher;

		WrapperContainsMatcherImpl(IStringStartsAtMatcher headMatcher, IStringEndsAtMatcher tailMatcher) {
			this.headMatcher = headMatcher;
			this.tailMatcher = tailMatcher;
		}

		@Override
		boolean hasHead(CharSequence str) {
			IStringStartsAtMatcher m = headMatcher;
			return m.startsAt(str, 0);
		}

		@Override
		boolean hasTail(CharSequence str) {
			IStringEndsAtMatcher m = tailMatcher;
			int end = str.length();
			return m.endsAt(str, end);
		}

		@Override
		int lengthStartsWith(CharSequence str) {
			IStringStartsAtMatcher m = headMatcher;
			return m.indexOfEndStartingAt(str, 0);
		}

		@Override
		int lengthEndsWith(CharSequence str) {
			IStringEndsAtMatcher m = tailMatcher;
			int end = str.length();
			int index = m.indexOfEndingAt(str, end);
			return (index != -1) ? (end - index) : -1;
		}
	}

}
