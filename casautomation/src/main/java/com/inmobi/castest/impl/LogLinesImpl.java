package com.inmobi.castest.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.inmobi.castest.api.LogLines;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 16/3/15.
 */
@Getter
@Setter
public class LogLinesImpl implements LogLines {
    private List<String> logLines;

    public LogLinesImpl(BufferedReader bufferedReader) {
        if (null == bufferedReader) {
            logLines = null;
        }

        try {
            logLines = new ArrayList<>();
            String line;
            while (StringUtils.isNotEmpty(line = bufferedReader.readLine())) {
                logLines.add(line);
            }
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    @Override
    public String applyRegex(String ...regexes) {
        if (null == logLines || logLines.size() > 1) {
            System.out.println("ERROR: Cannot apply regex to multiple log lines. "
                    + "Please grep for a more unique log line.");
            return null;
        } else if (0 == regexes.length) {
            System.out.println("ERROR: No regex found");
            return null;
        }

        String searchString = logLines.get(0);
        Matcher matcher = null;
        for (String regex : regexes) {
            matcher = Pattern.compile(regex).matcher(searchString);
            if (matcher.find()) {
                searchString = matcher.group();
            } else {
                searchString = null;
                break;
            }
        }

        return searchString;
    }

    @Override
    public List<String> getAllLogLines() {
        return logLines;
    }

    @Override
    public void printAllLogLines() {
        for (String logLine : logLines) {
            System.out.println(logLine);
        }
    }

    @Override
    public boolean isNotEmpty() {
        if (null == logLines) {
            return false;
        } else {
            return logLines.size()>0;
        }
    }

    @Override
    public int getSize() {
        if (null == logLines) {
            return 0;
        } else {
            return logLines.size();
        }
    }
}
