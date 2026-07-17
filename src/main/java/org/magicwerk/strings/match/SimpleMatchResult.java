/**
 * 
 */
package org.magicwerk.strings.match;

import java.util.regex.MatchResult;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link SimpleMatchResult} allows to create a {@link MatchResult} out of a string.
 */
public class SimpleMatchResult implements MatchResult {

	CharSequence str;
	int start;
	int end;

	public SimpleMatchResult(CharSequence str, int start, int end) {
		this.str = str;
		this.start = start;
		this.end = end;
	}

	@Override
	public int start() {
		return start;
	}

	@Override
	public int end() {
		return end;
	}

	@Override
	public String group() {
		return str.subSequence(start, end).toString();
	}

	@Override
	public int start(int group) {
		checkGroup(group);
		return start();
	}

	@Override
	public int end(int group) {
		checkGroup(group);
		return end();
	}

	@Override
	public String group(int group) {
		checkGroup(group);
		return group();
	}

	@Override
	public int groupCount() {
		return 0;
	}

	void checkGroup(int group) {
		CheckTools.check(group == 0);
	}

}