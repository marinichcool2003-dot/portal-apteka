package com.apteka.portal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @EntityGraph(attributePaths = "userGroup")
    Optional<Account> findByIdWithUserGroup(UUID id);

    Optional<Account> findByLogin(String login);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.userGroup.id =: userGroupId")
    Integer countByUserGroupId(@Param("userGroupId") Integer userGroupId);
}
