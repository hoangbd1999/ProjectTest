/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.service;

import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.notify.model.Notify;
import com.elcom.metacen.notify.model.dto.NotifyDTO;
import com.elcom.metacen.notify.model.dto.NotifyRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Admin
 */
public interface NotifyService {

    void save(Notify notify);

    Iterable<Notify> saveAll(List<Notify> notifyList);

    boolean update(Notify notify);

    void remove(Notify notify);

    Page<Notify> findNotifyByUser(Integer page, Integer size, String keyword, String userId, long fromDate, long toDate, List<String> typeIcon,int type);

    boolean updateStatusView(String userId, int statusView);

    Notify updateStatusViewOne(String userId, String notiId, int statusView);

    long countNotifyNotRead(String userId, int statusView, long fromDate, long toDate, int type);

    NotifyDTO transform(Notify x);

    ResponseMessage sendToMobileApp(NotifyRequestDTO requestDTO);

    ResponseMessage notifyExportFile(NotifyRequestDTO requestDTO);
}
