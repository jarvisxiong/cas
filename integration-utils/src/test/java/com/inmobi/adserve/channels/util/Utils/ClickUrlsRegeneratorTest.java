package com.inmobi.adserve.channels.util.Utils;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClickUrlsRegeneratorTest {
    static Configuration mockConfig;

    private static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("key.1.value")).andReturn("SecretKey").times(1);
        expect(mockConfig.getString("beaconURLPrefix")).andReturn("BeaconURLPrefix").times(2);
        expect(mockConfig.getString("clickURLPrefix")).andReturn("ClickURLPrefix").times(1);
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() {
        prepareMockConfig();
        ClickUrlsRegenerator.init(mockConfig);
    }

    @Test
    public void testRegenerateBeaconUrlRmAdTrue() throws Exception {
        final String oldBeaconURL =
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3";
        final String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        final String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        final String newExpectedBeaconURL =
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/0/0/x/0/nw/101/1/1/7ac0ff55";
        final String newActualBeaconURL =
                ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, true);
        final int index = StringUtils.ordinalIndexOf(newActualBeaconURL, "/", 15);
        final String newActualImpressionId =
                newActualBeaconURL.substring(index + 1, StringUtils.indexOf(newActualBeaconURL, "/", index + 1));

        assertThat(newActualBeaconURL, is(equalTo(newExpectedBeaconURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateBeaconUrlRmAdFalse() throws Exception {
        final String oldBeaconURL = TestUtils.SampleStrings.beaconUrl;
        final String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        final String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        final String newExpectedBeaconURL =
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/0/0/x/0/nw/101/1/1/7ac0ff55";
        final String newActualBeaconURL =
                ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, false);
        final int index = StringUtils.ordinalIndexOf(newActualBeaconURL, "/", 15);
        final String newActualImpressionId =
                newActualBeaconURL.substring(index + 1, StringUtils.indexOf(newActualBeaconURL, "/", index + 1));

        assertThat(newActualBeaconURL, is(equalTo(newExpectedBeaconURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateClickUrl() throws Exception {
        final String oldClickURL = TestUtils.SampleStrings.clickUrl;
        final String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        final String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        final String newExpectedClickURL =
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/0/0/x/0/nw/101/1/1/126167a7";
        final String newActualClickURL =
                ClickUrlsRegenerator.regenerateClickUrl(oldClickURL, oldImpressionId, newExpectedImpressionId);
        final int index = StringUtils.ordinalIndexOf(newActualClickURL, "/", 15);
        final String newActualImpressionId =
                newActualClickURL.substring(index + 1, StringUtils.indexOf(newActualClickURL, "/", index + 1));

        assertThat(newActualClickURL, is(equalTo(newExpectedClickURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateClickUrlNull() throws Exception {
        final String oldClickURL = null;
        final String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        final String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";
        final String newActualClickURL =
                ClickUrlsRegenerator.regenerateClickUrl(oldClickURL, oldImpressionId, newExpectedImpressionId);

        assertThat(newActualClickURL, is(equalTo(null)));
    }

    @Test
    public void testRegenerateBeaconUrlNull() throws Exception {
        final String oldBeaconURL = null;
        final String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        final String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";
        final String newActualBeaconURL =
                ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, true);

        assertThat(newActualBeaconURL, is(equalTo(null)));
    }
}
