package eu.monnetproject.framework.test.systemstate;

/**
 * Secondary interface for services that want to be notified of changes in the system state.
 * The lifecycle is as follows:<br/>
 * When the system is starting the state is set to STARTING and the onStarting callback is 
 * called on all {@link SystemStateListener} objects. Possible next states: AVAILABLE, UNAVAILABLE, STOPPING.<br/>
 * When according to the algorithm used by the {@link SystemStateService} the system is AVAILABLE
 * the onAvailable callback is called. Possible next states: UNAVAILABLE, STOPPING.
 * When according to the algorithm used by the {@link SystemStateService} the system is UNAVAILABLE
 * the onAvailable callback is called. Possible next states: AVAILABLE, STOPPING.
 * When the system is stopping by means of stopping the Framework bundle the onStopping callback
 * is called. Possible next states: none.
 */
public interface SystemStateListener {

	/**
	 * Indicates the system is starting.
	 */
	void onStarting();
	
	/**
	 * Indicates the system is stopping.
	 */
	void onStopping();
	
	/**
	 * Indicates the system is available.
	 */
	void onAvailable();
	
	/**
	 * Indicates the system is unavailable.
	 */
	void onUnavailable();
	
}
