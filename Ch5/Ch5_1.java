public class Ch5_1 {
    public static void main(String[] args) {
        ColorTV myTV = new ColorTV(32,1024);
        myTV.printProperty();
    }
}

class TV {
    //size가 private이다. 결국 getSize를 사용해야한다.
    private int size;
    public TV(int size) {this.size = size;}
    protected int getSize() {
        return size;
    }
}

class ColorTV extends TV {
    private int color;
    // ColorTV colorTV= new ColorTV();
    // getSize를 이용하기 위해 객체를 생성해야하나 싶었다.

    public ColorTV(int size, int color) {
        super(size);
        this.color = color;
    }

    public void printProperty() {
        System.out.println(getSize() + "인치 "+color+"컬러");
    }
}
