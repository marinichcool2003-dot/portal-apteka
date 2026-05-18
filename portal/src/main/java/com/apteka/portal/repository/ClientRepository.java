package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.Client;

public interface ClientRepository extends JpaRepository<Client, UUID>{

    @EntityGraph(attributePaths = { "userGroup", "roles"})
    Optional<Client> findByLogin(String login);

    List<Client> findByUserGroupId(Integer groupId);

    boolean existsByLogin(String login);
}
