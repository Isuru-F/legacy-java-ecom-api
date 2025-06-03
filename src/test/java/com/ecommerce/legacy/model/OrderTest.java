package com.ecommerce.legacy.model;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class OrderTest {

    private Order order;
    private User testUser;
    private Product testProduct;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(50.00));
        testProduct.setStockQuantity(10);

        order = new Order();
    }

    @Test
    public void testDefaultConstructor() {
        Order newOrder = new Order();
        
        assertNotNull(newOrder.getOrderDate());
        assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus());
        assertEquals(BigDecimal.ZERO, newOrder.getTotalAmount());
        assertNotNull(newOrder.getOrderItems());
        assertTrue(newOrder.getOrderItems().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        String shippingAddress = "123 Test Street";
        Order newOrder = new Order(testUser, shippingAddress);
        
        assertEquals(testUser, newOrder.getUser());
        assertEquals(shippingAddress, newOrder.getShippingAddress());
        assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus());
        assertEquals(BigDecimal.ZERO, newOrder.getTotalAmount());
        assertNotNull(newOrder.getOrderDate());
    }

    @Test
    public void testAddOrderItem() {
        OrderItem orderItem = new OrderItem(order, testProduct, 2, BigDecimal.valueOf(50.00));
        
        order.addOrderItem(orderItem);
        
        assertEquals(1, order.getOrderItems().size());
        assertEquals(order, orderItem.getOrder());
        assertEquals(BigDecimal.valueOf(100.00), order.getTotalAmount());
    }

    @Test
    public void testAddMultipleOrderItems() {
        OrderItem orderItem1 = new OrderItem(order, testProduct, 2, BigDecimal.valueOf(50.00));
        OrderItem orderItem2 = new OrderItem(order, testProduct, 1, BigDecimal.valueOf(30.00));
        
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);
        
        assertEquals(2, order.getOrderItems().size());
        assertEquals(BigDecimal.valueOf(130.00), order.getTotalAmount());
    }

    @Test
    public void testRemoveOrderItem() {
        OrderItem orderItem1 = new OrderItem(order, testProduct, 2, BigDecimal.valueOf(50.00));
        OrderItem orderItem2 = new OrderItem(order, testProduct, 1, BigDecimal.valueOf(30.00));
        
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);
        
        order.removeOrderItem(orderItem1);
        
        assertEquals(1, order.getOrderItems().size());
        assertEquals(BigDecimal.valueOf(30.00), order.getTotalAmount());
        assertNull(orderItem1.getOrder());
    }

    @Test
    public void testRemoveAllOrderItems() {
        OrderItem orderItem1 = new OrderItem(order, testProduct, 2, BigDecimal.valueOf(50.00));
        OrderItem orderItem2 = new OrderItem(order, testProduct, 1, BigDecimal.valueOf(30.00));
        
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);
        
        order.removeOrderItem(orderItem1);
        order.removeOrderItem(orderItem2);
        
        assertEquals(0, order.getOrderItems().size());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    @Test
    public void testGettersAndSetters() {
        Long id = 1L;
        String shippingAddress = "456 Another Street";
        LocalDateTime orderDate = LocalDateTime.now();
        Order.OrderStatus status = Order.OrderStatus.CONFIRMED;
        BigDecimal totalAmount = BigDecimal.valueOf(150.00);

        order.setId(id);
        order.setUser(testUser);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);

        assertEquals(id, order.getId());
        assertEquals(testUser, order.getUser());
        assertEquals(shippingAddress, order.getShippingAddress());
        assertEquals(orderDate, order.getOrderDate());
        assertEquals(status, order.getStatus());
        assertEquals(totalAmount, order.getTotalAmount());
    }

    @Test
    public void testOrderStatusEnum() {
        assertEquals(5, Order.OrderStatus.values().length);
        
        assertTrue(containsStatus(Order.OrderStatus.PENDING));
        assertTrue(containsStatus(Order.OrderStatus.CONFIRMED));
        assertTrue(containsStatus(Order.OrderStatus.SHIPPED));
        assertTrue(containsStatus(Order.OrderStatus.DELIVERED));
        assertTrue(containsStatus(Order.OrderStatus.CANCELLED));
    }

    @Test
    public void testOrderStatusTransitions() {
        order.setStatus(Order.OrderStatus.PENDING);
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        
        order.setStatus(Order.OrderStatus.CONFIRMED);
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        
        order.setStatus(Order.OrderStatus.SHIPPED);
        assertEquals(Order.OrderStatus.SHIPPED, order.getStatus());
        
        order.setStatus(Order.OrderStatus.DELIVERED);
        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    public void testOrderStatusCancellation() {
        order.setStatus(Order.OrderStatus.PENDING);
        order.setStatus(Order.OrderStatus.CANCELLED);
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
    }

    private boolean containsStatus(Order.OrderStatus status) {
        for (Order.OrderStatus s : Order.OrderStatus.values()) {
            if (s == status) {
                return true;
            }
        }
        return false;
    }
}
