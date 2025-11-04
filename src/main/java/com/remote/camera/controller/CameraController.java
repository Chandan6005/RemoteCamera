package com.remote.camera.controller;

import com.remote.camera.model.CapturedImage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class CameraController {

    private final List<CapturedImage> imageStore = Collections.synchronizedList(new ArrayList<>());

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
        File outputFile = new File("captured_images/" + timestamp + ".jpg");
        FileUtils.writeByteArrayToFile(outputFile, data);

        CapturedImage image = new CapturedImage("/images/" + timestamp + ".jpg", timestamp);
        imageStore.add(image);

        return "OK";
    }

    @GetMapping("/images")
    @ResponseBody
    public List<CapturedImage> getImages() {
        return imageStore;
    }
}