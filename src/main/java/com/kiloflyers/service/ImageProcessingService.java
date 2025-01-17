package com.kiloflyers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiloflyers.model.AirtableRecord;
import com.kiloflyers.model.AirtableResponse;
import com.kiloflyers.model.Fields;
import com.kiloflyers.model.Image;
import com.kiloflyers.service.ImageSegmentationService;
import com.kiloflyers.service.LocalImageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {
	@Value("${airtable.apiKey}")
	private String airtableApiKey;

	@Autowired
	private ImageReframeService imageReframeService;
	@Value("${airtable.baseId}")
	private String airtableBaseId;

	@Value("${airtable.tableName}")
	private String airtableTableName;

	@Value("${base-url}")
	private String baseUrl;

	private final ImageSegmentationService imageSegmentationService;

	@Autowired
	LocalImageService localImageService;

	public ImageProcessingService(ImageSegmentationService imageSegmentationService) {
		this.imageSegmentationService = imageSegmentationService;
	}

	@Scheduled(fixedDelay = 5000) // 5000 milliseconds = 5 seconds
	public List<AirtableRecord> fetchUnprocessedRecords() {
	    String url = String.format("https://api.airtable.com/v0/%s/%s", this.airtableBaseId, this.airtableTableName);
	    System.out.println("Airtable endpoint: " + url);

	    HttpResponse<String> response = ((GetRequest) Unirest.get(url)
	            .header("Authorization", "Bearer " + this.airtableApiKey)).asString();
	    String jsonResponse = response.getBody();
	    //System.out.println("Raw Airtable Response: " + jsonResponse);

	    if (response.getStatus() == 200) {
	        ObjectMapper objectMapper = new ObjectMapper();
	        try {
	            AirtableResponse airtableResponse = objectMapper.readValue(jsonResponse, AirtableResponse.class);
	            List<AirtableRecord> records = airtableResponse.getRecords();
	            for (AirtableRecord airtableRecord : records) {
	                Fields fields = airtableRecord.getFields();
	                
	                // Check for null fields
	                if (fields != null) {
	                    List<Image> originalImages = fields.getOriginalImage();

	                    // Check if the record is not processed and images are available
	                    if (!fields.isProcessed() && originalImages != null && !originalImages.isEmpty()) {
	                        // Proceed with processing images
	                        String backgroundRemovedImageUrl = removeBackground(airtableRecord);
	                        framedBackground(airtableRecord);
	                        framedCroppedBackground(airtableRecord, backgroundRemovedImageUrl);
	                        setFileName(airtableRecord);
	                        updateIsProcessed(true, airtableRecord.getId());
	                    } else {
	                        // Logging for cases where images are null or processed
	                        if (originalImages == null) {
	                            System.out.println("No original images found for record ID: " + airtableRecord.getId());
	                        } else if (originalImages.isEmpty()) {
	                            System.out.println("Original images list is empty for record ID: " + airtableRecord.getId());
	                        } else if (fields.isProcessed()) {
	                            //System.out.println("Record ID " + airtableRecord.getId() + " has already been processed.");
	                        }
	                    }
	                } else {
	                    System.out.println("Fields are null for record ID: " + airtableRecord.getId());
	                }
	            }
	            return records; // Return the records regardless of processing
	        } catch (IOException e) {
	            System.err.println("Error deserializing Airtable response: " + e.getMessage());
	        }
	    } else {
	        System.err.println("Error fetching Airtable records: " + response.getStatus());
	    }
	    return new ArrayList<>();
	}

	public String removeBackground(AirtableRecord record) {
		
		List<Image> originalImages = record.getFields().getOriginalImage();
		if (originalImages.isEmpty()) {
			System.out.println("No original images found for record: " + String.valueOf(record));
			return null;
		}
		String  filename=((Image) originalImages.get(0)).getFilename();
		String newExtension = ".png";
        
        // Remove the current extension and add the new one
        String newFileName = filename.replaceFirst("[.][^.]+$", "") + newExtension;
		
		
		
		String originalImageUrl = ((Image) originalImages.get(0)).getUrl();
		
		String backgroundRemovedImageUrl = callRemoveBgApi(originalImageUrl,
				newFileName);
		System.out
				.println("Removed Background  Image has been succesfully and stored in :" + backgroundRemovedImageUrl);
		uploadNoBckImageToAirtable(backgroundRemovedImageUrl, record.getId());

		return backgroundRemovedImageUrl;
	}

	public void framedBackground(AirtableRecord record) {

		
		String finalframedUrl = null;
		String framedUrl  = null;
		List<Image> originalImages = record.getFields().getOriginalImage();
		System.out.println("unframed image now processing :" + ((Image) originalImages.get(0)).getFilename());
		if (originalImages.isEmpty()) {
			System.out.println("No original images found for record: " + String.valueOf(record));
			return;
		}
		String  filename=((Image) originalImages.get(0)).getFilename();
		String newExtension = ".png";
        
        // Remove the current extension and add the new one
        String newFileName = filename.replaceFirst("[.][^.]+$", "") + newExtension;
		String originalImageUrl = ((Image) originalImages.get(0)).getUrl();
		try {
			//framedUrl = localImageService.saveFramedImageToCache1(originalImageUrl,
			//		((Image) originalImages.get(0)).getFilename());
			System.out.println("unframed Image ready to be reframed :" + originalImageUrl);
			
			
			finalframedUrl = imageReframeService.reframeImage(originalImageUrl,
					newFileName);

			System.out.println("Framed Image has been succesfully and stored in :" + finalframedUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		uploadFramedImageToAirtable(finalframedUrl, record.getId());
	}

	public void framedCroppedBackground(AirtableRecord record, String backgroundRemovedImageUrl) {
		String finalframedCroppedUrl = null;
		List<Image> originalImages = record.getFields().getOriginalImage();
		if (originalImages.isEmpty()) {
			System.out.println("No original images found for framed cropped record: " + String.valueOf(record));
			return;
		}

		String  filename=((Image) originalImages.get(0)).getFilename();
		String newExtension = ".png";
        
        // Remove the current extension and add the new one
        String newFileName = filename.replaceFirst("[.][^.]+$", "") + newExtension;
		
		try {
			finalframedCroppedUrl = imageReframeService.reframeAndSaveCroppedImageFromUrl(backgroundRemovedImageUrl,
					newFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("framed cropped record url : " + finalframedCroppedUrl);
		uploadFramedCropped(finalframedCroppedUrl, record.getId());
	}

	public void setFileName(AirtableRecord record) {
		List<Image> originalImages = record.getFields().getOriginalImage();
		if (originalImages.isEmpty()) {
			System.out.println("No original images found for set name record: " + String.valueOf(record));
			return;
		}
		String  filename=((Image) originalImages.get(0)).getFilename();
		String newExtension = ".png";
        
        // Remove the current extension and add the new one
        String newFileName = filename.replaceFirst("[.][^.]+$", "") + newExtension;
		
		updateImageName(newFileName, record.getId());
	}

	private String callRemoveBgApi(String imageUrl, String filename) {
	    
	    System.out.println("Calling background removal API: " + imageUrl);
	    try {
	        Optional<byte[]> optionalSegmentedImage = Optional.ofNullable(localImageService.getImageFromRepo("images", filename));
	        byte[] segmentedImage;

	        if (optionalSegmentedImage.isPresent()) {
	            segmentedImage = optionalSegmentedImage.get();
	        } else {
	            try {
	                segmentedImage = this.imageSegmentationService.segmentImage(imageUrl, filename);
	            } catch (IOException e) {
	                System.err.println("Error during image segmentation: " + e.getMessage());
	                segmentedImage = null;
	            }
	        }

	        if (segmentedImage == null) {
	            return null; // Return null if segmentation failed
	        }

	        String url = this.localImageService.saveImageToCache(segmentedImage, filename);
	        return url;

	    } catch (IOException e) {
	        System.err.println("Error during image processing: " + e.getMessage());
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
				System.out.println("Framed Image URL is: " + imageUrl);
				System.out.println("Record updated successfully uploadFramedImageToAirtable: "
						+ String.valueOf(updateResponse.getBody()));
			} else {
				System.err.println("Error updating record uploadFramedImageToAirtable: " + updateResponse.getStatus()
						+ " - " + String.valueOf(updateResponse.getBody()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateIsProcessed(boolean isProcessed, String recordId) {
		try {
			String fieldId = "fldYLNaQ5lk8gtSYK"; // Replace with the actual field name if needed
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s", this.airtableBaseId,
					this.airtableTableName, recordId);

			// Modify the JSON payload to directly set the boolean value for the field
			HttpResponse<JsonNode> updateResponse = Unirest.patch(updateUrl)
					.header("Authorization", "Bearer " + this.airtableApiKey).header("Content-Type", "application/json")
					.body(String.format("{\"fields\": {\"%s\": %b}}", fieldId, isProcessed)).asJson();

			if (updateResponse.isSuccess()) {
				System.out.println(
						"Record updated successfully uploadFramedImageToAirtable: " + updateResponse.getBody());
			} else {
				System.err.println("Error updating record uploadFramedImageToAirtable: " + updateResponse.getStatus()
						+ " - " + updateResponse.getBody());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateImageName(String filename, String recordId) {
		try {
			String fieldId = "fldxlt7WQiweHlsrL"; // Replace with the actual field name if necessary
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s", this.airtableBaseId,
					this.airtableTableName, recordId);

			// Corrected JSON payload to set the string value for the field
			HttpResponse<JsonNode> updateResponse = Unirest.patch(updateUrl)
					.header("Authorization", "Bearer " + this.airtableApiKey).header("Content-Type", "application/json")
					.body(String.format("{\"fields\": {\"%s\": \"%s\"}}", fieldId, filename)) // Use %s for strings
					.asJson();

			if (updateResponse.isSuccess()) {
				System.out.println("Record File Name: " + filename);
				System.out.println("Record updated successfully with file name: " + updateResponse.getBody());
			} else {
				System.err.println("Error updating record update Image Name ToAirtable: " + updateResponse.getStatus()
						+ " - " + updateResponse.getBody());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void uploadFramedCropped(String imageUrl, String recordId) {
		try {
			String fieldId = "fldWK9WTeGr7Uja7i";
			String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s",
					new Object[] { this.airtableBaseId, this.airtableTableName, recordId });
			HttpResponse<JsonNode> updateResponse = ((HttpRequestWithBody) ((HttpRequestWithBody) Unirest
					.patch(updateUrl).header("Authorization", "Bearer " + this.airtableApiKey))
					.header("Content-Type", "application/json"))
					.body(String.format("{\"fields\": {\"%s\": [{\"url\": \"%s\"}]}}",
							new Object[] { fieldId, imageUrl }))
					.asJson();
			if (updateResponse.isSuccess()) {
				System.out.println("Record updated successfully uploadFramedCroppedImageToAirtable: "
						+ String.valueOf(updateResponse.getBody()));
			} else {
				System.err.println("Error updating record uploadFramedCroppedImageToAirtable: "
						+ updateResponse.getStatus() + " - " + String.valueOf(updateResponse.getBody()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void renameFileOnAirtable(String newImageUrl, String recordId) {
        try {
            String fieldId = "fldm3FcmoMkuyMQX6"; // Update with your actual field ID
            String updateUrl = String.format("https://api.airtable.com/v0/%s/%s/%s",
                    airtableBaseId, airtableTableName, recordId);

            // Perform the update with the new URL
            HttpResponse<JsonNode> updateResponse = Unirest
                    .patch(updateUrl)
                    .header("Authorization", "Bearer " + airtableApiKey)
                    .header("Content-Type", "application/json")
                    .body(String.format("{\"fields\": {\"%s\": [{\"url\": \"%s\"}]}}", fieldId, newImageUrl))
                    .asJson();

            // Check if update was successful
            if (updateResponse.isSuccess()) {
                System.out.println("Record updated successfully with new file URL: " 
                        + updateResponse.getBody());
            } else {
                System.err.println("Error updating record: " 
                        + updateResponse.getStatus() + " - " 
                        + updateResponse.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}