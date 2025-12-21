package com.ceres.project.models.database;

import com.ceres.project.models.jpa_helpers.enums.AppDomains;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "system_permission", schema = "public", catalog = "project_db")
public class SystemPermissionModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "permission_code")
    private String permissionCode;
    @Basic
    @Column(name = "permission_name")
    private String permissionName;
    @Basic
    @Column(name = "domain")
    @Enumerated(EnumType.STRING)
    private AppDomains permissionDomain;
}
