package com.electronicshop.service;

import com.electronicshop.entity.Order;
import com.electronicshop.entity.OrderItem;
import com.electronicshop.entity.Product;
import com.electronicshop.repository.OrderItemRepository;
import com.electronicshop.repository.OrderRepository;
import com.electronicshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order checkout(Long userId, List<CartService.CartItem> cartItems, BigDecimal total) {
        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotal(total);
        order.setStatus("NEW");
        Order saved = orderRepository.save(order);

        for (CartService.CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrderId(saved.getId());
            item.setProductId(cartItem.getProduct().getId());
            item.setQuantity(cartItem.getQuantity());
            orderItemRepository.save(item);
        }

        return saved;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Order> getById(Long id) {
        return orderRepository.findById(id);
    }

    public List<OrderItem> getItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Map<Long, Product> getProductMap(List<OrderItem> items) {
        List<Long> ids = items.stream().map(OrderItem::getProductId).distinct().collect(Collectors.toList());
        return productRepository.findAllById(ids).stream().collect(Collectors.toMap(Product::getId, p -> p));
    }

    public long countOrders() {
        return orderRepository.count();
    }

    public BigDecimal totalRevenue() {
        return orderRepository.sumRevenue();
    }

    public List<Order> allOrders() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    public Map<String, Object> dashboardData() {
        Map<String, Object> data = new HashMap<>();

        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        List<Order> recentOrders = orderRepository.findFromDate(start);

        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            revenueByDay.put(LocalDate.now().minusDays(i), BigDecimal.ZERO);
        }

        for (Order order : recentOrders) {
            LocalDate day = order.getCreatedAt().toLocalDate();
            if (revenueByDay.containsKey(day)) {
                revenueByDay.put(day, revenueByDay.get(day).add(order.getTotal()));
            }
        }

        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartValues = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : revenueByDay.entrySet()) {
            chartLabels.add(entry.getKey().toString());
            chartValues.add(entry.getValue());
        }

        data.put("chartLabels", chartLabels);
        data.put("chartValues", chartValues);

        List<Object[]> rawTop = orderItemRepository.topProducts();
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Object[] row : rawTop.stream().limit(5).collect(Collectors.toList())) {
            Long productId = ((Number) row[0]).longValue();
            Long soldQty = ((Number) row[1]).longValue();
            productRepository.findById(productId).ifPresent(product -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("quantity", soldQty);
                topProducts.add(item);
            });
        }

        data.put("topProducts", topProducts);
        return data;
    }
}
