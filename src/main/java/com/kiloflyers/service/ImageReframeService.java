package com.kiloflyers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageReframeService {

	@Autowired
	private LocalImageService localImageService;

	@Value("${base-url}")
	private String baseUrl;

	@Autowired
	private ResourceLoader resourceLoader;

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
	
	
	public String reframeImage(String imageFile, String fileName) throws IOException {
		BufferedImage originalImage = loadImageFromLocalPath(imageFile);
		BufferedImage reframedImage = createReframedImage(originalImage);
		byte[] imageBytes = convertImageToByteArray(reframedImage);

		// save byte array to local repo
		String url = localImageService.getFramedImageURLFromStaticFolder(imageBytes, fileName);
		return url;
	}

	/**
	 * Reframes an image from a URL and saves it to a specified path.
	 * 
	 * @param imageUrl the URL of the image to be reframed
	 * @param fileName the name of the file to save the reframed image as
	 * @return the URL path to the saved image
	 * @throws IOException if an error occurs during image processing
	 */
	public String reframeImageFromUrl(String imagePath, String fileName) throws IOException {
		
		BufferedImage originalImage = loadImageFromLocalPath(imagePath);
		BufferedImage reframedImage = createReframedImage(originalImage);

		saveImage(reframedImage, fileName);
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

		saveImage(reframedImage, fileName);
		return buildImageUrl(fileName, "/framedCropped/");
	}

	private BufferedImage loadImageFromMultipartFile(MultipartFile imageFile) throws IOException {
		return ImageIO.read(imageFile.getInputStream());
	}

	private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
		try {

			System.out.println("Framed Image url to be procesed before encoding :" + imageUrl);
			// Encode the entire URL to handle spaces and special characters
//            String encodedUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString())
//                    .replace("+", "%20"); // Replace "+" with "%20" for spaces
//            System.out.println("Framed Image url to be procesed after encoding :" + encodedUrl);
			// Create a URI from the encoded string
//            URI uri = new URI(imageUrl);
//
//            // Check if the URI is absolute
//            if (!uri.isAbsolute()) {
//                throw new IllegalArgumentException("URI is not absolute: " + imageUrl);
//            }

			// Convert URI to URL
			URL url = new URL(imageUrl);

			// Open a connection to the URL
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(true); // Follow redirects

			// Read the image from the input stream
			return ImageIO.read(connection.getInputStream());
		} catch (IOException e) {
			// Log the exception and the URL
			System.err.println("Error loading image from URL: " + imageUrl);
			e.printStackTrace();
			throw e; // Rethrow the exception if necessary
		} catch (IllegalArgumentException e) {
			// Log and rethrow if the URI is not absolute
			System.err.println(e.getMessage());
			throw new IOException(e);
		}
	}
	private BufferedImage loadImageFromLocalPath(String imageLocalPath) throws IOException {
	    try {
	        System.out.println("Loading image from local path: " + imageLocalPath);

	        // Create a File object with the local path
	        File localFile = new File(imageLocalPath);

	        // Check if the file exists and is indeed a file
	        if (!localFile.exists() || !localFile.isFile()) {
	            throw new IOException("File not found or not a valid file: " + imageLocalPath);
	        }

	        // Read and return the image from the file
	        return ImageIO.read(localFile);

	    } catch (IOException e) {
	        // Log any IOException and rethrow it for handling by the caller
	        System.err.println("Error loading image from local path: " + imageLocalPath);
	        e.printStackTrace();
	        throw e;
	    }
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

	public static byte[] bufferedImageToByteArray(BufferedImage image, String format) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			// Write the BufferedImage to the ByteArrayOutputStream in the specified format
			ImageIO.write(image, format, baos);
			baos.flush(); // Ensure all data is written out
			return baos.toByteArray();
		}
	}

	private void saveImage(BufferedImage image, String fileName) {
		if (image == null) {
			System.err.println("Error: BufferedImage is null. Unable to save the image.");
			return;
		}

		try {
			byte[] imageBytes = bufferedImageToByteArray(image, "png");
			saveFramedImageToStaticFolder(imageBytes,fileName);
			// Use imageBytes as needed, e.g., save to database or send over network
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void saveFramedImageToStaticFolder(byte[] imageBytes, String fileName) throws IOException {
	    // Set the directory to Heroku's temporary filesystem
	    String IMAGE_DIRECTORY = "src/main/resources/static/framed/";

	    // Create the path for the file, using the temporary directory
	    Path filePath = Paths.get(IMAGE_DIRECTORY + fileName);

	    // Create the directory if it doesn’t exist
	    File directory = new File(IMAGE_DIRECTORY);
	    if (!directory.exists()) {
	        directory.mkdirs();
	    }

	    // Write the image to the temporary file
	    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
	        fos.write(imageBytes);
	    } catch (IOException e) {
	        throw new IOException("Error saving the image to temporary storage", e);
	    }

	    // `filePath` points to the temporary file in Heroku's filesystem.
	    // Process or move the image file as needed after saving.
	}

	private String buildImageUrl(String fileName, String folder) {
		return baseUrl + folder + fileName;
	}
}
