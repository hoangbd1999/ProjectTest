/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elcom.vn.object;

/**
 *
 * @author Admin
 */
public class XmlMediaInfo {

    private String receiveTime;
    
    private String service;
    
    private int sourceID;
    
    private String iPsrc;
    
    private String iPdst;
    
    private int portSrc;
    
    private int portDest;
    
    private String phoneFrom;
    
    private String phoneTo;
    
    private String filePath;
    
    private long fileLen;

    public XmlMediaInfo() {
    }
    
    /**
     * @return the receiveTime
     */
    public String getReceiveTime() {
        return receiveTime;
    }

    /**
     * @param receiveTime the receiveTime to set
     */
    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * @return the iPsrc
     */
    public String getiPsrc() {
        return iPsrc;
    }

    /**
     * @param iPsrc the iPsrc to set
     */
    public void setiPsrc(String iPsrc) {
        this.iPsrc = iPsrc;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return the iPdst
     */
    public String getiPdst() {
        return iPdst;
    }

    /**
     * @param iPdst the iPdest to set
     */
    public void setiPdst(String iPdst) {
        this.iPdst = iPdst;
    }

    /**
     * @return the portSrc
     */
    public int getPortSrc() {
        return portSrc;
    }

    /**
     * @param portSrc the portSrc to set
     */
    public void setPortSrc(int portSrc) {
        this.portSrc = portSrc;
    }

    /**
     * @return the portDest
     */
    public int getPortDest() {
        return portDest;
    }

    /**
     * @param portDest the portDest to set
     */
    public void setPortDest(int portDest) {
        this.portDest = portDest;
    }

    /**
     * @return the phoneFrom
     */
    public String getPhoneFrom() {
        return phoneFrom;
    }

    /**
     * @param phoneFrom the phoneFrom to set
     */
    public void setPhoneFrom(String phoneFrom) {
        this.phoneFrom = phoneFrom;
    }

    /**
     * @return the phoneTo
     */
    public String getPhoneTo() {
        return phoneTo;
    }

    /**
     * @param phoneTo the phoneTo to set
     */
    public void setPhoneTo(String phoneTo) {
        this.phoneTo = phoneTo;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the fileLen
     */
    public long getFileLen() {
        return fileLen;
    }

    /**
     * @param fileLen the fileLen to set
     */
    public void setFileLen(long fileLen) {
        this.fileLen = fileLen;
    }
}
