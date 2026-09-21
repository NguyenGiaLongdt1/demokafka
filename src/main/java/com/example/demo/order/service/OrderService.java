package com.example.demo.order.service;

import com.example.demo.order.dto.CreateOrderRequest;
import com.example.demo.order.dto.OrderResponse;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.entity.OrderStatus;
import com.example.demo.order.exception.OrderNotFoundException;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.messaging.outbound.api.OutboundCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboundCommandService outboundCommandService;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setCustomerName(request.customerName());
        order.setAmount(request.amount());
        order.setOrderCode(generateOrderCode());
        order.setStatus(OrderStatus.DRAFT);
        OrderEntity saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public OrderResponse submit(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chi co the submit order DRAFT, hien tai: " + order.getStatus());
        }
        order.setStatus(OrderStatus.PENDING_SEND);

        String requestId = UUID.randomUUID().toString();
        outboundCommandService.enqueueOrderSubmit(order.getId(), order.getOrderCode(), requestId);

        return toResponse(order);

    }

    private String generateOrderCode() {
        Optional<OrderEntity> latest = orderRepository.findTopByOrderByIdDesc();
        if (latest.isEmpty()) {
            return "ORD-000001";
        }
        long next = latest.get().getId() + 1;
        return "ORD-" + String.format("%06d", next);
    }

    private OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerName(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}