package com.electronicshop.repository;

import com.electronicshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("select coalesce(sum(o.total), 0) from Order o")
    BigDecimal sumRevenue();

    @Query("select o from Order o where o.createdAt >= ?1 order by o.createdAt asc")
    List<Order> findFromDate(LocalDateTime start);
}
