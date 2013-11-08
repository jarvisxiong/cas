import junit.framework.TestCase;

import com.inmobi.adserve.channels.api.ReportTime;


public class ReportTimeTest extends TestCase
{

    private ReportTime reportTIme1;
    private ReportTime reportTIme2;
    private ReportTime reportTIme3;

    protected void setUp()
    {
        reportTIme1 = new ReportTime(2012, 03, 27, 30);
        reportTIme2 = new ReportTime(2011, 04, 26);
        reportTIme3 = new ReportTime("20100528", 15);

    }

    public void testGetYear()
    {
        assertEquals("Year", 2012, reportTIme1.getYear());
        assertEquals("Year", 2011, reportTIme2.getYear());
        assertEquals("Year", 2010, reportTIme3.getYear());

    }

    public void testGetMonth()
    {
        assertEquals("Month  ", 03, reportTIme1.getMonth());
        assertEquals("Month", 04, reportTIme2.getMonth());
        assertEquals("Month", 05, reportTIme3.getMonth());
    }

    public void testGetDay()
    {
        assertEquals("Day ", 27, reportTIme1.getDay());
        assertEquals("Day", 26, reportTIme2.getDay());
        assertEquals("Day", 28, reportTIme3.getDay());
    }

    public void testGetHour()
    {
        assertEquals("Hour", 30, reportTIme1.getHour());
        assertEquals("Hour", 0, reportTIme2.getHour());
        assertEquals("Hour", 15, reportTIme3.getHour());
    }

    public void testGetStringDate()
    {
        assertEquals("StringDate", "2012-03-27", reportTIme1.getStringDate("-"));
        assertEquals("StringDate", "2011-04-26", reportTIme2.getStringDate("-"));
        assertEquals("StringDate", "2010-05-28", reportTIme3.getStringDate("-"));

    }

    public void testGetNextDay()
    {
        ReportTime reportTimenextDay;
        reportTimenextDay = reportTIme1.getNextDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimenextDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimenextDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimenextDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimenextDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimenextDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimenextDay.getStringDate("-"));

        reportTimenextDay = reportTIme2.getNextDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimenextDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimenextDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimenextDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimenextDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimenextDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimenextDay.getStringDate("-"));

        reportTimenextDay = reportTIme3.getNextDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimenextDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimenextDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimenextDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimenextDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimenextDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimenextDay.getStringDate("-"));

    }

    public void testGetPreviousDay()
    {
        ReportTime reportTimePreviousDay;
        reportTimePreviousDay = reportTIme1.getPreviousDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimePreviousDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimePreviousDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimePreviousDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimePreviousDay.getStringDate("-"));

        reportTimePreviousDay = reportTIme2.getPreviousDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimePreviousDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimePreviousDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimePreviousDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimePreviousDay.getStringDate("-"));

        reportTimePreviousDay = reportTIme3.getPreviousDay(reportTIme1);
        assertEquals("Year", reportTIme1.getYear(), reportTimePreviousDay.getYear());
        assertNotSame("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay());
        assertEquals("Year", reportTIme1.getDay(), reportTimePreviousDay.getDay(), 1);
        assertEquals("Year", reportTIme1.getMonth(), reportTimePreviousDay.getMonth());
        assertEquals("Year", reportTIme1.getHour(), reportTimePreviousDay.getHour());
        assertNotSame("Year", reportTIme1.getStringDate("-"), reportTimePreviousDay.getStringDate("-"));

    }

}
