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



/**
 *
 * @author John McCrae
 */
public class CaseReport {

    private final String className;
    private final String name;
    private final double time;
    private final Result result;

    public CaseReport(String className, String name, double time, Result result) {
        this.className = className;
        this.name = name;
        this.time = time;
        this.result = result;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CaseReport other = (CaseReport) obj;
        if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (Double.doubleToLongBits(this.time) != Double.doubleToLongBits(other.time)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.time) ^ (Double.doubleToLongBits(this.time) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        if(result == null || result.isEmpty()) {
            return "<testcase classname=\""+this.className+"\" name=\""+this.name+"\" time=\""+time+"\"/>";
        } else {
            return "<testcase classname=\""+this.className+"\" name=\""+this.name+"\" time=\""+time+"\">\n" + result.toString() + "\n</testcase>";
        }
    }

    public Result getResult() {
        return result;
    }
    
    
    public static interface Result {
        public boolean isEmpty();
    }
    
    public static class Success implements Result {

        public boolean isEmpty() {
            return true;
        }
        
    }
    
    public static class Failure implements Result {
        private final String message;

        public Failure(String message) {
            this.message = message;
        }

        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            return "<failure type=\"eu.monnetproject.framework.test.AssertionError\">" + escapeXml(message)+"</failure>";
        }
    }
    
     /**
     * Escape XML
     * @param str The string
     * @return The string as valid XML
     */
    public static String escapeXml(String str) {
        return str.replaceAll("\"", "&quot;").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;");
    }
    
    public static class Error implements Result {
        private final Throwable cause;

        public Error(Throwable cause) {
            this.cause = cause;
        }

        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("<error type=\"").append(cause.getClass().getName()).append("\">\n");
            for (StackTraceElement traceElem : cause.getStackTrace()) {
                sb.append(escapeXml(traceElem.toString())).append("\n");
            }
            sb.append("</error>");
            return sb.toString();
        }
        
        
        
    }
}
