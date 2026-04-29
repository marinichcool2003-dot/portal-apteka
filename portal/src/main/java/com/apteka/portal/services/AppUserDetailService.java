package com.apteka.portal.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {
    private final ClientRepository clientRepository;
    private final AptekaRepository aptekaRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {

        return clientRepository.findByLogin(login)
                .map(AppUserDetails::new)
                .orElseGet(() -> aptekaRepository.findByLogin(login)
                        .map(AppUserDetails::new)
                        .orElseThrow(() -> new UsernameNotFoundException(login)));
    }
}
