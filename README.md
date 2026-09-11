# 게임 서버 단기 심화 트랙 9기 - 입문 주차 프로젝트: 붉은 달의 성채
팀스파르타 내일배움캠프 게임 서버 단기 트랙 9기에서 진행한 입문 주차 프로젝트입니다. Spring Boot와 JPA를 활용하여 카드 게임의 저장·조회 API를 구현하며 백엔드 개발의 기초를 학습했습니다.

제공된 게임 클라이언트와 서버 뼈대를 바탕으로 Lv 1~11 과제를 진행했습니다. 데이터베이스 연결과 의존성 주입부터 CRUD, 요청 검증, 전역 예외 처리, 저장 시간 기록, N+1 없는 카드 수 집계까지 단계적으로 구현했습니다.

<img width="1369" height="775" alt="image" src="https://github.com/user-attachments/assets/2c5176fb-534c-4555-a6c1-6b76583026b3" />


<br/>

## 프로젝트 배경

- **교육 과정:** 팀스파르타 내일배움캠프
- **프로젝트 구분:** 입문 주차 프로젝트
- **진행 범위:** Lv 1 ~ 11 (Lv 12는 미구현이므로 포함하지 않습니다.)
- **학습 목표:** Spring Boot 기반 REST API 구현과 JPA의 기본 동작 이해
- **제공 구성:** 게임 클라이언트, 게임 콘텐츠, 서버 뼈대, API 명세 및 과제 가이드
- **구현 범위:** 제공된 코드를 바탕으로 백엔드 설정과 단계별 API 기능 완성

<br/>

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.0, Spring MVC |
| 데이터 접근 | Spring Data JPA, Hibernate |
| 데이터베이스 | Docker - MySQL |
| 요청 검증 | Jakarta Bean Validation |
| 개발 도구 | Gradle Wrapper, Lombok |

<br/>

## 주요 기능

- 플레이어 이름과 시작 덱을 받아 새 게임 생성
- HP, 층, 게임 단계, 상태 및 전체 덱 저장
- 저장된 게임 목록과 상세 조회, 여정 이어하기
- 플레이어 이름 변경 및 게임·카드 삭제
- 종료된 게임의 진행 덮어쓰기 차단
- 400·404·409 에러 응답 형식 통일
- JPA Auditing을 통한 생성·수정 시각 기록
- JPQL `GROUP BY`와 DTO 프로젝션을 통한 게임별 카드 수 일괄 집계

<br/>

## 구조와 설계

```text
com.gamebasic
├── GameBasicApplication
├── common
│   ├── dto             # 공통 에러 응답
│   ├── entity          # BaseEntity
│   └── exception       # 사용자 정의 예외, 전역 예외 처리
├── game
│   ├── controller      # HTTP 요청과 응답
│   ├── service         # 게임 처리 흐름, 트랜잭션
│   ├── repository      # 게임 데이터 접근
│   ├── entity          # Game, GamePhase, GameStatus
│   └── dto             # 생성·수정 요청, 목록·상세 응답
└── runcard
    ├── repository      # 카드 조회·저장·삭제·집계
    ├── entity          # RunCard
    └── dto             # 카드 요청·응답, DeckCount
```

요청은 `Controller → Service → Repository → DB` 순서로 처리합니다. 서비스는 조회한 엔티티와 집계 결과를 응답 DTO로 변환하고, 컨트롤러는 HTTP 상태 코드와 함께 반환합니다.

`RunCard`가 `Game`을 참조하는 단방향 다대일 관계를 사용합니다. `Game`에는 카드 컬렉션을 두지 않으며, cascade와 orphanRemoval 없이 Repository를 통해 카드의 조회·저장·삭제를 명시적으로 처리합니다.

<br />

## Lv 1 ~ 11 학습 기록

### Lv 1. Docker MySQL 연결과 설정

데이터베이스 연결 실패 로그를 읽고 `application.properties`에 datasource 설정을 작성했습니다. MySQL 연결 주소, 드라이버, 계정 정보의 역할을 구분하고, 재시작 후 데이터를 유지하는 개발용 스키마 설정을 학습했습니다.

- `ddl-auto=update`: 기존 스키마를 바탕으로 변경 반영을 시도하는 개발용 설정
- `create`: 시작할 때 스키마를 새로 생성하므로 기존 데이터 보존에 부적합
- `validate`: 스키마를 변경하지 않고 매핑과의 일치 여부 확인

### Lv 2. 빈 등록과 생성자 주입

`required a bean of type ...` 오류를 통해 서비스가 스프링 빈으로 등록되어야 하는 이유를 학습했습니다. `@Service`로 서비스를 등록하고, `@RequiredArgsConstructor`와 `final` 필드를 사용해 Repository를 생성자로 주입했습니다.

### Lv 3. RESTful 경로와 목록 API

클라이언트가 호출하는 `GET /games`와 컨트롤러 매핑을 일치시켰습니다. 같은 자원 경로도 HTTP 메서드에 따라 조회와 생성을 구분하며, 경로와 메서드 조합이 맞지 않으면 요청을 처리할 수 없다는 점을 확인했습니다.

### Lv 4. 트랜잭션 설정 수정

게임 생성 과정의 읽기 전용 트랜잭션 문제를 다뤘습니다. 저장·수정·삭제에는 쓰기 가능한 `@Transactional`을 적용하고, 목록·상세 조회에는 `@Transactional(readOnly = true)`를 사용했습니다.

읽기 전용 설정은 조회 의도를 전달하고 최적화에 활용되는 설정입니다. 모든 환경에서 쓰기를 동일하게 차단하는 보안 장치로 이해하지 않도록 구분했습니다.

### Lv 5. 요청 검증과 응답 DTO

`RunCardRequest`의 필드에 검증 조건을 적용하고, `CardResponse`에 카드 ID·종류·획득 층을 담았습니다. 요청 DTO와 응답 DTO를 분리하여 입력 검증과 출력 형식의 책임을 구분했습니다.

| 어노테이션 | 검증 내용 |
| --- | --- |
| `@NotNull` | null 금지 |
| `@NotEmpty` | null과 빈 문자열·컬렉션 등 금지 |
| `@NotBlank` | null, 빈 문자열, 공백만 있는 문자열 금지 |
| `@Size` | 문자열 길이 또는 컬렉션 크기 제한 |
| `@Min`, `@Max` | 숫자의 최솟값·최댓값 제한 |
| `@Valid` | 대상 객체의 검증 실행 및 중첩 객체 검증 연결 |

### Lv 6. 진행과 전체 덱 저장

`PUT /games/{gameId}/progress`를 연결하여 보상 선택과 전투 이후의 진행을 저장하도록 구성했습니다. 요청의 덱은 변경된 카드 일부가 아니라 저장할 전체 덱이므로, 기존 카드를 삭제한 뒤 요청 순서대로 다시 저장합니다.

게임 정보 변경과 카드 교체는 하나의 트랜잭션에서 처리합니다.

### Lv 7. 목록·상세 조회

게임 목록은 ID 내림차순으로 조회하고, 상세 덱은 카드 ID 오름차순으로 조회합니다.

```java
findAllByOrderByIdDesc()
findAllByGameOrderByIdAsc(Game game)
```

Spring Data JPA가 메서드 이름의 조건과 정렬 규칙을 해석하는 방식을 학습했습니다. 목록은 `GameSummaryResponse`, 상세는 덱을 포함한 `GameDetailResponse`로 반환합니다.

### Lv 8. 변경 감지와 자식 우선 삭제

이름 변경은 트랜잭션 안에서 조회한 `Game`의 `rename()`을 호출합니다. 관리 중인 엔티티의 변경을 JPA가 감지하므로, 별도의 `save()` 호출 없이 변경 사항이 반영됩니다.

게임 삭제 시에는 외래 키 관계를 고려하여 `RunCard`를 먼저 삭제한 뒤 `Game`을 삭제합니다.

### Lv 9. 종료된 게임 보호

`Game.isFinished()`로 종료 여부를 판단하고, `CLEARED` 또는 `FAILED` 상태인 게임에 진행 저장을 요청하면 409 Conflict로 처리합니다. 진행 정보와 덱을 변경하기 전에 검사하여 종료된 기록의 덮어쓰기를 막습니다.

### Lv 10. 전역 예외 처리

`GameNotFoundException`과 `GameFinishedException`을 서비스에서 발생시키고, `@RestControllerAdvice`가 HTTP 응답으로 변환하도록 구성했습니다.

| 상황 | 상태 코드 |
| --- | --- |
| 요청 값이나 형식 오류 | 400 Bad Request |
| 존재하지 않는 게임 | 404 Not Found |
| 종료된 게임에 진행 저장 | 409 Conflict |

에러 응답은 `status`, `error`, `message`, `path` 필드로 통일합니다. 서비스는 실패 이유를 표현하고, 전역 핸들러는 상태 코드와 응답 형식을 결정합니다.

### Lv 11. 저장 시간과 카드 수 집계

#### 저장 시간

`GameBasicApplication`의 `@EnableJpaAuditing`으로 Auditing을 활성화하고, `Game`만 `BaseEntity`를 상속하도록 구성했습니다.

- `@MappedSuperclass`: 부모의 시간 필드를 Game의 매핑에 포함
- `@EntityListeners(AuditingEntityListener.class)`: 시간 기록 리스너 연결
- `@CreatedDate`: 최초 저장 시각 기록
- `@LastModifiedDate`: 엔티티 수정 시각 기록
- `@Column(updatable = false)`: 생성 시각을 UPDATE 대상에서 제외

Auditing을 활성화해도 모든 엔티티에 시간 필드가 자동으로 생기지는 않습니다. 시간 필드와 리스너 설정을 상속한 `Game`이 기록 대상입니다. 응답에는 현재 시간을 새로 생성하지 않고 엔티티에 기록된 값을 전달합니다.

#### N+1 문제와 일괄 집계

게임 목록을 조회한 뒤 각 게임의 카드 수를 별도로 조회하면, 게임이 N개일 때 `1 + N`번의 쿼리가 필요합니다. 이를 게임 조회 1회와 카드 수 집계 1회로 처리하도록 구성했습니다.

```java
@Query("""
    select new com.gamebasic.runcard.dto.DeckCount(
        r.game.id,
        count(r)
    )
    from RunCard r
    where r.game in :games
    group by r.game.id
    """)
List<DeckCount> countByGames(List<Game> games);
```

`GROUP BY`는 게임 ID별로 카드를 묶고, `COUNT`는 각 그룹의 장수를 계산합니다. `select new`는 결과 한 행마다 `DeckCount(Long gameId, Long deckSize)` 생성자를 호출합니다. 인터페이스 프로젝션은 사용하지 않습니다.

서비스는 집계 결과를 `Map<Long, Long>`에 담고, 게임 ID로 카드 수를 찾아 목록 DTO의 `deckSize`에 전달합니다. 집계 결과가 없는 게임은 `getOrDefault(game.getId(), 0L)`로 0장을 사용합니다.

<br />

## 학습 내용 정리

### 1. 3 Layer Architecture를 적절히 적용했는가? 이러한 구조가 필요한 이유는 무엇인가?

프로젝트에서는 Controller, Service, Repository의 역할을 다음과 같이 분리했습니다.

| 계층 | 프로젝트에서 담당하는 역할 |
| --- | --- |
| Controller | HTTP 요청 수신, 요청 DTO 검증 연결, 서비스 호출, 응답 상태 코드와 본문 반환 |
| Service | 게임 생성·진행 저장·조회·이름 변경·삭제 흐름 처리, 종료 여부 판단, 트랜잭션 관리 |
| Repository | JPA를 통한 데이터 조회·저장·삭제 및 게임별 카드 수 집계 |

예를 들어 진행 저장 요청은 `GameController`가 받은 뒤 `GameService`로 전달합니다. 서비스는 종료된 게임인지 확인하고, 게임 정보를 변경한 뒤 `RunCardRepository`를 통해 덱을 교체합니다.

이처럼 컨트롤러에서 직접 DB에 접근하지 않고, 서비스에서 HTTP 응답 형식을 결정하지 않도록 책임을 구분했습니다. 예외를 HTTP 에러 응답으로 바꾸는 작업은 `GlobalExceptionHandler`에서 처리합니다.

이러한 구조는 변경의 영향을 줄이기 위해 필요합니다. API 경로나 응답 상태가 바뀌면 컨트롤러를, 게임 처리 규칙이 바뀌면 서비스를, 조회 방식이 바뀌면 Repository를 중심으로 수정할 수 있습니다. 또한 같은 서비스 로직을 여러 요청에서 재사용하고, 계층별로 테스트하기도 쉬워집니다.

### 2. 엔티티를 그대로 응답하지 않고 DTO로 바꿔서 응답하는 이유는 무엇인가?

엔티티는 DB에 저장할 데이터와 연관관계를 표현하는 객체이고, 응답 DTO는 클라이언트에 전달할 정보를 표현하는 객체입니다. 두 객체를 분리하면 DB 구조와 API 응답 형식을 각각 관리할 수 있습니다.

DTO를 사용하는 이유는 다음과 같습니다.

- **불필요한 정보 노출 방지:** 엔티티에 필드가 추가되어도 응답에 자동으로 노출되지 않도록 관리할 수 있습니다.
- **유연한 구조:** API 스펙과 엔티티 간의 결합도를 낮춰 유지보수를 용이하게 하며, 변경에 대한 영향을 최소화합니다. 이로 인해 시스템의 확장성과 유연성이 향상됩니다.
- **성능 최적화:** 필요한 데이터만을 로드하여 성능을 향상시킬 수 있으며, 네트워크 트래픽을 최소화할 수 있습니다.

### 3. Bean Validation이 필요한 이유는 무엇인가?

Bean Validation은 요청 데이터가 정해진 조건을 만족하는지 확인하기 위해 필요합니다. 잘못된 값을 서비스나 DB까지 전달하기 전에 검증하여 데이터 오류를 줄이고, 클라이언트에 요청을 수정할 수 있는 정보를 제공합니다.

프로젝트에서는 요청 DTO에 다음과 같은 검증 조건을 선언했습니다.

| 적용 대상 | 검증 조건 | 목적 |
| --- | --- | --- |
| 플레이어 이름 | `@NotBlank`, `@Size(min = 2, max = 12)` | 공백 이름과 길이 조건을 벗어난 이름 방지 |
| 시작 덱 | `@NotEmpty` | null 또는 빈 덱 방지 |
| 카드 종류 | `@NotBlank` | 카드 종류 누락 및 공백 방지 |
| 카드 획득 층 | `@NotNull`, `@Min(0)`, `@Max(10)` | 값 누락과 범위를 벗어난 입력 방지 |

어노테이션으로 조건을 선언하면 필드마다 반복적인 검사 코드를 작성하는 부담을 줄이고, DTO를 읽는 것만으로 입력 조건을 파악할 수 있습니다.
