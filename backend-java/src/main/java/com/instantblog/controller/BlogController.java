
package com.instantblog.controller;

import com.instantblog.model.Blog;
import com.instantblog.model.Comment;
import com.instantblog.service.BlogService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;
    private final ObjectMapper objectMapper;

    public BlogController(BlogService blogService, ObjectMapper objectMapper) {
        this.blogService = blogService;
        this.objectMapper = objectMapper;
    }

    // GET /api/blog/all
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllBlogs() {

        List<Blog> blogs = blogService.getAllPublishedBlogs();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "blogs", blogs
        ));
    }

    // GET /api/blog/{blogId}
    @GetMapping("/{blogId}")
    public ResponseEntity<Map<String, Object>> getBlogById(
            @PathVariable String blogId
    ) {

        Optional<Blog> blog = blogService.getBlogById(blogId);

        if (blog.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "Blog not found"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "blog", blog.get()
        ));
    }
    @PostMapping(
            value = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> addBlog(
            @RequestPart("blog") String blogJson,
            @RequestPart("image") MultipartFile image
    ) {
        try {
            Blog blog = objectMapper.readValue(blogJson, Blog.class);

            Blog savedBlog = blogService.addBlog(blog, image);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Blog added successfully");
            response.put("blog", savedBlog);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Could not add blog");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
    @PostMapping("/toggle-publish")
    public ResponseEntity<?> togglePublish(@RequestBody Map<String, String> request) {

        try {
            String blogId = request.get("id");

            Blog blog = blogService.togglePublish(blogId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Blog publish status updated",
                    "blog", blog
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteBlog(@RequestBody Map<String, String> request) {

        try {
            String blogId = request.get("id");

            blogService.deleteBlog(blogId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Blog deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/add-comment")
    public ResponseEntity<?> addComment(@RequestBody Map<String, String> request) {

        try {
            Comment comment = blogService.addComment(
                    request.get("blog"),
                    request.get("name"),
                    request.get("content")
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Comment added successfully",
                    "comment", comment
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/comments")
    public ResponseEntity<?> getComments(@RequestBody Map<String, String> request) {

        try {
            List<Comment> comments =
                    blogService.getApprovedComments(request.get("blogId"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "comments", comments
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/generate")
    public ResponseEntity<?> generateContent(
            @RequestBody Map<String, String> request) {

        try {
            String prompt = request.get("prompt");

            String content = blogService.generateBlogContent(prompt);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "content", content
            ));

        } catch (Exception error) {

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", error.getMessage() == null
                            ? "Content generation failed"
                            : error.getMessage()
            ));
        }
    }
}
