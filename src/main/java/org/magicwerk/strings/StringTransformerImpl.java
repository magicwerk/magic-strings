package org.magicwerk.strings;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link StringTransformerImpl} provides the base implementation for all classes implementing {@link IStringTransformer}.
 * It handles the following special situations in an uniform way: <br>
 * - null value as input: transformer will not be applied, will immediately return null <br>
 * - transformer is applied, but no change is done: returns value or throws exception depending on configured {@link ReturnMode} <br>
 */
public abstract class StringTransformerImpl {

	final boolean checkNull = false;
	final boolean handleNotChanged = false;

	ReturnMode returnMode;

	public CharSequence transform(CharSequence str) {
		if (str == null) {
			return null;
		}
		CharSequence s = doTransform(str);
		if (s != null) {
			return s;
		}
		return handleNotChanged(str, returnMode);
	}

	public String transform(String str) {
		if (str == null) {
			return null;
		}
		String s = doTransformString(str);
		if (s != null) {
			return s;
		}
		return (String) handleNotChanged(str, returnMode);
	}

	public void transformInline(IString str) {
		CharSequence s = doTransformInline(str);
		if (s != null) {
			return;
		}
		handleNotChangedInline(str, returnMode);
	}

	//

	/** Transform {@link CharSequence}. If no transformation can be done for the input, null must be returned */
	abstract CharSequence doTransform(CharSequence str);

	/** Transform {@link String}. If no transformation can be done for the input, null must be returned */
	String doTransformString(String str) {
		return (String) doTransform(str);
	}

	/** Transform {@link IString} in place. If no transformation can be done for the input, null must be returned */
	abstract IString doTransformInline(IString str);

	//

	/** Method which handles the case that the input string passed has not been changed by the transformation. */
	CharSequence handleNotChanged(CharSequence str, ReturnMode returnMode) {
		switch (returnMode) {
		case RETURN_UNCHANGED:
			return str;
		case RETURN_NULL:
			return null;
		case THROW_EXCEPTION:
			throw CheckTools.error("String not changed");
		default:
			throw new AssertionError();
		}
	}

	/** Method which handles the case that the input string passed has not been changed by the transformation. */
	void handleNotChangedInline(IString str, ReturnMode returnMode) {
		switch (returnMode) {
		case RETURN_UNCHANGED:
			break;
		case RETURN_NULL:
			str.clear();
			return;
		case THROW_EXCEPTION:
			throw CheckTools.error("String not changed");
		default:
			throw new AssertionError();
		}
	}

}