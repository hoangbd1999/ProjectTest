package com.elcom.metacen.uploadservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class PropertiesConfig {

    @Value("${rootFolderFilePathInternal}")
    public static String ROOT_FOLDER_FILE_PATH_INTERNAL;

    @Value("${mediaLinkRootApi}")
    public static String MEDIA_LINK_ROOT_API;

    @Value("${satelliteRootFolderInternal}")
    public static String SATELLITE_ROOT_FOLDER_INTERNAL;

    @Value("${satelliteMediaLinkRootApi}")
    public static String SATELLITE_MEDIA_LINK_ROOT_API;

    @Autowired
    public PropertiesConfig(
            @Value("${rootFolderFilePathInternal}") String rootFolderFilePathInternal,
            @Value("${mediaLinkRootApi}") String mediaLinkRootApi,
            @Value("${satelliteRootFolderInternal}") String satelliteRootFolderInternal,
            @Value("${satelliteMediaLinkRootApi}") String satelliteMediaLinkRootApi) {

        ROOT_FOLDER_FILE_PATH_INTERNAL = rootFolderFilePathInternal;
        MEDIA_LINK_ROOT_API = mediaLinkRootApi;
        SATELLITE_ROOT_FOLDER_INTERNAL = satelliteRootFolderInternal;
        SATELLITE_MEDIA_LINK_ROOT_API = satelliteMediaLinkRootApi;
    }
}
