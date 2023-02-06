package com.elcom.metacen.uploadservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author anhdv
 */
public interface FileStorageService {

    Resource loadFileAsResource(String fileName);

    String storeFile(MultipartFile file);
}
