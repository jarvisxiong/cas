package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashSet;
import java.util.UUID;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.Creative;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplierTest;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;
import com.inmobi.types.PricingModel;

// TODO: Needs fixing
public class CreateThriftResponseIXTest {
    AuctionEngine auctionEngine;
    ResponseSender responseSender;
    ChannelSegmentEntity dummyChannelSegmentEntity;
    double expectedBidPrice;
    String impressionID;

    @BeforeMethod
    public void setUp() {
        expectedBidPrice = 1.23;
        impressionID = "00000000-000e-9f94-0000-0000000210f7";

        ImpressionIdGenerator.init((short) 123, (byte) 10);


        // Create dummy ChannelSegmentEntity
        dummyChannelSegmentEntity =
                new ChannelSegmentEntity(ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("Advertiser A",
                        "adgroupId", "adId", "channelId", 1, null, null, true, true, "externalSiteKey", null,
                        "campaignId", null, 1, true, "pricingModel", null, 1, null, false, false, false, false, false,
                        false, false, false, false, false, null, null, 0.0d, null, null, false, new HashSet<String>(),
                        0));


        // Mock IXAdNetwork, ChannelSegment, SASRequestParameters
        final IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        final ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        final SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(dummyChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockIXAdNetwork).anyTimes();

        expect(mockIXAdNetwork.getDst()).andReturn(DemandSourceType.IX).anyTimes();
        expect(mockIXAdNetwork.getBidPriceInUsd()).andReturn(expectedBidPrice).anyTimes();
        expect(mockIXAdNetwork.getSecondBidPriceInUsd()).andReturn(expectedBidPrice).anyTimes();
        expect(mockIXAdNetwork.getImpressionId()).andReturn(impressionID).anyTimes();
        expect(mockIXAdNetwork.getCurrency()).andReturn("USD").anyTimes();
        expect(mockIXAdNetwork.getBidPriceInLocal()).andReturn(expectedBidPrice).anyTimes();
        expect(mockIXAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        // IX specific parameters
        expect(mockIXAdNetwork.returnDealId()).andReturn("dealId").anyTimes();
        expect(mockIXAdNetwork.returnAdjustBid()).andReturn(0.5).anyTimes();
        expect(mockIXAdNetwork.returnPmpTier()).andReturn(3).anyTimes();

        expect(mockSASRequestParameters.getSlot()).andReturn((short) 0).anyTimes();


        // Create responseSender (the class to be tested)
        // The object of the class to be tested is partially mocked
        responseSender =
                EasyMock.createMockBuilder(ResponseSender.class).addMockedMethod("getRtbResponse").createMock();
        expect(responseSender.getRtbResponse()).andReturn(mockChannelSegment).anyTimes();

        replay(mockChannelSegment, mockIXAdNetwork, mockSASRequestParameters, responseSender);
        responseSender.sasParams = mockSASRequestParameters;
    }

    @Test
    // 1.
    public void testCreateThriftResponse() {
        final String response =
                "{\"pubContent\":\"Cgp7InVpZCI6IjEyMzQ1IiwidGl0bGUiOiJDb21wYXJ0ZSBjb2NoZSIsInN1YnRpdGxlIjoiRWxpZ2UgZW50cmUgbcODwoPDgsKhcyBkZSAxIG1pbGzDg8KDw4LCs24gZGUgY29uZHVjdG9yZXMgZW4gRXVyb3BhIiwiY2xpY2tfdXJsIjoiaHR0cDovL2EuYXBwbG92aW4uY29tL3JlZGlyZWN0P2NsY29kZVx1MDAzZDMhNzI4My4xNDAzNzYzMjk5ITFlQkFwZmc5RGlSa3U0cDlqcFJlYXV1cU9lRFgtTWhQc3pXVzdkamsxdkd6NWYxTlB5RTVKMHhGMjZGNzdoUkN0cUlXZXZ6ZE85cU1WSFRoSmVwU0RCaGFvSHZKa3htSEZCcWYzaWdQNVVNeWRuYmVtSHptcVo0QmRJc3N5a3dZVUhoNU1CbTVFSXBxajM5N2UzSlA2SHBuQThTSUpnLWR2UFhfemZHRnc3ZkQzVnh0amRBdGpzTWJraVVQTmdhYTBjYXQxRDhZMXFmMzBfdnVLY3pTRlJIVlpIX2tNOGhJbzlBdE4yWlBBRXh6SzRHVnRMdjBmdFJud19rYTFqRnR0dXBtcXNTX1JGLVh4bWJRSlpUbFFhLXpUbHE5MHVUWWFtT3RPcXM1YzVQcWNzWkdwOXVYWU9ZeGt4S2l0cTE4VjlUWU5JNFcwLU9ObXI4emlNY0JxZyoqIiwiYXBwX3VybCI6IiIsImljb25feGhkcGkiOnsidyI6MzAwLCJoIjozMDAsInVybCI6Imh0dHA6Ly9kMWJja2o2YTR2bTFiZy5jbG91ZGZyb250Lm5ldC9jNGYyNzcyZGMxNjNjYTI0MzgzNjk4MmQwYTMzMTEyNi5wbmcifSwiaW1hZ2VfeGhkcGkiOnsidyI6NjAwLCJoIjozMTQsInVybCI6Imh0dHA6Ly9kMWJja2o2YTR2bTFiZy5jbG91ZGZyb250Lm5ldC9hYmNmODIzYzUwMjQ0MmUyNTBlZGU0NmI1MTA2MWJiNy5wbmcifSwic3Rhcl9yYXRpbmciOiJudWxsIiwicGxheWVyc19udW0iOiIiLCJpbXBfaWQiOiIzMDkzNWQxOS0wMTQ4LTEwMDAtZDYwMC0wMDAxMDAwMDAwMDAiLCJjdGFfaW5zdGFsbCI6IlZpc2l0IFVzIn0\\u003d\",\"contextCode\":\"\\u003cscript type\\u003d\\\"text/javascript\\\" src\\u003d\\\"mraid.js\\\"\\u003e\\u003c/script\\u003e\\n\\u003cdiv style\\u003d\\\"display:none; position:absolute;\\\" id\\u003d\\\"im_18702_clickTarget\\\"\\u003e\\u003c/div\\u003e\\n\\u003cscript type\\u003d\\\"text/javascript\\\"\\u003e\\n(function() {\\nvar e\\u003dencodeURIComponent,f\\u003dwindow,h\\u003ddocument,k\\u003d\\u0027appendChild\\u0027,n\\u003d\\u0027createElement\\u0027,p\\u003d\\u0027setAttribute\\u0027,q\\u003d\\u0027\\u0027,r\\u003d\\u0027\\u0026\\u0027,s\\u003d\\u00270\\u0027,t\\u003d\\u00272\\u0027,u\\u003d\\u0027\\u003d\\u0027,v\\u003d\\u0027?m\\u003d\\u0027,w\\u003d\\u0027Events\\u0027,x\\u003d\\u0027_blank\\u0027,y\\u003d\\u0027a\\u0027,z\\u003d\\u0027click\\u0027,A\\u003d\\u0027clickCallback\\u0027,B\\u003d\\u0027clickTarget\\u0027,C\\u003d\\u0027error\\u0027,D\\u003d\\u0027event\\u0027,E\\u003d\\u0027function\\u0027,F\\u003d\\u0027height\\u0027,G\\u003d\\u0027href\\u0027,H\\u003d\\u0027iatSendClick\\u0027,I\\u003d\\u0027iframe\\u0027,J\\u003d\\u0027img\\u0027,K\\u003d\\u0027impressionCallback\\u0027,L\\u003d\\u0027onclick\\u0027,M\\u003d\\u0027openLandingPage\\u0027,N\\u003d\\u0027recordEvent\\u0027,O\\u003d\\u0027seamless\\u0027,P\\u003d\\u0027src\\u0027,Q\\u003d\\u0027target\\u0027,R\\u003d\\u0027width\\u0027;f.inmobi\\u003df.inmobi||{};\\nfunction S(a){\\nthis.g\\u003da.lp;this.h\\u003da.lps;this.c\\u003da.ct;this.d\\u003da.tc;this.e\\u003da.bcu;this.a\\u003da.ns;this.i\\u003da.ws;a\\u003dthis.a;var c\\u003dthis;\\nf[a+M]\\u003dfunction(){\\nvar a\\u003dS.b(c.g),b\\u003df.mraid;\\u0027undefined\\u0027!\\u003d\\u003dtypeof b\\u0026\\u0026\\u0027undefined\\u0027!\\u003d\\u003dtypeof b.openExternal?b.openExternal(a):(a\\u003dS.b(c.h),b\\u003dh[n](y),b[p](Q,x),b[p](G,a),h.body[k](b),S.f(b))};\\nf[a+A]\\u003dfunction(a){\\nT(c,a)};f[a+K]\\u003dfunction(){U(c)};\\nf[a+N]\\u003dfunction(a,b){V(c,a,b)}}f.inmobi.Bolt\\u003dS;\\nS.f\\u003dfunction(a){\\nif(typeof a.click\\u003d\\u003dE)a.click.call(a);\\nelse if(a.fireEvent)a.fireEvent(L);\\nelse if(a.dispatchEvent){\\nvar c\\u003dh.createEvent(w);c.initEvent(z,!1,!0);a.dispatchEvent(c)}};\\nS.b\\u003dfunction(a){\\nreturn a.replace(/\\\\\\\\$TS/g,q+(new Date).getTime())};\\nfunction W(a,c){\\nvar d\\u003dh.getElementById(a.a+B),b\\u003dh[n](I);b[p](P,c);b[p](O,O);b[p](F,s);b[p](R,t);d[k](b)}\\nfunction T(a,c){\\nvar d\\u003df[a.a+H];d\\u0026\\u0026d();for(var d\\u003da.c.length,b\\u003d0;b\\u003cd;b++)W(a,S.b(a.c[b]));a.i\\u0026\\u0026(c\\u003dc||eval(D),\\u0027undefined\\u0027!\\u003d\\u003dtypeof c\\u0026\\u0026(d\\u003dvoid 0!\\u003dc.touches?c.touches[0]:c,f.external.notify(JSON.stringify({j:d.clientX,k:d.clientY}))))}function U(a){if(null!\\u003da.d)try{var c\\u003dh.getElementById(a.a+B),d\\u003da.d,b\\u003dh[n](I);b[p](O,O);b[p](F,s);b[p](R,t);c[k](b);var g\\u003db.contentWindow;g\\u0026\\u0026g.document.write(d)}catch(m){}}\\nfunction V(a,c,d){\\nfunction b(c,d,g){if(!(0\\u003e\\u003dg)){\\nvar m\\u003dh.getElementById(a.a+B),l\\u003dh[n](J);l[p](P,c);l[p](F,s);l[p](R,t);void 0!\\u003dl.addEventListener\\u0026\\u0026l.addEventListener(C,function(){f.setTimeout(function(){3E5\\u003cd\\u0026\\u0026(d\\u003d3E5);b(c,2*d,g-1)},d*Math.random())},!1);m[k](l)}}var g\\u003da.e,g\\u003dg+(v+c);if(d)for(var m in d)g+\\u003dr+e(m)+u+e(d[m]);b(g,1E3,5);18\\u003d\\u003dc\\u0026\\u0026U(a);8\\u003d\\u003dc\\u0026\\u0026T(a,null)};})();\\nnew window.inmobi.Bolt({\\n\\\"lp\\\":\\\"http://a.applovin.com/redirect?clcode\\u003d3!7283.1403763299!1eBApfg9DiRku4p9jpReauuqOeDX-MhPszWW7djk1vGz5f1NPyE5J0xF26F77hRCtqIWevzdO9qMVHThJepSDBhaoHvJkxmHFBqf3igP5UMydnbemHzmqZ4BdIssykwYUHh5MBm5EIpqj397e3JP6HpnA8SIJg-dvPX_zfGFw7fD3VxtjdAtjsMbkiUPNgaa0cat1D8Y1qf30_vuKczSFRHVZH_kM8hIo9AtN2ZPAExzK4GVtLv0ftRnw_ka1jFttupmqsS_RF-XxmbQJZTlQa-zTlq90uTYamOtOqs5c5PqcsZGp9uXYOYxkxKitq18V9TYNI4W0-ONmr8ziMcBqg**\\\",\\n\\\"lps\\\":\\\"http://a.applovin.com/redirect?clcode\\u003d3!7283.1403763299!1eBApfg9DiRku4p9jpReauuqOeDX-MhPszWW7djk1vGz5f1NPyE5J0xF26F77hRCtqIWevzdO9qMVHThJepSDBhaoHvJkxmHFBqf3igP5UMydnbemHzmqZ4BdIssykwYUHh5MBm5EIpqj397e3JP6HpnA8SIJg-dvPX_zfGFw7fD3VxtjdAtjsMbkiUPNgaa0cat1D8Y1qf30_vuKczSFRHVZH_kM8hIo9AtN2ZPAExzK4GVtLv0ftRnw_ka1jFttupmqsS_RF-XxmbQJZTlQa-zTlq90uTYamOtOqs5c5PqcsZGp9uXYOYxkxKitq18V9TYNI4W0-ONmr8ziMcBqg**\\\",\\n\\\"ct\\\":[\\\"http://ads.mediasmart.es/m/mclk?ms_op_code\\u003dcxiinhtbk6\\u0026ts\\u003d20140705160421.220\\u0026r\\u003dhttp%3A//www.blablacar.es/compartir-coche%3Futm_source%3DMEDIASMART%26utm_medium%3Dbanner%26utm_campaign%3DES_MEDIASMART_PSGR_MOBILE-MEDIAIOS5_CARS%26comuto_cmkt%3DES_MEDIASMART_PSGR_MOBILE-MEDIAIOS5_CARS\\\"],\\n\\\"bcu\\\":\\\"http://10.14.118.147/c.asm/C/b/39jv/0/0/2m/-2m/u/2n/0/0/eyJVTTUiOiJhYmNkODA1NzFkNjU3MjBlZmFzZGZhZiIsIlVESUQiOiJhYmNkODA1NzFkNjU3MjBlZmFzZGZhZiJ9/30935d19-0148-1000-d600-000100000000/-1/0/-1/0/0/40.790000915527344,86.41000366210938/0/nw/101/5/1/fc8ce3b8\\\",\\n\\\"tc\\\":\\\"\\u003cimg src\\u003d\\\\\\\"http://10.14.118.147/c.asm/C/b/39jv/0/0/2m/-2m/u/2n/0/0/eyJVTTUiOiJhYmNkODA1NzFkNjU3MjBlZmFzZGZhZiIsIlVESUQiOiJhYmNkODA1NzFkNjU3MjBlZmFzZGZhZiJ9/30935d19-0148-1000-d600-000100000000/-1/0/-1/0/0/40.790000915527344,86.41000366210938/0/nw/101/5/1/fc8ce3b8?b\\u003d${WIN_BID}\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"http://rendered.action1\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"http://rendered.action2\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\\",\\n\\\"ws\\\":false,\\n\\\"ns\\\":\\\"im_18702_\\\"});\\n(function() {var b\\u003dwindow,c\\u003d\\u0027handleClick\\u0027,d\\u003d\\u0027handleTouchEnd\\u0027,f\\u003d\\u0027handleTouchStart\\u0027;b.inmobi\\u003db.inmobi||{};var g\\u003db.inmobi;function h(a,e){return function(l){e.call(a,l)}}function k(a,e){this.b\\u003de;this.a\\u003dthis.c\\u003d!1;b[a+c]\\u003dh(this,this.click);b[a+f]\\u003dh(this,this.start);b[a+d]\\u003dh(this,this.end)}k.prototype.click\\u003dfunction(){this.c||this.b()};k.prototype.start\\u003dfunction(a){this.a\\u003dthis.c\\u003d!0;a\\u0026\\u0026a.preventDefault()};k.prototype.end\\u003dfunction(){this.a\\u0026\\u0026(this.a\\u003d!1,this.b())};g.OldTap\\u003dk;})(); new window.inmobi.OldTap(\\\"im_18702_\\\", function() {  window[\\u0027im_18702_openLandingPage\\u0027]();  window[\\u0027im_18702_clickCallback\\u0027]();});\\u003c/script\\u003e\",\"namespace\":\"im_18702_\"}";
        final AdPoolResponse adPoolResponse = responseSender.createThriftResponse(response);

        final AdInfo ixAd = adPoolResponse.getAds().get(0);
        final AdIdChain adIdChain = ixAd.getAdIds().get(0);

        Assert.assertEquals(adIdChain.adgroup_guid, dummyChannelSegmentEntity.getAdgroupId());
        Assert.assertEquals(adIdChain.ad_guid, dummyChannelSegmentEntity.getAdId(ADCreativeType.BANNER));
        Assert.assertEquals(adIdChain.advertiser_guid, dummyChannelSegmentEntity.getAdvertiserId());
        Assert.assertEquals(adIdChain.campaign_guid, dummyChannelSegmentEntity.getCampaignId());
        Assert.assertEquals(adIdChain.ad, dummyChannelSegmentEntity.getIncId(ADCreativeType.BANNER));
        Assert.assertEquals(adIdChain.group, dummyChannelSegmentEntity.getAdgroupIncId());
        Assert.assertEquals(adIdChain.campaign, dummyChannelSegmentEntity.getCampaignIncId());
        // TODO: IX specific params (Deal id, adjustBid, buyer)
        Assert.assertEquals(ixAd.getPricingModel(), PricingModel.CPM);
        Assert.assertEquals(ixAd.getPrice(), ixAd.getBid());
        Assert.assertEquals(ixAd.getPrice(), (long) (expectedBidPrice * Math.pow(10, 6)));

        final UUID uuid = UUID.fromString(impressionID);
        Assert.assertEquals(ixAd.getImpressionId(),
                new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
        Assert.assertEquals(ixAd.getSlotServed(), 0);

        final Creative ixCreative = new Creative();
        ixCreative.setValue(response);
        Assert.assertEquals(ixAd.getCreative(), ixCreative);
        Assert.assertEquals(adPoolResponse.getMinChargedValue(), (long) (expectedBidPrice * Math.pow(10, 6)));
    }

}
