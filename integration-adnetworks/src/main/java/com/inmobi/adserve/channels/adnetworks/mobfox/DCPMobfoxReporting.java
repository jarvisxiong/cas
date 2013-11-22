package com.inmobi.adserve.channels.adnetworks.mobfox;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.configuration.Configuration;
import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;

public class DCPMobfoxReporting extends BaseReportingImpl {
	private final String accountId;
	private final String apiKey;
	private String startDate;
	private String endDate;
	private DebugLogger logger;
	private final String host;
	private final String advertiserId;
	private final String listUrl;
	private static JAXBContext jaxbContext;
	private static Unmarshaller jaxbUnmarshaller;
	private static JAXBContext jaxbContextReports;
	private static Unmarshaller jaxbUnmarshallerReports;
	private String publisherId=null;

	public DCPMobfoxReporting(final Configuration config) {
		this.accountId = config.getString("mobfox.accountId");
		this.apiKey = config.getString("mobfox.key");
		this.host = config.getString("mobfox.host");
		this.advertiserId = config.getString("mobfox.advertiserId");
		listUrl = config.getString("mobfox.listApiUrl");
		try {
			jaxbContext = JAXBContext.newInstance(ListResponse.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbContextReports = JAXBContext.newInstance(ReportingApiResponse.class);
			jaxbUnmarshallerReports = jaxbContextReports.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime,
			String key, ReportTime endTime) throws Exception {
		this.logger = logger;
		ReportResponse reportResponse = new ReportResponse(
				ReportResponse.ResponseStatus.SUCCESS);
		logger.debug("inside fetch rows of mobfox");
		try {
			this.startDate = startTime.getStringDate("-");
			logger.debug("start date inside mobfox is " + this.startDate);
			endDate = endTime == null ? getEndDate("-") : startDate;
			if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
				logger.debug("date is greater than the current date reporting window for mobfox");
				return null;
			}
		} catch (Exception exception) {
			reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
			logger.info("failed to obtain correct dates for fetching reports "
					+ exception.getMessage());
			return null;
		}
		String listApiresponse = invokeHTTPUrl(
				String.format(String.format(listUrl, accountId, apiKey)), logger);

		ListResponse response = (ListResponse) jaxbUnmarshaller
				.unmarshal(new ByteArrayInputStream(listApiresponse.getBytes()));
		
		publisherId=null;
		for (Publication publication:response.getListOfPublications()){
		String extSiteKey = publication.getId();
		if(key.equalsIgnoreCase(extSiteKey))
		{
			publisherId = publication.getPublisher_id();
		}
		}
		
		if(null !=publisherId){
			String reportingResponse = invokeHTTPUrl(
					getRequestUrl(), logger);
			ReportingApiResponse reportingApiResponse = (ReportingApiResponse) jaxbUnmarshallerReports
					.unmarshal(new ByteArrayInputStream(reportingResponse.getBytes()));
			
			ReportResponse.ReportRow row = new ReportResponse.ReportRow();
			row.request = reportingApiResponse.getReport().getStatistics().getRequests();
			row.clicks = reportingApiResponse.getReport().getStatistics().getClicks();
			row.impressions = reportingApiResponse.getReport().getStatistics().getImpressions();
			row.revenue = reportingApiResponse.getReport().getStatistics().getTotalEarnings().getAmount();
			
			ReportTime reportDate = new ReportTime(startDate,
					0);
			row.reportTime = reportDate;
			row.siteId = key;
			row.slotSize = getReportGranularity();
			logger.debug("parsing data inside MobFox"
					+ row.request);
			reportResponse.addReportRow(row);

		}
		
		
		return reportResponse;
	}

	@Override
	public String getRequestUrl() {
		String reportUrl = String.format(String.format(host, accountId, apiKey,startDate,startDate,publisherId));
		return reportUrl;
	}

	@Override
	public double getTimeZone() {
		return 1;
	}

	@Override
	public ReportGranularity getReportGranularity() {
		return ReportGranularity.DAY;
	}

	@Override
	public int ReportReconcilerWindow() {
		return 20;
	}

	@Override
	public String getName() {
		return "mobfox";
	}

	@Override
	public String getAdvertiserId() {
		return (advertiserId);
	}

	public String getEndDate(final String seperator) {
		try {
			logger.debug("calculating end date for mobfox");
			ReportTime reportTime = ReportTime.getUTCTime();
			reportTime = ReportTime.getPreviousDay(reportTime);
			if (reportTime.getHour() < ReportReconcilerWindow()) {
				reportTime = ReportTime.getPreviousDay(reportTime);
			}
			return (reportTime.getStringDate(seperator));
		} catch (Exception exception) {
			logger.info("failed to obtain end date inside mobfox "
					+ exception.getMessage());
			return "";
		}
	}

	@XmlRootElement(name="response")
	public static class ListResponse {

		private String status;

		public String getStatus() {
			return status;
		}

		@XmlAttribute
		public void setStatus(String status) {
			this.status = status;
		}

		public ArrayList<Publication> getListOfPublications() {
			return listOfPublications;
		}

		@XmlElement(name = "publication")
		public void setListOfPublications(
				ArrayList<Publication> listOfPublications) {
			this.listOfPublications = listOfPublications;
		}

		private ArrayList<Publication> listOfPublications;

		public ListResponse() {

		}

	}

	@XmlRootElement(namespace = "com.inmobi.adserve.channels.adnetworks.mobfox.DCPMobfoxReporting.Response")
	public static class Publication {

		private String id;
		private String publisher_id;

		public Publication() {

		}

		public String getId() {
			return id;
		}

		@XmlAttribute
		public void setId(String id) {
			this.id = id;
		}

		public String getPublisher_id() {
			return publisher_id;
		}

		@XmlElement
		public void setPublisher_id(String publisher_id) {
			this.publisher_id = publisher_id;
		}

	}

}
