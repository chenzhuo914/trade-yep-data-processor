package com.google.api.channelsales.grcn;

public class AccountsCostPerVertical {
  private int numAccounts;
  private double totalCost;
  
  public AccountsCostPerVertical() {
    this.numAccounts = 0;
    this.totalCost = 0;
  }
  
  public void update(double cost) {
    this.numAccounts++;
    this.totalCost += cost;
  }
  
  public int getNumAccounts() {
    return numAccounts;
  }
  
  public double getTotalCost() {
    return totalCost;
  }
}