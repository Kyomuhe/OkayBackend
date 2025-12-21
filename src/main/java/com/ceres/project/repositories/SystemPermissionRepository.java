package com.ceres.project.repositories;

import com.ceres.project.models.database.SystemPermissionModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemPermissionRepository extends JetRepository<SystemPermissionModel, Long> {
    Optional<SystemPermissionModel> findFirstByPermissionCode(String permission_code);
}
