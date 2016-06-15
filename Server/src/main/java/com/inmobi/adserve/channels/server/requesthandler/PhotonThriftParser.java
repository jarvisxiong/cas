package com.inmobi.adserve.channels.server.requesthandler;

import java.io.ByteArrayOutputStream;

import com.inmobi.user.photon.service.UserProfileService;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;

import com.inmobi.user.photon.datatypes.attribute.Attributes;
import com.inmobi.user.photon.datatypes.commons.Tenant;

/**
 * Created by avinash.kumar on 6/6/16.
 */
public class PhotonThriftParser extends TServiceClient {
    static final String fnName = "getAllAttributes";

    public PhotonThriftParser(TProtocol prot) {
        super(prot, prot);
    }

    public void send(String userId, Tenant tenant) throws Exception {
        this.sendBase(fnName, new UserProfileService.getAllAttributes_args(userId, tenant));
    }

    public Attributes receive(TProtocol protocol) throws Exception {
        this.iprot_ = protocol;
        UserProfileService.getAllAttributes_result result = new UserProfileService.getAllAttributes_result();
        this.receiveBase(result, fnName);
        if (result.isSetSuccess()) {
            return result.success;
        } else if (result.isSetPhotonException()) {
            throw result.getPhotonException();
        } else {
            throw new TApplicationException(5, "getAllAttribute failed with unknown reason.");
        }
    }
}
