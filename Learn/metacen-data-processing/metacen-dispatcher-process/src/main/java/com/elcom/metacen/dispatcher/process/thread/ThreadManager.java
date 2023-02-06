package com.elcom.metacen.dispatcher.process.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class ThreadManager {

    @Autowired
    @Qualifier("fixedThreadPool")
    private ExecutorService executorService;

    public <T> Future<T> executeWithResult(Callable<T> callable) {
        return executorService.submit(callable);
    }
    
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
