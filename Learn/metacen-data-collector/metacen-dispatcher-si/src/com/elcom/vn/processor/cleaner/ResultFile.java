package com.elcom.vn.processor.cleaner;

import java.util.List;

/**
 *
 * @author anhdv
 */
public class ResultFile {

    private String scan_Stop_Time;
    
    private List<ResultFileCarriers> carrier;
    
    public ResultFile() {
    }

    /**
     * @return the scan_Stop_Time
     */
    public String getScan_Stop_Time() {
        return scan_Stop_Time;
    }

    /**
     * @param scan_Stop_Time the scan_Stop_Time to set
     */
    public void setScan_Stop_Time(String scan_Stop_Time) {
        this.scan_Stop_Time = scan_Stop_Time;
    }

    /**
     * @return the carrier
     */
    public List<ResultFileCarriers> getCarrier() {
        return carrier;
    }

    /**
     * @param carrier the carrier to set
     */
    public void setCarrier(List<ResultFileCarriers> carrier) {
        this.carrier = carrier;
    }
}
