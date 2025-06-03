package com.ecommerce.legacy.controller;

import com.ecommerce.legacy.model.Order;
import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Order testOrder;
    private User testUser;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        testOrder.setShippingAddress("123 Test Street");
        testOrder.setOrderDate(LocalDateTime.now());
    }

    @Test
    public void testCreateOrder_Success() throws Exception {
        when(orderService.createOrder(eq(1L), eq("123 Test Street"))).thenReturn(testOrder);

        mockMvc.perform(post("/orders")
                .param("userId", "1")
                .param("shippingAddress", "123 Test Street"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).createOrder(1L, "123 Test Street");
    }

    @Test
    public void testCreateOrder_UserNotFound() throws Exception {
        when(orderService.createOrder(eq(999L), anyString()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(post("/orders")
                .param("userId", "999")
                .param("shippingAddress", "123 Test Street"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testCreateOrder_InvalidAddress() throws Exception {
        when(orderService.createOrder(eq(1L), eq("")))
                .thenThrow(new IllegalArgumentException("Shipping address cannot be blank"));

        mockMvc.perform(post("/orders")
                .param("userId", "1")
                .param("shippingAddress", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Shipping address cannot be blank"));
    }

    @Test
    public void testAddItemToOrder_Success() throws Exception {
        when(orderService.addItemToOrder(eq(1L), eq(1L), eq(2))).thenReturn(testOrder);

        mockMvc.perform(post("/orders/1/items")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(orderService).addItemToOrder(1L, 1L, 2);
    }

    @Test
    public void testAddItemToOrder_OrderNotFound() throws Exception {
        when(orderService.addItemToOrder(eq(999L), eq(1L), eq(2)))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(post("/orders/999/items")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testAddItemToOrder_InsufficientStock() throws Exception {
        when(orderService.addItemToOrder(eq(1L), eq(1L), eq(100)))
                .thenThrow(new IllegalArgumentException("Insufficient stock"));

        mockMvc.perform(post("/orders/1/items")
                .param("productId", "1")
                .param("quantity", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient stock"));
    }

    @Test
    public void testGetOrderById_Success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    public void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(999L))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testGetAllOrders_Success() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(orderService).getAllOrders();
    }

    @Test
    public void testGetOrdersByUser_Success() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByUser(1L)).thenReturn(orders);

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(orderService).getOrdersByUser(1L);
    }

    @Test
    public void testGetOrdersByUser_UserNotFound() throws Exception {
        when(orderService.getOrdersByUser(999L))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/orders/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testGetOrdersByUserPaginated_Success() throws Exception {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), PageRequest.of(0, 10), 1);
        when(orderService.getOrdersByUserPaginated(eq(1L), any())).thenReturn(orderPage);

        mockMvc.perform(get("/orders/user/1/paginated")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(orderService).getOrdersByUserPaginated(eq(1L), any());
    }

    @Test
    public void testGetOrdersByStatus_Success() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByStatus(Order.OrderStatus.PENDING)).thenReturn(orders);

        mockMvc.perform(get("/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orderService).getOrdersByStatus(Order.OrderStatus.PENDING);
    }

    @Test
    public void testGetOrdersByDateRange_Success() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByDateRange(any(), any())).thenReturn(orders);

        mockMvc.perform(get("/orders/date-range")
                .param("startDate", "2023-01-01T00:00:00")
                .param("endDate", "2023-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orderService).getOrdersByDateRange(any(), any());
    }

    @Test
    public void testUpdateOrderStatus_Success() throws Exception {
        Order confirmedOrder = new Order();
        confirmedOrder.setId(1L);
        confirmedOrder.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED)).thenReturn(confirmedOrder);

        mockMvc.perform(put("/orders/1/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);
    }

    @Test
    public void testUpdateOrderStatus_InvalidTransition() throws Exception {
        when(orderService.updateOrderStatus(1L, Order.OrderStatus.DELIVERED))
                .thenThrow(new IllegalStateException("Invalid status transition"));

        mockMvc.perform(put("/orders/1/status")
                .param("status", "DELIVERED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status transition"));
    }

    @Test
    public void testConfirmOrder_Success() throws Exception {
        Order confirmedOrder = new Order();
        confirmedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderService.confirmOrder(1L)).thenReturn(confirmedOrder);

        mockMvc.perform(put("/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).confirmOrder(1L);
    }

    @Test
    public void testShipOrder_Success() throws Exception {
        Order shippedOrder = new Order();
        shippedOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderService.shipOrder(1L)).thenReturn(shippedOrder);

        mockMvc.perform(put("/orders/1/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(orderService).shipOrder(1L);
    }

    @Test
    public void testDeliverOrder_Success() throws Exception {
        Order deliveredOrder = new Order();
        deliveredOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderService.deliverOrder(1L)).thenReturn(deliveredOrder);

        mockMvc.perform(put("/orders/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(orderService).deliverOrder(1L);
    }

    @Test
    public void testCancelOrder_Success() throws Exception {
        Order cancelledOrder = new Order();
        cancelledOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

        mockMvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    public void testCancelOrder_InvalidState() throws Exception {
        when(orderService.cancelOrder(1L))
                .thenThrow(new IllegalStateException("Cannot cancel shipped order"));

        mockMvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot cancel shipped order"));
    }

    @Test
    public void testDeleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));

        verify(orderService).deleteOrder(1L);
    }

    @Test
    public void testDeleteOrder_InvalidState() throws Exception {
        doThrow(new IllegalStateException("Cannot delete confirmed order"))
                .when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete confirmed order"));
    }
}
