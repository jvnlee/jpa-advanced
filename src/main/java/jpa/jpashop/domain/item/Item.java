package jpa.jpashop.domain.item;

import jpa.jpashop.domain.Category;
import jpa.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stock;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stock - quantity < 0) {
            throw new NotEnoughStockException("재고는 0보다 낮을 수 없습니다.");
        }

        this.stock -= quantity;
    }

    public void update(String name, int price, int stock) {
        setName(name);
        setPrice(price);
        setStock(stock);
    }

}
