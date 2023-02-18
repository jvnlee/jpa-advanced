package jpa.jpashop.domain;

import jpa.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    // createOrderItem()으로만 엔티티를 생성하기 때문에 생성자는 protected 으로 전환
    protected OrderItem() {
    }

    // 주문 상품 생성
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.decreaseStock(count);

        return orderItem;
    }

    // 주문 상품 취소
    public void cancelOrderItem() {
        this.item.increaseStock(count);
    }

    // 주문 상품 총 가격 조회
    public int getTotalPrice() {
        return this.orderPrice * this.count;
    }

}
