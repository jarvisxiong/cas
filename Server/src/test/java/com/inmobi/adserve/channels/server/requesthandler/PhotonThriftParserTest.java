package com.inmobi.adserve.channels.server.requesthandler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.ByteArrayOutputStream;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.inmobi.user.photon.datatypes.attribute.Attributes;
import com.inmobi.user.photon.datatypes.commons.Tenant;
import com.inmobi.user.photon.datatypes.profile.UserProfileView;

/**
 * Created by avinash.kumar on 6/9/16.
 */
public class PhotonThriftParserTest {
    public PhotonThriftParser photonThriftParser;
    public ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @BeforeTest
    public void setup() {
        final TIOStreamTransport transport = new TIOStreamTransport(byteArrayOutputStream);
        final TProtocol protocol = new TBinaryProtocol(transport);
        photonThriftParser = new PhotonThriftParser(protocol);
    }

    @Test
    public void thriftByteConversionTest() {

        // test for converting thrift request to Byte Object
        // request is getAllAttribute("0002e17f-4df0-4067-8776-27ff6efac8de", Tenant.BRAND)
        try {
            photonThriftParser.send("0002e17f-4df0-4067-8776-27ff6efac8de", Tenant.BRAND);
            final int[] arr = {-128, 1, 0, 1, 0, 0, 0, 14, 103, 101, 116, 80, 114, 111, 102, 105, 108, 101, 86, 105, 101, 119, 0, 0, 0, 1, 11, 0, 1, 0, 0, 0, 36, 48, 48, 48, 50, 101, 49, 55, 102, 45, 52, 100, 102, 48, 45, 52, 48, 54, 55, 45, 56, 55, 55, 54, 45, 50, 55, 102, 102, 54, 101, 102, 97, 99, 56, 100, 101, 8, 0, 2, 0, 0, 0, 4, 0};
            final byte[] outputBytes = new byte[arr.length];
            for (int i=0; i<arr.length; i++) {
                outputBytes[i] = new Byte(String.valueOf(arr[i]));
            }
            assertEquals(byteArrayOutputStream.toByteArray(), outputBytes);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }

        // Test for converting byte into thrift object
        // Response bytes are for request getAllAttribute("0002e17f-4df0-4067-8776-27ff6efac8de", Tenant.BRAND)
        final int[] responseByte = {-128, 1, 0, 2, 0, 0, 0, 14, 103, 101, 116, 80, 114, 111, 102, 105, 108, 101, 86, 105, 101, 119, 0, 0, 0, 1, 12, 0, 0, 15, 0, 1, 12, 0, 0, 0, 0, 12, 0, 2, 12, 0, 4, 12, 0, 2, 13, 0, 1, 11, 13, 0, 0, 0, 1, 0, 0, 0, 10, 80, 117, 115, 104, 83, 112, 114, 105, 110, 103, 8, 12, 0, 0, 0, 34, 0, 48, 98, 116, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 119, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 51, 86, 35, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 98, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 51, 86, 32, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 99, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 99, 101, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -90, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 102, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 51, 86, 36, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -24, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 105, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 51, 86, 41, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 106, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 51, 86, 45, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -81, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 80, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 99, 18, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -42, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 87, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 89, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 99, 31, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 95, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -33, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -63, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 72, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -119, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 73, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -54, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 74, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 75, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 99, 13, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, -51, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 48, 98, 77, 3, 0, 1, 100, 8, 0, 2, 87, 82, -16, 101, 0, 0, 0, 0, 8, 0, 3, 0, 0, 0, 40, 2, 0, 4, 0, 0, 0};
        final byte[] outputBytes = new byte[responseByte.length];
        for (int i=0; i<responseByte.length; i++) {
            outputBytes[i] = new Byte(String.valueOf(responseByte[i]));
        }
        ChannelBuffer channelBuffer = new BigEndianHeapChannelBuffer(outputBytes);
        ChannelBufferInputStream is = new ChannelBufferInputStream(channelBuffer);
        TProtocol proto = new TBinaryProtocol(new TIOStreamTransport(is));
        try {
            UserProfileView userProfileView = photonThriftParser.receive(proto);
            Attributes attributes = userProfileView.getAttributes();
            attributes.getBrand();
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertFalse(true);
            e.printStackTrace();
        }

    }

}