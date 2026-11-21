package com.nexa.backend.controller;

import com.nexa.backend.dto.request.CreateProjectRequest;
import com.nexa.backend.dto.response.ProjectResponse;
import com.nexa.backend.service.ProjectService;
import com.nexa.common.dto.ApiResponse;
import com.nexa.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjects(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectResponse> response;
        if (query != null && !query.trim().isEmpty()) {
            response = projectService.searchProjects(principal.getId(), query.trim(), pageable);
        } else {
            response = projectService.getProjects(principal.getId(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ProjectResponse response = projectService.getProject(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        projectService.deleteProject(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}
