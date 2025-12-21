package com.ceres.project.models.database;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "system_role_permission_assignment", schema = "public", catalog = "project_db")
public class SystemRolePermissionAssignmentModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "permission_code")
    private String permissionCode;
    @Basic
    @Column(name = "role_code")
    private String roleCode;
}
