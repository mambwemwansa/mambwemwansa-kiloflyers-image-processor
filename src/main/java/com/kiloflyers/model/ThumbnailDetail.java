package com.kiloflyers.model;

public class ThumbnailDetail {
	  private String url;
	  
	  private int width;
	  
	  private int height;
	  
	  public ThumbnailDetail() {}
	  
	  public ThumbnailDetail(String url, int width, int height) {
	    this.url = url;
	    this.width = width;
	    this.height = height;
	  }
	  
	  public String getUrl() {
	    return this.url;
	  }
	  
	  public void setUrl(String url) {
	    this.url = url;
	  }
	  
	  public int getWidth() {
	    return this.width;
	  }
	  
	  public void setWidth(int width) {
	    this.width = width;
	  }
	  
	  public int getHeight() {
	    return this.height;
	  }
	  
	  public void setHeight(int height) {
	    this.height = height;
	  }
	  
	  public String toString() {
	    return "ThumbnailDetail{url='" + this.url + "', width=" + this.width + ", height=" + this.height + "}";
	  }
	}
