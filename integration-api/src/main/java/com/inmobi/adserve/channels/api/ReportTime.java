package com.inmobi.adserve.channels.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class ReportTime
{

    private int year;
    private int month;
    private int day;
    private int hour;

    public ReportTime(int year, int month, int day, int hour)
    {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    public ReportTime(ReportTime reportTime)
    {
        this.year = reportTime.year;
        this.month = reportTime.month;
        this.day = reportTime.day;
        this.hour = reportTime.hour;
    }

    public ReportTime(int year, int month, int day)
    {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = 0;
    }

    public static long getEpoch(ReportTime reportTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reportTime.year);
        cal.set(Calendar.MONTH, reportTime.month - 1);
        cal.set(Calendar.DATE, reportTime.day);
        return (cal.getTimeInMillis() / 1000);
    }

    // Sample Input: 2012-03-12,05
    public ReportTime(String reportTime, int hour)
    {
        if (reportTime.contains("-") && (reportTime.length() >= 10)) {
            year = Integer.parseInt(reportTime.substring(0, 4));
            month = Integer.parseInt(reportTime.substring(5, 7));
            day = Integer.parseInt(reportTime.substring(8, 10));
            this.hour = hour;
        }
        else if (!reportTime.contains("-") && (reportTime.length() >= 8)) {
            year = Integer.parseInt(reportTime.substring(0, 4));
            month = Integer.parseInt(reportTime.substring(4, 6));
            day = Integer.parseInt(reportTime.substring(6, 8));
            this.hour = hour;
        }
    }

    public ReportTime(String reportTime, int hour, String separator)
    {
        if (reportTime.contains(separator) && (reportTime.length() >= 10)) {
            month = Integer.parseInt(reportTime.substring(0, 2));
            day = Integer.parseInt(reportTime.substring(3, 5));
            year = Integer.parseInt(reportTime.substring(6, 10));
            this.hour = hour;
        }
    }

    public int getYear()
    {
        return year;
    }

    public int getMonth()
    {
        return month;
    }

    public int getDay()
    {
        return day;
    }

    public int getHour()
    {
        return hour;
    }

    public String getStringDate(String separator)
    {
        String stringDate = String.format("%04d%s%02d%s%02d", year, separator, month, separator, day);
        return stringDate;
    }

    public String getMullahMediaStringDate()
    {
        String stringDate = String.format("%02d/%02d/%04d", month, day, year);
        return stringDate;
    }

    public static ReportTime getNextDay(ReportTime reportTime)
    {
        return getOffsetDay(reportTime, 1);
    }

    public static ReportTime getPreviousDay(ReportTime reportTime)
    {
        return getOffsetDay(reportTime, -1);
    }

    public static ReportTime getPreviousMonth(ReportTime reportTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reportTime.year);
        cal.set(Calendar.MONTH, reportTime.month);
        cal.set(Calendar.DATE, reportTime.day);
        return (new ReportTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) - 1, cal.get(Calendar.DAY_OF_MONTH),
                reportTime.hour));
    }

    public static ReportTime getOffsetDay(ReportTime reportTime, int offset)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reportTime.year);
        cal.set(Calendar.MONTH, reportTime.month - 1);
        cal.set(Calendar.DATE, reportTime.day + offset);
        ReportTime nextTime = new ReportTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), reportTime.hour);
        return nextTime;
    }

    public static ReportTime getPreviousHour(ReportTime reportTime)
    {
        return getOffsetHour(reportTime, -1);
    }

    public static ReportTime getNextHour(ReportTime reportTime)
    {
        return getOffsetHour(reportTime, 1);
    }

    private static ReportTime getOffsetHour(ReportTime reportTime, int offset)
    {
        ReportTime offsetHour;
        if ((offset == 1) && (reportTime.hour == 23)) {
            offsetHour = getNextDay(reportTime);
            offsetHour.hour = 0;
        }
        else if ((offset == -1) && (reportTime.hour == 0)) {
            offsetHour = getPreviousDay(reportTime);
            offsetHour.hour = 23;
        }
        else {
            offsetHour = new ReportTime(reportTime.year, reportTime.month, reportTime.day, reportTime.hour + offset);
        }
        return offsetHour;
    }

    // Returns 1 if arg1 is greater, 0 if both are equal and -1 if arg1 is smaller
    public static int compareDates(ReportTime arg1, ReportTime arg2)
    {
        String arg1String = String.format("%04d%02d%02d%02d", arg1.year, arg1.month, arg1.day, arg1.hour);
        String arg2String = String.format("%04d%02d%02d%02d", arg2.year, arg2.month, arg2.day, arg2.hour);
        if (arg1String.compareTo(arg2String) > 0) {
            return 1;
        }
        else if (arg1String.compareTo(arg2String) < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public static int compareStringDates(String arg1String, String arg2String)
    {
        if (arg1String.compareTo(arg2String) > 0) {
            return 1;
        }
        else if (arg1String.compareTo(arg2String) < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    // Returns 1 if calling object is greater, 0 if both are equal and -1 if
    // calling object is smaller
    public int compareDates(ReportTime arg1)
    {
        return compareDates(this, arg1);
    }

    // increment the watermark based on granularity of the adapter
    public static ReportTime formatWatermark(String watermarkTime, int hour)
    {
        String[] watermark = watermarkTime.split("_");
        int hr = 0;
        String[] timeArray = null;
        // checking if the watermark read from db has the proper hour value

        // TODO remove the check for 45 hours in next release.. its a hack just for now
        if (watermark != null && watermark.length == 2 && watermark[1].matches("^\\d+$")
                && (hr = Integer.parseInt(watermark[1])) >= 0 && (hr <= 24 || hr == 45)) {
            timeArray = watermark[0].split("-");
        }

        // checking if the date part is in yyyy-mm-dd format
        if (timeArray != null && timeArray.length == 3 && timeArray[0].length() == 4 && timeArray[1].length() == 2
                && timeArray[2].length() == 2) {
            ReportTime reportTime = new ReportTime(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]),
                    Integer.parseInt(timeArray[2]), Integer.parseInt(watermark[1]));
            ReportTime finalTime = null;
            if (hour == 1) {
                finalTime = getNextHour(reportTime);
            }
            else if (hour == 24) {
                finalTime = getNextDay(reportTime);
            }
            if (hour == -1) {
                finalTime = getPreviousHour(reportTime);
            }
            else if (hour == -24) {
                finalTime = getPreviousDay(reportTime);
            }
            return finalTime;
        }
        return null;
    }

    public static String getWatermark(ReportTime reportTime)
    {
        return String.format("%04d-%02d-%02d_%02d", reportTime.year, reportTime.month, reportTime.day, reportTime.hour);
    }

    public static ReportTime getStartTime(double timeZoneGap)
    {
        ReportTime reportTime = null;
        if (timeZoneGap == -7) {
            reportTime = ReportTime.getPacificTime();
        }
        else {
            reportTime = ReportTime.getUTCTime();
        }
        reportTime = ReportTime.getPreviousDay(reportTime);
        reportTime = ReportTime.getPreviousDay(reportTime);
        reportTime = ReportTime.getPreviousDay(reportTime);
        return reportTime;
    }

    public static String getMobileCommerceCurrentTime()
    {
        Calendar cal = Calendar.getInstance();
        ReportTime currentTime = new ReportTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY));
        return (getMobileCommerceDate(currentTime));
    }

    public static String getMobileCommerceDate(ReportTime reportTime)
    {
        return String.format("%04d%02d%02d", reportTime.year, reportTime.month, reportTime.day);
    }

    public static ReportTime getUTCTime()
    {
        java.util.Date date = new java.util.Date();
        DateFormat utcFormatDate = new SimpleDateFormat("yyyyMMdd");
        DateFormat utcFormatTime = new SimpleDateFormat("H");
        TimeZone utcTime = TimeZone.getTimeZone("GMT");
        utcFormatDate.setTimeZone(utcTime);
        utcFormatTime.setTimeZone(utcTime);
        return (new ReportTime(utcFormatDate.format(date), Integer.parseInt(utcFormatTime.format(date))));
    }

    public static String getUTCTimestamp()
    {
        java.util.Date date = new java.util.Date();
        DateFormat utcFormatDate = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
        TimeZone utcTime = TimeZone.getTimeZone("GMT");
        utcFormatDate.setTimeZone(utcTime);
        return (utcFormatDate.format(date));
    }

    public static String getTTime()
    {
        java.util.Date date = new java.util.Date();
        DateFormat utcFormatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        TimeZone utcTime = TimeZone.getTimeZone("GMT");
        utcFormatDate.setTimeZone(utcTime);
        return (utcFormatDate.format(date));
    }

    public static ReportTime getPacificTime()
    {
        java.util.Date date = new java.util.Date();
        DateFormat utcFormatDate = new SimpleDateFormat("yyyyMMdd");
        DateFormat utcFormatTime = new SimpleDateFormat("H");
        TimeZone utcTime = TimeZone.getTimeZone("PST");
        utcFormatDate.setTimeZone(utcTime);
        utcFormatTime.setTimeZone(utcTime);
        return (new ReportTime(utcFormatDate.format(date), Integer.parseInt(utcFormatTime.format(date))));
    }
}