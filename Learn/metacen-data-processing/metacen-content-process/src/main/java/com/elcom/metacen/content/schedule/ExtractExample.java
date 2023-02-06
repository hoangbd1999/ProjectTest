package com.elcom.metacen.content.schedule;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

import net.sf.sevenzipjbinding.*;
//import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
//import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
//import net.sf.sevenzipjbinding.util.ByteArrayStream;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

public class ExtractExample {
    String rootFolder;

    static class ExtractionException extends Exception {
        private static final long serialVersionUID = -5108931481040742838L;

        ExtractionException(String msg) {
            super(msg);
        }

        public ExtractionException(String msg, Exception e) {
            super(msg, e);
        }
    }

    class ExtractCallback implements IArchiveExtractCallback {
        private ISevenZipInArchive inArchive;
        private int index;
        private OutputStream outputStream;
        private File file;
        private ExtractAskMode extractAskMode;
        private boolean isFolder;

        ExtractCallback(ISevenZipInArchive inArchive) {
            this.inArchive = inArchive;
        }

        @Override
        public void setTotal(long total) throws SevenZipException {

        }

        @Override
        public void setCompleted(long completeValue) throws SevenZipException {

        }

        @Override
        public ISequentialOutStream getStream(int index,
                                              ExtractAskMode extractAskMode) throws SevenZipException {
            closeOutputStream();

            this.index = index;
            this.extractAskMode = extractAskMode;
            this.isFolder = (Boolean) inArchive.getProperty(index,
                    PropID.IS_FOLDER);

            if (extractAskMode != ExtractAskMode.EXTRACT) {
                // Skipped files or files being tested
                return null;
            }

            String path = (String) inArchive.getProperty(index, PropID.PATH);
            file = new File(outputDirectoryFile, path);
            if (isFolder) {
                createDirectory(file);
                return null;
            }

            createDirectory(file.getParentFile());

            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new SevenZipException("Error opening file: "
                        + file.getAbsolutePath(), e);
            }

            return new ISequentialOutStream() {
                public int write(byte[] data) throws SevenZipException {
                    try {
                        outputStream.write(data);
                    } catch (IOException e) {
                        throw new SevenZipException("Error writing to file: "
                                + file.getAbsolutePath());
                    }
                    return data.length; // Return amount of consumed data
                }
            };
        }

        private void createDirectory(File parentFile) throws SevenZipException {
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new SevenZipException("Error creating directory: "
                            + parentFile.getAbsolutePath());
                }
            }
        }

        private void closeOutputStream() throws SevenZipException {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    throw new SevenZipException("Error closing file: "
                            + file.getAbsolutePath());
                }
            }
        }

        @Override
        public void prepareOperation(ExtractAskMode extractAskMode)
                throws SevenZipException {

        }

        @Override
        public void setOperationResult(
                ExtractOperationResult extractOperationResult)
                throws SevenZipException {
            closeOutputStream();
            String path = (String) inArchive.getProperty(index, PropID.PATH);
            if (extractOperationResult != ExtractOperationResult.OK) {
                throw new SevenZipException("Invalid file: " + path);
            }

            if (!isFolder) {
                switch (extractAskMode) {
                    case EXTRACT:
                        System.out.println("Extracted " + path);
                        break;
                    case TEST:
                        System.out.println("Tested " + path);

                    default:
                }
            }
        }

    }

    private String archive;
    private String outputDirectory;
    private File outputDirectoryFile;
    private boolean test;
    private String filterRegex;

    public ExtractExample(String archive, String outputDirectory, boolean test, String filter) {
        this.archive = archive;
        this.outputDirectory = outputDirectory;
        this.test = test;
        this.filterRegex = filterToRegex(filter);
    }

    public void extract() throws ExtractionException {
        checkArchiveFile();
        prepareOutputDirectory();
        extractArchive();
    }

    private void prepareOutputDirectory() throws ExtractionException {
        outputDirectoryFile = new File(outputDirectory);
        if (!outputDirectoryFile.exists()) {
            outputDirectoryFile.mkdirs();
        } else {
            if (outputDirectoryFile.list().length != 0) {
                throw new ExtractionException("Output directory not empty: "
                        + outputDirectory);
            }
        }
    }

    private void checkArchiveFile() throws ExtractionException {
        if (!new File(archive).exists()) {
            throw new ExtractionException("Archive file not found: " + archive);
        }
        if (!new File(archive).canRead()) {
            System.out.println("Can't read archive file: " + archive);
        }
    }

    public void extractArchive() throws ExtractionException {
        RandomAccessFile randomAccessFile;
        boolean ok = false;
        try {
            randomAccessFile = new RandomAccessFile(archive, "r");
        } catch (FileNotFoundException e) {
            throw new ExtractionException("File not found", e);
        }
        try {
            extractArchive(randomAccessFile);
            ok = true;
        } finally {
            try {
                randomAccessFile.close();
            } catch (Exception e) {
                if (ok) {
                    throw new ExtractionException("Error closing archive file",
                            e);
                }
            }
        }
    }

    private static String filterToRegex(String filter) {
        if (filter == null) {
            return null;
        }
        return "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
    }

    private void extractArchive(RandomAccessFile file)
            throws ExtractionException {
        ISevenZipInArchive inArchive;
        boolean ok = false;
        try {
            inArchive = SevenZip.openInArchive(null,
                    new RandomAccessFileInStream(file));
        } catch (SevenZipException e) {
            throw new ExtractionException("Error opening archive", e);
        }
        try {

            int[] ids = null; // All items
            if (filterRegex != null) {
                ids = filterIds(inArchive, filterRegex);
            }
            inArchive.extract(ids, test, new ExtractCallback(inArchive));
            ok = true;
        } catch (SevenZipException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error extracting archive '");
            stringBuilder.append(archive);
            stringBuilder.append("': ");
            stringBuilder.append(e.getMessage());
            if (e.getCause() != null) {
                stringBuilder.append(" (");
                stringBuilder.append(e.getCause().getMessage());
                stringBuilder.append(')');
            }
            String message = stringBuilder.toString();

            throw new ExtractionException(message, e);
        } finally {
            try {
                inArchive.close();
            } catch (SevenZipException e) {
                if (ok) {
                    throw new ExtractionException("Error closing archive", e);
                }
            }
        }
    }

    private static int[] filterIds(ISevenZipInArchive inArchive, String regex) throws SevenZipException {
        List<Integer> idList = new ArrayList<Integer>();

        int numberOfItems = inArchive.getNumberOfItems();

        Pattern pattern = Pattern.compile(regex);
        for (int i = 0; i < numberOfItems; i++) {
            String path = (String) inArchive.getProperty(i, PropID.PATH);
            String fileName = new File(path).getName();
            if (pattern.matcher(fileName).matches()) {
                idList.add(i);
            }
        }

        int[] result = new int[idList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = idList.get(i);
        }
        return result ;
    }

    public static void main(String[] args) throws IOException{
        File file = new File("abc.tar.gz");
        File fileFolder = new File("testgz");
        List<File> a = new ArrayList<>();
//        unTarFile(file,fileFolder,a);
        unGzip(file,"testgz",a);


        System.out.println("d");
//        boolean test = false;
//        String filter = null;
//        try {
//            new ExtractExample("test2.rar", "test4", test, filter).extract();
//            System.out.println("Extraction successfull");
//        } catch (ExtractionException e) {
//            System.err.println("ERROR: " + e.getLocalizedMessage());
//            e.printStackTrace();
//        }
    }
    private static void unTarFile(File tarFile, File destFile, List<File> listFile) throws IOException{
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);
        TarArchiveEntry tarEntry = null;

        // tarIn is a TarArchiveInputStream
        while ((tarEntry = tis.getNextTarEntry()) != null) {
            File outputFile = new File(destFile + File.separator + tarEntry.getName());
            if(tarEntry.isDirectory()){
                if(!outputFile.exists()){
                    outputFile.mkdirs();
                }
            }else{
                //File outputFile = new File(destFile + File.separator + tarEntry.getName());
                System.out.println("outputFile File ---- " + outputFile.getAbsolutePath());
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                fos.close();
            }
        }
        tis.close();
    }
   private static void unGzip(File inputFile,String root,List<File> listFile){
       try {
           String path =inputFile.getPath();
           String inputPath= path;
           String folder1 ="a" + UUID.randomUUID().toString() + "b";
           String folder ="a" + UUID.randomUUID().toString() + "b";
//           String tmp = " "+root.substring(root.lastIndexOf("/")+1)+"/"+ folder;
//           String outPath = " "+root.substring(root.lastIndexOf("/")+1)+"/"+ folder;
           String tmp = "testgz"+"/"+ folder1;
           String outPath = "testgz"+"/"+ folder;
           String pathTarget=root;
           String pathOutput=root+"/"+folder;
           Files.createDirectories(Paths.get(pathOutput));
           String outputFileTmp = getFileName(inputFile, pathTarget);
           System.out.println("outputFile " + outputFileTmp);
           File tarFile = new File(outputFileTmp);
           // Calling method to decompress file
           tarFile = deCompressGZipFile(inputFile, tarFile);
           File destFile = new File(outPath);
           unTarFile(tarFile, destFile,listFile);
           tarFile.delete();

       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
   }
    private static String getFileName(File inputFile, String outputFolder){
        return outputFolder + File.separator +
                inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
    }

    private static File deCompressGZipFile(File gZippedFile, File tarFile) throws IOException{
        FileInputStream fis = new FileInputStream(gZippedFile);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fis);

        FileOutputStream fos = new FileOutputStream(tarFile);
        byte[] buffer = new byte[1024];
        int len;
        while((len = gZIPInputStream.read(buffer)) > 0){
            fos.write(buffer, 0, len);
        }
        fos.close();
        gZIPInputStream.close();
        return tarFile;
    }

}