package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.News;

public interface NewsRepository extends JpaRepository<News, Integer>{
    @EntityGraph(attributePaths = {"author", "userGroup"})
    @Query("SELECT n FROM News n ORDER BY COALESCE(n.updatedAt, n.creationDate) DESC")
    List<News> findAll();

    @EntityGraph(attributePaths = {"author", "userGroup"})
    @Query("SELECT n FROM News n WHERE n.userGroup.id = :userGroupId ORDER BY COALESCE(n.updatedAt, n.creationDate) DESC")
    List<News> findByUserGroupId(@Param("userGroupId") Integer userGroupId);

    @EntityGraph(attributePaths = {"author", "userGroup"})
    Optional<News> findById(Integer id); 
}
