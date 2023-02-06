package com.elcom.metacen.id.service.impl;

import com.elcom.metacen.id.auth.CustomUserDetails;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.repository.UserCustomizeRepository;
import com.elcom.metacen.id.repository.UserRepository;
import com.elcom.metacen.id.service.AuthService;

import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author anhdv
 */
@Service
public class AuthServiceImpl implements UserDetailsService, AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserCustomizeRepository userCustomizeRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String userInfo) {
        // Kiểm tra xem user có tồn tại trong database không?
        User user = userCustomizeRepository.findByEmail(userInfo);
        if (user == null) {
            user = userCustomizeRepository.findByMobile(userInfo);
        }
        if (user == null) {
            user = userCustomizeRepository.findByUserName(userInfo);
        }
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userInfo : " + userInfo);
        } else {
            LOGGER.info("Find user with " + userInfo + " ==> uuid: " + user.getUuid());
        }
        return new CustomUserDetails(user);
    }

    // JWTAuthenticationFilter sẽ sử dụng hàm này
    //@Transactional
    @Override
    public UserDetails loadUserByUuid(String uuid) {
        long start = System.currentTimeMillis();
        User user = userRepository.findById(uuid).orElse(null);
        long end = System.currentTimeMillis();
        LOGGER.info("loadUserByUuid >>> find by id: {} => In: {}ms", uuid, (end - start));
        if (user == null) {
            throw new UsernameNotFoundException("User not found with uuid : " + uuid);
        } else {
            //Lấy thêm thông tin cameraGroupIds
            end = System.currentTimeMillis();
            LOGGER.info("loadUserByUuid >>> getCameraGroupIdsOwner in: {}ms", (end - start));
        }
        return new CustomUserDetails(user);
    }
}
