package com.example.demo.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Khong tim thay order id=" + id);
    }
}