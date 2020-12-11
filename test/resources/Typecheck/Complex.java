class Complex {
	public static void main(String[] args) {
		System.out.println(new C1().f(0));
	}
}

class C1 extends C2 {
	int f;
	C2 x;
	public int f(int x) {
		C2 y;
		y = this;
		return new C2().f(x);
	}
}

class C2 extends RuntimeException {
	C1 x;
	public int f(int i) {
		C2 y;
		try {
			y = x;
		} catch (C2 e) {
		} catch (C1 e) {}
		return x.f(i);
	}
}
