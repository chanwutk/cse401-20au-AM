class OuterCatchException {
	public static void main(String[] args) {
		System.out.println(new ExceptionCatcher().test());
	}
}

class ExceptionCatcher {
	public int test() {
		int x;
		try {
			System.out.println(1);
			x = this.outerTryCatch();
			System.out.println(2);
			x = this.outerMethodCatch();
			System.out.println(3);
		} catch (BaseException e) {
			System.out.println(4);
		}
		System.out.println(5);
		return 42;
	}

	public int outerTryCatch() {
		try {
			try {
				throw new BaseException();
			} catch (DerivedException e) {
				System.out.println(6);
			}
		} catch (BaseException e) {
			System.out.println(7);
		}
		System.out.println(8);
		return 42;
	}

	public int outerMethodCatch() {
		try {
			throw new BaseException();
		} catch (DerivedException e) {
			System.out.println(9);
		}
		return 42;
	}
}

class BaseException extends RuntimeException {}
class DerivedException extends BaseException {}
