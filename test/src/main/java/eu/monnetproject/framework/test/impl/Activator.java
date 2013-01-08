package eu.monnetproject.framework.test.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.monnetproject.framework.test.TestMonitor;
import eu.monnetproject.framework.test.TestRunner;
import eu.monnetproject.framework.test.TestSuite;
import eu.monnetproject.framework.test.annotationprocessor.TestAnnotationProcessor;
import eu.monnetproject.framework.test.reporter.XMLReporter;
import eu.monnetproject.framework.test.runner.DefaultTestRunner;
import eu.monnetproject.framework.test.systemstate.SystemStateListener;

public class Activator implements BundleActivator {
	@Override
	public void start(final BundleContext context) throws Exception {
		System.err.println("Starting Test Framework");
		
		final TestAnnotationProcessor tap = new TestAnnotationProcessor(context);
		new ServiceTracker(context, Object.class.getName(), new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				return tap.add(reference);
			}
		}).open();
		
		if(System.getProperty("eu.monnetproject.framework.test.disable.junit.xml") == null) {
			context.registerService(TestMonitor.class.getName(), new XMLReporter(), null);
		}
		
		final DefaultTestRunner dtr = new DefaultTestRunner(context);
		new ServiceTracker(context, TestSuite.class.getName(), new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				dtr.removeTestSuite(reference, (TestSuite)service);
				
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				dtr.swapTestSuite(reference, null, reference, (TestSuite) service);
				
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				System.err.println("Got test suite");
				return dtr.addTestSuite(reference);
			}
		}).open();
		new ServiceTracker(context, TestMonitor.class.getName(), new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				dtr.removeTestMonitor(reference, (TestMonitor)service);
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				dtr.swapTestMonitor(reference, null, reference, (TestMonitor) service);
				
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				return dtr.addTestMonitor(reference);
			}
		}).open();
		
		context.registerService(TestRunner.class.getName(), dtr,null);
		
		final SystemStateListener ssl = new SystemStateListener() {
			
			@Override
			public void onUnavailable() {
				System.err.println("Test framework waiting for system to become ready");
			}
			
			@Override
			public void onStopping() {
				
			}
			
			@Override
			public void onStarting() {
				
			}
			
			@Override
			public void onAvailable() {
				System.err.println("System state is ready");
				final String doTest = System.getProperty("eu.monnetproject.framework.test", "false");
				if(Boolean.parseBoolean(doTest)) {
					System.err.println("Running tests");
					dtr.executeTests();
					System.err.println("Tests complete stopping framework");
					try {
						context.getBundles()[0].stop();
					} catch(BundleException x) {
						System.err.println("Could not stop the framework:");
						x.printStackTrace();
					}
				} else {
					System.err.println("eu.monnetproject.framework.test not set... not running tests");
				}
			}
		};
		context.registerService(SystemStateListener.class.getName(), ssl, null);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
