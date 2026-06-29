package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    @Query(value = "SELECT Client c FROM Client c WHERE c.account.isActive = :isActive", countQuery = "SELECT count(c) FROM Client c WHERE c.account.isActive = :isActive")
    Page<Client> findAll(Pageable pageable, @Param("isActive") boolean isActive);

    @Query("SELECT c FROM Client c JOIN FETCH c.account acc WHERE acc.login = :login AND acc.isActive = :isActive")
    Optional<Client> findByLogin(@Param("login") String login, @Param("isActive") boolean isActive);

    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    @Query("""
            SELECT c FROM Client c
            LEFT JOIN c.account acc
            LEFT JOIN acc.userGroup ug
            WHERE acc.isActive = :isActive
            AND ug.id = :groupId
                """)
    Page<Client> findByUserGroupId(@Param("groupId") Integer groupId, @Param("isActive") boolean isActive, Pageable pageable);

    @EntityGraph(attributePaths = {"account", "account.userGroup"})
    @Query("SELECT c FROM Client c WHERE c.account.userGroup.id = :userGroupId AND c.account.isActive = :isActive")
    List<Client> findByUserGroupId(@Param("userGroupId") Integer userGroupId, boolean isActive);

    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    @Query(value = """
            SELECT DISTINCT c FROM Client c
            LEFT JOIN c.account acc
            LEFT JOIN acc.userGroup ug
            LEFT JOIN acc.actions act
            WHERE acc.isActive = :isActive
            AND (:login IS NULL OR acc.login = :login)
            AND (:phoneNumber IS NULL OR acc.phoneNumber = :phoneNumber)
            AND (:userRoleCode IS NULL OR acc.userRole = :userRoleCode)
            AND (COALESCE(:actionsCodes, NULL) IS NULL OR act IN :actionsCodes)
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:fullName IS NULL OR c.fullName = :fullName)
            AND (:extensionNumber IS NULL OR c.extensionNumber = :extensionNumber)
            """, countQuery = """
            SELECT COUNT(DISTINCT c) FROM Client c
            LEFT JOIN c.account acc
            LEFT JOIN acc.userGroup ug
            WHERE acc.isActive = :isActive
            AND (:login IS NULL OR acc.login = :login)
            AND (:phoneNumber IS NULL OR acc.phoneNumber = :phoneNumber)
            AND (:userRoleCode IS NULL OR acc.userRole = :userRoleCode)
            AND (
                COALESCE(:actionsCodes, NULL) IS NULL
                OR EXISTS (
                    SELECT 1 FROM acc.actions a WHERE a IN :actionsCodes
                )
            )
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:fullName IS NULL OR c.fullName = :fullName)
            AND (:extensionNumber IS NULL OR c.extensionNumber = :extensionNumber)
            """)
    Page<Client> filter(
            Pageable pageable,
            @Param("login") String login,
            @Param("phoneNumber") String phoneNumber,
            @Param("userRoleCode") UserRole userRoleCode,
            @Param("actionsCodes") Set<AccountAction> actionsCodes,
            @Param("groupId") Integer groupId,
            @Param("isActive") boolean isActive,
            @Param("fullName") String fullName,
            @Param("extensionNumber") String extensionNumber);

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    @EntityGraph(attributePaths = { "account", "account.userGroup" })
    Optional<Client> findByIdWithAccount(UUID id);

    boolean existsByAccount_Login(String login);
}
