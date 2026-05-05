package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.Apteka;

@Repository
public interface AptekaRepository extends JpaRepository<Apteka, Integer> {
    boolean existsByLogin(String login);

    Optional<Apteka> findByLogin(String login);

    @Query("""
    SELECT a FROM Apteka a
    WHERE (:login IS NULL OR LOWER(a.login) LIKE LOWER(CONCAT(:login, '%')))
    AND (:groupId IS NULL OR a.userGroup.id = :groupId)
    AND (:number IS NULL OR a.number = :number)
    AND (:phoneNumber IS NULL OR LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')))
    """)
    List<Apteka> filter(@Param("login") String login, @Param("groupId") Integer groupId, @Param("number") Integer number, @Param("phoneNumber") String phoneNumber);
}
