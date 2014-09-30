package com.inmobi.adserve.channels.util.Utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ClickUrlsRegeneratorTest{
    Configuration mockConfig;

    public void prepareMockConfig() {
        mockConfig = EasyMock.createMock(Configuration.class);
        EasyMock.expect(mockConfig.getString("key.1.value")).andReturn("SecretKey").times(1);
        EasyMock.expect(mockConfig.getString("beaconURLPrefix")).andReturn("BeaconURLPrefix").times(2);
        EasyMock.expect(mockConfig.getString("clickURLPrefix")).andReturn("ClickURLPrefix").times(1);
        EasyMock.replay(mockConfig);
    }

    @Before
    public void setUp() {
        prepareMockConfig();
        ClickUrlsRegenerator.init(mockConfig);
    }

    @Test
    public void testRegenerateBeaconUrlRmAdTrue() throws Exception {
        String oldBeaconURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3";
        String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        String newExpectedBeaconURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/0/0/x/0/nw/101/1/1/7ac0ff55";
        String newActualBeaconURL = ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, true);
        int index = StringUtils.ordinalIndexOf(newActualBeaconURL, "/", 15);
        String newActualImpressionId = newActualBeaconURL.substring(index+1, StringUtils.indexOf(newActualBeaconURL, "/", index+1));

        assertThat(newActualBeaconURL, is(equalTo(newExpectedBeaconURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateBeaconUrlRmAdFalse() throws Exception {
        String oldBeaconURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3";
        String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        String newExpectedBeaconURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/0/0/x/0/nw/101/1/1/7ac0ff55";
        String newActualBeaconURL = ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, false);
        int index = StringUtils.ordinalIndexOf(newActualBeaconURL, "/", 15);
        String newActualImpressionId = newActualBeaconURL.substring(index+1, StringUtils.indexOf(newActualBeaconURL, "/", index+1));

        assertThat(newActualBeaconURL, is(equalTo(newExpectedBeaconURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateClickUrl() throws Exception {
        String oldClickURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/1/0/x/0/nw/101/1/1/5f2b3532";
        String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        String newExpectedClickURL = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124ba55-0148-1000-f71b-00022d0b0000/0/5l/-1/1/0/x/0/nw/101/1/1/ce077524";
        String newActualClickURL = ClickUrlsRegenerator.regenerateClickUrl(oldClickURL, oldImpressionId, newExpectedImpressionId);
        int index = StringUtils.ordinalIndexOf(newActualClickURL, "/", 15);
        String newActualImpressionId = newActualClickURL.substring(index+1, StringUtils.indexOf(newActualClickURL, "/", index+1));

        assertThat(newActualClickURL, is(equalTo(newExpectedClickURL)));
        assertThat(newActualImpressionId, is(equalTo(newExpectedImpressionId)));
    }

    @Test
    public void testRegenerateClickUrlNull() throws Exception {
        String oldClickURL = null;
        String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        String newExpectedClickURL = null;
        String newActualClickURL = ClickUrlsRegenerator.regenerateClickUrl(oldClickURL, oldImpressionId, newExpectedImpressionId);

        assertThat(newActualClickURL, is(equalTo(null)));
    }

    @Test
    public void testRegenerateBeaconUrlNull() throws Exception {
        String oldBeaconURL = null;
        String oldImpressionId = "c124b6b5-0148-1000-c54a-00012e330000";
        String newExpectedImpressionId = "c124ba55-0148-1000-f71b-00022d0b0000";

        String newExpectedBeaconURL = null;
        String newActualBeaconURL = ClickUrlsRegenerator.regenerateBeaconUrl(oldBeaconURL, oldImpressionId, newExpectedImpressionId, true);

        assertThat(newActualBeaconURL, is(equalTo(null)));
    }
}
