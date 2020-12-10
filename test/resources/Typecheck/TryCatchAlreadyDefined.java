class TryCatchAlreadyDefined {
  public static void main(String[] args) {
    System.out.println(new C1().run());
  }
}

class C0 extends RuntimeException {}

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

    try{}
    catch (C0 c) {
      throw c;
    }

    try{}
    catch (RuntimeException e) {
      throw new C0();
    }

    try{}
    catch (C1 e) {
      throw new C1();
    }

    return b;
  }
}
