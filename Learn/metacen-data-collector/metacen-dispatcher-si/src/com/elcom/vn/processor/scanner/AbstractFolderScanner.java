package com.elcom.vn.processor.scanner;

import com.elcom.util.miscellaneous.thread.ActionThread;
import com.elcom.util.queue.BoundBlockingQueue;
import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public abstract class AbstractFolderScanner extends ActionThread implements FilenameFilter {
    
    protected final Logger logger;
    
    protected final String dataMiningDirPath;
    
    protected final String childDirName;
    
    protected final boolean recursive;
    
    protected final int maxFilePerScan;
    
    protected final BoundBlockingQueue<String> decodeFileQueue;
    
    public AbstractFolderScanner(String name, Logger logger, String dataMiningDirPath
                                , int maxFilesPerScan
                                , String childDirName, boolean recursive, BoundBlockingQueue<String> decodeFileQueue) {
        
        super(name);

        this.logger = logger;
        this.dataMiningDirPath = dataMiningDirPath;
        this.childDirName = childDirName;
        this.recursive = recursive;
        this.maxFilePerScan = maxFilesPerScan;
        this.decodeFileQueue = decodeFileQueue;
    }

    @Override
    protected void onThrowable(Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected throwable", t);
        }
    }

    @Override
    protected void onExecuting() throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Running...");
        }
    }

    @Override
    protected void onKilling() {
        if (logger.isInfoEnabled()) {
            logger.info("Terminated.");
        }
    }

    @Override
    protected long sleeptime() throws Throwable {
        return 2L * 60L * 1000L; // Thời gian nghỉ giữa những lần scan xong.
//        return Long.MAX_VALUE;
    }

    @Override
    protected void action() throws Exception {
        
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            
            File[] files = new File(dataMiningDirPath).listFiles((file, fileName) -> fileName.endsWith("_FN"));
            
            if( files == null || files.length == 0 ) {
                logger.info("No files for scan!");
                return;
            }
            
            // long beginDayInMs = LocalDate.now().toDate().getTime();

//            Stopwatch stopwatch2 = Stopwatch.createStarted();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified)); // Sap xep thu tu tang dan cua lastModified(file cu hon xu ly truoc)
//            stopwatch2.stop();
//            logger.info("Finish sorting [ {} ] items, spent: [ {} ] ms", files.length, getElapsedTime(stopwatch2.elapsed(TimeUnit.MILLISECONDS)));
            
            for( File f : files ) {
                
                if( !f.isDirectory() )
                    continue;
                
                try {
                    //if( f.lastModified() > beginDayInMs ) {
                    String folderPath = f.getPath();
//                    if( folderPath.endsWith("_FN") && folderPath.equals("/mnt/data/thanhdd/remote_sensing/Sentinel2/bienDong_infor/S2B_MSIL2A_20221017T031719_N0400_R118_T48PWR_20221017T062917_FN") ) {
//                    if( folderPath.endsWith("_FN") ) {
                        
                        /*if( isDirEmpty(f.toPath()) ) {
                            try {
                                FileUtils.deleteDirectory(f);
                            } catch (Exception e) {
                                logger.error("[Scanner] Clean origin data folder error: {}", StringUtil.printException(e));
                            }
                            continue;
                        }*/
                        
//                        logger.info("Begin progress [ {} ] ............", f.getPath());

                        File srcFolder = new File(folderPath);
                        File destFolder = new File(folderPath.replace("_FN", ""));
                        
                        if( !srcFolder.renameTo(destFolder) ) {
                            logger.error("Rename [ {} ] -> to [ {} ] FAILED!", srcFolder.getPath(), destFolder.getPath());
                            continue;
                        }
                        
                        logger.info("Renamed [ {} ] -> to [ {} ] SUCCESS!", srcFolder.getPath(), destFolder.getPath());

                        this.enqueue(destFolder.getPath(), decodeFileQueue);

                        while ( this.decodeFileQueue.size() > this.maxFilePerScan / 4 ) {
                            logger.info("Sleep for waiting queue release ........");
                            Thread.sleep(10000L);
                        }

//                        logger.info("Finish dir [ {} ], currentQueueSize: {}", f.getPath(), this.decodeFileQueue.size());
//                    }
                } catch (Exception e) {
                    logger.error("ex: ", e);
                }
            }
            
            stopwatch.stop();
            
            logger.info("Finish scan, spent: [ {} ] ms", getElapsedTime(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            
        } catch (Exception ex) {
            logger.error("ex: ", ex);
        }
    }

    protected void enqueue(String e, BoundBlockingQueue<String> queue) {
        try {
            boolean ok;
            do {
                ok = queue.offer(e, 250L, TimeUnit.MILLISECONDS);
            } while (!ok);
        } catch (Exception ex) {
            logger.warn("enqueue.ex: ", e);
        }
    }

    private String getElapsedTime(long miliseconds) {
        return miliseconds + " (ms)";
    }
    
    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
