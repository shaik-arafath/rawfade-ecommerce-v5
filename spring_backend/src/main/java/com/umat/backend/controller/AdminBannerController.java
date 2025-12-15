/*
package com.umat.backend.controller;

import com.umat.backend.model.Banner;
import com.umat.backend.repository.BannerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

    private final BannerRepository bannerRepository;
    private final Path bannerUploadDir;

    public AdminBannerController(
            BannerRepository bannerRepository,
            @Value("${upload.path:./uploads}") String uploadRoot
    ) {
        this.bannerRepository = bannerRepository;
        this.bannerUploadDir = Paths.get(uploadRoot, "banners");
    }

    @PostConstruct
    void ensureUploadDirExists() {
        try {
            Files.createDirectories(bannerUploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare banner upload directory", e);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBanner(
            @RequestParam("image") @NonNull MultipartFile image,
            @RequestParam("title") @NonNull String title,
            @RequestParam("type") @NonNull String type,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "status", defaultValue = "active") String status
    ) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Image file is required"));
        }

        if (!StringUtils.hasText(title) || !StringUtils.hasText(type)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Title and type are required"));
        }

        String originalName = image.getOriginalFilename();
        // Fall back to a default name if originalName is null or empty
        String safeName = (originalName == null || originalName.isBlank()) ? "banner.jpg" : originalName;
        String sanitizedName = safeName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String filename = System.currentTimeMillis() + "_" + sanitizedName;

        try {
            Path target = bannerUploadDir.resolve(filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Banner banner = new Banner();
            banner.setTitle(title);
            banner.setType(type);
            banner.setUrl(url);
            banner.setStatus(status);
            banner.setImagePath("/uploads/banners/" + filename);

            Banner savedBanner = bannerRepository.save(banner);

            return ResponseEntity.ok(savedBanner);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to save banner: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBanners() {
        Iterable<Banner> banners = bannerRepository.findAll();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBannerById(@PathVariable Long id) {
        return bannerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBanner(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "status", required = false) String status
    ) {
        return bannerRepository.findById(id)
                .map(existingBanner -> {
                    try {
                        // Update image if provided
                        if (image != null && !image.isEmpty()) {
                            String originalName = image.getOriginalFilename();
                            String safeName = (originalName == null || originalName.isBlank()) ? "banner.jpg" : originalName;
                            String sanitizedName = safeName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                            String filename = System.currentTimeMillis() + "_" + sanitizedName;

                            Path target = bannerUploadDir.resolve(filename);
                            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                            // Delete old image file if it exists
                            String oldImagePath = existingBanner.getImagePath();
                            if (oldImagePath != null && oldImagePath.startsWith("/uploads/banners/")) {
                                String oldFileName = oldImagePath.substring("/uploads/banners/".length());
                                Path oldFile = bannerUploadDir.resolve(oldFileName);
                                try {
                                    Files.deleteIfExists(oldFile);
                                } catch (IOException e) {
                                    // Log error but don't fail the update
                                    e.printStackTrace();
                                }
                            }

                            existingBanner.setImagePath("/uploads/banners/" + filename);
                        }

                        // Update other fields if provided
                        if (title != null && !title.isBlank()) {
                            existingBanner.setTitle(title);
                        }
                        if (type != null && !type.isBlank()) {
                            existingBanner.setType(type);
                        }
                        if (url != null) {
                            existingBanner.setUrl(url);
                        }
                        if (status != null && !status.isBlank()) {
                            existingBanner.setStatus(status);
                        }

                        Banner updatedBanner = bannerRepository.save(existingBanner);
                        return ResponseEntity.ok(updatedBanner);
                    } catch (IOException e) {
                        return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to update banner: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        return bannerRepository.findById(id)
                .map(banner -> {
                    try {
                        // Delete image file if it exists
                        String imagePath = banner.getImagePath();
                        if (imagePath != null && imagePath.startsWith("/uploads/banners/")) {
                            String fileName = imagePath.substring("/uploads/banners/".length());
                            Path file = bannerUploadDir.resolve(fileName);
                            Files.deleteIfExists(file);
                        }

                        bannerRepository.deleteById(id);
                        return ResponseEntity.ok().build();
                    } catch (IOException e) {
                        return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to delete banner file: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
*/