package com.myapp.service;

import org.springframework.boot.jackson.autoconfigure.JacksonProperties.Json;

import com.myapp.model.User;

public interface portfolioInterface {
	public String registerUser(User user);
	public User loginUser(String username, String password);
//	public Json getAllStocks(Object input, Context context);
}
