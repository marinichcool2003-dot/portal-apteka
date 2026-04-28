package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.Client;

public interface ClientInterface extends JpaRepository<Client, UUID>{

    Optional<Client> findByLogin(String login);

    List<Client> findByGroupId(Integer groupId);

    boolean existsByLogin(String login);
}
