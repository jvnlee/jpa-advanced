package jpa.jpashop.repository;

import jpa.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    @PersistenceContext // 스프링 컨테이너가 엔티티 매니저 빈(프록시)을 주입해줌
    private EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.of(em.find(Member.class, id));
    }

    public Optional<Member> findByName(String name) {
        Member m = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getSingleResult();
        return Optional.of(m);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

}
