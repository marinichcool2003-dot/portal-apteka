package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Apteka;

public interface AptekaRepository extends JpaRepository<Apteka, UUID> {

        @Override
        @Query("""
                SELECT ap FROM Apteka ap
                JOIN FETCH ap.account
                        """)
        List<Apteka> findAll();

        boolean existsByAccount_Login(String login);

        boolean existsByAccount_UserGroup_IdAndNumber(Integer userGroupName, Integer number);

        @Query("""
                        SELECT a FROM Apteka a
                        LEFT JOIN FETCH a.account acc
                        LEFT JOIN FETCH acc.userGroup ug
                        WHERE (:login IS NULL OR LOWER(acc.login) LIKE LOWER(CONCAT(:login, '%')))
                        AND (:groupId IS NULL OR acc.userGroup.id = :groupId)
                        AND (:number IS NULL OR a.number = :number)
                        AND (:phoneNumber IS NULL OR LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')))
                        """)
        List<Apteka> filter(@Param("login") String login, @Param("groupId") Integer groupId,
                        @Param("number") Integer number, @Param("phoneNumber") String phoneNumber);
}
