package com.inmobi.adserve.channels.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONException;
import org.json.JSONObject;

public class SiegeSimulator {

  public static Map<String, String> responseMap = new HashMap<String, String>();

  public static void main(String[] args) {
    AtomicInteger tid = new AtomicInteger(0);
    int c = 200;
    int r = 50;
    List<Thread> threadList = new ArrayList<Thread>();
    for (int j = 0; j < c; j++) {
      String arg = "{\"site-type\":\"PERFORMANCE\",\"rq-h-user-agent\":\"Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Mobile/8G4\",\"handset\":[42279,\"apple_ipod_touch_ver4_3_1_subua\"],\"rq-mk-adcount\":\"1\",\"new-category\":[70,42],\"site-floor\":0,\"os-id\":5,\"rq-mk-ad-slot\":\"9\",\"u-id-params\":{\"O1\":\"8d10846582eef7c6f5873883b09a5a63\",\"u-id-s\":\"O1\",\"IX\":\"4fa7!506c!508902de!iPod3,1!8G4!19800\"},\"carrier\":[406,94,\"US\",12328,31118],\"site-url\":\"ww.inmobi.com\",\"tid\":\"0e919b0a-73c4-44cb-90ec-2b37b2249219\",\"rq-mk-siteid\":\"4028cba631d63df10131e1d3191d00cb\",\"site\":[34093,60],\"w-s-carrier\":\"3.0.0.0\",\"loc-src\":\"wifi\",\"slot-served\":\"9\",\"uparams\":{\"u-appdnm\":\"RichMediaSDK.app\",\"u-appver\":\"1008000\",\"u-postalcode\":\"302015\",\"u-key-ver\":\"1\",\"u-areacode\":\"bangalore\",\"u-appbid\":\"com.inmobi.profile1\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[13,8,19,4,17,16,14,3,11,29,23],\"source\":\"APP\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"i357\",\"pub-id\":\"4028cb9731d7d0ad0131e1d1996101ef\",\"os-id\":6}";
      Thread t = new Thread(new MakeRequest("http://localhost:8800/backfill", arg, r, tid));
      t.start();
      threadList.add(t);
    }

    for (Thread thread : threadList) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    FileWriter fstream;
    try {
      fstream = new FileWriter("/home/devashish/IFCResponses.txt");
      BufferedWriter out = new BufferedWriter(fstream);
      out.write((responseMap.toString()));
      //System.out.println(responseMap.toString());
      //Close the output stream
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  static class MakeRequest implements Runnable {

    private String args;
    private GetMethod method;
    int r;
    AtomicInteger tid;

    public MakeRequest(String method, String args, int r, AtomicInteger tid) {
      this.args = args;
      this.method = new GetMethod(method);
      this.r = r;
      this.tid = tid;
    }

    public void makeRequest() {
      HttpClient client = new HttpClient();

      try {
        JSONObject jObject = new JSONObject(args);
        jObject.put("tid", "TID-" + Integer.toString(tid.getAndIncrement()));

        method.setQueryString(new NameValuePair[] { new NameValuePair("args", jObject.toString()) });
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        long time = System.currentTimeMillis();
        // Execute the method.
        int statusCode = client.executeMethod(method);

        if(statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        // Deal with the response.
        // Use caution: ensure correct character encoding and is not binary data

        SiegeSimulator.responseMap.put(jObject.getString("tid"), responseBody);
        SiegeSimulator.responseMap.put(jObject.getString("tid")+"_LATENCY", Long.toString(System.currentTimeMillis()-time));
        //System.out.println(responseBody);
        //System.out.println(SiegeSimulator.responseMap);

      } catch (HttpException e) {
        System.err.println("Fatal protocol violation: " + e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        System.err.println("Fatal transport error: " + e.getMessage());
        e.printStackTrace();
      } catch (JSONException e) {
        System.err.println("Json error in parsing " + e.getMessage());
        e.printStackTrace();
      } finally {
        // Release the connection.
        method.releaseConnection();
      }
    }

    @Override
    public void run() {
      for (int i = 0; i < r; i++)
        makeRequest();
    }

  }
}