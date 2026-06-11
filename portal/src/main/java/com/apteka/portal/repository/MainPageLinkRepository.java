package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.MainPageLink;

public interface MainPageLinkRepository extends JpaRepository<MainPageLink, Integer>{
    @EntityGraph(attributePaths = "groupMainPageLinks")
    Optional<MainPageLink> findByid(Integer id);

    @EntityGraph(attributePaths = "groupMainPageLinks")
    List<MainPageLink> findByGroupMainPageLinksId(Integer groupMainPageLinksId);
}
