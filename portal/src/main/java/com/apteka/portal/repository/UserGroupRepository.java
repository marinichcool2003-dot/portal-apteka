package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
    Optional<UserGroup> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g.isActive FROM UserGroup ug WHERE g.id = :id")
    @Cacheable(value = CacheNames.GROUP_USER_STATUS, key = "#id")
    Boolean isGroupActive(@Param("id") Integer id);

    List<UserGroup> findAllByIdIn(Set<Integer> ids);
}
