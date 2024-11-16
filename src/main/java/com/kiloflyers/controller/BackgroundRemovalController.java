package com.kiloflyers.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
public class BackgroundRemovalController {

//    @Autowired
//    private ImageBackgroundRemovalService backgroundRemovalService;
//
//    @PostMapping("/remove-background")
//    public ResponseEntity<byte[]> removeBackground(@RequestParam("file") MultipartFile file) {
//        try {
//        	//tensorFlowGraphOperations.printGraphOperations();
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            byte[] result = backgroundRemovalService.removeBackground(image);
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reframed_image.png")
//                    .contentType(MediaType.IMAGE_PNG)
//                    .body(result);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
}

