class HierarchicalException {
	public static void main(String[] args) {
		System.out.println(new ExceptionCatcher().test());
	}
}

class ExceptionCatcher {
	public int test() {
		BaseException e;
		int x;

		e = new BaseException();
		x = e.setCode(1);
		x = this.catchException(e);

		e = new IOException();
		x = e.setCode(2);
		x = this.catchException(e);

		e = new FileNotFoundExecption();
		x = e.setCode(3);
		x = this.catchException(e);

		e = new EOFException();
		x = e.setCode(4);
		x = this.catchException(e);

		e = new IllegalArgException();
		x = e.setCode(5);
		x = this.catchException(e);

		return 42;
	}

	public int catchException(BaseException exception) {
		try {
			throw exception;
		} catch (FileNotFoundExecption e) {
			System.out.println(110);
			System.out.println(e.code());
		} catch (IOException e) {
			System.out.println(100);
			System.out.println(e.code());
		} catch (BaseException e) {
			System.out.println(0);
			System.out.println(e.code());
		}
		return 42;
	}
}

class BaseException extends RuntimeException {
	int code;

	public int code() {
		return code;
	}

	public int setCode(int c) {
		code = c;
		return code;
	}
}

class IOException extends BaseException {
	public int setCode(int c) {
		code = 100 + c;
		return code;
	}
}

class FileNotFoundExecption extends IOException {
	public int setCode(int c) {
		code = 110 + c;
		return code;
	}
}

class EOFException extends IOException {
	public int setCode(int c) {
		code = 120 + c;
		return code;
	}
}

class IllegalArgException extends BaseException {
	public int setCode(int c) {
		code = 200 + c;
		return code;
	}
}
