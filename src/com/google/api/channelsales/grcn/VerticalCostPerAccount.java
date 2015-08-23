package com.google.api.channelsales.grcn;

public class VerticalCostPerAccount {
  private String vertical;
  private double maxVerticalCost;  // cost of the max spent vertical
  private double totalCost;        // total cost of all verticals
  
  public VerticalCostPerAccount(String vertical, double cost) {
    this.vertical = vertical;
    this.maxVerticalCost = cost;
    this.totalCost = cost;
  }
  
  // Attribute all cost to the biggest vertical
  public void update(String vertical, double cost) {
    if (cost > this.maxVerticalCost) {
      this.vertical = vertical;
      this.maxVerticalCost = cost;
    }
    
    this.totalCost += cost;
  }
  
  public String getVertical() {
    return vertical;
  }
  
  public double getTotalCost() {
    return totalCost;
  }
}

