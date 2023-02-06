/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.service.impl;

import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.model.VsatMediaRelation;
import com.elcom.metacen.raw.data.model.dto.ConvertAndFetchVideoRequestDTO;
import com.elcom.metacen.raw.data.model.dto.DetailMediaRelationRequestDTO;
import com.elcom.metacen.raw.data.model.dto.MailContentDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.raw.data.model.dto.VsatMediaDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticResponseDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaRelationFilterDTO;
import com.elcom.metacen.raw.data.repository.VsatMediaDataRelationRepository;
import com.elcom.metacen.raw.data.repository.CustomVsatMediaOverallRepository;
import com.elcom.metacen.raw.data.repository.VsatMediaDataRepository;
import com.elcom.metacen.raw.data.service.VsatMediaDataService;
import com.elcom.metacen.raw.data.utils.MailUtil;
import com.elcom.metacen.raw.data.utils.MediaUtil;
import com.elcom.metacen.utils.StringUtil;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
@Service
public class VsatMediaDataServiceImpl implements VsatMediaDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaDataServiceImpl.class);

    @Autowired
    VsatMediaDataRepository vsatMediaDataRepository;

    @Autowired
    VsatMediaDataRelationRepository vsatMediaDataRelationRepository;

    @Autowired
    CustomVsatMediaOverallRepository customVsatMediaOverallRepository;

    @Override
    public Page<VsatMediaDTO> filterVsatMediaRawData(VsatMediaFilterDTO vsatMediaFilterDTO) {
        return vsatMediaDataRepository.filterVsatMediaRawData(vsatMediaFilterDTO);
    }

    @Override
    public Page<VsatMediaOverallDTO> filterVsatMediaDataOverall(VsatMediaOverallFilterDTO vsatMediaFilterDTO) {
        if (StringUtil.isNullOrEmpty(vsatMediaFilterDTO.getTerm())) {
            // filter on ClickHouse
            return vsatMediaDataRepository.filterVsatMediaDataOverall(vsatMediaFilterDTO);
        } else {
            // filter on Elastic Search
            return customVsatMediaOverallRepository.filterVsatMediaOverall(vsatMediaFilterDTO);
        }
    }

    @Override
    public VsatMediaOverallStatisticResponseDTO vsatMediaOverallStatistic(VsatMediaOverallStatisticFilterDTO vsatMediaOverallStatisticFilterDTO) {
        return vsatMediaDataRepository.vsatMediaOverallStatistic(vsatMediaOverallStatisticFilterDTO);
    }

    @Override
    public MailContentDTO fetchMailInfo(String emlFilePath) {
        return MailUtil.emlToEntity(emlFilePath);
    }

    @Override
    public String getM3U8File(String filePathLocal) {
        return MediaUtil.generateM3u8ByTs(filePathLocal);
    }

    @Override
    public String convertAndFetchVideo(ConvertAndFetchVideoRequestDTO data) {
        return MediaUtil.convertAndFetchVideo(data.getFilePath(), data.getTargetExtension());
    }

    @Override
    public Page<VsatMediaRelation> filterVsatMediaRelationRawData(VsatMediaRelationFilterDTO vsatMediaRelationFilterDTO) {
        return vsatMediaDataRelationRepository.filterVsatMediaRelationRawData(vsatMediaRelationFilterDTO);
    }

    @Override
    public MessageContent getDetailMediaRelation(DetailMediaRelationRequestDTO data) {
        return vsatMediaDataRepository.getDetailMediaRelation(data);
    }

}
