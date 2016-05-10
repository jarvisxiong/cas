package com.inmobi.castest.commons.dbhelper;

import com.inmobi.castest.casconfenums.def.QueryConf;
import com.inmobi.castest.casconfenums.impl.E2EQueryConf;
import com.inmobi.castest.commons.generichelper.RepoRefreshHelper;
import com.inmobi.castest.commons.iohelper.YamlDataIOHelper;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * Created by navaneeth on 14/3/16.
 */
public class E2EDataGen {

    public static void Selectquerry(String Site_id, String testcase) throws SQLException {

        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.UPDATE_WAP_SITE, Site_id, testcase), true);

        if ((Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.SELECT_WAP_PUBLISHER_IX, Site_id, testcase), false)).equals(null)) {
            String PublisherID =
                Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.SELECT_WAP_PUBLISHER_ID, Site_id, testcase), false);
            Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSEERT_WAP_PUBLISHER_IX, PublisherID, testcase), true);
        } else {

            Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.UPDATE_WAP_PUBLISHER_IX, Site_id, testcase), true);
        }
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.UPDATE_PLACEMENT, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_SITE_ADVERTISER_PREFERENCE, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.UPDATE_PLACEMENT_TEMPLATE, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_SITE_TAGS, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSERT_SITE_TAGS, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_SITE_CATEGORY_MANUAL, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSERT_SITE_CATEGORY_MANUAL, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_SITE_CATEGORY_OFFFLINE, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSERT_SITE_CATEGORY_OFFFLINE, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_PRICING_REPOSITORY, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSERT_INTO_PRICING_REPOSITORY, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.DELETE_RULES_REPOSITORY, Site_id, testcase), true);
        Updatedbwithqueries(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.INSERT_RULES_REPOSITORY, Site_id, testcase), true);

    }

    public static String Updatedbwithqueries(String QueryString, boolean IsUpdate) {
        String Result = null;
        if (!IsUpdate) {
            try {

                ArrayList<Map> arraylist = QueryManager.executeAndGetColumnsOutput(QueryString);
                Result = arraylist.toString();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                QueryManager.executeUpdateQuery(QueryString);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return Result;
    }

    public static void UpdateUMPDB(String testcase) throws SQLException, FileNotFoundException {
        ArrayList<Map> Selected_Site_ids = null;
        try {
            Selected_Site_ids =
                QueryManager.executeAndGetColumnsOutput(E2EQueryConf.setE2EQuery(QueryConf.E2EQuery.SELECT_WAP_SITE_SITE_ID, "", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Iterator<Map> iterator = Selected_Site_ids.iterator();
        Map dummy;
        File configFile = new File("src/test/resources/Siteid.properties");
        if (!configFile.exists()) {
            configFile = new File("casautomation/src/test/resources/Siteid.properties");
            //   System.out.println("if");
        }
        final HashMap<String, String> testCasesFromDataFile = YamlDataIOHelper.readE2ETestIndex();
        System.out.println(testCasesFromDataFile);
        if (null != testcase) {
            if (iterator.hasNext()) {
                dummy = iterator.next();
                System.out.println(dummy.get("id").toString());
                try {
                    FileInputStream in = new FileInputStream(configFile);
                    Properties props = new Properties();
                    props.load(in);
                    in.close();

                    FileOutputStream out = new FileOutputStream(configFile);
                    props.setProperty(testcase, dummy.get("id").toString());
                    props.store(out, null);
                    out.close();
                } catch (FileNotFoundException ex) {
                    // file does not exist
                } catch (IOException ex) {
                    // I/O error
                }

                Selectquerry(dummy.get("id").toString(), testcase);

            }

        }


    }

    public static void main(final String[] args) throws Exception {

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.println("Enter a testcase as in the E2EtestPArams.yml: ");

        String testcase = reader.next();
//        System.out.println(testcase);

        UpdateUMPDB(testcase);

        UpdateDBWithWAPAdgroupE2EData.updateDBWithData(testcase, false);

        //UpdateDBWithWAPAdgroupE2EData.updateDBWithIXPackageData();

        RepoRefreshHelper.RefreshRepo();

    }

}
