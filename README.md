# JPA Advanced

### 성능 최적화

### 1. 지연 로딩 설정

기본적으로 엔티티의 모든 연관 필드에서 LAZY fetch(지연 로딩) 전략을 사용하는 것을 권장함.

처음부터 한번에 fetch 해오는 EAGER 전략은 성능에 좋지 않음. 당장 데이터가 필요하지 않은데도 가져오는 것은 네트워크 자원 낭비와 불필요한 성능 부하를 줄 수 있음.

```java
@Entity
public class Order {
    ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
```

1:N, N:M 관계에서는 기본 fetch 전략이 LAZY이지만, N:1, 1:1 관계에서는 EAGER이기 때문에 LAZY 전략을 명시해주어야 함.

&nbsp;

### 2. 응답에서 DTO 사용

응답을 보낼 때, 응답 객체의 타입은 엔티티보다는 별도의 DTO 타입을 사용하는 것이 권장됨.

```java
@GetMapping("/orders/{orderId}")
public Order findOrder(@PathVariable("orderId") String orderId) {
    ...
}
```

이렇게 엔티티 타입을 그대로 사용해서 응답을 하게되면 JSON 변환 과정에서 문제가 생김

지연 로딩 사용 시, 아직 실제 데이터로 초기화 되지 않은 엔티티는 프록시로 존재하는데, Jackson 라이브러리는 이 프록시 객체를 JSON으로 파싱할 수 없음.

연관 엔티티를 강제 초기화 시킨다고 해도, 양방향 연관 관계가 걸려 있다면 JSON 변환 시 두 엔티티 간 순환 참조가 일어나 예외가 발생함.

> 양방향 관계 중 한쪽에 `@JsonIgnore`를 붙이면 되긴 하지만, 도메인 계층의 객체에 프레젠테이션 계층에 관련된 어노테이션을 붙이는 것 자체가 bad practice.

&nbsp;

따라서 엔티티를 직접 사용하는 대신, DTO 타입을 정의해서 사용함.

```java
@GetMapping("/orders/{orderId}")
public OrderDto findOrder(@PathVariable("orderId") String orderId) {
    ...
}
```

DTO의 장점

1. API 스펙에 맞게 필요한 필드만 사용하면 되기 때문에 효율적

2. 엔티티에 포함된 민감한 데이터를 외부로 노출시키지 않아도 됨

3. 단순 데이터 전송 목적의 객체이므로 자유롭게 사용 및 변경 가능

&nbsp;

### 3. Fetch Join

지연 로딩 전략을 사용하게 되면 최초 데이터 조회 시 당장 필요하지 않은 연관 엔티티는 불러오지 않아서 효율적이지만, 실제로 연관 엔티티 데이터가 필요할 때, 추가적인 쿼리를 해야한다는 단점이 존재함.

```java
@GetMapping("/orders/{orderId}")
public List<OrderDto> findAllOrders() {
    List<Order> orders = orderService.findAll();
    return orders.stream()
        .map(OrderDto::new)
        .collect(Collectors.toList());
}
```

```java
@Data
public class OrderDto {
    private String memberName;
    ...

    public OrderDto(Order order) {
        this.memberName = order.getMember().getName();
        ...
    }
}
```

Order 엔티티를 OrderDto로 변환하는 과정에서 OrderDto의 생성자가 연관 엔티티인 Member의 데이터를 초기화시킴

따라서 최초 쿼리는 Order를 조회하고, 이후에 Member를 조회하는 쿼리가 다시 나가게됨.

만약 최초 쿼리가 N개의 Order를 반환했다면, 1:1 관계에 있는 Member에 대해서 N번의 추가 쿼리가 발생함. (N+1 문제)

&nbsp;

지연 로딩으로 인해 발생한 N+1 문제를 JPQL 페치 조인으로 해결할 수 있음

페치 조인은 Order 엔티티 조회 시 연관 엔티티인 Member까지 한번에 조회하므로 후속 쿼리가 발생하지 않음

```jpaql
select o from Order o join fetch o.member m ...
```

&nbsp;

만약 페치 조인한 필드가 1:N, N:M 관계라면 DISTINCT를 사용해야 데이터 중복을 제거할 수 있음

```jpaql
select distinct o from Order o join fetch o.orderItems oi ...
```

> 사실 JPQL에 DISTINCT를 붙여서 SQL에 DISTINCT가 포함되어도 DB는 여전히 중복된 레코드를 반환하는데,
> JPA가 내부적으로 기준 엔티티(Order)의 PK를 가지고 중복 레코드를 제거해주는 것임. 

&nbsp;


### 4. Batch Size 옵션

1:N, N:M 관계에 있는 컬렉션 필드는 페치 조인을 하면 페이징을 할 수 없다는 단점이 있음

페치 조인을 하면 DB에서 반환하는 레코드의 수가 다(多) 쪽에 맞춰지기 때문에 개수가 부풀려지고, 이는 원본 엔티티의 개수를 기준으로 페이징할 수 없게 만듦.

> N:1, 1:1 관계에 있는 엔티티는 페치 조인해도 DB에서 반환해주는 레코드 수가 부풀려지지 않기 때문에 페이징을 해도 상관 없음

&nbsp;

이 경우에는 페이징을 위해 컬렉션 필드에 대한 페치 조인을 포기하고 Batch Size 옵션을 사용함.

페치 조인을 포기하면 다시 N+1 문제가 발생하는데, 이 후속 쿼리의 개수를 최적화 할 수 있는 것이 Batch Size 옵션.

1. 글로벌 설정 (application.yml): 애플리케이션 내의 모든 연관 엔티티에 대해 옵션 적용

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

2. `@BatchSize` 어노테이션 사용: 특정 연관 엔티티에 대해 옵션 적용

```java
@Entity
public class Order {
    ...
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @BatchSize(size = 100) // 1:N, N:M 관계인 엔티티인 경우 필드에 명시
    private List<OrderItem> orderItems = new ArrayList<>();
}
```

```java
@Entity
public class OrderItem {
    ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 1:1, N:1 관계인 엔티티인 경우는 필드가 아니라
}

@Entity
@BatchSize(size = 100) // 클래스 레벨에 명시
public class Item {
    ...
}
```

&nbsp;

Batch Size 옵션 설정 시, SQL에 IN절을 생성해 후속 쿼리를 절감시킴 (Batch size = IN절 집합의 크기)

> BatchSize가 100이라면, 후속 쿼리 1000개가 10번으로 해결

```sql
select ... from Order o -- ROOT QUERY

select ... from OrderItem oi where oi.order_id in (?, ?, ..., ?) --- BATCH QUERY 1

select ... from Item i where i.item_id in (?, ?, ..., ?) --- BATCH QUERY 2
```

일반적으로 size는 100 ~ 1000 정도의 값을 사용함. DB나 애플리케이션이 한번에 다량의 데이터를 가져오면서 감당할 부하를 고려해서 최적의 값으로 설정하면 됨.

&nbsp;

### 5. DTO 타입으로 직접 조회

이전까지의 내용에서는 엔티티 타입으로 조회하고, 응답을 내보낼 때는 DTO 타입을 사용했음

그런데 처음부터 DTO 타입으로 조회하는 방법도 있음

DTO 타입 조회 시, DTO 클래스의 full path를 포함해줘야하고, 생성자 파라미터를 적절하게 지정하면 됨.

```jpaql
select new jpa.jpashop.dto.OrderDto(...) from Order o ...
```

우선 1:1, N:1 관계인 엔티티들부터 조회하고, 1:N 관계인 엔티티는 별도 쿼리로 조회해서 넣어주는 방식을 사용함.

엔티티를 그대로 조회하는 것과는 달리, JPA의 최적화 기능인 fetch join이나 batch size 적용이 불가능함.

&nbsp;

### 6. 성능 최적화 순서

대부분 1번 안에서 최적화가 가능하고, 일부 예외적 케이스에서 2, 3번 사용

1. 엔티티 조회 방식 사용

   - 지연 로딩 + 페치 조인

   - 컬렉션 타입 + 페이징 필요 &#8594; Batch Size 적용

2. DTO 조회 방식 사용

3. Native SQL 또는 Spring JdbcTemplate 사용