package ix.request;

import java.util.List;

import ix.common.CommonExtension;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@lombok.Data
public final class User {
    private String id;
    private String buyeruid;
    private String keywords;
    private String customdata;
    private List<Data> data;
    private CommonExtension ext;
}
