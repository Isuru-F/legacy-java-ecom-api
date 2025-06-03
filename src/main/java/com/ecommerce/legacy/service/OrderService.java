package com.ecommerce.legacy.service;

import com.ecommerce.legacy.model.Order;
import com.ecommerce.legacy.model.OrderItem;
import com.ecommerce.legacy.model.Product;
import com.ecommerce.legacy.model.User;
import com.ecommerce.legacy.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserService userService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public Order createOrder(Long userId, String shippingAddress) {
        User user = userService.getUserById(userId);
        validateShippingAddress(shippingAddress);
        
        Order order = new Order(user, shippingAddress);
        return orderRepository.save(order);
    }

    public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
        Order order = getOrderById(orderId);
        Product product = productService.getProductById(productId);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify order that is not in PENDING status");
        }

        if (!productService.isProductAvailable(productId, quantity)) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        OrderItem orderItem = new OrderItem(order, product, quantity, product.getPrice());
        order.addOrderItem(orderItem);

        // Update product stock
        Integer newStock = product.getStockQuantity() - quantity;
        productService.updateStock(productId, newStock);

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUser(Long userId) {
        User user = userService.getUserById(userId);
        return orderRepository.findByUser(user);
    }

    public Page<Order> getOrdersByUserPaginated(Long userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        return orderRepository.findByUser(user, pageable);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        
        return orderRepository.save(order);
    }

    public Order confirmOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
    }

    public Order shipOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.SHIPPED);
    }

    public Order deliverOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.DELIVERED);
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
        }

        // Restore product stock for cancelled orders
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            restoreStockForOrder(order);
        }

        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete order that is not in PENDING or CANCELLED status");
        }

        orderRepository.deleteById(id);
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            Integer restoredStock = product.getStockQuantity() + item.getQuantity();
            productService.updateStock(product.getId(), restoredStock);
        }
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case PENDING:
                if (newStatus != Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != Order.OrderStatus.SHIPPED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != Order.OrderStatus.DELIVERED) {
                    throw new IllegalStateException("Invalid status transition from SHIPPED to " + newStatus);
                }
                break;
            case DELIVERED:
            case CANCELLED:
                throw new IllegalStateException("Cannot change status from " + currentStatus);
        }
    }

    private void validateShippingAddress(String shippingAddress) {
        if (StringUtils.isBlank(shippingAddress)) {
            throw new IllegalArgumentException("Shipping address cannot be blank");
        }
    }
}
