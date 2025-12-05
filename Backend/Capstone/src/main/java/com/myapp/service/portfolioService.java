package com.myapp.service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import com.myapp.model.HistoryEntry;
import com.myapp.model.PortfolioUpdateRequest;
import com.myapp.model.Stock;
//import com.amazonaws.services.lambda.runtime.Context;
import com.myapp.model.User;
import com.myapp.model.UserPortfolio;
import com.myapp.model.createStockModel;
import com.myapp.model.updateStockModel;
import com.myapp.repository.portfolioRepository;

@Service
public class portfolioService implements portfolioInterface {

    @Autowired
    private portfolioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public String registerUser(User user) {

        User existing = repo.findByEmail(user.getEmail());
        if (existing != null) {
            return "user-exists";
        }
        
        int userId = ThreadLocalRandom.current().nextInt(0, 100001);
        user.setUserId(userId);

        user.setPassword(encoder.encode(user.getPassword()));

        user.setCreatedAt(Date.valueOf(LocalDate.now()));
        user.setUpdatedAt(Date.valueOf(LocalDate.now()));

        if(user.getRole() == null) {
        	user.setRole("USER");
        }
        repo.save(user);
        return "registered";
    }
    
    public void verifyEmailInSES(String accessKey, String secretKey, String email) {
        try {
            System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

            AmazonSimpleEmailService ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTH_1)
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

            VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest()
                    .withEmailAddress(email);

            ses.verifyEmailIdentity(request);
            System.out.println("SES verification email sent to: " + email);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SES verification failed: " + e.getMessage());
        }
    }

    
    public void sendLoginEmail(String accessKey, String secretKey, String fromEmail, String toEmail, String name) {

        try {
            System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

            AmazonSimpleEmailService ses = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTH_1)
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

            String subject = "Login Notification - Stockify";
            String body = "Hello " + name + ",\n\nWelcome back !!\n\nYou have successfully logged in.\n\nRegards,\nTeam Stockify";

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(toEmail))
                    .withMessage(new Message()
                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
                            .withBody(new Body().withText(new Content().withCharset("UTF-8").withData(body)))
                    )
                    .withSource(fromEmail);

            ses.sendEmail(request);

            System.out.println("Login email sent to " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User loginUser(String email, String password) {

        User user = repo.findByEmail(email);

        if (user == null) {
            return null;
        }

        if (encoder.matches(password, user.getPassword())) {
            return user; 
        }

        return null; //for wrong password
    }

    public List<Stock> getAllStocks(String accessKey, String secretKey, String tableName) {
        try {
            System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTH_1)
                    .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS))
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

            DynamoDB dynamoDB = new DynamoDB(client);
            Table table = dynamoDB.getTable(tableName);

            ItemCollection<ScanOutcome> result = table.scan();
            Iterator<Item> iterator = result.iterator();

            List<Stock> list = new ArrayList<>();

            while (iterator.hasNext()) {
                Item item = iterator.next();
                list.add(toStock(item));
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public static Stock toStock(Item item) {
        Stock stock = new Stock();

        stock.setStockId(item.getString("StockId"));
        stock.setName(item.getString("CompanyName"));
        stock.setSymbol(item.getString("Symbol"));
        stock.setCurrentPrice(item.getNumber("CurrentPrice") != null
                ? item.getNumber("CurrentPrice").doubleValue()
                : null);

        List<Object> historyRaw = item.getList("history");
        List<HistoryEntry> historyList = new ArrayList<>();

        if (historyRaw != null) {
            for (Object obj : historyRaw) {
                if (!(obj instanceof Map)) {
                    continue; // Skip unexpected types
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> innerMap = (Map<String, Object>) obj;

                // Extract "price"
                Object priceObj = innerMap.get("price");
                String priceStr = extractDynamoDBAttributeValue(priceObj);

                // Extract "timestamp" (use "timeStamp" if found alternatively)
                Object tsObj = innerMap.containsKey("timestamp") ? innerMap.get("timestamp") : innerMap.get("timeStamp");
                String tsStr = extractDynamoDBAttributeValue(tsObj);

                // Only add if both price and timestamp were found
                if (priceStr != null && tsStr != null) {
                    try {
                        HistoryEntry h = new HistoryEntry();
                        h.setPrice(Double.parseDouble(priceStr));
                        h.setTimeStamp(tsStr);
                        historyList.add(h);
                    } catch (NumberFormatException ignored) { }
                }
            }
        }

        stock.setHistory(historyList);
        return stock;
    }

    private static String extractDynamoDBAttributeValue(Object attributeObj) {
        if (attributeObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> attrMap = (Map<String, Object>) attributeObj;

            // DynamoDB attribute maps have single entry like {"S": "value"} or {"N": "value"}
            for (Object val : attrMap.values()) {
                if (val != null) {
                    return val.toString();
                }
            }
        } else if (attributeObj instanceof String) {
            return (String) attributeObj;
        }
        return null;
    }


    
    public List<UserPortfolio> getUserPortfolio(String accessKey, String secretKey, String tableName, String userId) {
        try {
            System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
            
            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTH_1)
                    .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS))
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

            DynamoDB dynamoDB = new DynamoDB(client);
            Table table = dynamoDB.getTable(tableName);

            // Query by partition key (UserId)
            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("UserId = :userId")
                    .withValueMap(new ValueMap()
                            .withString(":userId", userId));

            ItemCollection<QueryOutcome> result = table.query(spec);
            Iterator<Item> iterator = result.iterator();

            List<UserPortfolio> portfolioList = new ArrayList<>();

            while (iterator.hasNext()) {
                Item item = iterator.next();
                portfolioList.add(toUserPortfolio(item));
            }

            return portfolioList;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static UserPortfolio toUserPortfolio(Item item) {
        UserPortfolio portfolio = new UserPortfolio();
        
        portfolio.setUserId(item.getString("UserId"));
        portfolio.setStockId(item.getString("StockId"));
        portfolio.setInvestedAmount(
                item.getNumber("investedAmount") != null 
                    ? item.getNumber("investedAmount").doubleValue() 
                    : null
        );
        portfolio.setPurchaseDate(item.getString("purchaseDate"));
        portfolio.setQuantity(
                item.getNumber("quantity") != null 
                    ? item.getNumber("quantity").doubleValue() 
                    : null
        );
        
        return portfolio;
    }
    
    //###################################//
    
	public boolean upsertPortfolioPosition(String accessKey, String secretKey, String tableName, PortfolioUpdateRequest request) {
		
		try {
			System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
			
			BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder
					.standard()
					.withRegion(Regions.AP_SOUTH_1)
					.withCredentials(new AWSStaticCredentialsProvider(creds))
					.build();

			DynamoDB dynamoDB = new DynamoDB(client);
			Table table = dynamoDB.getTable(tableName);

			// Calculate increments
			Double qtyIncrement = request.getQuantity();
			Double amountIncrement = qtyIncrement * request.getPricePerShare();

			// Build complex update expression
			StringBuilder updateExpression = new StringBuilder();
			ValueMap valueMap = new ValueMap();

			// ALWAYS increment totals
			updateExpression.append("ADD investedAmount :amt, quantity :qty ");

			// Update latest purchase date (always set to latest)
			updateExpression.append("SET purchaseDate = :latestDate ");

			// Add increments to value map
			valueMap.withNumber(":amt", amountIncrement);
			valueMap.withNumber(":qty", qtyIncrement);
			valueMap.withString(":latestDate",
					request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now().toString());

			// Atomic UPSERT with composite key
			UpdateItemSpec updateItemSpec = new UpdateItemSpec()
					.withPrimaryKey("UserId", request.getUserId(), "StockId", request.getStockId())
					.withUpdateExpression(updateExpression.toString()).withValueMap(valueMap);

			table.updateItem(updateItemSpec);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addStock(String accessKey, String secretKey, String tableName, createStockModel details) {
		try {
	        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

	        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

	        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
	                .withRegion(Regions.AP_SOUTH_1)
	                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS))
	                .withCredentials(new AWSStaticCredentialsProvider(creds))
	                .build();

	        DynamoDB dynamoDB = new DynamoDB(client);
	        Table table = dynamoDB.getTable(tableName);

	        // Generate StockId from Company Name → uppercase + underscore
	        String stockId = details.getCompanyName().toUpperCase().replace(" ", "_") + Integer.toString((int)(Math.random()*1000000));

//	        String stockId = details.getCompanyName().trim().toUpperCase() + Integer.toString((int)(Math.random()*100/12));
	        
	        // Create initial history entry
	        Map<String, Object> historyEntry1 = Map.of(
	                "price", details.getCurrentPrice().toString(),
	                "timestamp", java.time.Instant.now().toString()
	        );
	        
	        Instant now = Instant.now();
	        Instant oneMinuteLater = now.plusSeconds(60);
	        Map<String, Object> historyEntry2 = Map.of(
	                "price", details.getCurrentPrice().toString(),
	                "timestamp", oneMinuteLater.toString()
	        );
	        
	        List<Map<String, Object>> historyList = new ArrayList<>();
	        historyList.add(historyEntry1);
	        historyList.add(historyEntry2);
	        

	        // Build DynamoDB Item
	        Item item = new Item()
	                .withPrimaryKey("StockId", stockId)
	                .withString("CompanyName", details.getCompanyName())
	                .withNumber("CurrentPrice", details.getCurrentPrice())
	                .withString("Symbol", details.getSymbol())
	                .withList("history", historyList);

	        table.putItem(item);

	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean deleteStock(String accessKey, String secretKey, String tableName, String nestedName,
			String stockId) {
		try {
			System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

			BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1)
					.withCredentials(new AWSStaticCredentialsProvider(creds)).build();

			DynamoDB dynamoDB = new DynamoDB(client);
			Table table1 = dynamoDB.getTable(tableName);
//			Table table2 = dynamoDB.getTable(nestedName);

			DeleteItemOutcome outcome1 = table1.deleteItem("StockId", stockId);
//			DeleteItemOutcome outcome2 = table2.deleteItem("StockId", stockId);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	

	public boolean updateStockFull(String accessKey, String secretKey, String tableName, updateStockModel latestStock) {
		try {
	        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

	        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
	        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
	                .withRegion(Regions.AP_SOUTH_1)
	                .withCredentials(new AWSStaticCredentialsProvider(creds))
	                .build();

	        DynamoDB dynamoDB = new DynamoDB(client);
	        Table table = dynamoDB.getTable("StockTable");

	        // First: Check if item exists
	        Item existingItem = table.getItem("StockId", latestStock.getStockId());
	        if (existingItem == null) {
	            return false; // Stock does not exist
	        }

	        // Update only these 2 fields
	        UpdateItemSpec updateSpec = new UpdateItemSpec()
	                .withPrimaryKey("StockId", latestStock.getStockId())
	                .withUpdateExpression("SET CompanyName = :c, Symbol = :s")
	                .withValueMap(new ValueMap()
	                        .withString(":c", latestStock.getCompanyName())
	                        .withString(":s", latestStock.getSymbol())
	                );

	        table.updateItem(updateSpec);
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public List<User> getAllUsers() {
		return repo.findAll();
	}

	public boolean deleteUserById(String email) {
//		try {
//			repo.deleteById(userId);
//			repo.deleteById(Integer.toString(userId));
			try {
				repo.deleteById(email);
				
				return true;

			} 
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
//			return true;
//		}
//		catch(Exception e) {
//			System.out.println("The error is: " + e.getMessage());
//		}
		
//		return false;
		
	}

	public User updateUser(User incoming) {

	    if (incoming.getEmail() == null) {
	        throw new RuntimeException("Email is required to update user");
	    }

	    User existing = repo.findByEmail(incoming.getEmail());
	    if (existing == null) {
	        throw new RuntimeException("User not found");
	    }

	    // Update fields only if provided
	    if (incoming.getFirstName() != null) existing.setFirstName(incoming.getFirstName());
	    if (incoming.getLastName() != null) existing.setLastName(incoming.getLastName());
	    if (incoming.getUsername() != null) existing.setUsername(incoming.getUsername());
	    if (incoming.getPhone() != null) existing.setPhone(incoming.getPhone());
	    if (incoming.getDateOfBirth() != null) existing.setDateOfBirth(incoming.getDateOfBirth());
	    if (incoming.getRiskAppetite() != null) existing.setRiskAppetite(incoming.getRiskAppetite());
	    if (incoming.getExperience() != null) existing.setExperience(incoming.getExperience());
	    if (incoming.getInvestmentGoal() != null) existing.setInvestmentGoal(incoming.getInvestmentGoal());
	    if (incoming.getRole() != null) existing.setRole(incoming.getRole());

	    // Update password only if a new one is sent
//	    if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
//	        existing.setPassword(encoder.encode(incoming.getPassword()));
//	    }

	    existing.setUpdatedAt(Date.valueOf(LocalDate.now()));

	    return repo.save(existing);
	}
}
