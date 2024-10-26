package com.kiloflyers.model;


import com.kiloflyers.model.Thumbnails;

public class Image {
  private String id;
  
  private int width;
  
  private int height;
  
  private String url;
  
  private String filename;
  
  private long size;
  
  private String type;
  
  private Thumbnails thumbnails;
  
  public Image() {}
  
  public Image(String id, int width, int height, String url, String filename, long size, String type, Thumbnails thumbnails) {
    this.id = id;
    this.width = width;
    this.height = height;
    this.url = url;
    this.filename = filename;
    this.size = size;
    this.type = type;
    this.thumbnails = thumbnails;
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
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
  
  public String getUrl() {
    return this.url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getFilename() {
    return this.filename;
  }
  
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  public long getSize() {
    return this.size;
  }
  
  public void setSize(long size) {
    this.size = size;
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public Thumbnails getThumbnails() {
    return this.thumbnails;
  }
  
  public void setThumbnails(Thumbnails thumbnails) {
    this.thumbnails = thumbnails;
  }
  
  public String toString() {
    return "Image{id='" + this.id + "', width=" + this.width + ", height=" + this.height + ", url='" + this.url + "', filename='" + this.filename + "', size=" + this.size + ", type='" + this.type + "', thumbnails=" + String.valueOf(this.thumbnails) + "}";
  }
}