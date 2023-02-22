package jpa.jpashop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpa.jpashop.domain.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderItemDto {

    @JsonIgnore
    private Long orderId;

    private Long itemId;

    private String itemName;

    private int orderPrice;

    private int count;

    public OrderItemDto(OrderItem orderItem) {
        this.orderId = orderItem.getOrder().getId();
        this.itemId = orderItem.getItem().getId();
        this.itemName = orderItem.getItem().getName();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
    }

    public OrderItemDto(Long orderId, Long itemId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
