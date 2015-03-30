package com.inmobi.adserve.channels.util.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionBlock {
    private static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * 
     * @param aThrowable
     * @return
     */
    public static String getStackTrace(final Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * 
     * @param aThrowable
     * @return
     */
    public static String getCustomStackTrace(final Throwable aThrowable) {
        // add the class name and any message passed to constructor
        final StringBuilder result = new StringBuilder();
        result.append(aThrowable.toString());
        result.append(NEW_LINE);

        // add each element of the stack trace
        for (final StackTraceElement element : aThrowable.getStackTrace()) {
            result.append(element);
            result.append(NEW_LINE);
        }
        return result.toString();
    }

    /** Demonstrate output. */
    public static void main(final String... aArguments) {
        final Throwable throwable = new IllegalArgumentException("Blah");
        System.out.println(getStackTrace(throwable));
        System.out.println(getCustomStackTrace(throwable));
    }


}
