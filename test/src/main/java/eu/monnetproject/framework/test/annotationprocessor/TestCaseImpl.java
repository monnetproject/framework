package eu.monnetproject.framework.test.annotationprocessor;

import java.lang.reflect.Method;

import eu.monnetproject.framework.test.TestCase;
import eu.monnetproject.framework.test.TestMonitor;


/**
 * A {@code TestCase} implementation.
 * 
 * @since	4.0
 */
public class TestCaseImpl implements TestCase {

	private String m_identifier;

	private String m_label;

	private Object m_object;

	private Method m_method;

	@Override
	public String getIdentifier() {
		return m_identifier;
	}

	@Override
	public String getLabel() {
		return m_label;
	}

	/**
	 * @param	identifier
	 * 			the new identifier of this {@code TestCase}. 
	 * @since	4.0
	 */
	protected void setIdentifier(String identifier) {
		m_identifier = identifier;
	}

	/**
	 * @param	label
	 * 			the new label of this {@code TestCase}. 
	 * @since	4.0
	 */
	protected void setLabel(String label) {
		m_label = label;
	}

	/**
	 * @param	object
	 * 			the service to invoke the method on. 
	 * @since	4.0
	 */
	protected void setInstance(Object object) {
		m_object = object;
	}

	/**
	 * @param	method
	 * 			the test method to invoke.
	 * @since	4.0
	 */
	protected void setMethod(Method method) {
		m_method = method;
	}

	/**
	 * Invokes the specified test method on the instance object and passes the monitor along.
	 * @param	monitor
	 * 			the monitor to pass onto the invoked test method.
	 * @since	4.0
	 */
	protected void invoke(TestMonitor monitor) {
		try {
			m_method.invoke(m_object, monitor);
		} catch (Exception exception) {
			monitor.error("Test failed.", exception);
		}
	}

}