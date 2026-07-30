package com.apteka.portal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.Apteka;

public interface AptekaRepository extends JpaRepository<Apteka, UUID> {

    @Query(value = """
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.userGroup ug
            JOIN FETCH a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:groupId IS NULL OR ug.id = :groupId)
            """, 
            countQuery = """
            SELECT count(a) FROM Apteka a
            JOIN a.account acc
            JOIN acc.userGroup ug
            JOIN a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:groupId IS NULL OR ug.id = :groupId)
            """)
    Page<Apteka> findAll(@Param("isActive") boolean isActive, @Param("groupId") Integer groupId, Pageable pageable);

    @Query("""
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.userGroup ug
            WHERE a.id = :id
            AND (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            """)
    Optional<Apteka> findByIdWithAccount(@Param("id") UUID id, @Param("isActive") boolean isActive);

    boolean existsByAccount_Login(String login);

    boolean existsByAccount_UserGroup_IdAndNumber(Integer userGroupName, Integer number);

    @Query(value = """
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.userGroup ug
            JOIN FETCH a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:login IS NULL OR LOWER(acc.login) LIKE LOWER(CONCAT(:login, '%')))
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:number IS NULL OR a.number = :number)
            AND (:phoneNumber IS NULL OR LOWER(acc.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')))
            AND (:city IS NULL OR add.city = :city)
            AND (:street IS NULL OR add.street = :street)
            """, countQuery = """
            SELECT count(a) FROM Apteka a
            JOIN a.account acc
            JOIN acc.userGroup ug
            JOIN a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (:login IS NULL OR LOWER(acc.login) LIKE LOWER(CONCAT(:login, '%')))
            AND (:groupId IS NULL OR ug.id = :groupId)
            AND (:number IS NULL OR a.number = :number)
            AND (:phoneNumber IS NULL OR LOWER(acc.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')))
            AND (:city IS NULL OR add.city = :city)
            AND (:street IS NULL OR add.street = :street)
            """)
    Page<Apteka> filter(@Param("login") String login, @Param("groupId") Integer groupId,
            @Param("number") Integer number, @Param("phoneNumber") String phoneNumber,
            @Param("isActive") boolean isActive, @Param("city") String city, @Param("street") String street,
            Pageable pageable);
}
