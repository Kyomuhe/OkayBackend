package com.ceres.project.repositories;

import com.ceres.project.models.database.PostsModel;
import com.ceres.project.models.jpa_helpers.repository.JetRepository;

import java.util.List;

public interface PostsRepository extends JetRepository <PostsModel, Long> {
    List<PostsModel> findByUserId(Long id);
}
