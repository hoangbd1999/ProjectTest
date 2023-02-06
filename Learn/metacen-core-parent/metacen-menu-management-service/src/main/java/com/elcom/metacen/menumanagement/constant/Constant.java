/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.constant;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class Constant {
    
    // Validation message
    public static final String VALIDATION_INVALID_PARAM_VALUE = "Tham số truyền lên không đúng";
    public static final String VALIDATION_DATA_NOT_FOUND = "Không tìm thấy dữ liệu";
    
    // Default Role
    public static final String DEFAULT_ROLE = "its_USER";
    
    // Fix ADMIN service
    // USER_SERVICE ==> USER_ADMIN, STORE_SERVICE ==> STORE_ADMIN,...
    public static final Map<String, String> SERVICE_ADMIN_MAP = new HashMap<>();
}
