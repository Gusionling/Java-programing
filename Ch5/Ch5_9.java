import java.util.Scanner;

public class Ch5_9 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("총 스택 저장 공간의 크기 입력 >> ");
        int save = sc.nextInt();
        StringStack stringStack = new StringStack(save);
        while(true){
            System.out.print("문자열 입력 >> ");
            String val = sc.next();
            if(val.equals("그만")){break;}
            //조건문을 실행하는 것만으로도 stringStack.push(val)가 실행
            if(!stringStack.push(val)){
                System.out.println("스택이 꽉 차서 푸시 불가!");
            }
        }
        System.out.print("스택에 저장된 모든 문자열 팝 : ");
        for(int i = save; i>0; i--){
            System.out.print(stringStack.pop()+" ");
        }

    }
}

interface Stack {
    int length(); // 현재 스택에 저장된 개수 리턴
    int capacity(); // 스택의 전체 저장 가능한 개수 리턴
    String pop();
    boolean push(String val);
}

class StringStack implements Stack {
    private int flag;
    private int save; //저장가능한 개수
    private String[] Stack;
    //스택 객체를 어디서 만들지??
    //일단 생성자를 만들어보자

    public StringStack(int save) {
        this.save = save;
        Stack = new String[save];
        this.flag = 0;
    }

    @Override
    public int length() {
        return flag;
    }

    @Override
    public int capacity() {
        return save-flag;
    }

    @Override
    public String pop() {
        flag--;
        return Stack[flag];
    }

    @Override
    public boolean push(String val) {
        if(flag<save){
            Stack[flag] = val;
            flag++;
            return true;
        }
        else{
            return false;
        }
    }
}
