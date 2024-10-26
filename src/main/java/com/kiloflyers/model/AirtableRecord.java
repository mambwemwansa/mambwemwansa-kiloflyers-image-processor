package com.kiloflyers.model;

import com.kiloflyers.model.Fields;

public class AirtableRecord {
  private String id;
  
  private String createdTime;
  
  private Fields fields;
  
  public AirtableRecord() {}
  
  public AirtableRecord(String id, String createdTime, Fields fields) {
    this.id = id;
    this.createdTime = createdTime;
    this.fields = fields;
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getCreatedTime() {
    return this.createdTime;
  }
  
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }
  
  public Fields getFields() {
    return this.fields;
  }
  
  public void setFields(Fields fields) {
    this.fields = fields;
  }
  
  public String toString() {
    return "AirtableRecord{id='" + this.id + "', createdTime='" + this.createdTime + "', fields=" + String.valueOf(this.fields) + "}";
  }
}
