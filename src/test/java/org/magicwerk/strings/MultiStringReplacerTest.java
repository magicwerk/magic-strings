package org.magicwerk.strings;

import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.strings.MultiStringReplacer;
import org.magicwerk.strings.StringReplacer;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Test of class {@link MultiStringReplacer}.
 */
public class MultiStringReplacerTest {

	public static void main(String[] args) {
		new MultiStringReplacerTest().test();
	}

	void test() {
		testReplace();
	}

	@Trace
	public void testReplace() {
		{
			// Show that StringMultiReplacer behaves different than chaining of several replacers
			StringReplacer r1 = StringReplacer.builder().replaceString("a", "Ab").build();
			StringReplacer r2 = StringReplacer.builder().replaceString("b", "Ba").build();
			MultiStringReplacer smr = MultiStringReplacer.builder().setReplacers(r1, r2).build();

			String s = smr.replace("ab");
			String s0 = s.replace("a", "Ab").replace("b", "Ba");
			String s1 = s.replace("b", "Ba").replace("a", "Ab");
			CheckTools.check(!s0.equals(s));
			CheckTools.check(!s1.equals(s));
		}
		{
			// Show effect of preferLong
			StringReplacer r1 = StringReplacer.builder().replaceString("aa", "(short)").build();
			StringReplacer r2 = StringReplacer.builder().replaceString("aaaaa", "(long)").build();
			MultiStringReplacer smr1 = MultiStringReplacer.builder().setReplacers(r1, r2).build();
			MultiStringReplacer smr2 = MultiStringReplacer.builder().setReplacers(r1, r2).setPreferLong(false).build();
			for (StringReplacer smr : GapList.immutable(smr1, smr2)) {
				smr.replace("[aaa]");
				smr.replace("[aaaa]");
				smr.replace("[aaaaa]");
				smr.replace("[aaaaaa]");
				smr.replace("[aaaaaaa]");
			}
		}
		{
			StringReplacer r1 = StringReplacer.builder().replaceRegex("/\\*.*?\\*/", "COMMENT").build();
			StringReplacer r2 = StringReplacer.builder().replaceRegex("'.*?'", "STRING").build();
			StringReplacer r3 = StringReplacer.builder().replaceAnyChar("ab", "AB").build();
			MultiStringReplacer smr = MultiStringReplacer.builder().setReplacers(r1, r2, r3).build();

			smr.replace("abc /* comment */ def 'string' ghi");
			smr.replace("abc /* comment 'string' */ def ");
		}
	}
}
