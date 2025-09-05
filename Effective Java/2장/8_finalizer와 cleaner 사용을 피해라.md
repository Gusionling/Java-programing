# 아이템 8: finalizer와 cleaner 사용을 피하라

**자바는 두 가지 객체 소멸자를 제공한다.** 그 중 **finalizer는 예측할 수 없고, 상황에 따라 위험할 수 있어 일반적으로 불필요하다.** 오동작, 낮은 성능, 이식성 문제의 원인이 되기도 한다. **finalizer는 나름의 쓰임새가 몇 가지 있긴 하지만 기본적으로 '쓰지 말아야' 한다.** 그래서 자바 9에서는 finalizer를 사용 자제(deprecated) API로 지정하고 cleaner를 그 대안으로 소개했다(하지만 자바 라이브러리에서도 finalizer를 여전히 사용한다). **cleaner는 finalizer보다는 덜 위험하지만, 여전히 예측할 수 없고, 느리고, 일반적으로 불필요하다.**

## finalizer와 cleaner의 문제점들

### 1. 즉각 수행된다는 보장이 없다

**finalizer와 cleaner는 즉각 수행된다는 보장이 없다.** 객체에 접근할 수 없게 된 후 finalizer나 cleaner가 실행되기까지 얼마나 걸릴지 알 수 없다. **즉, finalizer와 cleaner로는 제때 실행되어야 하는 작업은 절대 할 수 없다.**

```java
// 잘못된 예 - finalizer에 의존하면 안 된다!
public class BadResource {
    @Override
    protected void finalize() throws Throwable {
        // 파일이나 스레드 등 중요한 자원을 여기서 정리하면 안 된다!
        closeImportantResource(); // 언제 실행될지 모름!
    }
}
```

### 2. 수행 시점뿐 아니라 수행 여부조차 보장하지 않는다

**finalizer나 cleaner를 얼마나 신속히 수행할지는 전적으로 가비지 컬렉터 알고리즘에 달렸고, 이는 가비지 컬렉터 구현마다 천차만별이다.** 테스트한 JVM에서는 완벽하게 동작하던 프로그램이 가장 중요한 고객의 시스템에서는 엄청난 재앙을 일으킬 수도 있다.

### 3. 예외 처리 문제 (finalizer만의 문제)

**finalizer 동작 중 발생한 예외는 무시되며, 처리할 작업이 남았어도 그 순간 종료된다.** 잡지 못한 예외 때문에 해당 객체는 자칫 마무리가 덜 된 상태로 남을 수 있다.

```java
// finalizer에서 예외가 발생하면?
@Override
protected void finalize() throws Throwable {
    throw new RuntimeException(); // 이 예외는 무시됨!
    // 아래 정리 작업은 실행되지 않음
    cleanupCriticalResource();
}
```

### 4. 심각한 성능 문제

**finalizer와 cleaner는 심각한 성능 문제도 동반한다.** 내 컴퓨터에서 간단한 AutoCloseable 객체를 생성하고 가비지 컬렉터가 수거하기까지 12ns가 걸린 반면(try-with-resources로 자신을 닫도록 했다), finalizer를 사용하면 550ns가 걸렸다. 다시 말해 finalizer를 사용한 객체를 생성하고 파괴하니 50배나 느렸다.

### 5. finalizer 공격에 노출

**finalizer를 사용한 클래스는 finalizer 공격에 노출되어 심각한 보안 문제를 일으킬 수 있다.** 공격 원리는 간단하다. 생성자나 직렬화 과정에서 예외가 발생하면, 이 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer가 수행될 수 있게 된다.

```java
// 공격을 받을 수 있는 코드
public class Vulnerable {
    public Vulnerable() {
        if (!authenticated()) {
            throw new SecurityException("인증 실패!");
        }
    }
    
    // finalizer 공격에 취약!
    @Override protected void finalize() {
        // 악의적 코드가 여기서 실행될 수 있음
    }
}

// 해결: final 클래스로 만들거나
public final class SafeVulnerable {
    // ...
}

// 또는 finalize 메서드를 final로 선언
@Override protected final void finalize() throws Throwable {
    super.finalize();
}
```

## 올바른 해결책: AutoCloseable 구현

**파일이나 스레드 등 종료해야 할 자원을 담고 있는 객체의 클래스에서 finalizer나 cleaner를 대신해줄 묘책은 그저 AutoCloseable을 구현해주고, 클라이언트에서 인스턴스를 다 쓰고 나면 close 메서드를 호출하면 된다.**

```java
// 올바른 방법 - AutoCloseable 구현
public class ProperResource implements AutoCloseable {
    private boolean closed = false;
    
    public void doSomething() {
        if (closed) {
            throw new IllegalStateException("Resource is closed");
        }
        // 작업 수행
    }
    
    @Override
    public void close() throws Exception {
        if (closed) return;
        
        closed = true;
        // 자원 정리 작업
        cleanupResources();
    }
}
```

### try-with-resources 사용

```java
// 안전하고 확실한 자원 해제
public void useResource() {
    try (ProperResource resource = new ProperResource()) {
        resource.doSomething();
        // 자동으로 close() 호출됨
    } catch (Exception e) {
        // 예외 처리
    }
}
```

## finalizer와 cleaner의 적절한 쓰임새

그렇다면 cleaner와 finalizer는 언제 써야 할까?

### 1. 안전망(Safety Net) 역할

**자원의 소유자가 close 메서드를 호출하지 않는 것에 대비한 안전망 역할이다.** cleaner나 finalizer가 즉시 호출되리라는 보장은 없지만, 클라이언트가 하지 않은 자원 회수를 늦게라도 해주는 것이 아예 안 하는 것보다는 낫다.

```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    // 청소가 필요한 자원. 절대 Room을 참조해서는 안 된다!
    private static class State implements Runnable {
        int numJunkPiles; // 방(Room) 안의 쓰레기 수
        
        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }
        
        // close 메서드나 cleaner가 호출한다.
        @Override public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }
    
    // 방의 상태. cleanable과 공유한다.
    private final State state;
    
    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;
    
    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }
    
    @Override public void close() {
        cleanable.clean();
    }
}
```

**중요한 점:** State 인스턴스는 '절대로' Room 인스턴스를 참조해서는 안 된다. Room 인스턴스를 참조할 경우 순환참조가 생겨 가비지 컬렉터가 Room 인스턴스를 회수해갈 (따라서 자동 청소가) 기회를 얻지 못한다.

### 2. 네이티브 피어(Native Peer)

**두 번째 활용은 네이티브 피어와 연결된 객체에서다.** 네이티브 피어란 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체를 말한다. 네이티브 피어는 자바 객체가 아니니 가비지 컬렉터는 그 존재를 알지 못한다.

**단, 성능 저하를 감당할 수 있고 네이티브 피어가 심각한 자원을 가지고 있지 않을 때에만 해당한다.** 네이티브 피어가 사용하는 자원을 즉시 회수해야 한다면 앞서 설명한 close 메서드를 사용해야 한다.

## 핵심 정리

**cleaner(자바 8까지는 finalizer)는 안전망 역할이나 중요하지 않은 네이티브 자원 회수용으로만 사용하자.** 물론 이런 경우라도 불확실성과 성능 저하에 주의해야 한다.

**절대 기억해야 할 점들:**

1. **finalizer와 cleaner는 즉시 수행되지 않는다** - 중요한 자원 해제에 사용하면 안 됨
2. **finalizer는 예외를 무시한다** - 객체가 불완전한 상태로 남을 수 있음
3. **성능이 현저히 나쁘다** - 일반적인 자원 해제보다 50배 느림
4. **보안 문제를 일으킬 수 있다** - finalizer 공격에 노출

**대신 사용할 것:**
- **AutoCloseable 구현** + **try-with-resources** 사용
- 명시적 **close() 메서드** 호출
- 안전망이 필요한 경우에만 **cleaner** 추가 고려

**정말 필요한 경우:**
- close() 메서드 호출을 깜빡한 경우의 **안전망**
- **네이티브 피어** 자원 회수 (중요하지 않은 자원에 한해)