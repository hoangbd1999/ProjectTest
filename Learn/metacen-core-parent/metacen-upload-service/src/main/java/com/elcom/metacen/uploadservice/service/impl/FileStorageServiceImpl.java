package com.elcom.metacen.uploadservice.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.elcom.metacen.uploadservice.exception.FileStorageException;
import com.elcom.metacen.uploadservice.exception.FileNotFoundException;
import com.elcom.metacen.uploadservice.exception.ValidationException;
import com.elcom.metacen.uploadservice.service.FileStorageService;
import com.elcom.metacen.uploadservice.upload.FileStorageConfig;
import com.elcom.metacen.uploadservice.utils.DateUtil;
import com.elcom.metacen.uploadservice.utils.MimeTypes;
import com.elcom.metacen.uploadservice.utils.StringUtil;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Autowired
    public FileStorageServiceImpl(FileStorageConfig fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Không tạo được thư mục chứa file upload.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtil.generateRandomString(6) + "-" + StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new ValidationException("Tên file không hợp lệ [" + fileName + "]");
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Không tạo được file [" + fileName + "]. Hãy thử lại sau!", ex);
        }
    }

    public String storeFile(MultipartFile file, String uploadDir, Boolean keepFileName) {
        if (uploadDir != null && uploadDir.contains("{ddmmyyyy}")) {
            String ddmmyyyy = DateUtil.today("ddMMyyyy");
            createUploadFolder(uploadDir.replace("{ddmmyyyy}", ""), ddmmyyyy);
            uploadDir = uploadDir.replace("{ddmmyyyy}", ddmmyyyy);
        }
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            fileName = fileName.replace("%", "");
        }
        // Normalize file name
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (keepFileName == null || !keepFileName) {
            //String fileName = StringUtil.generateRandomString(6) + "-" + StringUtils.cleanPath(file.getOriginalFilename());
            //Upload base64 khong co extension
            if (extension == null || extension.equals("null")) {
                extension = MimeTypes.getDefaultExt(file.getContentType());
                LOGGER.info("Extension null => Create from content type: {} => {}", file.getContentType(), extension);
            }
            //
            fileName = UUID.randomUUID().toString() + "." + extension;
        }
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new ValidationException("Tên file không hợp lệ [" + fileName + "]");
            }

            // Check image rotate
            if (isImageFile(fileName)) {
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
                ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

                int orientation = 1;
                if (exifIFD0Directory != null) {
                    LOGGER.info("check ExifIFD0Directory orientation ...");
                    try {
                        orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    LOGGER.info("orientation: {}", orientation);
                } else {
                    LOGGER.info("Cannot get orientation of image file");
                }
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                AffineTransform affineTransform = new AffineTransform();

                switch (orientation) {
                    case 1:
                        break;
                    case 2: // Flip X
                        affineTransform.scale(-1.0, 1.0);
                        affineTransform.translate(-width, 0);
                        break;
                    case 3: // PI rotation
                        affineTransform.translate(width, height);
                        affineTransform.rotate(Math.PI);
                        break;
                    case 4: // Flip Y
                        affineTransform.scale(1.0, -1.0);
                        affineTransform.translate(0, -height);
                        break;
                    case 5: // - PI/2 and Flip X
                        affineTransform.rotate(-Math.PI / 2);
                        affineTransform.scale(-1.0, 1.0);
                        break;
                    case 6: // -PI/2 and -width
                        affineTransform.translate(height, 0);
                        affineTransform.rotate(Math.PI / 2);
                        break;
                    case 7: // PI/2 and Flip
                        affineTransform.scale(-1.0, 1.0);
                        affineTransform.translate(-height, 0);
                        affineTransform.translate(0, width);
                        affineTransform.rotate(3 * Math.PI / 2);
                        break;
                    case 8: // PI / 2
                        affineTransform.translate(0, width);
                        affineTransform.rotate(3 * Math.PI / 2);
                        break;
                    default:
                        break;
                }

                if (orientation != 1) {
                    //Bi rotate => quay lai
                    AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC);
                    BufferedImage destinationImage = new BufferedImage(originalImage.getHeight(), originalImage.getWidth(), originalImage.getType());
                    destinationImage = affineTransformOp.filter(originalImage, destinationImage);
                    Path targetLocation = Paths.get(System.getProperty("user.dir") + uploadDir + File.separator + fileName);
                    ImageIO.write(destinationImage, extension, targetLocation.toFile());
                } else {
                    // Copy file to the target location (Replacing existing file with the same name)
                    Path targetLocation = Paths.get(System.getProperty("user.dir") + uploadDir + File.separator + fileName);
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                LOGGER.info("No image file ==> Upload normal ...");
                // Copy file to the target location (Replacing existing file with the same name)
                Path targetLocation = Paths.get(System.getProperty("user.dir") + uploadDir + File.separator + fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Không tạo được file [" + fileName + "]. Hãy thử lại sau!", ex);
        } catch (ImageProcessingException ex) {
            throw new FileStorageException("Lỗi kiểm tra image rotate. Hãy thử lại sau!", ex);
        }
    }

    public void createUploadFolder(String uploadDir, String ddmmyyyy) {
        //Create upload dir if not exist
        File checkUploadDir = new File(System.getProperty("user.dir") + uploadDir + ddmmyyyy);
        if (!checkUploadDir.exists()) {
            System.out.println("Tao folder upload: " + uploadDir + ddmmyyyy + " => " + checkUploadDir.mkdir());
        } else {
            System.out.println("Folder upload " + uploadDir + File.separator + ddmmyyyy + " da ton tai");
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(URLDecoder.decode(fileName, "UTF-8")).normalize();
            //Path filePath = Paths.get(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Không tìm thấy file " + fileName);
            }
        } catch (Exception ex) {
            throw new FileNotFoundException("Không tìm thấy file " + fileName, ex);
        }
    }

    public boolean isImageFile(String str) {
        // Regex to check valid image file extension.
        String regex = "([^\\s]+(\\.(?i)(jpe?g|png|gif|bmp))$)";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (str == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given string
        // and regular expression.
        Matcher m = p.matcher(str);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }
}
