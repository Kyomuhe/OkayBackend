package com.ceres.project.models.database;

import com.ceres.project.models.jpa_helpers.enums.AppDomains;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "system_role", schema = "public", catalog = "project_db")
public class SystemRoleModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "role_name")
    private String roleName;
    @Basic
    @Column(name = "role_code")
    private String roleCode;
    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
    @Basic
    @Column(name = "domain")
    @Enumerated(EnumType.STRING)
    private AppDomains roleDomain;
}
