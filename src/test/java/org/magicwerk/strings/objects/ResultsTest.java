package org.magicwerk.strings.objects;

import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.objects.MultiResult;
import org.magicwerk.brownies.core.objects.Result;

/**
 * Test of result classes {@link Result} and {@link MultiResult}.
 */
public class ResultsTest {

	public static void main(String[] args) {
		new ResultsTest().run();
	}

	void run() {
		testMultiResult();
	}

	@Trace(traceClass = "Result", traceMethod = "/.*/")
	public void testResult() {
		{
			Result<String> r = Result.value("ok");
			r.getValue();
			r.getValueOr(null);
			r.getError();
			r.getErrorOrNull();
		}
		{
			Result<String> r = Result.error(new RuntimeException("err"));
			r.getValue();
			r.getValueOr(null);
			r.getError();
			r.getErrorOrNull();
		}
	}

	@Trace(traceClass = "Result", traceMethod = "equals")
	public void testResultEquals() {
		{
			Result<String> r1 = Result.value("ok");
			Result<String> r2 = Result.value("ok");
			r1.equals(r2);
		}
		{
			// Comparing 2 exceptions typically returns false
			Result<String> r1 = Result.error(new RuntimeException("err"));
			Result<String> r2 = Result.error(new RuntimeException("err"));
			r1.equals(r2);
		}
	}

	@Trace(traceClass = "MultiResult", traceMethod = "/.*/")
	public void testMultiResult() {
		Result<String> resultOk = Result.value("ok");
		Result<String> resultErr1 = Result.error(new RuntimeException("err1"));
		Result<String> resultErr2 = Result.error(new RuntimeException("err2"));

		{
			MultiResult<String> mr = new MultiResult<String>(GapList.create(resultOk));
			mr.getValue();
			mr.getValueOr(null);
			mr.getError();
			mr.getErrorOrNull();
			mr.getErrors();
		}
		{
			MultiResult<String> mr = new MultiResult<String>(GapList.create(resultErr1, resultErr2, resultOk));
			mr.getValue();
			mr.getValueOr(null);
			mr.getError();
			mr.getErrorOrNull();
			mr.getErrors();
		}
		{
			MultiResult<String> mr = new MultiResult<String>(GapList.create(resultErr1, resultErr2));
			mr.getValue();
			mr.getValueOr(null);
			mr.getError();
			mr.getErrorOrNull();
			mr.getErrors();
		}
	}
}
