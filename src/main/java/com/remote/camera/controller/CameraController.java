package com.remote.camera.controller;

import com.remote.camera.model.CapturedImage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.FileUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.management.RuntimeErrorException;

@Controller
public class CameraController {
    private final List<CapturedImage> imageStore = Collections.synchronizedList(new ArrayList<>());
    private final SimpMessagingTemplate messagingTemplate;
    private static Path IMAGES_DIR_PATH;

    public CameraController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        // Create captured_images directory if it doesn't exist
        Path railwayVolume = Paths.get("/data/images");
        if (Files.exists(railwayVolume.getParent())) {
            IMAGES_DIR_PATH = railwayVolume;
        } else {
            IMAGES_DIR_PATH = Paths.get("captured_images");
        }

        try {
            Files.createDirectories(IMAGES_DIR_PATH);
            System.out.println("Image storage directory: " + IMAGES_DIR_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create image directory: " + IMAGES_DIR_PATH, e);
        }
    }

    @MessageMapping("/capture")
    @SendTo("/topic/capture")
    public String triggerCapture() {
        return "capture";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String receiveImage(@RequestBody String base64Image) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        base64Image = base64Image.replace("data:image/jpeg;base64,", "");
        byte[] data = Base64.getDecoder().decode(base64Image);
        
        Path outputPath = IMAGES_DIR_PATH.resolve(timestamp + ".jpg");
        File outputFile = outputPath.toFile();

        // Ensure parent directory exists
        outputFile.getParentFile().mkdirs();

        // Write the image file
        FileUtils.writeByteArrayToFile(outputFile, data);

        // Create and store the image record
        CapturedImage image = new CapturedImage("/images/" + timestamp + ".jpg", timestamp);
        imageStore.add(image);

        // Notify all clients about the new image
        messagingTemplate.convertAndSend("/topic/newImage", "new");
        System.out.println("Saved image: " + outputPath.toAbsolutePath());
        return "OK";
    }

    @GetMapping("/images")
    @ResponseBody
    public List<CapturedImage> getImages() {
        return imageStore;
    }
}