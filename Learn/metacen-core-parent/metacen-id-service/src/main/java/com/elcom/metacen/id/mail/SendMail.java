package com.elcom.metacen.id.mail;

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
 * Send mail
 *
 * @author anhdv
 */
public class SendMail {

    private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

    public boolean sendMail(String host, String port, final String fromMail, final String passFromMail,
            String fromName, String subject, String content, List<ToMail> listToMail) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", port);
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
            message.setFrom(new InternetAddress(fromMail, fromName));
            message.setRecipients(Message.RecipientType.TO, listAddress); //InternetAddress.parse(toMail)
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

    public boolean sendMail(String host, String port, final String fromMail, final String passFromMail,
            String fromName, String subject, String content, List<ToMail> listToMail, 
            List<ToMail> listCCMail, List<ToMail> listBCCMail) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", port);
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
    
    public boolean sendMail2(String host, String port, final String fromMail, final String passFromMail,
            String fromName, String subject, String content, List<ToMail> listToMail, 
            List<ToMail> listCCMail, List<ToMail> listBCCMail) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", port);
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
}
