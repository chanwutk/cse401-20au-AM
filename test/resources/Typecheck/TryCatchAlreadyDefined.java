class TryCatchAlreadyDefined {
  public static void main(String[] args) {
    System.out.println(new C1().run());
  }
}

class C1 {
  int a;
  public int run() {
    int b;
    try {

    } catch (RuntimeException a) {
      a = 0;
    }

    try {}
    catch (RuntimeException b) {
      b = 0;
    }
    return b;
  }
}
