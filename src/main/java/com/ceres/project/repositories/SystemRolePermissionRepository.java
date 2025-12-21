package com.ceres.project.repositories;

import com.ceres.project.models.database.SystemRolePermissionAssignmentModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SystemRolePermissionRepository extends JetRepository<SystemRolePermissionAssignmentModel, Long> {
    Optional<SystemRolePermissionAssignmentModel> findFirstByRoleCodeAndPermissionCode(String role_code, String permission_code);
    Collection<SystemRolePermissionAssignmentModel> findAllByRoleCode(String role_code);
}
