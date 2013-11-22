package com.inmobi.adserve.channels.adnetworks.mobfox;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "report"
})
@XmlRootElement(name = "response")
public class ReportingApiResponse {

 @XmlElement(required = true)
 protected ReportingApiResponse.Report report;
 @XmlAttribute(name = "status")
 protected String status;

 /**
  * Gets the value of the report property.
  * 
  * @return
  *     possible object is
  *     {@link ReportingApiResponse.Report }
  *     
  */
 public ReportingApiResponse.Report getReport() {
     return report;
 }

 /**
  * Sets the value of the report property.
  * 
  * @param value
  *     allowed object is
  *     {@link ReportingApiResponse.Report }
  *     
  */
 public void setReport(ReportingApiResponse.Report value) {
     this.report = value;
 }

 /**
  * Gets the value of the status property.
  * 
  * @return
  *     possible object is
  *     {@link String }
  *     
  */
 public String getStatus() {
     return status;
 }

 /**
  * Sets the value of the status property.
  * 
  * @param value
  *     allowed object is
  *     {@link String }
  *     
  */
 public void setStatus(String value) {
     this.status = value;
 }


  @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder = {
     "startDate",
     "endDate",
     "type",
     "statistics"
 })
 public static class Report {

     @XmlElement(name = "start_date", required = true)
     protected String startDate;
     @XmlElement(name = "end_date", required = true)
     protected String endDate;
     @XmlElement(required = true)
     protected String type;
     @XmlElement(required = true)
     protected ReportingApiResponse.Report.Statistics statistics;

     /**
      * Gets the value of the startDate property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getStartDate() {
         return startDate;
     }

     /**
      * Sets the value of the startDate property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setStartDate(String value) {
         this.startDate = value;
     }

     /**
      * Gets the value of the endDate property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getEndDate() {
         return endDate;
     }

     /**
      * Sets the value of the endDate property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setEndDate(String value) {
         this.endDate = value;
     }

     /**
      * Gets the value of the type property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getType() {
         return type;
     }

     /**
      * Sets the value of the type property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setType(String value) {
         this.type = value;
     }

     /**
      * Gets the value of the statistics property.
      * 
      * @return
      *     possible object is
      *     {@link ReportingApiResponse.Report.Statistics }
      *     
      */
     public ReportingApiResponse.Report.Statistics getStatistics() {
         return statistics;
     }

     /**
      * Sets the value of the statistics property.
      * 
      * @param value
      *     allowed object is
      *     {@link ReportingApiResponse.Report.Statistics }
      *     
      */
     public void setStatistics(ReportingApiResponse.Report.Statistics value) {
         this.statistics = value;
     }


     
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class Statistics {

         protected long requests;
         protected long impressions;
         protected long clicks;
         @XmlElement(name = "total_earnings", required = true)
         protected ReportingApiResponse.Report.Statistics.TotalEarnings totalEarnings;
         protected float clickThroughRate;

         /**
          * Gets the value of the requests property.
          * 
          */
         public long getRequests() {
             return requests;
         }

         /**
          * Sets the value of the requests property.
          * 
          */
         public void setRequests(long value) {
             this.requests = value;
         }

         /**
          * Gets the value of the impressions property.
          * 
          */
         public long getImpressions() {
             return impressions;
         }

         /**
          * Sets the value of the impressions property.
          * 
          */
         public void setImpressions(long value) {
             this.impressions = value;
         }

         /**
          * Gets the value of the clicks property.
          * 
          */
         public long getClicks() {
             return clicks;
         }

         /**
          * Sets the value of the clicks property.
          * 
          */
         public void setClicks(long value) {
             this.clicks = value;
         }

         /**
          * Gets the value of the totalEarnings property.
          * 
          * @return
          *     possible object is
          *     {@link ReportingApiResponse.Report.Statistics.TotalEarnings }
          *     
          */
         public ReportingApiResponse.Report.Statistics.TotalEarnings getTotalEarnings() {
             return totalEarnings;
         }

         /**
          * Sets the value of the totalEarnings property.
          * 
          * @param value
          *     allowed object is
          *     {@link ReportingApiResponse.Report.Statistics.TotalEarnings }
          *     
          */
         public void setTotalEarnings(ReportingApiResponse.Report.Statistics.TotalEarnings value) {
             this.totalEarnings = value;
         }

         
         /**
          * Gets the value of the clickThroughRate property.
          * 
          */
         public float getClickThroughRate() {
             return clickThroughRate;
         }

         /**
          * Sets the value of the clickThroughRate property.
          * 
          */
         public void setClickThroughRate(float value) {
             this.clickThroughRate = value;
         }

         

         @XmlAccessorType(XmlAccessType.FIELD)
         @XmlType(name = "", propOrder = {
             "currency",
             "amount"
         })
         public static class TotalEarnings {

             @XmlElement(required = true)
             protected String currency;
             protected double amount;

             /**
              * Gets the value of the currency property.
              * 
              * @return
              *     possible object is
              *     {@link String }
              *     
              */
             public String getCurrency() {
                 return currency;
             }

             /**
              * Sets the value of the currency property.
              * 
              * @param value
              *     allowed object is
              *     {@link String }
              *     
              */
             public void setCurrency(String value) {
                 this.currency = value;
             }

             /**
              * Gets the value of the amount property.
              * 
              */
             public double getAmount() {
                 return amount;
             }

             /**
              * Sets the value of the amount property.
              * 
              */
             public void setAmount(double value) {
                 this.amount = value;
             }

         }

     }

 }

}
