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
import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.BuilderReplacerBase;
import org.magicwerk.strings.StringReplacer.IReplaceMatcherImpl;
import org.magicwerk.strings.StringReplacer.RemoveFixedLenMatcherImpl;
import org.magicwerk.strings.StringReplacer.RemoveMatcherImpl;
import org.magicwerk.strings.StringReplacer.ReplaceMatcherCheckedImpl;
import org.magicwerk.strings.StringReplacer.RetainCharPredicateImpl;
import org.magicwerk.strings.StringReplacer.RetainCodePointPredicateImpl;
import org.magicwerk.strings.StringReplacer.RetainMatcherCheckedImpl;
import org.magicwerk.strings.StringReplacer.RetainMatcherImpl;
import org.magicwerk.strings.StringReplacer.StringRemoverReplacerImpl;
import org.magicwerk.strings.StringReplacerAppender.EmptyStringReplaceAppender;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.matcher.IStringFixedLenMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;

/**
 * Class {@link StringRemover} contains several implementations similar to {@link StringBuilder}.
 */
public interface StringRemover extends IStringTransformer {

	// Design TODO
	// In order to reduce memory consumption, it makes sense to accept CharSequence instead of String.
	// This however also makes it preferable to use CharSequence as return type, as it makes no sense
	// to create a String if the unchanged input (which can be a CharSequence) is returned.

	// IStringTransformer

	/**
	 * This method calls {@link #remove}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return remove(input);
	}

	/**
	 * This method calls {@link #removeInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		removeInline(input);
	}

	//

	default String remove(String str) {
		return remove(str, 0);
	}

	default String remove(String str, int start) {
		return (String) remove((CharSequence) str, start);
	}

	default CharSequence remove(CharSequence str) {
		return remove(str, 0);
	}

	CharSequence remove(CharSequence str, int start);

	default void removeInline(IString str) {
		removeInline(str, 0);
	}

	default void removeInline(IString str, int start) {
		// FIXME
		CharSequence result = remove(str, start);
		if (result == str || result == null) {
			return;
		}

		str.initArray(result.toString().toCharArray()); // FIXME
	}

	//

	/** Build {@link StringRemover} with specified builder function */
	public static StringRemover build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringRemover}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link StringRemover}.
	 */
	public static class Builder extends BuilderReplacerBase<Builder> {

		static final EmptyStringReplaceAppender emptyStringReplaceAppender = new EmptyStringReplaceAppender();

		/** True for retain instead of remove, i.e. the specified part will be kept */
		boolean retain;

		/** Setter for {@link #retain} */
		public Builder setRetain(boolean retain) {
			this.retain = retain;
			return this;
		}

		@Override
		void check() {
			CheckTools.check(isDefined(), "Builder has no search definition");
		}

		/**
		 * Build an instance of {@link StringRemover} with the specified configuration.
		 * The created instance must be of the specified type or an exception is thrown.
		 */
		public <T extends StringRemover> T build(Class<T> clazz) {
			@SuppressWarnings("unchecked")
			T finder = (T) build();
			return finder;
		}

		/**
		 * Build an instance of {@link StringRemover} with the specified configuration.
		 */
		@Override
		public StringRemover build() {
			check();

			StringRemoverReplacerImpl replacerImpl = null;
			boolean checked = true;
			if (!checkReplace) {
				if (numReplace == 0 || isEmptyString() || isEmptyAnyChar()) {
					replacerImpl = new NoChangeRemover(getAsStringMatcher());
				} else if (numReplace == -1) {
					checked = false;
				}
			}

			// Specialized implementations if retain is false
			if (replacerImpl == null) {
				if (!retain) {
					CharEqual charEqual = getCharEqual();
					CodePointEqual codePointEqual = getCodePointEqual();

					if (character != null) {
						// Character
						if (!checked) {
							if (charEqual == null) {
								replacerImpl = new RemoveCharImpl(character);
							} else {
								replacerImpl = new RemoveCharEqualImpl(character, charEqual);
							}
						} else {
							if (charEqual == null) {
								replacerImpl = new RemoveCharCheckedImpl(character);
							} else {
								replacerImpl = new RemoveCharEqualCheckedImpl(character, charEqual);
							}
						}

					} else if (codePoint != null) {
						// CodePoint
						if (!checked) {
							if (codePointEqual == null) {
								replacerImpl = new RemoveCodePointImpl(codePoint);
							} else {
								replacerImpl = new RemoveCodePointEqualImpl(codePoint, codePointEqual);
							}
						} else {
							if (codePointEqual == null) {
								replacerImpl = new RemoveCodePointCheckedImpl(codePoint);
							} else {
								replacerImpl = new RemoveCodePointEqualCheckedImpl(codePoint, codePointEqual);
							}
						}

					} else if (string != null) {
						// String
						if (charEqual == null && codePointEqual == null) {
							if (!checked) {
								replacerImpl = new RemoveStringImpl(string);
							} else {
								replacerImpl = new RemoveStringCheckedImpl(string);
							}
						} else if (charEqual != null) {
							if (!checked) {
								replacerImpl = new RemoveStringCharEqualImpl(string, charEqual);
							} else {
								replacerImpl = new RemoveStringCharEqualCheckedImpl(string, charEqual);
							}
						} else {
							assert codePointEqual != null;
							if (!checked) {
								replacerImpl = new RemoveStringCodePointEqualImpl(string, codePointEqual);
							} else {
								replacerImpl = new RemoveStringCodePointEqualCheckedImpl(string, codePointEqual);
							}
						}
					}

					if (replacerImpl == null) {
						// CharPredicate
						CharPredicate cp = getAsCharPredicate();
						if (cp != null) {
							if (!checked) {
								replacerImpl = new RemoveCharPredicateImpl(cp);
							} else {
								replacerImpl = new RemoveCharPredicateCheckedImpl(cp);
							}
						}
					}

					if (replacerImpl == null) {
						// CodePointPredicate
						IntPredicate cp = getAsCodePointPredicate();
						if (cp != null) {
							if (!checked) {
								replacerImpl = new RemoveCodePointPredicateImpl(cp);
							} else {
								replacerImpl = new RemoveCodePointPredicateCheckedImpl(cp);
							}
						}
					}
				}
			}

			// Specialized implementations if retain is true
			if (replacerImpl == null) {
				if (retain) {
					CharPredicate cp = getAsCharPredicate();
					if (cp != null) {
						replacerImpl = new RetainCharPredicateImpl(cp);
					} else {
						IntPredicate cpp = getAsCodePointPredicate();
						if (cpp != null) {
							replacerImpl = new RetainCodePointPredicateImpl(cpp);
						}
					}
				}
			}

			// Generic implementations
			if (replacerImpl == null) {
				IStringMatcher findMatcher = this.matcher;
				if (findMatcher == null) {
					findMatcher = getAsStringMatcher();
				}

				if (!retain) {
					if (!checked) {
						if (findMatcher instanceof IStringFixedLenMatcher) {
							replacerImpl = new RemoveFixedLenMatcherImpl((IStringFixedLenMatcher) findMatcher);
						} else {
							replacerImpl = new RemoveMatcherImpl(findMatcher);
						}
					} else {
						replacerImpl = new ReplaceMatcherCheckedImpl(findMatcher, emptyStringReplaceAppender);
					}
				} else {
					if (!checked) {
						replacerImpl = new RetainMatcherImpl(findMatcher);
					} else {
						replacerImpl = new RetainMatcherCheckedImpl(findMatcher);
					}
				}
			}

			replacerImpl.returnMode = returnMode;
			if (checked) {
				replacerImpl.checkReplace = checkReplace;
				replacerImpl.numReplace = numReplace;
			}

			if (replacerImpl instanceof IReplaceMatcherImpl) {
				return ((IReplaceMatcherImpl) replacerImpl);
			} else {
				return ((IStringRemoverImpl) replacerImpl);
			}
		}

	}

	static abstract class IStringRemoverImpl extends StringRemoverReplacerImpl implements StringRemover {

		@Override
		public CharSequence remove(CharSequence str, int start) {
			return transform(str);
		}
	}

	// No change

	static class NoChangeRemover extends IStringRemoverImpl {
		IStringMatcher matcher;

		NoChangeRemover(IStringMatcher matcher) {
			this.matcher = matcher;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return str;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return matcher;
		}
	}

	// Char

	static class RemoveCharImpl extends IStringRemoverImpl {
		char find;

		RemoveCharImpl(char findChar) {
			this.find = findChar;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeChar(str, find, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharMatcher(find);
		}
	}

	static class RemoveCharCheckedImpl extends RemoveCharImpl {

		RemoveCharCheckedImpl(char findChar) {
			super(findChar);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCharChecked(str, find, start, numReplace, checkReplace);
		}
	}

	static class RemoveCharEqualImpl extends RemoveCharImpl {
		CharEqual equal;

		RemoveCharEqualImpl(char findChar, CharEqual equals) {
			super(findChar);
			this.equal = equals;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeChar(str, find, equal, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharMatcher(find, equal);
		}
	}

	static class RemoveCharEqualCheckedImpl extends RemoveCharEqualImpl {

		RemoveCharEqualCheckedImpl(char findChar, CharEqual equals) {
			super(findChar, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCharChecked(str, find, equal, start, numReplace, checkReplace);
		}
	}

	// CharPredicate

	static abstract class IRemoveCharPredicateImpl extends IStringRemoverImpl {
		CharPredicate predicate;

		IRemoveCharPredicateImpl(CharPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCharPredicateMatcher(predicate);
		}
	}

	static class RemoveCharPredicateImpl extends IRemoveCharPredicateImpl {

		RemoveCharPredicateImpl(CharPredicate predicate) {
			super(predicate);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCharPredicate(str, predicate, start);
		}
	}

	static class RemoveCharPredicateCheckedImpl extends RemoveCharPredicateImpl {

		RemoveCharPredicateCheckedImpl(CharPredicate predicate) {
			super(predicate);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCharPredicateChecked(str, predicate, start, numReplace, checkReplace);
		}
	}

	// CodePointPredicate

	static abstract class IRemoveCodePointPredicateImpl extends IStringRemoverImpl {
		IntPredicate predicate;

		IRemoveCodePointPredicateImpl(IntPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointPredicateMatcher(predicate);
		}
	}

	static class RemoveCodePointPredicateImpl extends IRemoveCodePointPredicateImpl {

		RemoveCodePointPredicateImpl(IntPredicate predicate) {
			super(predicate);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCodePointPredicate(str, predicate, start);
		}
	}

	static class RemoveCodePointPredicateCheckedImpl extends RemoveCodePointPredicateImpl {

		RemoveCodePointPredicateCheckedImpl(IntPredicate predicate) {
			super(predicate);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doRemoveCodePointPredicateChecked(str, predicate, start, numReplace, checkReplace);
		}
	}

	// Code point

	static class RemoveCodePointImpl extends IStringRemoverImpl {
		int find;

		RemoveCodePointImpl(int findCodePoint) {
			this.find = findCodePoint;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.doRemoveCodePoint(str, find, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getCodePointMatcher(find);
		}
	}

	static class RemoveCodePointCheckedImpl extends RemoveCodePointImpl {

		RemoveCodePointCheckedImpl(int findCodePoint) {
			super(findCodePoint);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCodePointChecked(str, find, start, numReplace, checkReplace);
		}
	}

	static class RemoveCodePointEqualImpl extends RemoveCodePointImpl {
		CodePointEqual equal;

		RemoveCodePointEqualImpl(int searchCodePoint, CodePointEqual equal) {
			super(searchCodePoint);
			this.equal = equal;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCodePoint(str, find, equal, start);
		}
	}

	static class RemoveCodePointEqualCheckedImpl extends RemoveCodePointEqualImpl {

		RemoveCodePointEqualCheckedImpl(int findCodePoint, CodePointEqual equals) {
			super(findCodePoint, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeCodePointChecked(str, find, equal, start, numReplace, checkReplace);
		}
	}

	// String

	static abstract class IRemoveStringImpl extends IStringRemoverImpl {
		String findStr;

		IRemoveStringImpl(String findStr) {
			this.findStr = findStr;
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr);
		}
	}

	static class RemoveStringImpl extends IRemoveStringImpl {
		RemoveStringImpl(String findStr) {
			super(findStr);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeStringChecked(str, findStr, start, numReplace, checkReplace);
		}
	}

	static class RemoveStringCheckedImpl extends IRemoveStringImpl {
		RemoveStringCheckedImpl(String findStr) {
			super(findStr);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeStringChecked(str, findStr, start, numReplace, checkReplace);
		}
	}

	static class RemoveStringCharEqualImpl extends IRemoveStringImpl {
		CharEqual equal;

		RemoveStringCharEqualImpl(String findStr, CharEqual equal) {
			super(findStr);
			this.equal = equal;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeString(str, findStr, equal, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr, equal);
		}
	}

	static class RemoveStringCodePointEqualImpl extends IRemoveStringImpl {
		CodePointEqual equal;

		RemoveStringCodePointEqualImpl(String findStr, CodePointEqual equal) {
			super(findStr);
			this.equal = equal;
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeString(str, findStr, equal, start);
		}

		@Override
		protected IStringMatcher getAsIStringMatcher() {
			return BuilderHelper.getStringMatcher(findStr, equal);
		}
	}

	static class RemoveStringCharEqualCheckedImpl extends RemoveStringCharEqualImpl {

		RemoveStringCharEqualCheckedImpl(String findStr, CharEqual equals) {
			super(findStr, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeStringChecked(str, findStr, equal, start, numReplace, checkReplace);
		}
	}

	static class RemoveStringCodePointEqualCheckedImpl extends RemoveStringCodePointEqualImpl {

		RemoveStringCodePointEqualCheckedImpl(String findStr, CodePointEqual equals) {
			super(findStr, equals);
		}

		@Override
		protected CharSequence doReplace(CharSequence str, int start) {
			return CharSequenceTools.removeStringChecked(str, findStr, equal, start, numReplace, checkReplace);
		}
	}

}
