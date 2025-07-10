# 닫힘의 수단
자바 라이브러리에는 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많다. InputStream, OutputStream, java.sql.Connection 등이 좋은 예이다. 

닫아줘야 한다. -> 사용자가 실수하기 딱 좋음 그럼 finalizer를 써야 하나? 아니다. 그래서 제대로 닫힘을 보장하는 수단을 try-finaily가 쓰였다. 

근데 이건 자원을 2개 이상 사용하면 중첩이 되어 버리기 때문에 너무 지저분하다 

# try-with-resources
이 구조를 사용하기 위해서는 해당 자원이 AutoCloseable 인터페이스를 구현해야 한다. 
- 단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스이다. 