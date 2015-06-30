package com.inmobi.castest.utils.bidders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.thrift.TBase;

import com.inmobi.adserve.adpool.RequestedAdType;

/**
 * Created by aartika.rai on 07/11/14.
 */
public abstract class AdRequestHandler<T extends TBase, F extends TBase> {

    protected String seatId = "rtbAdvertiserId";
    protected RequestParser<T> requestParser;

    protected AdRequestHandler(final RequestParser<T> requestParser, final String seatId) {
        this.seatId = seatId;
        this.requestParser = requestParser;
    }

    public abstract F makeBidResponse(final T bidRequest);

    public T parseRequest(final String request) {
        return requestParser.getParsedRequest(request);
    }

    public abstract double getBidPrice(final T bidRequest);

    protected RequestedAdType getRequestedCreativeType(final T bidRequest) {
        if (isNativeRequest(bidRequest)) {
            return RequestedAdType.NATIVE;
        }
        if (isVideoRequest(bidRequest)) {
            return RequestedAdType.INTERSTITIAL;
        }
        return RequestedAdType.BANNER;
    }

    protected abstract boolean isNativeRequest(final T bidRequest);

    protected abstract boolean isVideoRequest(final T bidRequest);

    protected String getAdMarkup(final RequestedAdType requestedAdFormat) {
        final StringBuilder admBuilder = new StringBuilder();
        switch (requestedAdFormat) {
            case NATIVE:
                admBuilder.append("{\"version\": \"1.0\"," + "\"iconurl\": \"http://icon.png\","
                        + "\"title\": \"Hello World\", " + "\"description\": \"This is a beautiful experience\", "
                        + "\"actiontext\": \"Buy Now\", " + "\"actionlink\": \"http://buynow.action\","
                        + " \"pixelurl\": [ \"http://rendered.action1\", \"http://rendered.action2\" ],"
                        + " \"clickurl\": [ \"http://click.action1\", \"http://click.action2\" ], " + "\"callout\": 0,"
                        + " \"data\": [ { \"seq\": 1, \"value\": \"3.9\", \"label\": 0 }],"
                        + "\"image\":{\"imageurl\": \"http://im-age.png\",w:350,h:980}}");
                break;

            case INTERSTITIAL:
                // List<String> possibleVASTResponse = new ArrayList<String>();
                String line = new String();
                final StringBuilder vastResponse = new StringBuilder();

                final File vastXml = new File("src/test/resources/VAST.xml");

                if (vastXml.exists()) {

                    try {

                        final FileReader vastXmlReader = new FileReader(vastXml);
                        final BufferedReader bufferedReader = new BufferedReader(vastXmlReader);

                        while ((line = bufferedReader.readLine()) != null) {
                            vastResponse.append(line);
                        }

                    } catch (final FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    vastResponse
                            .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                    + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.0\" xsi:noNamespaceSchemaLocation=\"vast.xsd\">\n"
                                    + "<Ad id=\"3776076\">\n"
                                    + "<Wrapper>\n"
                                    + "<AdSystem version=\"1\">RubiconProject</AdSystem>\n"
                                    + "<VASTAdTagURI><![CDATA[http://bid.g.doubleclick.net/xbbe/creative/vast?d=APEucNU4yG7GSN14gn91cAVYmfo28JewLB15ihznOSLTMl0EammSpC3nJV5HGGVeeSaNQlDuy0cgvQjFK0A2K-fqROL7FyosLf2Ljw9LLLogN3wY5-qjHC4AmOSPHD3eQZVfH7z8VoM6NA9Tup5aFyIRDWeaz_bdB20ohNBBk4v3tTgI-Ovb7uoVi29RLojNMpy1LftX-rn4v627lsAF9M03r6c8fiOUMIcV6ryaDs-HQizBASibvNFn99T7rgVkMnfEwnsON4bMtagyG9zoq951KDR8xV-3tvve3hUf0QGm3qWuxNuf_dx7A78DTnAnvPEv20d-RTrqheS_Xn3V3Bcmh3z111aWDTE9AVzuxfZI7ZpHJTmsFQKOZTe6ffubld9zEyJ1HnrNGsmjjjHluaflQPt-I9jpvQ&pr=E17B8AC89DA0B6F8]]></VASTAdTagURI>\n"
                                    + "<Impression><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=CBADCB97774921C6F220776BCB42EE293838D7F57DB3271999691AAF848E146DCC30A92680297AF5F2E8271AD017B135BAD467818582DC674923C7A718D011330ADD1523441FF5B62EC17F96206B4604]]></Impression>\n"
                                    + "<Error><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=6120898058972C4616D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></Error>\n"
                                    + "<Creatives>\n"
                                    + "<Creative>\n"
                                    + "<Linear>\n"
                                    + "<TrackingEvents>\n"
                                    + "<Tracking event=\"creativeView\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=498E45A22EEBCA466723304C7E2DD80C16D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></Tracking>\n"
                                    + "<Tracking event=\"start\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=323A6F566D9A526F16D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></Tracking>\n"
                                    + "<Tracking event=\"midpoint\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=2D6EF7C7B3D881A9C905F422BDB46789DB5EDECB6D85616B8D0EECC357AE33988BDA63691B1DAFB6BCC86FFD5E18A14EB1B6C31FCF3BFC6C4C52A8D9F2606FDF836829DF7E445BC9]]></Tracking>\n"
                                    + "<Tracking event=\"firstQuartile\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=C1A0864160A107F4B226FF0CA313975790DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"thirdQuartile\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=3ADD8F99CD221680B226FF0CA313975790DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"complete\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=EF05D8E5B84FFBB0C905F422BDB46789DB5EDECB6D85616B8D0EECC357AE33988BDA63691B1DAFB6BCC86FFD5E18A14EB1B6C31FCF3BFC6C4C52A8D9F2606FDF836829DF7E445BC9]]></Tracking>\n"
                                    + "<Tracking event=\"mute\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=C1EF9A7ACD2CE027D8DB1B2D1C7C379EC01A50C0C1441CB2DDF665371CABFC7FF1ED3635DD56233D8E6B28F8AEBFA3B233CB06D7490F0C8AAB1509833AE14BD2DB2B871BCDBEC714]]></Tracking>\n"
                                    + "<Tracking event=\"unmute\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=1B24E58C8E04147390DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"pause\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=C126B25ED76556CE16D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></Tracking>\n"
                                    + "<Tracking event=\"rewind\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=3C51A362CE1A282290DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"resume\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=E166B0840251226290DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"fullscreen\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=FB78FA73384AC4F637A5C9E568F2755E3838D7F57DB3271999691AAF848E146DCC30A92680297AF5F2E8271AD017B135BAD467818582DC674923C7A718D011330ADD1523441FF5B62EC17F96206B4604]]></Tracking>\n"
                                    + "<Tracking event=\"expand\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=2F2D60639426C85090DC5C64E10348B00F96E09B5240078C7CF96E88111649D3D4BD035EE7B266878AAF6EE142063FC23A5A21B909EA5F3D8CA474E2AEA09E07B1CDD50B590BA14B]]></Tracking>\n"
                                    + "<Tracking event=\"collapse\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=C140A0DE32AE5E44C905F422BDB46789DB5EDECB6D85616B8D0EECC357AE33988BDA63691B1DAFB6BCC86FFD5E18A14EB1B6C31FCF3BFC6C4C52A8D9F2606FDF836829DF7E445BC9]]></Tracking>\n"
                                    + "<Tracking event=\"acceptInvitation\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=CB7F724D5631BC92B5E2858846470B46A863FB3B68CAC6830A1E6D5A88A76A04E74A783DE7B935A64E6C3261B8685C667183DA10401C2D23F295CE4C8D805D9DECF049C1BBEDD93A72533CCF23894F7C843D61C4C293BAD1]]></Tracking>\n"
                                    + "<Tracking event=\"close\"><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=11F74E138A4437A116D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></Tracking>\n"
                                    + "</TrackingEvents>\n"
                                    + "<VideoClicks>\n"
                                    + "<ClickTracking><![CDATA[http://beacon-us-east.rubiconproject.com/beacon/v?accountId=11726&siteId=38310&zoneId=161002&e=BE7AC75245C0764516D016827A05A08D1296378A0FBCBDF52655074B7A6E957235B80BF483C0A13FBFEFF7660F981A2C5FD5C1B593DA8E85E7BEC36BD9B59C706A4BBB3100ECE517]]></ClickTracking>\n"
                                    + "</VideoClicks>\n" + "</Linear>\n" + "</Creative>\n" + "</Creatives>\n"
                                    + "</Wrapper>\n" + "</Ad>\n" + "</VAST>");
                }
                admBuilder.append(vastResponse);
                break;

            case BANNER:
                admBuilder.append("<style type='text/css'>");
                admBuilder.append("body { margin:0;padding:0 }  </style> <p align='center'>");
                admBuilder
                        .append("<a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'>");
                admBuilder
                        .append("<img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/>");
                admBuilder.append("</a></p>"); // TODO fix the escape character
                // problem
        }

        return admBuilder.toString();
    }
}
