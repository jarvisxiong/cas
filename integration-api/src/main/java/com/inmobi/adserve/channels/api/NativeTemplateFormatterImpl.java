package com.inmobi.adserve.channels.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Data;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.NativeResponse;
import com.inmobi.casthrift.rtb.SeatBid;

public class NativeTemplateFormatterImpl implements NativeTemplateFormatter {
	
	private final static Logger            LOG                          = LoggerFactory.getLogger(NativeTemplateFormatterImpl.class);

	private static GsonBuilder gb = new GsonBuilder();
	static{
		gb.disableHtmlEscaping();
	}
	
	//TODO: remove it. Just added for tango for MVP.
	private String START = "{",
			 UID = "\"uid\":\"$UID\",",
			 TITLE = "\"title\":\"$TITLE\",",
			 SUBTITLE = "\"subtitle\":\"$DESCRIPTION\",",
			 CLICK_URL = "\"click_url\":\"$ACTION_LINK\",",
			 APP_URL = "\"app_url\":\"\",",
			 ICON = "\"icon_xhdpi\":{",
			    ICON_W=	 "\"w\":300,",
				ICON_H = "\"h\":300,",
				ICON_URL = "\"url\":\"$ICON_URL\""
				+ "},",
		     IMG = "\"image_xhdpi\":{",
		    	IMG_W = "\"w\":$IMG_WIDTH,",
		    	IMG_H = "\"h\":$IMG_HEIGHT,",
		    	IMG_URL = "\"url\":\"$IMG_URL\""+
		    	 "},",
		     STAR_RATING = "\"star_rating\":\"$RATING\",",
		     PLAYER_NUM = "\"players_num\":\"\",",
		     IMP_ID = "\"imp_id\":\"$IMPID\",",
		     CTA_INSTALL = "\"cta_install\":\"Install\"",
		     END = "}";
	
	
	
	private String contextCode = "\n<script type=\"text/javascript\" src=\"mraid.js\"></script>\n"
			+ "<div style=\"display:none; position:absolute;\" id=\"$NAMESPACEclickTarget\">"
			+ "</div>\n<script type=\"text/javascript\">\n"
			+ "(function() {"
			+ "var e=encodeURIComponent,f=window,h=document,k='appendChild',n='createElement',p='setAttribute',q='',r='&',s='0',t='2',u='=',v='?m=',w='Events',x='_blank',y='a',z='click',A='clickCallback',B='clickTarget',C='error',D='event',E='function',F='height',G='href',H='iatSendClick',I='iframe',J='img',K='impressionCallback',L='onclick',M='openLandingPage',N='recordEvent',O='seamless',P='src',Q='target',R='width';f.inmobi=f.inmobi||{};\n"
			+ "function S(a){"
			+ "this.g=a.lp;this.h=a.lps;this.c=a.ct;this.d=a.tc;this.e=a.bcu;this.a=a.ns;this.i=a.ws;a=this.a;var c=this;"
			+ "f[a+M]=function(){"
				+ "var a=S.b(c.g),b=f.mraid;'undefined'!==typeof b&&'undefined'!==typeof b.openExternal?b.openExternal(a):(a=S.b(c.h),b=h[n](y),b[p](Q,x),b[p](G,a),h.body[k](b),S.f(b))};"
			+ "f[a+A]=function(a){"
				+ "T(c,a)};f[a+K]=function(){U(c)};"
				+ "f[a+N]=function(a,b){V(c,a,b)}}f.inmobi.Bolt=S;\n"
				+ "S.f=function(a){"
				+ "if(typeof a.click==E)a.click.call(a);"
				+ "else if(a.fireEvent)a.fireEvent(L);"
				+ "else if(a.dispatchEvent){"
				+ "var c=h.createEvent(w);c.initEvent(z,!1,!0);a.dispatchEvent(c)}};"
				+ "S.b=function(a){"
				+ "return a.replace(/\\$TS/g,q+(new Date).getTime())};"
				+ "function W(a,c){"
				+ "var d=h.getElementById(a.a+B),b=h[n](I);b[p](P,c);b[p](O,O);b[p](F,s);b[p](R,t);d[k](b)}\n"
				+ "function T(a,c){"
				+ "var d=f[a.a+H];d&&d();for(var d=a.c.length,b=0;b<d;b++)W(a,S.b(a.c[b]));a.i&&(c=c||eval(D),'undefined'!==typeof c&&(d=void 0!=c.touches?c.touches[0]:c,f.external.notify(JSON.stringify({j:d.clientX,k:d.clientY}))))}function U(a){if(null!=a.d)try{var c=h.getElementById(a.a+B),d=a.d,b=h[n](I);b[p](O,O);b[p](F,s);b[p](R,t);c[k](b);var g=b.contentWindow;g&&g.document.write(d)}catch(m){}}\n"
				+ "function V(a,c,d){"
				+ "function b(c,d,g){if(!(0>=g)){"
				+ "var m=h.getElementById(a.a+B),l=h[n](J);l[p](P,c);l[p](F,s);l[p](R,t);void 0!=l.addEventListener&&l.addEventListener(C,function(){f.setTimeout(function(){3E5<d&&(d=3E5);b(c,2*d,g-1)},d*Math.random())},!1);m[k](l)}}var g=a.e,g=g+(v+c);if(d)for(var m in d)g+=r+e(m)+u+e(d[m]);b(g,1E3,5);18==c&&U(a);8==c&&T(a,null)};})();\n"
				+ " new window.inmobi.Bolt({"
				+ "\"lp\":\"$LANDING_PAGE\","
				+ "\"lps\":\"$OLD_LANDING_PAGE\","
				+ "\"ct\":[$CLICK_TRACKER],"
				+ "\"bcu\":\"$BEACON_URL\","
				+ "\"ws\":false,"
				+ "\"ns\":\"$NAMESPACE\"});"
				+ "\n(function() {var b=window,c='handleClick',d='handleTouchEnd',f='handleTouchStart';b.inmobi=b.inmobi||{};var g=b.inmobi;function h(a,e){return function(l){e.call(a,l)}}function k(a,e){this.b=e;this.a=this.c=!1;b[a+c]=h(this,this.click);b[a+f]=h(this,this.start);b[a+d]=h(this,this.end)}k.prototype.click=function(){this.c||this.b()};k.prototype.start=function(a){this.a=this.c=!0;a&&a.preventDefault()};k.prototype.end=function(){this.a&&(this.a=!1,this.b())};g.OldTap=k;})();\n new window.inmobi.OldTap(\"$NAMESPACE\", function() {\n  window['$NAMESPACEopenLandingPage']();\n  window['$NAMESPACEclickCallback']();\n});\n</script>";
	
	
	
	

	@Override
	public String getFormatterValue(String template, BidResponse response,Map<String, String> params) throws Exception {
		
		Bid bid = response.getSeatbid().get(0).getBid().get(0);
		String adm = bid.getAdm();
		
		NativeResponse natResponse = gb.create().fromJson(adm, NativeResponse.class);
		
		validateResponse(natResponse);
		
		String pubContent = preparePubContent(response,natResponse);
		String namespace = getNamespace();
	    String contextCode = prepareContextCode(response,params,natResponse).replaceAll("\\$NAMESPACE", namespace);
		
		return nativeAd(pubContent, contextCode, namespace);
	}
	
	private void throwException(String message) throws Exception{
		throw new Exception(message);
	}
	
	private void validateResponse(NativeResponse response) throws Exception{
		
		if(StringUtils.isEmpty(response.getIconurl())){
			throwException("Missing iconurl");
		}
		
		if(StringUtils.isEmpty(response.getTitle())){
			throwException("Missing title");
		}
		
		if(StringUtils.isEmpty(response.getDescription())){
			throwException("Missing description");
		}
		
		if(StringUtils.isEmpty(response.getActionlink())){
			throwException("Missing action link");
		}
		
		if(StringUtils.isEmpty(response.getActiontext())){
			throwException("Missing action text");
		}
		
		if(response.getPixelurlSize()<1){
			throwException("Missing pixelurl");
		}
		
		if(response.getClickurlSize()<1){
			throwException("missing clickurl");
		}
		
		if(!response.isSetImage()){
			throwException("Missing Image");
		}else if(StringUtils.isEmpty(response.getImage().getImageurl())){
			throwException("Missing image url");
		}else if(response.getImage().getW()<1){
			throwException("Image width is not defined.");
		}else if(response.getImage().getH()<1){
			throwException("Image height is not defined");
		}
		
		
	}
	
	private String prepareContextCode(BidResponse response, Map<String, String> params,NativeResponse natResponse){
		
		
		String cc = contextCode;
		
		StringBuilder bcu = new StringBuilder();
		String nUrl = null;
        try {
            nUrl = response.seatbid.get(0).getBid().get(0).getNurl();
            bcu.append(constructBeaconUrl(nUrl));
        }
        catch (Exception e) {
            LOG.debug("Exception while parsing response {}", e);
        }
        
        String beaconUrl = params.get("beaconUrl");
        if(!StringUtils.isEmpty(beaconUrl)){
        	bcu.append(constructBeaconUrl(beaconUrl));
        }
        
        List<String> pixelurls = natResponse.getPixelurl();
        for(String purl:pixelurls){
        	bcu.append(constructBeaconUrl(purl));
        }
        
        cc = cc.replaceAll("\\$BEACON_URL", bcu.toString());
        
        StringBuilder ct = new StringBuilder();
        
        List<String> clickUrls = natResponse.getClickurl();
        int i=0;
        for(;i<clickUrls.size()-1;){
        	ct.append("\"").append(clickUrls.get(i)).append("\"").append(",");
        	i++;
        }
        
        if(clickUrls.size()>0){
        	ct.append("\"").append(clickUrls.get(i)).append("\"");
        }
        cc = cc.replaceAll("\\$CLICK_TRACKER", ct.toString());

        
        String landingPageUrl = natResponse.getActionlink();
        cc = cc.replaceAll("\\$LANDING_PAGE", landingPageUrl);
        
		
		return cc;
	}
	
	private String constructBeaconUrl(String url){
		return "<img src="+url+" style=\"display:none;\" />";
	}
	
	private String getNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(10000))+10000) + "_";
    }
	
	
	private String preparePubContent(BidResponse response,NativeResponse natResponse) throws TException{
		Bid bid = response.getSeatbid().get(0).getBid().get(0);
		
		StringBuilder pubContent = new StringBuilder();
		pubContent.append(START);//STARTING JSON
		pubContent.append(UID);
		String title = natResponse.getTitle();
		if(StringUtils.isEmpty(title)){
			//throw exception.
		}
		
		pubContent.append(TITLE.replaceAll("\\$TITLE", title));
		
		String description = natResponse.getDescription();
		
		if(StringUtils.isEmpty(description)){
			//throw exception
		}
		
		pubContent.append(SUBTITLE.replaceAll("\\$DESCRIPTION", description));
		
		String actionLink = natResponse.getActionlink();
		String cta_install = natResponse.getActiontext();
		String iconUrl = natResponse.getIconurl();
		String impId = bid.getImpid();
		String imgUrl = natResponse.getImage().imageurl;
		int imgwidth = natResponse.getImage().w;
		int imgHeight = natResponse.getImage().h;
		
		String RATING ="";
		if(natResponse.getDataSize()>0){
			RATING = natResponse.getData().get(0).getValue();
		}
		
		pubContent
		.append(CLICK_URL.replaceAll("\\$ACTION_LINK", actionLink))
		.append(APP_URL)
		//ICON
		.append(ICON)
			.append(ICON_W)
			.append(ICON_H)
			.append(ICON_URL.replaceAll("\\$ICON_URL", iconUrl))
		//IMG	
		.append(IMG)
			.append(IMG_W.replaceAll("\\$IMG_WIDTH", String.valueOf(imgwidth)))
			.append(IMG_H.replaceAll("\\$IMG_HEIGHT", String.valueOf(imgHeight)))
			.append(IMG_URL.replaceAll("\\$IMG_URL", imgUrl))
			
		.append(STAR_RATING.replaceAll("\\$RATING", RATING))
		.append(PLAYER_NUM)
		.append(IMP_ID.replaceAll("\\$IMPID", impId))
		.append(CTA_INSTALL.replaceAll("\\$CTA_INSALL", cta_install))
		.append(END);

		return pubContent.toString();
		
	}
	
	
	public static void main(String args[]) throws Exception{
		
//		System.out.println("\"$LANDING_PAGE\"".replaceFirst("\\$LANDING_PAGE", "land"));
//		
//		NativeResponse natResponse = new NativeResponse();
//		
//		natResponse.setVersion("0.1");
//		natResponse.setIconurl("www.inmobi.com");
//		natResponse.setActionlink("actionlink.inmobi.com");
//		natResponse.setTitle("Hello World");
//		natResponse.setDescription("I am a description");
//		natResponse.setActiontext("Action text");
//		natResponse.setCallout(0);
//		NativeResponseData nrd = new NativeResponseData();
//		nrd.setLabel(0);
//		nrd.setSeq(0);
//		nrd.setValue("3.5");
//		natResponse.addToData(nrd);
//		
//		List<String> pixelurl = new ArrayList<String>();
//		pixelurl.add("http://rendered.action1");
//		pixelurl.add("http://rendered.action2");
//		
//		natResponse.setPixelurl(pixelurl);
//		
//		List<String> clickurl = new ArrayList<>();
//		clickurl.add("http://click.action1");
//		clickurl.add("http://click.action2");
//		
//		NativeResponseImage img  = new NativeResponseImage();
//		img.setImageurl("http://demo.image.com");
//		img.setW(350);
//		img.setH(980);
//		
//		natResponse.setImage(img);
//		
//		
//		TSerializer serialiser = new TSerializer(new TSimpleJSONProtocol.Factory());
//		System.out.println(serialiser.toString(natResponse));
		
		
		BidResponse response = new BidResponse();
		SeatBid sb = new SeatBid();
		List<Bid> bList = new ArrayList<>(1);
		Bid b = new Bid();
		b.setImpid("DDSSBFGH8765");
		b.setId("wxyabc876");
//		b.setAdm("{\"version\":\"1.0\",\"iconurl\":\"www.inmobi.com\",\"title\":\"Hello World\",\"description\":\"I am a description\",\"actiontext\":\"Action text\",\"actionlink\":\"actionlink.inmobi.com\",\"callout\":0,\"data\":[{\"label\":0,\"value\":\"3.5\",\"seq\":0}]}");
		b.setAdm("{\"version\": \"1.0\","
				+ "\"iconurl\": \"http://icon.png\","
				+ "\"title\": \"Hello World\", "
				+ "\"description\": \"This is a beautiful experience\", "
				+ "\"actiontext\": \"Buy Now\", "
				+ "\"actionlink\": \"http://buynow.action\","
				+ " \"pixelurl\": [ \"http://rendered.action1\", \"http://rendered.action2\" ],"
				+ " \"clickurl\": [ \"http://click.action1\", \"http://click.action2\" ], "
				+ "\"callout\": 0,"
				+ " \"data\": [ { \"seq\": 1, \"value\": \"3.9\", \"label\": 0 }],"
				+ "\"image\":{\"imageurl\": \"http://im-age.png\",w:350,h:980}"
				+ "}");
		bList.add(b);
		sb.setBid(bList);
		
		List<SeatBid> sbList = new ArrayList<>();
		sbList.add(sb);
		
		response.setSeatbid(sbList);
		
		Map<String, String> map = new HashMap<>();
		map.put("beaconUrl", "www.beaconurl.com");
		
		NativeTemplateFormatterImpl impl = new NativeTemplateFormatterImpl();
		System.out.println(impl.getFormatterValue(null, response, map));
		
	}
	
	 @Data
	    private static class NativeAd {
	        private final String pubContent;
	        private final String contextCode;
	        private final String namespace;
	    }

	    public String nativeAd(String pubContent, String contextCode,String namespace) {
	        pubContent = base64(pubContent);
	        NativeAd nativeAd = new NativeAd(pubContent,
							                contextCode,
							                namespace);
	        
	       return gb.create().toJson(nativeAd);
	    }
	    
	    public String base64(String input) {
	        // The escaping is not url safe, the input is decoded as base64 utf-8 string
	        Base64 base64 = new Base64();
	        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
	    }


	

}
