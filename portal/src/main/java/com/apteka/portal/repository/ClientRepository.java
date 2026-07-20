package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query(value = """
            SELECT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug 
            WHERE ((:isActive = true AND acc.isActive = true AND ug.isActive = true) OR (:isActive = false AND (acc.isActive = false OR ug.isActive = false)))
            """, 
            countQuery = "SELECT count(c) FROM Client c JOIN c.account acc JOIN acc.userGroup ug WHERE ((:isActive = true AND acc.isActive = true AND ug.isActive = true) OR (:isActive = false AND (acc.isActive = false OR ug.isActive = false)))")
    Page<Client> findAll(Pageable pageable, @Param("isActive") boolean isActive);

    @Query("""
            SELECT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug
            WHERE acc.login = :login
            AND (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            """)
    Optional<Client> findByLogin(@Param("login") String login, @Param("isActive") boolean isActive);

    @Query("""
            SELECT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND ug.id = :groupId
                """)
    Page<Client> findByUserGroupId(@Param("groupId") Integer groupId, @Param("isActive") boolean isActive,
            Pageable pageable);

    @Query("""
            SELECT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug
            WHERE ug.id = :userGroupId 
            AND (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            """)
    List<Client> findByUserGroupId(@Param("userGroupId") Integer userGroupId, @Param("isActive") boolean isActive);

    @Query(value = """
            SELECT DISTINCT c FROM Client c
            JOIN FETCH c.account acc
            JOIN FETCH acc.userGroup ug
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:login IS NULL OR acc.login = :login)
            AND (:phoneNumber IS NULL OR acc.phoneNumber = :phoneNumber)
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:fullName IS NULL OR c.fullName = :fullName)
            AND (:extensionNumber IS NULL OR c.extensionNumber = :extensionNumber)
            """, countQuery = """
            SELECT COUNT(DISTINCT c) FROM Client c
            JOIN c.account acc
            JOIN acc.userGroup ug
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:login IS NULL OR acc.login = :login)
            AND (:phoneNumber IS NULL OR acc.phoneNumber = :phoneNumber)
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:fullName IS NULL OR c.fullName = :fullName)
            AND (:extensionNumber IS NULL OR c.extensionNumber = :extensionNumber)
            """)
    Page<Client> filter(
            Pageable pageable,
            @Param("login") String login,
            @Param("phoneNumber") String phoneNumber,
            @Param("groupId") Integer groupId,
            @Param("isActive") boolean isActive,
            @Param("fullName") String fullName,
            @Param("extensionNumber") String extensionNumber);

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    Optional<Client> findByIdWithAccount(UUID id);

    boolean existsByAccount_Login(String login);
}
