/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elcom.vn.common;

/**
 * Exception throw by shindig XML parsing utility routines
 */
public class XmlException extends Exception {

    public XmlException(String message, Exception cause) {
        super(message, cause);
    }

    public XmlException(Exception cause) {
        super(cause);
    }

    public XmlException(String message) {
        super(message);
    }
}
