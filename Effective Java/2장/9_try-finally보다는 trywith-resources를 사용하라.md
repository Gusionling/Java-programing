# 아이템 9: try-finally보다는 try-with-resources를 사용하라

**자바 라이브러리에는 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많다.** InputStream, OutputStream, java.sql.Connection 등이 좋은 예다. 자원 닫기는 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 한다. 이런 자원 중 상당수가 안전망으로 finalizer를 활용하고는 있지만 finalizer는 그리 믿을만하지 못하다(아이템 8).

**전통적으로 자원이 제대로 닫힘을 보장하는 수단으로 try-finally가 쓰였다.** 예외가 발생하거나 메서드에서 반환되는 경우를 포함해서 말이다.

## 문제 상황: try-finally의 한계

### 1. 자원이 하나일 때 - 나쁘지 않음

```java
// try-finally - 더 이상 자원을 회수하는 최선의 방책이 아니다!
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

나쁘지 않지만, 자원을 하나 더 사용한다면 어떨까?

### 2. 자원이 둘 이상일 때 - 너무 지저분하다!

```java
// 자원이 둘 이상이면 try-finally 방식은 너무 지저분하다!
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}
```

**문제점:**
- 코드가 복잡하고 가독성이 떨어진다
- 중첩된 try-finally 구조가 지저분하다
- 실수로 자원 해제를 빼먹기 쉽다

### 3. 예외 정보 손실 문제

**try-finally 문을 제대로 사용한 앞의 두 코드 예제에조차 미묘한 결점이 있다.** 예외는 try 블록과 finally 블록 모두에서 발생할 수 있는데, 예컨대 기기에 물리적인 문제가 생긴다면 firstLineOfFile 메서드 안의 readLine 메서드가 예외를 던지고, 같은 이유로 close 메서드도 실패할 것이다. 이런 상황이라면 두 번째 예외가 첫 번째 예외를 완전히 집어삼켜 버린다. 그러면 스택 추적 내역에 첫 번째 예외에 관한 정보는 남지 않게 되어, 실제 시스템에서의 디버깅을 몹시 어렵게 한다.

## 해결 방법: try-with-resources

**이러한 문제들은 자바 7이 투척한 try-with-resources 덕에 모두 해결되었다.** 이 구조를 사용하려면 해당 자원이 AutoCloseable 인터페이스를 구현해야 한다. 단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스다. 자바 라이브러리와 서드파티 라이브러리들의 수많은 클래스와 인터페이스가 이미 AutoCloseable을 구현하거나 확장해뒀다. **닫아야 하는 자원을 뜻하는 클래스를 작성한다면 AutoCloseable을 반드시 구현하기 바란다.**

### 1. 자원 하나 - 간결하고 깔끔하다

```java
// try-with-resources - 자원을 회수하는 최선책!
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
    }
}
```

### 2. 복수 자원 처리 - 훨씬 깔끔하다

```java
// 복수의 자원을 처리하는 try-with-resources - 짧고 매혹적이다!
static void copy(String src, String dst) throws IOException {
    try (InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```

**try-with-resources 버전이 짧고 읽기 수월할 뿐 아니라 문제를 진단하기도 훨씬 좋다.** firstLineOfFile 메서드를 생각해보자. readLine과 (코드에는 나타나지 않는) close 호출 양쪽에서 예외가 발생하면, close에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록된다. 이처럼 실전에서는 프로그래머에게 보여줄 예외 하나만 보존되고 여러 개의 다른 예외가 숨겨질 수도 있다. **이렇게 숨겨진 예외들도 그냥 버려지지는 않고, 스택 추적 내역에 '숨겨졌다(suppressed)'는 꼬리표를 달고 출력된다.** 또한, 자바 7에서 Throwable에 추가된 getSuppressed 메서드를 이용하면 프로그램 코드에서 가져올 수도 있다.

### 3. catch 절과 함께 쓰기

**보통의 try-finally에서처럼 try-with-resources에서도 catch 절을 쓸 수 있다.** catch 절 덕분에 try 문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다.

```java
// try-with-resources를 catch 절과 함께 쓰는 모습
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
    } catch (IOException e) {
        return defaultVal;
    }
}
```

## AutoCloseable 구현하기

```java
// AutoCloseable 구현 예시
public class MyResource implements AutoCloseable {
    private boolean closed = false;
    
    public void doSomething() {
        if (closed) {
            throw new IllegalStateException("Resource is closed");
        }
        // 작업 수행
        System.out.println("작업 수행 중...");
    }
    
    @Override
    public void close() throws Exception {
        if (closed) return;
        
        System.out.println("자원 정리 중...");
        closed = true;
        // 실제 자원 정리 작업
    }
}

// 사용 예시
public void useMyResource() {
    try (MyResource resource = new MyResource()) {
        resource.doSomething();
        // 자동으로 close() 호출됨
    } catch (Exception e) {
        System.err.println("오류 발생: " + e.getMessage());
    }
}
```

## try-with-resources vs try-finally 비교

| 구분 | try-finally | try-with-resources |
|-----|-------------|------------------|
| **가독성** | 자원이 많아질수록 복잡해짐 | 항상 깔끔하고 간결함 |
| **안전성** | 실수로 close() 빼먹기 쉬움 | 자동으로 close() 호출 보장 |
| **예외 처리** | 나중 예외가 이전 예외를 덮어씀 | suppressed로 모든 예외 정보 보존 |
| **디버깅** | 원인 파악이 어려움 | 정확한 예외 정보 제공 |
| **성능** | 수동 관리로 인한 누수 위험 | 확실한 자원 해제로 안정적 |

## 실제 활용 예시들

### 파일 처리

```java
// 파일 읽기
public List<String> readAllLines(String path) {
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
        return reader.lines().collect(Collectors.toList());
    } catch (IOException e) {
        throw new RuntimeException("파일 읽기 실패: " + path, e);
    }
}

// 파일 쓰기
public void writeToFile(String path, List<String> lines) {
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
    } catch (IOException e) {
        throw new RuntimeException("파일 쓰기 실패: " + path, e);
    }
}
```

### 데이터베이스 연결

```java
// 데이터베이스 쿼리
public List<User> findUsers() {
    String sql = "SELECT * FROM users";
    
    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new User(rs.getString("name"), rs.getInt("age")));
        }
        return users;
        
    } catch (SQLException e) {
        throw new RuntimeException("데이터베이스 조회 실패", e);
    }
}
```

### 커스텀 자원 관리

```java
// 락 관리
public class LockManager implements AutoCloseable {
    private final Lock lock;
    
    public LockManager(Lock lock) {
        this.lock = lock;
        this.lock.lock();
    }
    
    @Override
    public void close() {
        lock.unlock();
    }
}

// 사용법
public void criticalSection() {
    try (LockManager lockManager = new LockManager(someLock)) {
        // 임계 구역 작업
        performCriticalOperation();
    } // 자동으로 언락됨
}
```

## 핵심 정리

**꼭 회수해야 하는 자원을 다룰 때는 try-finally 말고, try-with-resources를 사용하자.** 예외는 없다. 코드는 더 짧고 분명해지고, 만들어지는 예외 정보도 훨씬 유용하다. **try-finally로 작성하면 실용적이지 못할 만큼 코드가 지저분해지는 경우라도, try-with-resources로는 정확하고 쉽게 자원을 회수할 수 있다.**

**장점 요약:**
1. **코드가 간결하고 읽기 쉽다** - 중첩된 try-finally 제거
2. **자동 자원 관리** - close() 호출 실수 방지
3. **완전한 예외 정보** - suppressed 예외로 디버깅 정보 보존
4. **확장성** - 자원 개수에 관계없이 깔끔한 코드 유지
5. **안전성** - 예외 상황에서도 확실한 자원 해제

**사용 조건:**
- **AutoCloseable 인터페이스 구현 필수**
- Java 7 이상에서 사용 가능

**권장사항:**
- 새로운 자원 관리 클래스를 만든다면 **반드시 AutoCloseable 구현**
- 기존 try-finally 코드는 **try-with-resources로 리팩터링** 고려