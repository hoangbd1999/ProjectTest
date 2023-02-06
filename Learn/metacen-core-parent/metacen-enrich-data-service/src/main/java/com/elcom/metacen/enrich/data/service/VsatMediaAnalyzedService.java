/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedFilterDTO;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface VsatMediaAnalyzedService {

    Page<VsatMediaAnalyzedDTO> filterVsatMediaAnalyzed(VsatMediaAnalyzedFilterDTO vsatMediaAnalyzedFilterDTO);

    VsatMediaAnalyzedDTO getDetailVsatMediaAnalyzed(String uuid);
}
