package com.umat.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String filename;
    private String url;
    private String hash; // Optional - for deduplication
    private String uploadedBy; // User who uploaded the image
    private Long productId; // Nullable - which product uses this image
    private java.time.LocalDateTime createdAt;
    private Integer refCount; // Reference count for safe deletion
}