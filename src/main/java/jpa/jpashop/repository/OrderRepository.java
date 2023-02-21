package jpa.jpashop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderStatus;
import jpa.jpashop.dto.OrderSearchDto;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static jpa.jpashop.domain.QMember.*;
import static jpa.jpashop.domain.QOrder.*;
import static jpa.jpashop.domain.QOrderItem.*;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public List<Order> findBySearchCond(OrderSearchDto searchCond) {
        String memberName = searchCond.getMemberName();
        OrderStatus orderStatus = searchCond.getOrderStatus();

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(memberName)) {
            builder.and(member.name.like("%" + memberName + "%"));
        }

        if (orderStatus != null) {
            builder.and(order.status.eq(orderStatus));
        }

        return queryFactory
                .selectDistinct(order)
                .from(order)
                .join(order.member, member).fetchJoin()
                .join(order.delivery).fetchJoin()
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.item).fetchJoin()
                .where(builder)
                .fetch();
    }

}
