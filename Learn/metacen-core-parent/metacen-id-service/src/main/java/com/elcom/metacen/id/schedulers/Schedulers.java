package com.elcom.metacen.id.schedulers;

//import com.elcom.metacen.id.controller.UserController;
//import com.elcom.metacen.id.model.User;
//import com.elcom.metacen.id.service.UserService;

import com.elcom.metacen.id.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 *
 * @author anhdv
 */
@Service
public class Schedulers {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);
    @Autowired
    private TokenService tokenService;
    @Async
    @Scheduled( cron = "0 0 0 * * ?")
    public void removeRefreshTokenUpdate() throws InterruptedException{
        tokenService.removeTokenServer();
    }

    @Bean
    public TaskScheduler taskScheduler() {
      final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
      scheduler.setPoolSize(10);
      return scheduler;
    }
}
