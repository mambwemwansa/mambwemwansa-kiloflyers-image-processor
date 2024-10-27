package com.kiloflyers.service;

import com.kiloflyers.service.LocalImageService;
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

	private final RestTemplate restTemplate;

	public ImageSegmentationService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@SuppressWarnings({ "unchecked" })
	public byte[] segmentImage(String url_to_file, String filename) throws IOException {

		String urlToFile = this.localImageService.downloadImageToCache(url_to_file, filename);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.parseMediaType("image/png")));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("x-api-key", this.apiKey);

		File file = downloadImageToTempFile(url_to_file);
		if (!file.exists())
			throw new FileNotFoundException("File not found at " + url_to_file);
		FileSystemResource fileResource = new FileSystemResource(file);
		@SuppressWarnings("rawtypes")
		LinkedMultiValueMap linkedMultiValueMap = new LinkedMultiValueMap();
		linkedMultiValueMap.add("image_file", fileResource);
		@SuppressWarnings("rawtypes")
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(linkedMultiValueMap,
				(MultiValueMap) headers);
		ResponseEntity<byte[]> response = this.restTemplate.exchange(this.apiUrl, HttpMethod.POST, requestEntity,
				byte[].class, new Object[0]);
		return (byte[]) response.getBody();
	}

	public File downloadImageToTempFile(String imageUrl) throws IOException {
		// Generate a unique temporary file with the given prefix and suffix
		File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".png");

		// Ensure the temporary file is deleted when the program exits
		tempFile.deleteOnExit();

		// Open a connection to the URL and create an InputStream
		URL url = new URL(imageUrl);
		try (InputStream inputStream = url.openStream(); OutputStream outputStream = new FileOutputStream(tempFile)) {

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
