package ix.request;

import java.util.List;

import ix.common.CommonExtension;
import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class App {
    private String id;
    private String name;
    private String domain;
    private String ver;
    private List<String> cat;
    private String bundle;
    private Publisher publisher;
    private Content content;
    private String keywords;
    private String storeurl;
    private List<String> blocklists;
    private AdQuality aq;
    private Transparency transparency;
    private CommonExtension ext;
}
