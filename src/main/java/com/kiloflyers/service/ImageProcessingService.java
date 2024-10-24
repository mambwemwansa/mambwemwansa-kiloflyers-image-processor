package com.kiloflyers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiloflyers.model.AirtableRecord;
import com.kiloflyers.model.AirtableResponse;
import com.kiloflyers.model.Image;
import com.kiloflyers.service.ImageSegmentationService;
import com.kiloflyers.service.LocalImageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {
	@Value("${airtable.apiKey}")
	private String airtableApiKey;

	@Value("${airtable.baseId}")
	private String airtableBaseId;

	@Value("${airtable.tableName}")
	private String airtableTableName;

	private final ImageSegmentationService imageSegmentationService;

	@Autowired
	LocalImageService localImageService;

	public ImageProcessingService(ImageSegmentationService imageSegmentationService) {
		this.imageSegmentationService = imageSegmentationService;
	}

	public List<AirtableRecord> fetchUnprocessedRecords() {
		String url = String.format("https://api.airtable.com/v0/%s/%s",
				new Object[] { this.airtableBaseId, this.airtableTableName });
		System.out.println("Airtable endpoint: " + url);
		HttpResponse<String> response = ((GetRequest) Unirest.get(url).header("Authorization",
				"Bearer " + this.airtableApiKey)).asString();
		String jsonResponse = (String) response.getBody();
		System.out.println("Raw Airtable Response: " + jsonResponse);
		if (response.getStatus() == 200) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				AirtableResponse airtableResponse = (AirtableResponse) objectMapper.readValue(jsonResponse,
						AirtableResponse.class);
				List<AirtableRecord> records = airtableResponse.getRecords();
				for (AirtableRecord airtableRecord : records) {
					removeBackground(airtableRecord);
					//uploadNoBckImageToAirtable("https://kiloflyers-image-processor-35039419a88c.herokuapp.com/images/kikitoo17_461600438_1230252561528848_4648918935739787266_n.jpg",airtableRecord.getId());
					uploadToBeFramedImageToAirtable(
							"https://wallpapers.com/images/hd/fantastic-image-of-falling-spaceman-40xgzkhbx0i01m7r.jpg",
							airtableRecord.getId());
					uploadFramedImageToAirtable(
							"https://thumbs.dreamstime.com/b/trendy-neon-lighted-wallpaper-background-modern-design-contemporary-art-collage-sea-side-cityscape-bright-colors-207822161.jpg",
							airtableRecord.getId());
				}
				return airtableResponse.getRecords();
			} catch (IOException e) {
				System.err.println("Error deserializing Airtable response: " + e.getMessage());
			}
		} else {
			System.err.println("Error fetching Airtable records: " + response.getStatus());
		}
		return new ArrayList<>();
	}

	public void removeBackground(AirtableRecord record) {
		List<Image> originalImages = record.getFields().getOriginalImage();
		if (originalImages.isEmpty()) {
			System.out.println("No original images found for record: " + String.valueOf(record));
			return;
		}
		String originalImageUrl = ((Image) originalImages.get(0)).getUrl();
		String backgroundRemovedImageUrl = callRemoveBgApi(originalImageUrl,
				((Image) originalImages.get(0)).getFilename());
		System.out.println("Background has been removed succesfully and stored in :" + backgroundRemovedImageUrl);
		uploadNoBckImageToAirtable(backgroundRemovedImageUrl, record.getId());
	}

	private String callRemoveBgApi(String imageUrl, String filename) {
		try {
			byte[] segmentedImage = this.imageSegmentationService.segmentImage(imageUrl, filename);
			String url = this.localImageService.saveImageToStaticFolder(segmentedImage, filename);
			return url;
		} catch (IOException e) {
			System.err.println("Error during image segmentation: " + e.getMessage());
			return null;
		}
	}

	public void updateRecords(List<AirtableRecord> records) {
		String url = "https://api.airtable.com/v0/" + this.airtableBaseId + "/" + this.airtableTableName;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String requestBody = objectMapper.writeValueAsString(records);
			HttpResponse<String> response = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest.patch(url)
					.header("Authorization", "Bearer " + this.airtableApiKey))
					.header("Content-Type", "application/json")).body(requestBody).asString();
			if (response.getStatus() == 200) {
				System.out.println("Records updated successfully: " + (String) response.getBody());
			} else {
				System.out
						.println("Error updating records: " + response.getStatus() + " " + (String) response.getBody());
			}
		} catch (JsonProcessingException e) {
			System.err.println("Error processing JSON: " + e.getMessage());
		}
	}

	public void insertImageRecord(String imageUrl) {
		String url = "https://api.airtable.com/v0/" + this.airtableBaseId + "/" + this.airtableTableName;
		String jsonBody = "{\"fields\": { \"Original image\": [{ \"url\": \"" + imageUrl + "\" }]}}";
		HttpResponse<String> response = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest.post(url)
				.header("Authorization", "Bearer " + this.airtableApiKey)).header("Content-Type", "application/json"))
				.body(jsonBody).asString();
		if (response.getStatus() == 200 || response.getStatus() == 201) {
			System.out.println("Record inserted successfully.");
		} else {
			System.out.println("Error inserting record: " + (String) response.getBody());
		}
	}

	public void uploadNoBckImageToAirtable(String imageUrl, String recordId) {
		try {
			String fieldId = "fldUeqxxTtHg0HpvJ";
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s",
					new Object[] { this.airtableBaseId, this.airtableTableName, recordId });
			HttpResponse<JsonNode> updateResponse = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest
					.patch(updateUrl).header("Authorization", "Bearer " + this.airtableApiKey))
					.header("Content-Type", "application/json"))
					.body(String.format("{\"fields\": {\"%s\": [{\"url\": \"%s\"}]}}",
							new Object[] { fieldId, imageUrl }))
					.asJson();
			if (updateResponse.isSuccess()) {
				System.out.println("Record updated successfully: " + String.valueOf(updateResponse.getBody()));
			} else {
				System.err.println("Error updating record: " + updateResponse.getStatus() + " - "
						+ String.valueOf(updateResponse.getBody()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void uploadToBeFramedImageToAirtable(String imageUrl, String recordId) {
		try {
			String fieldId = "fldwZOo4d53tKkHGL";
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s",
					new Object[] { this.airtableBaseId, this.airtableTableName, recordId });
			HttpResponse<JsonNode> updateResponse = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest
					.patch(updateUrl).header("Authorization", "Bearer " + this.airtableApiKey))
					.header("Content-Type", "application/json"))
					.body(String.format("{\"fields\": {\"%s\": [{\"url\": \"%s\"}]}}",
							new Object[] { fieldId, imageUrl }))
					.asJson();
			if (updateResponse.isSuccess()) {
				System.out.println("Record updated successfully: " + String.valueOf(updateResponse.getBody()));
			} else {
				System.err.println("Error updating record: " + updateResponse.getStatus() + " - "
						+ String.valueOf(updateResponse.getBody()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void uploadFramedImageToAirtable(String imageUrl, String recordId) {
		try {
			String fieldId = "fldU7EqF7da4Qcsce";
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s",
					new Object[] { this.airtableBaseId, this.airtableTableName, recordId });
			HttpResponse<JsonNode> updateResponse = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest
					.patch(updateUrl).header("Authorization", "Bearer " + this.airtableApiKey))
					.header("Content-Type", "application/json"))
					.body(String.format("{\"fields\": {\"%s\": [{\"url\": \"%s\"}]}}",
							new Object[] { fieldId, imageUrl }))
					.asJson();
			if (updateResponse.isSuccess()) {
				System.out.println("Record updated successfully: " + String.valueOf(updateResponse.getBody()));
			} else {
				System.err.println("Error updating record: " + updateResponse.getStatus() + " - "
						+ String.valueOf(updateResponse.getBody()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}