package ix.request;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@Data
public final class RPImpressionExtension {
    private final String zone_id;
    private String enc;
    private Integer pmptier;
    private Integer dpf = 1;
    private RPTargetingExtension target;
    private RPTargetingExtension track;
    private RPTargetingExtension rtb;
    private RPTargetingExtension nolog;
}
