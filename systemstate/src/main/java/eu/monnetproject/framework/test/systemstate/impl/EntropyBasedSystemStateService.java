package eu.monnetproject.framework.test.systemstate.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;

import eu.monnetproject.framework.test.systemstate.State;
import eu.monnetproject.framework.test.systemstate.SystemStateListener;
import eu.monnetproject.framework.test.systemstate.SystemStateService;

public class EntropyBasedSystemStateService implements BundleListener, SystemStateService {

	private final Set<Token> tokens = Collections.synchronizedSet(new HashSet<Token>());

	private final List<SystemStateListener> listeners = new ArrayList<SystemStateListener>();

	private int timeoutInMillis = 2000;

	private int pollingInterval = 200;

	private final StopWatch idleWatch = new StopWatch();

	private volatile State currentState = State.UNAVAILABLE;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private final BundleContext bundleContext;

	private final static List<String> serviceInterfacesToIgnore = new ArrayList<String>();
	static {
		serviceInterfacesToIgnore.add("org.osgi.service.event.EventHandler");
	}

	public EntropyBasedSystemStateService(BundleContext context) {
		this.bundleContext = context;
		if (System.getProperty("entropyTimeout") != null) {
			timeoutInMillis = Integer.parseInt(System.getProperty("entropyTimeout"));
			//log.info("Using configured entropy timeout of {} ms.", new String[] { String.valueOf(timeoutInMillis) });
		}
		if (System.getProperty("entropyInterval") != null) {
			pollingInterval = Integer.parseInt(System.getProperty("entropyInterval"));
			//log.info("Using configured entropy polling interval of {} ms.", new String[] { String.valueOf(pollingInterval) });
		}
	}

	public String getCurrentSystemState() {
		return currentState.toString();
	}

	public int getCurrentWork() {
		return tokens.size();
	}

	void init() {
		bundleContext.addBundleListener(this);
	}

	void start() {
		currentState = State.STARTING;
		handleStateChange(State.STARTING);
	}

	void stop() {
		executor.shutdownNow();
	}

	void destroy() {
		bundleContext.removeBundleListener(this);
	}

	private void handleStateChange(State newState) {
		SystemStateListener[] list;
		// first make a copy of the current list of listeners in a synchronized block
		// because we want to handle concurrent access to this list
		synchronized (listeners) {
			list = listeners.toArray(new SystemStateListener[listeners.size()]);
		}
		// then invoke all listeners outside of the synchronized block because
		// we don't want to be holding any locks while invoking callbacks
		for (SystemStateListener listener : list) {
			try {
				notifyListener(listener, newState);
			} catch (Exception e) {
				//log.error("Exception while trying to notify listener " + listener.getClass().getName() + "(" + listener
				//		+ ") of system stable state change to " + newState, e);
			}
		}
		//log.info("Notified listeners, system is: " + newState);
	}

	private void notifyListener(SystemStateListener listener, State state) {
		switch (state) {
		case STARTING:
			listener.onStarting();
			break;
		case STOPPING:
			listener.onStopping();
			break;
		case AVAILABLE:
			listener.onAvailable();
			break;
		case UNAVAILABLE:
			listener.onUnavailable();
			break;
		default:
			throw new IllegalStateException("Attempt to notify of an unknown system state: " + state);
		}
	}

	void listenerAdded(SystemStateListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
		// notify the listener outside of the synchronized block so we're
		// not invoking any callbacks while holding a lock
		notifyListener(listener, currentState);
	}

	void listenerRemoved(SystemStateListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void handleSystemNoise() {
		boolean stateChanged = false;
		synchronized (currentState) {
			idleWatch.reset();
			idleWatch.start();
			if (currentState == State.AVAILABLE || currentState == State.STARTING) {
				currentState = State.UNAVAILABLE;
				// flag that indicates we still need to do work outside of the 
				// synchronized block (work we can't do here)
				stateChanged = true;
			}
		}
		if (stateChanged) {
			handleStateChange(State.UNAVAILABLE);
			executor.execute(new IdleWatchMonitor());
		}
	}

	void serviceAdded(ServiceReference reference) {
		if (isValidService(reference)) {
			handleSystemNoise();
		}
	}

	void serviceChanged(ServiceReference reference) {
		if (isValidService(reference)) {
			handleSystemNoise();
		}
	}

	void serviceRemoved(ServiceReference reference) {
		if (isValidService(reference)) {
			handleSystemNoise();
		}
	}

	private boolean isValidService(ServiceReference reference) {
		Object o = reference.getProperty("objectClass");
		if (o instanceof String[]) {
			String[] serviceInterfaces = (String[]) o;
			// Valid means that at least one of the service interfaces is not in the ignore list.
			for (String serviceInterface : serviceInterfaces) {
				if (!serviceInterfacesToIgnore.contains(serviceInterface)) {
					return true;
				}
			}
			return false;
		}
		// Unable to determine...we will react on it anyway
		return true;
	}

	class IdleWatchMonitor implements Runnable {
		boolean interrupted;

		@Override
		public void run() {
			while (!interrupted) {
				try {
					Thread.sleep(pollingInterval);
				//	log.debug("*** IdleWatchMonitor waking up.");
					boolean stateChanged = false;
					synchronized (currentState) {
						if (currentState != State.UNAVAILABLE) {
							interrupted = true;
						} else {
							// compare the split time of the monitor to the timeout, and also make
							// sure we have no outstanding tokens that signal that there is still ongoing
							// work
							if (idleWatch.getTime() > timeoutInMillis && tokens.isEmpty()) {
								// yay, we think the system is stable
								currentState = State.AVAILABLE;
								stateChanged = true;
								interrupted = true;
							}
						}
					}
					if (stateChanged) {
						handleStateChange(State.AVAILABLE);
					}
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		}
	}

	public int getTimeoutInMillis() {
		return timeoutInMillis;
	}

	public void setTimeoutInMillis(int timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}

	public int getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	private final Map<Bundle, Token> starting = Collections.synchronizedMap(new HashMap<Bundle, Token>());

	private final Map<Bundle, Token> stopping = Collections.synchronizedMap(new HashMap<Bundle, Token>());

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		int type = event.getType();

		// if any bundle enters the STARTING state, we create a unit of work
		// for it that will end as soon as that same bundle transitions into
		// any other state
		if (type == BundleEvent.STARTING) {
			Token token = startWork(bundle);
			Token old = starting.put(bundle, token);
			if (old != null) {
				// this is in fact very weird, the bundle already was in
				// starting state and entered the same state *again*
				//log.error("Bundle entered STARTING state twice, old token {}, new token {}", old, token);

				// for now we will try to recover by simply ending the old unit
				// of work
				endWork(old);
			}
		} else {
			// when a bundle enters any other state than STARTING, we should make sure it's
			// no longer on the starting list
			Token token = starting.remove(bundle);
			if (token != null) {
				// it was on the list, so we end the unit of work
				endWork(token);
			}
		}

		// if any bundle enters the STOPPING state, we create a unit of work
		// for it that will end as soon as that same bundle transitions into
		// any other state
		if (type == BundleEvent.STOPPING) {
			Token token = startWork(bundle);
			Token old = stopping.put(bundle, token);
			if (old != null) {
				// this is in fact very weird, the bundle already was in
				// starting state and entered the same state *again*
				//log.error("Bundle entered STOPPING state twice, old token {}, new token {}", old, token);

				// for now we will try to recover by simply ending the old unit
				// of work
				endWork(old);
			}
		} else {
			// when a bundle enters any other state than STOPPING, we should make sure it's
			// no longer on the stopping list
			Token token = stopping.remove(bundle);
			if (token != null) {
				// it was on the list, so we end the unit of work
				endWork(token);
			}
		}

		// when the system bundle is stopping, that's a special case, as we know
		// our framework is going down (sorry folks, no way out anymore now)
		if (bundle.getBundleId() == 0) {
			if (type == BundleEvent.STOPPING) {
				handleStateChange(State.STOPPING);
			}
		}
	}

	@Override
	public Token startWork(Object reference) {
		TokenImpl token = new TokenImpl(reference);
		tokens.add(token);
		return token;
	}

	@Override
	public void endWork(Token token) {
		if (token == null) {
			//log.debug("End of work signalled with null token, ignoring.");
			return;
		}
		boolean removed = tokens.remove(token);
		if (!removed) {
			//log.debug("{} signalled the end of work for a task that had not been started, ignoring.", token);
		}
	}
}
