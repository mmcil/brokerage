# Brokerage Firm Backend API

## Project Information
**Tools:** IntelliJ IDEA  
**Programming Language:** Java 24  
**Framework:** Spring Boot 3.5.4  

**Selected Architecture:**  
Layered Architecture (Traditional Spring Boot MVC with Rich Entities)

**Reasons for Choosing Architecture:**
- **Requirements:** CRUD operations with basic business rules  
- **Team Size:** Small team, easier maintenance  
- **Time to Market:** Faster development  
- **Complexity:** No need for complex modeling

  Hexagonal Architecture is not selected due to these reasons in this particular small study.

---

## REST API Endpoints

### 1. OrderController - Core Order Management
- **POST** `/api/orders` – Create a new order  
- **GET** `/api/orders?customerId=CUST001` – List orders (with optional date filtering)  
- **DELETE** `/api/orders/{orderId}?customerId=CUST001` – Cancel pending order  
- **GET** `/api/orders/{orderId}?customerId=CUST001` – Get specific order  

---

### 2. AssetController - Asset Management
- **GET** `/api/assets?customerId=CUST001` – List all customer assets  
- **GET** `/api/assets/{assetName}?customerId=CUST001` – Get specific asset  

---

### 3. AuthController - Customer Authentication *(Bonus 1)*
- **POST** `/api/auth/register` – Register new customer  
- **POST** `/api/auth/login` – Customer login  
- **POST** `/api/auth/logout` – Customer logout  

---

### 4. AdminController - Admin Operations *(Bonus 2)*
- **POST** `/api/admin/match-order` – Match pending order (using DTO)  
- **POST** `/api/admin/orders/{orderId}/match` – Alternative matching endpoint  
- **GET** `/api/admin/pending-orders` – List all pending orders  

---

## Database Access
You can access the H2 in-memory database console at:  
[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---
