package com.apteka.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Integer>{
    Optional<UserGroup> findByName(String name);
    boolean existsByName(String name);
}
