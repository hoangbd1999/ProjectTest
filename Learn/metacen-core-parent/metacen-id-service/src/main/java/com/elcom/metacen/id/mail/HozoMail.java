/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Admin
 */
public class HozoMail {

    private static final Logger logger = LoggerFactory.getLogger(HozoMail.class);

    public static final String fromMail = "support@colearn.vn";
    public static final String passFromMail = "Colearn@123";

    public boolean sendMail(String fromName, String subject, String content, List<ToMail> listToMail) {
        try {
            Properties props = new Properties();
            //Modify
            props.put("mail.smtp.host", "smtp.zoho.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            ////////////////////////////////////////////////////////////////////
            props.put("mail.mime.charset", "utf-8");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                //Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromMail, passFromMail);
                }
            });

            InternetAddress[] listAddress = new InternetAddress[listToMail.size()];
            InternetAddress address = null;

            for (int i = 0; i < listToMail.size(); i++) {
                ToMail toMail = listToMail.get(i);
                address = new InternetAddress();
                address.setAddress(toMail.getAddress());
                address.setPersonal(toMail.getName(), "UTF-8");
                listAddress[i] = address;
            }
            //Message message = new MimeMessage(session);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromMail, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, listAddress); //InternetAddress.parse(toMail)
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html; charset=UTF-8");
            //message.setText(content);

            Transport.send(message);
            logger.info("Gửi mail thành công!");
            return true;
        } catch (Exception ex) {
            logger.error("HozoMail.sendMail.ex: " + ex.toString());
            ex.printStackTrace();
        }

        return false;
    }

    public boolean sendMail(String fromName, String subject, String content, List<ToMail> listToMail,
            List<ToMail> listCCMail, List<ToMail> listBCCMail) {
        try {
            Properties props = new Properties();

            props.put("mail.smtp.host", "smtp.zoho.com");
            props.put("mail.smtp.socketFactory.port", 465);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.startssl.enable", "true");
            props.put("mail.mime.charset", "utf-8");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                //Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromMail, passFromMail);
                }
            });

            //To Mail
            InternetAddress[] listAddress = new InternetAddress[listToMail.size()];
            InternetAddress address = null;
            for (int i = 0; i < listToMail.size(); i++) {
                ToMail toMail = listToMail.get(i);
                address = new InternetAddress();
                address.setAddress(toMail.getAddress());
                address.setPersonal(toMail.getName(), "UTF-8");
                listAddress[i] = address;
            }

            //CC Mail
            InternetAddress[] listCCAddress = null;
            if (listCCMail != null && listCCMail.size() > 0) {
                listCCAddress = new InternetAddress[listCCMail.size()];
                for (int i = 0; i < listCCMail.size(); i++) {
                    ToMail ccMail = listCCMail.get(i);
                    address = new InternetAddress();
                    address.setAddress(ccMail.getAddress());
                    address.setPersonal(ccMail.getName(), "UTF-8");
                    listCCAddress[i] = address;
                }
            }

            //BCC Mail
            InternetAddress[] listBCCAddress = null;
            if (listBCCMail != null && listBCCMail.size() > 0) {
                listBCCAddress = new InternetAddress[listBCCMail.size()];
                for (int i = 0; i < listBCCMail.size(); i++) {
                    ToMail bccMail = listBCCMail.get(i);
                    address = new InternetAddress();
                    address.setAddress(bccMail.getAddress());
                    address.setPersonal(bccMail.getName(), "UTF-8");
                    listBCCAddress[i] = address;
                }
            }

            //Message message = new MimeMessage(session);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromMail, fromName));
            message.setRecipients(Message.RecipientType.TO, listAddress); //InternetAddress.parse(toMail)
            if (listCCMail != null && listCCMail.size() > 0) {
                message.setRecipients(Message.RecipientType.CC, listCCAddress);
            }
            if (listBCCMail != null && listBCCMail.size() > 0) {
                message.setRecipients(Message.RecipientType.BCC, listBCCAddress);
            }
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html; charset=UTF-8");
            //message.setText(content);

            Transport.send(message);
            logger.info("Gửi mail thành công!");
            return true;
        } catch (Exception ex) {
            logger.error("SendMail.sendMail.ex: " + ex.toString());
        }
        return false;
    }

    public boolean sendMail2(String fromName, String subject, String content, List<ToMail> listToMail,
            List<ToMail> listCCMail, List<ToMail> listBCCMail) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.zoho.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            ////////////////////////////////////////////////////////////////////
            props.put("mail.mime.charset", "utf-8");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromMail, passFromMail);
                }
            });

            //To Mail
            InternetAddress[] listAddress = new InternetAddress[listToMail.size()];
            InternetAddress address = null;
            for (int i = 0; i < listToMail.size(); i++) {
                ToMail toMail = listToMail.get(i);
                address = new InternetAddress();
                address.setAddress(toMail.getAddress());
                address.setPersonal(toMail.getName(), "UTF-8");
                listAddress[i] = address;
            }

            //CC Mail
            InternetAddress[] listCCAddress = null;
            if (listCCMail != null && listCCMail.size() > 0) {
                listCCAddress = new InternetAddress[listCCMail.size()];
                for (int i = 0; i < listCCMail.size(); i++) {
                    ToMail ccMail = listCCMail.get(i);
                    address = new InternetAddress();
                    address.setAddress(ccMail.getAddress());
                    address.setPersonal(ccMail.getName(), "UTF-8");
                    listCCAddress[i] = address;
                }
            }

            //BCC Mail
            InternetAddress[] listBCCAddress = null;
            if (listBCCMail != null && listBCCMail.size() > 0) {
                listBCCAddress = new InternetAddress[listBCCMail.size()];
                for (int i = 0; i < listBCCMail.size(); i++) {
                    ToMail bccMail = listBCCMail.get(i);
                    address = new InternetAddress();
                    address.setAddress(bccMail.getAddress());
                    address.setPersonal(bccMail.getName(), "UTF-8");
                    listBCCAddress[i] = address;
                }
            }

            //Message message = new MimeMessage(session);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromMail, fromName));
            message.setRecipients(Message.RecipientType.TO, listAddress); //InternetAddress.parse(toMail)
            if (listCCMail != null && listCCMail.size() > 0) {
                message.setRecipients(Message.RecipientType.CC, listCCAddress);
            }
            if (listBCCMail != null && listBCCMail.size() > 0) {
                message.setRecipients(Message.RecipientType.BCC, listBCCAddress);
            }
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html; charset=UTF-8");
            //message.setText(content);

            Transport.send(message);
            logger.info("Gửi mail thành công!");
            return true;
        } catch (Exception ex) {
            logger.error("SendMail.sendMail.ex: " + ex.toString());
        }
        return false;
    }

    public static void main(String[] args) {
        HozoMail mailUtil = new HozoMail();
        String fromName = "Ha Nguyễn";
        String subject = "Email gửi test từ Hozo mail";
        String content = "Nội dung email gửi từ Hozo";
        List<ToMail> listToMail = new ArrayList<>();
        ToMail mail1 = new ToMail("Blue Heart", "hanm@elcom.com.vn");
        listToMail.add(mail1);

        boolean status = mailUtil.sendMail(fromName, subject, content, listToMail);
        System.out.println("Result: " + status);
    }
}
