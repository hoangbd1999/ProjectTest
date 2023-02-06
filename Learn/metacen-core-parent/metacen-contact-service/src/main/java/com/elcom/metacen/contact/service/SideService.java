/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Side;
import com.elcom.metacen.contact.model.dto.SideDTO;
import com.elcom.metacen.contact.model.dto.SideFilterDTO;
import org.springframework.data.domain.Page;


/**
 * @author hoangbd
 */
public interface SideService {

    Side save(SideDTO sideDTO);

    Side findById(String uuidKey);

    Side updateSide(Side side, SideDTO sideDTO);

    Page<Side> findListSide(SideFilterDTO sideFilterDTO);

    Side delete(Side side);

}
