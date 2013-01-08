package eu.monnetproject.framework.test.systemstate.impl;

public class StopWatch {
	private long time;
	
	public void reset() {
		time = System.currentTimeMillis();
	}
	
	public void start() {
		time = System.currentTimeMillis();
	}
	
	public long getTime() {
		return System.currentTimeMillis() - time;
	}
}
