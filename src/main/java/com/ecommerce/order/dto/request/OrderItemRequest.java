package com.ecommerce.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "mahsulot idsi bo'lishi shart")
    private Long productId;

    @NotNull(message = "Qiymat bo'lishi kerak")
    @Min(value = 1, message = "Qiymat kamida 1 bo'lishi kerak")
    private Integer quantity;
}
