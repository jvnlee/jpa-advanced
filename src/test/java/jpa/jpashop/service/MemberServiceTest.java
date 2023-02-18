package jpa.jpashop.service;

import jpa.jpashop.domain.Member;
import jpa.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("회원 가입 성공")
    public void join() {
        Member member = new Member();
        member.setName("Andy");

        Long id = memberService.join(member);

        Member findMember = memberRepository.findById(id).get();
        assertThat(member).isEqualTo(findMember);
    }

    @Test
    @DisplayName("회원 가입 실패 - 중복된 이름으로 가입 시도")
    public void joinWithDuplicateName() {
        Member member1 = new Member();
        member1.setName("Andy");
        Member member2 = new Member();
        member2.setName("Andy");

        memberService.join(member1);

        assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);
    }

}