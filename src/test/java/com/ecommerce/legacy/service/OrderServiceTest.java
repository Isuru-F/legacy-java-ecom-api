package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.Order;
import com.ecommerce.legacy.model.OrderItem;
import com.ecommerce.legacy.model.Product;
import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.repository.OrderRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setShippingAddress("123 Test St");
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setOrderDate(LocalDateTime.now());

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
    }

    @Test
    public void testCreateOrder_Success() {
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(1L, "123 Test St");

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals("123 Test St", result.getShippingAddress());
        verify(userService).getUserById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateOrder_UserNotFound() {
        when(userService.getUserById(anyLong())).thenThrow(new EntityNotFoundException("User not found"));

        orderService.createOrder(999L, "123 Test St");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrder_BlankShippingAddress() {
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        orderService.createOrder(1L, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrder_NullShippingAddress() {
        when(userService.getUserById(anyLong())).thenReturn(testUser);

        orderService.createOrder(1L, null);
    }

    @Test
    public void testAddItemToOrder_Success() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(productService.getProductById(anyLong())).thenReturn(testProduct);
        when(productService.isProductAvailable(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.addItemToOrder(1L, 1L, 2);

        assertNotNull(result);
        verify(productService).updateStock(1L, 8); // 10 - 2 = 8
        verify(orderRepository).save(testOrder);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddItemToOrder_OrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        orderService.addItemToOrder(999L, 1L, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddItemToOrder_OrderNotPending() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.addItemToOrder(1L, 1L, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddItemToOrder_InsufficientStock() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(productService.getProductById(anyLong())).thenReturn(testProduct);
        when(productService.isProductAvailable(anyLong(), anyInt())).thenReturn(false);

        orderService.addItemToOrder(1L, 1L, 15);
    }

    @Test
    public void testGetOrderById_Success() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetOrderById_NotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        orderService.getOrderById(999L);
    }

    @Test
    public void testGetAllOrders() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findAll();
    }

    @Test
    public void testGetOrdersByUser_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(orderRepository.findByUser(any(User.class))).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(userService).getUserById(1L);
        verify(orderRepository).findByUser(testUser);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetOrdersByUser_UserNotFound() {
        when(userService.getUserById(anyLong())).thenThrow(new EntityNotFoundException("User not found"));

        orderService.getOrdersByUser(999L);
    }

    @Test
    public void testGetOrdersByUserPaginated_Success() {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserById(anyLong())).thenReturn(testUser);
        when(orderRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(orderPage);

        Page<Order> result = orderService.getOrdersByUserPaginated(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testOrder, result.getContent().get(0));
        verify(userService).getUserById(1L);
        verify(orderRepository).findByUser(testUser, pageable);
    }

    @Test
    public void testGetOrdersByStatus() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(any(Order.OrderStatus.class))).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findByStatus(Order.OrderStatus.PENDING);
    }

    @Test
    public void testGetOrdersByDateRange() {
        List<Order> orders = Arrays.asList(testOrder);
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        when(orderRepository.findByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(orders);

        List<Order> result = orderService.getOrdersByDateRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findByOrderDateBetween(startDate, endDate);
    }

    @Test
    public void testUpdateOrderStatus_Success() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateOrderStatus_InvalidTransition() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, Order.OrderStatus.PENDING);
    }

    @Test
    public void testUpdateOrderStatus_SameStatus() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.PENDING);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testConfirmOrder() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.confirmOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testShipOrder() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.shipOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testDeliverOrder() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.deliverOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testCancelOrder_PendingOrder() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
        // No stock restoration needed for pending orders
        verify(productService, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    public void testCancelOrder_ConfirmedOrderWithItems() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.addOrderItem(testOrderItem);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
        // Stock should be restored
        verify(productService).updateStock(1L, 12); // 10 + 2 = 12
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelOrder_ShippedOrder() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelOrder_DeliveredOrder() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);
    }

    @Test
    public void testDeleteOrder_PendingOrder() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    public void testDeleteOrder_CancelledOrder() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteOrder_ConfirmedOrder() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteOrder_ShippedOrder() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);
    }

    // Test status transition validation methods

    @Test(expected = IllegalStateException.class)
    public void testStatusTransition_PendingToShipped() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED);
    }

    @Test(expected = IllegalStateException.class)
    public void testStatusTransition_ConfirmedToPending() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, Order.OrderStatus.PENDING);
    }

    @Test(expected = IllegalStateException.class)
    public void testStatusTransition_ShippedToCancelled() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);
    }

    @Test
    public void testStatusTransition_PendingToConfirmed() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testStatusTransition_ConfirmedToShipped() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testStatusTransition_ShippedToDelivered() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.DELIVERED);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }
}
