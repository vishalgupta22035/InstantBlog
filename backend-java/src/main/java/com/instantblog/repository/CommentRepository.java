
package com.instantblog.repository;

import com.instantblog.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    // Find approved comments for a particular blog
    List<Comment> findByBlogAndIsApprovedTrueOrderByCreatedAtDesc(String blog);

    // Find all comments sorted by creation date
    List<Comment> findAllByOrderByCreatedAtDesc();

    // Delete all comments belonging to a blog
    void deleteByBlog(String blog);
}
