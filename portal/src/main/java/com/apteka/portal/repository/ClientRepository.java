package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Override
    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    List<Client> findAll();

    @Query("SELECT c FROM Client c JOIN FETCH c.account acc WHERE acc.login = :login")
    Optional<Client> findByLogin(@Param("login") String login);

    @Query("""
            SELECT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug
            WHERE ug.id = :groupId
                """)
    List<Client> findByUserGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    Optional<Client> findByIdWithAccount(UUID id);

    boolean existsByAccount_Login(String login);
}
