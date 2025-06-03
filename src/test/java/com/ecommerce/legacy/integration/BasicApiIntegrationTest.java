package com.ecommerce.legacy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasicApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Test
    public void testUserApiEndpoints() {
        // Test user creation
        Map<String, Object> user = new HashMap<>();
        user.put("username", "inttest1");
        user.put("email", "inttest1@example.com");
        user.put("password", "password123");
        user.put("firstName", "Integration");
        user.put("lastName", "Test");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/users", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("id"));
        
        Long userId = Long.valueOf(response.getBody().get("id").toString());

        // Test get user by ID
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/users/" + userId, Map.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("inttest1", getResponse.getBody().get("username"));

        // Test get all users
        ResponseEntity<List> allUsersResponse = restTemplate.getForEntity(baseUrl + "/users", List.class);
        assertEquals(HttpStatus.OK, allUsersResponse.getStatusCode());
        assertTrue(allUsersResponse.getBody().size() > 0);
    }

    @Test
    public void testProductApiEndpoints() {
        // Test product creation
        Map<String, Object> product = new HashMap<>();
        product.put("name", "Integration Test Product");
        product.put("description", "Test Description");
        product.put("price", new BigDecimal("149.99"));
        product.put("sku", "INT-TEST-001");
        product.put("category", "Electronics");
        product.put("stockQuantity", 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(product, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/products", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("id"));
        
        Long productId = Long.valueOf(response.getBody().get("id").toString());

        // Test get product by ID
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/products/" + productId, Map.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("INT-TEST-001", getResponse.getBody().get("sku"));

        // Test get product by SKU
        ResponseEntity<Map> skuResponse = restTemplate.getForEntity(baseUrl + "/products/sku/INT-TEST-001", Map.class);
        assertEquals(HttpStatus.OK, skuResponse.getStatusCode());
        assertEquals("Integration Test Product", skuResponse.getBody().get("name"));

        // Test get all products
        ResponseEntity<Map> allProductsResponse = restTemplate.getForEntity(baseUrl + "/products?page=0&size=10", Map.class);
        assertEquals(HttpStatus.OK, allProductsResponse.getStatusCode());
    }

    @Test
    public void testOrderApiEndpoints() {
        // First create a user and product for the order
        Map<String, Object> user = new HashMap<>();
        user.put("username", "ordertest1");
        user.put("email", "ordertest1@example.com");
        user.put("password", "password123");
        user.put("firstName", "Order");
        user.put("lastName", "Test");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> userRequest = new HttpEntity<>(user, headers);

        ResponseEntity<Map> userResponse = restTemplate.postForEntity(baseUrl + "/users", userRequest, Map.class);
        Long userId = Long.valueOf(userResponse.getBody().get("id").toString());

        Map<String, Object> product = new HashMap<>();
        product.put("name", "Order Test Product");
        product.put("description", "Test Description");
        product.put("price", new BigDecimal("99.99"));
        product.put("sku", "ORDER-TEST-001");
        product.put("category", "Electronics");
        product.put("stockQuantity", 100);

        HttpEntity<Map<String, Object>> productRequest = new HttpEntity<>(product, headers);
        ResponseEntity<Map> productResponse = restTemplate.postForEntity(baseUrl + "/products", productRequest, Map.class);
        Long productId = Long.valueOf(productResponse.getBody().get("id").toString());

        // Test order creation
        String orderUrl = baseUrl + "/orders?userId=" + userId + "&shippingAddress=123 Test Street";
        
        ResponseEntity<Map> orderResponse = restTemplate.postForEntity(orderUrl, null, Map.class);
        assertEquals(HttpStatus.CREATED, orderResponse.getStatusCode());
        assertNotNull(orderResponse.getBody().get("id"));
        
        Long orderId = Long.valueOf(orderResponse.getBody().get("id").toString());

        // Test get order by ID
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/orders/" + orderId, Map.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("PENDING", getResponse.getBody().get("status"));

        // Test get orders by user
        ResponseEntity<List> userOrdersResponse = restTemplate.getForEntity(baseUrl + "/orders/user/" + userId, List.class);
        assertEquals(HttpStatus.OK, userOrdersResponse.getStatusCode());
        assertTrue(userOrdersResponse.getBody().size() > 0);
    }

    @Test
    public void testErrorHandling() {
        // Test 404 for non-existent user
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/users/99999", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Test 404 for non-existent product
        ResponseEntity<Map> productResponse = restTemplate.getForEntity(baseUrl + "/products/99999", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, productResponse.getStatusCode());

        // Test 404 for non-existent order
        ResponseEntity<Map> orderResponse = restTemplate.getForEntity(baseUrl + "/orders/99999", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, orderResponse.getStatusCode());
    }

    @Test
    public void testApiResponseFormats() {
        // Create a test product and verify JSON response structure
        Map<String, Object> product = new HashMap<>();
        product.put("name", "JSON Test Product");
        product.put("description", "Test Description");
        product.put("price", new BigDecimal("29.99"));
        product.put("sku", "JSON-TEST-001");
        product.put("category", "Books");
        product.put("stockQuantity", 25);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(product, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/products", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("id"));
        assertEquals("JSON Test Product", responseBody.get("name"));
        assertEquals("JSON-TEST-001", responseBody.get("sku"));
        assertEquals("Books", responseBody.get("category"));
        assertEquals(25, responseBody.get("stockQuantity"));
    }
}
