package com.inmobi.adserve.channels.server.kafkalogging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.messaging.ClientConfig;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.kafka.ConduitCallback;
import com.inmobi.messaging.kafka.ConduitCallbackResult;
import com.inmobi.messaging.kafka.Constants;
import com.inmobi.messaging.kafka.KafkaConfiguration;
import com.inmobi.messaging.kafka.KafkaMessagingPublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.user.photon.ThriftUtil;
import com.inmobi.user.photon.datatypes.activity.NestedActivity;
import com.inmobi.user.photon.datatypes.activity.NestedActivityData;
import com.inmobi.user.photon.datatypes.activity.NestedActivityRecord;
import com.inmobi.user.photon.datatypes.nestedactivity.cas.CASUserActivity;
import com.inmobi.user.photon.datatypes.nestedactivity.cas.DemandSideInfo;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class PhotonCasActivityWriter {
    private static final String CAS = "cas";
    private static final String KAFKA_HOST_LIST = "kafka.host_list";
    private static final String KAFKA_PHOTON_TOPIC = "cas_photon_activity";
    private static String kafkaTopic;
    private static KafkaMessagingPublisher publisher = null;

    private static ConduitCallback callback;

    public static void init(final Configuration serverConfiguration, final boolean isProdEnvironment) throws IOException {
        Map<String, String> pintailConfig = new HashMap<String, String>();
        kafkaTopic = serverConfiguration.getString(KAFKA_PHOTON_TOPIC);
        
        // Setting some service level mandatory default values
        pintailConfig.put(MessagePublisherFactory.PUBLISHER_CLASS_NAME_KEY, KafkaMessagingPublisher.class.getName());
        pintailConfig.put(KafkaConfiguration.PRODUCER_MODE.D3.value(), Constants.PRODUCER_TYPE.ASYNC.value());
        pintailConfig.put(KafkaConfiguration.APP_NAME.D3.value(), CAS);
        pintailConfig.put(KafkaConfiguration.ZK_HOST.D3.value(), serverConfiguration.getString(KAFKA_HOST_LIST));
        pintailConfig.put(KafkaConfiguration.ATTACH_HEADERS.D3.value(), "false");
        pintailConfig.put(KafkaConfiguration.TOPIC_NAME.D3.value(), kafkaTopic);
        
        log.debug("kafka host list is {} and topic are {}", serverConfiguration.getString(KAFKA_HOST_LIST), serverConfiguration.getString(KAFKA_PHOTON_TOPIC));

        callback = new ConduitCallback() {
            @Override
            public void onCompletion(final ConduitCallbackResult callbackResult) {
                switch (callbackResult.result()) {
                    case SPOOLED:
                    case SUCCESS:
                        log.debug("Kafka publish successful");
                        break;
                    case EXCEPTION:
                        log.debug("Kafka publish unsuccessful" + callbackResult.exception().toString());

                        break;
                    default:
                        log.debug("Kafka publish unsuccessful");
                        break;
                }
            }
        };
        try{
            publisher = (KafkaMessagingPublisher) MessagePublisherFactory.create(new ClientConfig(pintailConfig));    
        }catch(Exception e){
            if(isProdEnvironment){
                throw new RuntimeException(e);
            }
        }
        
        log.debug("Kafka publisher initialization successfull");
    }

    private PhotonCasActivityWriter() {

    }

    private static PhotonCasActivityWriter photonCasActivityWriter;

    public static PhotonCasActivityWriter getInstance() {
        if (photonCasActivityWriter == null) {
            synchronized (PhotonCasActivityWriter.class) {
                photonCasActivityWriter = new PhotonCasActivityWriter();
            }
        }
        return photonCasActivityWriter;
    }

    public void publish(final NestedActivityRecord nestedActivityRecord) {
        if (nestedActivityRecord == null) {
            log.debug("The record is null");
            return;
        } else if (publisher == null) {
            log.debug("The publisher is not initialized");
            return;
        }
        try {
            log.debug("Publishing to Photon Kafka");
            final Message message = new Message(ThriftUtil.threadSafeSerialize(nestedActivityRecord));
            publisher.publish(kafkaTopic, message, callback);
        } catch (final Exception e) {
            log.error("Error while publishing to photon kafka queue: ", e);
        }
    }

    private NestedActivityRecord getNestedActivityRecord(final String userId, final String impressionId,
            final String creativeId, final String dspId, final String advertiserId, final String dealId) {
        if (userId == null || impressionId == null || creativeId == null || dspId == null || advertiserId == null) {
            if (log.isDebugEnabled()) {
                log.debug("NestedActivityRecord generation -- userId {}, impressionId {}, creativeId {}, dspId {}, advertiserId{}",
                        userId, impressionId, creativeId, dspId, advertiserId);
            }
            return null;
        }
        final UUID uuid = UUID.fromString(impressionId);
        final long impressionEpochSecs = uuid.timestamp() / 1000;

        final DemandSideInfo demandSideInfo = new DemandSideInfo();
        demandSideInfo.setCreativeId(creativeId);
        demandSideInfo.setDspId(dspId);
        demandSideInfo.setAdvertiserId(advertiserId);
        demandSideInfo.setDealId(dealId);
        demandSideInfo.setImpressionEpochSecs((int) impressionEpochSecs);

        final CASUserActivity casUserActivity = new CASUserActivity();
        casUserActivity.setDemandSideInfo(demandSideInfo);

        final NestedActivity nestedActivity = new NestedActivity();
        nestedActivity.setCas(casUserActivity);

        final NestedActivityData nestedActivityData = new NestedActivityData();
        nestedActivityData.setCorrelationId(impressionId);

        nestedActivityData.setCorrelationIdEpochSecs((int) impressionEpochSecs);
        nestedActivityData.setNestedActivity(nestedActivity);
        nestedActivityData.setSubActivityId(impressionId);

        final NestedActivityRecord nestedActivityRecord = new NestedActivityRecord();
        nestedActivityRecord.setUserId(userId);
        List<NestedActivityData> nestedActivityDataList = new ArrayList<>();
        nestedActivityDataList.add(nestedActivityData);
        nestedActivityRecord.setNestedActivityData(nestedActivityDataList);

        return nestedActivityRecord;
    }

    public NestedActivityRecord getNestedActivityRecord(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams) {
        if (channelSegment == null) {
            return null;
        }
        final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
        final AdNetworkInterface adNetworkInterface = channelSegment.getAdNetworkInterface();

        // TODO: Do this for RTBD
        if (null == channelSegmentEntity || null == adNetworkInterface || !(adNetworkInterface instanceof IXAdNetwork)) {
            if (log.isDebugEnabled()) {
                log.debug("channelSegmentEntity or adNetworkInterface is null or adnetwork not instance of IXAdNetwork");
            }
            return null;
        }

        final IXAdNetwork ixAdNetwork = (IXAdNetwork) adNetworkInterface;
        final String impressionId = adNetworkInterface.getImpressionId();
        final Map<String, String> uidParams = sasParams.getTUidParams();
        String userId = null;
        if (uidParams != null) {
            if (uidParams.containsKey(UidType.GPID.toString())) {
                userId = uidParams.get(UidType.GPID.toString());
            } else if (uidParams.containsKey(UidType.IDA.toString())) {
                userId = uidParams.get(UidType.IDA.toString());
            } else if (uidParams.containsKey(UidType.O1.toString())) {
                userId = uidParams.get(UidType.O1.toString());
            }
        }
        final String advertiserId = ixAdNetwork.getAdvId();
        final String dspId = ixAdNetwork.getDspId();
        final String creativeId = ixAdNetwork.getCreativeId();
        final String dealId = (null != ixAdNetwork.getDeal()) ? ixAdNetwork.getDeal().getId() : null;

        return getNestedActivityRecord(userId, impressionId, creativeId, dspId, advertiserId, dealId);
    }

}
