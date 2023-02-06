package com.elcom.vn.processor.cleaner;

import com.elcom.util.miscellaneous.thread.ActionThread;
import com.elcom.vn.utils.StringUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class CleanFolderScanner extends ActionThread {

    protected final Logger logger;
    
    protected final String cleanFolderDirPath;

    public CleanFolderScanner(String name, Logger logger, String cleanFolderDirPath) {
        
        super(name);
        
        this.logger = logger;
        
        this.cleanFolderDirPath = cleanFolderDirPath;
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
        return 1L * 60L * 60L * 1000L; // 1 hours sleep
    }

    @Override
    protected void action() throws Exception {

        String[] dirArr = this.cleanFolderDirPath.split(",");
        
        long currTime = System.currentTimeMillis();
        final long oldTime = 2L * 60L * 60L * 1000L; // 3 hours
        
        for( String dir : dirArr ) {
            
            File[] level1Files = new File(dir).listFiles();
            
            if ( level1Files == null || level1Files.length == 0 )
                continue;
            
            //Arrays.sort(level1Files, Comparator.comparingLong(File::lastModified)); // Sap xep thu tu tang dan cua lastModified(thu muc cu hon xu ly truoc)
            
            for ( File f1 : level1Files ) {
                
                if ( f1.lastModified() > currTime - oldTime )
                    continue;
                
                try {
                    
                    logger.info("Delete [ {} ]: {}", f1.getPath(), FileUtils.deleteQuietly(f1));
                    
                } catch (Exception e) {
                    logger.error("ex: {}", StringUtil.printException(e));
                }
            }
        }
    }
    
    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
