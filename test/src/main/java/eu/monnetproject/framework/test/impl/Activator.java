package eu.monnetproject.framework.test.impl;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.beinformed.framework.osgi.frameworkstate.FrameworkStateListener;
import com.beinformed.framework.osgi.osgitest.TestMonitor;
import com.beinformed.framework.osgi.osgitest.TestRunner;

import eu.monnetproject.framework.test.reporter.XMLReporter;

public class Activator extends DependencyActivatorBase implements FrameworkStateListener {
	
	private volatile BundleContext context;
	private volatile TestRunner testRunner;
	
	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		System.err.println("Starting Test Framework");
		
		/*
		 * We're not registering the annotation processor and default test runner from here
		 * since those are both started from their own bundle.
		 */
		
		if(System.getProperty("eu.monnetproject.framework.test.disable.junit.xml") == null) {
			manager.add(createComponent().setInterface(TestMonitor.class.getName(), null)
					.setImplementation(XMLReporter.class));
		}
		
		manager.add(createComponent().setInterface(FrameworkStateListener.class.getName(), null)
				.setImplementation(this)
				.add(createServiceDependency().setService(TestRunner.class).setRequired(true)));
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		
	}

	@Override
	public void onStarting() {
		
	}

	@Override
	public void onStopping() {
		
	}

	@Override
	public void onAvailable() {
		System.err.println("System state is ready");
		final String doTest = System.getProperty("eu.monnetproject.framework.test", "false");
		if(Boolean.parseBoolean(doTest)) {
			System.err.println("Running tests");
			testRunner.executeTests();
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

	@Override
	public void onUnavailable() {
		
	}

}
