package eu.monnetproject.framework.launcher.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

//import com.beinformed.framework.osgi.frameworkstate.FrameworkStateListener;
import eu.monnetproject.framework.launcher.Command;

public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		final String exec = System.getProperty("exec.mainClass");
		if(exec == null) {
			return;
		}
		final String[] args = System.getProperty("exec.args","").split("(?<!\\\\)\\s+");
		final CommandRunner runner = new CommandRunner(context,exec);
		new ServiceTracker(context, Command.class.getName(),runner).open();
		
		//final FrameworkStateListener ssl = new FrameworkStateListener() {
		//	
		//	@Override
		//	public void onUnavailable() {
		//		
		//	}
		//	
		//	@Override
		//	public void onStopping() {
		//		
		//	}
		//	
		//	@Override
		//	public void onStarting() {
		//		
		//	}
		//	
		//	@Override
		//	public void onAvailable() {
		//		runner.exec(args);
		//		try {
		//			context.getBundles()[0].stop();
		//		} catch(BundleException x) {
		//			x.printStackTrace();
		//		}
		//	}
		//};
		//context.registerService(FrameworkStateListener.class.getName(), ssl, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
