package eu.monnetproject.framework.test.annotationprocessor;

import java.util.ArrayList;
import java.util.List;

import eu.monnetproject.framework.test.TestCase;
import eu.monnetproject.framework.test.TestMonitor;
import eu.monnetproject.framework.test.TestSuite;


/**
 * A {@code TestSuite} implementation.
 * 
 * @since	4.0
 */
public class TestSuiteImpl implements TestSuite {

	private String label;

	private final List<TestCase> testCases;

	/**
	 * Default {@code TestSuiteImpl} constructor.
	 * @since	4.0
	 */
	public TestSuiteImpl() {
		this.testCases = new ArrayList<TestCase>();
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public List<TestCase> getTestCases() {
		return testCases;
	}

	@Override
	public void test(String testCaseId, TestMonitor monitor) {
		for (TestCase tc : testCases) {
			if (tc.getIdentifier().equals(testCaseId)) {
				TestCaseImpl impl = (TestCaseImpl) tc;
				impl.invoke(monitor);
			}
		}
	}

	/**
	 * Setter for the label member of this {@code TestSuite}.
	 * @param	label
	 * 			the new label to use for this {@code TestSuite}.
	 * @since	4.0
	 */
	protected void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Adds a {@code TestCaseImpl} to this {@code TestSuite}.
	 * @param	testCase
	 * 			the {@code TestCaseImpl} to add to this {@code TestSuite}.
	 * @since	4.0
	 */
	protected void addTestCase(TestCaseImpl testCase) {
		testCases.add(testCase);
	}

}