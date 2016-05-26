package com.inmobi.adserve.channels.server;

import static com.inmobi.adserve.channels.util.LoggerUtils.sendMail;
import static com.inmobi.adserve.channels.util.Utils.ExceptionBlock.getStackTrace;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NON_PROD;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class ChannelServerHelper {
    private final static Logger LOG = LoggerFactory.getLogger(ChannelServerHelper.class);
    protected static final String DATA_CENTRE_ID_KEY = "dc.id";
    protected static final String DATA_CENTRE_NAME_KEY = "dc.name";
    protected static final String CONTAINER_NAME_KEY = "container.name";
    protected static final String RUN_ENVIRONMENT_KEY = "run.environment";

    protected static final String NON_PROD_DATA_CENTRE_NAME = "corp";
    protected static final byte NON_PROD_DATA_CENTRE_ID = 0;
    protected static final String NON_PROD_CONTAINER_NAME = "localhost";
    protected static final short NON_PROD_CONTAINER_ID = 0;


    /**
     * Returns the dataCentreName, defaulting to null if the relevant environment variable was not set.
     * For non prod environments, the dataCentreName is always set to NON_PROD_DATA_CENTRE_NAME.
     *
     * @return appropriate dataCentreId
     */
    final String getDataCentreName() {
        String dataCentreName = System.getProperty(DATA_CENTRE_NAME_KEY);
        final String runEnvironment = System.getProperty(RUN_ENVIRONMENT_KEY);

        if (null == runEnvironment || NON_PROD.equalsIgnoreCase(runEnvironment)) {
            LOG.error("Defaulting dataCentreName to {} as non_prod environment was detected",
                NON_PROD_DATA_CENTRE_NAME);
            dataCentreName = NON_PROD_DATA_CENTRE_NAME;
        }

        LOG.error("Detected dataCentreName: {}", dataCentreName);
        return dataCentreName;
    }


    /**
     * Returns the dataCentreId, defaulting to null if the relevant environment variable was not set.
     * For non prod environments, the dataCentreId is always set to NON_PROD_DATA_CENTRE_ID.
     *
     * @return appropriate dataCentreId
     */
    final Byte getDataCentreId() {
        Byte dataCentreId = null;
        final String dataCentreIdStr = System.getProperty(DATA_CENTRE_ID_KEY);
        final String runEnvironment = System.getProperty(RUN_ENVIRONMENT_KEY);

        if (null == runEnvironment || NON_PROD.equalsIgnoreCase(runEnvironment)) {
            LOG.error("Defaulting dataCentreId to {} as non_prod environment was detected", NON_PROD_DATA_CENTRE_ID);
            dataCentreId = NON_PROD_DATA_CENTRE_ID;
        } else {
            try {
                if (StringUtils.isNotEmpty(dataCentreIdStr)) {
                    dataCentreId = Byte.parseByte(dataCentreIdStr);
                }
            } catch (final Exception e) {
                LOG.error("Exception parsing dataCentreId; Exception raised: {}", e);
            }
        }

        LOG.error("Detected DataCentreId: {}", dataCentreId);
        return dataCentreId;
    }


    /**
     * Returns the containerName, defaulting to null if the relevant environment variable was not set.
     * For non prod environments, the containerName is always set to NON_PROD_CONTAINER_NAME.
     *
     * @return appropriate containerName
     */
    final String getContainerName() {
        String containerName = System.getProperty(CONTAINER_NAME_KEY);
        final String runEnvironment = System.getProperty(RUN_ENVIRONMENT_KEY);

        if (null == runEnvironment || NON_PROD.equalsIgnoreCase(runEnvironment)) {
            LOG.error("SETTING containerName to {} as non_prod environment was detected", NON_PROD_CONTAINER_NAME);
            containerName = NON_PROD_CONTAINER_NAME;
        }

        LOG.error("Detected ContainerName: {}", containerName);
        return containerName;
    }
    
    /**
     * Return true if it is production environment else it returns false
     * @return
     */
    final boolean isProdEnvironment() {
        final String containerName = getContainerName();
        if (containerName == NON_PROD_CONTAINER_NAME) {
            return false;
        }
        return true;
    }


    /**
     * Extracts the containerId from the containerName if a prod environment is detected, defaulting to null if the
     * containerId cannot be extracted.
     * For non prod environments, the containerId is always set to NON_PROD_CONTAINER_ID.
     *
     * Assumption: Assuming that the containerName is of the form .{3}[0-9]{4}.* . This must be changed when unique
     * container name logic is finalised.
     *
     * @param containerName
     * @return appropriate containerId
     */
    final Short getContainerId(final String containerName) {
        Short containerId = null;
        final String runEnvironment = System.getProperty(RUN_ENVIRONMENT_KEY);

        if (null == runEnvironment || NON_PROD.equalsIgnoreCase(runEnvironment)) {
            LOG.error("Setting containerId to {} as non_prod environment was detected", NON_PROD_CONTAINER_ID);
            containerId = NON_PROD_CONTAINER_ID;
        } else {
            try {
                if (StringUtils.isNotEmpty(containerName)) {
                    final Pattern pattern = Pattern.compile(".*([0-9]{4}).*");
                    final Matcher matcher = pattern.matcher(containerName);
                    if (matcher.find()) {
                        containerId = Short.parseShort(matcher.group(1));
                    }
                }
            } catch (final Exception e) {
                LOG.error("Please ensure that the containerName is of the form .{3}[0-9]{4}.* .Exception extracting containerId from containerName({}); Exception raised: {}",
                    containerName, e);
            }
        }

        LOG.error("Detected ContainerId: {}", containerId);
        return containerId;
    }


    final static void handleChannelServerFailure(final String cause) {
        ServerStatusInfo.setStatusCodeAndString(404, cause);
        if (null != LOG) {
            LOG.error("Error starting CAS. Reason: {}", cause);
        }
        System.out.println("Error starting CAS. Reason: " + cause);
        sendMail(cause, null, CasConfigUtil.getServerConfig());
    }


    final static void handleChannelServerFailure(final Exception exception) {
        ServerStatusInfo.setStatusCodeAndString(404, getStackTrace(exception));
        if (null != LOG) {
            LOG.error("Exception in Channel Server " + exception);
            LOG.error("Stack trace is " + getStackTrace(exception));
        } else {
            System.out.println("Error in loading config file or logger config");
            System.out.println("Exception in Channel Server " + exception);
            System.out.println("Stack trace is " + getStackTrace(exception));
        }
        sendMail(exception.getMessage(), getStackTrace(exception), CasConfigUtil.getServerConfig());
    }
}
