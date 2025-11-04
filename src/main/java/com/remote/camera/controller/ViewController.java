package com.remote.camera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index"; // looks for templates/index.html
    }

    @GetMapping("/camera")
    public String camera() {
        // serve the static camera page at /camera.html
        return "redirect:/camera.html";
    }

    @GetMapping("/control")
    public String control() {
        // serve the static controller page at /controller.html
        return "redirect:/controller.html";
    }
}
