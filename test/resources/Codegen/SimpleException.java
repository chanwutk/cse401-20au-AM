class SimpleException {
	public static void main(String[] args) {
		try {
			System.out.println(new X().f());
		} catch (E e) {
			System.out.println(e.getX());
		}
	}
}

class X {
	public int f() {
		int x;
		E e;
		e = new E();
		x = e.setX(42);
		if (true)
			try {
				try {
					throw e;
				} catch (E e_) {
					throw e_;
				}
			} catch (E e_) {
				try {
					throw e_;
				} catch (E e__) {
					throw e__;
				}
			}
		else {}
		return 0;
	}
}

class E extends RuntimeException {
	int x;

	public int getX() {
		return x;
	}

	public int setX(int ax) {
		x = ax;
		return x;
	}
}
