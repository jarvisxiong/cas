package com.inmobi.adserve.channels.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * This is a Base class for reporting Adapters for third party Ad networks.
 */
public abstract class BaseReportingImpl implements ReportingInterface {
    private static final Logger            LOG                   = LoggerFactory.getLogger(BaseReportingImpl.class);

    private static HashMap<String, Double> currencyConversionMap = new HashMap<String, Double>();

    public String invokeUrl(final String url, final String soapMessage) throws ServerException {
        String responseString = "";
        try {
            LOG.debug("inside invokeUrl with url {}", url);
            URL urlObject;
            urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            // Setting connection and read timeout to 5 min
            connection.setReadTimeout(300000);
            connection.setConnectTimeout(300000);
            if (soapMessage != null) {
                connection.setDoOutput(true);
                connection.connect();
                OutputStream out = null;
                Writer wout = null;
                try {
                    out = connection.getOutputStream();
                    wout = new OutputStreamWriter(out);
                    wout.write(soapMessage);
                }
                catch (IOException ioe) {
                    LOG.info("Error in Httpool invokeHTTPUrl : {}", ioe);
                }
                finally {
                    out.close();
                    wout.flush();
                    wout.close();
                    connection.disconnect();
                }
            }
            int statusCode = connection.getResponseCode();
            LOG.debug("status code is {}", statusCode);
            if (statusCode != 200) {
                throw new ServerException("Erroneous Status Code");
            }
            InputStream inp = connection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inp));
            String inputLine;
            while ((inputLine = buffer.readLine()) != null) {
                LOG.debug("response line is {}", inputLine);
                responseString = responseString + inputLine + "\n";
            }
        }

        catch (MalformedURLException exception) {
            LOG.error("malformed url {}", exception);
            throw new ServerException("Invalid Url");
        }
        catch (IOException exception) {
            LOG.info("io error while reading response {}", exception);
            throw new ServerException("Error While Reading Response");
        }
        return responseString;
    }

    protected double getCurrencyConversionRate(final String currencyId, String queryDate, final Connection connection) {
        String currencyDateKey = currencyId + "_" + queryDate;
        if (currencyConversionMap.containsKey(currencyDateKey)) {
            return currencyConversionMap.get(currencyDateKey);
        }
        queryDate = queryDate + " 00:00:00";
        String query = "select conversion_rate from currency_conversion_rate where start_date<='" + queryDate
                + "' and end_date>='" + queryDate + "' and currency_id='" + currencyId.toUpperCase() + "' limit 1";
        ResultSet resultSet = null;
        Double conversionRate;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();
            resultSet.next();
            conversionRate = resultSet.getDouble("conversion_rate");
            currencyConversionMap.put(currencyDateKey, conversionRate);
            resultSet.close();
        }
        catch (SQLException ex) {
            LOG.info("Getting Sql Exception {}", ex);
            return -1;
        }
        LOG.debug("returning after collecting data from currency conversion rate table ");
        return conversionRate;
    }

    protected String invokeHTTPUrl(final String url) throws ServerException, ClientProtocolException, IOException {
        String retStr = null;
        LOG.debug("url is {}", url);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new ServerException("Erroneous status code");
        }
        InputStream is = response.getEntity().getContent();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1;) {
            buffer.append(new String(b, 0, n));
        }
        retStr = buffer.toString();
        return retStr;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) throws Exception {
        LOG.info("Site wise reporting not configured for {}", getName());
        return null;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        return null;
    }

    protected boolean decodeBlindedSiteId(final String blindedSiteId, final ReportResponse.ReportRow row) {
        UUID uuid;
        try {
            uuid = UUID.fromString(blindedSiteId);
        }
        catch (Exception e) {
            return false;
        }
        row.siteIncId = uuid.getLeastSignificantBits();
        row.adGroupIncId = uuid.getMostSignificantBits();
        return true;
    }
}