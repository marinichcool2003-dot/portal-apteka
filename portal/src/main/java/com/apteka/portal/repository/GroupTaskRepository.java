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
                LEFT JOIN gt.creatorGroup cg
                LEFT JOIN gt.intendedGroup ig
                WHERE gt.name = :name
                    AND ig.id = :intendedGroupId
                    AND (gt.isActive = true AND cg.isActive = true AND ig.isActive = true)
            )
            """)
    boolean existsByNameAndIntendedGroupIdAndActive(@Param("name") String name, @Param("intendedGroupId") Integer intendedGroupId);

    @Override
    @EntityGraph(attributePaths = { "creatorGroup", "intendedGroup" })
    Optional<GroupTask> findById(Integer id);

    @Query("""
            SELECT gt FROM GroupTask gt
            LEFT JOIN gt.creatorGroup cg
            LEFT JOIN gt.intendedGroup ig
            WHERE cg.id = :creatorGroupId
            AND ig.id = :intendedGroupId
            AND (
                (:isActive = true AND gt.isActive = true AND cg.isActive = true AND ig.isActive = true)
                OR
                (:isActive = false AND (gt.isActive = false OR cg.isActive = false OR ig.isActive = false))
            )
            ORDER BY cg.id, gt.id, gt.name
            """)
    List<GroupTask> findByGroupsAndIsActive(@Param("creatorGroupId") Integer creatorGroupId, @Param("intendedGroupId") Integer intendedGroupId, @Param("isActive") boolean isActive);

    boolean existsByNameAndCreatorGroupIdAndIntendedGroupId(String name, Integer creatorGroupId, Integer intendedGroupId);

    // AUDIT-FIX: sibling GroupTask одного отдела с intended = APTEKA_GROUP (для bulk/sync)
    @Query("""
            SELECT gt FROM GroupTask gt
            JOIN FETCH gt.creatorGroup cg
            JOIN FETCH gt.intendedGroup ig
            WHERE cg.id = :creatorGroupId
              AND gt.name = :name
              AND ig.groupType = :aptekaType
              AND (
                  (:isActive IS NULL)
                  OR (:isActive = true AND gt.isActive = true)
                  OR (:isActive = false AND gt.isActive = false)
              )
            ORDER BY ig.id
            """)
    List<GroupTask> findSiblingAptekaGroupTasks(
            @Param("creatorGroupId") Integer creatorGroupId,
            @Param("name") String name,
            @Param("isActive") Boolean isActive,
            @Param("aptekaType") com.apteka.portal.models.UserGroupType aptekaType);

    default List<GroupTask> findSiblingAptekaGroupTasks(Integer creatorGroupId, String name, Boolean isActive) {
        return findSiblingAptekaGroupTasks(creatorGroupId, name, isActive,
                com.apteka.portal.models.UserGroupType.APTEKA_GROUP);
    }
}
