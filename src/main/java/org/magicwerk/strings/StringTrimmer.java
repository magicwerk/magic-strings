package org.magicwerk.strings;

import java.util.function.UnaryOperator;

import org.magicwerk.strings.BuilderHelper.BuilderFinderBase;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilder;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.function.MultiPredicate.Mode;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CheckTools.Check;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.AnchoredStringMatcher;
import org.magicwerk.strings.matcher.IStringMatcher;
import org.magicwerk.strings.matcher.RepeatedStringMatcher;

/**
 * Class {@link StringTrimmer} removes prefix/suffix at the head/tail of a string.
 */
public interface StringTrimmer extends IStringTransformer {

	public enum TrimMode {
		HEAD(true, false),
		TAIL(false, true),
		HEAD_TAIL(true, true);

		boolean head;
		boolean tail;

		TrimMode(boolean head, boolean tail) {
			this.head = head;
			this.tail = tail;
		}

		public boolean isHead() {
			return head;
		}

		public boolean isTail() {
			return tail;
		}
	}

	public enum CollapseMode {
		HEAD(true, false, false),
		TAIL(false, false, true),
		HEAD_TAIL(true, false, true),
		BODY(false, true, false),
		HEAD_BODY_TAIL(true, true, true),
		HEAD_BODY(true, true, false),
		BODY_TAIL(false, true, true);

		boolean head;
		boolean body;
		boolean tail;

		CollapseMode(boolean head, boolean body, boolean tail) {
			this.head = head;
			this.body = body;
			this.tail = tail;
		}

		public boolean isHead() {
			return head;
		}

		public boolean isBody() {
			return body;
		}

		public boolean isTail() {
			return tail;
		}
	}

	// IStringTransformer

	/**
	 * This method calls {@link #trim}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default String apply(String input) {
		return trim(input);
	}

	/**
	 * This method calls {@link #trimInline}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	default void applyInline(IString input) {
		trimInline(input);
	}

	//

	/** Remove specified characters from string */
	default String trim(String str) {
		return (String) trim((CharSequence) str);
	}

	/** Remove specified characters from string */
	CharSequence trim(CharSequence str);

	/** Remove specified characters from string (operation changes passed mutable string) */
	void trimInline(IString str);

	//

	/** Build {@link StringTrimmer} with specified builder function */
	public static StringTrimmer build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link StringTrimmer}.
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to create instances of {@link StringTrimmer}.
	 */
	public static class Builder extends BuilderFinderBase<Builder> implements IStringTransformerBuilder {

		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;
		TrimMode trimMode;
		CollapseMode collapseMode;
		String collapseStr;

		public Builder setTrimMode(TrimMode location) {
			this.trimMode = location;
			return this;
		}

		public Builder setCollapseMode(CollapseMode collapseMode) {
			this.collapseMode = collapseMode;
			return this;
		}

		public Builder setCollapseString(String collapseStr) {
			this.collapseStr = collapseStr;
			return this;
		}

		public Builder setCollapseChar(char collapseChar) {
			this.collapseStr = String.valueOf(collapseChar);
			return this;
		}

		@Override
		public Builder setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return this;
		}

		/**
		 * Build an instance of {@link StringTrimmer} with the specified configuration.
		 */
		@Override
		public StringTrimmer build() {
			Check.forNotNull(Mode.ANY).withMessage("At least one of trimMode/collapseMode must be set").check(trimMode, collapseMode);
			CheckTools.check(isDefined(), "charPredicate must be set");
			if (collapseStr != null) {
				CheckTools.checkNonNull(collapseMode, "collapseString needs collapseMode to be set");
			} else {
				CheckTools.check(collapseMode == null, "collapseString must be set if collapseMode to be set");
			}

			StringTrimmerImpl sr;
			CharPredicate cp = getAsCharPredicate();
			if (collapseMode == null) {
				if (cp != null) {
					if (cp == CharPredicates.whitespace) {
						sr = getStringTrimmerCharPredicateWhitespaceImpl(cp);
					} else {
						sr = getStringTrimmerCharPredicateImpl(cp);
					}
				} else {
					sr = getStringTrimmerMatcherImpl();
				}
			} else {
				StringTrimmerCollapseImpl st;
				if (cp != null) {
					if (collapseStr.length() == 1 && trimMode == TrimMode.HEAD_TAIL && collapseMode == CollapseMode.BODY) {
						StringTrimmerCollapseCharPredicateCharImpl su = new StringTrimmerCollapseCharPredicateCharImpl();
						su.trimPredicate = cp;
						su.collapseChar = collapseStr.charAt(0);
						st = su;
					} else {
						StringTrimmerCollapseCharPredicateImpl su = new StringTrimmerCollapseCharPredicateImpl();
						su.trimPredicate = cp;
						st = su;
					}
				} else {
					StringTrimmerCollapseMatcherImpl su = new StringTrimmerCollapseMatcherImpl();
					IStringMatcher sm = getAsStringMatcher();
					RepeatedStringMatcher rsm = RepeatedStringMatcher.of(sm, false);
					su.matcher = rsm;
					su.matcherHead = AnchoredStringMatcher.of(rsm, 0);
					su.matcherTail = AnchoredStringMatcher.of(rsm, -1);
					st = su;
				}
				st.trimMode = trimMode;
				st.collapseMode = collapseMode;
				st.collapseStr = ("".equals(collapseStr)) ? null : collapseStr;
				sr = st;
			}
			sr.returnMode = returnMode;
			return sr;
		}

		StringTrimmerCharPredicateImpl getStringTrimmerCharPredicateImpl(CharPredicate cp) {
			StringTrimmerCharPredicateImpl sr;
			switch (trimMode) {
			case HEAD:
				sr = new StringTrimmerCharPredicateHeadImpl();
				break;
			case TAIL:
				sr = new StringTrimmerCharPredicateTailImpl();
				break;
			case HEAD_TAIL:
				sr = new StringTrimmerCharPredicateHeadTailImpl();
				break;
			default:
				throw new AssertionError();
			}

			sr.trimPredicate = cp;
			return sr;
		}

		StringTrimmerCharPredicateImpl getStringTrimmerCharPredicateWhitespaceImpl(CharPredicate cp) {
			StringTrimmerCharPredicateImpl sr;
			switch (trimMode) {
			case HEAD:
				sr = new StringTrimmerCharPredicateWhitespaceHeadImpl();
				break;
			case TAIL:
				sr = new StringTrimmerCharPredicateWhitespaceTailImpl();
				break;
			case HEAD_TAIL:
				sr = new StringTrimmerCharPredicateWhitespaceHeadTailImpl();
				break;
			default:
				throw new AssertionError();
			}

			sr.trimPredicate = cp;
			return sr;
		}

		StringTrimmerMatcherImpl getStringTrimmerMatcherImpl() {
			StringTrimmerMatcherImpl sr;
			switch (trimMode) {
			case HEAD:
				sr = new StringTrimmerMatcherHeadImpl();
				break;
			case TAIL:
				sr = new StringTrimmerMatcherTailImpl();
				break;
			case HEAD_TAIL:
				sr = new StringTrimmerMatcherHeadTailImpl();
				break;
			default:
				throw new AssertionError();
			}

			IStringMatcher sm = getAsStringMatcher();
			RepeatedStringMatcher rsm = RepeatedStringMatcher.of(sm, false);
			sr.matcherHead = AnchoredStringMatcher.of(rsm, 0);
			sr.matcherTail = AnchoredStringMatcher.of(rsm, -1);
			return sr;
		}
	}

	// Implementation

	abstract static class StringTrimmerImpl extends StringTransformerImpl implements StringTrimmer {

		static final TrimApplier trimApplier = new TrimApplier();
		static final TrimInlineApplier trimInlineApplier = new TrimInlineApplier();

		// StringTrimmer

		@Override
		public CharSequence trim(CharSequence str) {
			return transform(str);
		}

		@Override
		public void trimInline(IString str) {
			transformInline(str);
		}

		// StringTransformer

		@Override
		public CharSequence doTransform(CharSequence str) {
			return doTrim(trimApplier, str);
		}

		@Override
		public IString doTransformInline(IString str) {
			return (IString) doTrim(trimInlineApplier, str);
		}

		abstract <T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str);
	}

	//

	abstract static class StringTrimmerCharPredicateImpl extends StringTrimmerImpl {

		CharPredicate trimPredicate;
	}

	static class StringTrimmerCharPredicateHeadImpl extends StringTrimmerCharPredicateImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimHead(worker, str, trimPredicate);
		}

		<T extends CharSequence> CharSequence doTrimHead(ITrimApplier<T> worker, T str, CharPredicate cp) {
			int end = str.length();
			int start = CharSequenceTools.doSkipHead(str, cp, end);
			if (start != 0) {
				return worker.change(str, start, end);
			} else {
				return null;
			}
		}
	}

	static class StringTrimmerCharPredicateTailImpl extends StringTrimmerCharPredicateImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimTail(worker, str, trimPredicate);
		}

		<T extends CharSequence> CharSequence doTrimTail(ITrimApplier<T> worker, T str, CharPredicate cp) {
			int len = str.length();
			int end = CharSequenceTools.doSkipTail(str, cp, len);
			if (end != len) {
				return worker.change(str, 0, end);
			} else {
				return null;
			}
		}
	}

	static class StringTrimmerCharPredicateHeadTailImpl extends StringTrimmerCharPredicateImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimHeadTail(worker, str, trimPredicate);
		}

		<T extends CharSequence> CharSequence doTrimHeadTail(ITrimApplier<T> worker, T str, CharPredicate cp) {
			int len = str.length();
			int end = len;
			int start = CharSequenceTools.doSkipHead(str, cp, len);
			if (start != end) {
				end = CharSequenceTools.doSkipTail(str, cp, len);
			}

			if (start != 0 || end != len) {
				return worker.change(str, start, end);
			} else {
				return null;
			}
		}

	}

	static class StringTrimmerCharPredicateWhitespaceHeadImpl extends StringTrimmerCharPredicateHeadImpl {
		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimHead(worker, str, BuilderHelper.trimmerPredicateWhitespace);
		}
	}

	static class StringTrimmerCharPredicateWhitespaceTailImpl extends StringTrimmerCharPredicateTailImpl {
		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimTail(worker, str, BuilderHelper.trimmerPredicateWhitespace);
		}
	}

	static class StringTrimmerCharPredicateWhitespaceHeadTailImpl extends StringTrimmerCharPredicateHeadTailImpl {
		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return doTrimHeadTail(worker, str, BuilderHelper.trimmerPredicateWhitespace);
		}
	}

	//

	abstract static class StringTrimmerMatcherImpl extends StringTrimmerImpl {

		AnchoredStringMatcher matcherHead;
		AnchoredStringMatcher matcherTail;
	}

	static class StringTrimmerMatcherHeadImpl extends StringTrimmerMatcherImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			int start = matcherHead.indexOfEnd(str);
			if (start != -1) {
				return worker.change(str, start, str.length());
			} else {
				return null; // nothing to trim
			}
		}
	}

	static class StringTrimmerMatcherTailImpl extends StringTrimmerMatcherImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			int end = matcherTail.indexOfEndingAt(str, str.length());
			if (end != -1) {
				return worker.change(str, 0, end);
			} else {
				return null; // nothing to trim
			}
		}
	}

	static class StringTrimmerMatcherHeadTailImpl extends StringTrimmerMatcherImpl {

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			int len = str.length();
			int start = matcherHead.indexOfEnd(str);
			int end;
			if (start == len) {
				end = start; // everything trimmed, empty string remains
			} else {
				if (start == -1) {
					start = 0;
				}
				end = matcherTail.indexOfEndingAt(str, len);
				if (end == -1) {
					end = len;
				}
			}

			if (start != 0 || end != len) {
				return worker.change(str, start, end);
			} else {
				return null;
			}
		}
	}

	//

	abstract static class StringTrimmerCollapseImpl extends StringTrimmerImpl {

		TrimMode trimMode;
		CollapseMode collapseMode;
		String collapseStr;

	}

	static class StringTrimmerCollapseCharPredicateImpl extends StringTrimmerCollapseImpl {

		CharPredicate trimPredicate;

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return collapse(str, trimPredicate, trimMode, collapseMode, collapseStr);
		}

		static CharSequence collapse(CharSequence str, CharPredicate trimPredicate, TrimMode trimLoc, CollapseMode collapseLoc, String collapseStr) {
			boolean collapseHead = (collapseLoc != null && collapseLoc.isHead());
			boolean handleHead = collapseHead || (trimLoc == TrimMode.HEAD || trimLoc == TrimMode.HEAD_TAIL);
			boolean collapseTail = (collapseLoc != null && collapseLoc.isTail());
			boolean handleTail = collapseHead || (trimLoc == TrimMode.TAIL || trimLoc == TrimMode.HEAD_TAIL);
			boolean collapseBody = (collapseLoc != null && collapseLoc.isBody());
			int len = str.length();

			int start = 0;
			boolean trimHead = false;
			if (handleHead) {
				start = CharSequenceTools.doSkipHead(str, trimPredicate, len);
				trimHead = (start > 0);
				collapseHead = collapseHead && (collapseStr != null);
			} else {
				collapseHead = false;
			}

			int end = len;
			boolean trimTail = false;
			if (handleTail) {
				end = CharSequenceTools.doSkipTail(str, trimPredicate, len);
				trimTail = (end < len);
				collapseTail = collapseTail && (collapseStr != null);
			} else {
				collapseTail = false;
			}

			if (start == end) {
				return "";
			}
			if (!collapseBody) {
				return str.subSequence(start, end).toString();
			}

			// Collapse
			StringBuilder buf = (trimHead || trimTail || collapseHead || collapseTail) ? new StringBuilder(len) : null;
			if (collapseHead) {
				buf.append(collapseStr);
			}
			CharPredicate keepPredicate = trimPredicate.negate();
			while (true) {
				// Handle range until next trim char
				int pos = CharSequenceTools.doSkipBody(str, keepPredicate, start, end);
				if (pos == end) {
					if (buf != null) {
						buf.append(str, start, pos);
					}
					break;
				}

				if (start < pos) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(str, start, pos);
				}

				// Handle range of trim chars
				start = CharSequenceTools.doSkipBody(str, trimPredicate, pos, end);
				if (collapseStr != null) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(collapseStr);
				}
			}

			if (collapseTail) {
				buf.append(collapseStr);
			}
			return (buf != null) ? buf.toString() : null;
		}
	}

	static class StringTrimmerCollapseCharPredicateCharImpl extends StringTrimmerCollapseImpl {

		CharPredicate trimPredicate;
		char collapseChar;

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return collapse(str);
		}

		CharSequence collapse(CharSequence str) {
			int len = str.length();
			int src = 0;
			char[] chars = null;

			// Head
			while (src < len && trimPredicate.test(str.charAt(src))) {
				src++;
			}
			int start = src;
			if (start > 0) {
				chars = CharSequenceTools.toCharArray(str);
			}

			// Body / Tail
			int tgt = src;
			int end = -1;
			while (src < len) {
				char c = str.charAt(src);
				if (!trimPredicate.test(c)) {
					// copy character
					if (chars != null) {
						chars[tgt++] = c;
					}
					src++;

				} else {
					// Skip collapse range
					end = tgt;
					int num = 1;
					src++;
					while (src < len) {
						c = str.charAt(src);
						if (!trimPredicate.test(c)) {
							break;
						}
						num++;
						src++;
					}
					if (src < len) {
						// body
						if (num > 1 || c != collapseChar) {
							// collapse
							if (chars == null) {
								chars = CharSequenceTools.toCharArray(str);
							}
							chars[tgt++] = collapseChar;
						}
					}
				}
			}
			return (chars != null) ? new String(chars, start, end - start) : null;
		}
	}

	static class StringTrimmerCollapseMatcherImpl extends StringTrimmerCollapseImpl {

		IStringMatcher matcher;
		AnchoredStringMatcher matcherHead;
		AnchoredStringMatcher matcherTail;

		@Override
		<T extends CharSequence> CharSequence doTrim(ITrimApplier<T> worker, T str) {
			return collapse(str);
		}

		CharSequence collapse(CharSequence str) {
			boolean collapseHead = (collapseMode != null && collapseMode.isHead());
			boolean handleHead = collapseHead || (trimMode == TrimMode.HEAD || trimMode == TrimMode.HEAD_TAIL);
			boolean collapseTail = (collapseMode != null && collapseMode.isTail());
			boolean handleTail = collapseHead || (trimMode == TrimMode.TAIL || trimMode == TrimMode.HEAD_TAIL);
			boolean collapseBody = (collapseMode != null && collapseMode.isBody());
			int len = str.length();

			int start = 0;
			boolean trimHead = false;
			if (handleHead) {
				start = matcherHead.indexOfEnd(str);
				if (start == -1) {
					start = 0;
				} else {
					trimHead = true;
				}
				collapseHead = collapseHead && (collapseStr != null);
			} else {
				collapseHead = false;
			}

			int end = len;
			boolean trimTail = false;
			if (handleTail) {
				end = matcherTail.indexOfEndingAt(str, len);
				if (end == -1) {
					end = len;
				} else {
					trimTail = true;
				}
				collapseTail = collapseTail && (collapseStr != null);
			} else {
				collapseTail = false;
			}

			if (start == end) {
				return "";
			}
			if (!collapseBody) {
				return str.subSequence(start, end).toString();
			}

			// Collapse
			StringBuilder buf = (trimHead || trimTail || collapseHead || collapseTail) ? new StringBuilder(len) : null;
			if (collapseHead) {
				buf.append(collapseStr);
			}

			while (true) {
				IMatch match = matcher.find(str, start);
				int pos = (match != null) ? match.getStart() : -1;
				if (pos == -1 || pos == end) {
					if (start < end) {
						if (buf == null) {
							buf = new StringBuilder(len);
						}
						buf.append(str, start, end);
					}
					break;
				}

				if (start < pos) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(str, start, pos);
				}
				if (collapseStr != null) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(collapseStr);
				}
				start = match.getEnd();
			}

			if (collapseTail) {
				buf.append(collapseStr);
			}
			return (buf != null) ? buf.toString() : null;
		}

	}

	//

	interface ITrimApplier<T extends CharSequence> {
		CharSequence change(T str, int start, int end);
	}

	static class TrimApplier implements ITrimApplier<CharSequence> {

		@Override
		public CharSequence change(CharSequence str, int start, int end) {
			return str.subSequence(start, end);
		}
	}

	static class TrimInlineApplier implements ITrimApplier<IString> {

		@Override
		public CharSequence change(IString str, int start, int end) {
			str.retain(start, end - start);
			return str;
		}
	}
}