package com.kiloflyers.service;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalImageService {
  @Value("${base-url}")
  private String baseUrl;
  
  public String saveImageToStaticFolder(byte[] imageBytes, String fileName) throws IOException {
    String IMAGE_DIRECTORY = "src/main/resources/static/images/";
    Path filePath = Paths.get("src/main/resources/static/images/" + fileName, new String[0]);
    File directory = new File("src/main/resources/static/images/");
    if (!directory.exists())
      directory.mkdirs(); 
    FileOutputStream fos = new FileOutputStream(filePath.toFile());
    try {
      fos.write(imageBytes);
      fos.close();
    } catch (Throwable throwable) {
      try {
        fos.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return this.baseUrl + "/images/" + fileName;
  }
  
  public String downloadImageToStaticFolder(String imageUrl, String fileName) throws IOException {
    String IMAGE_DIRECTORY = "src/main/resources/static/downloads/";
    URL url = new URL(imageUrl);
    File directory = new File("src/main/resources/static/downloads/");
    if (!directory.exists())
      directory.mkdirs(); 
    File targetFile = new File("src/main/resources/static/downloads/" + fileName);
    FileUtils.copyURLToFile(url, targetFile);
    return targetFile.getAbsolutePath();
  }
}
