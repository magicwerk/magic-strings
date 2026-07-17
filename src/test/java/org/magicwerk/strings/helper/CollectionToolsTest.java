package org.magicwerk.strings.helper;

import static org.magicwerk.strings.helper.CollectionTools.createHashMap;
import static org.magicwerk.strings.helper.CollectionTools.createLinkedHashMap;
import static org.magicwerk.strings.helper.CollectionTools.createTreeMap;

import java.util.Comparator;
import java.util.Map;

import org.magictest.client.Format;
import org.magictest.client.Trace;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;
import org.magicwerk.collections.helper.NaturalComparator;
import org.magicwerk.strings.helper.CollectionTools;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link CollectionTools}.
 */
public class CollectionToolsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CollectionToolsTest().run();
	}

	void run() {
	}

	@Trace(traceMethod = "/checkCollectionOrder|indexOfWrongOrder|getWrongOrderInfo|getOrderMode/")
	public void testCollectionOrder() {
		IList<Integer> ordered = GapList.create(1, 2, 3, 4);
		IList<Integer> unordered = GapList.create(1, 3, 2, 4);
		IList<Integer> reversed = GapList.create(4, 3, 2, 1);
		IList<Integer> equal = GapList.create(1, 1, 1, 1);
		Comparator<Integer> comp = NaturalComparator.getComparator(Integer.class);

		CollectionTools.getOrderMode(ordered, comp);
		CollectionTools.getOrderMode(unordered, comp);
		CollectionTools.getOrderMode(reversed, comp);
		CollectionTools.getOrderMode(equal, comp);
	}

	@Trace(traceMethod = "/createHashMap|createTreeMap|createLinkedHashMap/", formats = { @Format(apply = Trace.RESULT, formatter = "formatMap") })
	public static void testCreateMaps() {
		createHashMap("k1", 1, "k2", 2);
		createTreeMap("k2", 2, "k1", 1);
		createLinkedHashMap("k2", 2, "k1", 1);

		// Error
		createHashMap("k1", 1, "k2");
		createHashMap("k1", 1, 2, 3);
		createHashMap("k1", 1, "k2", "2");
	}

	static String formatMap(Map<?, ?> map) {
		if (map == null) {
			return "null";
		}
		return map.getClass().getSimpleName() + ": " + map.toString();
	}

}
