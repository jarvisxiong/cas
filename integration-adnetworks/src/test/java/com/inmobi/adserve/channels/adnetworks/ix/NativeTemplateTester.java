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
                "CgABbLJswnzSXaUIAAIAAAACCAADAAAAAwwABAsAAQAAADNOYXRpdmUxLjAgdmlkZW9iZTE3YjEwMS1jMjEwLTQzYTgtYTRlYy1kZTY2ZGNjMWUxYWMLAAIAAAgDI3NldCAoJHRpdGxlID0gJHRvb2wuanBhdGgoJGZpcnN0LCJhZC50aXRsZSIpKQojc2V0ICgkdmFzdENvbnRlbnQgPSAkdG9vbC5nZXRWYXN0WE1sKCRmaXJzdCkpCiNpZiAoJHRvb2wuaXNOb25OdWxsKCR0aXRsZSkpCiNzZXQoJHRpdGxlID0gJHRpdGxlLnJlcGxhY2VBbGwoIlxzKyIsICIgIikpCiNlbmQKI3NldCgkaWNvbnMgPSAkdG9vbC5qcGF0aCgkZmlyc3QsImFkLmljb25zIikpCiNmb3JlYWNoKCAkaWNvbiBpbiAkaWNvbnMpCiMjIEFzc3VtaW5nIGljb24ncyBhc3BlY3QgcmF0aW8gaXMgYWx3YXlzIDE6MS4gTm8gZXh0cmEgbG9naWMgZm9yIEFSIGNoZWNrIHRvIGltcHJvdmUgcGVyZm9ybWFuY2UuCiNzZXQgKCR3aWR0aCA9ICRpY29uLmdldCgid2lkdGgiKSkKIyMgVHJ5IHRvIG1hdGNoIHRoZSBleGFjdCB3aWR0aC4gCiNpZiAoJHdpZHRoID09IDc1KQojc2V0ICgkc2VsX2ljb24gPSAkaWNvbikKI2JyZWFrCiNlbmQKI2lmICgkd2lkdGggPiAxNTAgJiYgJHdpZHRoIDw9IDMwMCkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRoX2ljb24pKQojc2V0ICgkaF9pY29uID0gJGljb24pCiNlbmQKI2Vsc2VpZiAoJHdpZHRoID4gNzUgJiYgJHdpZHRoIDw9IDE1MCkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRtX2ljb24pKSAKI3NldCAoJG1faWNvbiA9ICRpY29uKQojZW5kCiNlbHNlaWYgKCR3aWR0aCA+IDM3ICYmICR3aWR0aCA8PSA3NSkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRsX2ljb24pKQojc2V0ICgkbF9pY29uID0gJGljb24pCiNlbmQKI2VuZAojaWYgKCRoX2ljb24gJiYgJG1faWNvbiAmJiAkbF9pY29uKQojYnJlYWsgCiNlbmQKI2VuZAojIyBJZiBzZWxfaWNvbiBpcyBudWxsLCB0aGVuIHRyeSBzZWxlY3Rpbmcgb25lIG9mIHRoZSBoX2ljb24sIG1faWNvbiBvciBsX2ljb24gb3IgaW4gd29yc3QgY2FzZSB0aGUgZmlyc3QgaWNvbiBpbiB0aGUgaWNvbnMtYXJyYXkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRzZWxfaWNvbikpCiNpZiAoJHRvb2wuaXNOb25OdWxsKCRsX2ljb24pKQojc2V0ICgkc2VsX2ljb24gPSAkbF9pY29uKQojZWxzZWlmICgkdG9vbC5pc05vbk51bGwoJG1faWNvbikpCiNzZXQgKCRzZWxfaWNvbiA9ICRtX2ljb24pCiNlbHNlaWYgKCR0b29sLmlzTm9uTnVsbCgkaF9pY29uKSkKI3NldCAoJHNlbF9pY29uID0gJGhfaWNvbikKI2Vsc2UKI3NldCAoJHNlbF9pY29uID0gJGljb25zLmdldCgwKSkKI2VuZAojZW5kCiMjIENyZWF0aW5nIGEgY29weSBvZiB0aGUgc2VsX2ljb24gYmVsb3cgc28gdGhhdCB1cGRhdGlvbiBvZiB0aGUgdXJsIGRvZXMgbm90IGFmZmVjdCBhY3R1YWwganNvbiBvYmplY3QKI3NldCAoJHNlbF9pY29uID17IndpZHRoIiA6ICRzZWxfaWNvbi5nZXQoIndpZHRoIiksCiJoZWlnaHQiIDogJHNlbF9pY29uLmdldCgiaGVpZ2h0IiksCiJ1cmwiIDogJHNlbF9pY29uLmdldCgidXJsIiksCiJhc3BlY3RSYXRpbyIgOiAkc2VsX2ljb24uZ2V0KCJhc3BlY3RSYXRpbyIpfSkKI2lmKCRhZC5zZWN1cmUpCiNzZXQgKCRzZWxfaWNvbi51cmw9Imh0dHBzIisgJHNlbF9pY29uLnVybC5zdWJzdHJpbmcoJHNlbF9pY29uLnVybC5pbmRleE9mKCI6Ly8iKSkpCiNlbHNlCiNzZXQgKCRzZWxfaWNvbi51cmw9Imh0dHAiKyAkc2VsX2ljb24udXJsLnN1YnN0cmluZygkc2VsX2ljb24udXJsLmluZGV4T2YoIjovLyIpKSkKI2VuZAojc2V0KCRwdWJDb250ZW50TWFwID0gIHsgIlNhbXBsZS1UaXRsZSIgOiAkdGl0bGUsCgkJCSJJQ09OIiA6ICRzZWxfaWNvbiwKCQkJInZhc3RDb250ZW50IiA6ICR2YXN0Q29udGVudCwKCQkJImNvbnRyb2xpY29uLXBsYXkiIDogImh0dHBzOi8vaW5tb2JpLWludHMtYWR0b29sLnMzLmFtYXpvbmF3cy5jb20vcmVwbGF5X2ljb24ucG5nIiwKCQkJImNvbnRyb2xpY29uLXJlcGxheSIgOiAiaHR0cHM6Ly9pbm1vYmktaW50cy1hZHRvb2wuczMuYW1hem9uYXdzLmNvbS9wbGF5X2ljb24ucG5nIn0pCiNzZXQoJHB1YkNvbnRlbnQgPSAkdG9vbC5qc29uRW5jb2RlKCRwdWJDb250ZW50TWFwKSkKJHRvb2wubmF0aXZlQWQoJGZpcnN0LCAkcHViQ29udGVudCkKAAMAAAFUXAVeEQoABAAAAVReBx8VCwAFAAAADmJpa2FzaC5yYWpndXJ1CwAGAAAAAzEuMAwABwoAAQAAAAAAAAAADAACCgABAAAAAAAAAAAKAAIAAAAAAAAAAAAADAAICAABAAAAZAgAAgAAAGQACAAJAAAAAAgACgAAAAEIAAsAAAABAgANAA8ADwgAAAAAAAwABQ8AAQgAAAAACAACAAAAAwAMAAYMAAEMAAEPAAEMAAAAAQgAAQAAAAAIAAIAAAAAAAACAAIACAADAAAAAQAPAAIIAAAAAAIAAwECAAQACAAIAAAAAwIACQACAAwAAgANAAAMAAcIAAEAAAARDAADDAABCgABAAAAAAAAAAEIAAIAAAACAAwAAgoAAQAAAAAAAAABCAACAAAAAgAADAAEDwABDAAAAAEIAAEAAAAACAACAAAAAAAADwAGCwAAAAMAAAAJYWQudmlkZW9zAAAACGFkLmljb25zAAAACGFkLnRpdGxlCAALAAAAEQACAAgACAAKAAAAAQA=";
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
            contextBuilder.setActionText(DefaultResponses.DEFAULT_CTA);
            contextBuilder.setDesc(DefaultResponses.DEFAULT_DESC);
            contextBuilder.setRating(DefaultResponses.DEFAULT_RATING);
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
                    .makeIXResponse((com.inmobi.template.context.App) contextBuilder.build(), params, true);
            System.out.println(responseContent);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}