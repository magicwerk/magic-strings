/*
 * Copyright 2011 by Thomas Mauch
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
package org.magicwerk.strings;

import java.util.function.IntPredicate;
import java.util.regex.Pattern;

import org.magicwerk.strings.StringWrapper.WrapMode;
import org.magicwerk.strings.StringWrapper.WrapperContainsImpl;
import org.magicwerk.strings.StringWrapper.WrapperContainsMatcherImpl;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CodePointPredicates;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharCaseMode;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.chars.CharCaseTools.ICharMode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.RegexTools;
import org.magicwerk.strings.matcher.AnchoredStringMatcher;
import org.magicwerk.strings.matcher.AnyCharMatcher;
import org.magicwerk.strings.matcher.AnyCodePointMatcher;
import org.magicwerk.strings.matcher.CharIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CharMatcher;
import org.magicwerk.strings.matcher.CharPredicateMatcher;
import org.magicwerk.strings.matcher.CodePointIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CodePointMatcher;
import org.magicwerk.strings.matcher.CodePointPredicateMatcher;
import org.magicwerk.strings.matcher.IStringEndsAtMatcher;
import org.magicwerk.strings.matcher.IStringIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.IStringStartsAtMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;
import org.magicwerk.strings.matcher.StringMatcher;
import org.magicwerk.strings.matcher.AnyCharMatcher.AnyCharIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.AnyCodePointMatcher.AnyCodePointIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CharPredicateMatcher.CharPredicateIgnoreCaseMatcher;
import org.magicwerk.strings.matcher.CodePointPredicateMatcher.CodePointPredicateIgnoreCaseMatcher;

/**
 * Class {@link BuilderHelper} contains several implementations similar to {@link StringBuilder}.
 */
public class BuilderHelper {

	static final CharPredicate trimmerPredicateWhitespace = CharPredicates.whitespace;
	static final char padderCharSpace = ' ';

	static IStringMatcher getStringMatcher(String string) {
		return StringMatcher.of(string);
	}

	static IStringMatcher getCharMatcher(char character) {
		return new CharMatcher(character);
	}

	static IStringMatcher getCodePointMatcher(int codePoint) {
		return new CodePointMatcher(codePoint);
	}

	static IStringMatcher getCharPredicateMatcher(CharPredicate charPredicate) {
		return new CharPredicateMatcher(charPredicate);
	}

	static IStringMatcher getCodePointPredicateMatcher(IntPredicate codePointPredicate) {
		return new CodePointPredicateMatcher(codePointPredicate);
	}

	static IStringMatcher getStringMatcher(String string, CharEqual charEqual) {
		return StringMatcher.of(string, charEqual);
	}

	static IStringMatcher getStringMatcher(String string, CodePointEqual codePointEqual) {
		return StringMatcher.of(string, codePointEqual);
	}

	static IStringMatcher getCharMatcher(char character, CharEqual charEqual) {
		return new CharIgnoreCaseMatcher(character, charEqual);
	}

	static IStringMatcher getCodePointMatcher(int codePoint, CodePointEqual codePointEqual) {
		return new CodePointIgnoreCaseMatcher(codePoint, codePointEqual);
	}

	//

	/**
	 * Class {@link BuilderStringBase} implements a literal definition of a string which can be used both for searching 
	 * (as part of {@link BuilderMatcherBase}), but also to create literal text, e.g. for adding wrappings in StringWrapper.
	 * It supports char, code point, and string.
	 */
	public static class BuilderStringBase {
		String string;
		Character character;
		Integer codePoint;

		//

		/** Set string (may be optimized to char or codePoint) */
		protected void setString(CharSequence str) {
			if (str.length() == 1) {
				// Treat string as char if possible
				doSetChar(str.charAt(0));
			} else {
				int cp = CodePointTools.singleCodePoint(str);
				if (cp != -1) {
					// Treat string as code point if possible
					doSetCodePoint(cp);
				} else {
					doSetString(str);
				}
			}
		}

		void doSetString(CharSequence str) {
			reset();
			this.string = str.toString();
		}

		/** Set codePoint (may be optimized to char) */
		protected void setCodePoint(int codePoint) {
			if (CodePointTools.isCharCodePoint(codePoint)) {
				// Treat code point as char if possible
				doSetChar((char) codePoint);
			} else {
				doSetCodePoint(codePoint);
			}
		}

		/** Set character */
		void doSetCodePoint(int codePoint) {
			reset();
			this.codePoint = codePoint;
		}

		/** Set character */
		protected void setChar(char c) {
			doSetChar(c);
		}

		/** Set character */
		void doSetChar(char c) {
			reset();
			this.character = c;
		}

		void reset() {
			string = null;
			character = null;
			codePoint = null;
		}

		/** Returns true if text to read/write is defined */
		boolean isDefined() {
			return string != null || character != null || codePoint != null;
		}

		Character doGetChar() {
			return character;
		}

		Integer doGetCodePoint() {
			return codePoint;
		}

		String doGetString() {
			return string;
		}

		boolean isEmptyString() {
			return string != null && string.isEmpty();
		}

		/** Get value as string, null if not set */
		String getAsString() {
			if (string != null) {
				return string;
			} else if (character != null) {
				return Character.toString(character);
			} else if (codePoint != null) {
				return Character.toString(codePoint);
			} else {
				return null;
			}
		}
	}

	/**
	 * Class {@link BuilderMatcherBase} represents a definition to look for a text match.
	 * It supports char, code point, string, char predicate, code point predicate, any char, regex, and IStringMatcher.
	 * The support for char, code point, string is inherited from {@link BuilderStringBase}.
	 * It supports case sensitive and insensitive matching through {@link #charMode}.
	 */
	public static class BuilderMatcherBase extends BuilderStringBase {
		/** How to handle case on searching, null for case sensitivity */
		ICharMode charMode;

		CharPredicate charPredicate;
		IntPredicate codePointPredicate;
		String anyChar;
		String regex;
		IStringMatcher matcher;

		//

		/** Return {@link CharEqual} to use, or null for default equality operator ("==") */
		CharEqual getCharEqual() {
			if (charMode == null) {
				return null;
			}
			CharEqual charEqual = charMode.getCharEqual();
			if (charEqual == null || charEqual == CharEqual.isEqualChar()) {
				return null;
			}
			return charEqual;
		}

		/** Return {@link CodePointEqual} to use, or null for default equality operator ("==") */
		CodePointEqual getCodePointEqual() {
			if (charMode == null) {
				return null;
			}
			CodePointEqual codePointEqual = charMode.getCodePointEqual();
			if (codePointEqual == null || codePointEqual == CodePointEqual.isEqualCodePoint()) {
				return null;
			}
			return codePointEqual;
		}

		/** Setter for {@link #charMode} (using {@link CharMode#getCharMode}) */
		void doSetIgnoreCase(boolean ignoreCase) {
			this.charMode = CharMode.getCharMode(ignoreCase);
		}

		/** Setter for {@link #charMode} */
		void doSetCharMode(ICharMode charMode) {
			this.charMode = charMode;
		}

		protected void setCharPredicate(CharPredicate charPredicate) {
			doSetCharPredicate(charPredicate);
		}

		void doSetCharPredicate(CharPredicate charPredicate) {
			reset();
			this.charPredicate = charPredicate;
		}

		protected void setCodePointPredicate(IntPredicate codePointPredicate) {
			doSetCodePointPredicate(codePointPredicate);
		}

		void doSetCodePointPredicate(IntPredicate codePointPredicate) {
			reset();
			this.codePointPredicate = codePointPredicate;
		}

		/** Set anyChar (may be optimized to character or codePoint) */
		protected void setAnyChar(String anyChar) {
			int len = anyChar.length();
			if (len == 1) {
				// Treat as single char
				doSetChar(anyChar.charAt(0));
			} else if (len == 2 && CodePointTools.codePointCount(anyChar) == 1) {
				// Treat as single code point
				doSetCodePoint(CodePointTools.firstCodePoint(anyChar));
			} else {
				doSetAnyChar(anyChar);
			}
		}

		void doSetAnyChar(String anyChar) {
			reset();
			this.anyChar = anyChar;
		}

		boolean isEmptyAnyChar() {
			return anyChar != null && anyChar.isEmpty();
		}

		/** Set regex (may be optimized to string) */
		protected void setRegex(String regex) {
			String literal = getRegexAsStringLiteral(regex);
			if (literal != null) {
				// Treat regex as string if possible
				setString(literal);
			} else {
				doSetRegex(regex);
			}
		}

		void doSetRegex(String regex) {
			reset();
			this.regex = regex;
		}

		String getRegexAsStringLiteral(String regex) {
			return RegexTools.getStringLiteral(regex);
		}

		protected void setMatcher(IStringMatcher matcher) {
			doSetMatcher(matcher);
		}

		void doSetMatcher(IStringMatcher matcher) {
			reset();
			this.matcher = matcher;
		}

		@Override
		boolean isDefined() {
			return super.isDefined() || charPredicate != null || codePointPredicate != null || anyChar != null || regex != null || matcher != null;
		}

		@Override
		void reset() {
			super.reset();

			charPredicate = null;
			codePointPredicate = null;
			anyChar = null;
			regex = null;
			matcher = null;
		}

		boolean supportsCaseInsensitive() {
			return character != null || codePoint != null || string != null || anyChar != null || regex != null;
		}

		int getCharSupport() {
			ICharMode cm = (charMode != null) ? charMode : CharMode.CS_CHAR; // TODO

			if (cm.getCharCaseMode() != CharCaseMode.CASE_SENSITIVE) {
				CheckTools.check(supportsCaseInsensitive(), "Case Insensitive handling not supported");
			}

			if (character != null) {
				return CharCaseTools.getCharSupport(cm, character);
			} else if (codePoint != null) {
				return CharCaseTools.getCharSupport(cm, codePoint);
			} else {
				String s = getStringLike();
				if (s != null) {
					return CharCaseTools.getCharSupport(cm, s);
				}
			}
			return 0;
		}

		String getStringLike() {
			if (string != null) {
				return string;
			} else if (anyChar != null) {
				return anyChar;
			} else if (regex != null) {
				return regex;
			} else {
				return null;
			}
		}

		IStringMatcher getAsStringMatcher() {
			return getAsStringMatcher(charMode);
		}

		/** Returns IStringMatcher representing the defined match (never null) */
		IStringMatcher getAsStringMatcher(ICharMode ignoreCase) {
			return (ignoreCase != null) ? doGetAsStringMatcherIgnoreCase(ignoreCase) : doGetAsStringMatcher();
		}

		/** Returns {@link CharPredicate} representing the defined match, null if not possible */
		CharPredicate getAsCharPredicate() {
			if (charMode != null && charMode.getCharCaseMode() != CharCaseMode.CASE_SENSITIVE) { // FIXME
				return null;
			}
			if (charPredicate != null) {
				return charPredicate;
			} else if (character != null) {
				return CharPredicates.of(character);
			} else if (anyChar != null) {
				if (!CodePointTools.containsCodePoint(anyChar)) {
					return CharPredicates.oneOf(anyChar);
				}
			}
			return null;
		}

		/** Returns {@link IntPredicate} representing the defined match, null if not possible */
		IntPredicate getAsCodePointPredicate() {
			if (charMode != null && charMode.getCharCaseMode() != CharCaseMode.CASE_SENSITIVE) { // FIXME 
				return null;
			}
			CharPredicate cp = getAsCharPredicate();
			if (cp != null) {
				return CharCaseTools.toCodePointPredicate(cp);
			}
			if (codePointPredicate != null) {
				return codePointPredicate;
			} else if (codePoint != null) {
				return CodePointPredicates.of(codePoint);
			} else if (anyChar != null) {
				if (CodePointTools.containsCodePoint(anyChar)) {
					return CodePointPredicates.oneOf(anyChar);
				}
			}
			return null;
		}

		/**
		 * Returns an instance of {@link IStringMatcher} for the specified search configuration.
		 */
		IStringMatcher doGetAsStringMatcher() {
			if (string != null) {
				return getStringMatcher(string);
			} else if (character != null) {
				return getCharMatcher(character);
			} else if (codePoint != null) {
				return getCodePointMatcher(codePoint);
			} else if (charPredicate != null) {
				return getCharPredicateMatcher(charPredicate);
			} else if (codePointPredicate != null) {
				return getCodePointPredicateMatcher(codePointPredicate);
			} else if (anyChar != null) {
				if (CodePointTools.containsCodePoint(anyChar)) {
					return new AnyCodePointMatcher(anyChar);
				} else {
					return new AnyCharMatcher(anyChar);
				}
			} else if (regex != null) {
				Pattern pattern = RegexTools.getPatternIgnoreCase(regex, false);
				return new RegexStringMatcher().setPattern(pattern);
			} else if (matcher != null) {
				return matcher;
			} else {
				throw new AssertionError();
			}
		}

		/**
		 * Returns an instance of {@link IStringMatcher} for the specified search configuration which ignores case.
		 */
		IStringMatcher doGetAsStringMatcherIgnoreCase(ICharMode ic) {
			if (string != null) {
				if (CodePointTools.containsCodePoint(string)) {
					return getStringMatcher(string, ic.getCodePointEqual());
				} else {
					return getStringMatcher(string, ic.getCharEqual());
				}
			} else if (character != null) {
				return getCharMatcher(character, ic.getCharEqual());
			} else if (codePoint != null) {
				return getCodePointMatcher(codePoint, ic.getCodePointEqual());
			} else if (charPredicate != null) {
				return new CharPredicateIgnoreCaseMatcher(charPredicate);
			} else if (codePointPredicate != null) {
				return new CodePointPredicateIgnoreCaseMatcher(codePointPredicate);
			} else if (anyChar != null) {
				if (CodePointTools.containsCodePoint(anyChar)) {
					return new AnyCodePointIgnoreCaseMatcher(anyChar, ic.getCodePointEqual());
				} else {
					return new AnyCharIgnoreCaseMatcher(anyChar, ic.getCharEqual());
				}
			} else if (regex != null) {
				Pattern pattern = RegexTools.getPatternIgnoreCase(regex, true);
				return new RegexStringMatcher().setPattern(pattern);
			} else if (matcher != null) {
				if (matcher instanceof IStringIgnoreCaseMatcher) {
					IStringIgnoreCaseMatcher m = (IStringIgnoreCaseMatcher) matcher;
					CheckTools.check(m.isIgnoreCase(), "Matcher has ignoreCase not enabled");
					return matcher;
				} else {
					throw CheckTools.error("Matcher does not support ignoreCase"); // TODO
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	public static class BuilderFinderBase<B extends BuilderFinderBase<B>> extends BuilderMatcherBase {

		@SuppressWarnings("unchecked")
		B castThis() {
			return (B) this;
		}

		/** Setter for {@link CharMode} (using {@link CharMode#getCharMode}) */
		public B setIgnoreCase(boolean ignoreCase) {
			doSetIgnoreCase(ignoreCase);
			return castThis();
		}

		/** Setter for {@link CharMode} */
		public B setCharMode(ICharMode charMode) {
			doSetCharMode(charMode);
			return castThis();
		}

		public B setFindString(CharSequence str) {
			setString(str);
			return castThis();
		}

		public B setFindChar(char c) {
			setChar(c);
			return castThis();
		}

		public B setFindCodePoint(int codePoint) {
			setCodePoint(codePoint);
			return castThis();
		}

		public B setFindCharPredicate(CharPredicate charPredicate) {
			setCharPredicate(charPredicate);
			return castThis();
		}

		public B setFindCodePointPredicate(IntPredicate codePointPredicate) {
			setCodePointPredicate(codePointPredicate);
			return castThis();
		}

		public B setFindAnyChar(String anyChar) {
			setAnyChar(anyChar);
			return castThis();
		}

		public B setFindRegex(String regex) {
			setRegex(regex);
			return castThis();
		}

		public B setFindMatcher(IStringMatcher matcher) {
			setMatcher(matcher);
			return castThis();
		}

		/** Check that the required information for creating the builder are present */
		void check() {
			CheckTools.check(isDefined(), "Builder has no search definition");
		}
	}

	public interface IStringTransformerBuilderBase {
		/** Setter for returnMode ({@link ReturnMode}, default value is {@link ReturnMode#RETURN_UNCHANGED}) */
		IStringTransformerBuilderBase setReturnMode(ReturnMode returnMode);
	}

	public interface IStringTransformerBuilder extends IStringTransformerBuilderBase {
		IStringTransformer build();
	}

	/**
	 * Class {@link BuilderReplacerBase} is the common base class for the builders of
	 * {@link StringRemover} and {@link StringReplacer}.
	 */
	public abstract static class BuilderReplacerBase<B extends BuilderReplacerBase<B>> extends BuilderFinderBase<B> implements IStringTransformerBuilder {
		/** Number of replace operations to execute, -1 for unlimited */
		int numReplace = -1;
		/** If true, there may not be more matches than specified in {@link #numReplace} */
		boolean checkReplace = false;
		/** Determine what to return if no replacement is made */
		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;

		//

		/** Setter for {@link #returnMode} */
		@Override
		public B setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return castThis();
		}

		/** Setter for {@link #numReplace} */
		public B setNumReplace(int numReplace) {
			this.numReplace = numReplace;
			return castThis();
		}

		/** Setter for {@link #checkReplace} */
		public B setCheckReplace(boolean checkReplace) {
			this.checkReplace = checkReplace;
			return castThis();
		}

		@Override
		@SuppressWarnings("unchecked")
		B castThis() {
			return (B) this;
		}
	}

	/**
	 * Class {@link BuilderWrapperBase} is base class for StringWrapper / StringUnwrapper.
	 */
	public abstract static class BuilderWrapperBase<B extends BuilderWrapperBase<B>> implements IStringTransformerBuilder {

		/**
		 * Class {@link WrapperContainsBuilder} c
		 */
		static class WrapperContainsBuilder {
			/** True if information for handling head is needed */
			boolean needsHead;
			/** True if information for handling tail is needed */
			boolean needsTail;

			boolean useChar;
			Character headChar;
			Character tailChar;
			boolean useString;
			String headString;
			String tailString;
			boolean useMatcher;
			IStringMatcher headMatcher;
			IStringMatcher tailMatcher;

			/** Returns true if either headChar, headString or headMatcher is non-null */
			boolean hasHead() {
				return headMatcher != null || headString != null || headChar != null;
			}

			/** Returns true if either tailChar, tailString or tailMatcher is non-null */
			boolean hasTail() {
				return tailMatcher != null || tailString != null || tailChar != null;
			}
		}

		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;
		/** {@link WrapMode}, default value is {@link WrapMode#HEAD_AND_TAIL} */
		WrapMode wrapMode = WrapMode.HEAD_AND_TAIL;
		BuilderMatcherBase containsHead = new BuilderMatcherBase();
		BuilderMatcherBase containsTail = new BuilderMatcherBase();

		@SuppressWarnings("unchecked")
		B castThis() {
			return (B) this;
		}

		@Override
		public B setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return castThis();
		}

		/** Setter for {@link CharMode} (using {@link CharMode#getCharMode}) */
		public B setIgnoreCase(boolean ignoreCase) {
			containsHead.doSetIgnoreCase(ignoreCase);
			containsTail.doSetIgnoreCase(ignoreCase);
			return castThis();
		}

		/** Setter for {@link CharMode} */
		public B setCharMode(ICharMode charMode) {
			containsHead.doSetCharMode(charMode);
			containsTail.doSetCharMode(charMode);
			return castThis();
		}

		/** Setter for {@link #wrapMode} */
		public B setWrapMode(WrapMode wrapMode) {
			this.wrapMode = wrapMode;
			return castThis();
		}

		WrapperContainsImpl createStringLocationContainsMatcherImpl(IStringMatcher headMatcher, IStringMatcher tailMatcher) {
			WrapperContainsMatcherImpl c = new WrapperContainsMatcherImpl(
					createIStringStartsAtMatcher(headMatcher), createIStringEndsAtMatcher(tailMatcher));
			return c;
		}

		IStringStartsAtMatcher createIStringStartsAtMatcher(IStringMatcher matcher) {
			return (matcher instanceof IStringStartsAtMatcher) ? (IStringStartsAtMatcher) matcher : AnchoredStringMatcher.startsWith(matcher);
		}

		IStringEndsAtMatcher createIStringEndsAtMatcher(IStringMatcher matcher) {
			return (matcher instanceof IStringEndsAtMatcher) ? (IStringEndsAtMatcher) matcher : AnchoredStringMatcher.endsWith(matcher);
		}

		WrapperContainsBuilder analyzeContains() {
			WrapperContainsBuilder contains = new WrapperContainsBuilder();

			contains.needsHead = (wrapMode != WrapMode.TAIL);
			contains.needsTail = (wrapMode != WrapMode.HEAD);

			if (contains.needsHead && containsHead.isDefined()) {
				contains.headChar = containsHead.doGetChar();
				if (contains.headChar != null) {
					contains.useChar = true;
				} else {
					contains.headString = containsHead.getAsString();
					if (contains.headString != null) {
						contains.useString = true;
					} else {
						contains.headMatcher = containsHead.getAsStringMatcher();
						if (contains.headMatcher != null) {
							contains.useMatcher = true;
						}
					}
				}
			}

			if (contains.needsTail && containsTail.isDefined()) {
				contains.tailChar = containsTail.doGetChar();
				if (contains.tailChar != null) {
					contains.useChar = true;
				} else {
					contains.tailString = containsTail.getAsString();
					if (contains.tailString != null) {
						contains.useString = true;
					} else {
						contains.tailMatcher = containsTail.getAsStringMatcher();
						if (contains.tailMatcher != null) {
							contains.useMatcher = true;
						}
					}
				}
			}

			return contains;
		}

	}

}
