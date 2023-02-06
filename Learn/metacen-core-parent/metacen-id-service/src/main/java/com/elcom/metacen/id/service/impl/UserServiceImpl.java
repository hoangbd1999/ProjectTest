package com.elcom.metacen.id.service.impl;

import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.UserSpecs;
import com.elcom.metacen.id.model.dto.UserPagingDTO;
import com.elcom.metacen.id.repository.UserCustomizeRepository;
import com.elcom.metacen.id.repository.UserRepository;
import com.elcom.metacen.id.service.UnitService;
import com.elcom.metacen.id.service.UserService;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Admin
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    //private static final String URI = ApplicationConfig.ITS_ROOT_URL + "/v1.0/its/management/stage/site/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UnitService unitService;

    @Autowired
    private UserCustomizeRepository userCustomizeRepository;

    public long countAll() {
        return userRepository.count();
    }

    @Override
    public UserPagingDTO findAll(String keyword, Integer status, Integer currentPage,
            Integer rowsPerPage, String sort, Integer signupType, Integer mobileVerify,
            List<String> adminUuidList, String startDate, String endDate) {
        // Old before use UserCustomizeRepository (Lay het user)
        UserPagingDTO result = new UserPagingDTO();
        try {
            if (currentPage > 0) {
                currentPage--;
            }
            if (StringUtil.isNullOrEmpty(sort)) {
                sort = "createdAt";
            }
            Pageable paging = PageRequest.of(currentPage, rowsPerPage, Sort.by(sort).ascending());
            Page<User> pagedResult = null;
            LOGGER.info("keyword: {}, status: {}", keyword, status);
            if (StringUtil.isNullOrEmpty(keyword) && status == null) {
                pagedResult = userRepository.findAll(paging);
            } else if (StringUtil.isNullOrEmpty(keyword) && status != null) {
                pagedResult = userRepository.findByStatus(status, paging);
            } else if (!StringUtil.isNullOrEmpty(keyword) && status == null) {
                pagedResult = userRepository.findAll(Specification.where(UserSpecs.searchByKeyword(keyword)), paging);
            } else {
                pagedResult = userRepository.findAll(Specification.where(UserSpecs.searchByStatusAndKeyword(status, keyword)), paging);
            }
            if (pagedResult != null && pagedResult.hasContent()) {
                result.setDataRows(pagedResult.getContent());
                result.setTotalRows(pagedResult.getTotalElements());
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return result;
    }

    @Override
    public User findByUuid(String uuid) {
        //return userCustomizeRepository.findByUuid(uuid);
        return userRepository.findByUuidAndIsDelete(uuid,0);
    }

    @Override
    public int updateInActive(String uuid) {
        return userRepository.updateInActive(uuid);
    }

    @Override
    public int updateActive(String uuid) {
        return userRepository.updateActive(uuid);
    }

    @Override
    public List<User> findByGroup(String groupId) {
        Unit unit = unitService.findByUuid(groupId);
        return userRepository.findByUnit(unit);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void remove(User user) {
        userRepository.delete(user);
    }

    @Override
    public int deleteByUuid(User user) {
        return userRepository.deleteByUuid(user.getUuid());
    }

    @Override
    public boolean insertTest() {
        return userCustomizeRepository.insertTest();
    }

    @Override
    public boolean updateLastLogin(String uuid, String loginIp) {
        return userCustomizeRepository.updateLastLogin(uuid, loginIp);
    }

    @Override
    public boolean update(User user) {
        return userCustomizeRepository.updateUser(user);
    }

    @Override
    public User findByEmail(String email) {
        try {
            return userCustomizeRepository.findByEmail(email);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public User findByMobile(String mobile) {
        return userCustomizeRepository.findByMobile(mobile);
    }

    @Override
    public User findByUserName(String userName) {
        return userCustomizeRepository.findByUserName(userName);
    }

    @Override
    public User findBySocial(Integer signupType, String socialId) {
        try {
            return userCustomizeRepository.findBySocial(signupType, socialId);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmailOrMobile(String userInfo) {
        try {
            return userCustomizeRepository.findByEmailOrMobile(userInfo);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmailOrMobileOrUserName(String userInfo) {
        try {
            return userCustomizeRepository.findByEmailOrMobileOrUserName(userInfo);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean connectSocial(User user, Integer socialType, String socialId) {
        return userCustomizeRepository.connectSocial(user, socialType, socialId);
    }

    @Override
    public List<User> findByUuidIn(List<String> uuidList) {
        List<User> result = null;
        try {
            result = userRepository.findByUuidIn(uuidList);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("findByUuidIn >>> Error to get list user by uuids: {}", ex.toString());
        }
        return result;
    }

    @Override
    public boolean changePassword(User user) {
        return userRepository.changePassword(user.getPassword(), user.getSetPassword(), user.getUuid()) > 0;
    }

    @Override
    public boolean changeEmail(User user) {
        return userRepository.changeEmail(user.getEmail(), user.getEmailVerify(), user.getUuid()) > 0;
    }

    @Override
    public boolean changeStatus(User user) {
        return userRepository.changeStatus(user.getStatus(), user.getUuid()) > 0;
    }

    @Override
    public boolean changeOtpMobile(User user) {
        if (user.getOtpTime() != null) {
            return userRepository.changeOtpMobile(user.getOtpMobile(), user.getMobileVerify(), user.getUuid()) > 0;
        } else {
            return userRepository.changeOtpMobile(user.getOtpMobile(), user.getMobileVerify(),
                    new Timestamp(System.currentTimeMillis() + Constant.OTP_TIME_EXIPRED),
                    user.getUuid()) > 0;
        }
    }

    @Override
    public List<User> findByStatus(Integer status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public boolean createOTP(User user, String otp) {
        return userRepository.createOTP(otp, user.getOtpTime(), user.getUuid()) > 0;
    }

    @Override
    public boolean updateMobileVerify(User user, int mobileVerify) {
        return userRepository.updateMobileVerify(mobileVerify, user.getUuid()) > 0;
    }

    @Override
    public boolean createOTPPassword(User user, String otpPassword) {
        return userRepository.createOTPPassword(otpPassword, user.getOtpPasswordTime(), user.getUuid()) > 0;
    }

    @Override
    public void remove(List<String> uuidList) {
        userRepository.deleteUsersWithIds(uuidList);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUserBySiteId(String siteId) {
//        final String uri = URI + siteId;
//        Response response = restTemplate.getForObject(uri, Response.class);
//        if (response != null && response.getStatus() == HttpStatus.OK.value()) {
//            String codeStage = response.getData().toString();
//            List<User> userContainsStage = new ArrayList<>();
//            List<Unit> unitList = unitService.findAll();
//            if (unitList != null && !unitList.isEmpty()) {
//                for (Unit unit : unitList) {
//                    List<String> stageCodes = Arrays.asList(unit.getLisOfStage().split(","));
//                    if (stageCodes.stream().anyMatch(str -> str.trim().equals(codeStage))) {
//                        List<User> user = userRepository.findByUnit(unit);
//                        if (user != null && !user.isEmpty()) {
//                            userContainsStage.addAll(user);
//                        }
//                    }
//                }
//            }
//            return userContainsStage;
//        } else {
//            return new ArrayList<>();
//        }
        return new ArrayList<>();
    }

    @Override
    public List<User> getUserByStage(String codeStage) {
        List<User> userContainsStage = new ArrayList<>();
        List<Unit> unitList = unitService.findAll();
        if (unitList != null && !unitList.isEmpty()) {
            for (Unit unit : unitList) {
                List<String> stageCodes = Arrays.asList(unit.getLisOfStage().split(","));
                if (stageCodes.contains(codeStage)) {
                    List<User> user = userRepository.findByUnit(unit);
                    if (user != null && !user.isEmpty()) {
                        userContainsStage.addAll(user);
                    }
                }
            }
        }
        return userContainsStage;
    }

    @Override
    public List<User> getUserByUnits(List<String> units) {
        return userRepository.findByUnits(units);
    }
}
