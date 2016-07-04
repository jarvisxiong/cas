package com.inmobi.castest.utils.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inmobi.adserve.adpool.*;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.Carrier;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.DemandType;
import com.inmobi.adserve.adpool.Device;
import com.inmobi.adserve.adpool.DeviceType;
import com.inmobi.adserve.adpool.Education;
import com.inmobi.adserve.adpool.Ethnicity;
import com.inmobi.adserve.adpool.Geo;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.LatLong;
import com.inmobi.adserve.adpool.MaritalStatus;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.SexualOrientation;
import com.inmobi.adserve.adpool.Site;
import com.inmobi.adserve.adpool.SiteTemplateSettings;
import com.inmobi.adserve.adpool.SupplyContentType;
import com.inmobi.adserve.adpool.UidParams;
import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.adpool.User;
import com.inmobi.adserve.adpool.UserProfile;
import com.inmobi.fds.thrift.mapp.MappResponse;
import com.inmobi.fds.thrift.mapp.Score;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.inmobi.types.ContentRating;
import com.inmobi.types.Gender;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;
import com.inmobi.types.SupplySource;

import io.netty.util.CharsetUtil;

public class AdserveBackfillRequest {
    private static final String DEFAULT_NAPP_SCORE = "100";

    public static String defaultSetVariable(final String reqString, final String default_val) {
        final String nullString = "NULL";
        final String spaceString = "SPACE";
        final String emptyString = "EMPTY";

        String finalVal = null;

        if (reqString == null) {
            finalVal = default_val;
        } else if (reqString.equals(spaceString)) {
            finalVal = " ";
        } else if (reqString.equals(emptyString)) {
            finalVal = "";
        } else if (reqString.equals(nullString)) {
            return finalVal;
        } else {
            finalVal = reqString;
        }

        // if (reqString == nullString) {
        // return finalVal;
        // } else if (reqString == spaceString) {
        // finalVal = "  ";
        // } else if (reqString == emptyString) {
        // finalVal = "";
        // } else {
        //
        // if (reqString == null) {
        // finalVal = default_val;
        // } else {
        // finalVal = reqString;
        // }
        // }

        return finalVal;
    }

    public static AdPoolRequest formulateNewBackFillRequest(final Map<String, String> requestObject) {

        final String def_adpool_taskid = WilburyUUID.getUUID(System.nanoTime()).toString();
        final String def_adpool_remotehostip = "10.14.100.205";

        final String def_appbundleid = "testAppBundleId";
        final String def_siteincid = "34093";
        final String def_cpcfloor = "2";
        final String def_ecpmfloor = "0.04";
        final String def_cpmfloor = "0";
        final String def_siteurl = "newsiteurl";
        final String def_siteid = "newsiteid";
        final String def_publisherid = "newpublisherid";
        final String def_inventorytype = "APP";
        final String def_contentrating = "PERFORMANCE";
        final String def_contenttype = "PERFORMANCE";
        final String def_sitetags = "70,71";
        final String def_sitetaxonomies = "70,71";
        final String def_site_rewarded_video = "false";
        final String def_enriched_media_attributes = "0,2"; // 0- Banner , 2 - Video
        final String def_media_preferences =
                "{\"incentiveJSON\": \"{}\",\"video\" :{\"preBuffer\": \"WIFI\",\"skippable\": false,\"soundOn\": "
                        + "false }}";
        final String def_device_useragent = "useragent";
        final String def_device_modelid = "1234";
        final String def_device_manufacturerid = "12";
        final String def_device_osid = "3";
        final String def_device_osmajorversion = "0";
        final String def_device_browserid = "0";
        final String def_device_browsermajorversion = "0";
        final String def_device_handsetinternalid = "46180";
        final String def_device_devicetype = "SMARTPHONE";
        final String def_device_manufacturername = "WHATEVER_MANUF_NAME";
        final String def_device_modelname = "WHATEVER_MODEL_NAME";
        final String def_device_locale = "";

        final String def_carrier_carrierid = "0";
        final String def_carrier_networktype = "WIFI";
        final String def_requestadcount = "1";
        final String def_responseformat = "imai";
        final String def_tracerequest = "false";
        final String def_transcoderipdetected = "false";
        final String def_requestadtype = null;
        final String def_supplycapability = "BANNER";
        final String def_geo_countryid = "94";
        final String def_geo_countrycode = "US";
        final String def_geo_locationsource = null;
        final String def_geo_latlong_latitude = "0";
        final String def_geo_latlong_longitude = "0";
        final String def_geo_latlong_accuracy = "0";
        final String def_geo_zipids = null;
        final String def_geo_fenceids = null;
        final String def_geo_cityids = null;
        final String def_geo_stateids = null;

        final String def_integrationdetails_integrationtype = IntegrationType.ANDROID_SDK.toString();
        final String def_integrationdetails_integrationversion = "450";

        final String def_uidparams_rawuidvalues_um5 = null;
        final String def_uidparams_rawuidvalues_udid = null;
        final String def_uidparams_rawuidvalues_o1 = null;
        final String def_uidparams_rawuidvalues_ix = null;
        final String def_uidparams_rawuidvalues_lid = null;
        final String def_uidparams_rawuidvalues_sid = null;
        final String def_uidparams_rawuidvalues_ida = null;
        final String def_uidparams_rawuidvalues_idv = null;
        final String def_uidparams_rawuidvalues_so1 = null;
        final String def_uidparams_rawuidvalues_iuds1 = null;
        final String def_uidparams_rawuidvalues_gid = null;
        final String def_uidparams_rawuidvalues_wc = null;
        final String def_adpool_iem = null;

        final String def_uidparams_udidfromrequest = null;
        final String def_uidparams_udidfromuidcookie = null;
        final String def_uidparams_limitiosadtracking = "false";

        final String def_user_datavendorid = "1";
        final String def_user_datavendorname = null;
        final String def_user_yearofbirth = "0";
        final String def_user_gender = "MALE";
        final String def_user_income = "0";
        final String def_user_maritalstatus = null;
        final String def_user_education = null;
        final String def_user_nativelanguage = null;
        final String def_user_interests = null;
        final String def_user_ethnicity = null;
        final String def_user_sexualorientation = null;
        final String def_user_haschildren = "true";

        final String def_userprofile_csitags = null;

        final String def_adpool_selectedslots = "9";
        final String def_adpool_demandtypesallowed = null;

        final String def_adpool_segmentid = "0";
        final String def_adpool_testrequest = "true";
        final String def_adpool_supplysource = null;
        final String def_adpool_ipfileversion = "0";
        final String def_adpool_guidanceBid = "0";
        // This is not the task id. It is a unique id used between adserving and the sdk.
        final String def_adpool_requestGuid = WilburyUUID.getUUID(System.nanoTime()).toString();
        final String def_adpool_placementId = "1234";
        final String def_rqSslEnabled = "false";
        final boolean rqSslEnabled =
                Boolean.parseBoolean(defaultSetVariable(requestObject.get("adpool_rqSslEnabled"), def_rqSslEnabled));
        final Long adpool_placementId =
                Long.parseLong(defaultSetVariable(requestObject.get("adpool_placementId"), def_adpool_placementId));

        final String adpool_taskid = defaultSetVariable(requestObject.get("adpool_taskid"), def_adpool_taskid);
        final String adpool_remotehostip =
                defaultSetVariable(requestObject.get("adpool_remotehostip"), def_adpool_remotehostip);
        final Long adpool_guidanceBid =
                Long.valueOf(defaultSetVariable(requestObject.get("adpool_guidanceBid"), def_adpool_guidanceBid));

        final Long site_siteincid =
                Long.parseLong(defaultSetVariable(requestObject.get("site_siteincid"), def_siteincid));
        final String site_siteurl = defaultSetVariable(requestObject.get("site_siteurl"), def_siteurl);
        final Double site_cpcfloor =
                Double.parseDouble(defaultSetVariable(requestObject.get("site_cpcfloor"), def_cpcfloor));
        final Double site_ecpmfloor =
                Double.parseDouble(defaultSetVariable(requestObject.get("site_ecpmfloor"), def_ecpmfloor));

        final Double site_cpmfloor =
                Double.parseDouble(defaultSetVariable(requestObject.get("site_cpmfloor"), def_cpmfloor));

        final String site_siteid = defaultSetVariable(requestObject.get("site_siteid"), def_siteid);
        final String site_publisherid = defaultSetVariable(requestObject.get("site_publisherid"), def_publisherid);
        final String site_inventorytype =
                defaultSetVariable(requestObject.get("site_inventorytype"), def_inventorytype);
        final String site_contentrating =
                defaultSetVariable(requestObject.get("site_contentrating"), def_contentrating);

        final String site_contentType = defaultSetVariable(requestObject.get("site_contenttype"), def_contenttype);

        final Set<Integer> site_sitetags =
                getListOfIntegers(defaultSetVariable(requestObject.get("site_sitetags"), def_sitetags));
        final Set<Integer> site_sitetaxonomies =
                getListOfIntegers(defaultSetVariable(requestObject.get("site_sitetaxonomies"), def_sitetaxonomies));
        String siteEnrichedMediaAttributesStr = requestObject.get("site_enriched_media_attributes");
        if (StringUtils.isEmpty(siteEnrichedMediaAttributesStr)) {
            siteEnrichedMediaAttributesStr = def_enriched_media_attributes;
        }
        final Set<Integer> site_enriched_media_attributes = new HashSet<>();
        for (final String subSiteEnrichedMediaAttributesStr : siteEnrichedMediaAttributesStr.split(",")) {
            site_enriched_media_attributes.add(Integer.parseInt(subSiteEnrichedMediaAttributesStr));
        }
        final String site_media_preferences =
                defaultSetVariable(requestObject.get("site_media_preferences"), def_media_preferences);
        final String device_useragent =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_useragent"), def_device_useragent);

        final String temp_device_modelid =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_modelid"), def_device_modelid);

        Long device_modelid = null;
        if (temp_device_modelid != null) {
            device_modelid = Long.parseLong(temp_device_modelid);
        }

        final String temp_device_manufacturerid =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_manufacturerid"),
                        def_device_manufacturerid);

        Long device_manufacturerid = null;
        if (temp_device_manufacturerid != null) {
            device_manufacturerid = Long.parseLong(temp_device_manufacturerid);
        }

        final String temp_device_osid =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_osid"), def_device_osid);
        Long device_osid = null;
        if (temp_device_osid != null) {
            device_osid = Long.parseLong(temp_device_osid);
        }

        final String temp_device_osmajorversion =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_osmajorversion"),
                        def_device_osmajorversion);
        // Double device_osmajorversion = null;
        String device_osmajorversion = null;
        if (temp_device_osmajorversion != null) {
            device_osmajorversion = temp_device_osmajorversion;
        }

        final String temp_device_browserid =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_browserid"), def_device_browserid);
        Long device_browserid = null;
        if (temp_device_browserid != null) {
            device_browserid = Long.parseLong(temp_device_browserid);
        }

        final String device_locale =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("locale"), def_device_locale);

        final String temp_device_browsermajorversion =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_browsermajorversion"),
                        def_device_browsermajorversion);

        // Double device_browsermajorversion = null;
        String device_browsermajorversion = null;
        if (temp_device_browsermajorversion != null) {
            device_browsermajorversion = temp_device_browsermajorversion;
        }

        final String temp_device_handsetinternalid =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("device_handsetinternalid"),
                        def_device_handsetinternalid);

        Long device_handsetinternalid = null;
        if (temp_device_handsetinternalid != null) {
            device_handsetinternalid = Long.parseLong(temp_device_handsetinternalid);
        }
        final String device_devicetype =

        defaultSetVariable(requestObject.get("device_devicetype"), def_device_devicetype);

        final String device_manufacturername =
                defaultSetVariable(requestObject.get("device_manufacturername"), def_device_manufacturername);

        final String device_modelname = defaultSetVariable(requestObject.get("device_modelname"), def_device_modelname);

        final String temp_carrier_carrierid =
                defaultSetVariable(requestObject.get("carrier_carrierid"), def_carrier_carrierid);
        Long carrier_carrierid = null;
        if (temp_carrier_carrierid != null) {
            carrier_carrierid = Long.parseLong(temp_carrier_carrierid);
        }
        final String carrier_networktype =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("carrier_networktype"),
                        def_carrier_networktype);

        final String temp_adpool_requestadcount =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("adpool_requestedadcount"),
                        def_requestadcount);
        Short adpool_requestadcount = null;
        if (temp_adpool_requestadcount != null) {
            adpool_requestadcount = Short.parseShort(temp_adpool_requestadcount);
        }

        final ResponseFormat adpool_responseformat =
                getResponseFormat(defaultSetVariable(requestObject.get("adpool_responseformat"), def_responseformat));
        final String adpool_tracerequest =
                defaultSetVariable(requestObject.get("adpool_tracerequest"), def_tracerequest);
        final String adpool_transcoderipdetected =
                defaultSetVariable(requestObject.get("adpool_transcoderipdetected"), def_transcoderipdetected);
        final String adpool_requestadtype =
                defaultSetVariable(requestObject.get("adpool_requestedadtype"), def_requestadtype);
        final String adpool_supplycapability =
                defaultSetVariable(requestObject.get("adpool_supplycapability"), def_supplycapability);
        final String temp_geo_countryid = defaultSetVariable(requestObject.get("geo_countryid"), def_geo_countryid);
        Integer geo_countryid = null;
        if (temp_geo_countryid != null) {
            geo_countryid = Integer.parseInt(temp_geo_countryid);
        }

        final String geo_countrycode = defaultSetVariable(requestObject.get("geo_countrycode"), def_geo_countrycode);
        final String geo_locationsource =
                defaultSetVariable(requestObject.get("geo_locationsource"), def_geo_locationsource);

        final String temp_geo_latlong_latitude =
                defaultSetVariable(requestObject.get("latlong_latitude"), def_geo_latlong_latitude);
        Double geo_latlong_latitude = null;
        if (temp_geo_latlong_latitude != null) {
            geo_latlong_latitude = Double.parseDouble(temp_geo_latlong_latitude);
        }

        final String temp_geo_latlong_longitude =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("latlong_longitude"),
                        def_geo_latlong_longitude);
        Double geo_latlong_longitude = null;
        if (temp_geo_latlong_longitude != null) {
            geo_latlong_longitude = Double.parseDouble(temp_geo_latlong_longitude);
        }

        final String temp_geo_latlong_accuracy =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("latlong_accuracy"),
                        def_geo_latlong_accuracy);

        Double geo_latlong_accuracy = null;
        if (temp_geo_latlong_accuracy != null) {
            geo_latlong_accuracy = Double.parseDouble(temp_geo_latlong_accuracy);
        }

        final Set<Integer> geo_zipids =
                getListOfIntegers(defaultSetVariable(requestObject.get("geo_zipids"), def_geo_zipids));
        final Set<Long> geo_fenceids =
                getListOfLong(defaultSetVariable(requestObject.get("geo_fenceids"), def_geo_fenceids));
        final Set<Integer> geo_cityids =
                getListOfIntegers(defaultSetVariable(requestObject.get("geo_cityids"), def_geo_cityids));
        final Set<Integer> geo_stateids =
                getListOfIntegers(defaultSetVariable(requestObject.get("geo_stateids"), def_geo_stateids));

        final IntegrationType adpool_integration_integrationtype =
                getIntegrationType(defaultSetVariable(requestObject.get("integration_type"),
                        def_integrationdetails_integrationtype));

        final int adpool_integration_integrationversion =
                Integer.valueOf(defaultSetVariable(requestObject.get("integration_version"),
                        def_integrationdetails_integrationversion));

        final String adpool_requestguid = defaultSetVariable(requestObject.get("requestguid"), def_adpool_requestGuid);

        final String adpool_uidparams_rawuidvalues_um5 =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_um5"), def_uidparams_rawuidvalues_um5);
        final String adpool_uidparams_rawuidvalues_udid =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_udid"), def_uidparams_rawuidvalues_udid);
        final String adpool_uidparams_rawuidvalues_o1 =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_o1"), def_uidparams_rawuidvalues_o1);
        final String adpool_uidparams_rawuidvalues_ix =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_ix"), def_uidparams_rawuidvalues_ix);
        final String adpool_uidparams_rawuidvalues_lid =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_lid"), def_uidparams_rawuidvalues_lid);
        final String adpool_uidparams_rawuidvalues_sid =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_sid"), def_uidparams_rawuidvalues_sid);
        final String adpool_uidparams_rawuidvalues_ida =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_ida"), def_uidparams_rawuidvalues_ida);
        final String adpool_uidparams_rawuidvalues_idv =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_idv"), def_uidparams_rawuidvalues_idv);
        final String adpool_uidparams_rawuidvalues_so1 =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_so1"), def_uidparams_rawuidvalues_so1);
        final String adpool_uidparams_rawuidvalues_iuds1 =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_iuds1"), def_uidparams_rawuidvalues_iuds1);
        final String adpool_uidparams_rawuidvalues_gid =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_gid"), def_uidparams_rawuidvalues_gid);
        final String adpool_uidparams_rawuidvalues_wc =
                defaultSetVariable(requestObject.get("uidparams_rawuidvalues_wc"), def_uidparams_rawuidvalues_wc);
        final String adpool_iem = defaultSetVariable(requestObject.get("adpool_iem"), def_adpool_iem);

        final String adpool_uidparams_udidfromrequest =
                defaultSetVariable(requestObject.get("uidparams_udidfromrequest"), def_uidparams_udidfromrequest);
        final String adpool_uidparams_uuidfromuidcookie =
                defaultSetVariable(requestObject.get("uidparams_uuidfromuidcookie"), def_uidparams_udidfromuidcookie);
        final String adpool_uidparams_limitiosadtracking =
                defaultSetVariable(requestObject.get("uidparams_limitiosadtracking"), def_uidparams_limitiosadtracking);


        final String temp_user_datavendorid =
                defaultSetVariable(requestObject.get("user_datavendorid"), def_user_datavendorid);
        Long user_datavendorid = null;
        if (temp_user_datavendorid != null) {
            user_datavendorid = Long.parseLong(temp_user_datavendorid);
        }
        final String user_datavendorname =
                defaultSetVariable(requestObject.get("user_datavendorname"), def_user_datavendorname);
        final String temp_userprofile_csitags =
                defaultSetVariable(requestObject.get("userprofile_csitags"), def_userprofile_csitags);
        final Set<Integer> userprofile_csitags = new HashSet<Integer>();

        if (temp_userprofile_csitags != null) {
            final String[] temp = temp_userprofile_csitags.split(",");
            for (final String a : temp) {
                userprofile_csitags.add(Integer.parseInt(a));
            }
        }

        final String temp_user_yearofbirth =
                AdserveBackfillRequest.defaultSetVariable(requestObject.get("user_yearofbirth"), def_user_yearofbirth);
        Short user_yearofbirth = null;
        if (temp_user_yearofbirth != null) {
            user_yearofbirth = Short.parseShort(temp_user_yearofbirth);
        }
        final Gender user_gender = getGender(defaultSetVariable(requestObject.get("user_gender"), def_user_gender));
        final String temp_user_income = defaultSetVariable(requestObject.get("user_income"), def_user_income);
        Long user_income = null;
        if (temp_user_income != null) {
            user_income = Long.parseLong(temp_user_income);
        }

        final MaritalStatus user_maritalstatus =
                getMaritalStatus(defaultSetVariable(requestObject.get("user_maritalstatus"), def_user_maritalstatus));
        final Education user_education =
                getEducation(defaultSetVariable(requestObject.get("user_education"), def_user_education));
        final String user_nativelanguage =
                defaultSetVariable(requestObject.get("user_nativelanguage"), def_user_nativelanguage);
        final List<String> user_interests =
                getListOfString(defaultSetVariable(requestObject.get("user_interests"), def_user_interests));
        final Ethnicity user_ethnicity =
                getEthnicity(defaultSetVariable(requestObject.get("user_ethnicity"), def_user_ethnicity));
        final SexualOrientation user_sexualorientation =
                getSexualOrientation(defaultSetVariable(requestObject.get("user_sexualorientation"),
                        def_user_sexualorientation));
        final Boolean user_haschildren =
                Boolean.parseBoolean(defaultSetVariable(requestObject.get("user_haschildren"), def_user_haschildren));

        final List<Short> adpool_selectedslots =
                getListOfShort(defaultSetVariable(requestObject.get("adpool_selectedslots"), def_adpool_selectedslots));

        final Set<DemandType> adpool_demandtypesallowed =
                getSetOfDemandTypes(defaultSetVariable(requestObject.get("adpool_demandtypesallowed"),
                        def_adpool_demandtypesallowed));

        final String temp_adpool_segmentid =
                defaultSetVariable(requestObject.get("adpool_segmentid"), def_adpool_segmentid);
        Long adpool_segmentid = null;
        if (temp_adpool_segmentid != null) {
            adpool_segmentid = Long.parseLong(temp_adpool_segmentid);
        }
        final Boolean adpool_testrequest =
                Boolean.parseBoolean(defaultSetVariable(requestObject.get("adpool_testrequest"), def_adpool_testrequest));

        final SupplySource adpool_supplySource =
                getSupplySource(defaultSetVariable(requestObject.get("adpool_supplysource"), def_adpool_supplysource));

        final String temp_adpool_ipfileversion =
                defaultSetVariable(requestObject.get("adpool_ipfileversion"), def_adpool_ipfileversion);
        Long adpool_ipfileversion = null;
        if (temp_adpool_ipfileversion != null) {
            adpool_ipfileversion = Long.parseLong(temp_adpool_ipfileversion);
        }

        final AdPoolRequest adPoolRequest = new AdPoolRequest();

        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setIntegrationType(adpool_integration_integrationtype);
        integrationDetails.setIntegrationMethod(IntegrationMethod.SDK);
        integrationDetails.setIntegrationVersion(adpool_integration_integrationversion);
        final String app_bundle_id = requestObject.get("app_bundle_id");
        adPoolRequest.setRqSslEnabled(rqSslEnabled);
        adPoolRequest.setTaskId(adpool_taskid);
        adPoolRequest.setRemoteHostIp(adpool_remotehostip);
        adPoolRequest.setGuidanceBid(adpool_guidanceBid);
        adPoolRequest.setIpFileVersion(adpool_ipfileversion);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        if (StringUtils.isNotBlank(app_bundle_id)) {
            adPoolRequest.setAppBundleId(app_bundle_id);
        }


        final Site site = new Site();
        site.setSiteIncId(site_siteincid);
        site.setSiteUrl(site_siteurl);
        site.setCpcFloor(site_cpcfloor);
        site.setEcpmFloor(site_ecpmfloor);
        site.setCpmFloor(site_cpmfloor);
        site.setSiteId(site_siteid);
        site.setPublisherId(site_publisherid);
        site.setInventoryType(getInventoryType(site_inventorytype));
        site.setContentRatingDeprecated(getContentRating(site_contentrating));
        site.setSiteTags(site_sitetags);
        site.setSiteTaxonomies(site_sitetaxonomies);
        site.setSiteContentType(getSiteContentType(site_contentType));
        site.setEnrichedSiteAllowedMediaAttributes(site_enriched_media_attributes);
        site.setMediaPreferences(site_media_preferences);

        site.setCustomTemplatesOnly(false);

        final SiteTemplateSettings sts = new SiteTemplateSettings();

        // sts.addToCustomAdUnitExperimentList(1234589);
        // sts.addToCustomAdUnitExperimentList(6);
        // sts.addToCustomTemplateExperimentList(6);
        // sts.addToCustomTemplateStableList(10);
        // sts.addToCustomAdUnitStableList(23);
        // sts.setEnableAllDefaultTemplates(false);
        // sts.setEnableSystemTemplateFallback(false);
        // sts.setSiteCustomTemplatesExperimentEnabled(false);
        // sts.addToDefaultTemplateInclusionList(908);

        site.setSiteTemplateSettings(sts);
        // site.setAdvInclType(AdvInclType.ADVERTISER);



        // site.setRewarded(true);

        adPoolRequest.setSite(site);

        final Device device = new Device();
        device.setUserAgent(device_useragent);
        device.setModelId(device_modelid);
        device.setManufacturerId(device_manufacturerid);
        device.setOsId(device_osid);
        device.setOsMajorVersion(device_osmajorversion);
        device.setBrowserId(device_browserid);
        device.setBrowserMajorVersion(device_browsermajorversion);
        device.setHandsetInternalId(device_handsetinternalid);
        device.setDeviceTypeDeprecated(getDeviceTypeDeprecated(device_devicetype));

        device.setDeviceType(getDeviceType(device_devicetype));
        device.setManufacturerName(device_manufacturername);
        device.setModelName(device_modelname);
        if (StringUtils.isNotBlank(device_locale)) {
            device.setLocale(device_locale);
        }

        adPoolRequest.setDevice(device);

        final Carrier carrier = new Carrier();
        carrier.setCarrierId(carrier_carrierid);
        carrier.setNetworkType(getNetworkType(carrier_networktype));

        adPoolRequest.setCarrier(carrier);

        adPoolRequest.setRequestedAdCount(adpool_requestadcount);
        adPoolRequest.setResponseFormatDeprecated(adpool_responseformat);
        // adPoolRequest.setTraceRequest(Boolean.parseBoolean(adpool_tracerequest));
        adPoolRequest.setTranscoderIpDetected(Boolean.parseBoolean(adpool_transcoderipdetected));
        adPoolRequest.setRequestedAdType(getRequestedAdType(adpool_requestadtype));
        adPoolRequest.setSupplyAllowedContents(getListOfSupplyContentType(adpool_supplycapability));

        final Geo geo = new Geo();
        geo.setCountryId(geo_countryid);
        geo.setCountryCode(geo_countrycode);
        geo.setLocationSource(LocationSource.NO_TARGETING);

        final LatLong latLong = new LatLong();
        latLong.setLatitude(geo_latlong_latitude);
        latLong.setLongitude(geo_latlong_longitude);
        latLong.setAccuracy(geo_latlong_accuracy);
        geo.setLatLong(latLong);

        geo.setZipIds(geo_zipids);
        geo.setFenceIds(geo_fenceids);
        geo.setCityIds(geo_cityids);
        geo.setStateIds(geo_stateids);

        adPoolRequest.setGeo(geo);

        final UidParams uidParams = new UidParams();

        final Map<UidType, String> rawUidValues = new HashMap<UidType, String>();
        if (adpool_uidparams_rawuidvalues_um5 != null) {
            rawUidValues.put(UidType.UM5, adpool_uidparams_rawuidvalues_um5);
        }
        if (adpool_uidparams_rawuidvalues_udid != null) {
            rawUidValues.put(UidType.UDID, adpool_uidparams_rawuidvalues_udid);
        }
        if (adpool_uidparams_rawuidvalues_o1 != null) {
            rawUidValues.put(UidType.O1, adpool_uidparams_rawuidvalues_o1);
        }
        if (adpool_uidparams_rawuidvalues_ix != null) {
            rawUidValues.put(UidType.IX, adpool_uidparams_rawuidvalues_ix);
        }
        if (adpool_uidparams_rawuidvalues_lid != null) {
            rawUidValues.put(UidType.LID, adpool_uidparams_rawuidvalues_lid);
        }
        if (adpool_uidparams_rawuidvalues_sid != null) {
            rawUidValues.put(UidType.SID, adpool_uidparams_rawuidvalues_sid);
        }
        if (adpool_uidparams_rawuidvalues_ida != null) {
            rawUidValues.put(UidType.IDA, adpool_uidparams_rawuidvalues_ida);
        }
        if (adpool_uidparams_rawuidvalues_idv != null) {
            rawUidValues.put(UidType.IDV, adpool_uidparams_rawuidvalues_idv);
        }
        if (adpool_uidparams_rawuidvalues_so1 != null) {
            rawUidValues.put(UidType.SO1, adpool_uidparams_rawuidvalues_so1);
        }
        if (adpool_uidparams_rawuidvalues_iuds1 != null) {
            rawUidValues.put(UidType.IUDS1, adpool_uidparams_rawuidvalues_iuds1);
        }
        if (adpool_uidparams_rawuidvalues_gid != null) {
            rawUidValues.put(UidType.GID, adpool_uidparams_rawuidvalues_gid);
        }
        if (adpool_uidparams_rawuidvalues_wc != null) {
            rawUidValues.put(UidType.WC, adpool_uidparams_rawuidvalues_wc);
        }

        uidParams.setRawUidValues(rawUidValues);
        uidParams.setUdidFromRequest(adpool_uidparams_udidfromrequest);
        uidParams.setUuidFromUidCookie(adpool_uidparams_uuidfromuidcookie);
        uidParams.setLimitIOSAdTracking(Boolean.parseBoolean(adpool_uidparams_limitiosadtracking));
        int uidTypeValue = 2;
        try {
            uidTypeValue = Integer.parseInt(requestObject.get("uidparam_selecteduidtypevalue"));
        } catch (final Exception e) {

        }
        uidParams.setSelectedUidType(UidType.findByValue(uidTypeValue));

        // uidParams.putToRawUidValues(UidType.UDID, "9f262f4a37ed060b4c500737c95ce90f4582a433");
        // uidParams.putToRawUidValues(UidType.O1, "6c1ed2a7a2131e913b902ab82907fd0bfaafa320");
        // uidParams.putToLoggedUidParams(UidType.UDID, "61b6a7bfd92e9b767e508cd70a442f2a");
        // uidParams.putToLoggedUidParams(UidType.O1, "6c1ed2a7a2131e913b902ab82907fd0bfaafa320");
        adPoolRequest.setUidParams(uidParams);

        if (null != adpool_iem) {
            adPoolRequest.setIem(adpool_iem);
        }

        final User user = new User();
        user.setDataVendorId(user_datavendorid);
        user.setDataVendorName(user_datavendorname);
        user.setYearOfBirth(user_yearofbirth);
        user.setGender(user_gender);
        user.setIncome(user_income);
        user.setMaritalStatus(user_maritalstatus);
        user.setEducation(user_education);
        user.setNativeLanguage(user_nativelanguage);
        user.setInterests(user_interests);
        user.setEthnicity(user_ethnicity);
        user.setSexualOrientation(user_sexualorientation);
        user.setHasChildren(user_haschildren);

        final UserProfile userProfile = new UserProfile();
        userProfile.setCsiTags(userprofile_csitags);

        user.setUserProfile(userProfile);

        adPoolRequest.setUser(user);

        adPoolRequest.setSelectedSlots(adpool_selectedslots);
        adPoolRequest.setDemandTypesAllowed(adpool_demandtypesallowed);

        adPoolRequest.setSiteSegmentId(adpool_segmentid);
        adPoolRequest.setTestRequestDeprecated(adpool_testrequest);
        adPoolRequest.setSupplySource(adpool_supplySource);
        adPoolRequest.setIpFileVersion(adpool_ipfileversion);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        adPoolRequest.setRequestGuid(adpool_requestguid);

        // Placement related setters
        adPoolRequest.setPlacementId(adpool_placementId);

        // setting napp score
        final byte nappScore = Byte.parseByte(defaultSetVariable(requestObject.get("napp_score"), DEFAULT_NAPP_SCORE));
        final MappResponse mappResponse = new MappResponse();
        final Score score = new Score();
        score.setScore(nappScore);
        mappResponse.setEffectiveScore(score);
        adPoolRequest.setMappResponse(mappResponse);

        Map<String, String> requestHeader = new HashMap<>();
        final String headerKey = requestObject.get("request_header");
        if (StringUtils.isNotBlank(headerKey)) {
            requestHeader.put(headerKey, "true");
        }
        adPoolRequest.setRequestHeaders(requestHeader);

        return adPoolRequest;
    }

    public static ContentType getSiteContentType(final String site_contentType) {
        if (site_contentType != null) {
            if (site_contentType.toUpperCase().equals("PERFORMANCE")) {
                return ContentType.PERFORMANCE;
            } else if (site_contentType.toUpperCase().equals("FAMILY_SAFE")) {
                return ContentType.FAMILY_SAFE;
            } else if (site_contentType.toUpperCase().equals("MATURE")) {
                return ContentType.MATURE;
            }
        }
        return null;
    }

    public static InventoryType getInventoryType(final String inventory_type) {
        if (inventory_type != null) {
            if (inventory_type.toUpperCase().equals("BROWSER") || inventory_type.toUpperCase().equals("WAP")) {
                return InventoryType.BROWSER;
            } else if (inventory_type.toUpperCase().equals("APP")) {
                return InventoryType.APP;
            }
        }

        return null;
    }

    public static ContentRating getContentRating(final String content_rating) {
        if (content_rating != null) {
            if (content_rating.toUpperCase().equals("PERFORMANCE")) {
                return ContentRating.PERFORMANCE;
            } else if (content_rating.toUpperCase().equals("FAMILY_SAFE")) {
                return ContentRating.FAMILY_SAFE;
            } else if (content_rating.toUpperCase().equals("MATURE")) {
                return ContentRating.MATURE;
            }

        }

        return null;
    }

    public static com.inmobi.types.DeviceType getDeviceType(final String device_type) {
        if (device_type != null) {
            if (device_type.toUpperCase().equals("FEATURE_PHONE")) {
                return com.inmobi.types.DeviceType.FEATURE_PHONE;
            } else if (device_type.toUpperCase().equals("TABLET")) {
                return com.inmobi.types.DeviceType.TABLET;
            } else if (device_type.toUpperCase().equals("SMARTPHONE")) {
                return com.inmobi.types.DeviceType.SMARTPHONE;
            }
        }

        return null;

    }

    public static DeviceType getDeviceTypeDeprecated(final String device_type) {
        if (device_type != null) {
            if (device_type.toUpperCase().equals("FEATURE_PHONE")) {
                return DeviceType.FEATURE_PHONE;
            } else if (device_type.toUpperCase().equals("TABLET")) {
                return DeviceType.TABLET;
            } else if (device_type.toUpperCase().equals("SMARTPHONE")) {
                return DeviceType.SMARTPHONE;
            }
        }

        return null;

    }

    public static SupplySource getSupplySource(final String supplysource) {
        if (supplysource != null) {
            if (supplysource.toUpperCase().equals("NETWORK_SUPPLY")) {
                return SupplySource.NETWORK_SUPPLY;
            } else if (supplysource.toUpperCase().equals("RTB_EXCHANGE")) {
                return SupplySource.RTB_EXCHANGE;
            }
        }

        return null;
    }

    public static NetworkType getNetworkType(final String network_type) {
        if (network_type != null) {
            if (network_type.toUpperCase().equals("WIFI")) {
                return NetworkType.WIFI;
            } else if (network_type.toUpperCase().equals("NON_WIFI")) {
                return NetworkType.NON_WIFI;
            }
        }

        return null;
    }

    public static RequestedAdType getRequestedAdType(final String requestadtype) {
        if (requestadtype != null) {
            if (requestadtype.toUpperCase().equals("INTERSTITIAL")) {
                return RequestedAdType.INTERSTITIAL;
            } else if (requestadtype.toUpperCase().equals("NATIVE")) {
                return RequestedAdType.NATIVE;
            } else if (requestadtype.toUpperCase().equals("BANNER")) {
                return RequestedAdType.BANNER;
            } else if (requestadtype.toUpperCase().equals("VAST")) {
                return RequestedAdType.VAST;
            }

        }

        return null;
    }

    public static SupplyContentType getSupplyContent(final String supplycapability) {
        if (supplycapability != null) {
            if (supplycapability.toUpperCase().equals("TEXT")) {
                return SupplyContentType.TEXT;
            } else if (supplycapability.toUpperCase().equals("BANNER")) {
                return SupplyContentType.BANNER;
            } else if (supplycapability.toUpperCase().equals("JS")) {
                return SupplyContentType.JS;
            } else if (supplycapability.toUpperCase().equals("RICH_MEDIA")) {
                return SupplyContentType.RICH_MEDIA;
            } else if (supplycapability.toUpperCase().equals("RICH_TEXT")) {
                return SupplyContentType.RICH_TEXT;
            }
        }
        return null;
    }

    public static LocationSource getLocationSource(final String locationsource) {
        if (locationsource != null) {
            if (locationsource.toUpperCase().equals("CCID")) {
                return LocationSource.CCID;
            } else if (locationsource.toUpperCase().equals("WIFI")) {
                return LocationSource.WIFI;
            } else if (locationsource.toUpperCase().equals("LATLON")) {
                return LocationSource.LATLON;
            } else if (locationsource.toUpperCase().equals("DERIVED_LAT_LON")) {
                return LocationSource.DERIVED_LAT_LON;
            } else if (locationsource.toUpperCase().equals("NO_TARGETING")) {
                return LocationSource.NO_TARGETING;
            } else if (locationsource.toUpperCase().equals("BSSID_DERIVED")) {
                return LocationSource.BSSID_DERIVED;
            }
        }

        return null;
    }

    public static SexualOrientation getSexualOrientation(final String sexualorientation) {
        if (sexualorientation != null) {
            if (sexualorientation.toUpperCase().equals("STRAIGHT")) {
                return SexualOrientation.STRAIGHT;
            } else if (sexualorientation.toUpperCase().equals("BISEXUAL")) {
                return SexualOrientation.BISEXUAL;
            } else if (sexualorientation.toUpperCase().equals("GAY")) {
                return SexualOrientation.GAY;
            } else if (sexualorientation.toUpperCase().equals("UNKNOWN")) {
                return SexualOrientation.UNKNOWN;
            }
        }

        return null;
    }

    public static Ethnicity getEthnicity(final String ethnicity) {
        if (ethnicity != null) {
            if (ethnicity.toUpperCase().equals("HISPANIC")) {
                return Ethnicity.HISPANIC;
            } else if (ethnicity.toUpperCase().equals("AFRICANAMERICAN")) {
                return Ethnicity.AFRICANAMERICAN;
            } else if (ethnicity.toUpperCase().equals("ASIAN")) {
                return Ethnicity.ASIAN;
            } else if (ethnicity.toUpperCase().equals("CAUCASIAN")) {
                return Ethnicity.CAUCASIAN;
            } else if (ethnicity.toUpperCase().equals("OTHER")) {
                return Ethnicity.OTHER;
            }
        }

        return null;
    }

    public static MaritalStatus getMaritalStatus(final String maritalstatus) {
        if (maritalstatus != null) {
            if (maritalstatus.toUpperCase().equals("SINGLE")) {
                return MaritalStatus.SINGLE;
            } else if (maritalstatus.toUpperCase().equals("DIVORCED")) {
                return MaritalStatus.DIVORCED;
            } else if (maritalstatus.toUpperCase().equals("ENGAGED")) {
                return MaritalStatus.ENGAGED;
            } else if (maritalstatus.toUpperCase().equals("RELATIONSHIP")) {
                return MaritalStatus.RELATIONSHIP;
            }
        }

        return null;
    }

    public static Gender getGender(final String gender) {
        if (gender != null) {
            if (gender.toUpperCase().equals("FEMALE")) {
                return Gender.FEMALE;
            } else if (gender.toUpperCase().equals("MALE")) {
                return Gender.MALE;
            } else if (gender.toUpperCase().equals("UNKNOWN")) {
                return Gender.UNKNOWN;
            }
        }

        return null;
    }

    public static IntegrationType getIntegrationType(final String integrationType) {
        if (null != integrationType) {
            return IntegrationType.valueOf(integrationType.trim().toUpperCase());
        }
        return null;
    }

    public static ResponseFormat getResponseFormat(final String responseformat) {
        if (responseformat != null) {
            if (responseformat.toUpperCase().equals("AXML")) {
                return ResponseFormat.AXML;
            } else if (responseformat.toUpperCase().equals("HTML")) {
                return ResponseFormat.HTML;
            } else if (responseformat.toUpperCase().equals("XHTML")) {
                return ResponseFormat.XHTML;
            } else if (responseformat.toUpperCase().equals("JSON")) {
                return ResponseFormat.JSON;
            } else if (responseformat.toUpperCase().equals("RTBS")) {
                return ResponseFormat.RTBS;
            } else if (responseformat.toUpperCase().equals("IMAI")) {
                return ResponseFormat.IMAI;
            } else if (responseformat.toUpperCase().equals("NATIVE")) {
                return ResponseFormat.NATIVE;
            } else if (responseformat.toUpperCase().equals("VAST")) {
                return ResponseFormat.VAST;
            }
        }

        return null;
    }

    public static Education getEducation(final String education) {
        if (education != null) {
            if (education.toUpperCase().equals("HIGH_SCHOOL_OR_LESS")) {
                return Education.HIGH_SCHOOL_OR_LESS;
            } else if (education.toUpperCase().equals("COLLEGE_GRADUATE")) {
                return Education.COLLEGE_GRADUATE;
            } else if (education.toUpperCase().equals("POST_GRADUATE_OR_ABOVE")) {
                return Education.POST_GRADUATE_OR_ABOVE;
            }
        }
        return null;
    }

    public static DemandType getDemandType(final String demandtype) {
        if (demandtype != null) {
            if (demandtype.toUpperCase().equals("BRAND")) {
                return DemandType.BRAND;
            } else if (demandtype.toUpperCase().equals("PERFORMANCE")) {
                return DemandType.PERFORMANCE;
            } else if (demandtype.toUpperCase().equals("PROGRAMMATIC")) {
                return DemandType.PROGRAMMATIC;
            }
        }

        return null;
    }

    public static Set<DemandType> getSetOfDemandTypes(final String input_string) {
        final Set<DemandType> listInt = new HashSet<DemandType>();

        if (input_string != null) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(getDemandType(sEach));
                }
            }
        }

        return listInt;
    }

    public static Set<Integer> getListOfIntegers(final String input_string) {
        final Set<Integer> listInt = new HashSet<Integer>();

        if (input_string != null && input_string.length() != 0) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(Integer.parseInt(sEach));
                }
            }
        }

        return listInt;
    }

    public static List<String> getListOfString(final String input_string) {
        final List<String> listInt = new LinkedList<String>();

        if (input_string != null) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(sEach);
                }
            }
        }

        return listInt;
    }

    public static List<SupplyContentType> getListOfSupplyContentType(final String input_string) {
        final List<SupplyContentType> listInt = new LinkedList<SupplyContentType>();

        if (input_string != null) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(getSupplyContent(sEach));
                }
            }
        }

        return listInt;
    }

    public static List<Short> getListOfShort(final String input_string) {
        final List<Short> listInt = new LinkedList<Short>();

        if (input_string != null) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(Short.parseShort(sEach));
                }
            }
        }

        return listInt;
    }

    public static Set<Long> getListOfLong(final String input_string) {
        final Set<Long> listInt = new HashSet<Long>();

        if (input_string != null) {
            final String[] splitValues = input_string.split(",");

            if (splitValues.length != 0) {
                for (final String sEach : splitValues) {
                    listInt.add(Long.parseLong(sEach));
                }
            }
        }

        return listInt;
    }

    public static byte[] getSerializedBody(final AdPoolRequest adPoolRequest) {
        try {

            // String targetUrl = "http://localhost:8800/rtbdFill";
            final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());

            System.out.println(adPoolRequest.toString());
            final byte[] urlParameters = serializer.serialize(adPoolRequest);

            return urlParameters;

        } catch (final TException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static AdPoolResponse getDeserializedBody(final String responseByteArray) {
        try {

            // String targetUrl = "http://localhost:8800/rtbdFill";
            final byte[] result = responseByteArray.getBytes(CharsetUtil.UTF_8);
            final AdPoolResponse adPoolResponse = new AdPoolResponse();
            final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
            tDeserializer.deserialize(adPoolResponse, result);
            System.out.println("AdPool Response is : " + adPoolResponse.toString());

            return adPoolResponse;

        } catch (final TException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }

        return null;

    }

}
