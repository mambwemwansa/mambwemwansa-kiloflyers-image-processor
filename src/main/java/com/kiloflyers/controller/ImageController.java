package com.kiloflyers.controller;

import com.kiloflyers.model.AirtableRecord;
import com.kiloflyers.service.ImageProcessingService;
import com.kiloflyers.service.ImageReframeService;
import com.kiloflyers.service.ImageSegmentationService;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImageController {
  @Autowired
  private ImageProcessingService imageProcessingService;
  @Autowired
  private ImageReframeService imageReframeService;

  
//  @GetMapping({"/"})
//  public String showImages(Model model) {
//    List<AirtableRecord> records = this.imageProcessingService.fetchUnprocessedRecords();
//    model.addAttribute("records", records);
//    return "index";
//  }
  
  @PostMapping("/reframe")
  public ResponseEntity<byte[]> reframeImage(@RequestParam("file") MultipartFile file) {
      try {
          byte[] reframedImage = imageReframeService.reframeImage(file);
          return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reframed_image.png")
                  .contentType(MediaType.IMAGE_PNG)
                  .body(reframedImage);
      } catch (IOException e) {
          return ResponseEntity.badRequest().build();
      }
  }
}
