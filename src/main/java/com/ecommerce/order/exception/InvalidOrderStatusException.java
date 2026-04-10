package com.ecommerce.order.exception;

import com.ecommerce.order.domain.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus requestedStatus) {
        super(String.format("Cannot change order status from '%s' to '%s'. " +
                "Only PENDING orders can be modified.", currentStatus, requestedStatus));
    }
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
