package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
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

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ThriftRequestMaker {
    private static final URLCodec            urlCodec = new URLCodec();

    public static void main(String[] args) throws Exception {
        /*
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
    list.add((short)15);
    adPoolRequest.setSelectedSlots(list);
    List<SupplyCapability> supplyCapabilities = new ArrayList<SupplyCapability>();
    supplyCapabilities.add(SupplyCapability.BANNER);
    adPoolRequest.setSupplyCapabilities(supplyCapabilities);
    adPoolRequest.setRequestedAdCount((short)1);
    */
    String rawContent = "%250B%2500%2501%2500%2500%2500%25244b13d851-0145-1cb3-c0a0-90b11c1302a2%250B%2500%2502%2500%2500%2500%250D106.219.11.44%250C%2500%2503%250A%2500%2501%2500%2504%25E7%2581%25EB%25C8%25C0%2518%250B%2500%2502%2500%2500%2500Dhttps%253A%252F%252Fplay.google.com%252Fstore%252Fapps%252Fdetails%253Fid%253Dcom.outfit7.talkingtom%2504%2500%2503%253F%2584z%25E1G%25AE%2514%257B%2504%2500%2504%2500%2500%2500%2500%2500%2500%2500%2500%2504%2500%2505%2500%2500%2500%2500%2500%2500%2500%2500%250B%2500%2506%2500%2500%2500%2B27a2658ff092431e912e470f1519806e%250B%2500%2507%2500%2500%2500%2B4028cb9029c4e5f70129d6655bbd04bf%2508%2500%2508%2500%2500%2500%2502%2508%2500%2509%2500%2500%2500%2502%250E%2500%250A%2508%2500%2500%2500%2502%2500%2500%2500%2501%2500%2500%2500%250A%250E%2500%250B%2508%2500%2500%2500%2501%2500%2500%2500%2508%2500%250C%2500%2504%250B%2500%2501%2500%2500%2500%2583Mozilla%252F5.0%2B%2528Linux%253B%2BU%253B%2BAndroid%2B4.1.2%253B%2Ben-gb%253B%2BSM-T211%2BBuild%252FJZO54K%2529%2BAppleWebKit%252F534.30%2B%2528KHTML%252C%2Blike%2BGecko%2529%2BVersion%252F4.0%2BSafari%252F534.30%250A%2500%2502%2500%2500%2500%2500%2500%2500%25B9%2529%250A%2500%2503%2500%2500%2500%2500%2500%2500%2500%2507%250A%2500%2504%2500%2500%2500%2500%2500%2500%2500%2503%250B%2500%2505%2500%2500%2500%25034.1%250A%2500%2506%2500%2500%2500%2500%2500%2500%2500%2507%250B%2500%2507%2500%2500%2500%25030.0%250A%2500%2508%2500%2500%2500%2500%2500%2500%25BDW%2508%2500%2509%2500%2500%2500%2502%2500%250C%2500%2505%250A%2500%2501%2500%2500%2500%2500%2500%2500%2501%2560%2508%2500%2502%2500%2500%2500%2501%2500%2506%2500%2506%2500%2501%2508%2500%2507%2500%2500%2500%2501%2502%2500%2508%2500%2502%2500%2509%2500%250F%2500%250B%2508%2500%2500%2500%2503%2500%2500%2500%2501%2500%2500%2500%2503%2500%2500%2500%2502%250C%2500%250C%2508%2500%2501%2500%2500%2500%250B%250B%2500%2502%2500%2500%2500%2502IN%2508%2500%2503%2500%2500%2500%2505%2500%250C%2500%250D%250D%2500%2501%2508%250B%2500%2500%2500%2502%2500%2500%2500%2502%2500%2500%2500%2528e9ff5d39084067d82b658c63d3873a2e1dd55a42%2500%2500%2500%2500%2500%2500%2500%2B3fa6c635c7de1d189f6f091ce0ed6b3e%2500%250C%2500%250E%2500%250F%2500%250F%2506%2500%2500%2500%2501%2500%250F%2502%2500%2512%2500%250C%2500%2513%2508%2500%2501%2500%2500%2500%2501%2508%2500%2502%2500%2500%2500%2500%2508%2500%2503%2500%2500%2500%2502%2508%2500%2504%2500%2500%2501%2592%2508%2500%2505%2500%2500%2500%2501%2500%2508%2500%2514%2500%2500%2500%2500%250A%2500%2515%2500%2500%2500%2500%2500%2500%2500%2505%2500";
    String urlSafe = urlCodec.decode(rawContent);
    byte[] decodedContent = urlCodec.decode(urlSafe.getBytes(StandardCharsets.US_ASCII));
    //LOG.debug("Decoded String : {}", decodedContent.toString());
    TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
    AdPoolRequest adPoolRequest = new AdPoolRequest();
    try {
        tDeserializer.deserialize(adPoolRequest, decodedContent);
        //thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters,
        //      dst);
    }
    catch (TException ex) {
        //terminationReason = ServletHandler.thriftParsingError;
        //LOG.error(traceMarker, "Error in de serializing thrift ", ex);
    }
    System.out.println(adPoolRequest);

    System.out.println("Request is : " + adPoolRequest);
    sendClientGet(adPoolRequest);

}


    // HTTP GET request
    private static void sendClientGet(AdPoolRequest adPoolRequest) throws Exception {

        String url = "http://localhost:8800/backfill?adPoolRequest=";

        HttpClient client = new DefaultHttpClient();

        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] requestContent = serializer.serialize(adPoolRequest);
        URLCodec urlCodec = new URLCodec();
        String content = new String(urlCodec.encode(urlCodec.encode(requestContent)));
        //request.addHeader("adPoolRequest", content);
        url = url + content;
        AdPoolRequest adPoolRequest1 = new AdPoolRequest();
        TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
        try {
            tDeserializer.deserialize(adPoolRequest1, urlCodec.decode(content.getBytes()));
        } catch (TException ex) {
            ex.printStackTrace();
        }


        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);

        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println(result.toString());

    }

    private static void sendPost(AdPoolRequest adPoolRequest) throws Exception {

        String targetUrl = "http://localhost:8800/rtbdFill";
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] urlParameters = serializer.serialize(adPoolRequest);
        System.out.println(urlParameters.length);

        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetUrl);
            System.out.println(" Url Path is " + url.getPath());
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-thrift");

            connection.setRequestProperty("Content-Length", "" +
                    123456);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.write(urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response	
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
