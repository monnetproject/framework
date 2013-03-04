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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import com.beinformed.framework.osgi.osgitest.TestCase;
import com.beinformed.framework.osgi.osgitest.TestMonitor;
import com.beinformed.framework.osgi.osgitest.TestSuite;

/**
 *
 * @author John McCrae
 */
public class XMLReporter implements TestMonitor {

    private final String TEST_DIR = "generated"+System.getProperty("file.separator")+"test-reports"+System.getProperty("file.separator");
    private final int POST_TEST_WAIT = 2000;
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private XMLReporterMonitor monitor = new XMLReporterMonitor();
    private Report currentReport = null;
    private Date currentTestStart;
    private CaseReport.Result currentResult;

    private PrintStream createPipe(PrintStream to, ByteArrayOutputStream baos) {
        return new ForkingPrintStream(baos, to);
    }

    public void beginTestRun() {
        // Ignore
    }

    public void beginTestSuite(TestSuite ts) {
        log.info("Beginning suite " + ts.getLabel());
        currentReport = new Report(ts.getLabel());
        monitor.monitorCase(ts);
    }

    public void beginTest(TestCase tc) {
        log.info("Beginning case " + tc.getLabel());
        currentTestStart = new Date();
    }

    public void assertion(boolean condition, String string) {
        if (currentResult != null) {
            if (!condition) {
                log.warning("Assertion failed: " + string);
            }
            return;
        }
        if (!condition) {
            log.warning("Assertion failed: " + string);
            currentResult = new CaseReport.Failure(string);
        }
    }

    public void error(String string, Throwable thrwbl) {
        if (thrwbl != null && thrwbl instanceof AssertionError) {
            return;
        }
        log.severe(string);
//        log.stackTrace(thrwbl);
        if (currentResult != null) {
            return;
        } else {
            log.warning("Error: " + thrwbl.getClass().getName());
            currentResult = new CaseReport.Error(thrwbl);
        }
    }

    private double getTestTime() {
        return (double) (new Date().getTime() - currentTestStart.getTime()) / 1000.0;
    }

    public void endTest(TestCase tc) {
        log.info("Ending test " + tc.getLabel());
        final CaseReport caseReport = new CaseReport(tc.getClass().getName(), tc.getLabel(), getTestTime(), currentResult);
        currentReport.addCase(caseReport);
        currentResult = null;
    }
    
    private final Object lock = new Object();

    public void endTestSuite(TestSuite ts) {
        log.info("Ending test suite " + ts.getLabel());
        currentReport.timeStamp();
        final File testDir = new File(TEST_DIR);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        final File out = new File(TEST_DIR + "TEST-" + ts.getLabel() + ".xml");
        final PrintWriter fileWriter;
        try {
            log.info("Writing file " + ts.getLabel());
            fileWriter = new PrintWriter(out);
            currentReport.write(fileWriter, "","");
            fileWriter.close();
        } catch (IOException ex) {
            log.severe("Could not write file");
            //log.stackTrace(ex);
        }
        monitor.caseFinished(ts);
        new Thread(new Runnable() {

            public void run() {
                synchronized(lock) {
                    try {
                        lock.wait(POST_TEST_WAIT);
                    } catch (InterruptedException ex) {
                    }
                    monitor.checkStop();
                }
            }
        }).start();
    }

    public void endTestRun() {
    }
    
}
