package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.GroupMainPageLinks;

public interface GroupMainPageLinksRepository extends JpaRepository<GroupMainPageLinks, Integer>{
    boolean existsByName(String name);
}
