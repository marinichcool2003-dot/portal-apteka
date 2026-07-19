package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.GroupTask;

public interface GroupTaskRepository extends JpaRepository<GroupTask, Integer> {
    @Query("""
            SELECT EXISTS(
                SELECT 1
                FROM GroupTask gt
                WHERE gt.name = :name
                    AND gt.intendedGroup.id = :intendedGroupId
                    AND gt.isActive = true
            )
            """)
    boolean existsByNameAndExecutorGroupIdAndActive(@Param("name") String name, @Param("intendedGroupId") Integer intendedGroupId);

    @Override
    @EntityGraph(attributePaths = { "creatorGroup", "intendedGroup" })
    Optional<GroupTask> findById(Integer id);

    @Query("""
            SELECT gt FROM GroupTask gt
            WHERE gt.creatorGroup.id = :creatorGroupId
            AND gt.intendedGroup.id = :intendedGroupId
            AND gt.isActive = :isActive
            """)
    List<GroupTask> findByGroupsAndIsActive(@Param("creatorGroupId") Integer creatorGroupId, @Param("intendedGroupId") Integer intendedGroupId, @Param("isActive") boolean isActive);

    boolean existsByNameAndCreatorGroupIdAndIntendedGroupId(String name, Integer creatorGroupId, Integer intendedGroupId);
}
