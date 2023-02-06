/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.constant;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;

/**
 *
 * @author Admin
 */
public class Constant {

    //Typesafe config
    public static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config" + File.separator;
    private static final Config CONFIG;

    //Constant
    public static String CONSTANT_STR1 = "";
    public static int CONSTANT_INT2 = 0;
    public static boolean CONSTANT_BOOL3 = false;
    public static final int USER_SIGNUP_NORMAL = 0;
    public static final int USER_SIGNUP_FACEBOOK = 1;
    public static final int USER_SIGNUP_GOOGLE = 2;
    public static final int USER_SIGNUP_APPLE = 3;

    // Validation message
    public static final String VALIDATION_INVALID_PARAM_VALUE = "Invalid param value";
    public static final String VALIDATION_DATA_NOT_FOUND = "Data not found";
    public static final String VALIDATION_ACCOUNT_LOCKED = "Tài khoản đang bị khóa";

    //Forgot password
    public static final String MAIL_FORGOT_PW_SEND_FROM = "CoLearn support";
    public static final String MAIL_FORGOT_PW_TITLE = "Qu\u00EAn m\u1EADt kh\u1EA9u t\u00E0i kho\u1EA3n %s";
    public static final String MAIL_FORGOT_PW_CONTENT = "Xin ch\u00E0o %s,"
            + "<br />\u0110\u00E2y l\u00E0 m\u1EADt kh\u1EA9u c\u1EE7a b\u1EA1n: <b>%s</b>"
            + "<br />Xin c\u1EA3m \u01A1n!";
    public static final String MAIL_FORGOT_PW_CONTENT_LINK = "Xin ch\u00E0o %s,"
            + "<br />\u0110\u01B0\u1EDDng d\u1EABn \u0111\u1EB7t l\u1EA1i m\u1EADt kh\u1EA9u c\u1EE7a b\u1EA1n l\u00E0 : <a href='%s'>%s</a>"
            + "<br />\u0110\u01B0\u1EDDng d\u1EABn t\u1ED3n t\u1EA1i trong v\u00F2ng <b>%s ph\u00FAt</b>."
            + "<br />Xin c\u1EA3m \u01A1n!";
    public static final String MAIL_FORGOT_PW_CONTENT_OTP = "Xin ch\u00E0o %s,"
            + "<br />M\u00E3 OTP \u0111\u1EC3 x\u00E1c th\u1EF1c qu\u00EAn m\u1EADt kh\u1EA9u l\u00E0: <b>%s</b>"
            + "<br />OTP t\u1ED3n t\u1EA1i trong v\u00F2ng <b>%s ph\u00FAt</b>."
            + "<br />Xin c\u1EA3m \u01A1n!";
    
    // OTP time expired
    public static final long OTP_TIME_EXIPRED = 10 * 60 * 1000;//15ph
    public static final String its_OTP_KEY = "its_OTP_KEY";
    public static final String its_OTP_PASSWORD_KEY = "its_OTP_PASSWORD_KEY";
    public static final int MAX_SMS_PER_EXPIRED = 5;//Trong 1 ngưỡng Expired cho 5 lần gửi SMS
    
    // AES Key
    public static final String AES_KEY = "Elc0m2020@321456";

    static {
        Config baseConfig = ConfigFactory.load("constant");
        CONFIG = ConfigFactory.parseFile(new File(CONFIG_DIR + "constant.conf")).withFallback(baseConfig);
        loadConfig();
    }

    public static void loadConfig() {
        CONSTANT_STR1 = CONFIG.getString("CONSTANT_STR1");
        CONSTANT_INT2 = CONFIG.getInt("CONSTANT_INT2");
        CONSTANT_BOOL3 = CONFIG.getBoolean("CONSTANT_BOOL3");
    }
}
