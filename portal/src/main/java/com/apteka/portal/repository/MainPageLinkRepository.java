package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.MainPageLink;

public interface MainPageLinkRepository extends JpaRepository<MainPageLink, Integer>{
    @Override
    @EntityGraph(attributePaths = "groupMainPageLinks")
    Optional<MainPageLink> findById(Integer id);

    @EntityGraph(attributePaths = "groupMainPageLinks")
    List<MainPageLink> findAll();

    @EntityGraph(attributePaths = "groupMainPageLinks")
    List<MainPageLink> findByGroupMainPageLinksIdAndIsActive(Integer groupMainPageLinksId, Boolean isActive);

    boolean existsByName(String name);
    boolean existsByLink(String link);
}
