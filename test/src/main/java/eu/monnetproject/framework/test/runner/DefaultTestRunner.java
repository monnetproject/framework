package eu.monnetproject.framework.test.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.monnetproject.framework.test.TestCase;
import eu.monnetproject.framework.test.TestMonitor;
import eu.monnetproject.framework.test.TestRunner;
import eu.monnetproject.framework.test.TestSuite;
import eu.monnetproject.framework.test.base.NullTestMonitor;
import eu.monnetproject.util.Logger;
import eu.monnetproject.util.Logging;
import java.util.*;

/**
 * Default test runner implementation. <br />
 * Test runner acts also as a Job. <br />
 * Manageable settings are: <br />
 * TODO BACKLOG: afmaken
 * 
 * @navassoc - Whiteboard - TestSuite
 * @navassoc - Whiteboard - TestMonitor
 * 
 * @since 4.0
 */
public class DefaultTestRunner implements TestRunner {

	private final Logger log = Logging.getLogger(this);
	private final BundleContext context;

	private final Map<ServiceReference, TestSuite> testSuites = new ConcurrentHashMap<ServiceReference, TestSuite>();

	private final Map<ServiceReference, TestMonitor> testMonitors = new ConcurrentHashMap<ServiceReference, TestMonitor>();

	private boolean deploymentTestingEnabled = false;

	private ExecutorService runTestsExecutorService = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private String testSuitesConcurrent;

	private final TestMonitor monitor = new CompositeTestMonitor();

	private int nrOfWarmUpRuns = 0;

	private int nrOfTestRuns = 1;

	
	
	public DefaultTestRunner(BundleContext context) {
		super();
		this.context = context;
	}

	public Object addTestSuite(ServiceReference reference) {
		final Object ref = context.getService(reference);
		if(ref == null || !(ref instanceof TestSuite)) {
			return null;
		}
		final TestSuite testSuite = (TestSuite)ref;
		System.err.println("Add test suite " + testSuite.getLabel());
		testSuites.put(reference, testSuite);

		if (deploymentTestingEnabled) {
			executeTest(testSuite);
		}
		return ref;
	}

	public void removeTestSuite(ServiceReference reference, TestSuite testSuite) {
		System.err.println("Remove test suite " + testSuite.getLabel());
		testSuites.remove(reference);
	}

	public void swapTestSuite(ServiceReference previousReference,
			TestSuite previousTestSuite, ServiceReference currentReference,
			TestSuite currentTestSuite) {
		removeTestSuite(previousReference, previousTestSuite);
		addTestSuite(currentReference);
	}

	public Object addTestMonitor(ServiceReference reference) {
		final Object ref = context.getService(reference);
		if(ref == null || !(ref instanceof TestMonitor)) {
			return null;
		}
		final TestMonitor testMonitor = (TestMonitor)ref;
		testMonitors.put(reference, testMonitor);
		return ref;
	}

	public void removeTestMonitor(ServiceReference reference,
			TestMonitor testMonitor) {
		log.finest("Remove test monitor " + testMonitor.getClass().getName());
		testMonitors.remove(reference);
	}

	public void swapTestMonitor(ServiceReference previousReference,
			TestMonitor previousTestMonitor, ServiceReference currentReference,
			TestMonitor currentTestMonitor) {
		removeTestMonitor(previousReference, previousTestMonitor);
		addTestMonitor(currentReference);
	}

	public void executeTests() {
		List<TestSuite> testSuitesCopy = new ArrayList<TestSuite>(
				testSuites.values());
		Collections.sort(testSuitesCopy, new TestSuiteComparator());

		log.info("Executing tests (number of testsuites="
				+ testSuitesCopy.size() + ")");

		log.info("Current number of warmup runs " + nrOfWarmUpRuns);
		log.info("Current number of test runs " + nrOfTestRuns);
		monitor.beginTestRun();
		for (TestSuite testSuite : testSuitesCopy) {
			handleWarmUp(nrOfWarmUpRuns, testSuite);

			for (int i = 0; i < nrOfTestRuns; i++) {
				log.info("Executing testsuite " + testSuite.getLabel() + " ("
						+ (i + 1) + ")");
				try {
					executeTest(testSuite);
				} catch (Throwable t) {
					monitor.error("Exception while running test run", t);
				}
				monitor.endTestRun();
			}
		}
	}

	private void handleWarmUp(int nrOfWarmUpRuns, TestSuite suite) {
		if (nrOfWarmUpRuns > 0) {
			TestMonitor nullMonitor = new NullTestMonitor();
			for (TestCase testCase : suite.getTestCases()) {
				suite.test(testCase.getIdentifier(), nullMonitor);
			}
		}
	}

	public void executeTest(String testSuiteLabel) {
		TestSuite testSuite = findTestSuiteByLabel(testSuiteLabel);
		if (testSuite != null) {
			executeTest(testSuite);
		} else {
			log.info("No Test suite found with label: " + testSuiteLabel);
		}
	}

	private TestSuite findTestSuiteByLabel(String testSuiteLabel) {
		List<TestSuite> testSuitesCopy = new ArrayList<TestSuite>(
				testSuites.values());
		for (TestSuite testSuite : testSuitesCopy) {
			String label = testSuite.getLabel();
			if (label != null && label.equals(testSuiteLabel)) {
				return testSuite;
			}
		}
		return null;
	}

	private void executeTest(final TestSuite testSuite) {
		boolean runInParallel = isTestSuiteConcurrent(testSuite.getLabel());

		monitor.beginTestSuite(testSuite);
		try {
			for (final TestCase testCase : testSuite.getTestCases()) {
				executeTestCase(testSuite, runInParallel, testCase);
			}
		} catch (Throwable t) {
			monitor.error("Exception while running test suite", t);
		} finally {
			monitor.endTestSuite(testSuite);
		}
	}

	private void executeTestCase(final TestSuite testSuite,
			boolean runInParallel, final TestCase testCase) {
		monitor.beginTest(testCase);
		try {
			final String testCaseId = testCase.getIdentifier();
			if (runInParallel) {
				runTestsExecutorService.execute(new Runnable() {
					@Override
					public void run() {
						testSuite.test(testCaseId, monitor);
					}
				});
			} else {
				testSuite.test(testCaseId, monitor);
			}
		} catch (Throwable t) {
			monitor.error("Exception while running test case", t);
		} finally {
			monitor.endTest(testCase);
		}
	}

	public final String getTestSuites() {
		List<TestSuite> testSuitesCopy = new ArrayList<TestSuite>(
				testSuites.values());
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<TestSuite> iter = testSuitesCopy.iterator();
        while(iter.hasNext()) {
            stringBuilder.append(iter.next());
            if(iter.hasNext()) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
	}

	/**
	 * Comma separated list of test suites (defined by label) which tests must
	 * be run in parallel.
	 */
	public String[] getTestSuitesConcurrentList() {
		return testSuitesConcurrent.split(",");
	}

	private boolean isTestSuiteConcurrent(String testSuiteLabel) {
		if (testSuitesConcurrent != null) {
			List<String> testSuitesConcurrentList = Arrays
					.asList(testSuitesConcurrent.split(","));
			return testSuitesConcurrentList.contains(testSuiteLabel);
		} else {
			return false;
		}
	}

	/**
	 * Comma separated list of test suites (defined by label) which tests must
	 * be run in parallel.
	 */
	public void setTestSuitesConcurrent(String testSuitesConcurrent) {
		this.testSuitesConcurrent = testSuitesConcurrent;
	}

	public void setNrOfWarmUpRuns(int nrOfWarmUpRuns) {
		this.nrOfWarmUpRuns = nrOfWarmUpRuns;
	}

	public void setNrOfTestRuns(int nrOfTestRuns) {
		this.nrOfTestRuns = nrOfTestRuns;
	}

	public int getNrOfTestRuns() {
		return nrOfTestRuns;
	}

	public int getNrOfWarmUpRuns() {
		return nrOfWarmUpRuns;
	}

	private class CompositeTestMonitor implements TestMonitor {

		@Override
		public void beginTestRun() {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.beginTestRun();
			}
		}

		@Override
		public void beginTestSuite(TestSuite suite) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.beginTestSuite(suite);
			}
		}

		@Override
		public void beginTest(TestCase testCase) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.beginTest(testCase);
			}
		}

		@Override
		public void assertion(boolean condition, String messageOnFailure) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.assertion(condition, messageOnFailure);
			}
		}

		@Override
		public void error(String message, Throwable exception) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.error(message, exception);
			}
		}

		@Override
		public void endTest(TestCase testCase) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.endTest(testCase);
			}
		}

		@Override
		public void endTestSuite(TestSuite suite) {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.endTestSuite(suite);
			}
		}

		@Override
		public void endTestRun() {
			for (TestMonitor monitor : testMonitors.values()) {
				monitor.endTestRun();
			}
		}

        @Override
        public void fail(String message) {
            assertion(false, message);
        }
                
                

	}

	private class TestSuiteComparator implements Comparator<TestSuite> {

		@Override
		public int compare(TestSuite t0, TestSuite t1) {
			if (t0.getLabel() == null && t1.getLabel() == null) {
				return 0;
			} else if (t0.getLabel() == null) {
				return -1;
			} else if (t1.getLabel() == null) {
				return 1;
			} else {
				return t0.getLabel().compareToIgnoreCase(t1.getLabel());
			}
		}
	}

}
