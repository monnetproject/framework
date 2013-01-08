package eu.monnetproject.framework.test.systemstate.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.monnetproject.framework.test.systemstate.SystemStateListener;

public class Activator implements BundleActivator {

	EntropyBasedSystemStateService service;

	@Override
	public void start(final BundleContext context) throws Exception {
		try {
			service = new EntropyBasedSystemStateService(context);
			final ServiceListener listener = new ServiceListener() {

				@Override
				public void serviceChanged(ServiceEvent event) {
					final ServiceReference reference = event
							.getServiceReference();
					if (event.getType() == ServiceEvent.REGISTERED) {
						service.serviceAdded(reference);
					} else if (event.getType() == ServiceEvent.MODIFIED) {
						service.serviceChanged(reference);
					} else if (event.getType() == ServiceEvent.UNREGISTERING) {
						service.serviceRemoved(reference);
					} else {
						throw new RuntimeException("Bad event type");
					}
				}
			};
			context.addServiceListener(listener);
			new ServiceTracker(context, SystemStateListener.class.getName(),
					new ServiceTrackerCustomizer() {

						@Override
						public void removedService(ServiceReference reference,
								Object service2) {
							service.listenerRemoved((SystemStateListener) service2);
						}

						@Override
						public void modifiedService(ServiceReference reference,
								Object service2) {

						}

						@Override
						public Object addingService(ServiceReference reference) {
							final Object s = context.getService(reference);
							if (s != null) {
								service.listenerAdded((SystemStateListener) s);
							}
							return s;
						}
					}).open();
			service.init();
			service.start();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		service.stop();

	}

}
