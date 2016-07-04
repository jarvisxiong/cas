package com.inmobi.adserve.channels.server.requesthandler;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;

import com.inmobi.user.photon.datatypes.commons.Tenant;
import com.inmobi.user.photon.datatypes.profile.UserProfileView;
import com.inmobi.user.photon.service.UserProfileService;

/**
 * Created by avinash.kumar on 6/6/16.
 */
public class PhotonThriftParser extends TServiceClient {
    static final String fnName = "getProfileView";

    public PhotonThriftParser(final TProtocol prot) {
        super(prot, prot);
    }

    public void send(final String userId, final Tenant tenant) throws Exception {
        sendBase(fnName, new UserProfileService.getAllAttributes_args(userId, tenant));
    }

    public UserProfileView receive(final TProtocol protocol) throws Exception {
        iprot_ = protocol;
        final UserProfileService.getProfileView_result result = new UserProfileService.getProfileView_result();
        receiveBase(result, fnName);
        if (result.isSetSuccess()) {
            return result.success;
        } else if (result.isSetPhotonException()) {
            throw result.getPhotonException();
        } else {
            throw new TApplicationException(5, fnName + " failed with unknown reason.");
        }
    }
}
