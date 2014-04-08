package com.inmobi.adserve.channels.server.requesthandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import sun.net.www.protocol.http.HttpURLConnection;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.Carrier;
import com.inmobi.adserve.adpool.Device;
import com.inmobi.adserve.adpool.Geo;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.Site;
import com.inmobi.adserve.adpool.SupplyCapability;
import com.inmobi.types.ContentRating;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;


public class ThriftRequestMaker {

    public static void main(final String[] args) throws Exception {

        Site site = new Site();
        site.setSiteIncId(2);
        site.setSiteUrl("siteurl");
        site.setCpcFloor(0.03);
        site.setEcpmFloor(.3);
        site.setSiteId("site");
        site.setPublisherId("sitepub");
        site.setContentRating(ContentRating.PERFORMANCE);
        site.setInventoryType(InventoryType.APP);
        Set<Integer> tags = new HashSet<Integer>();
        tags.add(7);
        tags.add(32);
        site.setSiteTags(tags);
        site.setSiteTaxonomies(tags);

        Device device = new Device();
        device.setUserAgent("useragent");
        device.setModelId(2);
        device.setManufacturerId(2);
        device.setOsId(3);
        device.setOsMajorVersion("3.2");

        Carrier carrier = new Carrier();
        carrier.setCarrierId(123);

        Geo geo = new Geo();
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        Set<Integer> cities = new HashSet<Integer>();
        cities.add(1);
        geo.setCityIds(cities);
        Set<Integer> states = new HashSet<Integer>();
        states.add(1);
        geo.setStateIds(states);
        Set<Integer> zips = new HashSet<Integer>();
        zips.add(1);
        geo.setZipIds(zips);

        AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setRequestId("requestId");
        adPoolRequest.setRemoteHostIp("10.14.118.13");
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);

        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        adPoolRequest.setResponseFormat(ResponseFormat.XHTML);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setIpFileVersion(1234);
        List<Short> list = new ArrayList<Short>();
        list.add((short) 15);
        adPoolRequest.setSelectedSlots(list);
        List<SupplyCapability> supplyCapabilities = new ArrayList<SupplyCapability>();
        supplyCapabilities.add(SupplyCapability.BANNER);
        adPoolRequest.setSupplyCapabilities(supplyCapabilities);
        adPoolRequest.setRequestedAdCount((short) 1);
        System.out.println("Request is : " + adPoolRequest);
        sendClientGet(adPoolRequest);

    }

    // HTTP GET request
    private static void sendClientGet(final AdPoolRequest adPoolRequest) throws Exception {

        String url = "http://localhost:8800/backfill";

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] requestContent = serializer.serialize(adPoolRequest);
        URLCodec urlCodec = new URLCodec();
        String content = new String(urlCodec.encode(requestContent));
        request.addHeader("adPoolRequest", content);

        AdPoolRequest adPoolRequest1 = new AdPoolRequest();
        TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
        try {
            tDeserializer.deserialize(adPoolRequest1, urlCodec.decode(content.getBytes()));
        }
        catch (TException ex) {
            ex.printStackTrace();
        }

        HttpResponse response = client.execute(request);

        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(response.getEntity().getContent());
            BufferedReader rd = new BufferedReader(inputStreamReader);

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            System.out.println(result.toString());
        }
        finally {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }

    }

    private static void sendPost(final AdPoolRequest adPoolRequest) throws Exception {

        String targetUrl = "http://localhost:8800/rtbdFill";
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] urlParameters = serializer.serialize(adPoolRequest);
        System.out.println(urlParameters.length);

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetUrl);
            System.out.println(" Url Path is " + url.getPath());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-thrift");

            connection.setRequestProperty("Content-Length", "" + 123456);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(urlParameters);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int next = is.read();
            while (next > -1) {
                bos.write(next);
                next = is.read();
            }
            bos.flush();
            byte[] result = bos.toByteArray();
            AdPoolResponse adPoolResponse = new AdPoolResponse();
            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
            tDeserializer.deserialize(adPoolResponse, result);
            System.out.println("AdPool Response is" + adPoolResponse.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
