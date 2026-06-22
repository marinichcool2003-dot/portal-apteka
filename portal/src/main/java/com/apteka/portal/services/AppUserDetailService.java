package com.apteka.portal.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {

        return accountRepository.findByLogin(login).map(account -> new AppUserDetails(account.getClient()))
                .orElseThrow(() -> new UsernameNotFoundException(login));
    }
}
