class NullPointer {
  public static void main(String[] args) {
    System.out.println(new C().f());
  }
}

class C {
  public int f() {
    C c;
    return c.f();
  }
}
