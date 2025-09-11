import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class MethodReferencePractice {

    // 문제 2에서 사용할 정적 메소드
    public static int convertAndDouble(String s) {
        return Integer.parseInt(s) * 2;
    }

    // 문제 3에서 사용할 인스턴스 메소드
    public void printWithPrefix(String s) {
        System.out.println("Printing: " + s);
    }

    public static void main(String[] args) {
        List<String> messages = Arrays.asList("java", "is", "fun");

        // --- 문제 1: 정적 메소드 레퍼런스 ---
        System.out.println("--- Problem 1: Static Method Reference ---");
        System.out.println("Goal: messages 리스트의 각 요소를 System.out.println을 사용하여 출력하세요.");
        // TODO: `System.out::println` 형태의 정적 메소드 레퍼런스를 사용하여 아래 코드를 완성하세요.
        messages.forEach(System.out::println);

        System.out.println("Expected output:\njava\nis\nfun\n");


        // --- 문제 2: 사용자 정의 정적 메소드 레퍼런스 ---
        System.out.println("--- Problem 2: Custom Static Method Reference ---");
        List<String> numbersAsStrings = Arrays.asList("1", "2", "3", "4");
        System.out.println("Goal: numbersAsStrings 리스트의 각 문자열을 숫자로 변환한 후 2를 곱한 결과를 출력하세요.");
        // TODO: `convertAndDouble` 정적 메소드를 메소드 레퍼런스로 사용하여 아래 스트림 코드를 완성하세요.
        numbersAsStrings.stream()
                .map(MethodReferencePractice::convertAndDouble)
                .forEach(System.out::println);
        System.out.println("Expected output:\n2\n4\n6\n8\n");


        // --- 문제 3: 특정 객체의 인스턴스 메소드 레퍼런스 ---
        System.out.println("--- Problem 3: Instance Method Reference (Specific Object) ---");
        MethodReferencePractice printer = new MethodReferencePractice();
        System.out.println("Goal: messages 리스트의 각 요소를 `printer` 객체의 `printWithPrefix` 메소드를 사용하여 출력하세요.");
        // TODO: `printer` 객체의 `printWithPrefix` 메소드를 참조하여 아래 코드를 완성하세요.
        messages.forEach(printer::printWithPrefix);
        System.out.println("Expected output:\nPrinting: java\nPrinting: is\nPrinting: fun\n");


        // --- 문제 4: 임의 객체의 인스턴스 메소드 레퍼런스 ---
        System.out.println("--- Problem 4: Instance Method Reference (Arbitrary Object) ---");
        System.out.println("Goal: messages 리스트의 각 문자열을 대문자로 변환하여 출력하세요.");
        // TODO: String 클래스의 toUpperCase 메소드를 메소드 레퍼런스로 사용하여 아래 스트림 코드를 완성하세요.

        messages.stream()
            .map( String::toUpperCase )
            .forEach(System.out::println);

        System.out.println("Expected output:\nJAVA\nIS\nFUN\n");


        // --- 문제 5: 생성자 레퍼런스 ---
        System.out.println("--- Problem 5: Constructor Reference ---");
        System.out.println("Goal: `ArrayList::new` 생성자 레퍼런스를 사용하여 비어 있는 새로운 ArrayList를 만드세요.");

        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> newList = listSupplier.get();
        System.out.println("Is the new list an instance of ArrayList? " + (newList instanceof ArrayList));
        System.out.println("Expected output:\nIs the new list an instance of ArrayList? true\n");
    }
}
