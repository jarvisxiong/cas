package com.inmobi.castest.casconfenums.impl;

/**
 * @author santosh.vaidyanathan
 */

import com.inmobi.castest.casconfenums.def.CasConf.Repo;

public class RepoConf {

    private static final Exception RepoNotFoundException = new Exception("Repo Name not found in Repo enum list");

    public static String setRepoNameForRefresh(final Repo repo) throws Exception {
        String repoName = new String();
        switch (repo) {
            case CHANNEL_AD_GROUP_REPO: {

                repoName = "ChannelAdGroupRepository";
                break;
            }
            case CHANNEL_REPO: {
                repoName = "ChannelRepository";
                break;
            }
            case CHANNEL_FEEDBACK_REPO: {
                repoName = "ChannelFeedbackRepository";
                break;
            }
            case CHANNEL_SEGMENT_FEEDBACK_REPO: {

                repoName = "ChannelSegmentFeedbackRepository";
                break;
            }
            case PRICING_ENGINE_REPO: {
                repoName = "PricingEngineRepository";
                break;
            }
            case SITE_ECPM_REPO: {
                repoName = "SiteEcpmRepository";
                break;
            }
            case SITE_META_DATA_REPO: {
                repoName = "SiteMetaDataRepository";
                break;
            }
            default: {
                throw RepoNotFoundException;
            }
        }
        return repoName;

    }
}
