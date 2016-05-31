package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.template.context.MovieBoardAdData.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.trackers.MovieBoardResponseMaker;
import com.inmobi.adtemplate.platform.AdTemplate;
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
        //final String templateBinary =
          //      "CgABbLJswnzSXaUIAAIAAAACCAADAAAAAwwABAsAAQAAAC9Nb3ZpZUJvYXJkc2NmODdkYjA2LTZlM2EtNDYwOS1hN2FjLWU0MjY4ZWM1OWY5YgsAAgAAE/Ijc2V0KCRjcmVhdGl2ZURhdGEgPSBbXSkjZm9yZWFjaCgkY3JlYXRpdmUgaW4gJHthZC5jcmVhdGl2ZUxpc3R9KSNzZXQgKCRDQVJEX0NUQV9URVhUID0gJHRvb2wuZXZhbEpzb25QYXRoKCR7Y3JlYXRpdmUuanNvbk9iamVjdH0sImFkLmN0YS50ZXh0IikpI3NldCAoJFZBU1RfQ09OVEVOVCA9ICR2YXN0Q29udGVudCkgI2lmICggJHZhc3RYbWwgPT0gdHJ1ZSApI3NldCAoJFZBU1RfQ09OVEVOVCA9JHZhc3RDb250ZW50ICkjZWxzZSAjc2V0ICgkVkFTVF9DT05URU5UID0oIiNwYXJzZSgnd2lkZ2V0cy92aWRlb09uTmF0aXZlV2lkZ2V0LnZtJykiKSkpI2VuZCNzZXQgKCRhcHAgPSB7CgkJCSJhc3NldE5hbWUiOiAiY2FyZF9jb250YWluZXIiLAoJCQkiYXNzZXRUeXBlIjogImNvbnRhaW5lciIsCgkJCSJhc3NldFN0eWxlUmVmIjogImNhcmRDb250YWluZXJTdHlsZSIsCgkJCSJhc3NldE1pc2MiOiB7CgkJCQkibWlzYyI6ICRNSVNDLAoJCQkJIm1pc2NQcm9kIjogJE1JU0NQUk9ECgkJCX0sCgkJCSJhc3NldFZhbHVlIjogCgkJCVsKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogIml0ZW1fdmlkZW8iLAoJCQkJCSJhc3NldFR5cGUiOiAiVklERU8iLAoJCQkJCSJhc3NldElkIjogIiRBU1NFVF9JRCIsCgkJCQkJImFzc2V0U3R5bGUiOiB7CgkJCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkJCTAsCgkJCQkJCQkwLAoJCQkJCQkJMzQwLAoJCQkJCQkJMTkyCgkJCQkJCV0KCQkJCQl9LAoJCQkJCSJhc3NldFZhbHVlIjogWwoJCQkJCQkJIiRWQVNUX0NPTlRFTlQiCgkJCQkJXQoJCQkJfSwKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogImN0YSIsCgkJCQkJImFzc2V0VHlwZSI6ICJjb250YWluZXIiLAoJCQkJCSJhc3NldFN0eWxlIjogewoJCQkJCQkiYmFja2dyb3VuZENvbG9yIjogIiNmZkZCNEE4NyIsCgkJCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkJCTIzMCwKCQkJCQkJCTIwNiwKCQkJCQkJCTkwLAoJCQkJCQkJMzAKCQkJCQkJXSwKCQkJCQkJImJvcmRlciI6IHsKCQkJCQkgICAgICAgICAgICAgICAgImNvcm5lciI6ICJjdXJ2ZWQiLAoJCQkJCSAgICAgICAgICAgICAgICAic3R5bGUiOiAibm9uZSIsCgkJCQkJICAgICAgICAgICAgICAgICJjb2xvciI6ICIjZmZDMkNBQ0YiCgkJCQkJCSAgICAgICAgICB9CgkJCQkJfSwKCQkJCQkiYXNzZXRWYWx1ZSI6IFtdLAoJCQkJCSJhc3NldE9uY2xpY2siOiB7CgkJCQkJCSJpdGVtVXJsIjogIiRBU1NFVF9JRCIKCQkJCQl9CgkJCQl9LCAKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogImN0YV90ZXh0IiwKCQkJCQkiYXNzZXRUeXBlIjogIlRFWFQiLAoJCQkJCSJhc3NldFN0eWxlIjogewoJCQkJCQkiZ2VvbWV0cnkiOiBbCgkJCQkJCQkyNDMsCgkJCQkJCQkyMTMsCgkJCQkJCQk2NCwKCQkJCQkJCTE2CgkJCQkJCV0sCgkJCQkJCSJ0ZXh0IjogewoJCQkJCQkJImZvbnQiOiAiSGVsdmV0aWNhTmV1ZSIsCgkJCQkJCQkic2l6ZSI6ICIxMSIsCgkJCQkJCQkiY29sb3IiOiAiI2ZmZmZmZmZmIiwKCQkJCQkJCSJzdHlsZSI6IFsKCQkJCQkJCQkiYm9sZCIKCQkJCQkJCV0KCQkJCQkJfQoJCQkJCX0sCgkJCQkJImFzc2V0VmFsdWUiOiBbCgkJCQkJCSIkQ0FSRF9DVEFfVEVYVCIKCQkJCQldCgkJCQl9CgkJCV0KfSkgI3NldCgkYm9vbCA9ICRjcmVhdGl2ZURhdGEuYWRkKCRhcHApKSNlbmQjc2V0ICgkQ0FSRCA9ICRjcmVhdGl2ZURhdGEpI3NldCAoJERFQ0tfREVTQ1JJUFRJT04gPSAiSG93IG1hbnkgb2YgdGhlc2UgaW5zYW5lbHkgYWRkaWN0aXZlIGdhbWVzIGhhdmUgeW91IHRyaWVkPyIpI3NldCAoJERFQ0tfVFJBQ0tFUlMgPSBbXSkjZm9yZWFjaCgkaXQgaW4gJHthZC5ub0pzT2JqZWN0LmV0WzEyMF0udXJsc30pI3NldCAoJGxfdHJhY2tlciA9IHsidXJsIjokaXQsInRyYWNrZXJUeXBlIjoiVVJMX1BJTkciLCJ1aUV2ZW50IjoiUkVOREVSIn0pI3NldCgkYm9vbCA9ICRERUNLX1RSQUNLRVJTLmFkZCgkbF90cmFja2VyKSkjZW5kI3NldCAoJERFQ0tfSEVBREVSX0lNQUdFID0gImh0dHA6Ly9pbm1vYmlhc3NldHMuaW5tb2JpY2RuLm5ldC9zdG9yeWJvYXJkL2hlYWRlci5wbmciKSNzZXQgKCRERUNLX1RJVExFID0gIkhvdyBtYW55IG9mIHRoZXNlIGluc2FuZWx5IGFkZGljdGl2ZSBnYW1lcyBoYXZlIHlvdSB0cmllZD8iKSNzZXQgKCRERUNLX0lDT04gPSAiaHR0cDovL2lubW9iaWFzc2V0cy5pbm1vYmljZG4ubmV0L2dhbWluZy5wbmciKSNzZXQgKCRwdWJDb250ZW50ID0gewoJInZlcnNpb24iOiAwLjEsCgkic3R5bGVSZWZzIjogewoJCSJjYXJkQ29udGFpbmVyU3R5bGUiOiB7CgkJCSJnZW9tZXRyeSI6IFsKCQkJCTAsCgkJCQkwLAoJCQkJMzQwLAoJCQkJMjUwCgkJCV0sCgkJCSJiYWNrZ3JvdW5kQ29sb3IiOiAiI2ZmZmZmZmZmIgoJCX0KCX0sCgkicm9vdENvbnRhaW5lciI6IHsKCQkiYXNzZXROYW1lIjogInJvb3QiLAoJCSJhc3NldFR5cGUiOiAiY29udGFpbmVyIiwKCQkiYXNzZXRTdHlsZSI6IHsKCQkJImdlb21ldHJ5IjogWwoJCQkJMCwKCQkJCTAsCgkJCQkzNjAsCgkJCQkzMDAKCQkJXSwKCQkJImJhY2tncm91bmRDb2xvciI6ICIjY2NlNmU2ZTYiCgkJfSwKCQkidHJhY2tlcnMiOiRERUNLX1RSQUNLRVJTLAoJCSJhc3NldFZhbHVlIjogWwoJCXsKCQkJImFzc2V0TmFtZSI6ICJmaXhlZF9wYXJ0IiwKCQkJImFzc2V0VHlwZSI6ICJjb250YWluZXIiLAoJCQkiYXNzZXRTdHlsZSI6IHsKCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkxMCwKCQkJCQkxMCwKCQkJCQkzNDAsCgkJCQkJMjgwCgkJCQldLAoJCQkJImJhY2tncm91bmRDb2xvciI6ICIjZmZmZmZmZmYiCgkJCX0sCgkJCSJhc3NldFZhbHVlIjogWwoJCQkJewoJCQkJCSJhc3NldE5hbWUiOiAiY29udGFpbmVyXzEiLAoJCQkJCSJhc3NldFR5cGUiOiAiY29udGFpbmVyIiwKCQkJCQkiYXNzZXRTdHlsZSI6IHsKCQkJCQkJImJhY2tncm91bmRDb2xvciI6ICIjZmZmZmZmZmYiLAoJCQkJCQkiZ2VvbWV0cnkiOiBbCgkJCQkJCQkwLAoJCQkJCQkJMCwKCQkJCQkJCTM0MCwKCQkJCQkJCTI1CgkJCQkJCV0KCQkJCX0sCgkJCQkiYXNzZXRWYWx1ZSI6IFsKCQkJCQl7CgkJCQkJCSJhc3NldE5hbWUiOiAic3BvbnNvcmVkIiwKCQkJCQkJImFzc2V0VHlwZSI6ICJURVhUIiwKCQkJCQkJImFzc2V0U3R5bGUiOiB7CgkJCQkJCQkiZ2VvbWV0cnkiOiBbCgkJCQkJCQkJMjcwLAoJCQkJCQkJCTgsCgkJCQkJCQkJNjQsCgkJCQkJCQkJMTQKCQkJCQkJCV0sCgkJCQkJCQkidGV4dCI6IHsKCQkJCQkJCQkiZm9udCI6ICJIZWx2ZXRpY2FOZXVlIiwKCQkJCQkJCQkic2l6ZSI6ICIxMSIsCgkJCQkJCQkJImNvbG9yIjogIiM5OTAwMDAwMCIsCgkJCQkJCQkJInN0eWxlIjogWwoJCQkJCQkJCQkibm9uZSIKCQkJCQkJCQldCgkJCQkJCQl9CgkJCQkJCX0sCgkJCQkJCSJhc3NldFZhbHVlIjogWwoJCQkJCQkJIlNwb25zb3JlZCIKCQkJCQkJXQoJCQkJCX0KCQkJCV0KCQkJfQoJCQldCgkJfSwgCgkJewoJCQkiYXNzZXROYW1lIjogImNhcmRfbm9uX3Njcm9sbGFibGUiLAoJCQkiYXNzZXRUeXBlIjogImNvbnRhaW5lciIsCgkJCSJhc3NldFN0eWxlIjogewoJCQkJImdlb21ldHJ5IjogWwoJCQkJCTEwLAoJCQkJCTQwLAoJCQkJCTM0MCwKCQkJCQkyNTAKCQkJCV0sCgkJCQkidHJhbnNpdGlvbkRpcmVjdGlvbiI6ICJob3Jpem9udGFsIiwKCQkJCSJ0cmFuc2l0aW9uRWZmZWN0IjogInNsaWRlciIKCQkJfSwKCQkJImFzc2V0VmFsdWUiOiAkQ0FSRAoJCX0KCV0KCX0KfSkjc2V0ICgkcGFyZW50Vmlld1dpZHRoTG9jYWwgPSAke2FkLnJlcXVlc3RKc29uLmFQYXJhbXMucGFyZW50Vmlld1dpZHRofSkjc2V0ICgkcGFyZW50Vmlld1dpZHRoRGJsID0gJG1hdGgudG9Eb3VibGUoJHtwYXJlbnRWaWV3V2lkdGhMb2NhbH0pKSNzZXQgKCRzY2FsaW5nRmFjdG9yID0gJHBhcmVudFZpZXdXaWR0aERibC8zNTApI3NldCgkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvPSR0b29sLmV2YWxKc29uUGF0aCgke3B1YkNvbnRlbnR9LCJzdHlsZVJlZnMuY2FyZENvbnRhaW5lclN0eWxlLmdlb21ldHJ5IikpI2lmKCRjYXJkQ29udGFpbmVyU3R5bGVSZWZHZW8pI3NldCgkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzBdPSRjYXJkQ29udGFpbmVyU3R5bGVSZWZHZW9bMF0qJHNjYWxpbmdGYWN0b3IpI3NldCgkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzFdPSRjYXJkQ29udGFpbmVyU3R5bGVSZWZHZW9bMV0qJHNjYWxpbmdGYWN0b3IpI3NldCgkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzJdPSRjYXJkQ29udGFpbmVyU3R5bGVSZWZHZW9bMl0qJHNjYWxpbmdGYWN0b3IpI3NldCgkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzNdPSRjYXJkQ29udGFpbmVyU3R5bGVSZWZHZW9bM10qJHNjYWxpbmdGYWN0b3IpI2VuZCNzZXQoJHJvb3RDb250YWluZXI9JHtwdWJDb250ZW50LnJvb3RDb250YWluZXJ9KSNzZXQoJGNvbnRhaW5lclE9W10pI3NldCgkYm9vbD0kY29udGFpbmVyUS5hZGQoJHJvb3RDb250YWluZXIpKSNmb3JlYWNoKCRpIGluIFswLi4xMDAwMF0pI2lmKCRjb250YWluZXJRLmlzRW1wdHkoKSkjYnJlYWsjZW5kI3NldCgkY29udD0kY29udGFpbmVyUS5yZW1vdmUoMCkpI2lmKCRjb250LmFzc2V0U3R5bGUuZ2VvbWV0cnkpI3NldCgkZ2VvbWV0cnk9JHRvb2wuZXZhbEpzb25QYXRoKCRjb250LCJhc3NldFN0eWxlLmdlb21ldHJ5IikpI3NldCgkZ2VvbWV0cnlbMF09JGdlb21ldHJ5WzBdKiRzY2FsaW5nRmFjdG9yKSNzZXQoJGdlb21ldHJ5WzFdPSRnZW9tZXRyeVsxXSokc2NhbGluZ0ZhY3Rvcikjc2V0KCRnZW9tZXRyeVsyXT0kZ2VvbWV0cnlbMl0qJHNjYWxpbmdGYWN0b3IpI3NldCgkZ2VvbWV0cnlbM109JGdlb21ldHJ5WzNdKiRzY2FsaW5nRmFjdG9yKSNlbmQjZm9yZWFjaCgkY2hpbGQgaW4gJGNvbnQuYXNzZXRWYWx1ZSkjc2V0KCRhZGRlZD0kY29udGFpbmVyUS5hZGQoJGNoaWxkKSkjZW5kI2VuZCNzZXQgKCRwdWJDb250ZW50SnNvbiA9ICR0b29sLmpzb25FbmNvZGUoJHB1YkNvbnRlbnQpKSNzZXQgKCRwdWJDb250ZW50QmFzZTY0ID0gJHRvb2wuYmFzZTY0KCRwdWJDb250ZW50SnNvbikpI3NldCAoJGZpbmFsT3V0cHV0ID0geyJwdWJDb250ZW50IjogJHB1YkNvbnRlbnRCYXNlNjQgfSkkdG9vbC5qc29uRW5jb2RlKCRmaW5hbE91dHB1dCkLAAUAAAAbdGVtcGxhdGUtc2VydmljZUBpbm1vYmkuY29tCwAGAAAAAzEuMAwACAgAAQAAAGQIAAIAAABkAAgACQAAAAEIAAoAAAABCAALAAAAAQIADQAADAAFDwABCAAAAAAIAAIAAAADAAwABgwAAQwAAQ8AAQwAAAABCAABAAAAAAgAAgAAAAAAAAIAAgAIAAMAAAABAAIAAwEIAAgAAAADAgAJAAACAAgBDQAJCAwAAAAB/////w0AAQgMAAAAAgAAAAEIAAEAAAABDAACDAABCgABAAAAAAAAAAEIAAIAAAACAAwAAgoAAQAAAAAAAAABCAACAAAAAgAADAADDAADDAABCgABAAAAAAAAAAEIAAIAAAACAAwAAgoAAQAAAAAAAAABCAACAAAAAgAADAAEDwABDAAAAAEIAAEAAAAACAACAAAAAAAADwAGCwAAAAIAAAAHYWQudmFzdAAAAAthZC5jdGEudGV4dAgACwAAABEAAAAAAAIIAAEAAAABDAACDAABCgABAAAAAAAAAAEIAAIAAAABAAwAAgoAAQAAAAAAAAABCAACAAAAAQAADAADDAADDAABCgABAAAAAAAAAAEIAAIAAAACAAwAAgoAAQAAAAAAAAABCAACAAAAAgAADAAEDwABDAAAAAEIAAEAAAAACAACAAAAAAAADwAGCwAAAAIAAAAHYWQudmFzdAAAAAthZC5jdGEudGV4dAgACwAAABEAAAAIAAoAAAABAA==+IDM3ICYmICR3aWR0aCA8PSA3NSkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRsX2ljb24pKQojc2V0ICgkbF9pY29uID0gJGljb24pCiNlbmQKI2VuZAojaWYgKCRoX2ljb24gJiYgJG1faWNvbiAmJiAkbF9pY29uKQojYnJlYWsgCiNlbmQKI2VuZAojIyBJZiBzZWxfaWNvbiBpcyBudWxsLCB0aGVuIHRyeSBzZWxlY3Rpbmcgb25lIG9mIHRoZSBoX2ljb24sIG1faWNvbiBvciBsX2ljb24gb3IgaW4gd29yc3QgY2FzZSB0aGUgZmlyc3QgaWNvbiBpbiB0aGUgaWNvbnMtYXJyYXkKI2lmICghJHRvb2wuaXNOb25OdWxsKCRzZWxfaWNvbikpCiNpZiAoJHRvb2wuaXNOb25OdWxsKCRsX2ljb24pKQojc2V0ICgkc2VsX2ljb24gPSAkbF9pY29uKQojZWxzZWlmICgkdG9vbC5pc05vbk51bGwoJG1faWNvbikpCiNzZXQgKCRzZWxfaWNvbiA9ICRtX2ljb24pCiNlbHNlaWYgKCR0b29sLmlzTm9uTnVsbCgkaF9pY29uKSkKI3NldCAoJHNlbF9pY29uID0gJGhfaWNvbikKI2Vsc2UKI3NldCAoJHNlbF9pY29uID0gJGljb25zLmdldCgwKSkKI2VuZAojZW5kCiMjIENyZWF0aW5nIGEgY29weSBvZiB0aGUgc2VsX2ljb24gYmVsb3cgc28gdGhhdCB1cGRhdGlvbiBvZiB0aGUgdXJsIGRvZXMgbm90IGFmZmVjdCBhY3R1YWwganNvbiBvYmplY3QKI3NldCAoJHNlbF9pY29uID17IndpZHRoIiA6ICRzZWxfaWNvbi5nZXQoIndpZHRoIiksCiJoZWlnaHQiIDogJHNlbF9pY29uLmdldCgiaGVpZ2h0IiksCiJ1cmwiIDogJHNlbF9pY29uLmdldCgidXJsIiksCiJhc3BlY3RSYXRpbyIgOiAkc2VsX2ljb24uZ2V0KCJhc3BlY3RSYXRpbyIpfSkKI2lmKCRhZC5zZWN1cmUpCiNzZXQgKCRzZWxfaWNvbi51cmw9Imh0dHBzIisgJHNlbF9pY29uLnVybC5zdWJzdHJpbmcoJHNlbF9pY29uLnVybC5pbmRleE9mKCI6Ly8iKSkpCiNlbHNlCiNzZXQgKCRzZWxfaWNvbi51cmw9Imh0dHAiKyAkc2VsX2ljb24udXJsLnN1YnN0cmluZygkc2VsX2ljb24udXJsLmluZGV4T2YoIjovLyIpKSkKI2VuZAojc2V0KCRwdWJDb250ZW50TWFwID0gIHsgIlNhbXBsZS1UaXRsZSIgOiAkdGl0bGUsCgkJCSJJQ09OIiA6ICRzZWxfaWNvbiwKCQkJInZhc3RDb250ZW50IiA6ICR2YXN0Q29udGVudCwKCQkJImNvbnRyb2xpY29uLXBsYXkiIDogImh0dHBzOi8vaW5tb2JpLWludHMtYWR0b29sLnMzLmFtYXpvbmF3cy5jb20vcmVwbGF5X2ljb24ucG5nIiwKCQkJImNvbnRyb2xpY29uLXJlcGxheSIgOiAiaHR0cHM6Ly9pbm1vYmktaW50cy1hZHRvb2wuczMuYW1hem9uYXdzLmNvbS9wbGF5X2ljb24ucG5nIn0pCiNzZXQoJHB1YkNvbnRlbnQgPSAkdG9vbC5qc29uRW5jb2RlKCRwdWJDb250ZW50TWFwKSkKJHRvb2wubmF0aXZlQWQoJGZpcnN0LCAkcHViQ29udGVudCkKAAMAAAFUXAVeEQoABAAAAVReBx8VCwAFAAAADmJpa2FzaC5yYWpndXJ1CwAGAAAAAzEuMAwABwoAAQAAAAAAAAAADAACCgABAAAAAAAAAAAKAAIAAAAAAAAAAAAADAAICAABAAAAZAgAAgAAAGQACAAJAAAAAAgACgAAAAEIAAsAAAABAgANAA8ADwgAAAAAAAwABQ8AAQgAAAAACAACAAAAAwAMAAYMAAEMAAEPAAEMAAAAAQgAAQAAAAAIAAIAAAAAAAACAAIACAADAAAAAQAPAAIIAAAAAAIAAwECAAQACAAIAAAAAwIACQACAAwAAgANAAAMAAcIAAEAAAARDAADDAABCgABAAAAAAAAAAEIAAIAAAACAAwAAgoAAQAAAAAAAAABCAACAAAAAgAADAAEDwABDAAAAAEIAAEAAAAACAACAAAAAAAADwAGCwAAAAMAAAAJYWQudmlkZW9zAAAACGFkLmljb25zAAAACGFkLnRpdGxlCAALAAAAEQACAAgACAAKAAAAAQA=";
        final String templateBinary =
                "CgABbLJswnzSXaUIAAIAAAACCAADAAAAAwwABAsAAQAAAC9Nb3ZpZUJvYXJkc2NmODdkYjA2LTZlM2EtNDYwOS1hN2FjLWU0MjY4ZWM1OWY5YgsAAgAAFXYjc2V0KCRjcmVhdGl2ZURhdGEgPSBbXSkjZm9yZWFjaCgkY3JlYXRpdmUgaW4gJHthZC5jcmVhdGl2ZUxpc3R9KSNzZXQgKCRBU1NFVF9JRD0gJG1hdGgucmFuZG9tKCAxLCAyMDApKSNzZXQgKCRDQVJEX0NUQV9URVhUID0gJHRvb2wuZXZhbEpzb25QYXRoKCR7Y3JlYXRpdmUuanNvbk9iamVjdH0sImFkLmN0YS50ZXh0IikpI3NldCAoJFZBU1RfQ09OVEVOVCA9ICR2YXN0Q29udGVudCkgI2lmICggJHZhc3RYbWwgPT0gdHJ1ZSApI3NldCAoJFZBU1RfQ09OVEVOVCA9JHRvb2wuZ2V0VmFzdFhNbCgkYWQpICkjZWxzZSAjc2V0ICgkVkFTVF9DT05URU5UID0oIiNwYXJzZSgnd2lkZ2V0cy92aWRlb09uTmF0aXZlV2lkZ2V0LnZtJykiKSkpI2VuZCNzZXQgKCRhcHAgPSB7CgkJCSJhc3NldE5hbWUiOiAiY2FyZF9jb250YWluZXIiLAoJCQkiYXNzZXRUeXBlIjogImNvbnRhaW5lciIsCgkJCSJhc3NldFN0eWxlUmVmIjogImNhcmRDb250YWluZXJTdHlsZSIsCgkJCSJhc3NldE1pc2MiOiB7CgkJCQkibWlzYyI6ICRNSVNDLAoJCQkJIm1pc2NQcm9kIjogJE1JU0NQUk9ECgkJCX0sCgkJCSJhc3NldFZhbHVlIjogCgkJCVsKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogIml0ZW1fdmlkZW8iLAoJCQkJCSJhc3NldFR5cGUiOiAiVklERU8iLAoJCQkJCSJhc3NldElkIjogIiRBU1NFVF9JRCIsCgkJCQkJImFzc2V0U3R5bGUiOiB7CgkJCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkJCTAsCgkJCQkJCQkwLAoJCQkJCQkJMzQwLAoJCQkJCQkJMTkyCgkJCQkJCV0KCQkJCQl9LAoJCQkJCSJhc3NldFZhbHVlIjogWwoJCQkJCQkJIiRWQVNUX0NPTlRFTlQiCgkJCQkJXQoJCQkJfSwKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogImN0YSIsCgkJCQkJImFzc2V0VHlwZSI6ICJjb250YWluZXIiLAoJCQkJCSJhc3NldFN0eWxlIjogewoJCQkJCQkiYmFja2dyb3VuZENvbG9yIjogIiNmZkZCNEE4NyIsCgkJCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkJCTIzMCwKCQkJCQkJCTIwNiwKCQkJCQkJCTkwLAoJCQkJCQkJMzAKCQkJCQkJXSwKCQkJCQkJImJvcmRlciI6IHsKCQkJCQkgICAgICAgICAgICAgICAgImNvcm5lciI6ICJjdXJ2ZWQiLAoJCQkJCSAgICAgICAgICAgICAgICAic3R5bGUiOiAibm9uZSIsCgkJCQkJICAgICAgICAgICAgICAgICJjb2xvciI6ICIjZmZDMkNBQ0YiCgkJCQkJCSAgICAgICAgICB9CgkJCQkJfSwKCQkJCQkiYXNzZXRWYWx1ZSI6IFtdLAoJCQkJCSJhc3NldE9uY2xpY2siOiB7CgkJCQkJCSJpdGVtVXJsIjogIiRBU1NFVF9JRCIKCQkJCQl9CgkJCQl9LCAKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogImN0YV90ZXh0IiwKCQkJCQkiYXNzZXRUeXBlIjogIlRFWFQiLAoJCQkJCSJhc3NldFN0eWxlIjogewoJCQkJCQkiZ2VvbWV0cnkiOiBbCgkJCQkJCQkyNDMsCgkJCQkJCQkyMTMsCgkJCQkJCQk2NCwKCQkJCQkJCTE2CgkJCQkJCV0sCgkJCQkJCSJ0ZXh0IjogewoJCQkJCQkJImZvbnQiOiAiSGVsdmV0aWNhTmV1ZSIsCgkJCQkJCQkic2l6ZSI6ICIxMSIsCgkJCQkJCQkiY29sb3IiOiAiI2ZmZmZmZmZmIiwKCQkJCQkJCSJzdHlsZSI6IFsKCQkJCQkJCQkiYm9sZCIKCQkJCQkJCV0KCQkJCQkJfQoJCQkJCX0sCgkJCQkJImFzc2V0VmFsdWUiOiBbCgkJCQkJCSIkQ0FSRF9DVEFfVEVYVCIKCQkJCQldCgkJCQl9CgkJCV0KfSkgI3NldCgkYm9vbCA9ICRjcmVhdGl2ZURhdGEuYWRkKCRhcHApKSNlbmQjc2V0ICgkQ0FSRCA9ICRjcmVhdGl2ZURhdGEpI3NldCAoJERFQ0tfREVTQ1JJUFRJT04gPSAiSG93IG1hbnkgb2YgdGhlc2UgaW5zYW5lbHkgYWRkaWN0aXZlIGdhbWVzIGhhdmUgeW91IHRyaWVkPyIpI3NldCAoJERFQ0tfVFJBQ0tFUlMgPSBbXSkjZm9yZWFjaCgkaXQgaW4gJHthZC5ub0pzT2JqZWN0LmV0WzEyMF0udXJsc30pI3NldCAoJGxfdHJhY2tlciA9IHsidXJsIjokaXQsInRyYWNrZXJUeXBlIjoiVVJMX1BJTkciLCJ1aUV2ZW50IjoiQ0xJRU5UX0ZJTEwifSkjc2V0KCRib29sID0gJERFQ0tfVFJBQ0tFUlMuYWRkKCRsX3RyYWNrZXIpKSNlbmQjZm9yZWFjaCgkaXQgaW4gJHthZC5ub0pzT2JqZWN0LmV0WzE4XS51cmxzfSkjc2V0ICgkbF90cmFja2VyID0geyJ1cmwiOiRpdCwidHJhY2tlclR5cGUiOiJVUkxfUElORyIsInVpRXZlbnQiOiJSRU5ERVIifSkjc2V0KCRib29sID0gJERFQ0tfVFJBQ0tFUlMuYWRkKCRsX3RyYWNrZXIpKSNlbmQjZm9yZWFjaCgkaXQgaW4gJHthZC5ub0pzT2JqZWN0LmV0WzFdLnVybHN9KSNzZXQgKCRsX3RyYWNrZXIgPSB7InVybCI6JGl0LCJ0cmFja2VyVHlwZSI6IlVSTF9QSU5HIiwidWlFdmVudCI6IlJFTkRFUiJ9KSNzZXQoJGJvb2wgPSAkREVDS19UUkFDS0VSUy5hZGQoJGxfdHJhY2tlcikpI2VuZCNzZXQgKCRERUNLX0hFQURFUl9JTUFHRSA9ICJodHRwOi8vaW5tb2JpYXNzZXRzLmlubW9iaWNkbi5uZXQvc3Rvcnlib2FyZC9oZWFkZXIucG5nIikjc2V0ICgkREVDS19USVRMRSA9ICJIb3cgbWFueSBvZiB0aGVzZSBpbnNhbmVseSBhZGRpY3RpdmUgZ2FtZXMgaGF2ZSB5b3UgdHJpZWQ/Iikjc2V0ICgkREVDS19JQ09OID0gImh0dHA6Ly9pbm1vYmlhc3NldHMuaW5tb2JpY2RuLm5ldC9nYW1pbmcucG5nIikjc2V0ICgkcHViQ29udGVudCA9IHsKCSJ2ZXJzaW9uIjogMC4xLAoJInN0eWxlUmVmcyI6IHsKCQkiY2FyZENvbnRhaW5lclN0eWxlIjogewoJCQkiZ2VvbWV0cnkiOiBbCgkJCQkwLAoJCQkJMCwKCQkJCTM0MCwKCQkJCTI1MAoJCQldLAoJCQkiYmFja2dyb3VuZENvbG9yIjogIiNmZmZmZmZmZiIKCQl9Cgl9LAoJInJvb3RDb250YWluZXIiOiB7CgkJImFzc2V0TmFtZSI6ICJyb290IiwKCQkiYXNzZXRUeXBlIjogImNvbnRhaW5lciIsCgkJImFzc2V0U3R5bGUiOiB7CgkJCSJnZW9tZXRyeSI6IFsKCQkJCTAsCgkJCQkwLAoJCQkJMzYwLAoJCQkJMzAwCgkJCV0sCgkJCSJiYWNrZ3JvdW5kQ29sb3IiOiAiI2NjZTZlNmU2IgoJCX0sCgkJInRyYWNrZXJzIjokREVDS19UUkFDS0VSUywKCQkiYXNzZXRWYWx1ZSI6IFsKCQl7CgkJCSJhc3NldE5hbWUiOiAiZml4ZWRfcGFydCIsCgkJCSJhc3NldFR5cGUiOiAiY29udGFpbmVyIiwKCQkJImFzc2V0U3R5bGUiOiB7CgkJCQkiZ2VvbWV0cnkiOiBbCgkJCQkJMTAsCgkJCQkJMTAsCgkJCQkJMzQwLAoJCQkJCTI4MAoJCQkJXSwKCQkJCSJiYWNrZ3JvdW5kQ29sb3IiOiAiI2ZmZmZmZmZmIgoJCQl9LAoJCQkiYXNzZXRWYWx1ZSI6IFsKCQkJCXsKCQkJCQkiYXNzZXROYW1lIjogImNvbnRhaW5lcl8xIiwKCQkJCQkiYXNzZXRUeXBlIjogImNvbnRhaW5lciIsCgkJCQkJImFzc2V0U3R5bGUiOiB7CgkJCQkJCSJiYWNrZ3JvdW5kQ29sb3IiOiAiI2ZmZmZmZmZmIiwKCQkJCQkJImdlb21ldHJ5IjogWwoJCQkJCQkJMCwKCQkJCQkJCTAsCgkJCQkJCQkzNDAsCgkJCQkJCQkyNQoJCQkJCQldCgkJCQl9LAoJCQkJImFzc2V0VmFsdWUiOiBbCgkJCQkJewoJCQkJCQkiYXNzZXROYW1lIjogInNwb25zb3JlZCIsCgkJCQkJCSJhc3NldFR5cGUiOiAiVEVYVCIsCgkJCQkJCSJhc3NldFN0eWxlIjogewoJCQkJCQkJImdlb21ldHJ5IjogWwoJCQkJCQkJCTI3MCwKCQkJCQkJCQk4LAoJCQkJCQkJCTY0LAoJCQkJCQkJCTE0CgkJCQkJCQldLAoJCQkJCQkJInRleHQiOiB7CgkJCQkJCQkJImZvbnQiOiAiSGVsdmV0aWNhTmV1ZSIsCgkJCQkJCQkJInNpemUiOiAiMTEiLAoJCQkJCQkJCSJjb2xvciI6ICIjOTkwMDAwMDAiLAoJCQkJCQkJCSJzdHlsZSI6IFsKCQkJCQkJCQkJIm5vbmUiCgkJCQkJCQkJXQoJCQkJCQkJfQoJCQkJCQl9LAoJCQkJCQkiYXNzZXRWYWx1ZSI6IFsKCQkJCQkJCSJTcG9uc29yZWQiCgkJCQkJCV0KCQkJCQl9CgkJCQldCgkJCX0KCQkJXQoJCX0sIAoJCXsKCQkJImFzc2V0TmFtZSI6ICJjYXJkX25vbl9zY3JvbGxhYmxlIiwKCQkJImFzc2V0VHlwZSI6ICJjb250YWluZXIiLAoJCQkiYXNzZXRTdHlsZSI6IHsKCQkJCSJnZW9tZXRyeSI6IFsKCQkJCQkxMCwKCQkJCQk0MCwKCQkJCQkzNDAsCgkJCQkJMjUwCgkJCQldLAoJCQkJInRyYW5zaXRpb25EaXJlY3Rpb24iOiAiaG9yaXpvbnRhbCIsCgkJCQkidHJhbnNpdGlvbkVmZmVjdCI6ICJzbGlkZXIiCgkJCX0sCgkJCSJhc3NldFZhbHVlIjogJENBUkQKCQl9CgldCgl9Cn0pI3NldCAoJHBhcmVudFZpZXdXaWR0aExvY2FsID0gJHthZC5yZXF1ZXN0SnNvbi5hUGFyYW1zLnBhcmVudFZpZXdXaWR0aH0pI3NldCAoJHBhcmVudFZpZXdXaWR0aERibCA9ICRtYXRoLnRvRG91YmxlKCR7cGFyZW50Vmlld1dpZHRoTG9jYWx9KSkjc2V0ICgkc2NhbGluZ0ZhY3RvciA9ICRwYXJlbnRWaWV3V2lkdGhEYmwvMzUwKSNzZXQoJGNhcmRDb250YWluZXJTdHlsZVJlZkdlbz0kdG9vbC5ldmFsSnNvblBhdGgoJHtwdWJDb250ZW50fSwic3R5bGVSZWZzLmNhcmRDb250YWluZXJTdHlsZS5nZW9tZXRyeSIpKSNpZigkY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvKSNzZXQoJGNhcmRDb250YWluZXJTdHlsZVJlZkdlb1swXT0kY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzBdKiRzY2FsaW5nRmFjdG9yKSNzZXQoJGNhcmRDb250YWluZXJTdHlsZVJlZkdlb1sxXT0kY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzFdKiRzY2FsaW5nRmFjdG9yKSNzZXQoJGNhcmRDb250YWluZXJTdHlsZVJlZkdlb1syXT0kY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzJdKiRzY2FsaW5nRmFjdG9yKSNzZXQoJGNhcmRDb250YWluZXJTdHlsZVJlZkdlb1szXT0kY2FyZENvbnRhaW5lclN0eWxlUmVmR2VvWzNdKiRzY2FsaW5nRmFjdG9yKSNlbmQjc2V0KCRyb290Q29udGFpbmVyPSR7cHViQ29udGVudC5yb290Q29udGFpbmVyfSkjc2V0KCRjb250YWluZXJRPVtdKSNzZXQoJGJvb2w9JGNvbnRhaW5lclEuYWRkKCRyb290Q29udGFpbmVyKSkjZm9yZWFjaCgkaSBpbiBbMC4uMTAwMDBdKSNpZigkY29udGFpbmVyUS5pc0VtcHR5KCkpI2JyZWFrI2VuZCNzZXQoJGNvbnQ9JGNvbnRhaW5lclEucmVtb3ZlKDApKSNpZigkY29udC5hc3NldFN0eWxlLmdlb21ldHJ5KSNzZXQoJGdlb21ldHJ5PSR0b29sLmV2YWxKc29uUGF0aCgkY29udCwiYXNzZXRTdHlsZS5nZW9tZXRyeSIpKSNzZXQoJGdlb21ldHJ5WzBdPSRnZW9tZXRyeVswXSokc2NhbGluZ0ZhY3Rvcikjc2V0KCRnZW9tZXRyeVsxXT0kZ2VvbWV0cnlbMV0qJHNjYWxpbmdGYWN0b3IpI3NldCgkZ2VvbWV0cnlbMl09JGdlb21ldHJ5WzJdKiRzY2FsaW5nRmFjdG9yKSNzZXQoJGdlb21ldHJ5WzNdPSRnZW9tZXRyeVszXSokc2NhbGluZ0ZhY3RvcikjZW5kI2ZvcmVhY2goJGNoaWxkIGluICRjb250LmFzc2V0VmFsdWUpI3NldCgkYWRkZWQ9JGNvbnRhaW5lclEuYWRkKCRjaGlsZCkpI2VuZCNlbmQjc2V0ICgkcHViQ29udGVudEpzb24gPSAkdG9vbC5qc29uRW5jb2RlKCRwdWJDb250ZW50KSkjc2V0ICgkcHViQ29udGVudEJhc2U2NCA9ICR0b29sLmJhc2U2NCgkcHViQ29udGVudEpzb24pKSNzZXQgKCRmaW5hbE91dHB1dCA9IHsicHViQ29udGVudCI6ICRwdWJDb250ZW50QmFzZTY0IH0pJHRvb2wuanNvbkVuY29kZSgkZmluYWxPdXRwdXQpCwAFAAAAG3RlbXBsYXRlLXNlcnZpY2VAaW5tb2JpLmNvbQsABgAAAAMxLjAMAAgIAAEAAABkCAACAAAAZAAIAAkAAAABCAAKAAAAAQgACwAAAAECAA0AAAwABQ8AAQgAAAAACAACAAAAAwAMAAYMAAEMAAEPAAEMAAAAAQgAAQAAAAAIAAIAAAAAAAACAAIACAADAAAAAQACAAMBCAAIAAAAAwIACQAAAgAIAQ0ACQgMAAAAAf////8NAAEIDAAAAAIAAAABCAABAAAAAQwAAgwAAQoAAQAAAAAAAAABCAACAAAAAgAMAAIKAAEAAAAAAAAAAQgAAgAAAAIAAAwAAwwAAwwAAQoAAQAAAAAAAAABCAACAAAAAgAMAAIKAAEAAAAAAAAAAQgAAgAAAAIAAAwABA8AAQwAAAABCAABAAAAAAgAAgAAAAAAAA8ABgsAAAACAAAAB2FkLnZhc3QAAAALYWQuY3RhLnRleHQIAAsAAAARAAAAAAACCAABAAAAAQwAAgwAAQoAAQAAAAAAAAABCAACAAAAAQAMAAIKAAEAAAAAAAAAAQgAAgAAAAEAAAwAAwwAAwwAAQoAAQAAAAAAAAABCAACAAAAAgAMAAIKAAEAAAAAAAAAAQgAAgAAAAIAAAwABA8AAQwAAAABCAABAAAAAAgAAgAAAAAAAA8ABgsAAAACAAAAB2FkLnZhc3QAAAALYWQuY3RhLnRleHQIAAsAAAARAAAACAAKAAAAAQA=";
        test.genrateAndAddTemplateToCache(templateBinary);
        test.buildNativeAd();
    }


    private void genrateAndAddTemplateToCache(final String templateBinary) throws TException {
        final TDeserializer deserializer = new TDeserializer();
        final AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
        deserializer.deserialize(adTemplate, Base64.decodeBase64(templateBinary));
        // Print fields from ad Template
        System.out.println(adTemplate.getDemandConstraints().getJsonPath());
        String templateString = "#set($creativeData = [])#foreach($creative in ${ad.creativeList})#set ($ASSET_ID= $math.random( 1, 200))#set ($CARD_CTA_TEXT = $tool.evalJsonPath(${creative.jsonObject},\"ad.cta.text\"))#set ($VAST_CONTENT = $vastContent) #if ( $vastXml == true )#set ($VAST_CONTENT =$tool.getVastXMl($ad) )#else #set ($VAST_CONTENT =(\"#parse('widgets/videoOnNativeWidget.vm')\")))#end#set ($app = {\n" +
                "\t\t\t\"assetName\": \"card_container\",\n" +
                "\t\t\t\"assetType\": \"container\",\n" +
                "\t\t\t\"assetStyleRef\": \"cardContainerStyle\",\n" +
                "\t\t\t\"assetMisc\": {\n" +
                "\t\t\t\t\"misc\": $MISC,\n" +
                "\t\t\t\t\"miscProd\": $MISCPROD\n" +
                "\t\t\t},\n" +
                "\t\t\t\"assetValue\": \n" +
                "\t\t\t[\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"assetName\": \"item_video\",\n" +
                "\t\t\t\t\t\"assetType\": \"VIDEO\",\n" +
                "\t\t\t\t\t\"assetId\": \"$ASSET_ID\",\n" +
                "\t\t\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t\t\t0,\n" +
                "\t\t\t\t\t\t\t0,\n" +
                "\t\t\t\t\t\t\t340,\n" +
                "\t\t\t\t\t\t\t192\n" +
                "\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"assetValue\": [\n" +
                "\t\t\t\t\t\t\t\"$VAST_CONTENT\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"assetName\": \"cta\",\n" +
                "\t\t\t\t\t\"assetType\": \"container\",\n" +
                "\t\t\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\t\t\"backgroundColor\": \"#ffFB4A87\",\n" +
                "\t\t\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t\t\t230,\n" +
                "\t\t\t\t\t\t\t206,\n" +
                "\t\t\t\t\t\t\t90,\n" +
                "\t\t\t\t\t\t\t30\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"border\": {\n" +
                "\t\t\t\t\t                \"corner\": \"curved\",\n" +
                "\t\t\t\t\t                \"style\": \"none\",\n" +
                "\t\t\t\t\t                \"color\": \"#ffC2CACF\"\n" +
                "\t\t\t\t\t\t          }\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"assetValue\": [],\n" +
                "\t\t\t\t\t\"assetOnclick\": {\n" +
                "\t\t\t\t\t\t\"itemUrl\": \"$ASSET_ID\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}, \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"assetName\": \"cta_text\",\n" +
                "\t\t\t\t\t\"assetType\": \"TEXT\",\n" +
                "\t\t\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t\t\t243,\n" +
                "\t\t\t\t\t\t\t213,\n" +
                "\t\t\t\t\t\t\t64,\n" +
                "\t\t\t\t\t\t\t16\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"text\": {\n" +
                "\t\t\t\t\t\t\t\"font\": \"HelveticaNeue\",\n" +
                "\t\t\t\t\t\t\t\"size\": \"11\",\n" +
                "\t\t\t\t\t\t\t\"color\": \"#ffffffff\",\n" +
                "\t\t\t\t\t\t\t\"style\": [\n" +
                "\t\t\t\t\t\t\t\t\"bold\"\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"assetValue\": [\n" +
                "\t\t\t\t\t\t\"$CARD_CTA_TEXT\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "}) #set($bool = $creativeData.add($app))#end#set ($CARD = $creativeData)#set ($DECK_DESCRIPTION = \"How many of these insanely addictive games have you tried?\")#set ($DECK_TRACKERS = [])#foreach($it in ${ad.noJsObject.et[120].urls})#set ($l_tracker = {\"url\":$it,\"trackerType\":\"URL_PING\",\"uiEvent\":\"CLIENT_FILL\"})#set($bool = $DECK_TRACKERS.add($l_tracker))#end#foreach($it in ${ad.noJsObject.et[18].urls})#set ($l_tracker = {\"url\":$it,\"trackerType\":\"URL_PING\",\"uiEvent\":\"RENDER\"})#set($bool = $DECK_TRACKERS.add($l_tracker))#end#foreach($it in ${ad.noJsObject.et[1].urls})#set ($l_tracker = {\"url\":$it,\"trackerType\":\"URL_PING\",\"uiEvent\":\"RENDER\"})#set($bool = $DECK_TRACKERS.add($l_tracker))#end#set ($DECK_HEADER_IMAGE = \"http://inmobiassets.inmobicdn.net/storyboard/header.png\")#set ($DECK_TITLE = \"How many of these insanely addictive games have you tried?\")#set ($DECK_ICON = \"http://inmobiassets.inmobicdn.net/gaming.png\")#set ($pubContent = {\n" +
                "\t\"version\": 0.1,\n" +
                "\t\"styleRefs\": {\n" +
                "\t\t\"cardContainerStyle\": {\n" +
                "\t\t\t\"geometry\": [\n" +
                "\t\t\t\t0,\n" +
                "\t\t\t\t0,\n" +
                "\t\t\t\t340,\n" +
                "\t\t\t\t250\n" +
                "\t\t\t],\n" +
                "\t\t\t\"backgroundColor\": \"#ffffffff\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"rootContainer\": {\n" +
                "\t\t\"assetName\": \"root\",\n" +
                "\t\t\"assetType\": \"container\",\n" +
                "\t\t\"assetStyle\": {\n" +
                "\t\t\t\"geometry\": [\n" +
                "\t\t\t\t0,\n" +
                "\t\t\t\t0,\n" +
                "\t\t\t\t360,\n" +
                "\t\t\t\t300\n" +
                "\t\t\t],\n" +
                "\t\t\t\"backgroundColor\": \"#cce6e6e6\"\n" +
                "\t\t},\n" +
                "\t\t\"trackers\":$DECK_TRACKERS,\n" +
                "\t\t\"assetValue\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"assetName\": \"fixed_part\",\n" +
                "\t\t\t\"assetType\": \"container\",\n" +
                "\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t10,\n" +
                "\t\t\t\t\t10,\n" +
                "\t\t\t\t\t340,\n" +
                "\t\t\t\t\t280\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"backgroundColor\": \"#ffffffff\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"assetValue\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"assetName\": \"container_1\",\n" +
                "\t\t\t\t\t\"assetType\": \"container\",\n" +
                "\t\t\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\t\t\"backgroundColor\": \"#ffffffff\",\n" +
                "\t\t\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t\t\t0,\n" +
                "\t\t\t\t\t\t\t0,\n" +
                "\t\t\t\t\t\t\t340,\n" +
                "\t\t\t\t\t\t\t25\n" +
                "\t\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"assetValue\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"assetName\": \"sponsored\",\n" +
                "\t\t\t\t\t\t\"assetType\": \"TEXT\",\n" +
                "\t\t\t\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t\t\t\t270,\n" +
                "\t\t\t\t\t\t\t\t8,\n" +
                "\t\t\t\t\t\t\t\t64,\n" +
                "\t\t\t\t\t\t\t\t14\n" +
                "\t\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\t\"text\": {\n" +
                "\t\t\t\t\t\t\t\t\"font\": \"HelveticaNeue\",\n" +
                "\t\t\t\t\t\t\t\t\"size\": \"11\",\n" +
                "\t\t\t\t\t\t\t\t\"color\": \"#99000000\",\n" +
                "\t\t\t\t\t\t\t\t\"style\": [\n" +
                "\t\t\t\t\t\t\t\t\t\"none\"\n" +
                "\t\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"assetValue\": [\n" +
                "\t\t\t\t\t\t\t\"Sponsored\"\n" +
                "\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t]\n" +
                "\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}, \n" +
                "\t\t{\n" +
                "\t\t\t\"assetName\": \"card_non_scrollable\",\n" +
                "\t\t\t\"assetType\": \"container\",\n" +
                "\t\t\t\"assetStyle\": {\n" +
                "\t\t\t\t\"geometry\": [\n" +
                "\t\t\t\t\t10,\n" +
                "\t\t\t\t\t40,\n" +
                "\t\t\t\t\t340,\n" +
                "\t\t\t\t\t250\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"transitionDirection\": \"horizontal\",\n" +
                "\t\t\t\t\"transitionEffect\": \"slider\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"assetValue\": $CARD\n" +
                "\t\t}\n" +
                "\t]\n" +
                "\t}\n" +
                "})#set ($parentViewWidthLocal = ${ad.requestJson.aParams.parentViewWidth})#set ($parentViewWidthDbl = $math.toDouble(${parentViewWidthLocal}))#set ($scalingFactor = $parentViewWidthDbl/350)#set($cardContainerStyleRefGeo=$tool.evalJsonPath(${pubContent},\"styleRefs.cardContainerStyle.geometry\"))#if($cardContainerStyleRefGeo)#set($cardContainerStyleRefGeo[0]=$cardContainerStyleRefGeo[0]*$scalingFactor)#set($cardContainerStyleRefGeo[1]=$cardContainerStyleRefGeo[1]*$scalingFactor)#set($cardContainerStyleRefGeo[2]=$cardContainerStyleRefGeo[2]*$scalingFactor)#set($cardContainerStyleRefGeo[3]=$cardContainerStyleRefGeo[3]*$scalingFactor)#end#set($rootContainer=${pubContent.rootContainer})#set($containerQ=[])#set($bool=$containerQ.add($rootContainer))#foreach($i in [0..10000])#if($containerQ.isEmpty())#break#end#set($cont=$containerQ.remove(0))#if($cont.assetStyle.geometry)#set($geometry=$tool.evalJsonPath($cont,\"assetStyle.geometry\"))#set($geometry[0]=$geometry[0]*$scalingFactor)#set($geometry[1]=$geometry[1]*$scalingFactor)#set($geometry[2]=$geometry[2]*$scalingFactor)#set($geometry[3]=$geometry[3]*$scalingFactor)#end#foreach($child in $cont.assetValue)#set($added=$containerQ.add($child))#end#end#set ($pubContentJson = $tool.jsonEncode($pubContent))#set ($pubContentBase64 = $tool.base64($pubContentJson))#set ($finalOutput = {\"pubContent\": $pubContentBase64 })$tool.jsonEncode($finalOutput)";
        adTemplate.getDetails().setContent(templateString);
        System.out.println(adTemplate.getDetails().getContent());
        // Add to cache
        TemplateManager.getInstance().addToTemplateCache(TEMPLATE_ID, adTemplate.getDetails().getContent());
    }


    private void buildNativeAd() {
        try {
            /*
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
                    */

            final Map<String, String> params = new HashMap<>();
            params.put(NativeResponseMaker.TEMPLATE_ID_PARAM, String.valueOf(TEMPLATE_ID));


            final Injector injector = Guice.createInjector(new TemplateModule());
            final MovieBoardResponseMaker movieBoardResponseMaker = new MovieBoardResponseMaker(
                    injector.getInstance(TemplateParser.class),
                    injector.getInstance(TemplateConfiguration.class));

            final Builder contextBuilder = newBuilder();
            contextBuilder.setVastContent("<xml></xml>");
            contextBuilder.setVastXml(true);
            contextBuilder.setCreativeObjectList(movieBoardResponseMaker.getCreativeListForMovieBoardTemplate());
            contextBuilder.setRequestJsonObject(movieBoardResponseMaker.getRequestJsonForMovieBoardTemplate(2));

            final String responseContent = movieBoardResponseMaker.makeIXMovieBoardVideoResponse(contextBuilder.build(), params);
            System.out.println(responseContent);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}