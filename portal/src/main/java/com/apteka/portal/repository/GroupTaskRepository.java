package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apteka.portal.models.GroupTask;

public interface GroupTaskRepository extends JpaRepository<GroupTask, Integer> {
    @Query("""
            SELECT EXISTS(
                SELECT 1
                FROM GroupTask gt
                WHERE gt.name = :name
                    AND gt.executorGroup.id = :executorGroupId
                    AND gt.Active = true
            )
            """)
    boolean existsByNameAndExecutorGroupIdAndActive(String name, Integer executorGroupId);

    @Override
    @EntityGraph(attributePaths = { "creatorGroup", "executorGroup" })
    Optional<GroupTask> findById(Integer id);

    @Query("""
            SELECT gt FROM GroupTask gt
            WHERE gt.creatorGroup.id = :creatorGroupId
            AND gt.executorGroup.id = :executorGroupId
            AND gt.Active =: isActive
            """)
    List<GroupTask> findByGroupsAndActive(Integer creatorGroupId, Integer executorGroupId, boolean isActive);

    boolean existsByNameAndCreatorGroupIdAndExecutorGroupId(String name, Integer creatorGroupId, Integer executorGroupId);
}
