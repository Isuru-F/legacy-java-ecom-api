package com.ecommerce.legacy.controller;

import com.ecommerce.legacy.model.Order;
import com.ecommerce.legacy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam Long userId, @RequestParam String shippingAddress) {
        try {
            Order createdOrder = orderService.createOrder(userId, shippingAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItemToOrder(
            @PathVariable Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            Order updatedOrder = orderService.addItemToOrder(orderId, productId, quantity);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUser(userId);
            return ResponseEntity.ok(orders);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<?> getOrdersByUserPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getOrdersByUserPaginated(userId, pageable);
            return ResponseEntity.ok(orders);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            Order confirmedOrder = orderService.confirmOrder(id);
            return ResponseEntity.ok(confirmedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            Order shippedOrder = orderService.shipOrder(id);
            return ResponseEntity.ok(shippedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long id) {
        try {
            Order deliveredOrder = orderService.deliverOrder(id);
            return ResponseEntity.ok(deliveredOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(cancelledOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok().body(createSuccessResponse("Order deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> success = new HashMap<>();
        success.put("message", message);
        return success;
    }
}
