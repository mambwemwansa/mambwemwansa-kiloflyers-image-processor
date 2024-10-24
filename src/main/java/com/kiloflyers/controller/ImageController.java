package com.kiloflyers.controller;

import com.kiloflyers.model.AirtableRecord;
import com.kiloflyers.service.ImageProcessingService;
import com.kiloflyers.service.ImageSegmentationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImageController {
  @Autowired
  private ImageProcessingService imageProcessingService;
  
  @Autowired
  private ImageSegmentationService imageSegmentationService;
  
  @GetMapping({"/"})
  public String showImages(Model model) {
    List<AirtableRecord> records = this.imageProcessingService.fetchUnprocessedRecords();
    model.addAttribute("records", records);
    return "index";
  }
}
