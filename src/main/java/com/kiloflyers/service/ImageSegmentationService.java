package com.kiloflyers.service;

import com.kiloflyers.service.LocalImageService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
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

	public byte[] segmentImage(String url_to_file, String filename) throws IOException {
		String pathToFile = this.localImageService.downloadImageToCache(url_to_file, filename);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.parseMediaType("image/png")));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("x-api-key", this.apiKey);
		File file = new File(pathToFile);
		if (!file.exists())
			throw new FileNotFoundException("File not found at " + url_to_file);
		FileSystemResource fileResource = new FileSystemResource(file);
		LinkedMultiValueMap linkedMultiValueMap = new LinkedMultiValueMap();
		linkedMultiValueMap.add("image_file", fileResource);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(linkedMultiValueMap,
				(MultiValueMap) headers);
		ResponseEntity<byte[]> response = this.restTemplate.exchange(this.apiUrl, HttpMethod.POST, requestEntity,
				byte[].class, new Object[0]);
		return (byte[]) response.getBody();
	}
}
