package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            JOIN FETCH acc.relations rel
            JOIN FETCH rel.userGroup ug
            JOIN FETCH a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (COALESCE(:groupsId, NULL) IS NULL OR ug.id IN (:groupsId))
            """, 
            countQuery = """
            SELECT count(a) FROM Apteka a
            JOIN a.account acc
            JOIN FETCH acc.relations rel
            JOIN FETCH rel.userGroup ug
            JOIN a.address add
            WHERE (
                (:isActive = true AND acc.isActive = true AND ug.isActive = true)
                OR
                (:isActive = false AND (acc.isActive = false OR ug.isActive = false))
            )
            AND (COALESCE(:groupsId, NULL) IS NULL OR ug.id IN (:groupsId))
            """)
    Page<Apteka> findAll(@Param("isActive") boolean isActive, @Param("groupsId") List<Integer> groupsId, Pageable pageable);

    @Query("""
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.relations rel
            JOIN FETCH rel.userGroup ug
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

    @Query("""
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.relations rel
            JOIN FETCH rel.userGroup ug
            JOIN FETCH a.address add
            WHERE (COALESCE(:userGroupIds, NULL) IS NULL OR ug.id IN (:userGroupIds)
            WHERE acc.isActive = true
            AND (:loginPattern IS NULL OR acc.login LIKE :loginPattern)
            AND (:number IS NULL OR a.number = :number)
            AND (:phonePattern IS NULL OR acc.phoneNumber LIKE :phonePattern)
            AND (:city IS NULL OR add.city = :city)
            AND (:street IS NULL OR add.street = :street)
            """)
    Page<Apteka> filterActive(@Param("loginPattern") String loginPattern, @Param("userGroupIds") Set<Integer> userGroupIds,
                              @Param("number") Integer number, @Param("phonePattern") String phonePattern,
                              @Param("city") String city, @Param("street") String street,
                              Pageable pageable);

    @Query("""
            SELECT a FROM Apteka a
            JOIN FETCH a.account acc
            JOIN FETCH acc.relations rel
            JOIN FETCH rel.userGroup ug
            JOIN FETCH a.address add
            WHERE (COALESCE(:userGroupIds, NULL) IS NULL OR ug.id IN (:userGroupIds)
            AND acc.isActive = false
            AND (:loginPattern IS NULL OR acc.login LIKE :loginPattern)
            AND (:number IS NULL OR a.number = :number)
            AND (:phonePattern IS NULL OR acc.phoneNumber LIKE :phonePattern)
            AND (:city IS NULL OR add.city = :city)
            AND (:street IS NULL OR add.street = :street)
            """)
    Page<Apteka> filterNonActive(@Param("loginPattern") String loginPattern, @Param("userGroupIds") Set<Integer> userGroupIds,
                              @Param("number") Integer number, @Param("phonePattern") String phonePattern,
                              @Param("city") String city, @Param("street") String street,
                              Pageable pageable);
}
