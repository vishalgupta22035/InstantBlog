
package com.instantblog.controller;

import com.instantblog.service.AdminService;

import com.instantblog.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.instantblog.model.Comment;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final BlogService blogService;

    public AdminController(AdminService adminService, BlogService blogService) {
        this.adminService = adminService;
        this.blogService = blogService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");
        String password = request.get("password");

        String token = adminService.login(email, password);

        if (token == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid email or password"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token
        ));
    }
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComments() {

        List<Comment> comments = blogService.getAllComments();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "comments", comments
        ));
    }
    @PostMapping("/approve-comment")
    public ResponseEntity<?> approveComment(
            @RequestBody Map<String, String> request) {

        try {
            Comment comment = blogService.approveComment(request.get("id"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Comment approved successfully",
                    "comment", comment
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/delete-comment")
    public ResponseEntity<?> deleteComment(
            @RequestBody Map<String, String> request) {

        try {
            blogService.deleteComment(request.get("id"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Comment deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {

        Map<String, Object> dashboard = blogService.getDashboardData();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "dashboardData", dashboard
        ));
    }
}
