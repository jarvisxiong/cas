package com.inmobi.template.context;

import java.util.HashMap;
import java.util.Map;

import com.inmobi.template.interfaces.Context;

// TODOD: To remove if not used
public class CreativeContext implements Context {


  Map<String, Object> params = new HashMap<String, Object>();

  public CreativeContext() {
    params.put("adGroupGuid", "1000");

  }

  public String getNs() {
    return "_namespace_1004";
  }

  public boolean getRequireMraidJs() {
    return true;
  }

  @Override
  public Object get(final String key) {

    return null;
  }

  public boolean isLatencyOptimised() {
    return true;
  }

}
