package com.myapp.model;

import java.util.List;

public class Stock {
    private String StockId;
    private String CompanyName;
    private String symbol;
    private Double CurrentPrice;
    private List<HistoryEntry> history;
	public String getStockId() {
		return StockId;
	}
	public void setStockId(String stockId) {
		StockId = stockId;
	}
	public String getName() {
		return CompanyName;
	}
	public void setName(String name) {
		this.CompanyName = name;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getCurrentPrice() {
		return CurrentPrice;
	}
	public void setCurrentPrice(Double currentPrice) {
		CurrentPrice = currentPrice;
	}
	public List<HistoryEntry> getHistory() {
		return history;
	}
	public void setHistory(List<HistoryEntry> history) {
		this.history = history;
	}
}

