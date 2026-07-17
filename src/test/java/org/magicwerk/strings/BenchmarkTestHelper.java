package org.magicwerk.strings;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Function;

import org.jdom2.Attribute;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.LogTools;
import org.magicwerk.brownies.core.MathTools2;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.core.collections.GridSelection;
import org.magicwerk.brownies.core.compare.Comparators.LookupComparator;
import org.magicwerk.brownies.core.function.IFormatter;
import org.magicwerk.brownies.core.print.PrintTools2;
import org.magicwerk.brownies.core.stat.CountTools.CountOrder;
import org.magicwerk.brownies.core.stat.KeyCount;
import org.magicwerk.brownies.core.stat.KeyCountCollection;
import org.magicwerk.brownies.core.time.TimeBuilders;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.validator.DecimalFormatter;
import org.magicwerk.brownies.core.values.ITableModel;
import org.magicwerk.brownies.core.values.Record;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.core.values.TableTools;
import org.magicwerk.brownies.core.values.typediff.ColMappingOptions;
import org.magicwerk.brownies.html.CssStyle;
import org.magicwerk.brownies.html.HtmlInline;
import org.magicwerk.brownies.html.HtmlReport;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.HtmlTools;
import org.magicwerk.brownies.html.StyleResource;
import org.magicwerk.brownies.html.content.HtmlFormatters;
import org.magicwerk.brownies.html.content.HtmlFormatters.ConditionalFormatter;
import org.magicwerk.brownies.html.content.HtmlFormatters.Context;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestExecution;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestMethodPerformance;
import org.magicwerk.brownies.tools.dev.jvm.JmhBenchmarkCreator.TestPerformance;
import org.magicwerk.strings.StringSplitter;
import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.function.Predicates;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.FuncTools;
import org.slf4j.Logger;

/**
 * Class {@link BenchmarkTestHelper} supports visualizing results of benchmark runs.
 */
public class BenchmarkTestHelper {

	static final Logger LOG = LogTools.getLogger();

	/** Class {@link ImplementationPeformance} shows performance and memory consumption of an implementation */
	static class ImplementationPeformance {
		String implementation;
		double performance;
		double memory;
	}

	static class RelativePercentageFormatter<T extends Number> implements IFormatter<T> {

		int scale = 1;

		@Override
		public String format(Number obj) {
			double val = obj.doubleValue();
			String str = TypeTools.format(val, scale, false);
			if (val > 0) {
				return "+" + str + "%";
			} else {
				return str + "%";
			}
		}
	}

	static final Type<Double> performanceType = Type.builder(Double.class).with(new DecimalFormatter<Double>("#,###")).toType();
	static final Type<Double> memoryType = Type.builder(Double.class).with(new DecimalFormatter<Double>("#,###.#")).toType();
	static final Type<Double> percentType = Type.builder(Double.class).with(new RelativePercentageFormatter<Double>()).toType();

	static final StringSplitter testMethodSplitter = StringSplitter.build(b -> b.setFindChar('_'));

	/** Name of implementation which will come first and be the base of 100% for calculating the change */
	String baseImplementation;

	public BenchmarkTestHelper setBaseImplementation(String baseImplementation) {
		this.baseImplementation = baseImplementation;
		return this;
	}

	void showReport(IList<TestExecution> tes) {
		IList<String> impls = getImplementationOrder(tes);
		Table tab = new Table();
		ImplementationPeformance stat = new ImplementationPeformance();
		for (TestExecution te : tes) {
			Record rec = addBenchmark(te, stat, impls);
			TableTools.addRecord(tab, rec, ColMappingOptions.union());
		}

		LOG.info("{}", tab);
		showReport(tab, stat);
	}

	IList<String> getImplementationOrder(IList<TestExecution> tes) {
		boolean hasBaseImpl = false;
		KeyCountCollection<String> coll = new KeyCountCollection<>();
		for (TestExecution te : tes) {
			for (TestPerformance tp : te.performances) {
				for (int i = 0; i < tp.testMethodsPerformance.size(); i++) {
					String impl = te.testMethods.get(i).getName();
					if (impl.equals(baseImplementation)) {
						hasBaseImpl = true;
					} else {
						coll.add(impl);
					}
				}
			}
		}
		IList<KeyCount<String>> list = coll.getAll(CountOrder.COUNT_DESC);
		IList<String> impls = list.map(KeyCount::getKey);
		if (hasBaseImpl) {
			impls.addFirst(baseImplementation);
		}
		return impls;
	}

	String getTestMethod(Class<?> c) {
		return testMethodSplitter.getFirst(c.getSimpleName());
	}

	Record addBenchmark(TestExecution te, ImplementationPeformance stat, IList<String> impls) {
		// JMH reports no timestamp when the benchmark was run
		ZonedDateTime now = TimeBuilders.ZonedDateTimeBuilder.DEFAULT.now();

		LOG.info("{}", te);

		String method = getTestMethod(te.testClass);
		String benchmark = te.testClass.getSimpleName();
		String argument = PrintTools2.print(te.testMethodsParamValues);
		String result = PrintTools2.print(te.testMethodsResults);
		String time = now.toLocalDateTime().toString();

		LookupComparator<ImplementationPeformance, String> comp = new LookupComparator<>(ip -> ip.implementation, impls);

		CheckTools.check(te.performances.size() == 1); // TODO
		for (TestPerformance tp : te.performances) {
			String java = tp.version.toString();
			IList<ImplementationPeformance> ips = GapList.create();
			for (int i = 0; i < tp.testMethodsPerformance.size(); i++) {
				TestMethodPerformance tmp = tp.testMethodsPerformance.get(i);

				ImplementationPeformance ip = new ImplementationPeformance();
				ip.implementation = te.testMethods.get(i).getName();
				ip.performance = tmp.performance;
				ip.memory = getMemoryValue(tmp.memory);
				ips.add(ip);
			}

			Record rec = new Record();
			rec.addField("Method", Type.STRING_TYPE, method);
			rec.addField("Benchmark", Type.STRING_TYPE, benchmark);
			rec.addField("Argument", Type.STRING_TYPE, argument);
			rec.addField("Result", Type.STRING_TYPE, result);
			rec.addField("Time", Type.STRING_TYPE, time);
			rec.addField("Java", Type.STRING_TYPE, java);

			ips.sort(comp);

			ImplementationPeformance first = ips.get(0);
			rec.addField("Implementation", Type.STRING_TYPE, first.implementation);
			rec.addField("Performance", performanceType, first.performance);
			rec.addField("Memory", memoryType, first.memory);

			for (int i = 1; i < ips.size(); i++) {
				ImplementationPeformance ip = ips.get(i);
				double perfDiff = getPerformanceDiff(first.performance, ip.performance);
				double memDiff = getMemoryDiff(first.memory, ip.memory);
				stat.performance += perfDiff;
				stat.memory += memDiff;

				String suffix = " [" + i + "]";
				rec.addField("Implementation" + suffix, Type.STRING_TYPE, ip.implementation);
				rec.addField("Performance" + suffix, performanceType, ip.performance);
				rec.addField("PerfDiff" + suffix, percentType, perfDiff);
				rec.addField("Memory" + suffix, memoryType, ip.memory);
				rec.addField("MemDiff" + suffix, percentType, memDiff);
				rec.addField("Comment" + suffix, Type.STRING_TYPE, getComment(te, ip));
			}
			return rec;
		}
		return null;
	}

	String getComment(TestExecution te, ImplementationPeformance ip) {
		if (isMagicStrings(ip)) {
			if (isNoChanges(te)) {
				if (ip.memory > 0) {
					return "Test with no changes allocates memory";
				}
			}
		}
		return "";
	}

	boolean isNoChanges(TestExecution te) {
		return te.testClass.getSimpleName().contains("_NoChange");
	}

	boolean isMagicStrings(ImplementationPeformance ip) {
		return !ip.implementation.equals("testStringUtils");
	}

	double getMemoryValue(double actual) {
		return MathTools2.round(actual, 0);
	}

	double getPerformanceDiff(double base, double actual) {
		return (actual / base * 100.0) - 100.0;
	}

	/**
	 * Computes normalized percentage improvement for memory usage (bytes).
	 * Positive = improvement (uses less memory), negative = regression.
	 *
	 * Rules:
	 *  - old > 0, new >= 0: (old - new) / old * 100
	 *  - old == 0, new == 0: 0
	 *  - old == 0, new  > 0: -100 (conventional "worst regression")
	 *  - old  > 0, new == 0: +100 (perfect improvement)
	 */
	double getMemoryDiff(double base, double actual) {
		if (base == 0) {
			if (actual == 0) {
				return 0; // no change
			} else {
				return -100; // regression from nothing to something
			}
		} else {
			if (actual == 0) {
				return 100; // perfect improvement
			} else {
				return (base - actual) / base * 100;
			}
		}
	}

	void sor2t(IList<ImplementationPeformance> ips) {
		int first = ips.indexOfIf(i -> i.implementation.equals(baseImplementation));
		if (first == -1) {
			ips.sort(Comparator.comparing(i -> i.implementation));
		} else {
			ImplementationPeformance ip = ips.remove(first);
			ips.sort(Comparator.comparing(i -> i.implementation));
			ips.addFirst(ip);
		}
	}

	void showReport(ITableModel tab, ImplementationPeformance ip) {
		HtmlTableFormatter htf = new HtmlTableFormatter();
		htf.setActive(true);
		htf.setFixedHeader(true);
		formatTable(tab, htf);
		HtmlTable ht = htf.format(tab);

		HtmlInline title = HtmlTools.createP(TimeBuilders.LocalDateTimeBuilder.DEFAULT.format(LocalDateTime.now()));
		String str = StringFormatter.format("PerfDiff: {}, MemDiff: {}",
				percentType.format(ip.performance), percentType.format(ip.memory));
		HtmlInline text = HtmlTools.createP(str);

		HtmlReport report = new HtmlReport();
		report.add(StyleResource.INSTANCE);
		report.add(HtmlTableFormatter.getHtmlResources());
		report.add(title);
		report.add(text);
		report.add(ht);
		report.showHtml();
	}

	void formatTable(ITableModel tab, HtmlTableFormatter htf) {
		String colGood = "#ddffdd";
		String colNeutral = "#ddddff";
		String colBad = "#ffdddd";

		ConditionalFormatter cf = new ConditionalFormatter();
		Function<Context, Double> value = c -> FuncTools.nvl(c.getValue(), 0.0);
		cf.add(c -> value.apply(c) > 0, t -> getCssStyle(colGood));
		cf.add(c -> value.apply(c) < 0, t -> getCssStyle(colBad));
		cf.add(Predicates.allow(), t -> getCssStyle(colNeutral));

		int firstCol = 9;
		int numCols = 6; // impl, perf, perfDiff, mem, memDiff, comment
		HtmlFormatters hf = new HtmlFormatters();
		int col = firstCol;
		while (col < tab.getNumCols()) {
			hf.addFormatter(getColSelection(tab, col + 2), cf);
			hf.addFormatter(getColSelection(tab, col + 4), cf);
			col += numCols;
		}
		htf.setFormatters(hf);
	}

	GridSelection getColSelection(ITableModel tab, int col) {
		return GridSelection.Region(0, col, tab.getNumRows() - 1, col);
	}

	Attribute getCssStyle(String bgColor) {
		return new CssStyle().setBackgroundColor(bgColor).getAttribute();
	}

}
