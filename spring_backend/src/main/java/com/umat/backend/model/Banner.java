package com.umat.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String type;
    private String url;
    private String status;
    private String imagePath;
}