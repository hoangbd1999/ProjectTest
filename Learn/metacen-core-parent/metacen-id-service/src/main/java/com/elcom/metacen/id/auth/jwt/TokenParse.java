/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.auth.jwt;

/**
 *
 * @author Hungnk
 */
public class TokenParse {
    private int CodeStatus;
    private String message;
    private String data;

    public TokenParse(int CodeStatus, String message, String data) {
        this.CodeStatus = CodeStatus;
        this.message = message;
        this.data = data;
    }

    public int getCodeStatus() {
        return CodeStatus;
    }

    public void setCodeStatus(int CodeStatus) {
        this.CodeStatus = CodeStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    
    
}
