/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.constant;

/**
 *
 * @author Admin
 */
public class Constant {

    // Validation message
    public static final String VALIDATION_INVALID_PARAM_VALUE = "Invalid param value";
    public static final String VALIDATION_DATA_NOT_FOUND = "Data not found";
    public static final String VALIDATION_ACCOUNT_LOCKED = "Tài khoản đang bị khóa";

    // Response messages
    public static final String RESPONSE_UNKNOW_ERR = "Lỗi không xác định";

    public static final String REDIS_COUNTRIES_LST_KEY = "METACEN_COUNTRIES_LST";
    public static final int REDIS_COUNTRIES_LST_FETCH_MAX = 400;

    public static final String REDIS_VESSEL_LST_KEY = "METACEN_VSAT_VESSEL_TYPE_LST";
    public static final int REDIS_VESSEL_LST_FETCH_MAX = 1000;
    
    public static final String REDIS_VSAT_DATA_SOURCE_LST_KEY = "METACEN_VSAT_DATA_SOURCE_LST";
    public static final int REDIS_VSAT_DATA_SOURCE_LST_FETCH_MAX = 20000;

}
