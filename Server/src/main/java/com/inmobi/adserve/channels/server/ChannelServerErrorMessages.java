package com.inmobi.adserve.channels.server;

/**
 * Created by ishan.bhatnagar on 05/10/15.
 */
final class ChannelServerErrorMessages {
    final static String DATA_CENTRE_ID_MISSING_IN_PROD_ENVIRONMENT = "Environment variable(dc.id) was not set.";
    final static String DATA_CENTRE_NAME_MISSING_IN_PROD_ENVIRONMENT = "Environment variable(dc.name) was not set.";
    final static String CONTAINER_NAME_MISSING_IN_PROD_ENVIRONMENT = "Environment variable(container.name) was not set.";
    final static String CONTAINER_ID_COULD_NOT_BE_EXTRACTED_IN_PROD_ENVIRONMENT = "ContainerId could not be extracted from the container name.";

}
