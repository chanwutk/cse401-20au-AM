class UncaughtException {
	public static void main(String[] args) {
		System.out.println(new X().f());
	}
}

class X {
	public int f() {
		if (true)
			throw new E();
		else {}
		return 0;
	}
}

class E extends RuntimeException {}
