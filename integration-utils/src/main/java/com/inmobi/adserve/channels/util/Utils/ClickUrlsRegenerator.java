package com.inmobi.adserve.channels.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class helps in regenerating new click and beacon urls.
 *  Regeneration is needed in IX because the impression id is modified after the RP response.
 */
public class ClickUrlsRegenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ClickUrlsRegenerator.class);
    private static CryptoHashGenerator cryptoHashGenerator;
    private static String              rmBeaconURLPrefix;
    private static String              imageBeaconURLPrefix;
    private static String              clickURLPrefix;
    private static Boolean             imageBeaconFlag;
    private static Boolean             isBeaconEnabledOnSite;


    public static void init(String cryptoSecretKey, String testCryptoSecretKey, String rmBeaconURLPrefix,
                            String imageBeaconURLPrefix, String clickURLPrefix, Boolean testMode,
                            Boolean imageBeaconFlag, Boolean isBeaconEnabledOnSite) {

        ClickUrlsRegenerator.rmBeaconURLPrefix     = rmBeaconURLPrefix;
        ClickUrlsRegenerator.imageBeaconURLPrefix  = imageBeaconURLPrefix;
        ClickUrlsRegenerator.clickURLPrefix        = clickURLPrefix;
        ClickUrlsRegenerator.imageBeaconFlag       = imageBeaconFlag;
        ClickUrlsRegenerator.isBeaconEnabledOnSite = isBeaconEnabledOnSite;

        if(testMode)
            cryptoHashGenerator = new CryptoHashGenerator(cryptoSecretKey);
        else
            cryptoHashGenerator = new CryptoHashGenerator(testCryptoSecretKey);
    }


    public static String regenerateClickUrl(String clickUrl, String oldImpressionId, String newImpressionId) {
        LOG.debug("Old Click Url: {}", clickUrl);

        // Removing trailing "/" + hash
        clickUrl = clickUrl.substring(0, clickUrl.lastIndexOf('/'));

        // Replacing old impression id with new impression id
        clickUrl = clickUrl.replace(oldImpressionId, newImpressionId);

        // Same logic (after the 25th field) as createClickUrls method in ClickUrlMakerV6
        if (null != clickURLPrefix) {
            // Appending new "/" + hash to clickUrl
            clickUrl = clickUrl + '/' + cryptoHashGenerator.generateHash(clickUrl.substring(clickURLPrefix.length()));
        } else {
            LOG.debug("clickURLPrefix is null, therefore cannot regenerate ClickURl");
            return null;
        }

        LOG.debug("New Click Url: {}", clickUrl);
        return clickUrl;
    }


    public static String regenerateBeaconUrl(Boolean isRmAd, String beaconUrl, String oldImpressionId, String newImpressionId) {
        LOG.debug("Old Beacon Url: {}", beaconUrl);

        // Removing trailing "/" + hash
        beaconUrl = beaconUrl.substring(0, beaconUrl.lastIndexOf('/'));

        // Replacing old impression id with new impression id
        beaconUrl = beaconUrl.replace(oldImpressionId, newImpressionId);

        // Same logic (after the 25th field) as createClickUrls method in ClickUrlMakerV6
        if (isRmAd) {
            if (null != rmBeaconURLPrefix) {
                // Appending new "/" + hash to beaconURL
                beaconUrl = beaconUrl + '/' + cryptoHashGenerator.generateHash(beaconUrl.substring(rmBeaconURLPrefix.length()));
            }
            else {
                LOG.debug("rmBeaconURLPrefix is null, therefore cannot regenerate BeaconURl");
                return null;
            }
        }
        else if (imageBeaconFlag || isBeaconEnabledOnSite){
            if (null != imageBeaconURLPrefix) {
                // Appending new "/" + hash to beaconURL
                beaconUrl = beaconUrl + '/' + cryptoHashGenerator.generateHash(beaconUrl.substring(imageBeaconURLPrefix.length()));
            } else {
                LOG.debug("imageBeaconURLPrefix is null, therefore cannot regenerate BeaconURl");
                return null;
            }
        }
        else {
            return null;
        }

        LOG.debug("New Beacon Url: {}", beaconUrl);
        return beaconUrl;
    }
}
