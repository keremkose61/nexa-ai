package com.nexa.backend.controller;

import com.nexa.backend.dto.response.UploadedFileResponse;
import com.nexa.backend.entity.UploadedFile;
import com.nexa.backend.service.FileService;
import com.nexa.common.dto.ApiResponse;
import com.nexa.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadedFileResponse>> uploadFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) UUID projectId) {
        UploadedFileResponse response = fileService.uploadFile(principal.getId(), projectId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UploadedFileResponse>>> getFiles(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UploadedFileResponse> files = fileService.getFiles(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UploadedFileResponse>> getFileMetadata(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        UploadedFile file = fileService.getFile(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(fileService.mapToResponse(file)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        UploadedFile file = fileService.getFile(principal.getId(), id);
        File physicalFile = new File(file.getFilePath());
        if (!physicalFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(physicalFile);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        fileService.deleteFile(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("File deleted", null));
    }
}
