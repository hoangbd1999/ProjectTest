/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.thread;

import com.elcom.metacen.id.mail.MailContentDTO;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class IdThreadManager {

    private final Logger LOGGER = LoggerFactory.getLogger(IdThreadManager.class);

    @Autowired
    @Qualifier("fixedThreadPool")
    private ExecutorService executorService;

    public <T> Future<T> executeWithResult(Callable<T> callable) {
        return executorService.submit(callable);
    }
    
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public boolean sendEmail(MailContentDTO item) {
        try {
            SendMailThread mailThread = new SendMailThread(item);
            executorService.submit(mailThread);
        } catch (Exception ex) {
            LOGGER.error("Error to send email >>> " + ex.toString());
            return false;
        }
        return true;
    }
}
