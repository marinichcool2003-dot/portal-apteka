package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer>{
    @EntityGraph(attributePaths = {"author", "userGroup"})
    List<News> findAll();

    @EntityGraph(attributePaths = {"author", "userGroup"})
    List<News> findByUserGroupId(Integer userGroupId);

    @EntityGraph(attributePaths = {"author", "userGroup"})
    Optional<News> findById(Integer id); 
}
