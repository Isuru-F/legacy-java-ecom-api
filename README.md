# Legacy E-commerce API

A legacy Java 11 e-commerce API built with Spring Boot 2.x, following traditional enterprise patterns and dependencies commonly used in legacy Java applications.

## Technology Stack

- **Java 11** - Legacy JDK version
- **Spring Boot 2.7.18** - Enterprise web framework
- **Spring Data JPA** - Data persistence layer
- **H2 Database** - In-memory database for development
- **Maven** - Build and dependency management
- **JUnit 4** - Legacy testing framework
- **Mockito** - Mocking framework for unit tests
- **Apache Commons** - Utility libraries (commons-lang3, commons-collections)

## Features

### User Management
- Create, read, update, delete users
- Username and email uniqueness validation
- User authentication endpoints

### Product Management
- Full CRUD operations for products
- Product search by name, category, price range
- Stock management
- Product availability checking

### Order Management
- Order creation and management
- Order status tracking (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- Order item management
- Stock deduction on order confirmation
- Order history by user

## API Endpoints

### Users (`/api/users`)
- `POST /users` - Create a new user
- `GET /users/{id}` - Get user by ID
- `GET /users/username/{username}` - Get user by username
- `GET /users/email/{email}` - Get user by email
- `GET /users` - Get all users
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user
- `GET /users/exists/username/{username}` - Check if username exists
- `GET /users/exists/email/{email}` - Check if email exists

### Products (`/api/products`)
- `POST /products` - Create a new product
- `GET /products/{id}` - Get product by ID
- `GET /products/sku/{sku}` - Get product by SKU
- `GET /products` - Get all products (paginated)
- `GET /products/all` - Get all products (list)
- `GET /products/category/{category}` - Get products by category
- `GET /products/search?name={name}` - Search products by name
- `GET /products/price-range?minPrice={min}&maxPrice={max}` - Get products by price range
- `GET /products/available` - Get available products (stock > 0)
- `GET /products/categories` - Get all product categories
- `PUT /products/{id}` - Update product
- `PUT /products/{id}/stock?stock={quantity}` - Update product stock
- `DELETE /products/{id}` - Delete product
- `GET /products/{id}/availability/{quantity}` - Check product availability

### Orders (`/api/orders`)
- `POST /orders?userId={id}&shippingAddress={address}` - Create a new order
- `POST /orders/{orderId}/items?productId={id}&quantity={qty}` - Add item to order
- `GET /orders/{id}` - Get order by ID
- `GET /orders` - Get all orders
- `GET /orders/user/{userId}` - Get orders by user
- `GET /orders/user/{userId}/paginated` - Get orders by user (paginated)
- `GET /orders/status/{status}` - Get orders by status
- `GET /orders/date-range?startDate={start}&endDate={end}` - Get orders by date range
- `PUT /orders/{id}/status?status={status}` - Update order status
- `PUT /orders/{id}/confirm` - Confirm order
- `PUT /orders/{id}/ship` - Ship order
- `PUT /orders/{id}/deliver` - Deliver order
- `PUT /orders/{id}/cancel` - Cancel order
- `DELETE /orders/{id}` - Delete order

## Project Structure

```
src/
├── main/
│   ├── java/com/ecommerce/legacy/
│   │   ├── LegacyEcommerceApplication.java
│   │   ├── controller/
│   │   │   ├── UserController.java
│   │   │   ├── ProductController.java
│   │   │   └── OrderController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── ProductService.java
│   │   │   └── OrderService.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderItemRepository.java
│   │   └── model/
│   │       ├── User.java
│   │       ├── Product.java
│   │       ├── Order.java
│   │       └── OrderItem.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/ecommerce/legacy/
        ├── service/
        │   ├── UserServiceTest.java
        │   └── ProductServiceTest.java
        └── controller/
            └── UserControllerTest.java
```

## Getting Started

### Prerequisites
- Java 22 or higher
- Maven 3.6+

### Running the Application

1. Clone the repository
```bash
git clone <repository-url>
cd legacy-java-amp-test
```

2. Build the project
```bash
mvn clean compile
```

3. Run tests
```bash
mvn test
```

4. Start the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### H2 Database Console

Access the H2 database console at: `http://localhost:8080/api/h2-console`

- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave blank)

## Legacy Patterns

This project demonstrates typical legacy Java enterprise patterns:

- **Traditional layered architecture** (Controller → Service → Repository → Model)
- **JPA annotations** with XML-style configuration
- **Manual exception handling** in controllers
- **HashMap-based response objects** instead of modern DTOs
- **JUnit 4** testing patterns with `@RunWith`
- **Mockito** with field injection using `@Mock` and `@InjectMocks`
- **Apache Commons** utility usage
- **Verbose getter/setter** patterns
- **Manual validation** logic in services

## Database Schema

The application uses the following entities:

- **Users**: User account information
- **Products**: Product catalog with pricing and inventory
- **Orders**: Customer orders with status tracking
- **OrderItems**: Individual items within orders

## Business Logic

- Users can place orders for multiple products
- Stock is automatically decremented when orders are confirmed
- Orders follow a state machine: PENDING → CONFIRMED → SHIPPED → DELIVERED
- Orders can be cancelled if not yet shipped
- Stock is restored when orders are cancelled
