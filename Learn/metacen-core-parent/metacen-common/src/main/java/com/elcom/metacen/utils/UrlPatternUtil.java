/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Admin
 */
public class UrlPatternUtil {

    public static boolean matchPattern(String urlpattern, String url) {
        Pattern pattern = Pattern.compile(urlpattern);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static void main(String[] args) {
        String urlpattern = "^/v[1-9].0/systemconfig/sites(/?|/.*|\\?.*)$";
        //String urlpattern = "/v1.0/systemconfig/sites";
        String url = "/v1.0/systemconfig/sites";
        System.out.println("result: " + matchPattern(urlpattern, url));
    }
}
