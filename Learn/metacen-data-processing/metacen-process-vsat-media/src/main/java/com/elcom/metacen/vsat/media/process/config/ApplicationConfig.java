package com.elcom.metacen.vsat.media.process.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class ApplicationConfig {

    public static String ROOT_FOLDER_FILE_PATH_INTERNAL;

    public static String MEDIA_LINK_ROOT_API;

    @Autowired
    public ApplicationConfig(@Value("${rootFolderFilePathInternal}") String rootFolderFilePathInternal,
            @Value("${mediaLinkRootApi}") String mediaLinkRootApi) {
        ROOT_FOLDER_FILE_PATH_INTERNAL = rootFolderFilePathInternal;
        MEDIA_LINK_ROOT_API = mediaLinkRootApi;
    }
}
