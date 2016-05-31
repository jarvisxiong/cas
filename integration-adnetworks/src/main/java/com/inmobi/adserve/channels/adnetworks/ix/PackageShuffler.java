package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;


public class PackageShuffler implements Comparator<Map.Entry<Integer, Boolean>> {
    private final Random random = new Random();

    @Override
    public int compare(Map.Entry<Integer, Boolean> o1, Map.Entry<Integer, Boolean> o2) {
        return random.nextInt(3) - 1;
    }
}
