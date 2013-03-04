/****************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package eu.monnetproject.framework.test.xmlreporter;

import java.util.LinkedList;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;

import com.beinformed.framework.osgi.osgitest.TestSuite;

/**
 *
 * @author John McCrae
 */
public class XMLReporterMonitor implements BundleListener {

    private final java.util.logging.Logger log = java.util.logging.Logger.getLogger(this.getClass().getName());
    private final LinkedList<Bundle> monitoredBundles = new LinkedList<Bundle>();
    private final LinkedList<TestSuite> monitoredCases = new LinkedList<TestSuite>();
    private boolean hasStopped = false;

    public void bundleChanged(BundleEvent be) {
        if(be.getBundle().getState() == Bundle.STARTING) {
            synchronized(this) {
                monitoredBundles.add(be.getBundle());
            }
        } else if(be.getBundle().getState() == Bundle.ACTIVE && monitoredBundles.contains(be.getBundle())) {
            synchronized(this) {
                monitoredBundles.remove(be.getBundle());
            }
        }
    }

    public void monitorCase(TestSuite testCase) {
        synchronized (this) {
            monitoredCases.add(testCase);
        }
    }

    public void caseFinished(TestSuite testCase) {
        synchronized (this) {
            monitoredCases.remove(testCase);
        }
        checkStop();
    }

    public void checkStop() {
        boolean canStop;
        synchronized (this) {
            canStop = monitoredCases.isEmpty() && monitoredBundles.isEmpty();
        }
        if (canStop && !hasStopped) {
            hasStopped = true;
            final Bundle thisBundle = FrameworkUtil.getBundle(this.getClass());
            if (thisBundle == null) {
                log.severe("Failed to stop framework... could not get current bundle");
                return;
            }
            final BundleContext thisContext = thisBundle.getBundleContext();
            if (thisContext == null) {
                log.severe("Failed to stop framework... could not get current bundle context");
                return;
            }
            final Bundle fwBundle = thisContext.getBundles()[0];
            try {
                fwBundle.stop();
            } catch (Exception x) {
                log.severe("Failed to stop framework... " + x.getMessage());
            }
        }
    }
}
