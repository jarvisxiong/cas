package com.inmobi.template.formatter;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.Inject;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.Tools;

public class TemplateParser {
	
//	private static String template = "#set($social = $tool.jpath($first, \"imNative.creative\").get(\"social\"))\n#if ($tool.isNonNull($social))\n#set($store = $social.get(\"appstore\"))\n#end\n## Subtitle shouldn't be more than 100 characters.\n#set($store = $social.get(\"appstore\"))\n#if($tool.isNonNull($store))\n#set($downloads=$store.get(\"downloads\"))\n#if ($tool.isNotEmpty($downloads))\n#if ($downloads.contains(\"<\"))\n#set($playerNum = $downloads.replaceAll(\"<\", \"\").trim())\n#elseif ($downloads.contains(\">\"))\n#set($playerNum = $downloads.replaceAll(\">\", \"\").trim())\n#elseif($downloads.contains(\"-\"))\n#set($idx = $downloads.indexOf(\"-\") + 1)\n#set($playerNum = $downloads.substring($idx).trim())\n#else\n#set($playerNum = $downloads)\n#end\n#set($playerNum = $playerNum.replaceAll(\",\", \"\"))\n#else\n#set($playerNum = \"\")\n#end\n#else\n#set($playerNum = \"\")\n#end\n## description\n#set($subtitle = $tool.jpath($first, \"imNative.creative.description.text\").replaceAll(\"\\s+\", \" \"))\n#if ($subtitle.length() > 100)\n#set($subtitle = \"$subtitle.substring(0, 97)...\")\n#end\n## icon\n#set ($icons = $tool.jpath($first, \"imNative.creative.icon\"))\n#foreach( $icon in $icons)\n#set ($width = $icon.get(\"width\"))\n\n#if ($width == 150)\n#set ($sel_icon = $icon)\n#break\n#end\n\n#if ($width >150 && $width <= 300)\n#if ($tool.isNonNull($h_icon))\n#set ($h_icon = $icon)\n#end\n#elseif ($width > 75 && $width <= 150)\n#if (!$tool.isNonNull($m_icon))\n#set ($m_icon = $icon)\n#end\n#elseif($width > 37 && $width <= 75)\n#if (!$tool.isNonNull($l_icon))\n#set ($l_icon = $icon)\n#end\n#end\n#if ($h_icon && $m_icon && $l_icon)\n#break\n#end\n#end\n#if ($tool.isNonNull($m_icon))\n#set ($sel_icon = $m_icon)\n#elseif ($tool.isNonNull($h_icon))\n#set ($sel_icon = $h_icon)\n#elseif ($l_icon)\n#set ($sel_icon = $l_icon)\n#else\n## No matching icon, pick first one.\n#set ($sel_icon = $icons.get(0))\n#end\n## get appId\n#if($tool.isNonNull($store))\n#set($appId=$store.get(\"uacId\"))\n#end\n#if(!$tool.isNonNull($appId))\n#set($appId=\"12345\")\n#end\n## get the rating\n#if($tool.isNonNull($store))\n #set($rating=$store.get(\"rating\"))\n#end\n#set($x_icon={})\n#set($x_icon.w=$sel_icon.width)\n#set($x_icon.h=$sel_icon.height)\n#set($x_icon.url=$sel_icon.url)\n## Define json map\n#set($pubContentMap = {\n \"uid\" : $String.valueOf($appId),\n \"title\" : $tool.jpath($first, \"imNative.creative.headline.text\"),\n \"subtitle\" : $subtitle,\n \"click_url\" : $first.openingLandingUrl,\n \"app_url\" : \"\",\n \"icon_xhdpi\" : $x_icon,\n \"star_rating\" : $String.valueOf($rating),\n \"players_num\": $playerNum,\n \"imp_id\" : $first.adImpressionId,\n \"cta_install\" : \"Install\" })\n#set($pubContent = $tool.jsonEncode($pubContentMap))\n$tool.nativeAd($first, $pubContent)";
//	
//	private String prettyTemplate(String template){
//		
//		StringTokenizer tokenizer = new StringTokenizer(template, "\\n");
//		StringBuilder builder = new StringBuilder();
//		while(tokenizer.hasMoreElements()){
//			builder.append(tokenizer.nextToken());
//		}
//		
//		return builder.toString();
//		
//	}
//	
	
	@Inject
	private Tools tools;
	
	@Inject
	private  MathTool mTool;
	
	
	private VelocityContext getVelocityContext(){
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("tool", tools);
	    velocityContext.put("math", mTool);
	    return velocityContext;
	}
	
	public String format(Context context,String templateName) throws ResourceNotFoundException, ParseErrorException, Exception{
		
	    //velocityContext.put("config", UnifiedFormatterConfiguration.getInstance());
	    //velocityContext.put("ad", adContext);
	   // List<CreativeContext> creativeContextList = adContext.getCreativeList();
	    //if (!creativeContextList.isEmpty()) {
	      //velocityContext.put("first", new ContextImpl());
	    //ContextImpl c = new ContextImpl();
	    //c.setApp(getSampleApp());
		VelocityContext velocityContext = getVelocityContext();
	    velocityContext.put("first", context);
	      
         Template template = TemplateManager.getTemplate(templateName);
	     StringWriter writer = new StringWriter();
	     template.merge( velocityContext, writer );
	        /* show the World */
	        System.out.println(" template : "+ writer.toString() );  
	      return writer.toString();
		
	}
	
	
		    
	
	

}
