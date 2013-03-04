/**
 * **************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************
 */
package eu.monnetproject.framework.test.xmlreporter;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;

/**
 *
 * @author John McCrae
 */
public class Report {

    private int errors = 0;
    private int failures = 0;
    private int tests = 0;
    private String hostName;
    private final String name;
    private double time = 0.0;
    private final Date timeStamp = new Date();
    private final LinkedList<CaseReport> caseReports = new LinkedList<CaseReport>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public Report(String name) {
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException x) {
            this.hostName = "unknown";
        }
        this.name = name;
    }

    public void addCase(CaseReport report) {
        caseReports.add(report);
        tests++;
        if (report.getResult() instanceof CaseReport.Failure) {
            failures++;
        } else if (report.getResult() instanceof CaseReport.Error) {
            errors++;
        }
    }

    public void timeStamp() {
        time = ((double) ((new Date()).getTime() - timeStamp.getTime())) / 1000.0;
    }

    /**
     * Escape XML
     *
     * @param str The string
     * @return The string as valid XML
     */
    public static String escapeXml(String str) {
        return str.replaceAll("\"", "&quot;").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;");
    }

    public void write(PrintWriter writer, String stderr, String stdout) {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        writer.println("<testsuite errors=\"" + errors + "\" failures=\"" + failures
                + "\" hostname=\"" + hostName + "\" name=\"" + name
                + "\" tests=\"" + tests + "\" time=\"" + time
                + "\" timestamp=\"" + sdf.format(timeStamp) + "\">");
        writer.println("  <properties>");
        final Enumeration propNames = System.getProperties().propertyNames();
        while (propNames.hasMoreElements()) {
            final String propName = propNames.nextElement().toString();
            final String propVal = System.getProperty(propName);
            writer.println("    <property name=\"" + propName + "\" value=\"" + escapeXml(propVal) + "\"/>");
        }
        writer.println("  </properties>");
        for (CaseReport caseReport : caseReports) {
            writer.println("  " + caseReport.toString());
        }
        writer.print("  <system-out><![CDATA[");
        writer.println(escapeXml(stdout));
        writer.println("]]></system-out>");
        writer.print("  <system-err><![CDATA[");
        writer.println(escapeXml(stderr));
        writer.println("]]></system-err>");
        writer.println("</testsuite>");
    }
}
