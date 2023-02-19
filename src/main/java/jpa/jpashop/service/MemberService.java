package jpa.jpashop.service;

import jpa.jpashop.domain.Member;
import jpa.jpashop.dto.MemberDto;
import jpa.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long join(MemberDto memberDto) {
        // 동시에 접근한 같은 이름의 멤버가 검증을 통과하는 경우도 고려해서 name 컬럼 자체에 unique 제약 조건을 넣는 것이 좋음
        validateDuplicateName(memberDto.getName());
        Member member = new Member();
        member.update(memberDto.getName(), memberDto.getAddress());
        memberRepository.save(member);
        return member.getId();
    }

    @Transactional(readOnly = true) // 조회 메서드는 JPA 성능 최적화를 위해 readOnly 옵션 true로 설정
    public MemberDto findMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();
        return new MemberDto(member.getName(), member.getAddress());
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findMembers() {
        List<Member> memberList = memberRepository.findAll();
        return memberList.stream()
                .map(m -> new MemberDto(m.getName(), m.getAddress()))
                .collect(Collectors.toList());
    }

    private void validateDuplicateName(String name) {
        Optional<Member> member = memberRepository.findByName(name);
        if (member.isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

}
