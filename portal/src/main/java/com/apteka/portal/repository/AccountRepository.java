package com.apteka.portal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByLogin(String login);
}
