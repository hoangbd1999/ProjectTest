/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.mail;

import com.google.gson.Gson;
import java.util.List;

/**
 *
 * @author Admin
 */
public class MailContentDTO {

    private String fromName;
    private String emailFrom;
    private String emailTo;
    private String emailTitle;
    private String emailContent;
    //Gui nhieu
    private List<ToMail> listEmail;
    private List<ToMail> listCC;
    private List<ToMail> listBCC;
    //type
    private String type = "one";

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getEmailTitle() {
        return emailTitle;
    }

    public void setEmailTitle(String emailTitle) {
        this.emailTitle = emailTitle;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public List<ToMail> getListEmail() {
        return listEmail;
    }

    public void setListEmail(List<ToMail> listEmail) {
        this.listEmail = listEmail;
    }

    public List<ToMail> getListCC() {
        return listCC;
    }

    public void setListCC(List<ToMail> listCC) {
        this.listCC = listCC;
    }

    public List<ToMail> getListBCC() {
        return listBCC;
    }

    public void setListBCC(List<ToMail> listBCC) {
        this.listBCC = listBCC;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getEmailToList(){
        if("one".equalsIgnoreCase(type)){
            return emailTo;
        } else if("multi".equalsIgnoreCase(type)){
            String result = "";
            if(listEmail != null && listEmail.size() > 0){
                result += "to: ";
                for(ToMail email : listEmail){
                    result += email.getAddress() + ";";
                }
            }
            if(listCC != null && listCC.size() > 0){
                result += "cc: ";
                for(ToMail email : listCC){
                    result += email.getAddress() + ";";
                }
            }
            if(listBCC != null && listBCC.size() > 0){
                result += "bcc: ";
                for(ToMail email : listBCC){
                    result += email.getAddress() + ";";
                }
            }
            return result;
        }
        return null;
    }
}
