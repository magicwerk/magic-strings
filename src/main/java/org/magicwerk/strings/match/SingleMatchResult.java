/*
 * Copyright 2010 by Thomas Mauch
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
 * $Id: RegexTools.java 4489 2019-08-27 09:14:30Z origo $
 */
package org.magicwerk.strings.match;

import java.util.regex.MatchResult;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link SingleMatchResult} implements a {@link MatchResult} representing a single match.
 */
public class SingleMatchResult implements MatchResult {

	MatchResult match;
	int group;

	public SingleMatchResult(MatchResult match, int group) {
		CheckTools.checkNonNull(match, "match");
		CheckTools.check(group <= match.groupCount(), "group {} is greater than group count in {}", group, match);

		this.match = match;
		this.group = group;
	}

	@Override
	public int start() {
		return match.start(group);
	}

	@Override
	public int end() {
		return match.end(group);
	}

	@Override
	public String group() {
		return match.group(group);
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
		CheckTools.check(group == 0, "invalid group");
	}

}
