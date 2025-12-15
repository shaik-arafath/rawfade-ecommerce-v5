package com.umat.backend.admin;

import com.umat.backend.model.Product;
import com.umat.backend.repository.ProductRepository;
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
public class AdminProductController {

    private final ProductRepository productRepository;
    
    @Value("${upload.path:./uploads}")
    private String uploadDir;

    // Max upload size: 5 MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Handle JSON product creation
    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    // Handle multipart form data product creation with image upload
    @PostMapping(path = "/products", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createProductWithImage(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        try {
            String imagePath = null;
            
            // Handle image upload
            if (images != null && images.length > 0 && images[0] != null && !images[0].isEmpty()) {
                MultipartFile image = images[0]; // Take the first image
                
                // Validate content type and size
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                    return ResponseEntity.badRequest().build();
                }
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    return ResponseEntity.badRequest().build();
                }

                Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path productsDir = uploadBase.resolve("products");
                Files.createDirectories(productsDir);
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "unknown.jpg";
                }
                String safeName = UUID.randomUUID().toString() + "-" + originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path target = productsDir.resolve(safeName);
                try (InputStream in = image.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                imagePath = "/img/products/" + safeName;
            }

            // Create product
            Product product = new Product();
            product.setTitle(title);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setBrand(brand);
            product.setImagePath(imagePath);

            Product saved = productRepository.save(product);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Handle JSON product update
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setTitle(productDetails.getTitle());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());
            product.setBrand(productDetails.getBrand());
            product.setImagePath(productDetails.getImagePath());
            
            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Handle multipart form data product update with image upload
    @PutMapping(path = "/products/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Product product = optionalProduct.get();
        
        try {
            // Handle image upload if provided
            if (images != null && images.length > 0 && images[0] != null && !images[0].isEmpty()) {
                MultipartFile image = images[0]; // Take the first image
                
                // Validate content type and size
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                    return ResponseEntity.badRequest().build();
                }
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    return ResponseEntity.badRequest().build();
                }

                Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path productsDir = uploadBase.resolve("products");
                Files.createDirectories(productsDir);
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "unknown.jpg";
                }
                String safeName = UUID.randomUUID().toString() + "-" + originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path target = productsDir.resolve(safeName);
                try (InputStream in = image.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }

                // Remove old image if present
                if (product.getImagePath() != null && product.getImagePath().startsWith("/img/products/")) {
                    Path old = productsDir.resolve(product.getImagePath().substring("/img/products/".length()));
                    try { Files.deleteIfExists(old); } catch (Exception ex) { /* ignore */ }
                }

                product.setImagePath("/img/products/" + safeName);
            }

            // Update other fields if provided
            if (title != null) product.setTitle(title);
            if (description != null) product.setDescription(description);
            if (price != null) product.setPrice(price);
            if (stock != null) product.setStock(stock);
            if (brand != null) product.setBrand(brand);

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            
            // Delete image file if present
            try {
                if (product.getImagePath() != null && product.getImagePath().startsWith("/img/products/")) {
                    Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
                    Path productsDir = uploadBase.resolve("products");
                    Path old = productsDir.resolve(product.getImagePath().substring("/img/products/".length()));
                    Files.deleteIfExists(old);
                }
            } catch (IOException e) {
                // Log error but don't prevent deletion
                e.printStackTrace();
            }
            
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}