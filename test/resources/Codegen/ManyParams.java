class ManyParams {
  public static void main(String[] args) {
    System.out.println(new C1().run(3,5,2,5,6,9,7,8,1,3));
  }
}

class C1 {
  public int run(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, int p10) {
    System.out.println(p1);
    System.out.println(p2);
    System.out.println(p3);
    System.out.println(p4);
    System.out.println(p5);
    System.out.println(p6);
    System.out.println(p7);
    System.out.println(p8);
    System.out.println(p9);
    System.out.println(p10);
    return this.run2(p1,p2,p3,p4,p5,p6,p7,p8,p9);
  }

  public int run2(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9) {
    System.out.println(p1);
    System.out.println(p2);
    System.out.println(p3);
    System.out.println(p4);
    System.out.println(p5);
    System.out.println(p6);
    System.out.println(p7);
    System.out.println(p8);
    System.out.println(p9);
    return 0;
  }
}
