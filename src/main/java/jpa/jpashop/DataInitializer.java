package jpa.jpashop;

import jpa.jpashop.domain.*;
import jpa.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final EntityManager em;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        Member memberA = new Member();
        memberA.setName("userA");
        memberA.setAddress(new Address("CityA", "StreetA", "12345"));
        em.persist(memberA);

        Member memberB = new Member();
        memberB.setName("userB");
        memberB.setAddress(new Address("CityB", "StreetB", "12345"));
        em.persist(memberB);

        Book bookA = new Book();
        bookA.setName("BookA");
        bookA.setPrice(10000);
        bookA.setStock(100);
        em.persist(bookA);

        Book bookB = new Book();
        bookB.setName("BookB");
        bookB.setPrice(20000);
        bookB.setStock(100);
        em.persist(bookB);

        OrderItem orderItemA1 = OrderItem.createOrderItem(bookA, 10000, 1);
        OrderItem orderItemA2 = OrderItem.createOrderItem(bookB, 20000, 2);

        Delivery deliveryA = new Delivery();
        deliveryA.setAddress(memberA.getAddress());

        Order orderA = Order.createOrder(memberA, deliveryA, orderItemA1, orderItemA2);
        em.persist(orderA);

        OrderItem orderItemB1 = OrderItem.createOrderItem(bookA, 10000, 3);
        OrderItem orderItemB2 = OrderItem.createOrderItem(bookB, 20000, 4);

        Delivery deliveryB = new Delivery();
        deliveryB.setAddress(memberB.getAddress());

        Order orderB = Order.createOrder(memberB, deliveryB, orderItemB1, orderItemB2);
        em.persist(orderB);
    }
}
