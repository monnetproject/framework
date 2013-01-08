package eu.monnetproject.framework.test;

/**
 * Note: This code is copied from the Be Informed test framework (com.beinformed.product.platform.framework.testability).
 * This framework is likely to be open sourced. Therefore please do not make changes to the interfaces since it will
 * make a transition towards the open sourced framework painful.
 */

/**
 * Monitor interface to provide callbacks for test results
 * 
 * @since 4.0
 */
public interface TestMonitor {

	/**
	 * Call back method to indicate the begin of a test run.
	 */
	void beginTestRun();

	/**
	 * Call back method to indicate the begin of a TestSuite.
	 * 
	 * @param suite
	 *            The TestSuite to run
	 */
	void beginTestSuite(TestSuite suite);

	/**
	 * Call back to indicate the start of a test case.
	 * 
	 * @param testCase
	 *            The meta data of the test case that is about to begin.
	 */
	void beginTest(TestCase testCase);

	/**
	 * Performs the given assertion. If the condition evaluates to false, the
	 * failure message applies.
	 * 
	 * @param condition
	 *            The condition to evaluate
	 * @param messageOnFailure
	 *            The message to be applied when the condition fails.
	 */
	void assertion(boolean condition, String messageOnFailure);

	/**
	 * Call back to indicate something went wrong.
	 * 
	 * @param message
	 *            The failure message.
	 * @param exception
	 *            An optional exception.
	 */
	void error(String message, Throwable exception);
        
        /**
         * Fail with a particular message. Short for assertion(false,message)
         * @param message The message to fail with
         */
        void fail(String message);

	/**
	 * Call back to indicate the end of a test case.
	 * 
	 * @param testCase
	 *            The meta data of the test case that has ended.
	 */
	void endTest(TestCase testCase);

	/**
	 * Call back method to indicate the end of a TestSuite.
	 * 
	 * @param suite
	 *            The TestSuite that has ended
	 */
	void endTestSuite(TestSuite suite);

	/**
	 * Call back method to indicate the end of a test run.
	 */
	void endTestRun();
}
