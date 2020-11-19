class WrongType {
	public static void main(String[] args) {
		while (1)
			if (new C3())
				System.out.println(true + 1);
			else
				System.out.println(false && 2);
	}
}

class C1 {
	C1 c1;
	public C1 f(C2 x) {
		c1 = x;
		return x;
	}
}

class C2 extends C1 {
	C2 c2;
	public C2 f(C1 x) {
		c1 = x;
		c2 = x;
		c1 = this;
		return x.f(this);
	}
}

class C3 {
	public int f(int x, int y, int z) {
		int a;
		a = this.f(x, y, z);
		a = this.f(x, y);
		a = this.f(x, y, z, true);
		a = this.f(x, y, false);
		return a;
	}
}
