package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ChannelServerHelper {
    private Logger logger;

    public ChannelServerHelper(Logger serverLogger) {
        logger = serverLogger;
    }

    public byte getDataCenterId(String dataCenterIdKey) {
        byte dataCenterIdCode;
        try {
            dataCenterIdCode = Byte.parseByte(System.getProperty(dataCenterIdKey));
        }
        catch (NumberFormatException e) {
            logger.info("NumberFormatException in getDataCenterId");
            dataCenterIdCode = 0;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("dataCenterId is " + dataCenterIdCode);
        }
        return dataCenterIdCode;
    }

    public short getHostId(String hostNameKey) {
        short hostId = 0;
        String hostName = System.getProperty(hostNameKey);
        if (hostName == null) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
                hostName = addr.getHostName();
            }
            catch (UnknownHostException e1) {
                logger.info("UnknownHostException in getHostId");
            }
        }
        try {
            if (null != hostName) {
                hostId = Short.parseShort(hostName.substring(3, 7));
            }
            else {
                return hostId;
            }
        }
        catch (NumberFormatException e) {
            logger.info("NumberFormatException in getHostId");
        }
        catch (StringIndexOutOfBoundsException e) {
            logger.info("StringIndexOutOfRangeException in getHostId");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hostid is " + hostId);
        }
        return hostId;
    }

    public String getDataCentreName(String key) {
        return System.getProperty(key);
    }

    public Integer getMaxConnections(String connectionsKey, ConnectionType connectionType) {
        Integer maxConnections = null;
        try {
            maxConnections = Integer.parseInt(System.getProperty(connectionsKey));
        }
        catch (NumberFormatException e) {
            logger.info("NumberFormatException " + connectionType.toString() + "maxConnections");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Max limit for " +  connectionType.toString() + " connections is " + maxConnections);
        }
        return maxConnections;
    }
}
