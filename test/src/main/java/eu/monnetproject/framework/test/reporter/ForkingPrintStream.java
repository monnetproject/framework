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
package eu.monnetproject.framework.test.reporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 *
 * @author John McCrae
 */
public class ForkingPrintStream extends PrintStream {
    private final PrintStream fork;

    public ForkingPrintStream(File file, String string, PrintStream fork) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, string);
        this.fork = fork;
    }

    public ForkingPrintStream(File file, PrintStream fork) throws FileNotFoundException {
        super(file);
        this.fork = fork;
    }

    public ForkingPrintStream(String string, String string1, PrintStream fork) throws FileNotFoundException, UnsupportedEncodingException {
        super(string, string1);
        this.fork = fork;
    }

    public ForkingPrintStream(String string, PrintStream fork) throws FileNotFoundException {
        super(string);
        this.fork = fork;
    }

    public ForkingPrintStream(OutputStream out, boolean bln, String string, PrintStream fork) throws UnsupportedEncodingException {
        super(out, bln, string);
        this.fork = fork;
    }

    public ForkingPrintStream(OutputStream out, boolean bln, PrintStream fork) {
        super(out, bln);
        this.fork = fork;
    }

    public ForkingPrintStream(OutputStream out, PrintStream fork) {
        super(out);
        this.fork = fork;
    }


    @Override
    public void write(byte[] bytes) throws IOException {
        fork.write(bytes);
        super.write(bytes);
    }

    @Override
    public void write(int i) {
        fork.write(i);
        super.write(i);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) {
        fork.write(bytes, i, i1);
        super.write(bytes, i, i1);
    }
}
