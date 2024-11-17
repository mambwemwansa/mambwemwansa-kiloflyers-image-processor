package com.kiloflyers.service;

import com.kiloflyers.service.LocalImageService;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ImageSegmentationService {
	@Value("${photoroom.api.url}")
	private String apiUrl;

	@Value("${photoroom.api.key}")
	private String apiKey;

	@Autowired
	LocalImageService localImageService;

    public byte[] segmentImage(String urlToFile, String filename) throws IOException {
        // Download the image to a temporary file
        File file = downloadImageToTempFile(urlToFile,filename);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found at " + urlToFile);
        }

        // Send the image file in a multipart request with Unirest
        HttpResponse<byte[]> response = Unirest.post(apiUrl)
                .header("x-api-key", apiKey)
                .header("Accept", "image/png")
                .field("image_file", file) // Add the image file to the form data
                .asBytes(); // Expect the response as a byte array

        // Delete the temporary file after processing
        if (!file.delete()) {
            System.err.println("Warning: Temporary file not deleted " + file.getAbsolutePath());
        }

        // Return the image data if the request is successful
        if (response.isSuccess()) {
        	System.out.println("Background removal API response: "+response.getBody());
            return response.getBody();
        } else {
            throw new IOException("Failed to segment image: " + response.getStatusText());
        }
    }

    public File downloadImageToTempFile(String imageUrl, String filename) throws IOException {
        // Generate a unique temporary file with the given prefix and suffix
        File tempFile = File.createTempFile(filename, ".png");

        // Ensure the temporary file is deleted when the program exits
        tempFile.deleteOnExit();

        // Open a connection to the URL and create an InputStream
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream();
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            // Write the input stream to the temporary file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // Return the temporary file
        return tempFile;
    }
}
