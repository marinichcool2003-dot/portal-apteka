package com.apteka.portal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.Client;

public interface ClientInterface extends JpaRepository<Client, UUID>{
    List<Client> findByRole(String role);

    List<Client> findByGroupId(Integer groupId);
}
