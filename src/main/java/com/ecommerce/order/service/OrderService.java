package com.ecommerce.order.service;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.request.CreateOrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Long id);

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

    void cancelOrder(Long id);

    List<OrderResponse> getOrdersByCustomerEmail(String email);
}
