package eu.monnetproject.framework.test.annotationprocessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.osgi.framework.ServiceReference;

import eu.monnetproject.framework.test.annotation.TestCase;
import eu.monnetproject.framework.test.annotation.TestSuite;
import org.osgi.framework.*;

/**
 * A {@code TestAnnotationProcessor} instance listens white-board style to all
 * registered services and creates a {@code TestSuite} for every service that is
 * annotated with the {@code TestSuite} annotation. Furthermore, for every
 * method in the service instance that is annotated with the {@code TestCase}
 * annotation a {@code TestCase} is created.
 *
 * @since	4.0
 */
public class TestAnnotationProcessor {

    private final BundleContext context;
    private final HashMap<ServiceReference, ServiceRegistration> registrations = new HashMap<ServiceReference, ServiceRegistration>();
    
    
    public TestAnnotationProcessor(BundleContext context) {
        this.context = context;
    }

    /**
     * Call back method that is called when a service is added. When the service
     * is annotated with {@code TestSuite} annotation a {@code TestSuite} is
     * created.
     *
     * @param	ref a reference to the service that is being added.
     * @param	service the new service that is added.
     * @since	4.0
     */
    public Object add(final ServiceReference ref) {
        final Object service = context.getService(ref);
        if (service == null) {
            return null;
        }
        if (service.getClass().getAnnotation(TestSuite.class) != null) {
            final TestSuiteImpl testSuite = createTestSuite(service);
            final ServiceRegistration serviceReg = context.registerService(eu.monnetproject.framework.test.TestSuite.class.getName(), testSuite, null);
            registrations.put(ref, serviceReg);
            context.addServiceListener(
                    new ServiceListener() {

                        @Override
                        public void serviceChanged(ServiceEvent se) {
                            if (se.getServiceReference() == ref && se.getType() == ServiceEvent.UNREGISTERING) {
                                serviceReg.unregister();
                                context.removeServiceListener(this);
                            }
                        }
                    });
            return service;
        } else {
            return null;
        }
    }


    /**
     * Call back method that is called when a service is removed. When a service
     * is annotated with a {@code TestSuite} annotation the created {@code TestSuite}
     * is also removed.
     *
     * @param	ref a reference to the service that is being removed.
     * @param	service the service that is removed.
     * @since	4.0
     */
    public void remove(ServiceReference ref, Object service) {
        if(registrations.containsKey(ref)) {
            registrations.remove(ref).unregister();
        }
    }

    /**
     * Creates a {@code TestSuiteImpl} instance for the given service parameter.
     *
     * @param	service the service to create a {@code TestSuite} for.
     * @return	an initialized {@code TestSuiteImpl} instance.
     * @since	4.0
     */
    private TestSuiteImpl createTestSuite(Object service) {
        TestSuiteImpl testSuite = new TestSuiteImpl();
        Class<?> c = service.getClass();
        String label = c.getAnnotation(TestSuite.class).label();
        testSuite.setLabel(label);
        Method[] declaredMethods = c.getDeclaredMethods();
        for (Method m : declaredMethods) {
            Annotation[] methodAnnotations = m.getAnnotations();
            for (Annotation a : methodAnnotations) {
                if (a instanceof TestCase) {
                    TestCase testAnnotation = (TestCase) a;
                    TestCaseImpl testCase = new TestCaseImpl();
                    boolean valid = true;
                    testCase.setIdentifier(testAnnotation.identifier());
                    testCase.setLabel(testAnnotation.label());
                    testCase.setInstance(service);
                    testCase.setMethod(m);
                    if (valid) {
                        testSuite.addTestCase(testCase);
                    }
                }
            }
        }
        return testSuite;
    }
}