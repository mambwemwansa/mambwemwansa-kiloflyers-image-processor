package com.kiloflyers.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class LocalImageService {

    @Value("${base-url}")
    private String baseUrl;

    private static final String IMAGE_DIRECTORY = "/tmp/images/";
    private static final String DOWNLOAD_DIRECTORY = "/tmp/downloads/";
    private static final String FRAMED_DIRECTORY = "/tmp/framed/";
    private static final String FRAMED_CROPPED_DIRECTORY = "/tmp/framedCropped/";

    // Method to save image bytes to static folder
    public String saveImageToStaticFolder(byte[] imageBytes, String fileName) throws IOException {
        return saveToFile(imageBytes, fileName, IMAGE_DIRECTORY);
    }

    // Check if a file exists in classpath
    public boolean doesFileExist(String fileName) {
        final String IMAGE_DIRECTORY = "static/images/";
        ClassPathResource resource = new ClassPathResource(IMAGE_DIRECTORY + fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            return true; // File exists
        } catch (IOException e) {
            return false; // File does not exist
        }
    }

    // Download image to static folder
    public String downloadImageToStaticFolder(String imageUrl, String fileName) throws IOException {
        return downloadToFile(imageUrl, fileName, DOWNLOAD_DIRECTORY);
    }

    // Download image and return URL
    public String downloadImageToStaticFolderreturnURL(String imageUrl, String fileName) throws IOException {
        downloadToFile(imageUrl, fileName, DOWNLOAD_DIRECTORY);
        return this.baseUrl + "/downloads/" + fileName;
    }

    // Save framed image to static folder
    public String saveToFramedImageToStaticFolder(String imageUrl, String fileName) throws IOException {
        System.out.println("Original unframed image being saved to local folder :" + imageUrl);
        downloadToFile(imageUrl, fileName, FRAMED_DIRECTORY);
        System.out.println("Original unframed image saved!");
        return this.baseUrl + "/framed/" + fileName;
    }

    // Get framed image URL from static folder
    public String getFramedImageURLFromStaticFolder(byte[] imageBytes, String fileName) throws IOException {
        return saveToFile(imageBytes, fileName, FRAMED_DIRECTORY);
    }

    // Save framed cropped image to static folder
    public String saveFramedCroppedImageToStaticFolder(byte[] imageBytes, String fileName) throws IOException {
        return saveToFile(imageBytes, fileName, FRAMED_CROPPED_DIRECTORY);
    }

    // Helper method to save file
    private String saveToFile(byte[] data, String fileName, String directory) throws IOException {
        Path filePath = Paths.get(directory + fileName);
        createDirectory(directory);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(data);
            return this.baseUrl + "/images/" + fileName; // Adjust URL return as necessary
        } catch (IOException e) {
            throw new IOException("Error saving file: " + fileName, e);
        }
    }

 // Helper method to download file
    private String downloadToFile(String imageUrl, String fileName, String directory) throws IOException {
        URL url = new URL(imageUrl);
        File targetFile = new File(directory, fileName); // Use comma to separate directory and file name
        createDirectory(directory);
        
        try {
            // Download the file
            FileUtils.copyURLToFile(url, targetFile);
            
            // Check if the file has been downloaded successfully
            if (targetFile.exists()) {
                System.out.println("Download successful: " + targetFile.getAbsolutePath());
                return targetFile.getAbsolutePath(); // Return absolute path
            } else {
                throw new IOException("File not found after download: " + targetFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new IOException("Error downloading file from URL: " + imageUrl, e);
        }
    }

    // Method to create a directory if it doesn't exist
    private void createDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directory created: " + dir.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
    }
}
