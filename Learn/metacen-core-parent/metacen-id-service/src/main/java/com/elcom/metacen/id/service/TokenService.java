/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.service;

/**
 *
 * @author Admin
 */
public interface TokenService {

    String getAccessToken();

    boolean removeAccessToken();

    String getAccessTokenUpdate(String uuid);

    String setAccessTokenUpdate(String uuid);

    boolean removeAccessTokenUpdate(String uuid);

    String getRefreshTokenUpdate(String uuid);

    String setRefreshTokenUpdate(String uuid);

    boolean removeRefreshTokenUpdate(String uuid);

    boolean removeTokenServer();
}
