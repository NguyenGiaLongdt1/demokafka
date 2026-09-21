package com.example.demo.order.repository;

import com.example.demo.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findTopByOrderByIdDesc();

    Optional<OrderEntity> findByOrderCode(String orderCode);
}