package com.apteka.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.UserGroup;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Integer>{
    Optional<UserGroup> findByName(String name);
}
