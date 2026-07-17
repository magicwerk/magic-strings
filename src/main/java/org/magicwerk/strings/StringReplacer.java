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
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.BuilderReplacerBase;
import org.magicwerk.strings.BuilderHelper.BuilderStringBase;
import org.magicwerk.strings.CharSequenceTools.AppendCharReplacer;
import org.magicwerk.strings.CharSequenceTools.AppendCodePointReplacer;
import org.magicwerk.strings.StringRemover.IStringRemoverImpl;
import org.magicwerk.strings.StringReplacerAppender.CharAppendReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.CharOperatorReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.CodePointAppendReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.CodePointOperatorReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.ConstStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.EmptyStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.IStringReplaceAppender;
import org.magicwerk.strings.StringReplacerAppender.RegexMatchReplaceAppender;
import org.magicwerk.strings.chars.CharCaseTools;
import org.magicwerk.strings.chars.CharOperator;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.format.StringFormat;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringFixedLenMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.RegexStringMatcher;

/**
 * Class {@link StringReplacer} contains several implementations similar to {@link StringBuilder}.
 */
public interface StringReplacer extends IStringTransformer {

	/**
	 * This method calls {@link #replace}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return replace(input);
	}

	/**
	 * This method calls {@link #replaceInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		replaceInline(input);
	}

	//

	default String replace(String str) {
		return replace(str, 0);
	}

	default String replace(String str, int start) {
		return (String) replace((CharSequence) str, start);
	}

	default CharSequence replace(CharSequence str) {
		return replace(str, 0);
	}

	CharSequence replace(CharSequence str, int start);

	default void replaceInline(IString str) {
		replaceInline(str, 0);
	}

	default void replaceInline(IString str, int start) {
		// FIXME
		CharSequence result = replace(str, start);
		if (result == str || result == null) {
			return;
		}

		str.initArray(result.toString().toCharArray()); // FIXME
	}

	//

	/** Build {@link StringReplacer} with specified builder function */
	public static StringReplacer build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringReplacer}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringReplacer}.
	 */
	public static class Builder extends BuilderReplacerBase<Builder> {
		BuilderStringBase replaceBase = new BuilderStringBase();
		String replaceAnyChar;
		CharOperator replaceCharOperator;
		IntUnaryOperator replaceCodePointOperator;
		AppendCharReplacer replaceCharAppender;
		AppendCodePointReplacer replaceCodePointAppender;
		IStringReplaceAppender replaceAppender;

		//

		@Override
		void reset() {
			super.reset();

			// replaceAnyChar can only be set by replaceAnyChar() which sets both find and replace part.
			// It is therefore also reset if the find part is changed, e.g. by calling setFindChar()
			replaceAnyChar = null;
		}

		public Builder replaceString(String findStr, String replaceStr) {
			setFindString(findStr);
			setReplaceString(replaceStr);
			return this;
		}

		public Builder setReplaceString(String replaceStr) {
			resetReplace();
			this.replaceBase.setString(replaceStr);
			return this;
		}

		public Builder replaceChar(char findChar, char replaceChar) {
			setFindChar(findChar);
			setReplaceChar(replaceChar);
			return this;
		}

		public Builder replaceChar(CharPredicate predicate, CharOperator replaceCharOperator) {
			setFindCharPredicate(predicate);
			setReplaceCharOperator(replaceCharOperator);
			return this;
		}

		public Builder setReplaceCharOperator(CharOperator replaceCharOperator) {
			resetReplace();
			this.replaceCharOperator = replaceCharOperator;
			return this;
		}

		public Builder setReplaceChar(char replaceChar) {
			resetReplace();
			replaceBase.setChar(replaceChar);
			return this;
		}

		public Builder replaceCodePoint(int findCodePoint, int replaceCodePoint) {
			setFindCodePoint(findCodePoint);
			setReplaceCodePoint(replaceCodePoint);
			return this;
		}

		public Builder setReplaceCodePoint(int replaceCodePoint) {
			resetReplace();
			replaceBase.setCodePoint(replaceCodePoint);
			return this;
		}

		public Builder replaceCodePoint(IntPredicate predicate, IntUnaryOperator replaceCodePointOperator) {
			setFindCodePointPredicate(predicate);
			setReplaceCodePointOperator(replaceCodePointOperator);
			return this;
		}

		public Builder setReplaceCodePointOperator(IntUnaryOperator replaceCodePointOperator) {
			resetReplace();
			this.replaceCodePointOperator = replaceCodePointOperator;
			return this;
		}

		public Builder replaceAnyChar(String findAnyChar, String replaceAnyChar) {
			// Treat as single chars
			int findLen = findAnyChar.length();
			int replaceLen = replaceAnyChar.length();
			if (findLen == 1 && replaceLen == 1) {
				replaceChar(findAnyChar.charAt(0), replaceAnyChar.charAt(0));
				return this;
			}

			// Treat as single code points
			int cp0 = CodePointTools.singleCodePoint(findAnyChar);
			int cp1 = CodePointTools.singleCodePoint(replaceAnyChar);
			if (cp0 != -1 && cp1 != -1) {
				replaceCodePoint(cp0, cp1);
				return this;
			}

			doSetAnyChar(findAnyChar);
			resetReplace();
			this.replaceAnyChar = replaceAnyChar;
			return this;
		}

		public Builder replaceRegex(String findRegex, StringFormat format) {
			RegexStringMatcher rsm = new RegexStringMatcher();
			rsm.setPattern(findRegex);

			RegexMatchReplaceAppender rmp = new RegexMatchReplaceAppender();
			rmp.setFormat(format);

			return replaceRegex(rsm, rmp);
		}

		public Builder replaceRegex(String findRegex, String replaceRegexFormat) {
			RegexStringMatcher rsm = new RegexStringMatcher();
			rsm.setPattern(findRegex);

			RegexMatchReplaceAppender rmp = new RegexMatchReplaceAppender();
			rmp.setFormat(replaceRegexFormat);

			return replaceRegex(rsm, rmp);
		}

		String getRegexFormatAsStringLiteral(String format) { // TODO
			StringFormat sf = new StringFormat(format);
			return sf.getLiteralString();
		}

		public Builder replaceRegex(RegexStringMatcher rsm, RegexMatchReplaceAppender rmp) {
			resetReplace();
			this.matcher = rsm;
			this.replaceAppender = rmp;
			return this;
		}

		public Builder replace(IStringMatcher matcher, IStringReplaceAppender replacer) {
			setFindMatcher(matcher);
			setReplaceAppender(replacer);
			return this;
		}

		public Builder setReplaceAppender(IStringReplaceAppender replacer) {
			resetReplace();
			this.replaceAppender = replacer;
			return this;
		}

		void resetReplace() {
			replaceBase.reset();
			replaceAnyChar = null;
			replaceCharOperator = null;
			replaceCodePointOperator = null;
			replaceAppender = null;
		}

		boolean hasReplace() {
			return replaceBase.isDefined() || replaceAnyChar != null || replaceCharOperator != null || replaceCodePointOperator != null
					|| replaceAppender != null;
		}

		String getReplaceAsString() {
			return replaceBase.getAsString();
		}

		IntUnaryOperator getReplaceCodePointOperator() {
			if (replaceCodePointOperator != null) {
				return replaceCodePointOperator;
			} else if (replaceCharOperator != null) {
				return CharCaseTools.toCodePointOperator(replaceCharOperator);
			} else {
				return null;
			}
		}

		IStringReplaceAppender getAsStringReplacer() {
			String str = null;
			if (replaceBase.string != null) {
				str = replaceBase.string;
			} else if (replaceBase.character != null) {
				str = Character.toString(replaceBase.character);
			} else if (replaceBase.codePoint != null) {
				str = Character.toString(replaceBase.codePoint);
			}
			if (str != null) {
				return new ConstStringReplaceAppender(str);
			} else {
				return replaceAppender;
			}
		}

		@Override
		void check() {
			CheckTools.check(isDefined(), "Builder has no search definition");
			CheckTools.check(hasReplace(), "Builder has no replace definition");
		}

		/**
		 * Build an instance of {@link StringReplacer} with the specified configuration.
		 * The created instance must be of the specified type or an exception is thrown.
		 */
		public <T extends StringReplacer> T build(Class<T> clazz) {
			@SuppressWarnings("unchecked")
			T finder = (T) build();
			return finder;
		}

		/**
		 * Build an instance of {@link StringReplacer} with the specified configuration.
		 */
		@Override
		public StringReplacer build() {
			check();

			StringRemoverReplacerImpl replacerImpl = null;
			boolean checked = true;
			if (!checkReplace) {
				if (numReplace == 0) {
					replacerImpl = getNoChangeReplacer();
				} else if (numReplace == -1) {
					checked = false;
				}
			}

			if (replacerImpl == null) {
				String findString = getAsString();
				String replaceString = getReplaceAsString();
				if (findString != null && findString.equals(replaceString)) {
					replacerImpl = (checkReplace && numReplace != 0) ? getNoChangeCheckedReplacer() : getNoChangeReplacer();
				}
			}

			if (replacerImpl == null) {
				CharEqual charEqual = getCharEqual();
				CodePointEqual codePointEqual = getCodePointEqual();

				if (character != null && replaceBase.character != null) {
					// character
					if (charEqual == null) {
						if (!checked) {
							if (ObjectTools.equals(character, replaceBase.character)) {
								replacerImpl = getNoChangeReplacer();
							} else {
								replacerImpl = new ReplaceCharImpl(character, replaceBase.character);
							}
						} else {
							replacerImpl = new ReplaceCharCheckedImpl(character, replaceBase.character);
						}
					} else {
						if (!checked) {
							replacerImpl = new ReplaceCharEqualImpl(character, replaceBase.character, charEqual);
						} else {
							replacerImpl = new ReplaceCharEqualCheckedImpl(character, replaceBase.character, charEqual);
						}
					}

				} else if (codePoint != null && replaceBase.codePoint != null) {
					// codePoint
					if (codePointEqual == null) {
						if (!checked) {
							if (ObjectTools.equals(codePoint, replaceBase.codePoint)) {
								replacerImpl = getNoChangeReplacer();
							} else {
								replacerImpl = new ReplaceCodePointImpl(codePoint, replaceBase.codePoint);
							}
						} else {
							replacerImpl = new ReplaceCodePointCheckedImpl(codePoint, replaceBase.codePoint);
						}
					} else {
						if (!checked) {
							replacerImpl = new ReplaceCodePointEqualImpl(codePoint, replaceBase.codePoint, codePointEqual);
						} else {
							replacerImpl = new ReplaceCodePointEqualCheckedImpl(codePoint, replaceBase.codePoint, codePointEqual);
						}
					}

				} else if (string != null && replaceBase.string != null) {
					// string
					if (charEqual == null && codePointEqual == null) {
						if (!checked) {
							if (ObjectTools.equals(string, replaceBase.string)) {
								replacerImpl = getNoChangeReplacer();
							} else {
								replacerImpl = new ReplaceStringImpl(string, replaceBase.string);
							}
						} else {
							replacerImpl = new ReplaceStringCheckedImpl(string, replaceBase.string);
						}

					} else if (charEqual != null) {
						if (!checked) {
							replacerImpl = new ReplaceStringCharEqualImpl(string, replaceBase.string, charEqual);
						} else {
							replacerImpl = new ReplaceStringCharEqualCheckedImpl(string, replaceBase.string, charEqual);
						}
					} else {
						assert codePointEqual != null;
						if (!checked) {
							replacerImpl = new ReplaceStringCodePointEqualImpl(string, replaceBase.string, codePointEqual);
						} else {
							replacerImpl = new ReplaceStringCodePointEqualCheckedImpl(string, replaceBase.string, codePointEqual);
						}
					}

				} else if (anyChar != null && replaceAnyChar != null) {
					// TODO ignoreCase
					// anyChar (replaceAnyChar is only set by replaceAnyChar)
					if (!CodePointTools.containsCodePoint(anyChar) && !CodePointTools.containsCodePoint(replaceAnyChar)) {
						// char
						int findLen = anyChar.length();
						int replaceLen = replaceAnyChar.length();
						CheckTools.check(findLen >= replaceLen);

						CharPredicate cp = getAsCharPredicate();
						if (findLen == replaceLen) {
							CharOperator op = c -> {
								int i = anyChar.indexOf(c);
								c = replaceAnyChar.charAt(i);
								return c;
							};

							if (!checked) {
								replacerImpl = new ReplaceCharPredicateOperatorImpl(cp, op);
							} else {
								replacerImpl = new ReplaceCharPredicateOperatorCheckedImpl(cp, op);
							}
						} else {
							AppendCharReplacer acr = (char c, StringBuilder buf) -> {
								int i = anyChar.indexOf(c);
								if (i < replaceAnyChar.length()) {
									c = replaceAnyChar.charAt(i);
									buf.append(c);
								}
							};

							if (!checked) {
								replacerImpl = new ReplaceCharPredicateAppenderImpl(cp, acr);
							} else {
								replacerImpl = new ReplaceCharPredicateAppenderCheckedImpl(cp, acr);
							}
						}

					} else {
						// codePoint
						int findLen = CodePointTools.codePointCount(anyChar);
						int replaceLen = CodePointTools.codePointCount(replaceAnyChar);
						CheckTools.check(findLen >= replaceLen);

						IntPredicate cp = getAsCodePointPredicate();
						if (findLen == replaceLen) {
							IntUnaryOperator op = c -> {
								int i = anyChar.indexOf(c);
								c = replaceAnyChar.charAt(i);
								return c;
							};

							if (!checked) {
								replacerImpl = new ReplaceCodePointPredicateOperatorImpl(cp, op);
							} else {
								replacerImpl = new ReplaceCodePointPredicateOperatorCheckedImpl(cp, op);
							}
						} else {
							AppendCodePointReplacer acr = (int c, StringBuilder buf) -> {
								int i = anyChar.indexOf(c);
								if (i < replaceAnyChar.length()) {
									c = replaceAnyChar.charAt(i);
									buf.append(c);
								}
							};

							if (!checked) {
								replacerImpl = new ReplaceCodePointPredicateAppenderImpl(cp, acr);
							} else {
								replacerImpl = new ReplaceCodePointPredicateAppenderCheckedImpl(cp, acr);
							}
						}
					}
				}
			}

			if (replacerImpl == null) {
				// CharPredicate
				CharPredicate cp = getAsCharPredicate();
				if (cp != null) {
					AppendCharReplacer acr = null;
					if (replaceCharOperator != null) {
						acr = AppendCharReplacer.replaceChar(replaceCharOperator);
					} else {
						String replaceString = getReplaceAsString();
						if (replaceString != null) {
							acr = AppendCharReplacer.appendString(replaceString);
						}
					}
					if (acr != null) {
						if (!checked) {
							replacerImpl = new ReplaceCharPredicateAppenderImpl(cp, acr);
						} else {
							replacerImpl = new ReplaceCharPredicateAppenderCheckedImpl(cp, acr);
						}
					}
				}
			}

			if (replacerImpl == null) {
				// CodePointPredicate
				IntPredicate cp = getAsCodePointPredicate();
				if (cp != null) {
					AppendCodePointReplacer acr = null;
					IntUnaryOperator op = getReplaceCodePointOperator();
					if (op != null) {
						acr = AppendCodePointReplacer.replaceCodePoint(op);
					} else {
						String replaceString = getReplaceAsString();
						if (replaceString != null) {
							acr = AppendCodePointReplacer.appendString(replaceString);
						}
					}
					if (acr != null) {
						if (!checked) {
							replacerImpl = new ReplaceCodePointPredicateAppenderImpl(cp, acr);
						} else {
							replacerImpl = new ReplaceCodePointPredicateAppenderCheckedImpl(cp, acr);
						}
					}
				}
			}

			if (replacerImpl == null) {
				IStringMatcher findMatcher = getAsStringMatcher();
				IStringReplaceAppender replaceAppender = getAsStringReplacer();

				if (!checked) {
					replacerImpl = new ReplaceMatcherImpl(findMatcher, replaceAppender);
				} else {
					replacerImpl = new ReplaceMatcherCheckedImpl(findMatcher, replaceAppender);
				}
			}

			if (checked) {
				replacerImpl.checkReplace = checkReplace;
				replacerImpl.numReplace = numReplace;
			}
			replacerImpl.returnMode = returnMode;
			if (replacerImpl instanceof IReplaceMatcherImpl) {
				return ((IReplaceMatcherImpl) replacerImpl);
			} else {
				return ((IStringReplacerImpl) replacerImpl);
			}
		}

		StringRemoverReplacerImpl getNoChangeReplacer() {
			return new NoChangeReplacer(getAsStringMatcher());
		}

		StringRemoverReplacerImpl getNoChangeCheckedReplacer() {
			return new NoChangeCheckedReplacer(getAsStringMatcher());
		}

	}

	/** 
	 * Class {@link StringRemoverReplacerImpl} is the base class for both 
	 * {@link IStringRemoverImpl} and {@link IStringReplacerImpl}.
	 */
	static abstract class StringRemoverReplacerImpl extends StringTransformerImpl {
		int numReplace;
		boolean checkReplace;

		//

		protected abstract IStringMatcher getAsIStringMatcher();

		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return null;
		}

		//

		protected String doReplaceString(String str) {
			// This method will be overwritten to directly call String methods
			return doReplaceString(str, 0);
		}

		protected String doReplaceString(String str, int start) {
			// This method will be overwritten to directly call String methods
			return (String) doReplace(str, start);
		}

		/** 
		 * Method to do the replacement operation. 
		 * It is guaranteed that parameter string is not null.
		 * If no replacement is done because none is found, the method must return the same unchanged object as received.
		 * If no replacement is done because the specified check fails, the method must return null.
		 */
		protected abstract CharSequence doReplace(CharSequence str, int start);

		protected IString doReplaceInline(IString str, int start) {
			// FIXME
			CharSequence result = doReplace(str, start);
			if (result == str || result == null) {
				return (IString) result;
			}

			str.initArray(result.toString().toCharArray());
			return str;
		}

		//

		@Override
		public String doTransformString(String str) {
			return doReplaceString(str);
		}

		@Override
		public CharSequence doTransform(CharSequence str) {
			return doReplace(str, 0);
		}

		@Override
		public IString doTransformInline(IString str) {
			return doReplaceInline(str, 0);
		}

		//

		@Override
		public String transform(String str) {
			String s = doTransformString(str);
			if (s != null && s != str) {
				return s; // some change done
			}
			return (String) handleNotChanged(s, returnMode);
		}

		@Override
		public CharSequence transform(CharSequence str) {
			CharSequence s = doTransform(str);
			if (s != null && s != str) {
				return s; // some change done
			}
			return handleNotChanged(s, returnMode);
		}

		@Override
		public void transformInline(IString str) {
			CharSequence s = doTransformInline(str);
			if (s != null && s != str) {
				return; // some change done
			}
			handleNotChangedInline((IString) s, returnMode);
		}

		@Override
		public CharSequence handleNotChanged(CharSequence str, ReturnMode returnMode) {
			ReturnMode mode = returnMode;
			if (str == null && mode == ReturnMode.RETURN_UNCHANGED) {
				mode = ReturnMode.THROW_EXCEPTION;
			}
			return super.handleNotChanged(str, mode);
		}

		@Override
		public void handleNotChangedInline(IString str, ReturnMode returnMode) {
			ReturnMode mode = returnMode;
			if (str == null && mode == ReturnMode.RETURN_UNCHANGED) {
				mode = ReturnMode.THROW_EXCEPTION;
			}
			super.handleNotChangedInline(str, mode);
		}
	}

	//  

	static abstract class IStringReplacerImpl extends StringRemoverReplacerImpl implements StringReplacer {
		@Override
		public String replace(String str) {
			return transform(str);
		}

		@Override
		public CharSequence replace(CharSequence str, int start) {
			return transform(str);
		}

		@Override
		public void replaceInline(IString str) {
			transformInline(str);
		}

		@Override
		protected abstract IStringReplaceAppender getAsIStringReplaceAppender();
	}

	// No change

	/** Class {@link NoChangeReplacer} could match, but the match would not result in a change (e.g. as find and replace string are equal */
	static class NoChangeReplacer extends IStringReplacerImpl {
		IStringMatcher matcher;

		NoChangeReplacer(IStringMatcher matcher) {
			this.matcher = matcher;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return str;
		}

		@Override
		public IString doReplaceInline(IString str, int start) {
			return str;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return matcher;
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new EmptyStringReplaceAppender();
		}
	}

	static class NoChangeCheckedReplacer extends NoChangeReplacer {

		NoChangeCheckedReplacer(IStringMatcher matcher) {
			super(matcher);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return null;
		}

		@Override
		public IString doReplaceInline(IString str, int start) {
			return null;
		}
	}

	// Char

	static abstract class IReplaceCharImpl extends IStringReplacerImpl {
		char findChar;
		char replaceChar;

		IReplaceCharImpl(char findChar, char replaceChar) {
			this.findChar = findChar;
			this.replaceChar = replaceChar;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharMatcher(findChar);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new ConstStringReplaceAppender(replaceChar);
		}
	}

	static class ReplaceCharImpl extends IReplaceCharImpl {

		ReplaceCharImpl(char findChar, char replaceChar) {
			super(findChar, replaceChar);
		}

		@Override
		protected String doReplaceString(String str) {
			// Fast call: String.replace(char, char)
			return str.replace(findChar, replaceChar);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceChar(str, findChar, replaceChar, start);
		}
	}

	static class ReplaceCharCheckedImpl extends IReplaceCharImpl {

		ReplaceCharCheckedImpl(char findChar, char replaceChar) {
			super(findChar, replaceChar);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCharChecked(str, findChar, replaceChar, start, numReplace, checkReplace);
		}
	}

	static class ReplaceCharEqualImpl extends IReplaceCharImpl {

		CharEqual equals;

		ReplaceCharEqualImpl(char findChar, char replaceChar, CharEqual equals) {
			super(findChar, replaceChar);
			this.equals = equals;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceChar(str, findChar, replaceChar, equals, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharMatcher(findChar, equals);
		}
	}

	static class ReplaceCharEqualCheckedImpl extends ReplaceCharEqualImpl {

		ReplaceCharEqualCheckedImpl(char findChar, char replaceChar, CharEqual equals) {
			super(findChar, replaceChar, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCharChecked(str, findChar, replaceChar, equals, start, numReplace, checkReplace);
		}
	}

	// Code point

	static class ReplaceCodePointImpl extends IStringReplacerImpl {
		int findCodePoint;
		int replaceCodePoint;

		ReplaceCodePointImpl(int findCodePoint, int replaceCodePoint) {
			this.findCodePoint = findCodePoint;
			this.replaceCodePoint = replaceCodePoint;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCodePoint(str, findCodePoint, replaceCodePoint, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointMatcher(findCodePoint);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new ConstStringReplaceAppender(replaceCodePoint);
		}
	}

	static class ReplaceCodePointCheckedImpl extends ReplaceCodePointImpl {

		ReplaceCodePointCheckedImpl(int findCodePoint, int replaceCodePoint) {
			super(findCodePoint, replaceCodePoint);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCodePointChecked(str, findCodePoint, replaceCodePoint, start, numReplace, checkReplace);
		}
	}

	static class ReplaceCodePointEqualImpl extends ReplaceCodePointImpl {
		CodePointEqual equals;

		ReplaceCodePointEqualImpl(int searchCodePoint, int replaceCodePoint, CodePointEqual equals) {
			super(searchCodePoint, replaceCodePoint);
			this.equals = equals;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCodePoint(str, findCodePoint, replaceCodePoint, equals, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointMatcher(findCodePoint, equals);
		}
	}

	static class ReplaceCodePointEqualCheckedImpl extends ReplaceCodePointEqualImpl {

		ReplaceCodePointEqualCheckedImpl(int findCodePoint, int replaceCodePoint, CodePointEqual equals) {
			super(findCodePoint, replaceCodePoint, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceCodePointChecked(str, findCodePoint, replaceCodePoint, equals, start, numReplace, checkReplace);
		}
	}

	// String

	abstract static class IReplaceStringImpl extends IStringReplacerImpl {
		String findStr;
		String replaceStr;

		IReplaceStringImpl(String findStr, String replaceStr) {
			this.findStr = findStr;
			this.replaceStr = replaceStr;
		}

		public IReplaceStringImpl replaceString(String findStr, String replaceStr) {
			this.findStr = findStr;
			this.replaceStr = replaceStr;
			return this;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new ConstStringReplaceAppender(replaceStr);
		}
	}

	static class ReplaceStringImpl extends IReplaceStringImpl {

		ReplaceStringImpl(String findStr, String replaceStr) {
			super(findStr, replaceStr);
		}

		@Override
		protected String doReplaceString(String str) {
			// Fast call: String.replace(CharSequence, CharSequence)
			return str.replace(findStr, replaceStr);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceString(str, findStr, replaceStr, start);
		}
	}

	static class ReplaceStringCheckedImpl extends IReplaceStringImpl {

		ReplaceStringCheckedImpl(String findStr, String replaceStr) {
			super(findStr, replaceStr);
		}

		@Override
		protected String doReplaceString(String str, int start) {
			return (String) CharSequenceTools.replaceStringChecked(str, findStr, replaceStr, start, numReplace, checkReplace);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceStringChecked(str, findStr, replaceStr, start, numReplace, checkReplace);
		}
	}

	static class ReplaceStringCharEqualImpl extends IReplaceStringImpl {
		CharEqual charEqual;

		ReplaceStringCharEqualImpl(String findStr, String replaceStr, CharEqual equals) {
			super(findStr, replaceStr);
			this.charEqual = equals;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceString(str, findStr, replaceStr, charEqual, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr, charEqual);
		}
	}

	static class ReplaceStringCharEqualCheckedImpl extends ReplaceStringCharEqualImpl {

		ReplaceStringCharEqualCheckedImpl(String findStr, String replaceStr, CharEqual equals) {
			super(findStr, replaceStr, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceStringChecked(str, findStr, replaceStr, charEqual, start, numReplace, checkReplace);
		}
	}

	static class ReplaceStringCodePointEqualImpl extends IReplaceStringImpl {
		CodePointEqual codePointEqual;

		ReplaceStringCodePointEqualImpl(String findStr, String replaceStr, CodePointEqual codePointEqual) {
			super(findStr, replaceStr);
			this.codePointEqual = codePointEqual;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceString(str, findStr, replaceStr, codePointEqual, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr, codePointEqual);
		}
	}

	static class ReplaceStringCodePointEqualCheckedImpl extends ReplaceStringCodePointEqualImpl {

		ReplaceStringCodePointEqualCheckedImpl(String findStr, String replaceStr, CodePointEqual codePointEqual) {
			super(findStr, replaceStr, codePointEqual);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.replaceStringChecked(str, findStr, replaceStr, codePointEqual, start, numReplace, checkReplace);
		}
	}

	// CharPredicate

	abstract static class IReplaceCharPredicateImpl extends IStringReplacerImpl {
		CharPredicate findPredicate;

		IReplaceCharPredicateImpl(CharPredicate finder) {
			this.findPredicate = finder;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharPredicateMatcher(findPredicate);
		}
	}

	static class ReplaceCharPredicateOperatorImpl extends IReplaceCharPredicateImpl {
		CharOperator operator;

		ReplaceCharPredicateOperatorImpl(CharPredicate finder, CharOperator operator) {
			super(finder);
			this.operator = operator;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCharPredicate(str, findPredicate, operator, start);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new CharOperatorReplaceAppender(operator);
		}
	}

	static class ReplaceCharPredicateOperatorCheckedImpl extends ReplaceCharPredicateOperatorImpl {

		ReplaceCharPredicateOperatorCheckedImpl(CharPredicate finder, CharOperator operator) {
			super(finder, operator);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCharPredicateChecked(str, findPredicate, operator, start, numReplace, checkReplace);
		}
	}

	static class ReplaceCharPredicateAppenderImpl extends IReplaceCharPredicateImpl {
		AppendCharReplacer appendReplacer;

		ReplaceCharPredicateAppenderImpl(CharPredicate finder, AppendCharReplacer appendReplacer) {
			super(finder);
			this.appendReplacer = appendReplacer;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCharPredicate(str, findPredicate, appendReplacer, start);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new CharAppendReplaceAppender(appendReplacer);
		}
	}

	static class ReplaceCharPredicateAppenderCheckedImpl extends ReplaceCharPredicateAppenderImpl {

		ReplaceCharPredicateAppenderCheckedImpl(CharPredicate finder, AppendCharReplacer appendReplacer) {
			super(finder, appendReplacer);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCharPredicateChecked(str, findPredicate, appendReplacer, start, numReplace, checkReplace);
		}
	}

	// CodePointPredicate

	static abstract class IReplaceCodePointPredicateImpl extends IStringReplacerImpl {
		IntPredicate findPredicate;

		IReplaceCodePointPredicateImpl(IntPredicate finder) {
			this.findPredicate = finder;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointPredicateMatcher(findPredicate);
		}
	}

	static class ReplaceCodePointPredicateOperatorImpl extends IReplaceCodePointPredicateImpl {
		IntUnaryOperator operator;

		ReplaceCodePointPredicateOperatorImpl(IntPredicate finder, IntUnaryOperator operator) {
			super(finder);
			this.operator = operator;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCodePointPredicate(str, findPredicate, operator, start);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new CodePointOperatorReplaceAppender(operator);
		}
	}

	static class ReplaceCodePointPredicateOperatorCheckedImpl extends ReplaceCodePointPredicateOperatorImpl {

		ReplaceCodePointPredicateOperatorCheckedImpl(IntPredicate finder, IntUnaryOperator operator) {
			super(finder, operator);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCodePointPredicateChecked(str, findPredicate, operator, start, numReplace, checkReplace);
		}
	}

	static class ReplaceCodePointPredicateAppenderImpl extends IReplaceCodePointPredicateImpl {
		AppendCodePointReplacer appendReplacer;

		ReplaceCodePointPredicateAppenderImpl(IntPredicate finder, AppendCodePointReplacer appendReplacer) {
			super(finder);
			this.appendReplacer = appendReplacer;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCodePointPredicate(str, findPredicate, appendReplacer, start);
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return new CodePointAppendReplaceAppender(appendReplacer);
		}
	}

	static class ReplaceCodePointPredicateAppenderCheckedImpl extends ReplaceCodePointPredicateAppenderImpl {

		ReplaceCodePointPredicateAppenderCheckedImpl(IntPredicate finder, AppendCodePointReplacer appendReplacer) {
			super(finder, appendReplacer);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doReplaceCodePointPredicateChecked(str, findPredicate, appendReplacer, start, numReplace, checkReplace);
		}
	}

	//

	abstract static class IReplaceMatcherImpl extends StringRemoverReplacerImpl implements StringReplacer, StringRemover {

		IStringMatcher matcher;
		IStringReplaceAppender replacer;

		IReplaceMatcherImpl(IStringMatcher matcher, IStringReplaceAppender replacer) {
			this.matcher = matcher;
			this.replacer = replacer;
		}

		@Override
		public CharSequence replace(CharSequence str, int start) {
			return transform(str);
		}

		@Override
		public CharSequence remove(CharSequence str, int start) {
			return transform(str);
		}

		@Override
		public String apply(String input) {
			// Ok as both remove() and replace() implement the same behavior
			return StringReplacer.super.apply(input);
		}

		@Override
		public void applyInline(IString input) {
			// Ok as both remove() and replace() implement the same behavior
			StringReplacer.super.applyInline(input);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return matcher;
		}

		@Override
		protected IStringReplaceAppender getAsIStringReplaceAppender() {
			return replacer;
		}
	}

	public static class RemoveMatcherImpl extends IReplaceMatcherImpl {

		public RemoveMatcherImpl(IStringMatcher matcher) {
			super(matcher, null);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			StringBuilder buf = null;
			while (true) {
				IMatch match = matcher.find(str, start);
				if (match == null) {
					break;
				}

				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, match.getStart());
				start = match.getEnd();
			}

			if (buf == null) {
				return str;
			}
			buf.append(str, start, str.length());
			return buf.toString();
		}
	}

	public static class RemoveFixedLenMatcherImpl extends IReplaceMatcherImpl {

		IStringFixedLenMatcher matcher;

		public RemoveFixedLenMatcherImpl(IStringFixedLenMatcher matcher) {
			super(matcher, null);
			this.matcher = matcher;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			// Performance improvements compared to doReplace_OLD:
			// - if matcher.find() is used, the allocation of the returned IMatch cannot be optimized away (until Java 25)
			// - if we do not check for any match first, but allocate the StringBuilder conditionally, it uses more memory...

			int matchStart = matcher.indexOf(str, start);
			if (matchStart == -1) {
				return str;
			}

			int len = str.length();
			StringBuilder buf = new StringBuilder(len);
			while (matchStart != -1) {
				buf.append(str, start, matchStart);
				start = matchStart + matcher.getMatchLength();
				matchStart = matcher.indexOf(str, start);
			}
			buf.append(str, start, len);
			return buf.toString();
		}

		// TODO
		protected CharSequence doReplace_OLD(CharSequence str, int start) {
			StringBuilder buf = null;
			while (true) {
				IMatch match = matcher.find(str, start);
				if (match == null) {
					break;
				}

				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, match.getStart());
				start = match.getEnd();
			}

			if (buf == null) {
				return str;
			}
			buf.append(str, start, str.length());
			return buf.toString();
		}

	}

	public static class ReplaceMatcherImpl extends IReplaceMatcherImpl {

		public ReplaceMatcherImpl(IStringMatcher matcher, IStringReplaceAppender replacer) {
			super(matcher, replacer);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			IMatch match = matcher.find(str, start);
			if (match == null) {
				return str;
			}

			int len = str.length();
			StringBuilder buf = new StringBuilder(len);
			int num = 0;
			while (match != null) {
				buf.append(str, start, match.getStart());
				replacer.replace(num, match, buf);
				start = match.getEnd();
				num++;
				match = matcher.find(str, start);
			}

			buf.append(str, start, str.length());
			return buf.toString();
		}
	}

	public static class ReplaceMatcherCheckedImpl extends IReplaceMatcherImpl {

		public ReplaceMatcherCheckedImpl(IStringMatcher matcher, IStringReplaceAppender replacer) {
			super(matcher, replacer);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			StringBuilder buf = null;
			int num = 0;
			while (true) {
				IMatch match = matcher.find(str, start);
				if (match == null) {
					break;
				}

				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, match.getStart());
				replacer.replace(num, match, buf);
				start = match.getEnd();

				// BEGIN numReplace/checkReplace
				num++;
				if (!checkReplace) {
					if (num == numReplace) {
						break;
					}
				} else {
					if (num > numReplace && numReplace != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace
			}

			// BEGIN numReplace/checkReplace
			if (checkReplace) {
				if (numReplace == -1) {
					if (num == 0) {
						return null; // return null as no replacement has been made
					}
				} else {
					if (num != numReplace) {
						return null; // return null as the number of replacement was wrong
					}
				}
			}
			// END numReplace/checkReplace

			if (buf == null) {
				return str;
			}
			buf.append(str, start, str.length());
			return buf.toString();
		}
	}

	// IRetainMatcherImpl

	public static class RetainCharPredicateImpl extends IStringRemoverImpl {

		CharPredicate predicate;

		public RetainCharPredicateImpl(CharPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.retainCharPredicate(str, start, predicate);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharPredicateMatcher(predicate.negate());
		}
	}

	public static class RetainCodePointPredicateImpl extends IStringRemoverImpl {

		IntPredicate predicate;

		public RetainCodePointPredicateImpl(IntPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.retainCodePointPredicate(str, start, predicate);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointPredicateMatcher(predicate.negate());
		}
	}

	abstract static class IRetainMatcherImpl extends IReplaceMatcherImpl {

		IRetainMatcherImpl(IStringMatcher matcher) {
			super(matcher, null);
		}

	}

	public static class RetainMatcherImpl extends IRetainMatcherImpl {

		public RetainMatcherImpl(IStringMatcher matcher) {
			super(matcher);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			StringBuilder buf = null;
			boolean found = false;
			while (true) {
				IMatch match = matcher.find(str, start);
				if (match == null) {
					break;
				}
				found = true;

				if (buf == null) {
					if (match.getStart() != start) {
						buf = new StringBuilder();
					}
				}
				if (buf != null) {
					buf.append(str, match.getStart(), match.getEnd());
				}
				start = match.getEnd();
			}

			if (buf == null) {
				return (found) ? str : "";
			}
			return buf.toString();
		}
	}

	public static class RetainMatcherCheckedImpl extends IRetainMatcherImpl {

		public RetainMatcherCheckedImpl(IStringMatcher matcher) {
			super(matcher);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			StringBuilder buf = null;
			int len = str.length();
			int num = 0;
			while (true) {
				IMatch match = matcher.find(str, start);
				if (match == null) {
					break;
				} else if (buf == null && match.getStart() == start && match.getEnd() == len) {
					return str;
				}

				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, match.getStart(), match.getEnd());
				start = match.getEnd();

				// BEGIN numReplace/checkReplace
				num++;
				if (!checkReplace) {
					if (num == numReplace) {
						break;
					}
				} else {
					if (num > numReplace && numReplace != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace
			}

			// BEGIN numReplace/checkReplace
			if (checkReplace) {
				if (numReplace == -1) {
					if (num == 0) {
						return null; // return null as no replacement has been made
					}
				} else {
					if (num != numReplace) {
						return null; // return null as the number of replacement was wrong
					}
				}
			}
			// END numReplace/checkReplace

			// FIXME handle case of no change
			if (buf == null) {
				return "";
			}
			return buf.toString();
		}
	}

}
