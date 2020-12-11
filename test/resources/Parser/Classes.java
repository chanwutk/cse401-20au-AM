class Classes {
	public static void main(String[] args) {
		System.out.println(0);
	}
}

class Ex extends RuntimeException {}

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
		} catch (Ex e) {
			throw new Ex();
		}
		try {
			ret = this.f(this);
			try {
				ret = this.f(this);
			} catch (Ex e) {
				try {
					ret = this.f(this);
				} catch (Ex e) {
					throw new Ex();
				}
			} catch (Ex2 e) {}
		} catch (Ex e) {
			throw e;
		}
		return ret;
	}
}
