package org.magicwerk.strings.objects;

import org.magictest.client.Capture;
import org.magicwerk.brownies.core.print.PrintTools2;
import org.magicwerk.brownies.test.BrowniesJavaEnv;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.collections.GapList;
import org.magicwerk.collections.helper.MutableInt;
import org.magicwerk.strings.objects.Array;
import org.magicwerk.strings.objects.Single;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Test of class {@link Array} and related classes.
 */
public class ArrayTest {

	public static void main(String[] args) {
		new ArrayTest().run();
	}

	void run() {
		MutableIntJmhTest.test();
	}

	@Capture
	public static void testArray() {
		Array<String> as = new Array<String>(2);
		as.set(0, "a");
		as.set(1, "b");
		System.out.println(as);

		for (int i = 0; i < as.size(); i++) {
			String s = as.get(i);
			System.out.println(s);
		}

		Object[] arr = as.toArray();
		System.out.println(PrintTools2.print(arr));
	}

	public static class MutableIntJmhTest {

		//		Java 11
		//		ArrayTest.MutableIntJmhTest.testArray                          thrpt    5    5660597.607 �    170264.916   ops/s
		//		ArrayTest.MutableIntJmhTest.testArray:gc.alloc.rate            thrpt    5        129.526 �         3.898  MB/sec
		//		ArrayTest.MutableIntJmhTest.testMutableInt                     thrpt    5  273322558.822 � 104351456.378   ops/s
		//		ArrayTest.MutableIntJmhTest.testMutableInt:gc.alloc.rate       thrpt    5       4169.417 �      1592.193  MB/sec
		//		ArrayTest.MutableIntJmhTest.testSingle                         thrpt    5     276061.576 �     27207.032   ops/s
		//		ArrayTest.MutableIntJmhTest.testSingle:gc.alloc.rate           thrpt    5       3680.453 �       363.184  MB/sec
		//
		//		Java 17 / 21
		//		ArrayTest.MutableIntJmhTest.testArray                          thrpt    5  2082413337.545 � 293792517.038   ops/s
		//		ArrayTest.MutableIntJmhTest.testArray:gc.alloc.rate            thrpt    5           0.001 �         0.001  MB/sec
		//		ArrayTest.MutableIntJmhTest.testMutableInt                     thrpt    5  2089475770.094 � 259585875.039   ops/s
		//		ArrayTest.MutableIntJmhTest.testMutableInt:gc.alloc.rate       thrpt    5           0.001 �         0.001  MB/sec
		//		ArrayTest.MutableIntJmhTest.testSingle                         thrpt    5      289180.725 �     24467.979   ops/s
		//		ArrayTest.MutableIntJmhTest.testSingle:gc.alloc.rate           thrpt    5        3855.475 �       326.031  MB/sec

		static void test() {
			Options opts = new Options().includeClass(MutableIntJmhTest.class);
			opts.setUseGcProfiler(true);
			opts.setJdkCommands(GapList.create(BrowniesJavaEnv.JdkCommands11, BrowniesJavaEnv.JdkCommands17, BrowniesJavaEnv.JdkCommands21));

			JmhRunner runner = new JmhRunner();
			//runner.setFastMode(true);
			//runner.verifyJmhMethods(opts, 10);
			runner.runJmh(opts);
		}

		static final int NUM = 1000;

		@Benchmark
		public int testArray() {
			int[] val = new int[1];
			consume(NUM, () -> val[0]++);
			return val[0];
		}

		@Benchmark
		public int testSingle() {
			Single<Integer> val = new Single<>(0);
			consume(NUM, () -> val.set(val.get() + 1));
			return val.get();
		}

		@Benchmark
		public int testMutableInt() {
			MutableInt val = new MutableInt();
			consume(NUM, () -> val.increment());
			return val.intValue();
		}

		void consume(int num, Runnable run) {
			for (int i = 0; i < num; i++) {
				run.run();
			}
		}

	}

}
