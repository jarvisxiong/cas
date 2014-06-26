package com.inmobi.adserve.channels.server.requesthandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.DecoderException;
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
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.Site;
import com.inmobi.adserve.adpool.SupplyCapability;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.inmobi.types.ContentRating;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

public class ThriftRequestMaker {
    private static final URLCodec urlCodec = new URLCodec();
    static AtomicInteger atomicInteger = new AtomicInteger();

    private static String getImpressionId() {
        String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), 12)).toString();
        String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, (short)2003)).toString();
        String uuidWithCyclicCounter = (WilburyUUID.setCyclicCounter(uuidMachineKey, (byte) atomicInteger.getAndIncrement())).toString();
        return (WilburyUUID.setDataCenterId(uuidWithCyclicCounter, (byte) 123)).toString();
    }

    public static void main(final String[] args) throws Exception {
        /*
        Integer adIncId = WilburyUUID.getIntKey("227ee495-0146-1000-2251-e49510070000");
        System.out.println("ad_inc_id " + adIncId);
        */
        System.out.println(WilburyUUID.getCyclicCounter(getImpressionId()));
        System.out.println(WilburyUUID.getCyclicCounter(getImpressionId()));

        AdPoolRequest adPoolRequest = createAdPoolRequest();
        System.out.println("Request is : " + adPoolRequest);
       // sendBackFillGet(adPoolRequest);
        sendUMPPost(adPoolRequest);

    }


    private static AdPoolRequest createAdPoolRequest() {
        Site site = new Site();
        site.setSiteIncId(2);
        site.setSiteUrl("siteurl");
        site.setCpcFloor(0.03);
        site.setEcpmFloor(.3);
//        site.setSiteId("4028cb1334ef46a9013578fe1c1f18fc");
        //tango siteid
        site.setSiteId("69d6ab27d03f407f9f6fa9c5fad77afd");
//        site.setSiteId("b79bb8cbab6e479bbd1c14b2dd448f7e");
        site.setPublisherId("sitepub");
        site.setContentRating(ContentRating.PERFORMANCE);
        site.setInventoryType(InventoryType.APP);
        Set<Integer> tags = new HashSet<Integer>();
        tags.add(10);
        tags.add(11);
        tags.add(13);
        tags.add(9);
        tags.add(16);
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
        states.add(1); geo.setStateIds(states);
        Set<Integer> zips = new HashSet<Integer>();
        zips.add(1); geo.setZipIds(zips);

        AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setRequestId("requestId");
        adPoolRequest.setRemoteHostIp("10.14.118.13");
        adPoolRequest.setSite(site);
        adPoolRequest.setSegmentId(0);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);

        IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setIntegrationType(IntegrationType.ANDROID_SDK);
        integrationDetails.setIntegrationVersion(370);

        adPoolRequest.setRequestedAdType(RequestedAdType.ADHESION);
        adPoolRequest.setResponseFormat(ResponseFormat.NATIVE); adPoolRequest.setGeo(geo);
        adPoolRequest.setIpFileVersion(1234);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        List<Short> list = new ArrayList<Short>();
        list.add((short)9);
        adPoolRequest.setSelectedSlots(list);
        List<SupplyCapability> supplyCapabilities = new ArrayList<>();
        supplyCapabilities.add(SupplyCapability.BANNER);
        adPoolRequest.setSupplyCapabilities(supplyCapabilities);
        adPoolRequest.setRequestedAdCount((short)1);
        return adPoolRequest;
    }

    private static AdPoolRequest createAdPoolRequestFromString() throws DecoderException {
        String rawContent = "%250B%2500%2501%2500%2500%2500%25244b13d85b-0145-1cb3-c1dc-90b11c1302a2%250B%2500%2502%2500%2500%2500%250E81.247.219.201%250C%2500%2503%250A%2500%2501%2500%2500%2500%2500%2500%2502V%25D7%250B%2500%2502%2500%2500%2500Jhttps%253A%252F%252Fplay.google.com%252Fstore%252Fapps%252Fdetails%253Fid%253Dcom.g6677.android.girlsgames%2504%2500%2503%253F%2584z%25E1G%25AE%2514%257B%2504%2500%2504%253F%25D9%2599%2599%2599%2599%2599%259A%2504%2500%2505%2500%2500%2500%2500%2500%2500%2500%2500%250B%2500%2506%2500%2500%2500%2B7f4dd39064984c4f868134702ded2f74%250B%2500%2507%2500%2500%2500%2Bb64375edb35043f9baade9a16b726d88%2508%2500%2508%2500%2500%2500%2502%2508%2500%2509%2500%2500%2500%2502%250E%2500%250A%2508%2500%2500%2500%2509%2500%2500%2500%2501%2500%2500%2500%250D%2500%2500%2500n%2500%2500%2500%25E0%2500%2500%2500%25E4%2500%2500%2500%25EE%2500%2500%2500%25F9%2500%2500%2501Q%2500%2500%2501g%250E%2500%250B%2508%2500%2500%2500%2502%2500%2500%2500%2515%2500%2500%2500%2519%2500%250C%2500%2504%250B%2500%2501%2500%2500%2500%258AMozilla%252F5.0%2B%2528Linux%253B%2BU%253B%2BAndroid%2B4.2.2%253B%2Bfr-fr%253B%2BGT-I9195%2BBuild%252FJDQ39%2529%2BAppleWebKit%252F534.30%2B%2528KHTML%252C%2Blike%2BGecko%2529%2BVersion%252F4.0%2BMobile%2BSafari%252F534.30%250A%2500%2502%2500%2500%2500%2500%2500%2500%25BAm%250A%2500%2503%2500%2500%2500%2500%2500%2500%2500%2507%250A%2500%2504%2500%2500%2500%2500%2500%2500%2500%2503%250B%2500%2505%2500%2500%2500%25034.2%250A%2500%2506%2500%2500%2500%2500%2500%2500%2500%2507%250B%2500%2507%2500%2500%2500%25030.0%250A%2500%2508%2500%2500%2500%2500%2500%2500%25CEk%2508%2500%2509%2500%2500%2500%2501%2500%250C%2500%2505%250A%2500%2501%2500%2500%2500%2500%2500%2500%2502B%2508%2500%2502%2500%2500%2500%2500%2500%2506%2500%2506%2500%2501%2508%2500%2507%2500%2500%2500%2501%2502%2500%2508%2500%2502%2500%2509%2500%250F%2500%250B%2508%2500%2500%2500%2504%2500%2500%2500%2500%2500%2500%2500%2501%2500%2500%2500%2503%2500%2500%2500%2502%250C%2500%250C%2508%2500%2501%2500%2500%2500o%250B%2500%2502%2500%2500%2500%2502BE%2508%2500%2503%2500%2500%2500%2505%2500%250C%2500%250D%250D%2500%2501%2508%250B%2500%2500%2500%2502%2500%2500%2500%2502%2500%2500%2500%25280551ccd1715e488e95279b91f18af5c2f1d6705d%2500%2500%2500%2500%2500%2500%2500%2Bf0a0cdfb37a32a88e1f68225eb44b284%2500%250C%2500%250E%2500%250F%2500%250F%2506%2500%2500%2500%2501%2500%250F%2502%2500%2512%2500%250C%2500%2513%2508%2500%2501%2500%2500%2500%2501%2508%2500%2502%2500%2500%2500%2500%2508%2500%2503%2500%2500%2500%2502%2508%2500%2504%2500%2500%2501%259B%2508%2500%2505%2500%2500%2500%2501%2500%2508%2500%2514%2500%2500%2500%2500%250A%2500%2515%2500%2500%2500%2500%2500%2500%2500%2505%2500";
        String urlSafe = urlCodec.decode(rawContent);
        byte[] decodedContent = urlCodec.decode(urlSafe.getBytes(StandardCharsets.US_ASCII));
        // LOG.debug("Decoded String : {}", decodedContent.toString());
        TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
        AdPoolRequest adPoolRequest = new AdPoolRequest();
        try {
            tDeserializer.deserialize(adPoolRequest, decodedContent);
        }
        catch (TException ex) {
            System.out.println(ex);
        }
        return  adPoolRequest;
    }

    // HTTP GET request
    private static void sendBackFillGet(final AdPoolRequest adPoolRequest) throws Exception {

        String url = "http://localhost:8800/backfill?adPoolRequest=";

        HttpClient client = new DefaultHttpClient();

        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] requestContent = serializer.serialize(adPoolRequest);
        URLCodec urlCodec = new URLCodec();
        String content = new String(urlCodec.encode(urlCodec.encode(requestContent)));
        // request.addHeader("adPoolRequest", content);
        url = url + content;
        AdPoolRequest adPoolRequest1 = new AdPoolRequest();
        TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
        try {
            tDeserializer.deserialize(adPoolRequest1, urlCodec.decode(content.getBytes()));
        }
        catch (TException ex) {
            ex.printStackTrace();
        }

        HttpGet request = new HttpGet(url);

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

    private static void sendUMPPost(final AdPoolRequest adPoolRequest) throws Exception {

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
            connection.setRequestProperty("x-mkhoj-tracer", "true");
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
