package com.ceres.project.repositories;

import com.ceres.project.models.database.SystemDomainModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemDomainRepository extends JetRepository<SystemDomainModel, Long> {
}
