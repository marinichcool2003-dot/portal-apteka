package com.apteka.portal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Apteka;

public interface AptekaRepository extends JpaRepository<Apteka, UUID> {

        @Override
        @EntityGraph(attributePaths = {"account", "account.userGroup"})
        Page<Apteka> findAll(Pageable pageable);

        @Query("SELECT a FROM Apteka a WHERE a.id = :id")
        @EntityGraph(attributePaths = {"account", "account.userGroup"})
        Optional<Apteka> findByIdWithAccount(UUID id);

        boolean existsByAccount_Login(String login);

        boolean existsByAccount_UserGroup_IdAndNumber(Integer userGroupName, Integer number);

        @EntityGraph(attributePaths = {"account", "account.userGroup"})
        @Query("""
                        SELECT a FROM Apteka a
                        LEFT JOIN a.account acc
                        LEFT JOIN acc.userGroup ug
                        WHERE (:login IS NULL OR LOWER(acc.login) LIKE LOWER(CONCAT(:login, '%')))
                        AND (:groupId IS NULL OR acc.userGroup.id = :groupId)
                        AND (:number IS NULL OR a.number = :number)
                        AND (:phoneNumber IS NULL OR LOWER(acc.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')))
                        """)
        Page<Apteka> filter(@Param("login") String login, @Param("groupId") Integer groupId,
                        @Param("number") Integer number, @Param("phoneNumber") String phoneNumber, Pageable pageable);
}
