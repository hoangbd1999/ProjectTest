package com.elcom.metacen.dispatcher.process.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Admin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataProcessConfigValue implements Serializable {

    private String coordinates;
    private String source_ip;
    private String dest_ip;
    private String source_id;
    private String data_type;
    private String format;
    
    public static void main(String[] args) {
        String s = "-181.09821267891";
        Double ff1 = Double.parseDouble(s);
        BigDecimal ff2 = new BigDecimal(s);
        System.out.println("ff1: " + ff1);
        System.out.println("ff2: " + ff2);
    }

    public DataProcessConfigValue() {
    }
    
    /**
     * @return the coordinates
     */
    public String getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * @return the source_ip
     */
    public String getSource_ip() {
        return source_ip;
    }

    /**
     * @param source_ip the source_ip to set
     */
    public void setSource_ip(String source_ip) {
        this.source_ip = source_ip;
    }

    /**
     * @return the dest_ip
     */
    public String getDest_ip() {
        return dest_ip;
    }

    /**
     * @param dest_ip the dest_ip to set
     */
    public void setDest_ip(String dest_ip) {
        this.dest_ip = dest_ip;
    }

    /**
     * @return the source_id
     */
    public String getSource_id() {
        return source_id;
    }

    /**
     * @param source_id the source_id to set
     */
    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    /**
     * @return the data_type
     */
    public String getData_type() {
        return data_type;
    }

    /**
     * @param data_type the data_type to set
     */
    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
