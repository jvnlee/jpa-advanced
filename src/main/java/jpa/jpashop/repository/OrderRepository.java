package jpa.jpashop.repository;

import jpa.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

}
