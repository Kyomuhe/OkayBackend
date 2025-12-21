package com.ceres.project.models.database;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_domain", schema = "public", catalog = "project_db")
@Builder
public class SystemDomainModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "domain_name")
    private String domainName;
}
