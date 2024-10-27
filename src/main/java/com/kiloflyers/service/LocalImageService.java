package com.kiloflyers.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class LocalImageService {

    @Value("${base-url}")
    private String baseUrl;

    // Define cache maps to replace directories
    public final ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, byte[]> downloadCache = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, byte[]> framedCache = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, byte[]> framedCroppedCache = new ConcurrentHashMap<>();

    // Save image bytes to cache instead of static folder
    public String saveImageToCache(byte[] imageBytes, String fileName) {
        imageCache.put(fileName, imageBytes);
        return this.baseUrl + "/images/" + fileName;
    }

    // Check if a file exists in the classpath
    public boolean doesFileExist(String fileName) {
        final String IMAGE_DIRECTORY = "static/images/";
        ClassPathResource resource = new ClassPathResource(IMAGE_DIRECTORY + fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            return true; // File exists
        } catch (IOException e) {
            return false; // File does not exist
        }
    }

    // Download image to cache instead of static folder
    public String downloadImageToCache(String imageUrl, String fileName) throws IOException {
        byte[] imageBytes = downloadImageBytes(imageUrl);
        downloadCache.put(fileName, imageBytes);
        return this.baseUrl + "/downloads/" + fileName;
    }

    // Save framed image to cache instead of static folder
    public String saveFramedImageToCache(String imageUrl, String fileName) throws IOException {
        System.out.println("Original unframed image being saved to cache:" + imageUrl);
        byte[] imageBytes = downloadImageBytes(imageUrl);
        framedCache.put(fileName, imageBytes);
        System.out.println("Original unframed image saved to cache!");
        return this.baseUrl + "/framed/" + fileName;
    }

    // Get framed image URL from cache instead of static folder
    public String getFramedImageURLFromCache(byte[] imageBytes, String fileName) {
        framedCache.put(fileName, imageBytes);
        return this.baseUrl + "/framed/" + fileName;
    }

    // Save framed cropped image to cache instead of static folder
    public String saveFramedCroppedImageToCache(byte[] imageBytes, String fileName) {
        framedCroppedCache.put(fileName, imageBytes);
        return this.baseUrl + "/framedCropped/" + fileName;
    }

    // Helper method to download image bytes from URL
    private byte[] downloadImageBytes(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IOException("Error downloading file from URL: " + imageUrl, e);
        }
    }
}
