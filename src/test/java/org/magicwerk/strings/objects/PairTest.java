package org.magicwerk.strings.objects;

import org.magictest.client.Trace;
import org.magicwerk.strings.objects.Pair;

/**
 * Test of class Pair.
 *
 * @author Thomas Mauch
 * @version $Id: PairTest.java 2747 2015-03-01 23:51:00Z origo $
 */
public class PairTest {
	
	@Trace(traceMethod = "/.+/")
	public static void testPair() {
		Pair<Integer> pi = new Pair<>(1, 2);
		pi.getItem0();
		pi.getItem1();
	}
}
