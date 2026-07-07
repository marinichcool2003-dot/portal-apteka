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

    @Query("SELECT g.isActive FROM UserGroup ug WHERE ug.id = :id")
    @Cacheable(value = CacheNames.GROUP_USER_STATUS, key = "#id")
    Boolean isGroupActive(@Param("id") Integer id);

    List<UserGroup> findAllByIdIn(Set<Integer> ids);

    @Query("""
            SELECT DISTINCT vg FROM UserGroup g
            LEFT JOIN g.visibleGroups vg
            WHERE (g.id = :groupId AND g.isActive = true AND vg.isActive = true)
               OR (g.id = :groupId AND g.isActive = true AND vg.id = g.id)
            """)
    List<UserGroup> findVisibleGroupsIncludingSelf(@Param("groupId") Integer groupId);

    List<UserGroup> findByActive(boolean active);

    @Query("""
             SELECT COUNT(vg) > 0 FROM UserGroup g
             JOIN g.visibleGroups vg
             WHERE g.id = :currentUserGroupId AND vg.id = :targetGroupId AND vg.isActive =: isActive
            """)
    boolean isGroupVisibleToAnother(@Param("currentUserGroupId") Integer currentUserGroupId,
            @Param("targetGroupId") Integer targetGroupId, @Param("isActive") boolean isActive);

    @Cacheable(value = CacheNames.USER_GROUP, key = "#id", unless = "!#result.isPresent() || !#result.get().isActive()", sync = true)
    @Query("SELECT g FROM UserGroup g WHERE g.id = :id")
    Optional<UserGroup> findByIdAndCache(@Param("id") Integer id);
}
