class TryCatchAlreadyDefined {
  public static void main(String[] args) {
    System.out.println(new C1().run());
  }
}

class C0 extends RuntimeException {}
class C2 extends C0 {}
class C1 {
  int a;
  public int run() {
    int b;
    try {

    } catch (C0 a) {
      a = 0;
    }

    try {}
    catch (C2 b) {
      b = 0;
    }

    try{}
    catch (C0 c) {
      throw c;
    }

    try{}
    catch (C2 e) {
      throw new C0();
    }

    try{}
    catch (C1 e) {
      throw new C1();
    }

    try{}
    catch (C2 e) {
      throw new C2();
    }

    return b;
  }
}
