package elcom.com.neo4j.utils;//package com.elcom.media.utils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.RandomAccessFile;
//import java.io.SequenceInputStream;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//
//import it.sauronsoftware.jave.AudioAttributes;
//import it.sauronsoftware.jave.AudioInfo;
//import it.sauronsoftware.jave.Encoder;
//import it.sauronsoftware.jave.EncodingAttributes;
//import it.sauronsoftware.jave.MultimediaInfo;
//
///**
// *
// * @author Admin
// */
//public class Mp3Util {
//
//    public static void main(String[] args) throws Exception {
//        /*
//		File file = new File("C:/Users/ddp/Desktop/aa");
//		File[] allFiles = file.listFiles();
//		for (int i = 0; i < allFiles.length; i++) {
//			File filei = allFiles[i];
//			ReadFileUtils readFileUtils = new ReadFileUtils(filei);
//			System.out.println( "String file" + i + " = \"" + filei.getAbsolutePath().replace("\\", "/") + "\";" 
//					+ "\n//"  + readFileUtils.getFileSizeToMB() + "==" + readFileUtils.m);
//		}
//         */
//        
//
////        String file0 = "C:/Users/ddp/Desktop/aa/1.mp3";
//        //3.82==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=250000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file1 = "C:/Users/ddp/Desktop/aa/10.mp3";
//        //2.59==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=169600, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
//        
//        //9.33==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=244400, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=320))
////        String file3 = "C:/Users/ddp/Desktop/aa/12.mp3";
//        //3.92==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=171400, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=192))
////        String file4 = "C:/Users/ddp/Desktop/aa/13.mp3";
//        //8.02==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=350600, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=192))
////        String file5 = "C:/Users/ddp/Desktop/aa/2.mp3";
//        //10.43==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=273500, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=320))
////        String file6 = "C:/Users/ddp/Desktop/aa/3.mp3";
//        //4.84==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=317000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file7 = "C:/Users/ddp/Desktop/aa/4.mp3";
//        //4.84==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=317000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file8 = "C:/Users/ddp/Desktop/aa/5.mp3";
//        //3.82==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=250000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file9 = "C:/Users/ddp/Desktop/aa/6.mp3";
//        //4.84==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=317000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file10 = "C:/Users/ddp/Desktop/aa/7.mp3";
//        //10.43==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=273500, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=320))
////        String file11 = "C:/Users/ddp/Desktop/aa/8.mp3";
//        //10.43==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=273500, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=320))
////        String file12 = "C:/Users/ddp/Desktop/aa/9.mp3";
//        //9.33==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=244400, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=320))
//        
////        String file14 = "C:/Users/ddp/Desktop/aa/qjhy_hh.mp3";
//        //0.08==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=5000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=48000, channels=2, bitRate=128))
//        
//        //0.02==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=5400, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=16000, channels=1, bitRate=24))
////        String file16 = "C:/Users/ddp/Desktop/aa/Sleep Away.mp3";
//        //4.62==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=201700, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=192))
////        String file17 = "C:/Users/ddp/Desktop/aa/ .mp3";
//        //4.07==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=266500, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
////        String file18 = "C:/Users/ddp/Desktop/aa/Zhou Bichang-The Thief of the Moon.mp3";
//        //3.82==it.sauronsoftware.jave.MultimediaInfo (format=mp3, duration=250000, video=null, audio=it.sauronsoftware.jave.AudioInfo (decoder=mp3, samplingRate=44100, channels=2, bitRate=128))
//
//        String file2 = "H://Learn/1.mp3";
//        String file13 = "H://Learn/2.mp3";
////        String file15 = "H://Learn/3.mp3";
//        String finalFile = "H://Learn/merge.mp3";
//        
//        Long start = new Date().getTime();
//        String outFile1 = testCompressMp3Samll(file2, 128, 44100);
//        Long end = new Date().getTime();
//        System.out.println("Time spent:" + (end - start));
//
//        ReadFileUtils readFileUtils1 = new ReadFileUtils(new File(outFile1));
//        System.out.println(readFileUtils1.getFileSize() + "==" + readFileUtils1.m);
//
//        Long start1 = new Date().getTime();
//        String outFile2 = testCompressMp3Samll(file13, 128, 44100);
//        Long end1 = new Date().getTime();
//        System.out.println("Time spent:" + (end1 - start1));
//        ReadFileUtils readFileUtils = new ReadFileUtils(new File(outFile2));
//        System.out.println(readFileUtils.getFileSize() + "==" + readFileUtils.m);
//
//        //String outFile1 = testCompressMp3Samll(file6, 128, 44100);
//        //ReadFileUtils readFileUtils1 = new ReadFileUtils(new File(outFile1));
//        //System.out.println(readFileUtils1.m);
//        
//        List<String> files = new ArrayList<>();
//        files.add(file2);
//        files.add(file13);
//        Long start3 = new Date().getTime();
//        packageMp3(finalFile, files);
//        Long end3 = new Date().getTime();
//        System.out.println("Merge time:" + (end3 - start3));
//        ReadFileUtils readFileUtils3 = new ReadFileUtils(new File(finalFile));
//        System.out.println(readFileUtils3.getFileSize() + "==" + readFileUtils3.m);
//
//        ReadFileUtils readFileUtils2 = new ReadFileUtils(new File(file13));
//        System.out.println(readFileUtils2.getFileSize() + "==" + readFileUtils2.m);
//    }
//
//    /**
//     * Convert the file
//     *
//     * @param filePaths
//     * @throws Exception
//     */
//    public static void changeMp3(List<String> filePaths) throws Exception {
//        if (CollectionUtils.isEmpty(filePaths)) {
//            return;
//        }
//
//        //Get the most samplingRate in mp3, if it is a file, do not convert, if the samplingRate of the file is consistent, do not convert
//        if (filePaths.size() == 1) {
//            return;
//        }
//
//        Map<Integer, Integer> sampLingRateNums = new HashMap<>();
//
//        //The largest sampLingRate of the program
//        Integer maxSampLingRate = 0;
//        //The size of the largest file
//        Long maxFileSize = 0L;
//        for (String file : filePaths) {
//            ReadFileUtils readFileUtils = new ReadFileUtils(new File(file));
//            Long fileSize = readFileUtils.getFileSize();
//
//            MultimediaInfo m = readFileUtils.m;
//            AudioInfo audioInfo = m.getAudio();
//            int sampLingRate = audioInfo.getSamplingRate();
//
//            if (fileSize > maxFileSize) {
//                maxFileSize = fileSize;
//                maxSampLingRate = sampLingRate;
//            }
//            Integer num = sampLingRateNums.get(sampLingRate);
//            if (num == null) {
//                num = 0;
//            }
//            sampLingRateNums.put(sampLingRate, ++num);
//        }
//
//        //If it is a file, do not convert, if the samplingRate of the file is consistent, do not convert
//        Set<Integer> sampLingRates = sampLingRateNums.keySet();
//        if (CollectionUtils.isEmpty(sampLingRates) || sampLingRates.size() == 1) {
//            return;
//        }
//
//        if (sampLingRates.size() != filePaths.size()) {//When the program is the same
//            //Get the samplingRate value with the most samplingRate
//            Integer maxSampLingRateNum = 0;
//            for (Integer integer : sampLingRateNums.keySet()) {
//                Integer sampLingRateNum = sampLingRateNums.get(integer);
//                if (sampLingRateNum == null) {
//                    continue;
//                }
//                if (sampLingRateNum > maxSampLingRateNum) {
//                    maxSampLingRateNum = sampLingRateNum;
//                    maxSampLingRate = integer;
//                }
//            }
//        }
//
//        //Circularly determine whether the file needs to be converted
//        for (int i = 0; i < filePaths.size(); i++) {
//            String file = filePaths.get(i);
//
//            ReadFileUtils readFileUtils = new ReadFileUtils(new File(file));
//            MultimediaInfo m = readFileUtils.m;
//            AudioInfo audioInfo = m.getAudio();
//            int bitRate = audioInfo.getBitRate();
//            int sampLingRate = audioInfo.getSamplingRate();
//
//            //Do not process when it is the most samplingRate
//            if (sampLingRate == maxSampLingRate) {
//                continue;
//            }
//
//            try {
//                String oupPutFile = testCompressMp3Samll(file, bitRate, maxSampLingRate);
//                //Delete the original data in the list and add new file data
//                filePaths.set(i, oupPutFile);
//            } catch (Exception e) {
//                throw new Exception("Failed to convert sample rate");
//            }
//        }
//    }
//
//    /**
//     * Conversion file sampling rate
//     *
//     * @param inputFilePath
//     * @param bitRate
//     * @param samplingRate
//     * @throws Exception
//     */
//    public static String testCompressMp3Samll(String inputFilePath, int bitRate, int samplingRate) throws Exception {
//        AudioAttributes audio = new AudioAttributes();
//        audio.setCodec("libmp3lame");
//        audio.setBitRate(bitRate);//Set the bit rate
//        audio.setChannels(new Integer(2));
//        audio.setSamplingRate(samplingRate);//Sampling Rate
//        EncodingAttributes attrs = new EncodingAttributes();
//        attrs.setFormat("mp3");//Set the format, my file was originally in mp3 format
//        attrs.setAudioAttributes(audio);
//        //attrs.setDuration(360f); // Set the duration of interception
//        Encoder encoder = new Encoder();
//        File inputFile = new File(inputFilePath);
//        String outPutFile = inputFile.getParent() + File.separator + "changeMp3_" + inputFile.getName();
//        encoder.encode(inputFile, new File(outPutFile), attrs);
//
//        return outPutFile;
//    }
//
//    public static void uniteAMRFile(String[] partsPaths, String unitedFilePath) {
//        try {
//            File unitedFile = new File(unitedFilePath);
//            FileOutputStream fos = new FileOutputStream(unitedFile);
//            RandomAccessFile ra = null;
//            for (int i = 0; i < partsPaths.length; i++) {
//                ra = new RandomAccessFile(partsPaths[i], "r");
//                if (i != 0) {
//                    ra.seek(6);
//                }
//                byte[] buffer = new byte[1024 * 8];
//                int len = 0;
//                while ((len = ra.read(buffer)) != -1) {
//                    fos.write(buffer, 0, len);
//                }
//            }
//            ra.close();
//            fos.close();
//        } catch (Exception e) {
//        }
//    }
//
//    public static void packageMp3(String fileOutPath, List<String> filePaths) throws Exception {
//        if (StringUtils.isEmpty(fileOutPath)) {
//            throw new Exception("The storage address of the mp3 program file is empty");
//        }
//
//        if (CollectionUtils.isEmpty(filePaths)) {
//            throw new Exception("The material file list is empty");
//        }
//
//        //changeMp3(filePaths);
//        FileOutputStream fos = new FileOutputStream(fileOutPath);
//
//        String[] fileList = new String[filePaths.size()];
//        // A collection of materials that need to be merged
//        ArrayList<FileInputStream> list = new ArrayList<>();
//        for (String string : filePaths) {
//            FileInputStream fistream1 = new FileInputStream(string);
//            list.add(fistream1);
//        }
//
//        filePaths.toArray(fileList);
//
//        //uniteAMRFile(fileList, fileOutPath);
//        Iterator<FileInputStream> it = list.iterator();
//        SequenceInputStream sis = new SequenceInputStream(new Enumeration<FileInputStream>() {
//
//            @Override
//            public boolean hasMoreElements() {
//                return it.hasNext();
//            }
//
//            @Override
//            public FileInputStream nextElement() {
//                return it.next();
//            }
//        });
//
//        byte buff[] = new byte[1024];
//        int length = 0;
//        while ((length = sis.read(buff)) != -1) {
//            fos.write(buff, 0, length);
//        }
//
//        fos.close();
//        sis.close();
//    }
//}
