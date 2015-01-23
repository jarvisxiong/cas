package ix.request;

import ix.common.CommonExtension;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@lombok.Data
public class Data {
    private final Integer type;
    private Integer len;
    private CommonExtension ext;
}
