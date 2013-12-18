package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.adnetworks.ifc.IFCReporting;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportResponse.ReportRow;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.SlotSizeMapping;


public class IFCReportingTest extends TestCase {

    private Configuration mockConfig = null;

    private IFCReporting  ifcReporting;
    private final String  loggerConf = "/tmp/channel-server.properties";
    private final String  debug      = "debug";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("ifc.baseUrl"))
                .andReturn("http://localhost:8080/IFCGateway/admin/v1/billings")
                    .anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        Calendar.getInstance();
        replay(mockConfig);

    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File("/tmp/channel-server.properties");
        if (!f.exists()) {
            f.createNewFile();
        }
        prepareMockConfig();
        SlotSizeMapping.init();
        ifcReporting = new IFCReporting(mockConfig);
    }

    public void testFetchRows() {
        ReportTime reportTime = new ReportTime("2011-08-01", 05);
        ReportResponse reportResponse = ifcReporting.fetchRows(reportTime, null);
        List<ReportRow> reportRows = reportResponse.getReportRows();
        for (Iterator<ReportRow> iterator = reportRows.iterator(); iterator.hasNext();) {
            ReportRow reportRow = iterator.next();
            assertTrue(0 < reportRow.revenue);
        }
    }
}
