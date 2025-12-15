package com.umat.backend.repository;

import com.umat.backend.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByHash(String hash);
    Optional<Image> findByFilename(String filename);
}