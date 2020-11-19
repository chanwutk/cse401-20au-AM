class Cycle {
	public static void main(String[] args) {
		System.out.println(new C1().f(0));
	}
}

class C1 extends C2 {
	public int f(int x) {
		return x;
	}
}

class C2 extends C1 {
	public int f(int x) {
		return x;
	}
}
