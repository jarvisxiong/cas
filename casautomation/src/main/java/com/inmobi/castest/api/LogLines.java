package com.inmobi.castest.api;

import java.util.List;

/**
 * Created by ishanbhatnagar on 16/3/15.
 */
public interface LogLines {

    public String applyRegex(final String... regexes);

    public List<String> getAllLogLines();

    public void printAllLogLines();

    public boolean isNotEmpty();

    public int getSize();
}
