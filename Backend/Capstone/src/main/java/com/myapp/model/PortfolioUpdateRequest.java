package com.myapp.model;

public class PortfolioUpdateRequest {
    private String userId;
    private String stockId;
    private Double quantity;        // Amount to ADD
    private Double pricePerShare;   // Latest price for calculations
    private String purchaseDate;    // Latest purchase date (optional)
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getStockId() {
		return stockId;
	}
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	public Double getQuantity() {
		return quantity;
	}
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public Double getPricePerShare() {
		return pricePerShare;
	}
	public void setPricePerShare(Double pricePerShare) {
		this.pricePerShare = pricePerShare;
	}
	public String getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(String purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
}
