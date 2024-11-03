package com.kiloflyers.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kiloflyers.util.LimitedCache;

@Service
public class LocalImageService {

    @Value("${base-url}")
    private String baseUrl;

    // Define caches with a max size of 50 entries each to prevent excessive memory use
    public final LimitedCache<String, byte[]> imageCache = new LimitedCache<>(50);
    public final LimitedCache<String, byte[]> downloadCache = new LimitedCache<>(50);
    public final LimitedCache<String, byte[]> framedCache = new LimitedCache<>(50);
    public final LimitedCache<String, byte[]> framedCroppedCache = new LimitedCache<>(50);

    // Compress and save image bytes to the image cache
    public String saveImageToCache(byte[] imageBytes, String fileName) throws IOException {
        byte[] compressedImageBytes = compressImage(imageBytes);
        imageCache.put(fileName, compressedImageBytes);
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

    // Download and cache compressed image bytes
    public String downloadImageToCache(String imageUrl, String fileName) throws IOException {
        byte[] imageBytes = downloadImageBytes(imageUrl);
        byte[] compressedImageBytes = compressImage(imageBytes);
        downloadCache.put(fileName, compressedImageBytes);
        return this.baseUrl + "/downloads/" + fileName;
    }

    // Save framed image to cache with compression
    public String saveFramedImageToCache(String imageUrl, String fileName) throws IOException {
        System.out.println("Saving framed image to cache: " + imageUrl);
        byte[] imageBytes = downloadImageBytes(imageUrl);
        byte[] compressedImageBytes = compressImage(imageBytes);
        framedCache.put(fileName, compressedImageBytes);
        return this.baseUrl + "/framed/" + fileName;
    }

    // Get framed image URL from cache
    public String getFramedImageURLFromCache(byte[] imageBytes, String fileName) throws IOException {
        byte[] compressedImageBytes = compressImage(imageBytes);
        framedCache.put(fileName, compressedImageBytes);
        return this.baseUrl + "/framed/" + fileName;
    }

    // Save framed cropped image to cache with compression
    public String saveFramedCroppedImageToCache(byte[] imageBytes, String fileName) throws IOException {
        byte[] compressedImageBytes = compressImage(imageBytes);
        framedCroppedCache.put(fileName, compressedImageBytes);
        return this.baseUrl + "/framedcropped/" + fileName;
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

    // Helper method to compress image
    private byte[] compressImage(byte[] imageBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        }
    }

    // Scheduled task to clear all caches every hour
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void clearAllCaches() {
        System.out.println("Clearing all caches to free memory.");
        imageCache.clear();
        downloadCache.clear();
        framedCache.clear();
        framedCroppedCache.clear();
    }
}
