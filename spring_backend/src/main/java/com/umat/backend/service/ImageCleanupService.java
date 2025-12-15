package com.umat.backend.service;

import com.umat.backend.model.Image;
import com.umat.backend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ImageCleanupService {
    
    private static final Logger logger = Logger.getLogger(ImageCleanupService.class.getName());
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Autowired
    private ImageRepository imageRepository;
    
    // Run daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOrphanImages() {
        logger.info("Starting orphan image cleanup job");
        
        try {
            // Create garbage directory if it doesn't exist
            Path garbageDir = Paths.get(uploadPath, "garbage");
            if (!Files.exists(garbageDir)) {
                Files.createDirectories(garbageDir);
            }
            
            // Find images with refCount = 0 (orphans)
            List<Image> orphanImages = imageRepository.findAll().stream()
                    .filter(img -> img.getRefCount() == null || img.getRefCount() == 0)
                    .toList();
            
            int movedCount = 0;
            for (Image image : orphanImages) {
                try {
                    // Move orphaned file to garbage directory with timestamp prefix
                    Path sourcePath = Paths.get(uploadPath, "products", image.getFilename());
                    if (Files.exists(sourcePath)) {
                        String timestamp = LocalDateTime.now().toString().replace(":", "-");
                        Path targetPath = garbageDir.resolve(timestamp + "_" + image.getFilename());
                        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        movedCount++;
                        logger.info("Moved orphan image to garbage: " + image.getFilename());
                    }
                    
                    // Optionally delete the database record for orphan images
                    // imageRepository.delete(image);
                } catch (IOException e) {
                    logger.warning("Failed to move orphan image " + image.getFilename() + ": " + e.getMessage());
                }
            }
            
            logger.info("Orphan image cleanup completed. Moved " + movedCount + " files to garbage directory.");
        } catch (Exception e) {
            logger.severe("Error during orphan image cleanup: " + e.getMessage());
        }
    }
}