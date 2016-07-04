package com.inmobi.adserve.channels.repository.pmp;

import static lombok.AccessLevel.PRIVATE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class PackageHelperV2 {
    private static final String SQL_ARRAY_DELIMITER = ",";

    @SuppressWarnings("unchecked")
    static <T> Set<T> getSet(String columnValue) throws Exception {
        Set<T> returnValue = null;

        if (StringUtils.isNotBlank(columnValue)) {
            columnValue = columnValue.substring(1, columnValue.length() - 1);
            returnValue = Stream.of(columnValue.split(SQL_ARRAY_DELIMITER))
                    .filter(StringUtils::isNotBlank)
                    .map(a -> (T)a)
                    .collect(Collectors.toSet());
        }

        return returnValue;
    }

    // TODO: Should use an Enum -> String mapping
    public static List<Map<String, String>> getThirdPartyTrackerMapList(final String[] thirdPartyTrackerJsonList) {
        final ImmutableList.Builder<Map<String, String>> thirdPartyTrackerMapListBuilder = new ImmutableList.Builder<>();
        if (null != thirdPartyTrackerJsonList) {
            for (final String thirdPartyTrackerJson : thirdPartyTrackerJsonList) {
                final Map<String, String> thirdPartyJsonMap = getThirdPartyTrackerMap(thirdPartyTrackerJson);
                if (null != thirdPartyJsonMap) {
                    thirdPartyTrackerMapListBuilder.add(thirdPartyJsonMap);
                }
            }
        }
        return thirdPartyTrackerMapListBuilder.build();
    }

    public static Map<String, String> getThirdPartyTrackerMap(final String thirdPartyTrackerJson) {
        if (StringUtils.isNotBlank(thirdPartyTrackerJson)) {
            final ImmutableMap.Builder<String, String> thirdPartyTrackerMapBuilder = new ImmutableMap.Builder<>();
            if (StringUtils.isNotBlank(thirdPartyTrackerJson)) {
                try {
                    final JSONObject jsonObj = new JSONObject(thirdPartyTrackerJson);
                    final Iterator iterator = jsonObj.keys();
                    while(iterator.hasNext()) {
                        final String key = (String)iterator.next();
                        thirdPartyTrackerMapBuilder.put(key, jsonObj.getString(key));
                    }
                } catch (final JSONException jse) {
                    log.error("Invalid third party tracker json: \nException: " + thirdPartyTrackerJson, jse);
                }
            }
            return thirdPartyTrackerMapBuilder.build();
        } else {
            return null;
        }
    }

    public static Set<Set<Integer>> extractDmpFilterExpression(final String dmpFilterExpressionJson)
            throws JSONException {
        final Set<Set<Integer>> dmpFilterSegmentExpression = new HashSet<>();

        if (StringUtils.isNotEmpty(dmpFilterExpressionJson)) {
            final JSONArray dmpSegmentsJsonArray = new JSONArray(dmpFilterExpressionJson);
            for (int andSetIdx = 0; andSetIdx < dmpSegmentsJsonArray.length(); andSetIdx++) {
                final JSONArray andJsonArr = (JSONArray) dmpSegmentsJsonArray.get(andSetIdx);
                final Set<Integer> orSet = new HashSet<>();
                for (int orSetIdx = 0; orSetIdx < andJsonArr.length(); orSetIdx++) {
                    orSet.add((Integer) andJsonArr.get(orSetIdx));
                }
                dmpFilterSegmentExpression.add(orSet);
            }
        }

        return dmpFilterSegmentExpression;
    }

    /**
     * This function extracts the os version targeting meta data. Meta Data consists of a map that maps the os id to a
     * Closed Range
     *
     * note: osId in the adPoolRequest is a long, osId in CAS is an int and osId in the ix_packages table is a short
     *
     * @param osVersionTargetingJson
     * @return
     * @throws JSONException
     */
    public static Map<Integer, Range<Double>> extractOsVersionTargeting(final String osVersionTargetingJson)
            throws JSONException {
        final ImmutableMap.Builder<Integer, Range<Double>> osVersionTargeting = new ImmutableMap.Builder<>();

        if (StringUtils.isNotEmpty(osVersionTargetingJson)) {
            final JSONArray jsonArray = new JSONArray(osVersionTargetingJson);

            // Iterate over all os ids
            for (int index = 0; index < jsonArray.length(); ++index) {
                final JSONObject osEntry = (JSONObject) jsonArray.get(index);
                final JSONArray osVersionRangeJsonArray = osEntry.getJSONArray("range");
                Range<Double> osVersionRange;

                // Sanity for malformed ranges
                if (osVersionRangeJsonArray.length() != 2) {
                    osVersionRange = Range.all();
                } else {
                    double minVer = osVersionRangeJsonArray.getDouble(0);
                    double maxVer = osVersionRangeJsonArray.getDouble(1);
                    // Sanity for range: minVer must always be <= maxVer
                    if (minVer > maxVer) {
                        final double temp = minVer;
                        minVer = maxVer;
                        maxVer = temp;
                    }

                    osVersionRange = Range.closed(minVer, maxVer);
                }

                osVersionTargeting.put(osEntry.getInt("osId"), osVersionRange);
            }
        }

        return osVersionTargeting.build();
    }

    /**
     * This function extracts the device manufacturer and device model targeting meta data. Meta Data consists of a map
     * that maps the device manufacturer id (Long) to the inclusion boolean (Boolean) to the set of device model ids.
     *
     * @param manufModelTargetingJson
     * @return Map as described above
     * @throws JSONException
     */
    public static Map<Long, Pair<Boolean, Set<Long>>> extractManufModelTargeting(final String manufModelTargetingJson)
            throws JSONException {
        final ImmutableMap.Builder<Long, Pair<Boolean, Set<Long>>> manufModelTargeting = new ImmutableMap.Builder<>();

        if (StringUtils.isNotEmpty(manufModelTargetingJson)) {
            final JSONArray jsonArray = new JSONArray(manufModelTargetingJson);

            // Iterate over all device manufacturer ids
            for (int manufIndex = 0; manufIndex < jsonArray.length(); ++manufIndex) {
                final JSONObject manufEntry = (JSONObject) jsonArray.get(manufIndex);

                final ImmutableSet.Builder<Long> modelIds = new ImmutableSet.Builder<>();
                final JSONArray modelIdsJsonArray = manufEntry.getJSONArray("modelIds");

                // Iterate over all the device model ids and add them to the modelIds Set
                for (int modelIndex = 0; modelIndex < modelIdsJsonArray.length(); ++modelIndex) {
                    modelIds.add(modelIdsJsonArray.getLong(modelIndex));
                }

                // Determine whether the modelIds Set is an inclusion or an exclusion Set
                final Boolean incl = manufEntry.getBoolean("incl");

                // Sanity: If modelIds Set is empty and incl is false, then skip manufacturer
                if (0 == modelIdsJsonArray.length() && !incl) {
                    continue;
                }

                manufModelTargeting.put(manufEntry.getLong("manufId"), ImmutablePair.of(incl, modelIds.build()));
            }
        }

        return manufModelTargeting.build();
    }

    public static Map<Integer, Pair<Boolean, Set<Integer>>> extractCountryCitiesTargeting(final String countryCitiesTargetingJson)
            throws JSONException {
        final ImmutableMap.Builder<Integer, Pair<Boolean, Set<Integer>>> countryCitiesTargeting = new ImmutableMap.Builder<>();

        if (StringUtils.isNotEmpty(countryCitiesTargetingJson)) {
            final JSONArray jsonArray = new JSONArray(countryCitiesTargetingJson);

            // Iterate over all country ids
            for (int countryIndex = 0; countryIndex < jsonArray.length(); ++countryIndex) {
                final JSONObject countryEntry = (JSONObject) jsonArray.get(countryIndex);

                final ImmutableSet.Builder<Integer> citiesIds = new ImmutableSet.Builder<>();
                final JSONArray cityIdsJsonArray = countryEntry.getJSONArray("cityIds");

                // Iterate over all the city ids and add them to the cityIds Set
                for (int cityIndex = 0; cityIndex < cityIdsJsonArray.length(); ++cityIndex) {
                    citiesIds.add(cityIdsJsonArray.getInt(cityIndex));
                }

                // Determine whether the cityIds Set is an inclusion or an exclusion Set
                final Boolean incl = countryEntry.getBoolean("incl");

                // Sanity: If cityIds Set is empty and incl is false, then skip country
                if (0 == cityIdsJsonArray.length() && !incl) {
                    continue;
                }

                countryCitiesTargeting.put(countryEntry.getInt("countryId"), ImmutablePair.of(incl, citiesIds.build()));
            }
        }

        return countryCitiesTargeting.build();
    }

    public static Pair<Boolean, Set<Integer>> extractSdkVersionTargeting(final String sdkVersionTargetingJson)
            throws JSONException {
        final ImmutableSet.Builder<Integer> sdkVersionSet = new ImmutableSet.Builder<>();
        boolean exclusion = true;

        if (StringUtils.isNotBlank(sdkVersionTargetingJson)) {
            try {
                final JSONObject sdkVersionTargetingJsonObject = new JSONObject(sdkVersionTargetingJson);
                JSONArray sdkVersionJsonArray = null;

                // Inclusion has higher priority in case of faulty jsons
                if (sdkVersionTargetingJsonObject.has("inclusion")) {
                    exclusion = false;
                    sdkVersionJsonArray = sdkVersionTargetingJsonObject.getJSONArray("inclusion");
                } else if (sdkVersionTargetingJsonObject.has("exclusion")) {
                    sdkVersionJsonArray = sdkVersionTargetingJsonObject.getJSONArray("exclusion");
                }

                if (null != sdkVersionJsonArray) {
                    for (int index = 0; index < sdkVersionJsonArray.length(); ++index) {
                        try {
                            sdkVersionSet.add(sdkVersionJsonArray.getInt(index));
                        } catch (final Exception e) {
                            // Ignore entry
                        }
                    }
                }
            } catch (final JSONException je) {
                // Ignore list
            }
        }

        return ImmutablePair.of(exclusion, sdkVersionSet.build());
    }



}
