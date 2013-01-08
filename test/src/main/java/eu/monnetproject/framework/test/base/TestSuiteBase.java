package eu.monnetproject.framework.test.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.monnetproject.framework.test.TestCase;
import eu.monnetproject.framework.test.TestMonitor;
import eu.monnetproject.framework.test.TestSuite;
import java.util.logging.Logger;

/**
 * Note: This code is copied from the Be Informed test framework (com.beinformed.product.platform.framework.testability).
 * This framework is likely to be open sourced. Therefore please do not make changes to the interfaces since it will
 * make a transition towards the open sourced framework painful.
 */

/**
 * Abstract base implementation for TestSuites reflection based. <br />
 * Subclasses can call addTest() methods with as argument the method accepting
 * the TestCaseMonitor as only argument.
 * 
 * @since 4.0
 */
public abstract class TestSuiteBase implements TestSuite {

	private final transient Map<TestCase, Method> testCases = new HashMap<TestCase, Method>();
	// TODO: Use a generic logging API instead of a custom interface.
//	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteBase.class);
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final String label;

	public TestSuiteBase(String label) {
		this.label = label;
		initialize();
	}

	public TestSuiteBase(Class<?> clazz) {
		this(clazz.getName().replaceAll(".", "/"));
	}

	protected void initialize() {
		// Adds all methods that have a name that starts with 'test' and have only one argument of type TestMonitor
		for (Method method : this.getClass().getMethods()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1 && parameterTypes[0] == TestMonitor.class && method.getName().startsWith("test")) {
				addTest(method.getName());
			}
		}
	}

	/**
	 * Adds test meta data with the given identifier as identifier and label.
	 * 
	 * @param testMethod
	 *            The name of the test method. This method must accept one
	 *            argument of type {@link TestMonitor}.
	 */
	protected final void addTest(final String testMethod) {
		addTest(testMethod, testMethod);
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Adds test meta data with the given identifier and label.
	 * 
	 * @param testMethod
	 *            The name of the test method. This method must accept one
	 *            argument of type {@link TestMonitor}.
	 * @param label
	 *            The label of the test case. If the label is {@code null}, the
	 *            identifier will be used as a label.
	 */
	protected final void addTest(final String testMethod, final String label) {
		synchronized (testCases) {
			final TestCase testCase = new TestMetaData(testMethod, label);
			if (testCases.containsKey(testCase)) {
				log.warning("Test method '" + testMethod + "' already exists in this TestSuite. The existing test case will be overridden.");
			}
			Method method;
			try {
				method = this.getClass().getMethod(testMethod, TestMonitor.class);
				testCases.put(testCase, method);
			} catch (Exception e) {
				log.severe("Could not add test method " + testMethod);
			}
		}
	}

	/**
	 * Removes the given test case.
	 * 
	 * @param testCase
	 *            the meta data of the test case to remove from this TestSuite.
	 */
	protected final void removeTest(final TestCase testCase) {
		removeTest(testCase.getIdentifier());
	}

	/**
	 * Removes the given test case.
	 * 
	 * @param identifier
	 *            the identifier of the test case to remove from this TestSuite.
	 */
	protected final void removeTest(final String identifier) {
		Method removedTestMethod = null;
		synchronized (testCases) {
			removedTestMethod = testCases.remove(new TestMetaData(identifier));
		}
		if (removedTestMethod == null) {
			log.warning("Test case with identifier '" + identifier + "' does not exist in this TestSuite. Test case could not be removed.");
		}
	}

	@Override
	public final List<TestCase> getTestCases() {
		synchronized (testCases) {
			return new ArrayList<TestCase>(testCases.keySet());
		}
	}

	@Override
	public final void test(final String testCaseId, final TestMonitor monitor) {
		Method testMethod = null;
		final TestCase testCase = new TestMetaData(testCaseId);
		synchronized (testCases) {
			testMethod = testCases.get(testCase);
		}
		if (testMethod == null) {
			final String message = "Could not execute test with testCaseId '" + testCaseId + "' because there is no such test case present in this testSuite.";
			monitor.error(message, null);
			return;
		}

		test(testMethod, testCase, monitor);
	}

	private void test(final Method testMethod, final TestCase testCase, final TestMonitor monitor) {
		try {
			log.info("Starting test: " + testCase.getIdentifier());
			testMethod.invoke(this, monitor);
			log.info("Finished test: " + testCase.getIdentifier());
		} catch (Exception e) {
			final String message = "Could not execute test case " + testCase;
			monitor.error(message, e);
		}
	}

}
