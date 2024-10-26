package com.kiloflyers.model;

import com.kiloflyers.model.AirtableRecord;
import java.util.List;

public class AirtableResponse {
  private List<AirtableRecord> records;
  
  public List<AirtableRecord> getRecords() {
    return this.records;
  }
  
  public void setRecords(List<AirtableRecord> records) {
    this.records = records;
  }
}
