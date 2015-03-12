package com.inmobi.castest.commons.dbhelper;

/**
 * @author santosh.vaidyanathan
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QueryManager {

    public static void executeUpdateQuery(final String dbQuery) throws SQLException, ClassNotFoundException {

        final ConnectionManager connectionManager = new ConnectionManager();
        final Connection connection = connectionManager.getDBConnection();
        final String statement = dbQuery;

        final PreparedStatement pStatement = connection.prepareStatement(statement);
        System.out.println("***" + pStatement.executeUpdate() + " Row Updated***");
        connection.close();

    }

    public static ArrayList<Map> executeAndGetColumnsOutput(final String dbQuery) throws SQLException,
            ClassNotFoundException {
        System.out.println(dbQuery);
        final ConnectionManager connectionManager = new ConnectionManager();
        final Connection connection = connectionManager.getDBConnection();
        final String statement = dbQuery;

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
        }
        connection.close();
        System.out.println("result set size : " + data.size() + "\n" + data);
        return data == null ? null : data;
        // return data;
    }
}
