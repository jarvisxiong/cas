package com.inmobi.adserve.channels.server;

import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ChannelServerHelper {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ChannelServerHelper.class);

    public byte getDataCenterId(final String dataCenterIdKey) {
        byte dataCenterIdCode;
        try {
            dataCenterIdCode = Byte.parseByte(System.getProperty(dataCenterIdKey));
        } catch (final NumberFormatException e) {
            LOG.error("NumberFormatException in getDataCenterId");
            dataCenterIdCode = 0;
        }

        LOG.debug("dataCenterId is {}", dataCenterIdCode);

        return dataCenterIdCode;
    }

    public short getHostId(final String hostNameKey) {
        short hostId = 0;
        String hostName = System.getProperty(hostNameKey);
        if (hostName == null) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
                hostName = addr.getHostName();
            } catch (final UnknownHostException e1) {
                LOG.error("UnknownHostException in getHostId, exception raised {}", e1);
            }
        }
        try {
            if (null != hostName) {
                hostId = Short.parseShort(hostName.substring(3, 7));
            } else {
                return hostId;
            }
        } catch (final NumberFormatException e) {
            LOG.error("NumberFormatException in getHostId, exception raised {}", e);
        } catch (final StringIndexOutOfBoundsException e) {
            LOG.error("StringIndexOutOfRangeException in getHostId, exception raised {}", e);
        }

        LOG.debug("hostid is {}", hostId);

        return hostId;
    }

    public String getDataCentreName(final String key) {
        return System.getProperty(key);
    }
}
