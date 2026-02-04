package com.apteka.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.GroupClient;
import com.apteka.portal.models.GroupTask;

@Repository
public interface GroupClientInterface extends JpaRepository<GroupClient, Integer>{
    Optional<GroupTask> findByName(String name);
}
