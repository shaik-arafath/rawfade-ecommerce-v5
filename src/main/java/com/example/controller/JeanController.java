package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JeanController {

    @GetMapping("/jean")
    public String jean() {
        return "jean"; // This maps to jean.html in the static folder
    }
}
