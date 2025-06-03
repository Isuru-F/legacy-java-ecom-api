package com.ecommerce.legacy.model;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class OrderItemTest {

    private OrderItem orderItem;
    private Order testOrder;
    private Product testProduct;
    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setShippingAddress("123 Test Street");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(25.99));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");

        orderItem = new OrderItem();
    }

    @Test
    public void testDefaultConstructor() {
        OrderItem newOrderItem = new OrderItem();
        
        assertNull(newOrderItem.getId());
        assertNull(newOrderItem.getOrder());
        assertNull(newOrderItem.getProduct());
        assertNull(newOrderItem.getQuantity());
        assertNull(newOrderItem.getPrice());
    }

    @Test
    public void testParameterizedConstructor() {
        Integer quantity = 3;
        BigDecimal price = BigDecimal.valueOf(25.99);
        
        OrderItem newOrderItem = new OrderItem(testOrder, testProduct, quantity, price);
        
        assertEquals(testOrder, newOrderItem.getOrder());
        assertEquals(testProduct, newOrderItem.getProduct());
        assertEquals(quantity, newOrderItem.getQuantity());
        assertEquals(price, newOrderItem.getPrice());
    }

    @Test
    public void testGettersAndSetters() {
        Long id = 1L;
        Integer quantity = 5;
        BigDecimal price = BigDecimal.valueOf(19.99);

        orderItem.setId(id);
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(price);

        assertEquals(id, orderItem.getId());
        assertEquals(testOrder, orderItem.getOrder());
        assertEquals(testProduct, orderItem.getProduct());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(price, orderItem.getPrice());
    }

    @Test
    public void testOrderItemWithDifferentQuantities() {
        OrderItem item1 = new OrderItem(testOrder, testProduct, 1, BigDecimal.valueOf(25.99));
        OrderItem item2 = new OrderItem(testOrder, testProduct, 10, BigDecimal.valueOf(25.99));
        
        assertEquals(Integer.valueOf(1), item1.getQuantity());
        assertEquals(Integer.valueOf(10), item2.getQuantity());
        assertEquals(item1.getPrice(), item2.getPrice());
    }

    @Test
    public void testOrderItemWithDifferentPrices() {
        OrderItem item1 = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(25.99));
        OrderItem item2 = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(19.99));
        
        assertEquals(item1.getQuantity(), item2.getQuantity());
        assertNotEquals(item1.getPrice(), item2.getPrice());
        assertEquals(BigDecimal.valueOf(25.99), item1.getPrice());
        assertEquals(BigDecimal.valueOf(19.99), item2.getPrice());
    }

    @Test
    public void testOrderItemAssociations() {
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        
        assertEquals(testOrder, orderItem.getOrder());
        assertEquals(testProduct, orderItem.getProduct());
        assertEquals(testUser, orderItem.getOrder().getUser());
        assertEquals("Test Product", orderItem.getProduct().getName());
    }

    @Test
    public void testOrderItemNullAssociations() {
        orderItem.setOrder(null);
        orderItem.setProduct(null);
        
        assertNull(orderItem.getOrder());
        assertNull(orderItem.getProduct());
    }

    @Test
    public void testOrderItemPriceCalculation() {
        BigDecimal unitPrice = BigDecimal.valueOf(15.50);
        Integer quantity = 4;
        
        OrderItem item = new OrderItem(testOrder, testProduct, quantity, unitPrice);
        
        assertEquals(unitPrice, item.getPrice());
        assertEquals(quantity, item.getQuantity());
        
        // Calculate total (this would typically be done in business logic)
        BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal actualTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        
        assertEquals(expectedTotal, actualTotal);
        assertEquals(BigDecimal.valueOf(62.00), actualTotal);
    }

    @Test
    public void testOrderItemWithZeroQuantity() {
        OrderItem item = new OrderItem(testOrder, testProduct, 0, BigDecimal.valueOf(25.99));
        
        assertEquals(Integer.valueOf(0), item.getQuantity());
        assertEquals(BigDecimal.valueOf(25.99), item.getPrice());
    }

    @Test
    public void testOrderItemWithZeroPrice() {
        OrderItem item = new OrderItem(testOrder, testProduct, 2, BigDecimal.ZERO);
        
        assertEquals(Integer.valueOf(2), item.getQuantity());
        assertEquals(BigDecimal.ZERO, item.getPrice());
    }

    @Test
    public void testOrderItemEquality() {
        OrderItem item1 = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(25.99));
        OrderItem item2 = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(25.99));
        
        item1.setId(1L);
        item2.setId(1L);
        
        // Note: Without overriding equals/hashCode, these will be different object references
        assertNotEquals(item1, item2);
        
        // But their properties should be equal
        assertEquals(item1.getId(), item2.getId());
        assertEquals(item1.getOrder(), item2.getOrder());
        assertEquals(item1.getProduct(), item2.getProduct());
        assertEquals(item1.getQuantity(), item2.getQuantity());
        assertEquals(item1.getPrice(), item2.getPrice());
    }

    @Test
    public void testOrderItemModification() {
        OrderItem item = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(25.99));
        
        // Modify quantity
        item.setQuantity(5);
        assertEquals(Integer.valueOf(5), item.getQuantity());
        
        // Modify price
        item.setPrice(BigDecimal.valueOf(19.99));
        assertEquals(BigDecimal.valueOf(19.99), item.getPrice());
        
        // Change product
        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setName("New Product");
        item.setProduct(newProduct);
        assertEquals(newProduct, item.getProduct());
        assertEquals("New Product", item.getProduct().getName());
    }
}
