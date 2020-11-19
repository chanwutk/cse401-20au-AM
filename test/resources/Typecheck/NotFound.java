class NotFound {
	public static void main(String[] args) {
		System.out.println(new X().f(0));
	}
}

class C1 extends Y {
	public int f(int x) {
		return y;
	}
}

class C2 {
	public int f(int x) {
		return this.g(x);
	}
}
