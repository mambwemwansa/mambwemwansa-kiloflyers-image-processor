package com.kiloflyers.controller;

import com.kiloflyers.model.AirtableRecord;
import com.kiloflyers.service.ImageProcessingService;
import com.kiloflyers.service.ImageReframeService;
import com.kiloflyers.service.ImageSegmentationService;
import com.kiloflyers.service.LocalImageService;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImageController {
  @Autowired
  private ImageProcessingService imageProcessingService;
  @Autowired
  private ImageReframeService imageReframeService;
  @Autowired
  private LocalImageService localImageService;

  
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
  
  
//Mapping of common image extensions to MediaType
  private static final Map<String, MediaType> MEDIA_TYPE_MAP = Map.of(
          "jpg", MediaType.IMAGE_JPEG,
          "jpeg", MediaType.IMAGE_JPEG,
          "png", MediaType.IMAGE_PNG,
          "gif", MediaType.IMAGE_GIF
  );

  @GetMapping("/downloads/{name}")
  public ResponseEntity<ByteArrayResource> getCachedDownloads(@PathVariable String name) {
	  name = URLDecoder.decode(name, StandardCharsets.UTF_8);
      try {
          byte[] imageBytes = localImageService.downloadCache.get(name);

          // Determine media type based on file extension
          String fileExtension = getFileExtension(name);
          MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension.toLowerCase(), MediaType.APPLICATION_OCTET_STREAM);

          ByteArrayResource resource = new ByteArrayResource(imageBytes);
          return ResponseEntity.ok()
                  .contentType(mediaType)
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                  .body(resource);

      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  }
  
  
  @GetMapping("/images/{name}")
  public ResponseEntity<ByteArrayResource> getCachedImage(@PathVariable String name) {
	  name = URLDecoder.decode(name, StandardCharsets.UTF_8);
      try {
          byte[] imageBytes = localImageService.imageCache.get(name);

          // Determine media type based on file extension
          String fileExtension = getFileExtension(name);
          MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension.toLowerCase(), MediaType.APPLICATION_OCTET_STREAM);

          ByteArrayResource resource = new ByteArrayResource(imageBytes);
          return ResponseEntity.ok()
                  .contentType(mediaType)
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                  .body(resource);

      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  }
  
  
  
  @GetMapping("/framed/{name}")
  public ResponseEntity<ByteArrayResource> getCachedFramed(@PathVariable String name) {
	  name = URLDecoder.decode(name, StandardCharsets.UTF_8);
      try {
          byte[] imageBytes = localImageService.framedCache.get(name);

          // Determine media type based on file extension
          String fileExtension = getFileExtension(name);
          MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension.toLowerCase(), MediaType.APPLICATION_OCTET_STREAM);

          ByteArrayResource resource = new ByteArrayResource(imageBytes);
          return ResponseEntity.ok()
                  .contentType(mediaType)
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                  .body(resource);

      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  }
  
  
  @GetMapping("/framedcropped/{name}")
  public ResponseEntity<ByteArrayResource> getCachedFramedCropped(@PathVariable String name) {
	  name = URLDecoder.decode(name, StandardCharsets.UTF_8);
      try {
          byte[] imageBytes = localImageService.framedCroppedCache.get(name);

          // Determine media type based on file extension
          String fileExtension = getFileExtension(name);
          MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension.toLowerCase(), MediaType.APPLICATION_OCTET_STREAM);

          ByteArrayResource resource = new ByteArrayResource(imageBytes);
          return ResponseEntity.ok()
                  .contentType(mediaType)
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                  .body(resource);

      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  }

  
  
  
  
  
  // Helper method to extract file extension
  private String getFileExtension(String fileName) {
      int dotIndex = fileName.lastIndexOf('.');
      return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
  }
}
