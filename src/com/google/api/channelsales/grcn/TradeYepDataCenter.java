package com.google.api.channelsales.grcn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class TradeYepDataCenter {
  private static final Logger LOGGER = Logger.getLogger(TradeYepDataCenter.class);

  private static String headerGeo      = "Geo";
  private static String headerCid      = "EntityID";
  private static String headerVertical = "Category";
  private static String headerCost     = "Cost USD";
  
  // indices of the fields
  private int iGeo, iCid, iVertical, iCost;
  
  // L1 key: geo, L2 key: cid
  private Map<String, Map<String, VerticalCostPerAccount>> data;
  
  public TradeYepDataCenter(final String[] arrHeaders) {
    List<String> headers = Arrays.asList(arrHeaders);
    
    iGeo = getHeaderIndex(headerGeo, headers);
    iCid = getHeaderIndex(headerCid, headers);
    iVertical = getHeaderIndex(headerVertical, headers);
    iCost = getHeaderIndex(headerCost, headers);
    
    data = new HashMap<String, Map<String, VerticalCostPerAccount>>();
  }
  
  private int getHeaderIndex(String header, List<String> headers) {
    int index = headers.indexOf(header);
    
    if (index == -1) {
      String errMsg = "Failed to locate required header \"" + header + "\" in the CSV file!";
      LOGGER.error(errMsg);
      throw new IllegalArgumentException(errMsg);
    }
    
    return index;
  }
  
  public void processCsvLine(final String[] line) {
    String geo = line[iGeo];
    String cid = line[iCid];
    String vertical = line[iVertical];
    double cost = Double.parseDouble(line[iCost]);
    
    Map<String, VerticalCostPerAccount> innerMap = data.get(geo);
    if (null == innerMap) {
      innerMap = new HashMap<String, VerticalCostPerAccount>();
      data.put(geo, innerMap);
    }
    
    VerticalCostPerAccount entry = innerMap.get(cid);
    if (null == entry) {
      entry = new VerticalCostPerAccount(vertical, cost);
      innerMap.put(cid, entry);
    }
    else {
      entry.update(vertical, cost);
    }
  }
  
  // L1 key: geo, L2 key: vertical
  public Map<String, Map<String, AccountsCostPerVertical>> produceResult() {
    Map<String, Map<String, AccountsCostPerVertical>> result = new HashMap<String, Map<String, AccountsCostPerVertical>>();
    
    for (Map.Entry<String, Map<String, VerticalCostPerAccount>> entry: data.entrySet()) {
      String geo = entry.getKey();
      
      // cid is not important as we just want number of accounts in result
      for (VerticalCostPerAccount value : entry.getValue().values()) {
        String vertical = value.getVertical();
        double cost = value.getTotalCost();
        
        Map<String, AccountsCostPerVertical> l1data = result.get(geo);
        if (null == l1data) {
          l1data = new HashMap<String, AccountsCostPerVertical>();
          result.put(geo, l1data);
        }
        
        AccountsCostPerVertical l2data = l1data.get(vertical);
        if (null == l2data) {
          l2data = new AccountsCostPerVertical();
          l1data.put(vertical, l2data);
        }
        l2data.update(cost);
      }
    }
    
    return result;
  }
}
