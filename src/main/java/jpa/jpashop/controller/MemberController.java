package jpa.jpashop.controller;

import jpa.jpashop.controller.response.Response;
import jpa.jpashop.domain.Address;
import jpa.jpashop.dto.MemberDto;
import jpa.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public Response<CreateMemberResponse> createMember(@RequestBody @Valid MemberDto memberDto) {
        Long id = memberService.join(memberDto);
        return new Response<>(new CreateMemberResponse(id));
    }

    @GetMapping
    public Response<List<MemberDto>> findAllMembers() {
        List<MemberDto> memberDtoList = memberService.findMembers();
        return new Response<>(memberDtoList);
    }

    @PutMapping("/{memberId}")
    public Response<UpdateMemberResponse> updateMember(@PathVariable("memberId") Long memberId, @RequestBody @Valid MemberDto memberDto) {
        memberService.updateMember(memberId, memberDto);
        return new Response<>(new UpdateMemberResponse(memberDto.getName(), memberDto.getAddress()));
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private String name;
        private Address address;
    }
}
