package com.kiloflyers.service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class ImageBackgroundRemovalService {

    static {
        try {
            // Load the OpenCV library from resources
            String libName = "opencv_java451";  // Adjusting for OpenCV version 4.5.1
            String osName = System.getProperty("os.name").toLowerCase();
            String libFileName = osName.contains("win") ? libName + ".dll" : "lib" + libName + ".so";

            // Attempt to load the OpenCV library from the resources folder
            InputStream inputStream = ImageBackgroundRemovalService.class.getResourceAsStream("/" + libFileName);
            if (inputStream == null) {
                throw new IllegalStateException("Library file not found in resources: " + libFileName);
            }

            // Creating a temporary file for the library
            File tempFile = File.createTempFile("libopencv", ".tmp");
            tempFile.deleteOnExit();  // Ensuring the temp file is deleted on exit
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Load the OpenCV native library
            System.load(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OpenCV library", e);
        }
    }

    public byte[] removeBackground(BufferedImage inputImage) {
        // Convert BufferedImage to OpenCV Mat
        Mat matImage = bufferedImageToMat(inputImage);

        // Convert the image to RGB (OpenCV uses BGR by default)
        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_BGR2RGB);

        // Create a mask for background detection (simple color-based segmentation)
        Mat mask = new Mat(matImage.size(), CvType.CV_8UC1);
        
        // Define the threshold range for the background color
        // You can adjust the color range as per your image's background
        Core.inRange(matImage, new Scalar(200, 200, 200), new Scalar(255, 255, 255), mask); 

        // Invert the mask to keep the foreground
        Core.bitwise_not(mask, mask);

        // Apply the mask to the image
        Mat result = new Mat(matImage.size(), CvType.CV_8UC4);
        matImage.copyTo(result, mask);

        // Convert result back to BufferedImage
        BufferedImage outputImage = matToBufferedImage(result);

        // Convert BufferedImage to byte array and return
        return bufferedImageToByteArray(outputImage, "png");
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        // Extract pixel data from the BufferedImage
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        
        // Create an OpenCV Mat object from the image pixels
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    public BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        BufferedImage bufferedImage = null;

        // Check the Mat type and process accordingly
        if (mat.channels() == 1) {
            // Grayscale image
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        } else if (mat.channels() == 3) {
            // Color image (3 channels: RGB)
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data); // Get the data into the byte array
            bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        } else if (mat.channels() == 4) {
            // Color image with alpha (4 channels: RGBA)
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            byte[] data = new byte[width * height * 4];
            mat.get(0, 0, data); // Get the data into the byte array
            bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        } else {
            throw new UnsupportedOperationException("Mat channels count is not supported for conversion to BufferedImage");
        }
        saveBufferedImage(bufferedImage,"C:\\test_images\\output_image.png");
        return bufferedImage;
    }


    private byte[] bufferedImageToByteArray(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Convert BufferedImage to byte array (PNG format in this case)
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BufferedImage to byte array", e);
        }
    }
    
    
 // Save the BufferedImage to a PNG file to check the output
    public void saveBufferedImage(BufferedImage bufferedImage, String outputPath) {
        try {
            // Ensure that the directory exists
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            // Save the image as a PNG file
            ImageIO.write(bufferedImage, "PNG", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
