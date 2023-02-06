package com.elcom.metacen.id.repository;

import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String>, JpaSpecificationExecutor<User> {

    List<User> findByUuidIn(List<String> uuidList);

    Page<User> findByStatus(Integer status, Pageable pageable);

    List<User> findByStatus(Integer status);

    List<User> findByUnit(Unit unit);

    @Override
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :password, u.setPassword = :setPassword, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changePassword(@Param("password") String password, @Param("setPassword") Integer setPassword, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.email = :email, u.emailVerify = :emailVerify, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changeEmail(@Param("email") String email, @Param("emailVerify") Integer emailVerify, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.mobile = :mobile, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changeMobile(@Param("mobile") String mobile, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = :status, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changeStatus(@Param("status") Integer status, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.otpMobile = :otpMobile, u.mobileVerify = :mobileVerify, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changeOtpMobile(@Param("otpMobile") String otpMobile, @Param("mobileVerify") Integer mobileVerify, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.otpMobile = :otpMobile, u.mobileVerify = :mobileVerify, u.otpTime = :otpTime, "
            + "u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int changeOtpMobile(@Param("otpMobile") String otpMobile, @Param("mobileVerify") Integer mobileVerify,
            @Param("otpTime") Timestamp otpTime, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.otp = :otp, u.otpTime = :otpTime, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int createOTP(@Param("otp") String otp, @Param("otpTime") Timestamp otpTime, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.mobileVerify = :mobileVerify, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int updateMobileVerify(@Param("mobileVerify") Integer mobileVerify, @Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.otpPassword = :otpPassword, u.otpPasswordTime = :otpPasswordTime, u.lastUpdate = now() WHERE u.uuid = :uuid ")
    int createOTPPassword(@Param("otpPassword") String otpPassword, @Param("otpPasswordTime") Timestamp otpPasswordTime, @Param("uuid") String uuid);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("Delete from User u where u.uuid in ?1")
    void deleteUsersWithIds(List<String> ids);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.isActive = 1 WHERE u.uuid = :uuid")
    int updateActive(@Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.isActive = 0 WHERE u.uuid = :uuid")
    int updateInActive(@Param("uuid") String uuid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.isDelete = 1 WHERE u.uuid = :uuid")
    int deleteByUuid(@Param("uuid") String uuid);

    @Override
    List<User> findAll();

    User findByUuidAndIsDelete(String id, int isDeleted);

    @Query(value = "SELECT u from User u WHERE u.unit.uuid in :units")
    List<User> findByUnits(List<String> units);
}
