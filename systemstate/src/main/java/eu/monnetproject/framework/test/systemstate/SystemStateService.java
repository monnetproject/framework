package eu.monnetproject.framework.test.systemstate;

public interface SystemStateService {

	/**
	 * @param callerReference
	 *            Identification of the caller.
	 * @return identification of the component that starts the work. Must also
	 *         be provided when calling {@link #endWork(Object)}
	 */
	Token startWork(Object callerReference);

	void endWork(Token token);

	/**
	 * Report busy state
	 */
	void handleSystemNoise();

	public interface Token {

	}
}
