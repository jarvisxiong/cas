package com.inmobi.adserve.channels.util.Utils;

/**
 * Created by ishanbhatnagar on 30/9/14.
 */

public class TestUtils {

    public static class SampleStrings {
        public static final String ixRequestJson = "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"imp\":[  {  \"id\":\"1\",\"banner\":{  \"id\":\"a35e38be-0148-1000-fa4a-00052e330000\",\"w\":320,\"h\":48,\"ext\":{  \"rp\":{  \"size_id\":43}}},\"bidfloor\":0.4,\"proxydemand\":{ \"marketrate\":0.4},\"instl\":0,\"ext\":{  \"rp\":{  \"zone_id\":\"161290\"}}}],\"site\":{ \"id\":\"495362deeca64c52bd14e2108d34b4c2\",\"name\":\"Tango-Android-Newsfeed\",\"domain\":\"https://play.google.com/store/apps/details?id=com.sgiggle.production\",\"page\":\"https://play.google.com/store/apps/details?id=com.sgiggle.production\",\"publisher\":{  \"cat\":[  \"IAB17\",\"IAB19\"],\"ext\":{  \"rp\":{ \"account_id\":11726}}},\"blocklists\":[  \"blk152347\",\"InMobiPERF\"],\"aq\":{  \"sensitivity\":\"low\"},\"transparency\":{  \"blind\":0,\"blindbuyers\":[  2865,2853,3158,3038,3107,3002,3320,3560,3600,3676]},\"ext\":{  \"rp\":{ \"site_id\":38132}}},\"device\":{  \"lmt\":0,\"ua\":\"Mozilla/5.0 (Linux; U; Android 2.2.2; es-us; SPH-M820-BST Build/FROYO) AppleWebKit/525.10  (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2\",\"ip\":\"50.121.84.235\",\"geo\":{ \"lat\":40.79,\"lon\":86.41,\"country\":\"USA\"},\"didmd5\":\"abcd80571d65720efasdfaf\",\"dpidmd5\":\"abcd80571d65720efasdfaf\",\"os\":\"Android\",\"osv\":\"2.2\",\"ext\":{  \"rp\":{  \"xff\":\"50.121.84.235\"}}},\"user\":{  \"id\":\"abcd80571d65720efasdfaf\"},\"tmax\":25000,\"regs\":{ \"coppa\":0}}";

        public static final String ixResponseJson = "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bid\":[  { \"id\":\"ab73dd4868a0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"dealid\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}";

        public static final String ixResponseADM = "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>";

        public static final String clickUrl = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3";

        public static final String beaconUrl = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3";

        public static final String impressionId = "c124b6b5-0148-1000-c54a-00012e330000";
    }

    public static class SampleServletQueries {
        public static final String servletRepoRefresh = "http://localhost/repoRefresh?args={\"repoName\":\"ChannelRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    }
}
