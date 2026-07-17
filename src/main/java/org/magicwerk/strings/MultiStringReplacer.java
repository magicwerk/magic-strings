package org.magicwerk.strings;

import java.util.function.UnaryOperator;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.strings.BuilderHelper.IStringTransformerBuilderBase;
import org.magicwerk.strings.StringReplacer.IStringReplacerImpl;
import org.magicwerk.strings.StringReplacerAppender.IStringReplaceAppender;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.CollectionTools;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.IStringMatcher;

/**
 * Class {@link MultiStringReplacer} allows multiple {@link StringReplacer} to work in parallel on text.
 * It guarantees that a single part of the text is only handled by one replacer.
 * Behavior is therefore differently from calling String.replace() repeatedly. 
 */
public class MultiStringReplacer extends IStringReplacerImpl {

	/** Build {@link MultiStringReplacer} with specified builder function */
	public static MultiStringReplacer build(UnaryOperator<Builder> builder) {
		return builder.apply(new Builder()).build();
	}

	/**
	 * Get {@link Builder} to create a {@link MultiStringReplacer}.
	 */
	public static MultiStringReplacer.Builder builder() {
		return new Builder();
	}

	/**
	 * Class {@link Builder} allows to build instances of {@link MultiStringReplacer}.
	 */
	public static class Builder implements IStringTransformerBuilderBase {
		/** List of replacers to be executed in parallel */
		IList<StringReplacer> replacers;
		boolean preferLong = true;
		/** Number of replace operations to execute, -1 for unlimited */
		int numReplace = -1;
		/** If true, there may not be more matches than specified in {@link #numReplace} */
		boolean checkReplace = false;
		/** Determine what to return if no replacement is made */
		ReturnMode returnMode = ReturnMode.RETURN_UNCHANGED;

		/** Setter for {@link #replacers} */
		public Builder setReplacers(StringReplacer... replacers) {
			this.replacers = GapList.create(replacers);
			return this;
		}

		/** Setter for {@link #numReplace} */
		public Builder setNumReplace(int numReplace) {
			this.numReplace = numReplace;
			return this;
		}

		/** Setter for {@link #preferLong} */
		public Builder setPreferLong(boolean preferLong) {
			this.preferLong = preferLong;
			return this;
		}

		/** Setter for {@link #checkReplace} */
		public Builder setCheckReplace(boolean checkReplace) {
			this.checkReplace = checkReplace;
			return this;
		}

		/** Setter for {@link #returnMode} */
		@Override
		public Builder setReturnMode(ReturnMode returnMode) {
			this.returnMode = returnMode;
			return this;
		}

		/** Build an instance of {@link MultiStringReplacer} with the specified configuration */
		public MultiStringReplacer build() {
			check();

			MultiStringReplacer mr = new MultiStringReplacer();
			mr.numReplace = numReplace;
			mr.checkReplace = checkReplace;
			mr.returnMode = returnMode;
			mr.preferLong = preferLong;

			mr.replacers = GapList.create();
			replacers.forEach(r -> add(mr.replacers, r));
			return mr;
		}

		void check() {
			CheckTools.check(CollectionTools.size(replacers) > 0, "No replacers specified");
		}

		void add(IList<IReplaceMatcherImpl> replacers, StringReplacer replacer) {
			if (replacer instanceof MultiStringReplacer) {
				replacers.addAll(((MultiStringReplacer) replacer).replacers);
			} else {
				replacers.add(getIReplaceMatcherImpl(replacer));
			}
		}

		IReplaceMatcherImpl getIReplaceMatcherImpl(StringReplacer replacer) {
			if (replacer instanceof StringRemoverReplacerImpl) {
				StringRemoverReplacerImpl re = (StringRemoverReplacerImpl) replacer;
				IStringMatcher m = re.getAsIStringMatcher();
				IStringReplaceAppender r = re.getAsIStringReplaceAppender();
				return new ReplaceMatcherImpl(m, r);
			} else { // FIXME
				throw CheckTools.error("Unsupported class: {}", replacer.getClass());
			}
		}
	}

	IList<IReplaceMatcherImpl> replacers;
	boolean preferLong;

	@Override
	protected CharSequence doReplace(CharSequence str, int start) {
		StringBuilder buf = null;
		int num = 0;
		while (true) {
			IStringReplaceAppender bestReplacer = null;
			IMatch bestMatch = null;
			for (IReplaceMatcherImpl replacer : replacers) {
				IMatch match = replacer.matcher.find(str, start);
				if (isMatchBetter(match, bestMatch)) {
					bestReplacer = replacer.replacer;
					bestMatch = match;
				}
			}
			if (bestMatch == null) {
				break;
			}

			if (buf == null) {
				buf = new StringBuilder();
			}
			buf.append(str, start, bestMatch.getStart());
			bestReplacer.replace(num, bestMatch, buf);
			start = bestMatch.getEnd();
			num++;
		}

		if (buf != null) {
			buf.append(str, start, str.length());
			return buf.toString();
		} else {
			return str;
		}
	}

	boolean isMatchBetter(IMatch match, IMatch bestMatch) {
		if (match == null) {
			return false;
		}
		if (preferLong) {
			return (bestMatch == null
					|| match.getStart() < bestMatch.getStart()
					|| (match.getStart() == bestMatch.getStart() && match.getLength() > bestMatch.getLength()));
		} else {
			return (bestMatch == null || match.getStart() < bestMatch.getStart());
		}
	}

	@Override
	protected IStringMatcher getAsIStringMatcher() {
		return null;
	}

	@Override
	protected IStringReplaceAppender getAsIStringReplaceAppender() {
		return null;
	}

}