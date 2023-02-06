package com.elcom.metacen.raw.data.config;

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

    public static String ROOT_FOLDER_FILE_MERGE_AUDIO;

    public static String ROOT_FOLDER_EMAIL_FILE_ATTACHMENTS;

    public static int MAX_RECORDS_AIS_RETURN_NOT_UNIQUE;

    public static String SATELLITE_ROOT_FOLDER_INTERNAL;

    public static String SATELLITE_MEDIA_LINK_ROOT_API;

    @Autowired
    public ApplicationConfig(@Value("${rootFolderFilePathInternal}") String rootFolderFilePathInternal,
            @Value("${mediaLinkRootApi}") String mediaLinkRootApi,
            @Value("${rootFolderFileMergeAudio}") String rootFolderFileMergeAudio,
            @Value("${rootFolderEmailFileAttachments}") String rootFolderEmailFileAttachments,
            @Value("${ais.max.records.return.not.unique}") int aisMaxRecordsReturnNotUnique,
            @Value("${satelliteRootFolderInternal}") String satelliteRootFolderInternal,
            @Value("${satelliteMediaLinkRootApi}") String satelliteMediaLinkRootApi) {
        ROOT_FOLDER_FILE_PATH_INTERNAL = rootFolderFilePathInternal;
        MEDIA_LINK_ROOT_API = mediaLinkRootApi;
        ROOT_FOLDER_FILE_MERGE_AUDIO = rootFolderFileMergeAudio;
        ROOT_FOLDER_EMAIL_FILE_ATTACHMENTS = rootFolderEmailFileAttachments;
        MAX_RECORDS_AIS_RETURN_NOT_UNIQUE = aisMaxRecordsReturnNotUnique;
        SATELLITE_ROOT_FOLDER_INTERNAL = satelliteRootFolderInternal;
        SATELLITE_MEDIA_LINK_ROOT_API = satelliteMediaLinkRootApi;
    }
}
