<h1 align="center">📈 Stockify - Trading & Portfolio Management Platform</h1>


---

# 👥 Team Information

## 🟥 **Team Name:** Red Team  
## 🧑‍💼 **Team Lead:** Ujjwal  

---

## 👨‍💻 Developers
- Rudraksh  
- Rishabh  
- Adarsh  
- Dev  

---

## 🧪 Testers
- Krithika  

---


<p align="center">
  <b>A cloud-native stock trading simulation + portfolio management system built with React, Spring Boot, AWS Lambda, DynamoDB, and RDS.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Frontend-React-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Database-DynamoDB-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white" />
  <img src="https://img.shields.io/badge/Database-RDS-527FFF?style=for-the-badge&logo=amazonaws&logoColor=white" />
  <br/>
  <img src="https://img.shields.io/badge/Hosted%20On-AWS%20Cloud-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" />
  <img src="https://img.shields.io/badge/Auth-JWT-yellow?style=for-the-badge&logo=jsonwebtokens&logoColor=black" />
</p>

---

# 📘 Overview

This project is a **full-featured stock trading simulation platform** that includes:

- 📊 Live stock updates every minute
- 💼 User portfolio tracking
- 💰 Buy/Sell order system
- 🛡️ JWT authentication with role-based access
- 🧑‍💼 Admin control panel
- 📰 Real-Time News Feed (from external API)
- 🔄 Auto-refresh dashboard

It combines **real-time AWS-backed automation** with a clean React UI and a secure Spring Boot backend.

---

# 🏗️ Architecture Overview

The platform consists of:

1. **Automated Stock Price Updater (AWS Lambda + EventBridge)**
2. **Backend APIs (Spring Boot on Elastic Beanstalk)**
3. **Frontend UI (React on EC2)**
4. **Dual Database System**
   - DynamoDB → High-speed stocks + orders + portfolio
   - RDS → Users + roles + authentication

---

# 🔄 1. Automated Stock Price Update System (AWS)

### ✔ AWS EventBridge

- Triggers every **1 minute**

### ✔ AWS Lambda (Java JAR)

- Runs a Java JAR 10 times × 5 sec cycles
- Generates updated stock prices
- Pushes them to DynamoDB

### ✔ DynamoDB (Stock Table)

- Stores high-velocity price updates
- Read by backend → frontend dashboard

---

# 👤 2. API + User Interaction Flow

## 🛡 A. Authentication (Login/Register)

- React sends login/register request
- Spring Boot validates user via **RDS**
- JWT issued with `role: User/Admin`
- Token stored in frontend (localStorage)

---

## 📊 B. General Stock Access

- Dashboard fetches `/stocks` every few seconds
- JWT validated on backend
- Stock data retrieved from **DynamoDB**

---

## 💼 C. Portfolio Management (User Flow)

### Endpoints

- `POST /buy`
- `POST /sell`
- `GET /portfolio`

### Flow

1. User buys/sells stock
2. Backend performs:
   - Read latest price
   - Update portfolio table
   - Add order history entry
3. DynamoDB stores:
   - Portfolio table
   - Orders table
4. Frontend updates portfolio charts + summary

### Portfolio Dashboard Shows

- Current holdings
- Profit/Loss
- Average buy price
- Transaction history
- Stock-wise allocation

---

## 🧑‍💼 D. Admin APIs

### Admin can:

- Add stocks
- Remove stocks
- Delete users
- Update stock metadata
- Moderate data

### Uses:

- DynamoDB (stock info)
- RDS (user info)

Role verification happens on every admin request.

---

# 📰 News Feature (From Portfolio Management App Guide)

The app integrates **Stock Market News Module** to provide users with market updates.

### News Component Features

- Auto loads latest finance news
- API polling every 10 minutes
- Responsive card layout
- Updates dashboard sidebar

### Code Behavior (Summary)

- Uses React `useEffect()` to fetch news
- Handles API errors & loading states
- Uses reusable `<NewsCard>` component

---

# ⚛ Frontend (React) — Additional Details

### State Management Used

- **Context API** for Auth
- **React state** for portfolio+dashboard
- **Axios interceptors** for JWT auto-attach
- **Custom hooks** for API fetching

### Reusable Components

- `<StockCard />`
- `<NewsCard />`
- `<PortfolioRow />`
- `<AdminTable />`
- `<ProtectedRoute />`

### Performance Improvements

- Cached stock data
- Debounced API calls
- Controlled re-renders
- Lazy-loaded admin panel

---

# 🛠 Backend (Spring Boot) — Additional Details

### Key Backend Modules

#### **1. Auth Module**

- Login/Register
- JWT generation
- Role-based filter

#### **2. Stocks Module**

- Fetch latest stock price
- Fetch stock history

#### **3. Portfolio Module**

- Buy/Sell logic
- Portfolio aggregation
- Order history

#### **4. Admin Module**

- Add/remove stocks
- Delete users
- System maintenance

### Error Handling

- Global Exception Handler
- Custom Exceptions
- 400/401/403/404 mapping

---

# 🗃 Database Structure

## **RDS (MySQL/Postgres)**

### Tables:

- `users`
- `roles`

Stores:

- User login info
- Role: User/Admin

---

## **DynamoDB**

### Tables:

- `StockTable`
- `PortfolioOrdersTable`

Benefits:

- Scalable
- High-speed writes
- Real-time updates
