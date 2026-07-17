package org.magicwerk.strings.chars;

import org.magictest.client.Trace;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.platform.logback.LogbackTools;
import org.magicwerk.brownies.test.TestValues;
import org.magicwerk.strings.GeneralStringTest.StringJmhBenchmark;
import org.magicwerk.strings.chars.CharPredicates.ManyChars;
import org.magicwerk.strings.chars.CharPredicates.OneChar;
import org.magicwerk.strings.chars.CharPredicates.RangeChars;
import org.magicwerk.strings.chars.CharPredicates.ThreeChars;
import org.magicwerk.strings.chars.CharPredicates.TwoChars;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link CharPredicates}.
 */
public class CharPredicatesTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new CharPredicatesTest().run();
	}

	void run() {
		//testGetFirstMatch();

		//new CharPredicatesTestJmh().test();
		new CharPredicateNegateTestJmh().test();
	}

	@Trace
	public void testGetFirstMatch() {
		CharPredicate cp = CharPredicates.oneOf("abc");
		CharPredicates.getFirstMatch(cp, 'x', false);
		CharPredicates.getFirstMatch(cp, 'x', true);
	}

	public static class CharPredicateNegateTestJmh extends StringJmhBenchmark {
		{
			setRunVerify(false);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char ch1 = 'a';
			char ch2 = 'b';
			CharPredicate cp = new OneChar(ch1);
			CharPredicate cp1 = negate(cp);
			CharPredicate cp2 = new NegatedCharPredicate(cp);
			CyclicSource<Character> source = new CyclicSource<>(ch1, ch2);
		}

		@Benchmark
		public Object testPositive(MyState state) {
			char c = state.source.next();
			return state.cp.test(c);
		}

		@Benchmark
		public Object testNegatedGeneric(MyState state) {
			char c = state.source.next();
			return state.cp1.test(c);
		}

		@Benchmark
		public Object testNegatedClass(MyState state) {
			char c = state.source.next();
			return state.cp2.test(c);
		}

		static CharPredicate negate(CharPredicate cp) {
			return (t) -> !cp.test(t);
		}

		static class NegatedCharPredicate implements CharPredicate {

			CharPredicate original;

			NegatedCharPredicate(CharPredicate original) {
				this.original = original;
			}

			@Override
			public boolean test(char c) {
				return !original.test(c);
			}
		}

	}

	/** 
	 * Test various implementations of CharPredicate.
	 */
	public static class CharPredicatesTestJmh extends StringJmhBenchmark {

		public CharPredicatesTestJmh() {
			//setJavaVersions(JavaVersion.JAVA_21);
		}

		@State(Scope.Benchmark)
		public static class MyState {
			char ch1 = 'a';
			char ch2 = 'b';
			char ch3 = 'c';
			char chX = '-';
			CharPredicate cp1 = new OneChar(ch1);
			CharPredicate cp1x = new ManyChars("" + ch1);
			CharPredicate cp2 = new TwoChars(ch1, ch2);
			CharPredicate cp3 = new ThreeChars(ch1, ch2, ch3);
			CharPredicate cp2x = new ManyChars("" + ch1 + ch2);
			CharPredicate cp3x = new ManyChars("" + ch1 + ch2 + ch3);
			CharPredicate cp3xx = new ManyChars(TestValues.getString('a', 100));
			CharPredicate cpDigitR = new RangeChars('0', '9');
			CharPredicate cpDigitX = new ManyChars(TestValues.getString('0', '9'));
			CharPredicate cpAlphaR = new RangeChars('a', 'z');
			CharPredicate cpAlphaX = new ManyChars(TestValues.getString('a', 'z'));
			CyclicSource<Character> source = new CyclicSource<>(ch1, ch2, ch3, chX);
		}

		@Benchmark
		public Object testChar1_OneChar(MyState state) {
			char c = state.source.next();
			return state.cp1.test(c);
		}

		@Benchmark
		public Object testChar1_ManyChars(MyState state) {
			char c = state.source.next();
			return state.cp1x.test(c);
		}

		@Benchmark
		public Object testChar2_TwoChars(MyState state) {
			char c = state.source.next();
			return state.cp2.test(c);
		}

		@Benchmark
		public Object testChar2_ManyChars(MyState state) {
			char c = state.source.next();
			return state.cp2x.test(c);
		}

		@Benchmark
		public Object testChar3_ThreeChars(MyState state) {
			char c = state.source.next();
			return state.cp3.test(c);
		}

		@Benchmark
		public Object testChar3_ManyChars(MyState state) {
			char c = state.source.next();
			return state.cp3x.test(c);
		}

		@Benchmark
		public Object testCharDigit_RangeChars(MyState state) {
			char c = state.source.next();
			return state.cpDigitR.test(c);
		}

		@Benchmark
		public Object testCharDigit_ManyChars(MyState state) {
			char c = state.source.next();
			return state.cpDigitX.test(c);
		}

		@Benchmark
		public Object testCharAlpha_RangeChars(MyState state) {
			char c = state.source.next();
			return state.cpAlphaR.test(c);
		}

		@Benchmark
		public Object testCharAlpha_ManyChars(MyState state) {
			char c = state.source.next();
			return state.cpAlphaX.test(c);
		}
	}

}
