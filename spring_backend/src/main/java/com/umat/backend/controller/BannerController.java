package com.umat.backend.controller;

import com.umat.backend.model.Banner;
import com.umat.backend.repository.BannerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerRepository bannerRepository;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping
    public List<Banner> getAllActiveBanners() {
        return bannerRepository.findAll();
    }
}