package eu.monnetproject.framework.test;

/**
 * Note: This code is copied from the Be Informed test framework (com.beinformed.product.platform.framework.testability).
 * This framework is likely to be open sourced. Therefore please do not make changes to the interfaces since it will
 * make a transition towards the open sourced framework painful.
 */

/**
 * Represents meta data for a particular test.<br />
 * 
 * @since 4.0
 * @see TestSuite
 */
public interface TestCase {

	/**
	 * @return The identifier of this TestCase
	 */
	String getIdentifier();

	/**
	 * @return The label of this TestCase
	 */
	String getLabel();
}
