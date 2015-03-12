package com.inmobi.castest.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVParser {

    public void run() {

        final String csvFile =
                "/Users/santosh.vaidyanathan/Documents/workspace_integrated_auto/casautomation/src/test/resource/table_ix.csv";
        BufferedReader br = null;
        String line = "";
        final String cvsSplitBy = "#";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                final String[] column = line.split(cvsSplitBy);

                System.out.println("Query [tag= " + column[0] + " , query=" + column[1] + "]");

            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
    }

    public static void main(final String[] args) {
        // TODO Auto-generated method stub
        new CSVParser().run();
    }

}
