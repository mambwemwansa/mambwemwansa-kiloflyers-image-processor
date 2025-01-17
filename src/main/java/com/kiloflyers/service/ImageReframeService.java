package com.kiloflyers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
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

	private static final int CANVAS_WIDTH = 2130;
	private static final int CANVAS_HEIGHT = 2130;

	private static final int TARGET_WIDTH = 4320;
	private static final int TARGET_HEIGHT = 4320;
	private static final int HEAD_TO_CHIN_HEIGHT = 777;
	private static final int EYE_LEVEL_Y = 950;

	private static final int IMAGE_WIDTH = 644;
	private static final int IMAGE_HEIGHT = 777;

	@Autowired
	private ImageCropService imageCropService;

	/**
	 * Reframes an image uploaded as a file and saves it.
	 *
	 * @param imageFile the image file to be reframed
	 * @return byte array of the reframed image
	 * @throws IOException if an error occurs during image processing
	 */
	public byte[] reframeImage(MultipartFile imageFile) throws IOException {
		BufferedImage originalImage = loadImageFromMultipartFile(imageFile);
		BufferedImage reframedImage = mergeImageWithCanvas(originalImage);
		byte[] imageBytes = convertImageToByteArray(reframedImage);

		// byte[] croppedImage = imageCropService.cropAndResizeImage(imageBytes, 2160,
		// 2160);

		return imageBytes;
	}

	public String reframeImage(String imageUrl, String fileName) throws IOException {
		System.out.println("Framing process starting..");
		BufferedImage originalImage = loadImageFromUrl(imageUrl);
		BufferedImage reframedImage = mergeImageWithCanvas(originalImage);
		byte[] imageBytes = convertImageToByteArray(reframedImage);

		// Get the URL after saving the image via LocalImageService
		return localImageService.getFramedImageURLFromCache(imageBytes, fileName);
	}

	/**
	 * Reframes an image from a URL and saves it to a specified path.
	 *
	 * @param imagePath the URL of the image to be reframed
	 * @param fileName  the name of the file to save the reframed image as
	 * @return the URL path to the saved image
	 * @throws IOException if an error occurs during image processing
	 */
	public String reframeImageFromUrl1(String imagePath, String fileName) throws IOException {
		BufferedImage originalImage = loadImageFromLocalPath(imagePath);
		BufferedImage reframedImage = mergeImageWithCanvas(originalImage);

		saveFramedImage(reframedImage, fileName);
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
		BufferedImage reframedImage = mergeImageWithCanvas(originalImage);

		saveFramedCroppedImage(reframedImage, fileName);
		return buildImageUrl(fileName, "/framedcropped/");
	}

	private BufferedImage loadImageFromMultipartFile(MultipartFile imageFile) throws IOException {
		return ImageIO.read(imageFile.getInputStream());
	}

	private BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
		String formattedURL = formatURL(imageUrl);

		final int maxRetries = 3;
		int attempt = 0;
		while (attempt < maxRetries) {
			HttpURLConnection connection = null;
			try {
				System.out.println("Image URL to be processed: " + formattedURL);
				URL url = new URL(formattedURL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setInstanceFollowRedirects(true);
				connection.setConnectTimeout(5000); // 5 seconds connect timeout
				connection.setReadTimeout(5000); // 5 seconds read timeout
				connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // Custom User-Agent

				int responseCode = connection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					throw new IOException("Failed to load image, HTTP response code: " + responseCode);
				}

				// Use BufferedInputStream for better handling of data read
				try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
					return ImageIO.read(in);
				}
			} catch (IOException e) {
				System.err.println("Attempt " + (attempt + 1) + " failed for URL: " + formattedURL);
				e.printStackTrace();
				if (attempt == maxRetries - 1) { // Last attempt, rethrow the exception
					throw e;
				}
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
			attempt++;
		}
		throw new IOException("Failed to load image after " + maxRetries + " attempts for URL: " + imageUrl);
	}

	public String formatURL(String fullURL) {
		try {
			// Find the last slash to separate the base URL and filename
			int lastSlashIndex = fullURL.lastIndexOf("/");
			if (lastSlashIndex == -1) {
				throw new IllegalArgumentException("Invalid URL format");
			}

			// Extract base URL and filename
			String baseURL = fullURL.substring(0, lastSlashIndex + 1);
			String fileName = fullURL.substring(lastSlashIndex + 1);

			// Encode the filename to handle spaces and special characters
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

			// Return the full formatted URL
			return baseURL + encodedFileName;
		} catch (Exception e) {
			System.err.println("Error encoding filename: " + e.getMessage());
			return null;
		}
	}

	private BufferedImage loadImageFromLocalPath(String imageLocalPath) throws IOException {
		Path localFilePath = Paths.get(imageLocalPath);
		if (!Files.exists(localFilePath)) {
			throw new IOException("File not found: " + imageLocalPath);
		}
		return ImageIO.read(localFilePath.toFile());
	}

	private BufferedImage createReframedImage(BufferedImage originalImage) {
		// Set target dimensions to the original image dimensions
		int targetWidth = originalImage.getWidth();
		int targetHeight = originalImage.getHeight();

		// Calculate aspect ratio of the original dimensions
		double aspectRatio = (double) targetWidth / targetHeight;

		// Determine cropping dimensions to match the original aspect ratio
		int cropWidth;
		int cropHeight;
		double originalAspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();

		if (originalAspectRatio > aspectRatio) {
			// If the original image is wider than the target aspect ratio,
			// crop width to match the target aspect ratio, keeping full height
			cropHeight = targetHeight;
			cropWidth = (int) (cropHeight * aspectRatio);
		} else {
			// If the original image is taller than the target aspect ratio,
			// crop height to match the target aspect ratio, keeping full width
			cropWidth = targetWidth;
			cropHeight = (int) (cropWidth / aspectRatio);
		}

		// Calculate starting points for the crop (centered)
		int cropStartX = (originalImage.getWidth() - cropWidth) / 2;
		int cropStartY = (originalImage.getHeight() - cropHeight) / 2;

		// Crop the image to the calculated dimensions
		BufferedImage croppedImage = originalImage.getSubimage(cropStartX, cropStartY, cropWidth, cropHeight);

		// Create a new BufferedImage to scale the cropped image to the target
		// dimensions
		BufferedImage reframedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = reframedImage.createGraphics();

		// Draw the cropped image onto the new BufferedImage, scaling it to fit target
		// dimensions
		graphics.drawImage(croppedImage, 0, 0, targetWidth, targetHeight, null);
		graphics.dispose();

		return reframedImage;
	}

	private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", outputStream);
		return outputStream.toByteArray();
	}

	private void saveFramedImage(BufferedImage image, String fileName) {
		if (image == null) {
			System.err.println("Error: BufferedImage is null. Unable to save the image.");
			return;
		}

		try {
			byte[] imageBytes = bufferedImageToByteArray(image, "png");
			localImageService.saveImageToCache(imageBytes, fileName);
		} catch (IOException e) {
			System.err.println("Failed to save the image: " + fileName);
			e.printStackTrace();
		}
	}

	private void saveFramedCroppedImage(BufferedImage image, String fileName) {
		if (image == null) {
			System.err.println("Error: BufferedImage is null. Unable to save the image.");
			return;
		}

		try {
			byte[] imageBytes = bufferedImageToByteArray(image, "png");
			localImageService.saveFramedCroppedImageToCache(imageBytes, fileName);
		} catch (IOException e) {
			System.err.println("Failed to save the image: " + fileName);
			e.printStackTrace();
		}
	}

	public static byte[] bufferedImageToByteArray(BufferedImage image, String format) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, format, baos);
		baos.flush();
		return baos.toByteArray();
	}

	private String buildImageUrl(String fileName, String folder) {
		return baseUrl + folder + fileName;
	}

	public BufferedImage mergeImageWithCanvas(BufferedImage originalImage) {
		// Define the canvas dimensions (4360x4360) with transparency
		int canvasWidth = 4360;
		int canvasHeight = 4360;

		// Define the target scale factor to make the subject image appear larger on the
		// canvas
		double scaleFactor = 4.0; // Adjust this scale factor to control the size on the canvas

		// Calculate new dimensions for the original image based on the scale factor
		int targetWidth = (int) (originalImage.getWidth() * scaleFactor);
		int targetHeight = (int) (originalImage.getHeight() * scaleFactor);

		// Create a transparent canvas
		BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = canvas.createGraphics();

		// Enable anti-aliasing and transparency for smooth rendering
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite(AlphaComposite.SrcOver);

		// Scale the original image to the target size
		Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

		// Calculate the centered position
		int x = (canvasWidth - targetWidth) / 2;

		// Adjust vertical position to align with desired eye level
		int yOffsetFromTop = 950 - 644; // Offset based on the example's eye level
		int y = yOffsetFromTop;

		// Draw the scaled image onto the canvas at the calculated position
		g.drawImage(scaledImage, x, y, null);
		g.dispose();

		return canvas;
	}

}
