package ix.common;

import ix.request.Blind;
import ix.request.DtExtensions;
import ix.request.RubiconExtension;
import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class CommonExtension {
    private RubiconExtension rp;
    private Blind blind;
    private DtExtensions dt;
}
