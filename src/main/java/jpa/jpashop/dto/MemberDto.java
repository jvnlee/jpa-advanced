package jpa.jpashop.dto;

import jpa.jpashop.domain.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    @NotEmpty
    private String name;

    private Address address;

}
