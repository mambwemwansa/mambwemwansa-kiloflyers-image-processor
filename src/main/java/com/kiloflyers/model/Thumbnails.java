package com.kiloflyers.model;


import com.kiloflyers.model.ThumbnailDetail;

public class Thumbnails {
  private ThumbnailDetail small;
  
  private ThumbnailDetail large;
  
  private ThumbnailDetail full;
  
  public Thumbnails() {}
  
  public Thumbnails(ThumbnailDetail small, ThumbnailDetail large, ThumbnailDetail full) {
    this.small = small;
    this.large = large;
    this.full = full;
  }
  
  public ThumbnailDetail getSmall() {
    return this.small;
  }
  
  public void setSmall(ThumbnailDetail small) {
    this.small = small;
  }
  
  public ThumbnailDetail getLarge() {
    return this.large;
  }
  
  public void setLarge(ThumbnailDetail large) {
    this.large = large;
  }
  
  public ThumbnailDetail getFull() {
    return this.full;
  }
  
  public void setFull(ThumbnailDetail full) {
    this.full = full;
  }
  
  public String toString() {
    return "Thumbnails{small=" + String.valueOf(this.small) + ", large=" + String.valueOf(this.large) + ", full=" + String.valueOf(this.full) + "}";
  }
}

