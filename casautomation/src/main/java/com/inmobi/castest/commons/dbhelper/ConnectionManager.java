package com.inmobi.castest.commons.dbhelper;

/**
 * @author santosh.vaidyanathan
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.inmobi.castest.utils.common.CasDBDetails;
import com.inmobi.castest.utils.common.CasE2EDBDetails;

public class ConnectionManager {
    private static Connection connection = null;

    private static String DB_URL =null;

    public Connection getDBConnection() throws SQLException {
        connection = null;

        if(CasDBDetails.isE2e()){

            DB_URL="jdbc:postgresql://" + CasE2EDBDetails.getDbHost() + ":"+ CasE2EDBDetails.getDbPort() + "/" + CasE2EDBDetails.getDbName();
        }
        else{

            DB_URL="jdbc:postgresql://" + CasDBDetails.getDbHost() + ":"+ CasDBDetails.getDbPort() + "/" + CasDBDetails.getDbName();
        }

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("before");
            System.out.println(DB_URL);
            System.out.println(CasDBDetails.getDbUserName());
            System.out.println(CasDBDetails.getDbPassword());
            connection =
                    DriverManager.getConnection(DB_URL, CasDBDetails.getDbUserName(), CasDBDetails.getDbPassword());
            System.out.println(connection.toString());
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return connection;
    }

    public ResultSet executeQuery(final String query) throws SQLException {
        getDBConnection();
        final Statement stmt = connection.createStatement();
        System.out.println(query);
        // final String query2 = "select * from wap_channel_adgroup limit 2";
        final ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {

            final String adgroup_id = rs.getString(1);
            final String ad_id = rs.getString(2);
            System.out.println("1 : " + adgroup_id);
            System.out.println("2 : " + ad_id);
            System.out.println();
        }
        rs.close();
        stmt.close();
        connection.close();
        return rs;
    }

    public void setAdGroupFromDB() {

    }

    public static ArrayList<Map> executeAndGetColumnsOutput(final Object dbQuery) throws SQLException,
            ClassNotFoundException {

        final String statement = (String) dbQuery;
        new ConnectionManager().getDBConnection();
        final PreparedStatement pStatement = connection.prepareStatement(statement);
        final ResultSet rs = pStatement.executeQuery();
        final ResultSetMetaData rsmd = rs.getMetaData();

        final int numberOfColumns = rsmd.getColumnCount();
        System.out.println("columns : " + numberOfColumns);
        final ArrayList<Map> data = new ArrayList<Map>();
        Map eachLine;

        while (rs.next()) { // process results one row at a time
            eachLine = new HashMap<String, String>();
            for (int i = 1; i <= numberOfColumns; i++) {
                eachLine.put(rsmd.getColumnName(i), rs.getString(i));
            }
            data.add(eachLine);
            System.out.println(eachLine.keySet());
        }
        connection.close();

        return data == null ? null : data;
        // return data;
    }
}
