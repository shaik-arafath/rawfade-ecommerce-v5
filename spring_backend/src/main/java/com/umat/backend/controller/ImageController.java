package com.umat.backend.controller;

import com.umat.backend.model.Image;
import com.umat.backend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private ImageRepository imageRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") @NonNull MultipartFile file) {
        try {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && 
                                        !contentType.equals("image/jpeg") && 
                                        !contentType.equals("image/jpg"))) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only PNG and JPEG images are allowed");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File size must be less than 10MB");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Compute file hash for deduplication
            String hash = computeHash(file.getBytes());
            
            // Check if image with same hash already exists
            var existingImage = imageRepository.findByHash(hash);
            if (existingImage.isPresent()) {
                // Return existing image URL instead of storing duplicate
                return ResponseEntity.ok(new ImageResponse(existingImage.get().getUrl(), existingImage.get().getFilename()));
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath, "products");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Save file to disk
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath);
            
            // Save image metadata to database
            Image image = new Image();
            image.setFilename(filename);
            image.setUrl("/uploads/products/" + filename);
            image.setHash(hash);
            image.setCreatedAt(LocalDateTime.now());
            image.setRefCount(0); // Will be updated when product is created/updated
            
            Image savedImage = imageRepository.save(image);
            
            return ResponseEntity.ok(new ImageResponse(savedImage.getUrl(), savedImage.getFilename()));
        } catch (IOException | NoSuchAlgorithmException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteImage(@PathVariable @NonNull String filename) {
        try {
            // Check if image exists in database
            var imageOpt = imageRepository.findByFilename(filename);
            if (imageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Image image = imageOpt.get();
            
            // Check if image is still referenced
            if (image.getRefCount() != null && image.getRefCount() > 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Cannot delete image that is still in use");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Delete file from disk
            Path filePath = Paths.get(uploadPath, "products", filename);
            Files.deleteIfExists(filePath);
            
            // Delete from database
            imageRepository.delete(image);
            
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Helper method to compute SHA-256 hash of file bytes
    private String computeHash(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        
        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Response DTO
    private static class ImageResponse {
        private String url;
        private String filename;

        public ImageResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }

        @SuppressWarnings("unused")
        public String getUrl() { return url; }
        @SuppressWarnings("unused")
        public String getFilename() { return filename; }
    }
}