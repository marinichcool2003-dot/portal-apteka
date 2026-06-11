package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.GroupTask;

public interface GroupTaskRepository extends JpaRepository<GroupTask, Integer> {
    @EntityGraph(attributePaths = {"userGroup"})
    Optional<GroupTask> findByNameAndUserGroupId(String name, Integer userGroupId);

    @EntityGraph(attributePaths = {"userGroup"})
    List<GroupTask> findByUserGroupId(Integer userGroupId);

    boolean existsByNameAndUserGroupId(String name, Integer userGroupId);
}
