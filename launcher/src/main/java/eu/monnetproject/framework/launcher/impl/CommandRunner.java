package eu.monnetproject.framework.launcher.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.monnetproject.framework.launcher.Command;

public class CommandRunner implements ServiceTrackerCustomizer {
	private Command command;
	private final BundleContext context;
	private final String mainClass;
	
	public CommandRunner(BundleContext context, String mainClass) {
		this.context = context;
		this.mainClass = mainClass;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		final Object s = context.getService(reference);
		if(s == null || !(s instanceof Command)) {
			return null;
		}
		if(s.getClass().getName().equals(mainClass)) {
			return command = (Command)s;
		} else {
			return null;
		}
	}

	@Override
	public void modifiedService(ServiceReference reference, Object s) {
		if(s != null && s.getClass().getName().equals(mainClass)) {
			command = (Command)s;
		} 
	}

	@Override
	public void removedService(ServiceReference reference, Object s) {
		if(s != null && s.getClass().getName().equals(mainClass)) {
			command = null;
		}
	}
	
	public void exec(String [] args) {
		if(command == null) {
			System.err.println("Command not found " + mainClass);
		} else {
			try {
				command.execute(args);
			} catch(Throwable x) {
				x.printStackTrace();
			}
		}	
	}
	
	
	
}
