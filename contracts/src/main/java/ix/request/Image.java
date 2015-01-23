package ix.request;

import java.util.List;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Image {
    private Integer type;
    private Integer w;
    private Integer wmin;
    private Integer h;
    private Integer hmin;
    private List<String> mimes;
    // TODO: ext Object
}
