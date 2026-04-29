package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.GroupTask;

@Repository
public interface GroupTaskInterface extends JpaRepository<GroupTask, Integer> {
    Optional<GroupTask> findByNameAndUserGroupId(String name, Integer userGroupId);
    List<GroupTask> findByUserGroupId(Integer userGroupId);
}
