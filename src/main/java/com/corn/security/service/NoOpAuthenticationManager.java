package com.corn.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

/**
 * Empty manager, actually, but AbstractAuthenticationProcessingFilter requires a manager.
 *
 * @author Oleg Zaidullin
 */
public class NoOpAuthenticationManager implements AuthenticationManager {

    @Override
    public Authentication authenticate(Authentication authentication) {
        return authentication;
    }

}
