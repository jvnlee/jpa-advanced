package jpa.jpashop.service;

import jpa.jpashop.domain.Address;
import jpa.jpashop.domain.Member;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderStatus;
import jpa.jpashop.domain.item.Book;
import jpa.jpashop.dto.OrderItemDto;
import jpa.jpashop.dto.OrderSearchDto;
import jpa.jpashop.exception.NotEnoughStockException;
import jpa.jpashop.repository.MemberRepository;
import jpa.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("주문 성공")
    void order() {
        Member member = createSampleMember("Andy");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList = createOrderItemDtoList(book1, book2, 5, 10);

        Long orderId = orderService.order(member.getId(), dtoList);

        Order order = orderRepository.findById(orderId).get();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(order.getOrderItems().size()).isEqualTo(2);
        assertThat(order.getTotalPrice()).isEqualTo(170000);
        assertThat(book1.getStock()).isEqualTo(5);
        assertThat(book2.getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("주문 실패 - 재고 부족")
    void order_outOfStock() {
        Member member = createSampleMember("Andy");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList = createOrderItemDtoList(book1, book2, 5, 1000);

        assertThatThrownBy(() -> orderService.order(member.getId(), dtoList))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder() {
        Member member = createSampleMember("Andy");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList = createOrderItemDtoList(book1, book2, 5, 10);
        Long orderId = orderService.order(member.getId(), dtoList);

        orderService.cancelOrder(orderId);

        Order order = orderRepository.findById(orderId).get();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book1.getStock()).isEqualTo(10);
        assertThat(book2.getStock()).isEqualTo(20);
    }

    @Test
    @DisplayName("주문 내역 검색 - 이름")
    void searchOrder_memberName() {
        Member member1 = createSampleMember("Andy");
        Member member2 = createSampleMember("Lee");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList1 = createOrderItemDtoList(book1, book2, 1, 1);
        List<OrderItemDto> dtoList2 = createOrderItemDtoList(book1, book2, 2, 2);

        orderService.order(member1.getId(), dtoList1);
        orderService.order(member2.getId(), dtoList2);

        OrderSearchDto searchCond = new OrderSearchDto();
        searchCond.setMemberName("Lee");

        List<Order> result = orderService.searchOrder(searchCond);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getMember().getName()).isEqualTo("Lee");
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(result.get(0).getTotalPrice()).isEqualTo(44000);
    }

    @Test
    @DisplayName("주문 내역 검색 - 주문상태")
    void searchOrder_orderStatus() {
        Member member1 = createSampleMember("Andy");
        Member member2 = createSampleMember("Lee");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList1 = createOrderItemDtoList(book1, book2, 1, 1);
        List<OrderItemDto> dtoList2 = createOrderItemDtoList(book1, book2, 2, 2);

        orderService.order(member1.getId(), dtoList1);
        orderService.order(member2.getId(), dtoList2);

        OrderSearchDto searchCond = new OrderSearchDto();
        searchCond.setOrderStatus(OrderStatus.CANCEL);

        List<Order> result = orderService.searchOrder(searchCond);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 내역 검색 - 이름과 주문상태")
    void searchOrder() {
        Member member1 = createSampleMember("Andy");
        Member member2 = createSampleMember("Lee");
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList1 = createOrderItemDtoList(book1, book2, 1, 1);
        List<OrderItemDto> dtoList2 = createOrderItemDtoList(book1, book2, 2, 2);

        orderService.order(member1.getId(), dtoList1);
        orderService.order(member2.getId(), dtoList2);

        OrderSearchDto searchCond = new OrderSearchDto();
        searchCond.setMemberName("Andy");
        searchCond.setOrderStatus(OrderStatus.ORDER);

        List<Order> result = orderService.searchOrder(searchCond);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getMember().getName()).isEqualTo("Andy");
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(result.get(0).getTotalPrice()).isEqualTo(22000);
    }

    private Member createSampleMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("Seoul", "Teheran-ro", "12345"));
        em.persist(member);
        return member;
    }

    private Book createSampleBook(String name, int price, int stock) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStock(stock);
        em.persist(book);
        return book;
    }

    private List<OrderItemDto> createOrderItemDtoList(Book book1, Book book2, int cnt1, int cnt2) {
        List<OrderItemDto> dtoList = new ArrayList<>();

        OrderItemDto dto1 = new OrderItemDto();
        dto1.setItemId(book1.getId());
        dto1.setCount(cnt1);

        OrderItemDto dto2 = new OrderItemDto();
        dto2.setItemId(book2.getId());
        dto2.setCount(cnt2);

        dtoList.add(dto1);
        dtoList.add(dto2);
        return dtoList;
    }
}