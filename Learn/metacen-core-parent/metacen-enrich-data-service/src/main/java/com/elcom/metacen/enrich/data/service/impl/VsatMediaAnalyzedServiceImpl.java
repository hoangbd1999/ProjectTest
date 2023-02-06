package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedFilterDTO;
import com.elcom.metacen.enrich.data.repository.CustomVsatMediaAnalyzedRepository;
import com.elcom.metacen.enrich.data.repository.VsatMediaAnalyzedRepository;
import com.elcom.metacen.enrich.data.service.VsatMediaAnalyzedService;
import java.math.BigInteger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
@Service
public class VsatMediaAnalyzedServiceImpl implements VsatMediaAnalyzedService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaAnalyzedServiceImpl.class);

    @Autowired
    CustomVsatMediaAnalyzedRepository customVsatMediaAnalyzedRepository;

    @Autowired
    VsatMediaAnalyzedRepository vsatMediaAnalyzedRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<VsatMediaAnalyzedDTO> filterVsatMediaAnalyzed(VsatMediaAnalyzedFilterDTO vsatMediaAnalyzedFilterDTO) {
        return customVsatMediaAnalyzedRepository.filterVsatMediaAnalyzed(vsatMediaAnalyzedFilterDTO);
    }

    @Override
    public VsatMediaAnalyzedDTO getDetailVsatMediaAnalyzed(String uuid) {
      //  Optional<VsatMediaAnalyzed> vsatMediaAnalyzed = vsatMediaAnalyzedRepository.findById(uuid);
        Object data = customVsatMediaAnalyzedRepository.findByUuid(uuid);
        if (data != null) {
            return entityToDto(data);
        }
        return null;
    }

    private VsatMediaAnalyzedDTO entityToDto(Object dataVsatMedia) {
         VsatMediaAnalyzedDTO vsatMediaAnalyzed = modelMapper.map(dataVsatMedia, VsatMediaAnalyzedDTO.class);
        VsatMediaAnalyzedDTO vsatMediaAnalyzedDTO = VsatMediaAnalyzedDTO.builder()
                .id(vsatMediaAnalyzed.getId())
                .vsatMediaUuidKey(vsatMediaAnalyzed.getVsatMediaUuidKey())
                .mediaTypeId(Long.valueOf(vsatMediaAnalyzed.getMediaTypeId()))
                .mediaTypeName(vsatMediaAnalyzed.getMediaTypeName())
                .sourceId(BigInteger.valueOf(vsatMediaAnalyzed.getSourceId().longValue()))
                .sourceName(vsatMediaAnalyzed.getSourceName())
                .sourceIp(vsatMediaAnalyzed.getSourceIp())
                .sourcePort(Long.valueOf(vsatMediaAnalyzed.getSourcePort()))
                .destId(BigInteger.valueOf(vsatMediaAnalyzed.getDestId().longValue()))
                .destName(vsatMediaAnalyzed.getDestName())
                .destIp(vsatMediaAnalyzed.getDestIp())
                .destPort(Long.valueOf(vsatMediaAnalyzed.getDestPort()))
                .filePathLocal(vsatMediaAnalyzed.getFilePath())
                .filePath(vsatMediaAnalyzed.getFilePath().replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API))
                .fileType(vsatMediaAnalyzed.getFileType())
                .fileSize(BigInteger.valueOf(vsatMediaAnalyzed.getFileSize().longValue()))
                .fileContentUtf8(vsatMediaAnalyzed.getFileContentUtf8())
                .fileContentGB18030(vsatMediaAnalyzed.getFileContentGB18030())
                .mailFrom(vsatMediaAnalyzed.getMailFrom())
                .mailReplyTo(vsatMediaAnalyzed.getMailReplyTo())
                .mailTo(vsatMediaAnalyzed.getMailTo())
                .mailAttachments(vsatMediaAnalyzed.getMailAttachments())
                .mailContents(vsatMediaAnalyzed.getMailContents())
                .mailSubject(vsatMediaAnalyzed.getMailSubject())
                .mailScanVirus(vsatMediaAnalyzed.getMailScanVirus())
                .mailScanResult(vsatMediaAnalyzed.getMailScanResult())
                .mailUserAgent(vsatMediaAnalyzed.getMailUserAgent())
                .mailContentLanguage(vsatMediaAnalyzed.getMailContentLanguage())
                .mailXMail(vsatMediaAnalyzed.getMailXMail())
                .mailRaw(vsatMediaAnalyzed.getMailRaw())
                .dataSourceId(vsatMediaAnalyzed.getDataSourceId())
                .dataSourceName(vsatMediaAnalyzed.getDataSourceName())
                .direction(vsatMediaAnalyzed.getDirection())
                .dataVendor(vsatMediaAnalyzed.getDataVendor())
                .analyzedEngine(vsatMediaAnalyzed.getAnalyzedEngine())
                .eventTime(vsatMediaAnalyzed.getEventTime())
                .ingestTime(vsatMediaAnalyzed.getIngestTime())
                .processTime(vsatMediaAnalyzed.getProcessTime())
                .processStatus(vsatMediaAnalyzed.getProcessStatus())
                .build();

        return vsatMediaAnalyzedDTO;
    }
}
