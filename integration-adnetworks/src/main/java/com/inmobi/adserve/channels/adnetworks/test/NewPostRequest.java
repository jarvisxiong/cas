package com.inmobi.adserve.channels.adnetworks.test;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
import lombok.Data;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;


public class NewPostRequest {


    public static byte[] sendPost(AdPoolRequest adPoolRequest, String servletName, String hostIp) throws Exception {

        // backfill
        // rtbdFill
        // "http://10.14.127.185:8800/rtbdFill"
        String targetUrl = hostIp + servletName;
        System.out.println("here "+new UUID(1137895, 1412029991643993l).toString());
        System.out.println(new UUID(2, 139788).toString());
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] urlParameters = serializer.serialize(adPoolRequest);

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetUrl);
            System.out.println(" Url Path is " + url.getPath());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-thrift");

            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("x-mkhoj-tracer", "true");

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
            System.out.println("Data is " + new String(result));

            System.out.println("Connection response code :- " + connection.getResponseCode());
            System.out.println("Connection Response content :- " + connection.getResponseMessage());


            return result;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static void main(String args[]) {
        try {
            String s="test=%sandlive=%s";
            System.out.println("args = [" + String.format(s,"hei","hello") + "]");
            NewPostRequest uu = new NewPostRequest();
            String abc = uu.generateNativeWrapperForDCP("{\"id\":\"__7d9a9e8c23eae2f1b1c9815e37775d87__fae009e57e8f12dd09ea190e6ab211ff\",\"session\":\"v2_10aada74ddb1ccb0f85ae8344e72609c_38E31250-DD17-4C48-A9B9-15B8B9AE5D02_1434710282_1434710282_CNawjgYQqaM-\",\"list\":[{\"branding\":\"VIOLET GREY\",\"type\":\"video\",\"name\":\"Best Beauty Products For Radiant Skin \",\"categories\":[\"beauty\"],\"duration\":\"0\",\"thumbnail\":[{\"url\":\"http://images.taboola.com/taboola/image/fetch/f_jpg%2Cq_80%2Ch_200%2Cw_200%2Cc_fill%2Cg_face%2Ce_sharpen/http%3A%2F%2Fcdn.violetgrey.com%2Fviolet-files%2Ftop-flight%2Fskincare-essentials-radiant-complexions%2F_desktop%2Fskincare-essentials-radiant-complexions-archive.jpg\",\"width\":\"200\",\"height\":\"200\"}],\"created\":\"Wed, 21 Jan 2015 00:52:26 UTC\",\"description\":\"The top ten skin care creams & serums that are exceptional for the red carpet or braving winter weather. Read the file. \",\"views\":\"0\",\"id\":\"~~V1~~8259524400671349896~~awtMmE7IkmFtX9Ed-KfYI_db6cxAdGvP_WUvWd-r2DyM1I9edokBygxdOWpn8nxe2pt4v9ii1e2e75qEbg4lQg\",\"origin\":\"sponsored\",\"url\":\"http://api.taboola.com/1.1/json/inmobi/recommendations.notify-click?app.type=mobile&app.apikey=fc1200c7a7aa52109d762a9f005b149abef01479&response.id=__7d9a9e8c23eae2f1b1c9815e37775d87__fae009e57e8f12dd09ea190e6ab211ff&response.session=v2_10aada74ddb1ccb0f85ae8344e72609c_38E31250-DD17-4C48-A9B9-15B8B9AE5D02_1434710282_1434710282_CNawjgYQqaM-&item.id=%7E%7EV1%7E%7E8259524400671349896%7E%7EawtMmE7IkmFtX9Ed-KfYI_db6cxAdGvP_WUvWd-r2DyM1I9edokBygxdOWpn8nxe2pt4v9ii1e2e75qEbg4lQg&item.type=video&redir=http%3A%2F%2Fwww.violetgrey.com%2Fviolet-files%2Ftop-flight%2Fskincare-essentials-radiant-complexions%3Futm_source%3Dtaboola%26utm_medium%3Ddisy%26utm_campaign%3Dtaboola_ad%26utm_content%3Dtvf_best_skincare_for_radiant_complexions%26utm_term%3Dinmobi\"}]}");
            System.out.println(abc);
            String aesKey = "12345678abcdefg";
            String ivKey = "12345678abcdefg";
            UUID uuid = null;
            try {
                uuid = UUID.fromString("00000000-000f-ff08-0004-e5aedad12c53");
                System.out.print("MAble Site Id : " + uuid.getLeastSignificantBits());
                uuid = UUID.fromString("00000000-000c-58ec-0004-f257edd7f458");
                System.out.print("Mable Site Id : " + uuid.getLeastSignificantBits());
                uuid = UUID.fromString("00000000-000c-58ec-0004-ecd49f9a2916");
                System.out.print("Mable Site Id : " + uuid.getLeastSignificantBits());
                uuid = UUID.fromString("00000000-000c-5957-0004-feec0a0943db");
                System.out.print("Mable Site Id : " + uuid.getLeastSignificantBits());


                //"c5b50bc6-06dc-3d93-895e-2eda1b9a8666");
                System.out.println(Float.valueOf("2.3.4"));
            } catch (Exception e) {

            }
            System.out.print("Site Id : " + uuid.getLeastSignificantBits());
            System.out.println("Site/Adgroup Id : " + uuid.getMostSignificantBits());
            //5d90a5e9-cd9d-3fc3-b1af-10dd068c402b
            final byte[] byteArr = java.nio.ByteBuffer.allocate(8).putLong(123556).array();
            System.out.println("new " + UUID.nameUUIDFromBytes(byteArr).toString());

            // row.adGroupIncId = uuid.getMostSignificantBits();
            /*for(int i=0;i<100;i++) {
                NewPostRequest.sendPost(IXNativeAdserveRequest.formulateNewBackFillRequest(aesKey.getBytes(), ivKey.getBytes()), "backfill", "http://10.14.118.29:8801/");
                Thread.sleep(100);
            }*/
            NewPostRequest.sendPost(AdserveBackfillRequest.formulateNewBackFillRequest(aesKey.getBytes(),
                ivKey.getBytes()), "backfill", "http://localhost:8801/");
            //NewPostRequest.sendPost(NativeAdserveBackfillRequest.formulateNewBackFillRequest(aesKey.getBytes(),
            //ivKey.getBytes()), "backfill", "http://cas1001.ads.uj1.inmobi.com:8801/");
            // NewPostRequest.sendPost(AdserveBackfillRequest.formulateNewBackFillRequest(aesKey.getBytes(),
            //ivKey.getBytes()), "ixFill", "http://cas1001.ads.uh1.inmobi.com:8801/");


            //NewPostRequest.sendPost(NativeAdserveBackfillRequest.formulateNewBackFillRequest(aesKey.getBytes(),
            //                                                                         ivKey.getBytes()), "ixFill", "http://cas1001.ads.uh1.inmobi.com:8801/");

            //NewPostRequest.sendPost(
            //     AdserveBackfillRequest.formulateNewBackFillRequest(aesKey.getBytes(), ivKey.getBytes()),
            //    "rtbdFill", "http://cas1001.ads.hkg1.inmobi.com:8801/");
           /* for(int i=0;i<2000;i++) {
                NewPostRequest.sendPost(IXNativeAdserveRequest.formulateNewBackFillRequest(aesKey.getBytes(), ivKey.getBytes()), "rtbdFill", "http://10.14.118.29:8801/");
                i++;
            }*/

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private String generateNativeWrapperForDCP(String response) throws Exception{
        JSONObject nativeResponse = new JSONObject();
        nativeResponse.put("request_id","testtt");
        JSONObject[] objectArray = new JSONObject[2];
        objectArray[0] = new JSONObject(response);
        nativeResponse.put("adds",objectArray);
        System.out.println(nativeResponse);
        return null;
    }
    @Data
    class NativeWrapper{
        private String request_id;
        private JSONArray adds;
    }

}
