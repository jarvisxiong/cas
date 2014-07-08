import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.Charsets;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.template.context.App;
import com.inmobi.template.formatter.TemplateManager;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.module.TemplateModule;

public class Test {
	
	
private static App getSampleApp(Injector injector){
	  
	   GsonManager gsonManager = injector.getInstance(GsonManager.class);
		
	   App app = gsonManager.createGson().fromJson("{\"clickurl\":[\"http://adtrack.king.com/modules/adTracking/adClicked.jsp?type=ad&androidId=$O1&st1=inmobi&st2=petrescuesaga&st3=us&st5=interstitial&st4=Native&st6=$IMP_ID&linkId=2302Native\" ],"
				+ "\"actionlink\":\"http://a.applovin.com/redirect?clcode=3!7283.1403763299!1eBApfg9DiRku4p9jpReauuqOeDX-MhPszWW7djk1vGz5f1NPyE5J0xF26F77hRCtqIWevzdO9qMVHThJepSDBhaoHvJkxmHFBqf3igP5UMydnbemHzmqZ4BdIssykwYUHh5MBm5EIpqj397e3JP6HpnA8SIJg-dvPX_zfGFw7fD3VxtjdAtjsMbkiUPNgaa0cat1D8Y1qf30_vuKczSFRHVZH_kM8hIo9AtN2ZPAExzK4GVtLv0ftRnw_ka1jFttupmqsS_RF-XxmbQJZTlQa-zTlq90uTYamOtOqs5c5PqcsZGp9uXYOYxkxKitq18V9TYNI4W0-ONmr8ziMcBqg**\","
				+ "\"actiontext\":\"Visit Us\","
				+"\"uid\": \"20\","
				+ "\"description\":\"AppLovin, Your customers aren't targets, or personas, they're people.\","
				+ "\"icon\":{\"h\":\"300\","
				+ "\"url\":\"http://applovin-assets.s3.amazonaws.com/applovin_logo_1200x627.jpg\","
				+ "\"w\":\"300\"},"
				+ " \"data\": [ { \"seq\": 1, \"value\": \"3.9\", \"label\": 0 }],"
				+ "\"image\":{\"h\":\"627\","
				+ "\"imageurl\":\"http://applovin-assets.s3.amazonaws.com/applovin_logo_1200x627.jpg\","
				+ "\"w\":\"1200\"},"
				+ "\"title\":\"AppLovin\"}",
				App.class);
	    		
	   
		return app;
	}

	public static String getContent() {
		File testFile = new File("/Users/prabhat.kumar/Documents/workspace/parser/templates/native_tango.vm");
		String testContents = null;
	    try {
	    	testContents = Files.toString(testFile, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return testContents;
	}

	public static void main(String args[]) throws ResourceNotFoundException, ParseErrorException, Exception{
//		TemplateManager.addToTemplateCache(new NativeTemplate());
		Injector injector = Guice.createInjector(new TemplateModule());
		TemplateManager.addToTemplateCache("native_tango.vm",getContent());
		TemplateParser parser = injector.getInstance(TemplateParser.class);
		parser.format(getSampleApp(injector),"native_tango.vm");
	}
	
}
