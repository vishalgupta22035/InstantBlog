
package com.instantblog.repository;

import com.instantblog.model.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BlogRepository extends MongoRepository<Blog, String> {

    List<Blog> findByIsPublishedTrue();

    List<Blog> findByIsPublishedTrueOrderByCreatedAtDesc();

    List<Blog> findAllByOrderByCreatedAtDesc();

    List<Blog> findTop5ByOrderByCreatedAtDesc();

    long countByIsPublishedFalse();
    long countByIsPublishedTrue();
}
