package com.elcom.metacen.id.repository;

import com.elcom.metacen.id.model.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends PagingAndSortingRepository<Unit, String> {
    Page<Unit> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCase(String code,String name,String address,String phone,String email, Pageable paging);
    Unit findByEmail(String email);
    Unit findByPhone(String phone);
    Unit findByCode(String code);
    Unit findByName(String name);
    Unit findByUuid(String uuid);
    List<Unit> findByUuidIn(List<String> uuids);
    
    List<Unit> findByLisOfStageContainingIgnoreCase(String stageId);
}
