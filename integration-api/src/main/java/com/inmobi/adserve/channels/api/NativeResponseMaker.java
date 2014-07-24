package com.inmobi.adserve.channels.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Data;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstrains;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Image;
import com.inmobi.template.context.App;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

public class NativeResponseMaker {
	
	private final static Logger            LOG                          = LoggerFactory.getLogger(NativeResponseMaker.class);
	
	private TemplateParser templateParser;
	
	private TemplateDecorator templateDecorator;
	
	private static final String errorStr = "%s can't be null."; 
	
	private Gson gson = null;
	
	@Inject
	RepositoryHelper repositoryHepler = null;
	
	@Inject
	public NativeResponseMaker(TemplateParser parser, TemplateConfiguration tc) throws TemplateException {
		gson = tc.getGsonManager().createGson();
		templateParser = parser;
		templateDecorator = tc.getTemplateDecorator(); 
	}
	
	
	public String makeResponse(BidResponse response,Map<String, String> params,NativeAdTemplateEntity templateEntity) throws Exception{
		 Preconditions.checkNotNull(response, errorStr,"BidResponse");
		 Preconditions.checkNotNull(params, errorStr,"params");
		 Preconditions.checkNotNull(params.containsKey("siteId"), errorStr,"siteId");
		 Preconditions.checkNotNull(templateEntity, errorStr,"templateEntity");
		 
		 String siteId = params.get("siteId");
		 
		 
		 App app = gson.fromJson(response.getSeatbid().get(0).getBid().get(0).getAdm(), App.class);
		 
		 validateResponse(app, templateEntity);
		 
		 VelocityContext vc = getVelocityContext(app,response,params);
		 String namespace = getNamespace();
		 vc.put("NAMESPACE", namespace);
		 
		 String pubContent = templateParser.format(app, siteId);
		 String contextCode = templateDecorator.getContextCode(vc);
		 
		 LOG.debug("Making response for siteId : "+siteId);
		return nativeAd(pubContent, contextCode, namespace);
	}
	
	
	private void validateResponse(App app,NativeAdTemplateEntity templateEntity) throws Exception{
		
		String mandatoryKey = templateEntity.getMandatoryKey();
		List<Integer> mandatoryList = NativeConstrains.getMandatoryList(mandatoryKey);
		for (Iterator<Integer> iterator = mandatoryList.iterator(); iterator.hasNext();) {
			Integer integer =  iterator.next();
			switch(integer){
				case NativeConstrains.Icon:
					 if(app.getIcons()==null || app.getIcons().size()<1||StringUtils.isEmpty(app.getIcons().get(0).getUrl())){
						 throwException(String.format(errorStr, "Icon"));
					 }
					 break;
				case NativeConstrains.Media:
					if(app.getScreenshots()==null ||app.getScreenshots().size()<1){
						throwException(String.format(errorStr, "Image"));
					}
					 break;
				case NativeConstrains.Headline:
					if(StringUtils.isEmpty(app.getTitle())){
						throwException(String.format(errorStr, "Title"));
					}
					break;
				case NativeConstrains.Description:
					if(StringUtils.isEmpty(app.getDesc())){
						throwException(String.format(errorStr, "Description"));
					}
					break;
			}
			
		}
		
		Image image = NativeConstrains.getImage(templateEntity.getImageKey());
		if(image!=null){
			Screenshot screenShot = app.getScreenshots().get(0);
			if(!(screenShot.getW()>=image.getMinwidth() && screenShot.getW() <=image.getMaxwidth())){
				throwException(String.format("Expected image contraints are %s. But got image attributes : %s ",image,screenShot));
			}
		}
		
		
	}
	
	private VelocityContext getVelocityContext(App app,BidResponse response,Map<String, String> params){
		VelocityContext context = new VelocityContext();
		
		String impId = response.getSeatbid().get(0).getBid().get(0).getImpid();
		
		context.put("IMP_ID", impId);
		context.put("LANDING_PAGE",app.getOpeningLandingUrl());
		context.put("BEACON_URL", getBeaconUrl(response,params,app));
		context.put("CLICK_TRACKER", getClickUrl(response,params,app));
		
		return context;
	}
	
	private String constructBeaconUrl(String url){
		return "<img src=\\\""+url+"\\\" style=\\\"display:none;\\\" />";
	}
	
	private String getBeaconUrl(BidResponse response,Map<String, String> params,App app){
		
		StringBuilder bcu = new StringBuilder();
		String nUrl = null;
        try {
            nUrl = response.seatbid.get(0).getBid().get(0).getNurl();
            if(nUrl!=null){
            	bcu.append(constructBeaconUrl(nUrl));
            }
        }
        catch (Exception e) {
            LOG.debug("Exception while parsing response {}", e);
        }
        
        String beaconUrl = params.get("beaconUrl");
        if(!StringUtils.isEmpty(beaconUrl)){
        	bcu.append(constructBeaconUrl(beaconUrl));
        }
        
        List<String> pixelurls = app.getPixelUrls();
        if(pixelurls!=null){
	        for(String purl:pixelurls){
	        	bcu.append(constructBeaconUrl(purl));
	        }
        }
        
        return bcu.toString();
	}
	
	private void throwException(String message) throws Exception{
		throw new Exception(message);
	}
	
	private String getClickUrl(BidResponse response,Map<String, String> params,App app){
		
		StringBuilder ct = new StringBuilder();
        
        List<String> clickUrls = app.getClickUrls();
        if(clickUrls!=null){
	        int i=0;
	        for(;i<clickUrls.size()-1;){
	        	ct.append("\"").append(clickUrls.get(i)).append("\"").append(",");
	        	i++;
	        }
	        
	        if(clickUrls.size()>0){
	        	ct.append("\"").append(clickUrls.get(i)).append("\"");
	        }
        }
        
		return ct.toString();
	}
	
	
	@Data
    private static class NativeAd {
        private final String pubContent;
        private final String contextCode;
        private final String namespace;
    }

    public String nativeAd(String pubContent, String contextCode,String namespace) {
        pubContent = base64(pubContent);
        NativeAd nativeAd = new NativeAd(pubContent, contextCode, namespace);
        return gson.toJson(nativeAd);
    }
    
    public String base64(String input) {
        // The escaping is not url safe, the input is decoded as base64 utf-8 string
        Base64 base64 = new Base64();
        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
    }
	
	private String getNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(10000))+10000) + "_";
    }
	
	

}
