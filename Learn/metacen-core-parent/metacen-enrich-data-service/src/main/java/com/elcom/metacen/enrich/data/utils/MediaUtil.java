package com.elcom.metacen.enrich.data.utils;

import com.elcom.metacen.enums.MediaStreamFileType;
import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaUtil.class);

    /**
     * Tạo file .m3u8 từ file .ts
     *
     * @param tsFileInput (đường dẫn file .ts)
     * @return m3u8FullPath: đường dẫn truy cập file .m3u8
     */
    public static String generateM3u8ByTs(String tsFileInput) {
        String m3u8FullPath = "";
        BufferedWriter w;
        try {
            LOGGER.info("==> tsFileInput: " + tsFileInput);
            long startTime = System.currentTimeMillis();
            File tsFileInputLocal = new File(tsFileInput);
            String urlFileNameTs = tsFileInputLocal.getParentFile() + File.separator + tsFileInputLocal.getName();
            String m3u8LocalFile = tsFileInputLocal.getParentFile() + File.separator + tsFileInputLocal.getName().replace(MediaStreamFileType.TS.toVal(), MediaStreamFileType.M3U8.toVal());
            LOGGER.info("==> m3u8LocalFile will be generate: " + m3u8LocalFile);

            File file = new File(m3u8LocalFile);
            if (!file.exists()) {
                w = new BufferedWriter(new FileWriter(file));
                w.write("#EXTM3U");
                w.newLine();
                w.write("#EXT-X-VERSION:3");
                w.newLine();
                w.write("#EXT-X-PLAYLIST-TYPE:VOD");
                w.newLine();
                w.write("#EXT-X-TARGETDURATION:13");
                w.newLine();
                w.write("#EXT-X-MEDIA-SEQUENCE:0");
                w.newLine();
                w.write("#EXTINF:9.000000");
                w.newLine();
                String tsLocalFilePath = ApplicationConfig.MEDIA_LINK_ROOT_API + urlFileNameTs.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, "");
                w.write(tsLocalFilePath);
                m3u8FullPath = tsLocalFilePath.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, "").replace(MediaStreamFileType.TS.toVal(), MediaStreamFileType.M3U8.toVal());
                LOGGER.info("==> m3u8FullPath return: {}, elapsed time: {}", m3u8FullPath, getElapsedTime(System.currentTimeMillis() - startTime));

                w.newLine();
                w.flush();
                w.close();
            } else {
                m3u8FullPath = ApplicationConfig.MEDIA_LINK_ROOT_API + m3u8LocalFile.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, "");
                LOGGER.info("==> m3u8FullPath return (existed): {}", m3u8FullPath);
            }
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return m3u8FullPath;
    }

    public static String getFileExtension(String path_local) {
        File file = new File(path_local);
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static void buildStreamFile(HttpServletRequest request, HttpServletResponse response,
             String filePathM3u8) throws UnsupportedEncodingException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        try {
            File fileLocal = new File(filePathM3u8);
            LOGGER.info("fileLocal.name: " + fileLocal.getName());
            OutputStream outStream;
            try ( FileInputStream inStream = new FileInputStream(fileLocal)) {
                response.setContentType("application/octet-stream");
                response.setContentLength((int) fileLocal.length());
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"", fileLocal.getName());
                response.setHeader(headerKey, headerValue);
                outStream = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
            outStream.close();
        } catch (Exception ex) {
            try ( PrintWriter out = response.getWriter()) {
                out.print("File không tồn tại!");
                out.flush();
            }
            LOGGER.error("Loi stream file : " + ex);
        }
    }

    /**
     * Convert file sang dạng mong muốn ( vd: .h264 => .mp4 )
     *
     * @param filePathOrigin (đường dẫn file gốc)
     * @param targetExtension (định dạng muốn convert sang)
     * @return newFilePath
     */
    public static String convertAndFetchVideo(String filePathOrigin, String targetExtension) {
        String newFilePath = null;
        try {
            LOGGER.info("==> filePathOrigin: [{}], targetExtension: [{}] ", filePathOrigin, targetExtension);
            Path fileToPath = Paths.get(filePathOrigin);
            if (!Files.exists(fileToPath)) {
                LOGGER.error("<== filePathOrigin [{}] not existed, return!", filePathOrigin);
                return null;
            }

            String fileName = fileToPath.getFileName().toString();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            if (filePathOrigin.contains("/")) {
                String[] arr = filePathOrigin.split("/");
                if (arr != null && arr.length > 3) {
                    fileName = arr[arr.length - 4] + "-" + arr[arr.length - 3]
                            + "-" + arr[arr.length - 2] + "-" + fileName;
                }
            }

            newFilePath = ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL + "/tmp-play/" + fileName + "." + targetExtension;
            if (Files.exists(Paths.get(newFilePath))) {
                LOGGER.info("<== File to convert [{}] existed, return!", newFilePath);
                return ApplicationConfig.MEDIA_LINK_ROOT_API
                        + newFilePath.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, "");
            }

            LOGGER.info("==> New file will be generate: [{}]", newFilePath);

            long startTime = System.currentTimeMillis();
            int exitCode = 1;
            try {
                String[] cmd = new String[]{"ffmpeg", "-y", "-i", filePathOrigin, "-c", "copy", newFilePath};
                LOGGER.info("cmd: [{}]", Arrays.toString(cmd));
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                Process process = processBuilder.start();
                exitCode = process.waitFor(50, TimeUnit.SECONDS) ? 0 : 1;

                /*ShellCommand sc = new ShellCommand();
                String[] cmd = new String[]{"ffmpeg", "-i", filePathOrigin, "-c", "copy", newFilePath, "-y"};
                LOGGER.info("cmd: [{}]", Arrays.toString(cmd));
                CommandResult cr = sc.runWaitFor(cmd, 20, TimeUnit.SECONDS);
                LOGGER.info("===> coverting return [" + cr + "]");
                if (cr.isSuccess()) {
                    //sc.runWaitFor(String.format("chmod 644 %s", newFilePath), 2, TimeUnit.SECONDS);
                    exitCode = 0;
                }
                //else
                    //sc.runWaitFor(new String[]{ "rm", "-f", newFilePath }, 2, TimeUnit.SECONDS);*/
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
            }

            LOGGER.info("convertAndFetchVideo() process finished, elapsed time: "
                    + getElapsedTime(System.currentTimeMillis() - startTime) + ", exitCode: " + exitCode);

            boolean isFileOutExisted = Files.exists(Paths.get(newFilePath));
            boolean isFileOutReadable = Files.isReadable(Paths.get(newFilePath));
            if (exitCode == 0 && isFileOutExisted && isFileOutReadable) {
                LOGGER.info("<== convertAndFetchVideo() SUCCESS, return file: [{}]", newFilePath);
                newFilePath = ApplicationConfig.MEDIA_LINK_ROOT_API
                        + newFilePath.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, "");
            } else {
                LOGGER.error("<== convertAndFetchVideo() FAILED, isFileOutExisted: {}, isFileOutReadable: {}",
                         isFileOutExisted, isFileOutReadable);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return newFilePath;
    }

    private static String getElapsedTime(long miliseconds) {
        return miliseconds + " (ms)";
    }
}
