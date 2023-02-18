package jpa.jpashop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemUpdateDto {

    private String name;

    private int price;

    private int stock;

}
