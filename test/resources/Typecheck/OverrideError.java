class OverrideError {
  public static void main(String[] args) {
    System.out.println(1);
  }
}

class Foo {
  public Foo m1(int n1, Foo n2) {
    return new Foo();
  }
}

class Foo2 extends Foo {
  // incompatible types
  public int m1(int n3, Foo2 n4) {
    return 2;
  }
}


class Foo3 {
  public Foo m1(int n1, Foo n2) {
    return new Foo();
  }
}

class Foo4 extends Foo3 {
  // wrong number of parameters
  public Foo m1(int n3, Foo n4, int a) {
    return new Foo();
  }
}

class Foo5 {
  public Foo m1(int n1, Foo n2) {
    return new Foo();
  }
}

class Foo6 extends Foo5 {
}

class Foo7 extends Foo6 {
  // wrong number of parameters from the base of base
  public Foo m1(int n3, Foo n4, int a) {
    return new Foo();
  }
}
