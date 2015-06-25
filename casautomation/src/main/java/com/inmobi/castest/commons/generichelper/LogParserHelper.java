package com.inmobi.castest.commons.generichelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inmobi.castest.api.LogLines;
import com.inmobi.castest.impl.LogLinesImpl;
import com.inmobi.castest.utils.common.CasServerDetails;

public class LogParserHelper {
    private static final String PASS = "PASS";
    private static final String FAIL = "FAIL";

    public static LogLines queryForLogs(final String logSearchString) throws IOException {
        final StringBuilder queryString = new StringBuilder();

        queryString.append("search=");
        queryString.append(logSearchString);
        queryString.append("&logFilePath=");
        queryString.append(CasServerDetails.getLogFilePath());

        System.out.println(queryString.toString());

        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost(CasServerDetails.getLogParserUrl());
        final StringEntity input = new StringEntity(queryString.toString());
        input.setContentType("application/json");
        post.setEntity(input);
        final HttpResponse response = client.execute(post);
        final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        final LogLines logLines = new LogLinesImpl(rd);
        if (logLines.isNotEmpty()) {
            System.out.println(logLines.getSize() + " log lines matched: " + logSearchString);
        } else {
            System.out.println("No log lines matched: " + logSearchString);
        }
        logLines.printAllLogLines();

        return logLines;
    }

    public static String logParser(final String... logSearchStrings) throws IOException {
        boolean result = true;

        for (final String logSearchString : logSearchStrings) {
            result &= queryForLogs(logSearchString).isNotEmpty();
        }

        return result ? PASS : FAIL;
    }
}
