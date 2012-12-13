package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

public class ChannelEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String username;
  private String password;
  private String accountId;
  private String reportingApiKey;
  private String reportingApiUrl;
  private boolean isActive;
  private boolean isTestMode;
  private long burstQps;
  private long impressionCeil;
  private long impressionFloor;
  private int priority;
  private int demandSourceTypeId;
  private Timestamp modified_on;
  private String urlBase;
  private String urlArg;
  private String rtbVer;
  private boolean isRtb;
  private String rtbMethod;
  private String wnUrl; //used if the win notification is sent via client
  private boolean wnRequied; // if url is already a part of bid response then wnRequired=false otherwise true
  private boolean wnFromClient; // if wnUrl is placed in the final ad response to publisher, it is server side wn
  // and if explicit call is made through cas client(adaptor connection) it is through client side.
  private String status;
  
  public ChannelEntity() {
  }
  
  public boolean isWnRequied() {
    return wnRequied;
  }

  public void setWnRequied(boolean wnRequied) {
    this.wnRequied = wnRequied;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isWnFromClient() {
    return wnFromClient;
  }

  public void setWnFromClient(boolean wnFromClient) {
    this.wnFromClient = wnFromClient;
  }  

  @Override
  public boolean equals(final Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(!(obj instanceof ChannelEntity))
      return false;
    ChannelEntity other = (ChannelEntity) obj;
    if(id != other.id)
      return false;
    return true;

  }

  @Override
  public String getJSON() {
    return null;
  }

  @Override
  public int hashCode() {
    return (id.hashCode());
  }

  @Override
  public String getId() {
    return id;
  }

  public String getName() {
    return this.name;
  }

  public String getAccountId() {
    return this.accountId;
  }

  public String getUsername() {
    return this.username;
  }

  public String getReportingApiKey() {
    return this.reportingApiKey;
  }

  public String getReportingApiUrl() {
    return this.reportingApiUrl;
  }

  public String getPassword() {
    return this.password;
  }

  public int getPriority() {
    return this.priority;
  }

  public int getDemandSourceTypeId() {
    return this.demandSourceTypeId;
  }

  public boolean getIsActive() {
    return this.isActive;
  }

  public long getImpressionCeil() {
    return this.impressionCeil;
  }
  
  public long getImpressionFloor() {
    return this.impressionFloor;
  }

  public long getBurstQps() {
    return this.burstQps;
  }

  public Timestamp getModifiedOn() {
    return this.modified_on;
  }

  public boolean getIsTestMode() {
    return this.isTestMode;
  }

  public ChannelEntity setId(String id) {
    this.id = id;
    return this;
  }

  public ChannelEntity setName(String name) {
    this.name = name;
    return this;
  }

  public ChannelEntity setAccountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  public ChannelEntity setUsername(String username) {
    this.username = username;
    return this;
  }

  public ChannelEntity setReportingApiKey(String reportingApiKey) {
    this.reportingApiKey = reportingApiKey;
    return this;
  }

  public ChannelEntity setReportingApiUrl(String reportingApiUrl) {
    this.reportingApiUrl = reportingApiUrl;
    return this;
  }

  public ChannelEntity setPassword(String password) {
    this.password = password;
    return this;
  }

  public ChannelEntity setPriority(int priority) {
    this.priority = priority;
    return this;
  }

  public ChannelEntity setDemandSourceTypeId(int demandSourceTypeId) {
    this.demandSourceTypeId = demandSourceTypeId;
    return this;
  }

  public ChannelEntity setIsActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  public ChannelEntity setImpressionCeil(long impressionCeil) {
    this.impressionCeil = impressionCeil;
    return this;
  }
  
  public ChannelEntity setImpressionFloor(long impressionFloor) {
    this.impressionFloor = impressionFloor;
    return this;
  }

  public ChannelEntity setBurstQps(long burstQps) {
    this.burstQps = burstQps;
    return this;
  }

  public ChannelEntity setModifiedOn(Timestamp modified_on) {
    this.modified_on = modified_on;
    return this;
  }

  public ChannelEntity setIsTestMode(boolean isTestMode) {
    this.isTestMode = isTestMode;
    return this;
  }

  public String getUrlBase() {

    return urlBase;
  }

  public void setUrlBase(String urlBase) {

    this.urlBase = urlBase;
  }

  public String getUrlArg() {

    return urlArg;
  }

  public void setUrlArg(String urlArg) {

    this.urlArg = urlArg;
  }

  public String getRtbVer() {

    return rtbVer;
  }

  public void setRtbVer(String rtbVer) {

    this.rtbVer = rtbVer;
  }

  public boolean isRtb() {

    return isRtb;
  }

  public void setRtb(boolean isRtb) {

    this.isRtb = isRtb;
  }

  public String getRtbMethod() {

    return rtbMethod;
  }

  public void setRtbMethod(String rtbMethod) {

    this.rtbMethod = rtbMethod;
  }

  public String getWnUrl() {

    return wnUrl;
  }

  public void setWnUrl(String wnString) {

    this.wnUrl = wnString;
  }

}
