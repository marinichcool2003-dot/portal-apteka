package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apteka.portal.models.Client;

public interface ClientInterface extends JpaRepository<Client, UUID>{
    List<Client> findByRole(String role);

    Optional<Client> findByLogin(String login);

    @Query("""
            SELECT c FROM Client c
            JOIN c.groupClient g
            WHERE g.id = :groupId
            """)
    List<Client> findByGroupId(Integer groupId);
}
