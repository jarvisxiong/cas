/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses;
import com.inmobi.adtemplate.platform.AdTemplate;
import com.inmobi.template.context.Icon;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateManager;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.module.TemplateModule;

/**
 * @author ritwik.kumar
 *
 */
public class NativeTemplateTester {
    private static final long TEMPLATE_ID = 999;

    /**
     * 
     * @param args
     * @throws TException
     */
    public static void main(final String[] args) throws TException {
        final NativeTemplateTester test = new NativeTemplateTester();
        final String templateBinary =
                "CAACAAAAAggAAwAAAAMMAAQLAAEAAAAPbmF0aXZlMS4wdmlkaWVvCwACAAAIAyNzZXQgKCR0aXRsZSA9ICR0b29sLmpwYXRoKCRmaXJzdCwiYWQudGl0bGUiKSkKI3NldCAoJHZhc3RDb250ZW50ID0gJHRvb2wuZ2V0VmFzdFhNbCgkZmlyc3QpKQojaWYgKCR0b29sLmlzTm9uTnVsbCgkdGl0bGUpKQojc2V0KCR0aXRsZSA9ICR0aXRsZS5yZXBsYWNlQWxsKCJccysiLCAiICIpKQojZW5kCiNzZXQoJGljb25zID0gJHRvb2wuanBhdGgoJGZpcnN0LCJhZC5pY29ucyIpKQojZm9yZWFjaCggJGljb24gaW4gJGljb25zKQojIyBBc3N1bWluZyBpY29uJ3MgYXNwZWN0IHJhdGlvIGlzIGFsd2F5cyAxOjEuIE5vIGV4dHJhIGxvZ2ljIGZvciBBUiBjaGVjayB0byBpbXByb3ZlIHBlcmZvcm1hbmNlLgojc2V0ICgkd2lkdGggPSAkaWNvbi5nZXQoIndpZHRoIikpCiMjIFRyeSB0byBtYXRjaCB0aGUgZXhhY3Qgd2lkdGguIAojaWYgKCR3aWR0aCA9PSA3NSkKI3NldCAoJHNlbF9pY29uID0gJGljb24pCiNicmVhawojZW5kCiNpZiAoJHdpZHRoID4gMTUwICYmICR3aWR0aCA8PSAzMDApCiNpZiAoISR0b29sLmlzTm9uTnVsbCgkaF9pY29uKSkKI3NldCAoJGhfaWNvbiA9ICRpY29uKQojZW5kCiNlbHNlaWYgKCR3aWR0aCA+IDc1ICYmICR3aWR0aCA8PSAxNTApCiNpZiAoISR0b29sLmlzTm9uTnVsbCgkbV9pY29uKSkgCiNzZXQgKCRtX2ljb24gPSAkaWNvbikKI2VuZAojZWxzZWlmICgkd2lkdGggPiAzNyAmJiAkd2lkdGggPD0gNzUpCiNpZiAoISR0b29sLmlzTm9uTnVsbCgkbF9pY29uKSkKI3NldCAoJGxfaWNvbiA9ICRpY29uKQojZW5kCiNlbmQKI2lmICgkaF9pY29uICYmICRtX2ljb24gJiYgJGxfaWNvbikKI2JyZWFrIAojZW5kCiNlbmQKIyMgSWYgc2VsX2ljb24gaXMgbnVsbCwgdGhlbiB0cnkgc2VsZWN0aW5nIG9uZSBvZiB0aGUgaF9pY29uLCBtX2ljb24gb3IgbF9pY29uIG9yIGluIHdvcnN0IGNhc2UgdGhlIGZpcnN0IGljb24gaW4gdGhlIGljb25zLWFycmF5CiNpZiAoISR0b29sLmlzTm9uTnVsbCgkc2VsX2ljb24pKQojaWYgKCR0b29sLmlzTm9uTnVsbCgkbF9pY29uKSkKI3NldCAoJHNlbF9pY29uID0gJGxfaWNvbikKI2Vsc2VpZiAoJHRvb2wuaXNOb25OdWxsKCRtX2ljb24pKQojc2V0ICgkc2VsX2ljb24gPSAkbV9pY29uKQojZWxzZWlmICgkdG9vbC5pc05vbk51bGwoJGhfaWNvbikpCiNzZXQgKCRzZWxfaWNvbiA9ICRoX2ljb24pCiNlbHNlCiNzZXQgKCRzZWxfaWNvbiA9ICRpY29ucy5nZXQoMCkpCiNlbmQKI2VuZAojIyBDcmVhdGluZyBhIGNvcHkgb2YgdGhlIHNlbF9pY29uIGJlbG93IHNvIHRoYXQgdXBkYXRpb24gb2YgdGhlIHVybCBkb2VzIG5vdCBhZmZlY3QgYWN0dWFsIGpzb24gb2JqZWN0CiNzZXQgKCRzZWxfaWNvbiA9eyJ3aWR0aCIgOiAkc2VsX2ljb24uZ2V0KCJ3aWR0aCIpLAoiaGVpZ2h0IiA6ICRzZWxfaWNvbi5nZXQoImhlaWdodCIpLAoidXJsIiA6ICRzZWxfaWNvbi5nZXQoInVybCIpLAoiYXNwZWN0UmF0aW8iIDogJHNlbF9pY29uLmdldCgiYXNwZWN0UmF0aW8iKX0pCiNpZigkYWQuc2VjdXJlKQojc2V0ICgkc2VsX2ljb24udXJsPSJodHRwcyIrICRzZWxfaWNvbi51cmwuc3Vic3RyaW5nKCRzZWxfaWNvbi51cmwuaW5kZXhPZigiOi8vIikpKQojZWxzZQojc2V0ICgkc2VsX2ljb24udXJsPSJodHRwIisgJHNlbF9pY29uLnVybC5zdWJzdHJpbmcoJHNlbF9pY29uLnVybC5pbmRleE9mKCI6Ly8iKSkpCiNlbmQKI3NldCgkcHViQ29udGVudE1hcCA9ICB7ICJTYW1wbGUtVGl0bGUiIDogJHRpdGxlLAoJCQkiSUNPTiIgOiAkc2VsX2ljb24sCgkJCSJ2YXN0Q29udGVudCIgOiAkdmFzdENvbnRlbnQsCgkJCSJjb250cm9saWNvbi1wbGF5IiA6ICJodHRwczovL2lubW9iaS1pbnRzLWFkdG9vbC5zMy5hbWF6b25hd3MuY29tL3JlcGxheV9pY29uLnBuZyIsCgkJCSJjb250cm9saWNvbi1yZXBsYXkiIDogImh0dHBzOi8vaW5tb2JpLWludHMtYWR0b29sLnMzLmFtYXpvbmF3cy5jb20vcGxheV9pY29uLnBuZyJ9KQojc2V0KCRwdWJDb250ZW50ID0gJHRvb2wuanNvbkVuY29kZSgkcHViQ29udGVudE1hcCkpCiR0b29sLm5hdGl2ZUFkKCRmaXJzdCwgJHB1YkNvbnRlbnQpCwAFAAAAG3RlbXBsYXRlLXNlcnZpY2VAaW5tb2JpLmNvbQsABgAAAAMxLjAMAAgIAAEAAABkCAACAAAAZAAIAAkAAAABCAALAAAAAQIADQAADAAFDwABCAAAAAAIAAIAAAADAAwABgwAAQwAAQ8AAQwAAAABCAABAAAAAAgAAgAAAAAAAAIAAgAIAAMAAAABAAIAAwEIAAgAAAADAgAJAAAMAAcMAAMMAAEKAAEAAAAAAAAAAQgAAgAAAAIADAACCgABAAAAAAAAAAEIAAIAAAACAAAMAAQPAAEMAAAAAQgAAQAAAAAIAAIAAAAAAAAPAAYLAAAAAwAAAAlhZC52aWRlb3MAAAAIYWQuaWNvbnMAAAAIYWQudGl0bGUIAAsAAAAWAAIACAAIAAoAAAABAA==";
        test.genrateAndAddTemplateToCache(templateBinary);
        test.buildNativeAd();
    }


    private void genrateAndAddTemplateToCache(final String templateBinary) throws TException {
        final TDeserializer deserializer = new TDeserializer();
        final AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
        deserializer.deserialize(adTemplate, Base64.decodeBase64(templateBinary));
        // Print fields from ad Template
        System.out.println(adTemplate.getDemandConstraints().getJsonPath());
        System.out.println(adTemplate.getDetails().getContent());
        // Add to cache
        TemplateManager.getInstance().addToTemplateCache(TEMPLATE_ID, adTemplate.getDetails().getContent());
    }


    private void buildNativeAd() {
        try {
            final com.inmobi.template.context.App.Builder contextBuilder = com.inmobi.template.context.App.newBuilder();
            contextBuilder.setVastContent("xml");
            contextBuilder.setTitle(DefaultResponses.DEFAULT_TITLE);
            // Add Icons
            final Icon.Builder iconbuilder = Icon.newBuilder();
            iconbuilder.setUrl(DefaultResponses.DEFAULT_ICON.getUrl());
            iconbuilder.setW(DefaultResponses.DEFAULT_ICON.getW());
            iconbuilder.setH(DefaultResponses.DEFAULT_ICON.getH());
            contextBuilder.setIcons(Collections.singletonList((Icon) iconbuilder.build()));

            final Map<String, String> params = new HashMap<>();
            params.put(NativeResponseMaker.TEMPLATE_ID_PARAM, String.valueOf(TEMPLATE_ID));
            params.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, "IMAI_BASE_URL");

            final Injector injector = Guice.createInjector(new TemplateModule());
            final NativeResponseMaker nativeResponseMaker = new NativeResponseMaker(
                    injector.getInstance(TemplateParser.class), injector.getInstance(TemplateDecorator.class),
                    injector.getInstance(TemplateConfiguration.class));
            final String responseContent = nativeResponseMaker
                    .makeIXResponse((com.inmobi.template.context.App) contextBuilder.build(), params);
            System.out.println(responseContent);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
