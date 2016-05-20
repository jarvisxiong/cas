package com.inmobi.template.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

class NoJsObject {
    @Getter
    private final Map<Integer, Map<String, List<String>>> et;

    public NoJsObject() {
        et = new HashMap<>();
    }
}
