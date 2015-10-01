package com.inmobi.castest.utils.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.AdPoolService;
import com.inmobi.adserve.adpool.ResponseFormat;

public class NewPostRequest {

    public static ResponseBuilder sendPost(final AdPoolRequest adPoolRequest, final String servletName,
            final String testCaseName) throws Exception {

        // backfill
        // rtbdFill
        // "http://10.14.127.185:8800/"
        final String hostIp = CasServerDetails.getCasServerEndPoint();
        final String targetUrl = hostIp + servletName;
        final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        final byte[] urlParameters = serializer.serialize(adPoolRequest);

        ResponseBuilder responseBuilder = new ResponseBuilder();

        int responseStatusCode = 204;
        byte[] responseData = null;
        ResponseFormat responseFormat = ResponseFormat.HTML;

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetUrl);
            System.out.println(" Url Path is " + url.getPath());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-thrift");

            /*
             * This is an automation requirement that adpool requests would have
             * have in production
             */
            /* This line here => */connection.setRequestProperty("x-mkhoj-automation", testCaseName);

            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept", "*/*");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(urlParameters);
            wr.flush();
            wr.close();

            // Get Response
            final InputStream is = connection.getInputStream();

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int next = is.read();
            while (next > -1) {
                bos.write(next);
                next = is.read();
            }
            bos.flush();
            final byte[] result = bos.toByteArray();
            System.out.println("Data is " + new String(result) + "\n");

            responseStatusCode = connection.getResponseCode();
            responseData = result;
            responseFormat = adPoolRequest.getResponseFormatDeprecated();

            if (new String(result) != null && new String(result).length() != 0 && servletName.equals("rtbdFill")) {
                final AdPoolResponse adPoolResponse = new AdPoolResponse();
                final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                tDeserializer.deserialize(adPoolResponse, result);
                System.out.println("AdPool Response is" + adPoolResponse.toString());
                System.out.println();
            }

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        responseBuilder =
                responseBuilder.setStatusCode(responseStatusCode).setResponseData(responseData)
                        .setResponseFormat(responseFormat).build();

        if (servletName.equals("rtbdFill")) {
            responseBuilder.setIsRtbd(true);
        }

        return responseBuilder;
    }

    public static AdPoolResponse sendPost(final AdPoolRequest adPoolRequest, final String testCaseName)
            throws Exception {

        final String hostIp = BrandServerDetails.getBrandServerEndPoint();
        final String targetUrl = hostIp + "phoenix/network"; // http://10.14.118.190:8080/phoenix/network

        try {
            final TTransport transport = new THttpClient(targetUrl);
            final TFramedTransport framedTransport = new TFramedTransport(transport);
            final TBinaryProtocol protocol = new TBinaryProtocol(framedTransport);
            final AdPoolService.Client client = new AdPoolService.Client(protocol);
            System.out.println("Sending request: " + adPoolRequest.toString());
            AdPoolResponse adPoolResponse = new AdPoolResponse();
            adPoolResponse = client.getAds(adPoolRequest);
            transport.close();

            System.out.println(adPoolResponse);

            return adPoolResponse;
        } catch (final TException ex) {
            ex.printStackTrace();
        }
        // try {
        // // Create connection
        // url = new URL(targetUrl);
        // System.out.println(" Url Path is " + url.getPath());
        // connection = (HttpURLConnection) url.openConnection();
        // connection.setRequestMethod("POST");
        // connection.setRequestProperty("Content-Type", "application/x-thrift");
        //
        // // connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.length));
        // // connection.setRequestProperty("Content-Language", "en-US");
        // // connection
        // // .setRequestProperty(
        // // "User-Agent",
        // //
        // "Mozilla/5.0 (Linux; U; Android 2.2.2; es-us; SPH-M820-BST Build/FROYO) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2");
        // // connection.setRequestProperty("Accept", "*/*");
        //
        // connection.setUseCaches(false);
        // connection.setDoInput(true);
        // connection.setDoOutput(true);
        //
        // // Send request
        // final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        // wr.write(urlParameters);
        // wr.flush();
        // wr.close();
        //
        // // Get Response
        // final InputStream is = connection.getInputStream();
        //
        // final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // int next = is.read();
        // while (next > -1) {
        // bos.write(next);
        // next = is.read();
        // }
        // bos.flush();
        // final byte[] result = bos.toByteArray();
        // System.out.println("Data is " + new String(result) + "\n");
        //
        // responseStatusCode = connection.getResponseCode();
        // responseData = result;
        // responseFormat = adPoolRequest.getResponseFormatDeprecated();
        //
        // } catch (final Exception e) {
        // e.printStackTrace();
        // } finally {
        // if (connection != null) {
        // connection.disconnect();
        // }
        // }
        //
        // responseBuilder =
        // responseBuilder.setStatusCode(responseStatusCode).setResponseData(responseData)
        // .setResponseFormat(responseFormat).build();
        //
        // System.out.println(responseBuilder.getResponseData());
        //
        // return responseBuilder;
        return null;
    }
}
