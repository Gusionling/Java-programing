public class Ch5_5 {
    //아직 생성자 쓰는거랑 getter, setter쓰는게 익숙하지 않지만 그래도 점점 감 잡고있는 것같다.
    public static void main(String[] args) {
        ColorPoint cp = new ColorPoint(5, 5, "YELLOW");
        cp.setXY(10, 20);
        cp.setColor("RED");
        String str = cp.toString();
        System.out.println(str+"입니다. ");
    }
    //출력결과: RED색의 (10,20)의 점입니다.
}

class Point {
    private int x,y,z;
    public Point(int x, int y){this.x=x; this.y=y;}
    public int getX(){return x;}
    public int getY() {return y;}
    protected void move(int x, int y){this.x=x; this.y=y;}
}

class ColorPoint extends Point{
    private String color;

    //생성자
    public ColorPoint(int x, int y, String color){
        super(x,y);
        this.color= color;
    }

    public void setXY(int x, int y){
        move(x,y);
    }

    public void setColor(String color){
        this.color=color;
    }

    public String toString(){
        return color+"색의"+" ("+getX()+","+getY()+")의 점";
    }
}
