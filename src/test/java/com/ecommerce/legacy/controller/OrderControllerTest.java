package com.ecommerce.legacy.controller;

import com.ecommerce.legacy.model.Order;
import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        objectMapper.registerModule(new JavaTimeModule());
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setShippingAddress("123 Test St");
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setOrderDate(LocalDateTime.now());
    }

    @Test
    public void testCreateOrder_Success() throws Exception {
        when(orderService.createOrder(anyLong(), anyString())).thenReturn(testOrder);

        mockMvc.perform(post("/orders")
                .param("userId", "1")
                .param("shippingAddress", "123 Test St"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.shippingAddress").value("123 Test St"));

        verify(orderService).createOrder(1L, "123 Test St");
    }

    @Test
    public void testCreateOrder_UserNotFound() throws Exception {
        when(orderService.createOrder(anyLong(), anyString()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(post("/orders")
                .param("userId", "999")
                .param("shippingAddress", "123 Test St"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testCreateOrder_InvalidArgument() throws Exception {
        when(orderService.createOrder(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid shipping address"));

        mockMvc.perform(post("/orders")
                .param("userId", "1")
                .param("shippingAddress", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid shipping address"));
    }

    @Test
    public void testAddItemToOrder_Success() throws Exception {
        when(orderService.addItemToOrder(anyLong(), anyLong(), anyInt())).thenReturn(testOrder);

        mockMvc.perform(post("/orders/1/items")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).addItemToOrder(1L, 1L, 2);
    }

    @Test
    public void testAddItemToOrder_OrderNotFound() throws Exception {
        when(orderService.addItemToOrder(anyLong(), anyLong(), anyInt()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(post("/orders/999/items")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testAddItemToOrder_IllegalState() throws Exception {
        when(orderService.addItemToOrder(anyLong(), anyLong(), anyInt()))
                .thenThrow(new IllegalStateException("Cannot modify confirmed order"));

        mockMvc.perform(post("/orders/1/items")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot modify confirmed order"));
    }

    @Test
    public void testGetOrderById_Success() throws Exception {
        when(orderService.getOrderById(anyLong())).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1));

        verify(orderService).getOrderById(1L);
    }

    @Test
    public void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(anyLong()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testGetAllOrders() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService).getAllOrders();
    }

    @Test
    public void testGetOrdersByUser_Success() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByUser(anyLong())).thenReturn(orders);

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService).getOrdersByUser(1L);
    }

    @Test
    public void testGetOrdersByUser_UserNotFound() throws Exception {
        when(orderService.getOrdersByUser(anyLong()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/orders/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testGetOrdersByUserPaginated_Success() throws Exception {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        when(orderService.getOrdersByUserPaginated(anyLong(), any())).thenReturn(orderPage);

        mockMvc.perform(get("/orders/user/1/paginated")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(orderService).getOrdersByUserPaginated(eq(1L), eq(PageRequest.of(0, 10)));
    }

    @Test
    public void testGetOrdersByStatus() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByStatus(any())).thenReturn(orders);

        mockMvc.perform(get("/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService).getOrdersByStatus(Order.OrderStatus.PENDING);
    }

    @Test
    public void testGetOrdersByDateRange() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByDateRange(any(), any())).thenReturn(orders);

        mockMvc.perform(get("/orders/date-range")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testUpdateOrderStatus_Success() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), any())).thenReturn(testOrder);

        mockMvc.perform(put("/orders/1/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);
    }

    @Test
    public void testUpdateOrderStatus_OrderNotFound() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), any()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(put("/orders/999/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testUpdateOrderStatus_IllegalState() throws Exception {
        when(orderService.updateOrderStatus(anyLong(), any()))
                .thenThrow(new IllegalStateException("Invalid status transition"));

        mockMvc.perform(put("/orders/1/status")
                .param("status", "DELIVERED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status transition"));
    }

    @Test
    public void testConfirmOrder_Success() throws Exception {
        when(orderService.confirmOrder(anyLong())).thenReturn(testOrder);

        mockMvc.perform(put("/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).confirmOrder(1L);
    }

    @Test
    public void testConfirmOrder_NotFound() throws Exception {
        when(orderService.confirmOrder(anyLong()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(put("/orders/999/confirm"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testShipOrder_Success() throws Exception {
        when(orderService.shipOrder(anyLong())).thenReturn(testOrder);

        mockMvc.perform(put("/orders/1/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).shipOrder(1L);
    }

    @Test
    public void testDeliverOrder_Success() throws Exception {
        when(orderService.deliverOrder(anyLong())).thenReturn(testOrder);

        mockMvc.perform(put("/orders/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).deliverOrder(1L);
    }

    @Test
    public void testCancelOrder_Success() throws Exception {
        when(orderService.cancelOrder(anyLong())).thenReturn(testOrder);

        mockMvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    public void testDeleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(anyLong());

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));

        verify(orderService).deleteOrder(1L);
    }

    @Test
    public void testDeleteOrder_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Order not found"))
                .when(orderService).deleteOrder(anyLong());

        mockMvc.perform(delete("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    public void testDeleteOrder_IllegalState() throws Exception {
        doThrow(new IllegalStateException("Cannot delete shipped order"))
                .when(orderService).deleteOrder(anyLong());

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete shipped order"));
    }
}
