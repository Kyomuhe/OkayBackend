package com.ceres.project.repositories;

import com.ceres.project.models.database.SystemUserModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemUserRepository extends JetRepository<SystemUserModel, Long> {
    SystemUserModel findFirstByUsername(String username);
    Optional<SystemUserModel> findFirstByUsernameOrEmail(String username, String email);
    SystemUserModel findFirstByEmail(String email);
}

