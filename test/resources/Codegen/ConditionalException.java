class ConditionalException {
	public static void main(String[] args) {
		System.out.println(new ExceptionTester().test());
	}
}

class ExceptionTester {
	public int test() {
		int i;
		int x;
		i = 0;
		x = 0;
		while (i < 10) {
			try {
				x = this.maybeThrow(i);
				System.out.println(0-1);
				System.out.println(i);
			} catch (E e) {
				System.out.println(0-2);
				System.out.println(e.getX());
			}
			i = i + 1;
		}
		return x;
	}

	public int maybeThrow(int i) {
		boolean even;
		E e;
		int x;
		e = new E();
		x = e.setX(i);
		even = true;
		while (0 < i) {
			even = !even;
			i = i - 1;
		}
		if (even)
			throw e;
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
