package jpa.jpashop.dto;

import jpa.jpashop.domain.Address;
import jpa.jpashop.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderId;

    private String memberName;

    private Address address;

    private int totalPrice;

    private LocalDateTime orderDate;

    private OrderStatus status;

}
