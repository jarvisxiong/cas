package com.inmobi.adserve.channels.api.attribute;

import java.util.ArrayList;
import java.util.List;

public class BTypeNativeAttributeType implements NativeAttributeType<List<Integer>> {

  List<Integer> bType = new ArrayList<>(4);

  public BTypeNativeAttributeType() {
    bType.add(1);
    bType.add(2);
    bType.add(3);
    bType.add(4);
  }

  @Override
  public List<Integer> getAttributes() {
    return bType;
  }

}
