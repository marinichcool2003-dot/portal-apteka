package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.MainPageLink;

public interface MainPageLinkRepository extends JpaRepository<MainPageLink, Integer>{
    @Override
    @EntityGraph(attributePaths = "groupMainPageLinks")
    Optional<MainPageLink> findById(Integer id);


    @Query("""
            SELECT m FROM MainPageLink m
            JOIN FETCH m.groupMainPageLinks gr
            WHERE (m.isActive = true AND gr.isActive = true)
            """)
    List<MainPageLink> findAll();

    @Query("""
            SELECT m from MainPageLink m
            JOIN FETCH m.groupMainPageLinks gr
            WHERE gr.id = :groupMainPageLinksId
            AND(
                (:isActive = true AND m.isActive = true AND gr.isActive = true)
                OR
                (:isActive = false AND (m.isActive = false OR gr.isActive = false))
            )
            """)
    List<MainPageLink> findByGroupMainPageLinksIdAndIsActive(@Param("groupMainPageLinksId") Integer groupMainPageLinksId, @Param("isActive") Boolean isActive);

    boolean existsByName(String name);
    boolean existsByLink(String link);
}
