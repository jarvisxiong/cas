package com.inmobi.castest.commons.generichelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.inmobi.castest.utils.common.CasServerDetails;

public class LogParserHelper {
    public static String logParser(final String... logSearchString) throws ClientProtocolException, IOException {
        String string = "search=";

        for (int x = 0; x < logSearchString.length - 1; x++) {
            string += logSearchString[x] + "@";
        }
        string += logSearchString[logSearchString.length - 1];
        string += "&logFilePath=/opt/mkhoj/logs/cas/debug/";
        System.out.println(string);
        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost(CasServerDetails.getLogParserUrl());
        final StringEntity input = new StringEntity(string);
        input.setContentType("application/json");
        post.setEntity(input);
        final HttpResponse response = client.execute(post);
        final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = new String();
        while ((line = rd.readLine()) != null) {
            System.out.println("Log Parser Output : " + line);
            return line;
        }
        return line;

    }
}
