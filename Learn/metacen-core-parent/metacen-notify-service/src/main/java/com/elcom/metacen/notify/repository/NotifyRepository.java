/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.repository;

import com.elcom.metacen.notify.model.Notify;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Repository
public interface NotifyRepository extends CrudRepository<Notify, String> {

    Page<Notify> findByUserIdAndTypeAndTimeSendNotifyBetween(String userId, int type, long fromDate, long toDate, Pageable pageable);

    Notify findByNotifyPK_IdAndUserId(String notiId, String userId);

    @Query("SELECT n FROM Notify n WHERE n.userId = :userId and  n.type = :type and ( UPPER(n.title) LIKE %:term% or UPPER(n.content) LIKE %:term% ) and ( n.timeSendNotify between :fromDate and :toDate )")
    Page<Notify> searchNotify(@Param("userId") String userId, @Param("type") int type, @Param("term") String term, @Param("fromDate") long fromDate, @Param("toDate") long toDate, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notify n SET n.statusView = :statusView WHERE n.userId = :userId")
    int updateStatusView(@Param("userId") String userId, @Param("statusView") int statusView);

    long countByUserIdAndTypeAndStatusViewAndTimeSendNotifyBetween(String userId, int type, int statusView, long fromDate, long toDate);

    @Query("SELECT n FROM Notify n WHERE n.userId = :userId and  n.type = :type and n.icon IN :icon and ( n.timeSendNotify between :fromDate and :toDate )")
    Page<Notify> findByUserIdAndIconAndTimeSendNotifyBetween(@Param("userId") String userId,@Param("type") int type, @Param("icon") List<String> icon, @Param("fromDate") long fromDate, @Param("toDate") long toDate, Pageable pageable);

    @Query("SELECT n FROM Notify n WHERE n.userId = :userId and  n.type = :type and n.icon IN :icon and ( UPPER(n.title) LIKE %:term% or UPPER(n.content) LIKE %:term% ) and ( n.timeSendNotify between :fromDate and :toDate )")
    Page<Notify> searchNotifybyKeywordAndYypeIcon(@Param("userId") String userId,@Param("type") int type, @Param("term") String term, @Param("icon") List<String> icon, @Param("fromDate") long fromDate, @Param("toDate") long toDate, Pageable pageable);

}
