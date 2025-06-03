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

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setShippingAddress("123 Test Street");
        testOrder.setOrderDate(LocalDateTime.now());
    }

    @Test
    public void testCreateOrder_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(1L, "123 Test Street");

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals("123 Test Street", result.getShippingAddress());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());

        verify(userService).getUserById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateOrder_UserNotFound() {
        when(userService.getUserById(999L)).thenThrow(new EntityNotFoundException("User not found"));

        orderService.createOrder(999L, "123 Test Street");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrder_BlankShippingAddress() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        orderService.createOrder(1L, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrder_NullShippingAddress() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        orderService.createOrder(1L, null);
    }

    @Test
    public void testAddItemToOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.isProductAvailable(1L, 2)).thenReturn(true);
        when(productService.updateStock(1L, 8)).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.addItemToOrder(1L, 1L, 2);

        assertNotNull(result);
        verify(productService).updateStock(1L, 8);
        verify(orderRepository).save(testOrder);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddItemToOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        orderService.addItemToOrder(999L, 1L, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddItemToOrder_OrderNotPending() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productService.getProductById(1L)).thenReturn(testProduct);

        orderService.addItemToOrder(1L, 1L, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddItemToOrder_InsufficientStock() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.isProductAvailable(1L, 20)).thenReturn(false);

        orderService.addItemToOrder(1L, 1L, 20);
    }

    @Test
    public void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        orderService.getOrderById(999L);
    }

    @Test
    public void testGetAllOrders_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderRepository).findAll();
    }

    @Test
    public void testGetOrdersByUser_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderRepository.findByUser(testUser)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).getUserById(1L);
        verify(orderRepository).findByUser(testUser);
    }

    @Test
    public void testGetOrdersByUserPaginated_Success() {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderRepository.findByUser(testUser, pageable)).thenReturn(orderPage);

        Page<Order> result = orderService.getOrdersByUserPaginated(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByUser(testUser, pageable);
    }

    @Test
    public void testGetOrdersByStatus_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByStatus(Order.OrderStatus.PENDING);
    }

    @Test
    public void testGetOrdersByDateRange_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        when(orderRepository.findByOrderDateBetween(start, end)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByDateRange(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByOrderDateBetween(start, end);
    }

    @Test
    public void testUpdateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateOrderStatus_InvalidTransition() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, Order.OrderStatus.DELIVERED);
    }

    @Test
    public void testConfirmOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.confirmOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testShipOrder_Success() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.shipOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testDeliverOrder_Success() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.deliverOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelOrder_ShippedOrder() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelOrder_DeliveredOrder() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);
    }

    @Test
    public void testCancelOrder_WithStockRestore() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        OrderItem orderItem = new OrderItem(testOrder, testProduct, 2, BigDecimal.valueOf(50.00));
        testOrder.getOrderItems().add(orderItem);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productService.updateStock(1L, 12)).thenReturn(testProduct);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        verify(productService).updateStock(1L, 12);
        verify(orderRepository).save(testOrder);
    }

    @Test
    public void testDeleteOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteOrder_ConfirmedOrder() {
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);
    }

    @Test
    public void testDeleteOrder_CancelledOrder() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }
}
