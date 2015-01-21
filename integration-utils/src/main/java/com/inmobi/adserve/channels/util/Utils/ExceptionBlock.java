package com.inmobi.adserve.channels.util.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionBlock {
    public static String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
