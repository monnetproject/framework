package eu.monnetproject.framework.test;

import java.util.List;

/**
 * Note: This code is copied from the Be Informed test framework (com.beinformed.product.platform.framework.testability).
 * This framework is likely to be open sourced. Therefore please do not make changes to the interfaces since it will
 * make a transition towards the open sourced framework painful.
 */

/**
 * Service interface to execute tests whiteboard style. <br />
 * TestSuite is a capability interface that classes can implement when they want
 * the test framework to test something. <br />
 * TestRunner implementations are responsible for executing the actual tests.
 * 
 * @navassoc - - - TestMonitor
 * @composed - - - TestCase
 * 
 * @see TestRunner
 * @since 4.0
 */
public interface TestSuite {

	/**
	 * Runs the given testCase
	 * 
	 * @param testCaseId
	 *            the test case to execute
	 * @param monitor
	 *            the call back monitor to accept test results.
	 */
	void test(String testCaseId, TestMonitor monitor);

	/**
	 * @return A list of all test cases for this TestSuite
	 */
	List<TestCase> getTestCases();
	
	/**
	 * @return A descriptive label for this TestSuite
	 * @return
	 */
	String getLabel();
}
