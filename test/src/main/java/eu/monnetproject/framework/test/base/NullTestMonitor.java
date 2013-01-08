package eu.monnetproject.framework.test.base;

import eu.monnetproject.framework.test.TestCase;
import eu.monnetproject.framework.test.TestMonitor;
import eu.monnetproject.framework.test.TestSuite;

/**
 * Note: This code is copied from the Be Informed test framework (com.beinformed.product.platform.framework.testability).
 * This framework is likely to be open sourced. Therefore please do not make changes to the interfaces since it will
 * make a transition towards the open sourced framework painful.
 */

public class NullTestMonitor implements TestMonitor {
	@Override
	public void error(String message, Throwable exception) {

	}

	@Override
	public void endTest(TestCase testCase) {
	}

	@Override
	public void beginTest(TestCase testCase) {
	}

	@Override
	public void assertion(boolean condition, String messageOnFailure) {

	}

	@Override
	public void beginTestRun() {
	}

	@Override
	public void endTestRun() {
	}

	@Override
	public void beginTestSuite(TestSuite suite) {
		
	}

	@Override
	public void endTestSuite(TestSuite suite) {
		
	}

    @Override
    public void fail(String message) {
    }
        
}