package com.electronicshop.repository;

import com.electronicshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("select oi.productId, sum(oi.quantity) from OrderItem oi group by oi.productId order by sum(oi.quantity) desc")
    List<Object[]> topProducts();
}
