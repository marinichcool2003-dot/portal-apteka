package com.apteka.portal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.apteka.portal.models.Account;
import com.apteka.portal.models.UserRole;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @EntityGraph(attributePaths = "userGroup")
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithUserGroup(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"userGroup", "client", "apteka", "actions"})
    Optional<Account> findByLogin(String login);

    @EntityGraph(attributePaths = {"userGroup", "client", "apteka", "actions"})
    Optional<Account> findByEmail(String email);

    @EntityGraph(attributePaths = {"userGroup", "client", "apteka", "actions"})
    @Query("SELECT a FROM Account a WHERE a.login = :identifier OR a.email = :identifier")
    Optional<Account> findByLoginOrEmail(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.userGroup.id = :userGroupId")
    Integer countByUserGroupId(@Param("userGroupId") Integer userGroupId);

    @EntityGraph(attributePaths = {"userGroup", "client"})
    @Query("""
            SELECT a FROM Account a
            WHERE a.userGroup.id = :userGroupId
              AND a.userRole = :role
              AND a.isActive = true
            """)
    List<Account> findActiveByUserGroupIdAndRole(
            @Param("userGroupId") Integer userGroupId,
            @Param("role") UserRole role);

    @Query("""
            SELECT a FROM Account a
            WHERE a.userGroup.id = :userGroupId
              AND a.isActive = true
            """)
    List<Account> findActiveByUserGroupId(@Param("userGroupId") Integer userGroupId);
}
