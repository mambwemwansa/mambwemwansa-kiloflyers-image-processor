package com.kiloflyers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageReframeService {

    @Autowired
    private LocalImageService localImageService;

    @Value("${base-url}")
    private String baseUrl;

    private static final int TARGET_WIDTH = 2160;
    private static final int TARGET_HEIGHT = 2160;
    private static final int HEAD_TO_CHIN_HEIGHT = 777;
    private static final int EYE_LEVEL_Y = 950;

    /**
     * Reframes an image uploaded as a file and saves it.
     * 
     * @param imageFile the image file to be reframed
     * @return byte array of the reframed image
     * @throws IOException if an error occurs during image processing
     */
    public byte[] reframeImage(MultipartFile imageFile) throws IOException {
        BufferedImage originalImage = loadImageFromMultipartFile(imageFile);
        BufferedImage reframedImage = createReframedImage(originalImage);
        byte[] imageBytes = convertImageToByteArray(reframedImage);
        
        localImageService.saveImageToStaticFolder(imageBytes, "reframed_image.png");
        return imageBytes;
    }

    /**
     * Reframes an image from a URL and saves it to a specified path.
     * 
     * @param imageUrl the URL of the image to be reframed
     * @param fileName the name of the file to save the reframed image as
     * @return the URL path to the saved image
     * @throws IOException if an error occurs during image processing
     */
    public String reframeImageFromUrl(String imageUrl, String fileName) throws IOException {
        BufferedImage originalImage = loadImageFromUrl(imageUrl);
        BufferedImage reframedImage = createReframedImage(originalImage);
        
        saveImage(reframedImage, fileName, "src/main/resources/static/framed/");
        return buildImageUrl(fileName, "/framed/");
    }

    /**
     * Reframes and saves a cropped image from a URL.
     * 
     * @param imageUrl the URL of the image to be reframed
     * @param fileName the name of the file to save the reframed image as
     * @return the URL path to the saved cropped image
     * @throws IOException if an error occurs during image processing
     */
    public String reframeAndSaveCroppedImageFromUrl(String imageUrl, String fileName) throws IOException {
        BufferedImage originalImage = loadImageFromUrl(imageUrl);
        BufferedImage reframedImage = createReframedImage(originalImage);
        
        saveImage(reframedImage, fileName, "src/main/resources/static/framedCropped/");
        return buildImageUrl(fileName, "/framedCropped/");
    }

    private BufferedImage loadImageFromMultipartFile(MultipartFile imageFile) throws IOException {
        return ImageIO.read(imageFile.getInputStream());
    }

    private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        return ImageIO.read(new URL(imageUrl));
    }

    private BufferedImage createReframedImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate scaling factor based on the head-to-chin height
        double scalingFactor = (double) HEAD_TO_CHIN_HEIGHT / (originalHeight / 3.0);
        int scaledWidth = (int) (originalWidth * scalingFactor);
        int scaledHeight = (int) (originalHeight * scalingFactor);

        BufferedImage reframedImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = reframedImage.createGraphics();

        // Calculate position for the scaled image
        int x = (TARGET_WIDTH - scaledWidth) / 2;
        int y = EYE_LEVEL_Y - (scaledHeight / 3); // Centered vertically

        // Draw the scaled image onto the new canvas
        graphics.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        graphics.dispose();

        return reframedImage;
    }

    private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }

    private void saveImage(BufferedImage image, String fileName, String directoryPath) throws IOException {
        Path outputPath = Paths.get(directoryPath, fileName);
        File outputFile = outputPath.toFile();

        // Ensure the directory exists
        outputFile.getParentFile().mkdirs();
        ImageIO.write(image, "png", outputFile);
    }

    private String buildImageUrl(String fileName, String folder) {
        return baseUrl + folder + fileName;
    }
}
