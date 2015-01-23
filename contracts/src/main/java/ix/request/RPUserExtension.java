package ix.request;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class RPUserExtension {
    private RPTargetingExtension target;
    private RPTargetingExtension track;
    private RPTargetingExtension rtb;
    private RPTargetingExtension nolog;
}
