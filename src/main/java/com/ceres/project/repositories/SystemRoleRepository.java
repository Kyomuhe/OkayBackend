package com.ceres.project.repositories;

import com.ceres.project.models.database.SystemRoleModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRoleRepository extends JetRepository<SystemRoleModel, Long> {
    Optional<SystemRoleModel> findFirstByRoleCode(String code);
}
