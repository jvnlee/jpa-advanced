package jpa.jpashop.controller;

import jpa.jpashop.controller.response.Response;
import jpa.jpashop.dto.OrderDto;
import jpa.jpashop.dto.OrderSearchDto;
import jpa.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Response<List<OrderDto>> findAllOrders(@RequestBody OrderSearchDto orderSearchDto) {
        List<OrderDto> orderList = orderService.searchOrder(orderSearchDto);
        return new Response<>(orderList);
    }

}
