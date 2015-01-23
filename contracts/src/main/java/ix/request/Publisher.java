package ix.request;

import java.util.List;

import ix.common.CommonExtension;
import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Publisher {
    private String id;
    private String name;
    private List<String> cat;
    private String domain;
    private CommonExtension ext;
}
