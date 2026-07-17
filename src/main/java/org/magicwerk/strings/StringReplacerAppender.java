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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.CharSequenceTools.AppendCharReplacer;
import org.magicwerk.strings.CharSequenceTools.AppendCodePointReplacer;
import org.magicwerk.strings.chars.CharOperator;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.format.StringFormat;
import org.magicwerk.strings.function.TriConsumer;
import org.magicwerk.strings.helper.RegexTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.RegexMatch;

/**
 * Class {@link StringReplacerAppender} contains several implementations similar of {@link IStringReplaceAppender}.
 */
public class StringReplacerAppender {

	/**
	 * Interface {@link IStringReplaceAppender} replaces a match with a replacement which is directly added to the result buffer.
	 */
	public interface IStringReplaceAppender {
		void replace(int matchIndex, IMatch match, StringBuilder buf);
	}

	/**
	 * Class {@link EmptyStringReplaceAppender} replaces a matched string with an empty one, i.e. it removes it.
	 */
	public static class EmptyStringReplaceAppender implements IStringReplaceAppender {

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
		}
	}

	/**
	 * Class {@link ConstStringReplaceAppender} allow to replace a matched string with a constant string.
	 */
	public static class ConstStringReplaceAppender implements IStringReplaceAppender {

		CharSequence str;

		public ConstStringReplaceAppender(char c) {
			str = Character.toString(c);
		}

		public ConstStringReplaceAppender(int cp) {
			str = Character.toString(cp);
		}

		public ConstStringReplaceAppender(CharSequence str) {
			this.str = str;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			// TODO match is not needed - possible performance improvement?
			buf.append(str);
		}
	}

	public static class CharOperatorReplaceAppender implements IStringReplaceAppender {

		CharOperator operator;

		CharOperatorReplaceAppender(CharOperator operator) {
			this.operator = operator;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			char c = match.getString().charAt(0);
			c = operator.apply(c);
			buf.append(c);
		}
	}

	public static class CodePointOperatorReplaceAppender implements IStringReplaceAppender {

		IntUnaryOperator operator;

		CodePointOperatorReplaceAppender(IntUnaryOperator operator) {
			this.operator = operator;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			int cp = CodePointTools.firstCodePoint(match.getString());
			cp = operator.applyAsInt(cp);
			buf.appendCodePoint(cp);
		}
	}

	public static class CharAppendReplaceAppender implements IStringReplaceAppender {

		AppendCharReplacer replacer;

		CharAppendReplaceAppender(AppendCharReplacer replacer) {
			this.replacer = replacer;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			char c = match.getString().charAt(0);
			replacer.replaceAppend(c, buf);
		}
	}

	public static class CodePointAppendReplaceAppender implements IStringReplaceAppender {

		AppendCodePointReplacer replacer;

		CodePointAppendReplaceAppender(AppendCodePointReplacer replacer) {
			this.replacer = replacer;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			int cp = CodePointTools.firstCodePoint(match.getString());
			replacer.replaceAppend(cp, buf);
		}
	}

	public static class RegexMatchReplaceAppender implements IStringReplaceAppender {

		/** Message format to use, null if {@link #group}/{@link #groupValue} should be used */
		StringFormat format;
		/** Group which will be replaced by specified {@link #groupValue}, only if {@link #format} is null */
		int group;
		/** Value which will be used instead of specified {@link #group}, only if {@link #format} is null */
		String groupValue;

		/**
		 * @return format
		 */
		public StringFormat getFormat() {
			return format;
		}

		/**
		 * Set format to use.
		 * 
		 * @param format format 
		 */
		public RegexMatchReplaceAppender setFormat(StringFormat format) {
			this.format = format;
			this.group = -1;
			this.groupValue = null;
			return this;
		}

		/**
		 * Set format to use.
		 * The format "({0})" will enclose the complete match with parentheses.
		 * It the format is invalid, an exception is thrown.
		 * 
		 * @param format format (see {@link StringFormat})
		 */
		public RegexMatchReplaceAppender setFormat(String format) {
			this.format = new StringFormat(format);
			this.group = -1;
			this.groupValue = null;
			return this;
		}

		/**
		 * Set string to use for replacing the specified matching group.
		 * The string is used literally as provided.
		 */
		public RegexMatchReplaceAppender setFormatGroup(int group, String value) {
			this.group = group;
			this.groupValue = value;
			this.format = null;
			return this;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			RegexMatch rm = (RegexMatch) match;
			String str = apply(rm); // TODO do not create temporary string
			buf.append(str);
		}

		public String apply(RegexMatch rm) {
			if (format != null) {
				IList<String> strs = RegexTools.getMatchedGroups(rm.getMatchResult());
				return format.formatList(strs);
			} else {
				return RegexTools.set(rm.getMatchResult(), rm.getInput().toString(), group, groupValue, false);
			}
		}
	}

	/**
	 * Class {@link StringAppendReplaceAppender} allow to replace a matched string with a replacement string out of the matched string.
	 */
	public static class StringAppendReplaceAppender implements IStringReplaceAppender {
		/** Function which creates the replacement string out of the matched string */
		BiConsumer<StringBuilder, String> appender;

		/** Construct {@link StringTransformReplaceAppender} */
		public StringAppendReplaceAppender(BiConsumer<StringBuilder, String> appender) {
			this.appender = appender;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			String str = match.getString();
			appender.accept(buf, str);
		}
	}

	/**
	 * Class {@link StringAppendIndexReplaceAppender} allow to replace a matched string with a replacement string out of the matched string
	 * and the index of the match.
	 */
	public static class StringAppendIndexReplaceAppender implements IStringReplaceAppender {
		/** Function which creates the replacement string out of the matched string and the index of the match */
		TriConsumer<StringBuilder, String, Integer> appender;

		public StringAppendIndexReplaceAppender(TriConsumer<StringBuilder, String, Integer> appender) {
			this.appender = appender;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			String str = match.getString();
			appender.accept(buf, str, matchIndex);
		}
	}

	/**
	 * Class {@link StringTransformReplaceAppender} allow to replace a matched string with a 
	 * replacement string out of the matched string. <br>
	 * Note that this transformer is not optimal for performance as it creates a temporary string which is then added to the StringBuilder.
	 */
	public static class StringTransformReplaceAppender implements IStringReplaceAppender {
		/** Function which creates the replacement string out of the matched string */
		UnaryOperator<String> op;

		/** Construct {@link StringTransformReplaceAppender} */
		public StringTransformReplaceAppender(UnaryOperator<String> op) {
			this.op = op;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			String str = match.getString();
			str = op.apply(str);
			buf.append(str);
		}
	}

	/**
	 * Class {@link StringTransformIndexReplaceAppender} allow to replace a matched string with a 
	 * replacement string out of the matched string and the index of the match. <br>
	 * Note that this transformer is not optimal for performance as it creates a temporary string which is then added to the StringBuilder.
	 */
	public static class StringTransformIndexReplaceAppender implements IStringReplaceAppender {
		/** Function which creates the replacement string out of the matched string and the index of the match */
		BiFunction<Integer, String, String> op;

		public StringTransformIndexReplaceAppender(BiFunction<Integer, String, String> op) {
			this.op = op;
		}

		@Override
		public void replace(int matchIndex, IMatch match, StringBuilder buf) {
			String str = match.getString();
			str = op.apply(matchIndex, str);
			buf.append(str);
		}
	}

}
