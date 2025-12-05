package com.myapp.controller;

import com.myapp.model.PortfolioUpdateRequest;
import com.myapp.model.Stock;
import com.myapp.model.User;
import com.myapp.model.UserPortfolio;
import com.myapp.model.loginUserTemplate;
import com.myapp.service.portfolioService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.myapp.config.JwtUtil;
import com.myapp.model.*;
import com.myapp.service.portfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:3000")
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
//@CrossOrigin(origins = "*",allowedHeaders = "*",methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class portfolioController {

    @Autowired
    private portfolioService service;
    
    @Autowired
    private com.myapp.config.MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

    	String accessKey = "Give Your Access Key";
        String secretKey = "Give Your Secret Access Key";
                
        String status = service.registerUser(user);

        if ("registered".equals(status)) {
        	String toEmail = user.getEmail().toLowerCase();
        	String name = user.getFirstName() + " " + user.getLastName();
        	String topicARN = "Give your Topic ARN Link";
        	
        	 // Generate JWT
            //String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        	
        	try{
        		service.verifyEmailInSES(accessKey, secretKey, toEmail);
        	}
        	catch(Exception e) {
        		System.out.println("The email error is: " + e.getMessage());
        	}
            return ResponseEntity.ok(user);
        } 
        else if ("user-exists".equals(status)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("User already exists");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Unknown error occurred");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody loginUserTemplate credentials) {
    	
    	String accessKey = "Give Your Access Key";
        String secretKey = "Give Your Secret Access Key";
        String fromEmail = "give from mail";

        String toemail = credentials.getEmail().toLowerCase();
        String password = credentials.getPassword();
//        System.out.println("Email: " + toemail + " and Password: " + password);
        
        User user = service.loginUser(toemail, password);
        String token = "";
        if (user == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }
        else {
        	try {
//        		token = JwtUtil.generateToken(user.getEmail(), user.getRole());
        		UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
                token = JwtUtil.generateToken(ud, user.getRole());
                
        		String name = user.getFirstName() + " " + user.getLastName();
        		service.sendLoginEmail(accessKey, secretKey, fromEmail, toemail, name);
        	}
        	catch(Exception e) {
        		System.out.println("The email error is: " + e.getMessage());
        	}
//            return ResponseEntity.ok(user);
        	
        	 return ResponseEntity.ok(Map.of(
        	            "token", token,
        	            "user", user
        	    ));
        }   
    }
    
    @GetMapping("/stocks")
    public ResponseEntity<?> getAllStocks() {

        try {
            String accessKey = "Give Your Access Key";
            String secretKey = "Give Your Secret Access Key";
            String tableName = "StockTable";

            if (accessKey == null || secretKey == null || tableName == null) {
                return ResponseEntity.badRequest().body("Missing required fields: accessKey, secretKey, tableName");
            }

            List<Stock> stocks = service.getAllStocks(accessKey, secretKey, tableName);

            return ResponseEntity.ok(stocks);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to fetch stocks: " + e.getMessage());
        }
    }
    
    @PostMapping("admin/stocks/add")
    public ResponseEntity<?> createStocks(@RequestBody createStockModel details){
//    	String stockId = details.getCompanyName().trim().toUpperCase() + Integer.toString((int)(Math.random()*100/12));
    	try {
            String accessKey = "Give Your Access Key";
            String secretKey = "Give Your Secret Access Key";
            String tableName = "StockTable";

            boolean result = service.addStock(accessKey, secretKey, tableName, details);

            if (result) {
                return ResponseEntity.ok("Stock added successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to add stock");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    
    @DeleteMapping("/admin/deleteStock/{stockId}")
    public ResponseEntity<?> deleteStock(@PathVariable String stockId) {

        String accessKey = "Give Your Access Key";
        String secretKey = "Give Your Secret Access Key";
        String tableName = "StockTable";
        String nestedTable = "OrderPortfolioTable";

        boolean deleted = service.deleteStock(accessKey, secretKey, tableName, nestedTable, stockId);

        if (deleted) {
            return ResponseEntity.ok("Stock deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Stock not found or delete failed");
        }
    }

	
    @PutMapping("/admin/stocks/update") 
    public ResponseEntity<?> updateStock(@RequestBody updateStockModel latestStock) { 
    	try { 
    		String accessKey = "Give Your Access Key";
    		String secretKey = "Give Your Secret Access Key"; 
    		String tableName = "StockTable";
	  
			boolean updated = service.updateStockFull(accessKey, secretKey, tableName, latestStock);
			  
			if (updated) { 
				 return ResponseEntity.ok("Stock updated successfully"); 
			} 
			else{ 
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stock not found");
			}	  
		} 
    	catch (Exception e) { 
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating stock: " + e.getMessage()); 
    	} 
    }
	 
    @GetMapping("/admin/allUsers")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = service.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching users: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/deleteUser/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        try {
            boolean deleted = service.deleteUserById(email);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    @PutMapping("/admin/user/update")
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser) {

        try {
            User savedUser = service.updateUser(updatedUser);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/user-portfolio/{userId}")
    public ResponseEntity<?> getUserPortfolio(@PathVariable String userId) {
        try {
            String accessKey = "Give Your Access Key";
            String secretKey = "Give Your Secret Access Key";
            String tableName = "OrderPortfolioTable"; // Replace with your table name

            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("UserId is required");
            }

            List<UserPortfolio> portfolio = service.getUserPortfolio(accessKey, secretKey, tableName, userId);
            return ResponseEntity.ok(portfolio);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to fetch user portfolio: " + e.getMessage());
        }
    }
    
    @PostMapping("/portfolio/update")
    public ResponseEntity<?> updatePortfolio(@Valid @RequestBody PortfolioUpdateRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            if (request.getStockId() == null || request.getStockId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("stockId is required");
            }
            if (request.getQuantity() == null || request.getPricePerShare() == null) {
                return ResponseEntity.badRequest().body("quantity and pricePerShare are required");
            }

            String accessKey = "Give Your Access Key";
            String secretKey = "Give Your Secret Access Key";
            String tableName = "OrderPortfolioTable";

            boolean updated = service.upsertPortfolioPosition(accessKey, secretKey, tableName, request);
            
            return ResponseEntity.ok(Map.of(
                "success", updated, 
                "message", updated ? "Portfolio updated successfully" : "New position created"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Update failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/news")
    public ResponseEntity<?> getNews() {
        try {
            String apiKey = "Give your api key"; // your NewsAPI key
            String url = "https://newsapi.org/v2/top-headlines?country=us&category=business&apiKey=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("articles")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No articles found");
            }

            List<?> articles = (List<?>) response.get("articles");

            // Return first 100 articles
            List<?> top100 = articles.size() > 100 ? articles.subList(0, 100) : articles;

            return ResponseEntity.ok(top100);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to fetch news: " + e.getMessage());
        }
    }

}
