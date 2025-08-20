package com.example.ocrproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/api/uploads")
public class UploadController {

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String,String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        Files.createDirectories(Path.of("uploads"));
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .map(StringUtils::getFilenameExtension).orElse("png");
        String name = UUID.randomUUID().toString() + "." + ext;
        Path dest = Path.of("uploads", name);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        Map<String,String> res = new HashMap<>();
        res.put("url", "/uploads/" + name);
        return ResponseEntity.ok(res);
    }
}