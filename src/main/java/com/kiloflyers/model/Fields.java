package com.kiloflyers.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Fields {
	@JsonProperty("Original image")
	private List<Image> originalImage;

	@JsonProperty("Photoroom remove BG")
	private List<Image> photoroomRemoveBg;

	@JsonProperty("Name")
	private String name;

	@JsonProperty("To be framed")
	private List<Image> tobeFramed;

	@JsonProperty("Framed")
	private List<Image> framed;
	
	@JsonProperty("Is processed")
	private boolean isProcessed; 
	
	@JsonProperty("Framed Cropped")
	private List<Image> framedCropped;

	public Fields() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Fields(List<Image> originalImage, List<Image> photoroomRemoveBg, String name, List<Image> tobeFramed,
			List<Image> framed, boolean isProcessed, List<Image> framedCropped) {
		super();
		this.originalImage = originalImage;
		this.photoroomRemoveBg = photoroomRemoveBg;
		this.name = name;
		this.tobeFramed = tobeFramed;
		this.framed = framed;
		this.isProcessed = isProcessed;
		this.framedCropped = framedCropped;
	}

	public List<Image> getOriginalImage() {
		return originalImage;
	}

	public void setOriginalImage(List<Image> originalImage) {
		this.originalImage = originalImage;
	}

	public List<Image> getPhotoroomRemoveBg() {
		return photoroomRemoveBg;
	}

	public void setPhotoroomRemoveBg(List<Image> photoroomRemoveBg) {
		this.photoroomRemoveBg = photoroomRemoveBg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Image> getTobeFramed() {
		return tobeFramed;
	}

	public void setTobeFramed(List<Image> tobeFramed) {
		this.tobeFramed = tobeFramed;
	}

	public List<Image> getFramed() {
		return framed;
	}

	public void setFramed(List<Image> framed) {
		this.framed = framed;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public List<Image> getFramedCropped() {
		return framedCropped;
	}

	public void setFramedCropped(List<Image> framedCropped) {
		this.framedCropped = framedCropped;
	}

	@Override
	public String toString() {
		return "Fields [originalImage=" + originalImage + ", photoroomRemoveBg=" + photoroomRemoveBg + ", name=" + name
				+ ", tobeFramed=" + tobeFramed + ", framed=" + framed + ", isProcessed=" + isProcessed
				+ ", framedCropped=" + framedCropped + "]";
	}

	

}
