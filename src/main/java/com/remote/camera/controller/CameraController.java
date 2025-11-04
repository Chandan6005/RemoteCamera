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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class CameraController {
    private final List<CapturedImage> imageStore = Collections.synchronizedList(new ArrayList<>());
    private final SimpMessagingTemplate messagingTemplate;
    private static final String IMAGES_DIR = "captured_images";

    public CameraController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        // Create captured_images directory if it doesn't exist
        File dir = new File(IMAGES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
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
        File outputFile = new File(IMAGES_DIR + File.separator + timestamp + ".jpg");

        // Ensure parent directory exists
        outputFile.getParentFile().mkdirs();

        // Write the image file
        FileUtils.writeByteArrayToFile(outputFile, data);

        // Create and store the image record
        CapturedImage image = new CapturedImage("/images/" + timestamp + ".jpg", timestamp);
        imageStore.add(image);

        // Notify all clients about the new image
        messagingTemplate.convertAndSend("/topic/newImage", "new");

        return "OK";
    }

    @GetMapping("/images")
    @ResponseBody
    public List<CapturedImage> getImages() {
        return imageStore;
    }
}