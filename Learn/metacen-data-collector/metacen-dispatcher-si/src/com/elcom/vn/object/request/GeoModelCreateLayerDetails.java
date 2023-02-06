package com.elcom.vn.object.request;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class GeoModelCreateLayerDetails implements Serializable {
    
    private String name;
    private String title;
    private String nativeCRS;
    private String supportedFormats;
    private String requestSRS;
    private String responseSRS;
    private String srs;

    public GeoModelCreateLayerDetails(String name, String title, String nativeCRS, String supportedFormats, String requestSRS, String responseSRS, String srs) {
        this.name = name;
        this.title = title;
        this.nativeCRS = nativeCRS;
        this.supportedFormats = supportedFormats;
        this.requestSRS = requestSRS;
        this.responseSRS = responseSRS;
        this.srs = srs;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the nativeCRS
     */
    public String getNativeCRS() {
        return nativeCRS;
    }

    /**
     * @param nativeCRS the nativeCRS to set
     */
    public void setNativeCRS(String nativeCRS) {
        this.nativeCRS = nativeCRS;
    }

    /**
     * @return the supportedFormats
     */
    public String getSupportedFormats() {
        return supportedFormats;
    }

    /**
     * @param supportedFormats the supportedFormats to set
     */
    public void setSupportedFormats(String supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    /**
     * @return the requestSRS
     */
    public String getRequestSRS() {
        return requestSRS;
    }

    /**
     * @param requestSRS the requestSRS to set
     */
    public void setRequestSRS(String requestSRS) {
        this.requestSRS = requestSRS;
    }

    /**
     * @return the responseSRS
     */
    public String getResponseSRS() {
        return responseSRS;
    }

    /**
     * @param responseSRS the responseSRS to set
     */
    public void setResponseSRS(String responseSRS) {
        this.responseSRS = responseSRS;
    }

    /**
     * @return the srs
     */
    public String getSrs() {
        return srs;
    }

    /**
     * @param srs the srs to set
     */
    public void setSrs(String srs) {
        this.srs = srs;
    }
}
