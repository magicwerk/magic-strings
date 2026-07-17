package org.magicwerk.strings.matcher;

/**
 * Interface {@link IStringIgnoreCaseMatcher} offers an easy way to ignore case on matching.
 * If a matcher implements this interface, it must query {@link #isIgnoreCase} for each call to determine whether matching must 
 * respect or ignore case. As this decision is taken at runtime for every call, it has a small performance drawback.
 */
public interface IStringIgnoreCaseMatcher extends IStringMatcher {

	// TODO 
	// IStringMatcher getAsStringMatcher() 
	// IStringMatcher getAsStringMatcherIgnoreCase(IIgnoreCase ic)

	/** Method */
	boolean isIgnoreCase();
}
