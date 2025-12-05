package com.myapp.model;

public class UserPortfolio {
    private String userId;
    private String stockId;
    private Double investedAmount;
    private String purchaseDate;
    private Double quantity;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getStockId() { return stockId; }
    public void setStockId(String stockId) { this.stockId = stockId; }
    
    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }
    
    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
