package com.elcom.metacen.id.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author anhdv
 */
public interface AuthService {

    UserDetails loadUserByUuid(String uuid);
}