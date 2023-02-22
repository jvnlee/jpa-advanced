package jpa.jpashop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpa.jpashop.domain.Order;
import jpa.jpashop.domain.OrderStatus;
import jpa.jpashop.dto.OrderDto;
import jpa.jpashop.dto.OrderItemDto;
import jpa.jpashop.dto.OrderSearchDto;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static jpa.jpashop.domain.QDelivery.*;
import static jpa.jpashop.domain.QMember.*;
import static jpa.jpashop.domain.QOrder.*;
import static jpa.jpashop.domain.QOrderItem.*;
import static jpa.jpashop.domain.item.QItem.*;

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
        long limit = searchCond.getLimit();
        long offset = searchCond.getOffset();

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
                .where(builder)
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public List<OrderDto> findBySearchCondV2(OrderSearchDto searchCond) {
        List<OrderDto> orderDtoList = findOrderDtos(searchCond);
        List<Long> orderIds = orderDtoList.stream().map(OrderDto::getOrderId).collect(Collectors.toList());

        List<OrderItemDto> orderItemDtoList = findOrderItemDtos(orderIds);
        Map<Long, List<OrderItemDto>> orderItemDtoMap = orderItemDtoList.stream().collect(Collectors.groupingBy(OrderItemDto::getOrderId));

        orderDtoList.forEach(o -> o.setOrderItems(orderItemDtoMap.get(o.getOrderId())));

        return orderDtoList;
    }

    public List<OrderDto> findOrderDtos(OrderSearchDto searchCond) {
        String memberName = searchCond.getMemberName();
        OrderStatus orderStatus = searchCond.getOrderStatus();
        long limit = searchCond.getLimit();
        long offset = searchCond.getOffset();

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(memberName)) {
            builder.and(member.name.like("%" + memberName + "%"));
        }

        if (orderStatus != null) {
            builder.and(order.status.eq(orderStatus));
        }

        return queryFactory
                .select(
                        Projections.constructor(
                                OrderDto.class,
                                order.id,
                                order.member.name,
                                delivery.address,
                                order.orderDate,
                                order.status
                        )
                )
                .from(order)
                .join(order.member, member)
                .join(order.delivery, delivery)
                .where(builder)
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    private List<OrderItemDto> findOrderItemDtos(List<Long> orderIds) {
        return queryFactory
                .select(
                        Projections.constructor(
                                OrderItemDto.class,
                                orderItem.order.id,
                                item.id,
                                item.name,
                                orderItem.orderPrice,
                                orderItem.count
                        )
                )
                .from(orderItem, orderItem)
                .join(orderItem.item, item)
                .where(orderItem.order.id.in(orderIds))
                .fetch();
    }
}
