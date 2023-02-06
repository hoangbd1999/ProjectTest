package com.elcom.metacen.id.service;

import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.UserPagingDTO;

import java.util.List;
//import java.util.Optional;

/**
 *
 * @author anhdv
 */
public interface UserService {

    UserPagingDTO findAll(String keyword, Integer status, Integer currentPage, Integer rowsPerPage, 
            String sort, Integer signupType, Integer mobileVerify, List<String> adminUuidList, 
            String startDate, String endDate);

    User findByUuid(String uuid);

    int updateActive(String uuid);

    int updateInActive(String uuid);

    List<User> findByGroup(String groupId);
    
    User findByEmail(String email);
    
    User findByMobile(String mobile);
    
    User findByUserName(String userName);
    
    User findBySocial(Integer signupType, String socialId);
    
    User findByEmailOrMobile(String userInfo);
    
    User findByEmailOrMobileOrUserName(String userInfo);
    
    List<User> findByUuidIn(List<String> uuidList);
    
    List<User> findByStatus(Integer status);
    
    void save(User user);
    
    boolean update(User user);
    
    boolean changePassword(User user);
    
    boolean changeEmail(User user);

    boolean changeStatus(User user);
    
    boolean changeOtpMobile(User user);

    void remove(User user);

    int deleteByUuid(User user);
    
    void remove(List<String> uuidList);
    
    boolean insertTest();
    
    boolean updateLastLogin(String uuid, String loginIp);
    
    boolean connectSocial(User user, Integer socialType, String socialId);
    
    boolean createOTP(User user, String otp);
    
    boolean updateMobileVerify(User user, int mobileVerify);
    
    boolean createOTPPassword(User user, String otpPassword);

    List<User> findAll();

    List<User> getUserBySiteId(String siteId);

    List<User> getUserByStage(String stageCode);
    List<User> getUserByUnits(List<String> units);


}