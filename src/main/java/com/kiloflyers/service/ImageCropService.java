package com.kiloflyers.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageCropService {


	
	/**
     * Crops and resizes an image provided as a byte array, maintaining the original dimensions.
     *
     * @param imageData Byte array of the source image.
     * @param cropWidth Width of the cropping area.
     * @param cropHeight Height of the cropping area.
     * @return Byte array of the cropped and resized image in PNG format.
     * @throws IOException if an error occurs during processing.
     */
    public byte[] cropAndResizeImage(byte[] imageData, int cropWidth, int cropHeight) throws IOException {
        // Step 1: Convert byte array to BufferedImage
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(inputStream);

        // Step 2: Calculate crop area, centered within the original image
        int x = (originalImage.getWidth() - cropWidth) / 2;
        int y = (originalImage.getHeight() - cropHeight) / 2;

        // Ensure crop area fits within the original dimensions
        BufferedImage croppedImage = originalImage.getSubimage(x, y, Math.min(cropWidth, originalImage.getWidth()), Math.min(cropHeight, originalImage.getHeight()));

        // Step 3: Resize the cropped image back to original dimensions
        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(croppedImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
        g2d.dispose();

        // Step 4: Convert the resulting BufferedImage back to a byte array in PNG format
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);

        return outputStream.toByteArray();
    }
}