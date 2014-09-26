package com.inmobi.adserve.channels.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.Configuration;

/** This class helps in regenerating new click and beacon urls.
 *  Regeneration is needed in IX because the impression id is modified after the RP response.
 */
public class ClickUrlsRegenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ClickUrlsRegenerator.class);
    private static CryptoHashGenerator cryptoHashGenerator;
    private static String              rmBeaconURLPrefix;
    private static String              imageBeaconURLPrefix;
    private static String              clickURLPrefix;


    public static void init(final Configuration configuration) {

        String cryptoSecretKey        =  configuration.getString("key.1.value");
        String rmBeaconURLPrefix      =  configuration.getString("beaconURLPrefix");
        String imageBeaconURLPrefix   =  configuration.getString("beaconURLPrefix");
        String clickURLPrefix         =  configuration.getString("clickURLPrefix");

        ClickUrlsRegenerator.rmBeaconURLPrefix = rmBeaconURLPrefix;
        ClickUrlsRegenerator.imageBeaconURLPrefix = imageBeaconURLPrefix;
        ClickUrlsRegenerator.clickURLPrefix = clickURLPrefix;

        /*
         * ClickURLMakerV6 always uses the non-testMode key for hash generation.
         * The same key that is being used in ClickURLMakerV6 is being used here.
         */
        cryptoHashGenerator = new CryptoHashGenerator(cryptoSecretKey);
    }


    public static String regenerateClickUrl(String clickUrl, String oldImpressionId, String newImpressionId) {
        LOG.debug("Old Click Url: {}", clickUrl);

        if (clickUrl == null) {
            return null;
        }

        // Removing trailing "/" + hash
        clickUrl = clickUrl.substring(0, clickUrl.lastIndexOf('/'));

        // Replacing old impression id with new impression id
        clickUrl = clickUrl.replace(oldImpressionId, newImpressionId);

        // Regenerate hash and new URL.
        clickUrl = clickUrl + '/' + cryptoHashGenerator.generateHash(clickUrl.substring(clickURLPrefix.length()));

        LOG.debug("New Click Url: {}", clickUrl);
        return clickUrl;
    }


    public static String regenerateBeaconUrl(String beaconUrl, String oldImpressionId, String newImpressionId, Boolean isRmAd) {
        LOG.debug("Old Beacon Url: {}", beaconUrl);

        if (beaconUrl == null) {
            return null;
        }

        // Removing trailing "/" + hash
        beaconUrl = beaconUrl.substring(0, beaconUrl.lastIndexOf('/'));

        // Replacing old impression id with new impression id
        beaconUrl = beaconUrl.replace(oldImpressionId, newImpressionId);

        // Same logic (after the 25th field) as createClickUrls method in ClickUrlMakerV6
        if (isRmAd) {
            // Appending new "/" + hash to beaconURL
            beaconUrl = beaconUrl + '/' + cryptoHashGenerator.generateHash(beaconUrl.substring(rmBeaconURLPrefix.length()));
        }
        else {
            // Appending new "/" + hash to beaconURL
            beaconUrl = beaconUrl + '/' + cryptoHashGenerator.generateHash(beaconUrl.substring(imageBeaconURLPrefix.length()));
        }

        LOG.debug("New Beacon Url: {}", beaconUrl);
        return beaconUrl;
    }
}
