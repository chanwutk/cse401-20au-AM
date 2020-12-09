class Classes {
	public static void main(String[] args) {
		System.out.println(0);
	}
}

class Base {
	int x;
	public int f(Base b) {
		int y;
		y = x + 5;
		return y;
	}
}

class Derived extends Base {
	public int f(Base b) {
		int ret;
		try {
			ret = this.f(this);
		} catch (Exception e) {
			ret = 0;
		}
		return ret;
	}
}
