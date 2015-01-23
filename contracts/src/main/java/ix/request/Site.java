package ix.request;

import java.util.List;

import ix.common.CommonExtension;
import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Site {
    private String id;
    private String name;
    private String domain;
    private String cat;
    private String page;
    private Publisher publisher;
    private Content content;
    private List<String> keywords;
    private List<String> blocklists;
    private AdQuality aq;
    private Transparency transparency;
    private CommonExtension ext;
}
