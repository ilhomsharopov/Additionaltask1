package com.ecommerce.order.dto.request;

import com.ecommerce.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status bo'lishi shart")
    private OrderStatus status;
}
