/*
package com.umat.backend.admin;

import com.umat.backend.model.Banner;
import com.umat.backend.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class LegacyAdminBannerController {
    private final BannerRepository repo;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // Max upload size: 5 MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");

    public LegacyAdminBannerController(BannerRepository repo) { this.repo = repo; }

    @GetMapping("/banners")
    public List<Banner> getAll() { return repo.findAll(); }

    @GetMapping("/banners/{id}")
    public ResponseEntity<Banner> getById(@PathVariable Long id) {
        Optional<Banner> o = repo.findById(id);
        return o.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Accept multipart/form-data for banner creation (supports 'image' file)
    @PostMapping(path = "/banners/legacy", consumes = {"multipart/form-data"})
    public ResponseEntity<Banner> create(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                // Validate content type and size
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                    return ResponseEntity.badRequest().body(null);
                }
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    return ResponseEntity.badRequest().body(null);
                }

                Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path bannersDir = uploadBase.resolve("banners");
                Files.createDirectories(bannersDir);
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "unknown.jpg";
                }
                String safeName = UUID.randomUUID().toString() + "-" + originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path target = bannersDir.resolve(safeName);
                try (InputStream in = image.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                imagePath = "/img/banners/" + safeName;
            }

            Banner b = new Banner(null, title, imagePath, type, url, status);
            Banner saved = repo.save(b);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Update supports optional multipart image replacement
    @PutMapping(path = "/banners/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Banner> update(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        Optional<Banner> optional = repo.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Banner existing = optional.get();

        try {
            if (image != null && !image.isEmpty()) {
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                    return ResponseEntity.badRequest().body(null);
                }
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    return ResponseEntity.badRequest().body(null);
                }

                Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path bannersDir = uploadBase.resolve("banners");
                Files.createDirectories(bannersDir);
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "unknown.jpg";
                }
                String safeName = UUID.randomUUID().toString() + "-" + originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path target = bannersDir.resolve(safeName);
                try (InputStream in = image.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }

                // Remove old image if present
                if (existing.getImagePath() != null && existing.getImagePath().startsWith("/img/banners/")) {
                    String fileName = existing.getImagePath().substring("/img/banners/".length());
                    Path old = bannersDir.resolve(fileName).normalize();
                    if (old.startsWith(bannersDir)) {
                        Files.deleteIfExists(old);
                    }
                }

                existing.setImagePath("/img/banners/" + safeName);
            }

            // Update other fields if provided
            if (title != null) existing.setTitle(title);
            if (type != null) existing.setType(type);
            if (url != null) existing.setUrl(url);
            if (status != null) existing.setStatus(status);

            Banner saved = repo.save(existing);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Banner> optional = repo.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        
        Banner existing = optional.get();
        
        // Delete image file if present
        try {
            if (existing.getImagePath() != null && existing.getImagePath().startsWith("/img/banners/")) {
                Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path bannersDir = uploadBase.resolve("banners").normalize();
                String fileName = existing.getImagePath().substring("/img/banners/".length());
                Path old = bannersDir.resolve(fileName).normalize();
                if (old.startsWith(bannersDir)) {
                    Files.deleteIfExists(old);
                }
            }
        } catch (IOException e) {
            // Log error but don't prevent deletion
            e.printStackTrace();
        }
        
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
*/