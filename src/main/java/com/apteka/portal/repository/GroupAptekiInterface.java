package com.apteka.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.GroupApteki;

@Repository
public interface GroupAptekiInterface extends JpaRepository<GroupApteki, Integer> {
    Optional<GroupApteki> findByName(String name);
}
