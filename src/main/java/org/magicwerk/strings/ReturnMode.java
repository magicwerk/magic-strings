package org.magicwerk.strings;

/**
 * Enumeration {@link ReturnMode} defines the return value of an attempted string transformation if applying it resulted in no change.
 */
public enum ReturnMode {
	/** Return input string unchanged if applying the transformation resulted in no change */
	RETURN_UNCHANGED,
	/** Return null value applying the transformation resulted in no change */
	RETURN_NULL,
	/** Throw an exception if applying the transformation resulted in no change */
	THROW_EXCEPTION
}