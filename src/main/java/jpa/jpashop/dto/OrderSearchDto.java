package jpa.jpashop.dto;

import jpa.jpashop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearchDto {

    private String memberName;

    private OrderStatus orderStatus;

    private long offset = 0;

    private long limit = 100;

}
