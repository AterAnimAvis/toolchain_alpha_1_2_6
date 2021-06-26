/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.github.ateranimavis.toploader.util.logging;

import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TracingPrintStream extends PrintStream {

    private static final int BASE_DEPTH = 4;
    private final Logger logger;

    public TracingPrintStream(Logger logger, PrintStream original) {
        super(original);
        this.logger = logger;
    }

    private void log(String s) {
        logger.info("{}{}", getPrefix(), s);
    }

    private static String getPrefix() {
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        StackTraceElement elem = elems[Math.min(BASE_DEPTH, elems.length - 1)]; // The caller is always at BASE_DEPTH, including this call.
        if (elem.getClassName().startsWith("kotlin.io.")) {
            elem = elems[Math.min(BASE_DEPTH + 2, elems.length - 1)]; // Kotlins IoPackage masks origins 2 deeper in the stack.
        } else if (elem.getClassName().startsWith("java.lang.Throwable")) {
            elem = elems[Math.min(BASE_DEPTH + 4, elems.length - 1)];
        }
        return "[" + elem.getClassName() + ":" + elem.getMethodName() + ":" + elem.getLineNumber() + "]: ";
    }

    @Override
    public void println(Object o) {
        log(String.valueOf(o));
    }

    @Override
    public void println(String s) {
        log(s);
    }

    @Override
    public void println(boolean x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        log(String.valueOf(x));
    }

    public static void inject() {
        System.setOut(new TracingPrintStream(LogManager.getLogger("STDOUT"), System.out));
        System.setErr(new TracingPrintStream(LogManager.getLogger("STDERR"), System.err));
    }
}
