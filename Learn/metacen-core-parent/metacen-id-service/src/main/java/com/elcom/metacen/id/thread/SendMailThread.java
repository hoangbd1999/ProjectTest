/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.thread;

//import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.mail.HozoMail;
import com.elcom.metacen.id.mail.MailContentDTO;
import com.elcom.metacen.id.mail.ToMail;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Admin
 */
public class SendMailThread implements Runnable {

    private MailContentDTO item;
    private final Logger logger = LoggerFactory.getLogger(SendMailThread.class);

    public SendMailThread(MailContentDTO item) {
        this.item = item;
    }

    @Override
    public void run() {
        if (item != null) {
            //SendMail mailUtil = new SendMail();
            HozoMail mailUtil = new HozoMail();

            boolean status = false;
            if ("one".equalsIgnoreCase(item.getType())) {
                ToMail toMail = new ToMail(item.getFromName(), item.getEmailTo());
                List<ToMail> listToMail = new ArrayList<>();
                listToMail.add(toMail);
                
                status = mailUtil.sendMail(item.getFromName(), item.getEmailTitle(), item.getEmailContent(), listToMail);
            } else if ("multi".equalsIgnoreCase(item.getType())) {
                status = mailUtil.sendMail2(item.getFromName(), item.getEmailTitle(),
                        item.getEmailContent(), item.getListEmail(), item.getListCC(), item.getListBCC());
            }
            logger.info("Send 2 list: " + item.getEmailToList() + ", title: " + item.getEmailTitle()
                    + " ==> " + status);
        }
    }
}
