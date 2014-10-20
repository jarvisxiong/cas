package com.inmobi.adserve.channels.util;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * @author abhishek.parwal
 * 
 */
public class DocumentBuilderHelperTest {

    @Test
    public void testParse() throws Exception {
        final DocumentBuilderHelper documentBuilderHelper = new DocumentBuilderHelper();
        final Document document = documentBuilderHelper.parse("<ad id=\"100\"><name>abhishek</name></ad>");
        Assert.assertNotNull(document);
    }
}
