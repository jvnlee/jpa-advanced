package jpa.jpashop.service;

import jpa.jpashop.domain.Address;
import jpa.jpashop.domain.Member;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderStatus;
import jpa.jpashop.domain.item.Book;
import jpa.jpashop.dto.OrderItemDto;
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
        Member member = createSampleMember();
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
        Member member = createSampleMember();
        Book book1 = createSampleBook("BookA", 10000, 10);
        Book book2 = createSampleBook("BookB", 12000, 20);
        List<OrderItemDto> dtoList = createOrderItemDtoList(book1, book2, 5, 1000);

        assertThatThrownBy(() -> orderService.order(member.getId(), dtoList))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder() {
        Member member = createSampleMember();
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

    private Member createSampleMember() {
        Member member = new Member();
        member.setName("Andy");
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