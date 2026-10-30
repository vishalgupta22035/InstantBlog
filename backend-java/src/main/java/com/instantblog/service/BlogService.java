
package com.instantblog.service;

import com.instantblog.model.Blog;
import com.instantblog.model.Comment;
import com.instantblog.repository.BlogRepository;
import com.instantblog.repository.CommentRepository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import com.google.genai.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.imagekit.client.ImageKitClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final ImageKitClient imageKitClient;
    private final CommentRepository commentRepository;
    private final Client geminiClient;

    public BlogService(
            BlogRepository blogRepository,
            CommentRepository commentRepository,
            ImageKitClient imageKitClient, Client geminiClient) {

        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.imageKitClient = imageKitClient;
        this.geminiClient = geminiClient;
    }

    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
    }

    public Optional<Blog> getBlogById(String blogId) {

        if (blogId == null || !ObjectId.isValid(blogId)) {
            return Optional.empty();
        }

        return blogRepository.findById(blogId);
    }
    public Blog addBlog(Blog blog, MultipartFile image)
            throws IOException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Blog image is required");
        }

        if (blog.getTitle() == null || blog.getTitle().isBlank()) {
            throw new IllegalArgumentException("Blog title is required");
        }

        String fileName = image.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "blog-image";
        }

        FileUploadParams params = FileUploadParams.builder()
                .file(image.getBytes())
                .fileName(fileName)
                .folder("/blogs")
                .build();

        FileUploadResponse uploadResponse =
                imageKitClient.files().upload(params);

        String imageUrl = uploadResponse.url()
                .orElseThrow(() -> new RuntimeException("ImageKit did not return an image URL"));

        blog.setImage(imageUrl);
        blog.setCreatedAt(Instant.now());
        blog.setUpdatedAt(Instant.now());

        if (blog.getIsPublished() == null) {
            blog.setIsPublished(false);
        }

        return blogRepository.save(blog);
    }

    public Blog togglePublish(String blogId) {

        if (blogId == null || !ObjectId.isValid(blogId)) {
            throw new IllegalArgumentException("Invalid blog ID");
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        blog.setIsPublished(!Boolean.TRUE.equals(blog.getIsPublished()));
        blog.setUpdatedAt(Instant.now());

        return blogRepository.save(blog);
    }

    public void deleteBlog(String blogId) {

        if (blogId == null || !ObjectId.isValid(blogId)) {
            throw new IllegalArgumentException("Invalid blog ID");
        }

        if (!blogRepository.existsById(blogId)) {
            throw new RuntimeException("Blog not found");
        }

        commentRepository.deleteByBlog(blogId);
        blogRepository.deleteById(blogId);
    }
    public Comment addComment(String blogId, String name, String content) {

        if (blogId == null || !ObjectId.isValid(blogId)) {
            throw new IllegalArgumentException("Invalid blog ID");
        }

        if (!blogRepository.existsById(blogId)) {
            throw new RuntimeException("Blog not found");
        }

        if (name == null || name.isBlank()
                || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Name and comment are required");
        }

        Comment comment = new Comment();

        comment.setBlog(blogId);
        comment.setName(name);
        comment.setContent(content);
        comment.setIsApproved(false);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        return commentRepository.save(comment);
    }

    public List<Comment> getApprovedComments(String blogId) {

        if (blogId == null || !ObjectId.isValid(blogId)) {
            throw new IllegalArgumentException("Invalid blog ID");
        }

        return commentRepository
                .findByBlogAndIsApprovedTrueOrderByCreatedAtDesc(blogId);
    }
    public List<Comment> getAllComments() {
        return commentRepository.findAllByOrderByCreatedAtDesc();
    }

    public Comment approveComment(String commentId) {

        if (commentId == null || !ObjectId.isValid(commentId)) {
            throw new IllegalArgumentException("Invalid comment ID");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setIsApproved(true);
        comment.setUpdatedAt(Instant.now());

        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId) {

        if (commentId == null || !ObjectId.isValid(commentId)) {
            throw new IllegalArgumentException("Invalid comment ID");
        }

        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment not found");
        }

        commentRepository.deleteById(commentId);
    }
    public Map<String, Object> getDashboardData() {

        long totalBlogs = blogRepository.count();
        long publishedBlogs = blogRepository.countByIsPublishedTrue();
        long draftBlogs = blogRepository.countByIsPublishedFalse();
        long totalComments = commentRepository.count();

        List<Blog> recentBlogs =
                blogRepository.findTop5ByOrderByCreatedAtDesc();

        List<Comment> recentComments =
                commentRepository.findAllByOrderByCreatedAtDesc()
                        .stream()
                        .limit(5)
                        .toList();

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalBlogs", totalBlogs);
        dashboard.put("publishedBlogs", publishedBlogs);
        dashboard.put("draftBlogs", draftBlogs);
        dashboard.put("totalComments", totalComments);
        dashboard.put("recentBlogs", recentBlogs);
        dashboard.put("recentComments", recentComments);

        return dashboard;
    }
    public String generateBlogContent(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        String finalPrompt = prompt
                + " Generate a blog content for this topic in simple text format.";

        var response = geminiClient.models.generateContent(
                "gemini-3.8-flash",
                finalPrompt,
                null
        );

        String content = response.text();

        if (content == null || content.isBlank()) {
            throw new RuntimeException("Gemini returned empty content");
        }

        return content;
    }
}
