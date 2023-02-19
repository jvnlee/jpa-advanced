package jpa.jpashop.service;

import jpa.jpashop.domain.Delivery;
import jpa.jpashop.domain.Member;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderItem;
import jpa.jpashop.domain.item.Item;
import jpa.jpashop.dto.OrderDto;
import jpa.jpashop.dto.OrderItemDto;
import jpa.jpashop.dto.OrderSearchDto;
import jpa.jpashop.repository.ItemRepository;
import jpa.jpashop.repository.MemberRepository;
import jpa.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, List<OrderItemDto> orderItemList) {
        // Member 조회
        Member member = memberRepository.findById(memberId).orElseThrow();

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // OrderItemDto를 통해 OrderItem을 생성하고 컬렉션에 모음
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDto orderItemDto : orderItemList) {
            Item item = itemRepository.findById(orderItemDto.getItemId()).orElseThrow();
            OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), orderItemDto.getCount());

            orderItems.add(orderItem);
        }

        OrderItem[] arr = orderItems.toArray(new OrderItem[0]);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, arr);

        /*
        주문 영속화
        Delivery와 OrderItem들은 CascadeType.ALL 옵션에 의해 Order가 영속화 될 때 자동적으로 함께 영속화 됨
        그래서 따로 DeliveryRepository나 OrderItemRepository를 두고 별도로 영속화 메서드를 정의할 필요가 없음
         */
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // JPA의 dirty-checking 덕분에 따로 쿼리를 작성하지 않아도 변경 사항을 반영해줌
        order.cancelOrder();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> searchOrder(OrderSearchDto searchCond) {
        List<Order> orderList = orderRepository.findBySearchCond(searchCond);
        return orderList
                .stream()
                .map(o -> new OrderDto(o.getId(), o.getMember().getName(), o.getDelivery().getAddress(), o.getTotalPrice(), o.getOrderDate(), o.getStatus()))
                .collect(Collectors.toList());
    }

}
