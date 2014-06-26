package com.inmobi.adserve.channels.api;

import java.util.Map;

import org.apache.thrift.TException;

import com.inmobi.casthrift.rtb.BidResponse;

public interface NativeTemplateFormatter{
	
	public String getFormatterValue(String template, BidResponse response,Map<String, String> params) throws Exception;

}
