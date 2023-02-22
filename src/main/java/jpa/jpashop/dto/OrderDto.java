package jpa.jpashop.dto;

import jpa.jpashop.domain.Address;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@Getter
@Setter
@NoArgsConstructor
public class OrderDto {

    private Long orderId;

    private String memberName;

    private Address address;

    private int totalPrice;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private List<OrderItemDto> orderItems;

    public OrderDto(Order order) {
        this.orderId = order.getId();
        this.memberName = order.getMember().getName();
        this.address = order.getDelivery().getAddress();
        this.totalPrice = order.getTotalPrice();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.orderItems = order.getOrderItems()
                .stream()
                .map(OrderItemDto::new)
                .collect(toList());
    }

    public OrderDto(Long orderId, String memberName, Address address, LocalDateTime orderDate, OrderStatus status) {
        this.orderId = orderId;
        this.memberName = memberName;
        this.address = address;
        this.orderDate = orderDate;
        this.status = status;
    }
}
