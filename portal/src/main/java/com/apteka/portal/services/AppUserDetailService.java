package com.apteka.portal.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.repository.AptekaInterface;
import com.apteka.portal.repository.ClientInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {
    private final ClientInterface clientInterface;
    private final AptekaInterface aptekaInterface;

    @Override
    public UserDetails loadUserByUsername(String login) {

        return clientInterface.findByLogin(login)
                .map(AppUserDetails::new)
                .orElseGet(() -> aptekaInterface.findByLogin(login)
                        .map(AppUserDetails::new)
                        .orElseThrow(() -> new UsernameNotFoundException(login)));
    }
}
