/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.service;

import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.model.VsatMediaRelation;
import com.elcom.metacen.raw.data.model.dto.ConvertAndFetchVideoRequestDTO;
import com.elcom.metacen.raw.data.model.dto.DetailMediaRelationRequestDTO;
import com.elcom.metacen.raw.data.model.dto.MailContentDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticResponseDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaRelationFilterDTO;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface VsatMediaDataService {

    Page<VsatMediaDTO> filterVsatMediaRawData(VsatMediaFilterDTO vsatMediaFilterDTO);

    Page<VsatMediaOverallDTO> filterVsatMediaDataOverall(VsatMediaOverallFilterDTO vsatMediaFilterDTO);

    VsatMediaOverallStatisticResponseDTO vsatMediaOverallStatistic(VsatMediaOverallStatisticFilterDTO vsatMediaOverallStatisticFilterDTO);

    MailContentDTO fetchMailInfo(String emlFilePath);

    String getM3U8File(String filePathLocal);

    String convertAndFetchVideo(ConvertAndFetchVideoRequestDTO data);

    Page<VsatMediaRelation> filterVsatMediaRelationRawData(VsatMediaRelationFilterDTO vsatMediaRelationFilterDTO);

    MessageContent getDetailMediaRelation(DetailMediaRelationRequestDTO data);
}
